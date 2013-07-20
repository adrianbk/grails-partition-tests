includeTargets << grailsScript("_GrailsEvents")

eventTestCompileStart = {testType ->
    def binding = getBinding()
    Integer totalSplits
    Integer split
    if (binding.hasVariable('totalSplits') && binding.hasVariable('split')) {
        grailsConsole.addStatus("Adding split support to grails test type: ${testType.class}")
        totalSplits = Integer.valueOf(binding.getVariable('totalSplits'))
        split = Integer.valueOf(binding.getVariable('split'))

        def splitClass
        def splitter
        try {
            splitClass = classLoader.loadClass('grails.plugin.partitiontests.GrailsTestSplitter')
            splitter = splitClass.newInstance(split, totalSplits)
            testType.metaClass.eachSourceFile = splitter.eachSourceFileHotReplace
        } catch (Throwable t) {
            grailsConsole.error("Could not add split support", t)
        }
        grailsConsole.addStatus("Ready to complie '${testType.name}' tests for split run")
    }
}
eventTestCompileEnd = {testType ->
    event("SplitTestTestCompileEnd", [testType])
}