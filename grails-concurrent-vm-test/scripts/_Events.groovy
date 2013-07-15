import org.codehaus.groovy.grails.test.GrailsTestType
import org.codehaus.groovy.grails.test.support.GrailsTestTypeSupport

includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsRun")
includeTargets << grailsScript("_GrailsSettings")
includeTargets << grailsScript("_GrailsClean")

eventTestPhaseStart = {args ->
    grailsConsole.updateStatus("Running test phase ${args} ....")
}
eventTestPhasesStart = { args ->
    Integer splitNumber = testOptions.split ? Integer.valueOf(testOptions.split) : null

    if(splitNumber){
        def splitClass = classLoader.loadClass('grails.plugin.splittest.GrailsSplitTestType')
        //Override runTests in _GrailsTest
        binding.runTests = { GrailsTestType type, File compiledClassesDir ->
            grailsConsole.updateStatus "Running: ${type.name} | GrailsTestType: ${type.class.name}| Split number: ${splitNumber} ..."
            if(type instanceof GrailsTestTypeSupport){
                type = splitClass.newInstance(type, splitNumber, 1)
                type.overrideSourceFileCollection()
            }

            def testCount = type.prepare(testTargetPatterns, compiledClassesDir, binding)
            grailsConsole.updateStatus "Test Count $testCount "
            if (testCount) {
                try {
                    event("TestSuiteStart", [type.name])
                    grailsConsole.updateStatus "Running ${testCount} $type.name test${testCount > 1 ? 's' : ''}..."

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