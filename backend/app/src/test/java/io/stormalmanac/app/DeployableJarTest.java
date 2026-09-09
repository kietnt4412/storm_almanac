package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What is actually inside {@code storm-almanac.jar}.
 *
 * <p>This is the guard on the development sign-in, and it is a test rather than
 * a comment because the guard is a build file line — {@code :app} depending on
 * {@code :modules:identity-dev} through {@code testAndDevelopmentOnly} rather
 * than {@code implementation} — and one word's difference between the two is
 * invisible in review, breaks nothing, and turns a URL into an account on a
 * public service. Nothing else in the build would notice. This does.
 *
 * <p><b>It looks inside the nested jars, not only at their names.</b> A boot jar
 * carries every dependency as a jar of its own under {@code BOOT-INF/lib}, so a
 * check on entry names would pass just as happily if the classes had been moved
 * into {@code :modules:identity} — which is the mistake most likely to be made
 * by someone who found the split inconvenient. Every archive is opened and every
 * class name in it is read.
 *
 * <p><b>Both halves are asserted.</b> An absence test that looks in the wrong
 * place passes, silently, forever. So the same scan that must not find the
 * development sign-in must find {@code SecurityConfig} — a class that is
 * unquestionably in the product — and the assertion that it does is what makes
 * the other one mean something.
 *
 * <p>The jar path arrives as a system property set in {@code build.gradle.kts},
 * which also makes {@code test} depend on {@code bootJar}. Guessing the path
 * from the working directory would make this class the one test that fails for a
 * reason having nothing to do with the application.
 */
class DeployableJarTest {

    private static final String DEVELOPMENT_SIGN_IN = "io/stormalmanac/devsignin/";
    private static final String IN_THE_PRODUCT = "io/stormalmanac/identity/SecurityConfig.class";

    private static List<String> classes;

    @BeforeAll
    static void readTheJar() throws IOException {
        Path jar = Path.of(System.getProperty(
                "storm-almanac.deployable-jar",
                "build/libs/storm-almanac.jar"));
        assertThat(jar)
                .as("the deployable jar, which `test` depends on `bootJar` to produce")
                .exists();
        classes = everyClassIn(jar);
        // A boot jar of this application is thousands of entries. A handful
        // would mean the scan below is reading something other than what it
        // thinks it is, and every assertion after it would be vacuous.
        assertThat(classes).as("class entries found in %s", jar).hasSizeGreaterThan(1000);
    }

    @Test
    @DisplayName("the deployable jar contains no development sign-in")
    void carriesNoDevelopmentSignIn() {
        assertThat(classes)
                .as("classes under %s, which `testAndDevelopmentOnly` must keep out of bootJar", DEVELOPMENT_SIGN_IN)
                .noneMatch(name -> name.startsWith(DEVELOPMENT_SIGN_IN));
    }

    @Test
    @DisplayName("and the scan that says so is looking in the right place")
    void findsTheProductItIsScanning() {
        assertThat(classes).contains(IN_THE_PRODUCT);
    }

    /**
     * Every class in the archive, including inside each nested dependency jar.
     *
     * @return entry names in {@code a/b/C.class} form, whichever archive they
     *         came from — where a class sits is exactly what is not being
     *         trusted here
     */
    private static List<String> everyClassIn(Path jar) throws IOException {
        List<String> found = new ArrayList<>();
        try (JarFile archive = new JarFile(jar.toFile())) {
            Enumeration<JarEntry> entries = archive.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    // BOOT-INF/classes/io/... -> io/...
                    found.add(name.startsWith("BOOT-INF/classes/")
                            ? name.substring("BOOT-INF/classes/".length())
                            : name);
                } else if (name.endsWith(".jar")) {
                    try (InputStream raw = archive.getInputStream(entry);
                            JarInputStream nested = new JarInputStream(raw)) {
                        for (JarEntry inner = nested.getNextJarEntry();
                                inner != null;
                                inner = nested.getNextJarEntry()) {
                            if (inner.getName().endsWith(".class")) {
                                found.add(inner.getName());
                            }
                        }
                    }
                }
            }
        }
        return found;
    }
}
