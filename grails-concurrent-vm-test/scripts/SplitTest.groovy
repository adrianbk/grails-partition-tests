includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("_GrailsSettings")
def log = {msg ->
    println msg

}
target(main: "The description of the script goes here!") {
    // TODO: Implement script here
    log("Called!")
    log("Called with arguments: $argsMap")



}

setDefaultTarget(main)
