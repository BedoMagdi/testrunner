package edu.illinois.cs.testrunner.execution;

import edu.illinois.cs.testrunner.data.results.TestResult;
import edu.illinois.cs.testrunner.data.results.TestResultFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

// This class is used to track result of each tests during execution.
public class JUnit5TestListener implements TestExecutionListener {

    // key is the full qualified method name
    private Map<String, TestResult> results;
    private Map<String, Long> startTimesMillis;
    private List<String> testOrder;  // a list of full qualified method names

    public JUnit5TestListener() {
        this.results = new HashMap<>();
        this.testOrder = new ArrayList<>();
        this.startTimesMillis = new HashMap<>();
    }

    public Map<String, TestResult> getResults() {
        return this.results;
    }

    public List<String> getTestOrder() {
        return this.testOrder;
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testplan) {
        // this listener doesn't reset for new TestPlan
    }

    @Override
    public void executionSkipped(TestIdentifier identifier, String reason) {
        if (!identifier.isTest()) {
            return;
        }
        String fullyQualifiedName = Utils.toFullyQualifiedName(
                identifier.getUniqueId());
        TestResult result = TestResultFactory.ignored(fullyQualifiedName);
        this.results.put(fullyQualifiedName, result);
        this.testOrder.add(fullyQualifiedName);
    }

    @Override
    public void executionStarted(TestIdentifier identifier) {
        if (!identifier.isTest()) {
            return;
        }

        this.startTimesMillis.put(
                Utils.toFullyQualifiedName(identifier.getUniqueId()),
                System.currentTimeMillis());
    }

    @Override
    public void executionFinished(TestIdentifier identifier,
                                  TestExecutionResult executionResult) {
        if (!identifier.isTest()) {
            return;
        }

        String fullyQualifiedName = Utils.toFullyQualifiedName(
                identifier.getUniqueId());
        double runtime = ( System.currentTimeMillis() -
                this.startTimesMillis.get(fullyQualifiedName)) / 1E3;
        this.testOrder.add(fullyQualifiedName);
        if (executionResult.getStatus() == Status.FAILED) {
            executionResult.getThrowable().get().printStackTrace();
            this.results.put(
                    fullyQualifiedName,
                    TestResultFactory.failOrError(
                            executionResult.getThrowable().get(),
                            runtime,
                            fullyQualifiedName));
        } else {
            // passed
            this.results.put(fullyQualifiedName,
                    TestResultFactory.passing(runtime,
                            fullyQualifiedName));
        }
    }

}