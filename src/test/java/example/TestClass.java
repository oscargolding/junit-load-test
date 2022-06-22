package example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import junit.framework.TestCase;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.ArrayList;
import java.util.List;

public class TestClass {

    public static void main(String[] args) {
        Launcher sesion = LauncherFactory.create();
        Runnable runnable = () -> testRunner(sesion);
        final List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
            threadList.add(thread);
            System.out.println("Thread started");
        }
        threadList.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("All done :)");
    }

    private static void testRunner(Launcher session) {
        for (int i = 0; i < 500; i++) {
            testExecutor(session);
        }
        System.out.println("FinishedRunning!!!!");
    }

    private static void testExecutor(Launcher session) {
        Launcher sesion = LauncherFactory.create();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectPackage("example"),
                        selectClass(TestCase.class)
                )
                .build();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        sesion.registerTestExecutionListeners(listener);
        sesion.execute(request);
        TestExecutionSummary summary = listener.getSummary();
        System.out.printf("Number of tests found %d%n", summary.getTestsFoundCount());
        System.out.printf("Number of tests passed %d%n", summary.getTestsSucceededCount());
    }

    @org.junit.jupiter.api.Test
    public void alwaysTestsTrue() throws InterruptedException {
        Thread.sleep(100);
        System.out.println("Test execution");
        assertEquals(1, 1);
    }
}
