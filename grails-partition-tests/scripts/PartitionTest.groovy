scriptEnv="test"
includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsTest")
includeTargets << grailsScript("TestApp")

USAGE = """
partition-test [grails test arguments] "--split=<splitNumber>" "--totalSplits=<totalSplits>"

where
grails test arguments: Any command line options available when running 'grails test-app'
splitNumber: The current partition/split to run from a total of 'totalSplits'.
totalSplits: The total number of partitions/splits
I.e. split 1 of totalSplits 3 would run approx 1/3 of the applications tests

Sample usage: grails partitionTest unit:spock "--split=1" "--totalSplits=2" --verbose
"""

def log = {msg ->
    grailsConsole.log(msg)
}
def error = {msg ->
    grailsConsole.error(msg)
}
//usage: grails partitionTest unit "--split=1" "--totalSplits=3"
target(partitionTests: "Splits all Grails test files based on arguments: split and totalSplits.") {
    if (!argsMap.split || !argsMap.totalSplits) {
        error("Both arguments: split and totalSplits must be suppplied e.g (grails splitTest unit \"--split=1\" \"--totalSplits=3\")")
        exit(1)
    }
    Integer split = Integer.valueOf(argsMap.split)
    Integer totalSplits = Integer.valueOf(argsMap.totalSplits)

    if(split < 0 || totalSplits < 0 ){
        error('Split arguments must not be negative!')
        exit(1)
    }

    if (split > totalSplits) {
        error("Split(${split}) must not be greater than totalSplits(${totalSplits})")
        exit(1)
    }

    getBinding().setVariable('split', split)
    getBinding().setVariable('totalSplits', totalSplits)

    log "** Running Tests in partition mode. Split (${split}) of (${totalSplits}) split${totalSplits > 1 ? "'s" : ''} **"
    if (!argsMap.skip) {
        log("Handing off to grails test-app")

        /* Calls default target in TestApp.groovy
        * A but nasty because if any other targets with name 'default' are included which one gets called?
        * Don't want to repeat all the logic in TestApp.groovy to resolve test types, phases, arguments, etc.
        */
        depends('default')
    } else{
        log('skipping test phase for some reason')
    }
}
setDefaultTarget(partitionTests)
