# Stdout and Stderr logs for failing tests

## What is the problem?

We have thousands of unit tests within our gradle project.
Most of the time all are green. But sometimes some are failing.
The default info is pretty useless (Which test is failing and few lines of stack)
```
ExampleTest > test_2_failing FAILED
    java.lang.AssertionError at ExampleTest.java:20
```

With some better config you get more infos that's ok - at least for very simple tests.

For complexer tests it is pretty importent to get the logs to see what happened right
before the failure.

The usual way is to use `--debug`. But you'll have to start the whole thing again and
you'll get so many logs... **but actually we only want to see the logs of the failing tests**  

## How to achive this?

Paste this in your `build.gradle`:
```
// Test-Logging
project.test {
    def outputCache = new LinkedList<String>()
    
    beforeTest { TestDescriptor td -> outputCache.clear() }    // clear everything right before the test starts
    
    onOutput { TestDescriptor td, TestOutputEvent toe ->       // when output is coming put it in the cache
        outputCache.add(toe.getMessage())
        while (outputCache.size() > 1000) outputCache.remove() // if we have more than 1000 lines -> drop first
    }

    /** after test -> decide what to print */
    afterTest { TestDescriptor td, TestResult tr ->
        if (tr.resultType == TestResult.ResultType.FAILURE && outputCache.size() > 0) {
            println()
            println(" Output of ${td.className}.${td.name}:")
            outputCache.each { print(" > $it") }
        }
    }
}

tasks.withType(Test) {
    reports.html.enabled = false
    testLogging {
        events /*'started',*/ 'passed', 'failed'
        exceptionFormat = 'full'
        afterSuite { desc, result -> if (!desc.parent) println "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)" }
    }
}
```

The solution was by chris_yones_yar and found here: https://discuss.gradle.org/t/show-stderr-for-failed-tests/8463/7

## What is the result?

```
╰─$ gradle test                                                                                                                                                                                                                                                                              1 ↵

> Task :test FAILED

 Output of ExampleTest.test_3_exception:
 > test_3_failing: line 1
 > test_3_failing: line 2
 > test_3_failing: line 3
 > and line 3

ExampleTest > test_3_exception FAILED
    java.lang.IllegalStateException: Let's throw an exception and see if we get the logs
        at ExampleTest.test_3_exception(ExampleTest.java:30)

ExampleTest > test_1_ok PASSED

ExampleTest > test_4_ok PASSED

 Output of ExampleTest.test_2_failing:
 > test_2_failing: line 1
 > test_2_failing: line 2

ExampleTest > test_2_failing FAILED
    java.lang.AssertionError: Let's fail the test and see if we get the logs
        at org.junit.Assert.fail(Assert.java:88)
        at ExampleTest.test_2_failing(ExampleTest.java:20)
Results: FAILURE (4 tests, 2 successes, 2 failures, 0 skipped)

4 tests completed, 2 failed


FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':test'.
> There were failing tests. See the results at: file:///Users/calliduslynx/Workspace/gradle-log-on-failure/build/test-results/test/

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

BUILD FAILED in 1s
```

## Old Situation

### `gradle test`
```
> Task :test FAILED

ExampleTest > test_3_exception FAILED
    java.lang.IllegalStateException at ExampleTest.java:29

ExampleTest > test_2_failing FAILED
    java.lang.AssertionError at ExampleTest.java:20

4 tests completed, 2 failed
```

### `gradle test with stack for failing tests`

```
=== build.gradle ===
tasks.withType(Test) {
    testLogging {
        exceptionFormat = 'full'
    }
}
```

will look like

```
> Task :test FAILED

ExampleTest > test_3_exception FAILED
    java.lang.IllegalStateException: Let's throw an exception and see if we get the logs
        at ExampleTest.test_3_exception(ExampleTest.java:29)

ExampleTest > test_2_failing FAILED
    java.lang.AssertionError: Let's fail the test and see if we get the logs
        at org.junit.Assert.fail(Assert.java:88)
        at ExampleTest.test_2_failing(ExampleTest.java:20)

4 tests completed, 2 failed


FAILURE: Build failed with an exception.
```

### `gradle test --debug`

```
11:35:20.578 [DEBUG] [TestEventLogger]
11:35:20.578 [DEBUG] [TestEventLogger] ExampleTest > test_4_ok STARTED
11:35:20.578 [DEBUG] [TestEventLogger]
11:35:20.578 [DEBUG] [TestEventLogger] ExampleTest > test_4_ok STANDARD_OUT
11:35:20.578 [DEBUG] [TestEventLogger]     test_4_ok: line 1
11:35:20.578 [DEBUG] [TestEventLogger]     test_4_ok: line 2
11:35:20.579 [DEBUG] [TestEventLogger]     test_4_ok: line 3
11:35:20.579 [DEBUG] [TestEventLogger]
11:35:20.579 [DEBUG] [TestEventLogger] ExampleTest > test_4_ok PASSED
11:35:20.579 [DEBUG] [TestEventLogger]
11:35:20.579 [DEBUG] [TestEventLogger] ExampleTest > test_2_failing STARTED
11:35:20.579 [DEBUG] [TestEventLogger]
11:35:20.579 [DEBUG] [TestEventLogger] ExampleTest > test_2_failing STANDARD_OUT
11:35:20.579 [DEBUG] [TestEventLogger]     test_2_failing: line 1
11:35:20.579 [DEBUG] [TestEventLogger]     test_2_failing: line 2
11:35:20.579 [DEBUG] [TestEventLogger]
11:35:20.579 [DEBUG] [TestEventLogger] ExampleTest > test_2_failing FAILED
11:35:20.580 [DEBUG] [TestEventLogger]     java.lang.AssertionError: Let's fail the test and see if we get the logs
11:35:20.580 [DEBUG] [TestEventLogger]         at org.junit.Assert.fail(Assert.java:88)
11:35:20.580 [DEBUG] [TestEventLogger]         at ExampleTest.test_2_failing(ExampleTest.java:20)
...
11:35:20.584 [DEBUG] [TestEventLogger]         at java.lang.Thread.run(Thread.java:748)
11:35:20.584 [DEBUG] [TestEventLogger]
11:35:20.584 [DEBUG] [TestEventLogger] ExampleTest FAILED
... (more then 1000 lines)
```

### `gradle test --stacktrace`

```
> Task :test FAILED

ExampleTest > test_3_exception FAILED
    java.lang.IllegalStateException at ExampleTest.java:29

ExampleTest > test_2_failing FAILED
    java.lang.AssertionError at ExampleTest.java:20

4 tests completed, 2 failed


FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':test'.
> There were failing tests. See the report at: file:///Users/calliduslynx/Workspace/gradle-log-on-failure/build/reports/tests/test/index.html

* Try:
Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Exception is:
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':test'.
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeActions(ExecuteActionsTaskExecuter.java:100)
(... very much useless lines)
```