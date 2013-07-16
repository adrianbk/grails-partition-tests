import org.codehaus.groovy.grails.test.GrailsTestType
import org.codehaus.groovy.grails.test.support.GrailsTestTypeSupport

includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsRun")
includeTargets << grailsScript("_GrailsSettings")
includeTargets << grailsScript("_GrailsClean")

eventTestPhaseStart = {args ->
    grailsConsole.addStatus("Running test phase ${args}")
}
eventTestPhasesStart = { args ->
    Integer splitNumber = testOptions.split ? Integer.valueOf(testOptions.split) : null
    Integer totalSplits = testOptions.totalSplits ? Integer.valueOf(testOptions.totalSplits) : null

    if(splitNumber){
        def splitClass = classLoader.loadClass('grails.plugin.splittest.GrailsSplitTestType')
        //Override runTests in _GrailsTest
        binding.runTests = { GrailsTestType type, File compiledClassesDir ->
            Integer shards = config.gcvt?.concurrentPhases?."${currentTestPhaseName}"?.shards ?: 1
            //GrailsTestType: ${type.class.name}
            grailsConsole.addStatus "\tRunning Test Phase: ${currentTestPhaseName} Test type: ${type.name} split number:${splitNumber} of:${totalSplits}"
            grailsConsole.addStatus "\t Concurrent shards: ${shards}"
            if(type instanceof GrailsTestTypeSupport){
                type = splitClass.newInstance(type, splitNumber, totalSplits, shards)
                type.overrideSourceFileCollection()
            }

            if(type.totalShards > 1) {
                grailsConsole.addStatus "Running sharded"
                (1 .. type.totalShards).each {shardNumber ->
                    type.shardNumber == shardNumber

                }

            } else{
                def testCount = type.prepare(testTargetPatterns, compiledClassesDir, binding)
                grailsConsole.addStatus "\t\tTest Count $testCount "
                if (testCount) {
                    try {
                        event("TestSuiteStart", [type.name])
                        grailsConsole.addStatus "\t\tRunning ${testCount} $type.name test${testCount > 1 ? 's' : ''}..."

                        def start = new Date()
                        def result = type.run(testEventPublisher)
                        def end = new Date()

                        testCount = result.passCount + result.failCount
                        grailsConsole.addStatus "\t\tCompleted $testCount $type.name test${testCount > 1 ? 's' : ''}, ${result.failCount} failed in ${end.time - start.time}ms"
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