package qsfl.tester.runner;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import qsfl.tester.LeastOf3_Bug2_WithTestRunner;

public class TestRunner {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(LeastOf3_Bug2_WithTestRunner.class);

        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

        System.out.println(result.wasSuccessful());
    }
}
