package grails.plugin.splittest

import org.codehaus.groovy.grails.test.GrailsTestType
import org.codehaus.groovy.grails.test.GrailsTestTypeResult

import java.util.concurrent.Callable

class GrailsTestTypeRunner implements Callable<GrailsSplitTestTypeResult>{
    GrailsTestType testType
    def testEventPublisher
    String phase
    Integer split
    Integer shard


    @Override
    GrailsSplitTestTypeResult call() {
        GrailsSplitTestTypeResult result = new GrailsSplitTestTypeResult()
        result.start = new Date()
        result.phase = phase
        result.split = split
        result.shard = shard

        GrailsTestTypeResult grailsTestTypeResult =  testType.run(testEventPublisher)
        println "called GrailsSplitTestTypeResult ${grailsTestTypeResult}"
        result.grailsTestTypeResult = grailsTestTypeResult
        result.finish = new Date()
        return result
    }
}
