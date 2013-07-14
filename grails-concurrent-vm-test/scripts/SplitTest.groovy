import org.codehaus.gant.GantBinding
import org.codehaus.groovy.grails.commons.spring.GrailsApplicationContext

includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("_GrailsSettings")
includeTargets << grailsScript("_GrailsTest")
includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsEvents")
def log = {msg ->
    println msg

}

log grailsSettings
GantBinding b = binding
def m = metadata
log m
target(splitTest: "The description of the script goes here!") {
    event("StatusTestStart", ["Split Test Started"])
    log config
    //Load App to load app context in GrailsBootstrap
//    depends(loadApp)
//    GrailsApplicationContext context = appCtx


    // TODO: Implement script here
    log("Called!")
    log("Called with arguments: $argsMap")




}

setDefaultTarget(splitTest)
