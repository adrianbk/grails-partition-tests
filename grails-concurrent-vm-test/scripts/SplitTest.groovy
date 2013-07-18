includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsTest")
def log = {msg ->
    grailsConsole.log(msg)
}
def error = {msg ->
    grailsConsole.error(msg)
}
//grails splitTest unit "--split=1" "--totalSplits=3"
target(splitTest: "Splits each the files to run in each grails test phase.") {
    if(!argsMap.split|| !argsMap.totalSplits){
        error("Both arguments: split and totalSplits must be suppplied e.g(grails splitTest unit \"--split=1\" \"--totalSplits=3)")
        exit(0)
    }
    Integer split = Integer.valueOf(argsMap.split)
    Integer totalSplits = Integer.valueOf(argsMap.totalSplits)
    getBinding().setVariable('split', split)
    getBinding().setVariable('totalSplits', totalSplits)

    log "** Running Tests in split mode. Rinning split (${split}) of (${totalSplits}) split${totalSplits > 1 ? "'s" : 's'}**"
    log("Reading input params")
    allTests()
}
setDefaultTarget(splitTest)
