import org.codehaus.groovy.grails.test.GrailsTestType
import org.codehaus.groovy.grails.test.GrailsTestTypeResult
import org.codehaus.groovy.grails.test.support.GrailsTestTypeSupport

import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.Future

includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsRun")
includeTargets << grailsScript("_GrailsSettings")
includeTargets << grailsScript("_GrailsClean")

eventTestPhaseStart = {args ->
    grailsConsole.addStatus("Running test phase ${args}")
}
eventTestPhasesStart = { args ->
    Integer splitNumber = 1//(testOptions.split ? Integer.valueOf(testOptions.split.value) : 1)
    Integer totalSplits = 1//(testOptions.totalSplits ? Integer.valueOf(testOptions.totalSplits.value) : 1)

    def testCount

    if (splitNumber) {
        def splitRunner = classLoader.loadClass('grails.plugin.splittest.GrailsTestTypeRunner')
        def splitClass = classLoader.loadClass('grails.plugin.splittest.GrailsSplitTestType')

        //Override runTests in _GrailsTest
        binding.runTests = { GrailsTestType type, File compiledClassesDir ->
            Integer totalShards = config.gcvt?.concurrentPhases?."${currentTestPhaseName}"?.shards ?: 1
            //GrailsTestType: ${type.class.name}
            grailsConsole.addStatus "Running Test Phase: ${currentTestPhaseName} Test type: ${type.name} split number:${splitNumber} of:${totalSplits}"
            if (type instanceof GrailsTestTypeSupport) {
                type = splitClass.newInstance(type, splitNumber, totalSplits, totalShards)
                type.overrideSourceFileCollection()
            }
            testCount = type.prepare(testTargetPatterns, compiledClassesDir, binding)
            if (type.totalShards > 1) {

                def pool = Executors.newFixedThreadPool(type.totalShards)
                ExecutorCompletionService executorCompletionService = new ExecutorCompletionService(pool);
                List<Future<?>> futures = new ArrayList<Future<?>>()



                (0..< type.totalShards).each {shardNumber ->
                    shardNumber += 1
                    type.shardNumber = shardNumber
                    testCount = type.prepare(testTargetPatterns, compiledClassesDir, binding)
                    grailsConsole.addStatus "Running shard ${shardNumber}. Test count ${testCount} $type.name test${testCount > 1 ? 's' : ''}...".padLeft(2)
//                    event("TestShardStart", "${shardNumber}")

                    if (testCount) {
                        type.shardNumber == shardNumber
                        def runner = splitRunner.newInstance()
                        runner.split = splitNumber
                        runner.shard = shardNumber
                        runner.phase = currentTestPhaseName
                        runner.testEventPublisher = testEventPublisher
                        runner.testType = type
                        futures.add(executorCompletionService.submit(runner))
                    }
                }

                futures.each {Future f ->
                    grailsConsole.addStatus "Shard complte"
                    def result
                    try {
                        result = executorCompletionService.take().get()
                        grailsConsole.addStatus "${result.toString()}"
                    }
                    catch (ExecutionException e) {
                        grailsConsole.error "Error running $type.name tests: ${e.message}", e
                        testsFailed = true
                    }
                    if (result.failCount > 0) testsFailed = true
                    event("TestSuiteEnd", [type.name])
                }
                pool.shutdown();




            } else {
                testCount = type.prepare(testTargetPatterns, compiledClassesDir, binding)
                grailsConsole.addStatus "Test Count $testCount "
                if (testCount) {
                    try {
                        event("TestSuiteStart", [type.name])
                        grailsConsole.addStatus "Running ${testCount} $type.name test${testCount > 1 ? 's' : ''}..."

                        def start = new Date()
                        def result = type.run(testEventPublisher)
                        def end = new Date()

                        testCount = result.passCount + result.failCount
                        grailsConsole.addStatus "Completed $testCount $type.name test${testCount > 1 ? 's' : ''}, ${result.failCount} failed in ${end.time - start.time}ms"
                        grailsConsole.lastMessage = ""

                        if (result.failCount > 0) testsFailed = true
                        event("TestSuiteEnd", [type.name])

                    }
                    catch (e) {
                        grailsConsole.error "Error running $type.name tests: ${e.message}", e
                        testsFailed = true
                    }
                    finally {
                        type.cleanup()
                    }
                }
            }
        }
    }
}
