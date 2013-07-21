includeTargets << grailsScript("_GrailsEvents")

eventTestCompileStart = {testType ->
    if (!binding.hasVariable('totalSplits') || !binding.hasVariable('split')) {
        return
    }

    grailsConsole.addStatus("Adding split support to grails test type: ${testType.class}")
    Integer totalSplits = Integer.valueOf(binding.getVariable('totalSplits'))
    Integer split = Integer.valueOf(binding.getVariable('split'))

    try {
        def splitClass = classLoader.loadClass('grails.plugin.partitiontests.GrailsTestSplitter')
        def splitter = splitClass.newInstance(split, totalSplits)
        testType.metaClass.eachSourceFile = splitter.eachSourceFileHotReplace
    } catch (Throwable t) {
        grailsConsole.error("Could not add split support", t)
    }
    grailsConsole.addStatus("Ready to complie '${testType.name}' tests for split run")
}

eventTestCompileEnd = {testType ->
    event("SplitTestTestCompileEnd", [testType])
}
