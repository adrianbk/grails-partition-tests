
//eventTewwwstPhasesStart = { args ->
//    Integer splitNumber = 1//(testOptions.split ? Integer.valueOf(testOptions.split.value) : 1)
//    Integer totalSplits = 1//(testOptions.totalSplits ? Integer.valueOf(testOptions.totalSplits.value) : 1)
//    Integer totalShards = 2
//
////    Integer totalShards = config.gcvt?.concurrentPhases?."${currentTestPhaseName}"?.shards ?: 1
//
//    if (splitNumber) {
//        def splitRunner = classLoader.loadClass('grails.plugin.splittest.GrailsTestTypeRunner')
//        def splitClass = classLoader.loadClass('grails.plugin.splittest.GrailsSplitTestType')
//
//        //Override runTests in _GrailsTest
//        binding.runTests = { GrailsTestType origTestType, File compiledClassesDir ->
//            def testCountForPhase = 0
//            grailsConsole.addStatus("Running Test type(${origTestType.name}) in Phase(${currentTestPhaseName})")
//
//            //GrailsTestType: ${type.class.name}
////            grailsConsole.addStatus "Running Test Phase: ${currentTestPhaseName} Test type: ${type.name} split number:${splitNumber} of:${totalSplits}"
//
//            //Maybe this is needed
////            if (type instanceof GrailsTestTypeSupport) {
////                type = splitClass.newInstance(type, splitNumber, totalSplits, totalShards)
////                type.overrideSourceFileCollection()
////            }
//
//            if (totalShards > 0) {
//                grailsConsole.addStatus("Splitting into ${totalShards} Shard(s)")
//                ExecutorService executor = Executors.newFixedThreadPool(totalShards);
//                CompletionService completionService = new ExecutorCompletionService(executor);
//
////                ExecutorService executorService = Executors.newFixedThreadPool(totalShards)
//
//                List futures = []
//                (0..< totalShards).each {shardNumber ->
//                    shardNumber += 1
//                    //Create a new type for each shard
//                    def shardType = splitClass.newInstance(origTestType, splitNumber, totalSplits, totalShards, shardNumber)
//
//                    def testCountForShard = shardType.prepare(testTargetPatterns, compiledClassesDir, binding)
//                    testCountForPhase += testCountForShard
////                    event("TestShardStart", "${shardNumber}")
//                    if (testCountForShard) {
//                        grailsConsole.addStatus "Created Shard Test Type (GrailsSplitTestType) ${shardType}"
//                        def runner = splitRunner.newInstance()
//                        runner.split = splitNumber
//                        runner.shard = shardNumber
//                        runner.phase = currentTestPhaseName
//                        runner.testEventPublisher = testEventPublisher
//                        runner.shardTestClasses = shardType.shardTestClasses
//                        runner.testType = shardType
//                        futures.add(completionService.submit(runner))
//                    }
//                }
//
//                for (int i=0; i<futures.size(); i++) {
//                    def fResult
//                    try {
//                        fResult = completionService.take().get();
//                        grailsConsole.addStatus "${new Date().format("yyyy-MM-dd HH:mm:ss SSS")} Shard complete: ${fResult}"
//                    }
//                    catch (ExecutionException e) {
//                        testsFailed = true
//                        grailsConsole.error "Error running tests: ${e.message}", e
//                    }
//                    if (fResult.failCount > 0) testsFailed = true
//                }
//                origTestType.cleanup()
//                executor.shutdown();
//                event("TestSuiteEnd", [origTestType.name])
//
//            }
//            //Copy from _GrailsTest
////            else {
////                def testCount = origTestType.prepare(testTargetPatterns, compiledClassesDir, binding)
////                grailsConsole.addStatus "Test Count $testCount "
////                if (testCount) {
////                    try {
////                        event("TestSuiteStart", [origTestType.name])
////                        grailsConsole.addStatus "Running ${testCount} $origTestType.name test${testCount > 1 ? 's' : ''}..."
////
////                        def start = new Date()
////                        def result = origTestType.run(testEventPublisher)
////                        def end = new Date()
////
////                        testCount = result.passCount + result.failCount
////                        grailsConsole.addStatus "Completed $testCount $origTestType.name test${testCount > 1 ? 's' : ''}, ${result.failCount} failed in ${end.time - start.time}ms"
////                        grailsConsole.lastMessage = ""
////
////                        if (result.failCount > 0) testsFailed = true
////                        event("TestSuiteEnd", [origTestType.name])
////
////                    }
////                    catch (e) {
////                        grailsConsole.error "Error running $origTestType.name tests: ${e.message}", e
////                        testsFailed = true
////                    }
////                    finally {
////                        origTestType.cleanup()
////                    }
////                }
////            }
//        }
//    }
//}

eventTestCompileStart = {testType ->
//    throw new RuntimeException('Explode')
    def splitClass = classLoader.loadClass('grails.plugin.splittest.GrailsTestSplitter')

    testType.metaClass.eachSourceFile = splitClass.newInstance(1, 2).eachSourceFileHotReplace
    println "Test Type: ${testType.name} Name: ${testType.type} "
    System.out.println("-----------------------sdfsfgsdfg-sdfg-")
    grailsConsole.addStatus("Test Type: ${testType.name} Name: ${testType.type} ")
}
