package io.stormalmanac.store;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LsmStoreTest {

    @TempDir
    Path dir;

    private static byte[] b(String s) {
        return s.getBytes(UTF_8);
    }

    private static List<String> keys(Iterator<KeyValue> entries) {
        List<String> keys = new ArrayList<>();
        entries.forEachRemaining(kv -> keys.add(new String(kv.key(), UTF_8)));
        return keys;
    }

    @Test
    @DisplayName("A put is read back, an overwrite replaces it, and a delete hides it")
    void putOverwriteDelete() {
        try (LsmStore store = LsmStore.open(dir)) {
            store.put(b("k"), b("one"));
            assertThat(store.get(b("k"))).hasValueSatisfying(v -> assertThat(v).isEqualTo(b("one")));

            store.put(b("k"), b("two"));
            assertThat(store.get(b("k"))).hasValueSatisfying(v -> assertThat(v).isEqualTo(b("two")));

            store.delete(b("k"));
            assertThat(store.get(b("k"))).isEmpty();
            assertThat(store.get(b("never written"))).isEmpty();
        }
    }

    @Test
    @DisplayName("A scan returns live keys in unsigned byte order inside its bounds, skipping deletes")
    void scanOrderAndBounds() {
        try (LsmStore store = LsmStore.open(dir)) {
            store.put(new byte[] {(byte) 0xFF}, b("high"));
            store.put(new byte[] {0x01}, b("low"));
            for (String key : List.of("a", "b", "c", "d")) store.put(b(key), b(key));
            store.delete(b("c"));

            List<byte[]> all = new ArrayList<>();
            store.scan(null, null).forEachRemaining(kv -> all.add(kv.key()));
            // Signed comparison would put 0xFF (-1) first; the tree's order is unsigned.
            assertThat(all.getFirst()).containsExactly(0x01);
            assertThat(all.getLast()).containsExactly(0xFF);

            assertThat(keys(store.scan(b("b"), b("d")))).containsExactly("b");
            assertThat(keys(store.scan(b("b"), null))).containsExactly("b", "d", "�");
        }
    }

    @Test
    @DisplayName("Reopening a directory recovers every acknowledged write from its log")
    void reopenRecovers() {
        try (LsmStore store = LsmStore.open(dir)) {
            store.put(b("kept"), b("1"));
            store.put(b("gone"), b("2"));
            store.delete(b("gone"));
            store.put(b("kept"), b("3"));
        }
        try (LsmStore store = LsmStore.open(dir)) {
            assertThat(store.recovered().records()).isEqualTo(4);
            assertThat(store.get(b("kept"))).hasValueSatisfying(v -> assertThat(v).isEqualTo(b("3")));
            assertThat(store.get(b("gone"))).isEmpty();
        }
    }

    @Test
    @DisplayName("A directory already open is refused a second time, and a closed store refuses use")
    void oneOwnerAndNoUseAfterClose() {
        LsmStore store = LsmStore.open(dir);
        assertThatThrownBy(() -> LsmStore.open(dir))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already open");
        store.close();

        assertThatThrownBy(() -> store.put(b("k"), b("v"))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> store.get(b("k"))).isInstanceOf(IllegalStateException.class);
        try (LsmStore again = LsmStore.open(dir)) {
            assertThat(again.get(b("k"))).isEmpty();
        }
    }

    @Test
    @DisplayName("A caller changing its arrays after a put or a read cannot change what the store holds")
    void arraysAreCopied() {
        try (LsmStore store = LsmStore.open(dir)) {
            byte[] key = b("k");
            byte[] value = b("v");
            store.put(key, value);
            key[0] = 'x';
            value[0] = 'x';
            store.get(b("k")).orElseThrow()[0] = 'y';

            assertThat(store.get(b("k"))).hasValueSatisfying(v -> assertThat(v).isEqualTo(b("v")));
        }
    }
}
