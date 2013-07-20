includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsTest")
includeTargets << grailsScript("TestApp")

def log = {msg ->
    grailsConsole.log(msg)
}
def error = {msg ->
    grailsConsole.error(msg)
}
//usage: grails splitTest unit "--split=1" "--totalSplits=3"
target(splitTest: "Splits all graisl test files based on arguments split and totalSplits.") {
    if (!argsMap.split || !argsMap.totalSplits) {
        error("Both arguments: split and totalSplits must be suppplied e.g (grails splitTest unit \"--split=1\" \"--totalSplits=3\")")
        exit(0)
    }
    Integer split = Integer.valueOf(argsMap.split)
    Integer totalSplits = Integer.valueOf(argsMap.totalSplits)

    if(split < 0 || totalSplits < 0 ){
        error('Split arguments must not be negative!')
    }

    if (split > totalSplits) {
        error("Split(${split}) must not be greater than totalSplits(${totalSplits})")
        exit(0)
    }

    getBinding().setVariable('split', split)
    getBinding().setVariable('totalSplits', totalSplits)

    log "** Running Tests in split mode. Rinning split (${split}) of (${totalSplits}) split${totalSplits > 1 ? "'s" : 's'}**"
    if (!argsMap.skip) {
        log("Running grails allTests()")

        /* Calls defalt target in TestApp.groovy
        * A but nasty because if any other targets with name 'default' are included which one gets called?
        * Don't want to repeat all the logic in TestApp.groovy to resolve test types, phases, arguments, etc.
        */
        depends('default')
    } else{
        log('skipping test phase for some reason')
    }
}
setDefaultTarget(splitTest)
