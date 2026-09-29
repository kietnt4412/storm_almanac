package io.stormalmanac.store;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * The store against a reference model: a {@link TreeMap} ordered the same way,
 * driven by the same random operations, must give the same answers after every
 * one — including after the store is closed and reopened, which replays its log.
 */
class LsmStoreModelProperties {

    sealed interface Op {
        record Put(byte[] key, byte[] value) implements Op {
            @Override
            public String toString() {
                return "put(" + Arrays.toString(key) + ", " + value.length + " bytes)";
            }
        }

        record Delete(byte[] key) implements Op {
            @Override
            public String toString() {
                return "delete(" + Arrays.toString(key) + ")";
            }
        }

        record Reopen() implements Op {}
    }

    @Property(tries = 200)
    @Label("Any sequence of puts, deletes and reopens reads back exactly as a sorted map would")
    void matchesTheModel(@ForAll("operations") List<Op> operations) throws IOException {
        Path dir = Files.createTempDirectory("lsm-model");
        TreeMap<byte[], byte[]> model = new TreeMap<>(Arrays::compareUnsigned);
        LsmStore store = LsmStore.open(dir, StoreOptions.defaults().withDurability(Durability.OS_BUFFERED));
        try {
            for (Op op : operations) {
                switch (op) {
                    case Op.Put put -> {
                        store.put(put.key(), put.value());
                        model.put(put.key(), put.value());
                    }
                    case Op.Delete delete -> {
                        store.delete(delete.key());
                        model.remove(delete.key());
                    }
                    case Op.Reopen reopen -> {
                        store.close();
                        store = LsmStore.open(dir, StoreOptions.defaults().withDurability(Durability.OS_BUFFERED));
                    }
                }
                assertAgrees(store, model);
            }
        } finally {
            store.close();
            deleteRecursively(dir);
        }
    }

    private static void assertAgrees(LsmStore store, TreeMap<byte[], byte[]> model) {
        List<Map.Entry<byte[], byte[]>> scanned = new ArrayList<>();
        store.scan(null, null).forEachRemaining(kv -> scanned.add(Map.entry(kv.key(), kv.value())));
        assertThat(scanned).hasSameSizeAs(model.entrySet());
        int i = 0;
        for (Map.Entry<byte[], byte[]> expected : model.entrySet()) {
            assertThat(scanned.get(i).getKey()).isEqualTo(expected.getKey());
            assertThat(scanned.get(i).getValue()).isEqualTo(expected.getValue());
            assertThat(store.get(expected.getKey())).hasValueSatisfying(v -> assertThat(v).isEqualTo(expected.getValue()));
            i++;
        }
        // A bounded scan must agree with the model's own sub-map.
        if (!model.isEmpty()) {
            byte[] from = model.firstKey();
            byte[] to = model.lastKey();
            List<byte[]> bounded = new ArrayList<>();
            store.scan(from, to).forEachRemaining(kv -> bounded.add(kv.key()));
            assertThat(bounded).containsExactlyElementsOf(new ArrayList<>(model.subMap(from, true, to, false).keySet()));
        }
    }

    @Provide
    Arbitrary<List<Op>> operations() {
        // A small key space, so overwrites and deletes of live keys are common;
        // bytes span the whole range, so unsigned ordering is exercised.
        Arbitrary<byte[]> keys = Arbitraries.bytes().array(byte[].class).ofMinSize(0).ofMaxSize(3)
                .filter(k -> k.length == 0 || (k[0] & 0x0F) < 4);
        Arbitrary<byte[]> values = Arbitraries.bytes().array(byte[].class).ofMaxSize(64);
        Arbitrary<Op> put = Combinators.combine(keys, values).as(Op.Put::new);
        Arbitrary<Op> delete = keys.map(Op.Delete::new);
        Arbitrary<Op> reopen = Arbitraries.just(new Op.Reopen());
        return Arbitraries.frequencyOf(
                        net.jqwik.api.Tuple.of(6, put),
                        net.jqwik.api.Tuple.of(3, delete),
                        net.jqwik.api.Tuple.of(1, reopen))
                .list().ofMaxSize(60);
    }

    private static void deleteRecursively(Path dir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
