import org.codehaus.groovy.grails.test.GrailsTestType
import org.codehaus.groovy.grails.test.GrailsTestTypeResult
import org.codehaus.groovy.grails.test.support.GrailsTestTypeSupport

import java.util.concurrent.CompletionService
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.ExecutorService
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


    if (splitNumber) {
        def splitRunner = classLoader.loadClass('grails.plugin.splittest.GrailsTestTypeRunner')
        def splitClass = classLoader.loadClass('grails.plugin.splittest.GrailsSplitTestType')

        //Override runTests in _GrailsTest
        binding.runTests = { GrailsTestType origTestType, File compiledClassesDir ->
            def testCountForPhase = 0
            grailsConsole.addStatus("Running test type ${origTestType.name}")
            Integer totalShards = config.gcvt?.concurrentPhases?."${currentTestPhaseName}"?.shards ?: 1
            //GrailsTestType: ${type.class.name}
//            grailsConsole.addStatus "Running Test Phase: ${currentTestPhaseName} Test type: ${type.name} split number:${splitNumber} of:${totalSplits}"

            //Maybe this is needed
//            if (type instanceof GrailsTestTypeSupport) {
//                type = splitClass.newInstance(type, splitNumber, totalSplits, totalShards)
//                type.overrideSourceFileCollection()
//            }

            if (totalShards > 1) {
                ExecutorService executor = Executors.newFixedThreadPool(totalShards);
                CompletionService completionService = new ExecutorCompletionService(executor);

//                ExecutorService executorService = Executors.newFixedThreadPool(totalShards)

                List futures = []
                (0..< totalShards).each {shardNumber ->
                    shardNumber += 1
                    //Create a new type for each shard
                    def shardType = splitClass.newInstance(origTestType, splitNumber, totalSplits, totalShards)
                    shardType.overrideSourceFileCollection()
                    shardType.totalShards = totalShards
                    shardType.shardNumber = shardNumber

                    def testCountForShard = shardType.prepare(testTargetPatterns, compiledClassesDir, binding)
                    testCountForPhase += testCountForShard
//                    event("TestShardStart", "${shardNumber}")
                    if (testCountForShard) {
                        grailsConsole.addStatus "Running shard ${shardNumber}. $shardType.name Classes: [${shardType.shardTestClasses }]test${testCountForShard > 1 ? 's' : ''}...".padLeft(2)
                        def runner = splitRunner.newInstance()
                        runner.split = splitNumber
                        runner.shard = shardNumber
                        runner.phase = currentTestPhaseName
                        runner.testEventPublisher = testEventPublisher
                        runner.shardTestClasses = shardType.shardTestClasses
                        runner.testType = shardType
                        futures.add(completionService.submit(runner))
                    }
                }

                for (int i=0; i<futures.size(); i++) {
                    def fResult
                    try {
                        fResult = completionService.take().get();
                        grailsConsole.addStatus "Shard complete: ${fResult}"
                    }
                    catch (ExecutionException e) {
                        testsFailed = true
                        grailsConsole.error "Error running tests: ${e.message}", e
                    }
                    if (fResult.failCount > 0) testsFailed = true
                }
                origTestType.cleanup()
                executor.shutdown();
                event("TestSuiteEnd", [origTestType.name])

            }
            //Copy from _GrailsTest
//            else {
//                def testCount = origTestType.prepare(testTargetPatterns, compiledClassesDir, binding)
//                grailsConsole.addStatus "Test Count $testCount "
//                if (testCount) {
//                    try {
//                        event("TestSuiteStart", [origTestType.name])
//                        grailsConsole.addStatus "Running ${testCount} $origTestType.name test${testCount > 1 ? 's' : ''}..."
//
//                        def start = new Date()
//                        def result = origTestType.run(testEventPublisher)
//                        def end = new Date()
//
//                        testCount = result.passCount + result.failCount
//                        grailsConsole.addStatus "Completed $testCount $origTestType.name test${testCount > 1 ? 's' : ''}, ${result.failCount} failed in ${end.time - start.time}ms"
//                        grailsConsole.lastMessage = ""
//
//                        if (result.failCount > 0) testsFailed = true
//                        event("TestSuiteEnd", [origTestType.name])
//
//                    }
//                    catch (e) {
//                        grailsConsole.error "Error running $origTestType.name tests: ${e.message}", e
//                        testsFailed = true
//                    }
//                    finally {
//                        origTestType.cleanup()
//                    }
//                }
//            }
        }
    }
}
