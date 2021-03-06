package base;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class MainChecker {
    private final Method method;
    public final Random random = new Random(8045702385702345702L);
    private final TestCounter counter = new TestCounter();

    public MainChecker(final String className) {
        try {
            final URL url = new File(".").toURI().toURL();
            final Class<?> clazz = new URLClassLoader(new URL[]{url}).loadClass(className);
            clazz.newInstance();
            method = clazz.getMethod("main", String[].class);
        } catch (final Exception e) {
            throw new AssertionError("Could not found main(String[]) in class "  + className, e);
        }
    }

    public List<String> run(final String... input) {
        counter.nextTest();
        System.err.format("Running test %02d: java %s \"%s\"\n", counter.getTest(), method.getDeclaringClass().getName(), join(input));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            method.invoke(null, new Object[]{input});
            return Arrays.stream(out.toString("UTF-8").split("\n")).map(String::trim).collect(Collectors.toList());
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            System.err.format("Error: %s %s\n", cause.getClass().getSimpleName(), cause.getMessage());
            cause.printStackTrace();
            return null;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String join(final String[] input) {
        final StringBuilder sb = new StringBuilder();
        for (final String s : input) {
            if (sb.length() > 0) {
                sb.append("\" \"");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public void checkEquals(final List<String> expected, final List<String> actual) {
        if (expected.size() != actual.size()) {
            System.err.println(String.format("Expected %d lines, found %d", expected.size(), actual.size()));
        } else {
            for (int i = 0; i < expected.size(); i++) {
                final String exp = expected.get(i);
                final String act = actual.get(i);
                if (!exp.equalsIgnoreCase(act)) {
                    System.err.println(String.format("Line %d: expected '%s' found '%s'", i + 1, exp, act));
                    return;
                }
            }
            counter.passed();
        }
    }

    public void printStatus() {
        counter.printStatus();
    }
}
