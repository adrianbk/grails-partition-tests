includeTargets << grailsScript("_GrailsEvents")

eventTestCompileStart = {testType ->
    grailsConsole.addStatus("Adding split support to grails test type: ${testType.class}")
    def binding = getBinding()
    grailsConsole.addStatus("Total Splits: ${totalSplits}")
    Integer totalSplits
    Integer split
    if(binding.hasVariable('totalSplits') && binding.hasVariable('split')){
        totalSplits = Integer.valueOf(binding.getVariable('totalSplits'))
        split = Integer.valueOf(binding.getVariable('split'))
    }
    def splitClass = classLoader.loadClass('grails.plugin.splittest.GrailsTestSplitter')
    def splitter = splitClass.newInstance(split, totalSplits)
    testType.metaClass.eachSourceFile = splitter.eachSourceFileHotReplace
    grailsConsole.addStatus("Added Split (Split: ${splitter.currentSplit} of:${splitter.totalSplits} )behavior for Test Type: ${testType.name} Name: ${testType.type} ")
}

eventTestCompileEnd = {testType ->
    event("SplitTestTestCompileEnd", [testType])
}

eventSplitTestTestCompileEnd = { testType ->
//    throw new RuntimeException((testType.testSplitter.properties.toString()))
}