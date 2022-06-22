package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    /**
     * Sample method.
     * @return a placeholder string
     */
    public String sampleMethod() {
        return "sampleMethod() called!";
    }

    public static class TestRunner implements Runnable {

        private long passedCount = 0;
        private long runCount = 0;

        @Override
        public void run() {
            long[] usingResults = testRunner();
            this.passedCount = usingResults[0];
            this.runCount = usingResults[1];
        }

        public long getTestsRun() {
            return this.runCount;
        }

        public long getTestPassed() {
            return this.passedCount;
        }

    }

    public static void main(String[] args) {
        Main runner = new Main();
        runner.runningTest();
    }

    public void runningTest() {
        final Instant startInstant = Instant.now();
        final List<Thread> threadList = new ArrayList<>();
        final List<TestRunner> testRunner = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            final TestRunner testRunnerInstance = new TestRunner();
            Thread thread = new Thread(testRunnerInstance);
            thread.start();
            threadList.add(thread);
            testRunner.add(testRunnerInstance);
            System.out.printf("Thread Started %d/300%n", i + 1);
        }
        AtomicLong runCount = new AtomicLong();
        AtomicLong passedCount = new AtomicLong();
        threadList.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        testRunner.forEach(runner -> {
            runCount.addAndGet(runner.getTestsRun());
            passedCount.addAndGet(runner.getTestPassed());
        });
        final Instant endInstant = Instant.now();
        writeToFile(startInstant, endInstant);
        System.out.println("All done :)");
        System.out.printf("Tests Run: %d%n", runCount.get());
        System.out.printf("Tests Passed: %d%n", passedCount.get());
    }

    private static void writeToFile(final Instant startTime, final Instant endTime) {
        try (FileWriter myWriter = new FileWriter(String.format("test-time-%s.txt", Instant.now().toString()))) {
            myWriter.write(String.format("Total time spent: %d", endTime.toEpochMilli() - startTime.toEpochMilli()));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long[] testRunner() {
        long[] tupleResult = {0, 0};
        for (int i = 0; i < 500; i++) {
            long[] newResults = testExecutor();
            tupleResult[0] += newResults[0];
            tupleResult[1] += newResults[1];
        }
        return tupleResult;
    }

    private static long[] testExecutor() {
        Launcher session = LauncherFactory.create();
        long[] tupleResult = {0, 0};
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectPackage("org.example"),
                        selectClass(Main.class)
                ).filters(
                        includeClassNamePatterns(".*Main")
                ).
                build();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        session.registerTestExecutionListeners(listener);
        session.execute(request);
        TestExecutionSummary summary = listener.getSummary();
        tupleResult[0] = summary.getTestsFoundCount();
        tupleResult[1] = summary.getTestsSucceededCount();
        return tupleResult;
    }

    @org.junit.jupiter.api.Test
    public void alwaysTestsTrue() throws InterruptedException {
        // Simulate a network request
        Thread.sleep(100);
        assertEquals(1, 1);
    }
}