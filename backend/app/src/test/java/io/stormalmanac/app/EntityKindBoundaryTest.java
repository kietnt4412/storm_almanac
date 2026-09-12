package io.stormalmanac.app;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.lang.conditions.ArchConditions.accessTargetWhere;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.stormalmanac.gamedata.catalog.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code Entity.kind} routes a catalog page and validates an ingest, and it
 * decides no behaviour anywhere else.
 *
 * <p>This closes the gap ADR 0007 recorded against itself. That ADR made
 * equipment an {@code Entity} rather than a third top-level concept and paid for
 * it with one opaque, game-supplied string; its own words are that
 * {@code planner}, {@code gacha} and {@code stats} <em>must never read</em> it,
 * because the moment behaviour depends on the distinction, the distinction was
 * real and the ADR was wrong. It then said the constraint was asserted and not
 * enforced, and deferred the rule on the grounds that the guarded modules were
 * empty and it would pass vacuously. {@code planner} is now about two thousand
 * lines across a resolver, a MIP and an optimizer, so the deferral has expired.
 *
 * <p>It is a bytecode rule and not an extension of {@link GameAgnosticismTest}'s
 * source scan on purpose. A scan for the text {@code kind()} cannot tell
 * {@code entity.kind()} from {@code change.kind()} — the diff module has one,
 * and {@code GameDataReadModel} reads both within a hundred lines of each other
 * — so the grep either misses the read or cries wolf on a sibling. ArchUnit
 * resolves the declaring type, which is the whole question here.
 *
 * <p><b>Both spellings of the read were put back into {@code planner} and this
 * rule watched to fail on each</b> — {@code e.kind()} and the one a stream
 * pipeline reaches for, {@code .map(Entity::kind)}. A method reference is filed
 * by ArchUnit as a {@code JavaMethodReference} rather than a call, so the second
 * looked like a hole worth a second condition; it is not one, because
 * {@code getAccessesFromSelf}, which {@code accessTargetWhere} walks, carries
 * references too. Measured rather than assumed, and written down here so the
 * next reader does not add the condition this one deleted.
 *
 * <p>The allowlist is by module rather than by class, because the module is the
 * boundary this project enforces everywhere else ({@link ModuleBoundaryTest}),
 * and because "the catalog surface and ingest validation" is not a line the
 * package structure draws finely enough to be worth guessing at.
 */
class EntityKindBoundaryTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("io.stormalmanac");

    /**
     * Where a {@code kind} legitimately lives: the model that defines it and
     * writes it back out, the read model that renders and routes a catalog page,
     * and the adapter that has to produce one from somebody else's shape.
     */
    private static final String[] CATALOG_AND_INGEST = {
        "io.stormalmanac.gamedata..", "io.stormalmanac.api..", "io.stormalmanac.adapters.."
    };

    @Test
    @DisplayName("Entity.kind is read by the catalog and by ingest, and nowhere else")
    void kindIsNotABehaviouralDiscriminator() {
        noClasses()
                .that()
                .resideOutsideOfPackages(CATALOG_AND_INGEST)
                .should(accessTargetWhere(entityKind()))
                .because(
                        "ADR 0007 bought one model for characters and equipment with an opaque string; "
                                + "a read of it outside the catalog is the evidence that the distinction "
                                + "was behavioural after all, and supersedes that ADR rather than being "
                                + "patched around")
                .check(CLASSES);
    }

    private static DescribedPredicate<JavaAccess<?>> entityKind() {
        return describe(
                "Entity.kind()",
                access -> Entity.class.getName().equals(access.getTargetOwner().getName())
                        && "kind".equals(access.getTarget().getName()));
    }
}
