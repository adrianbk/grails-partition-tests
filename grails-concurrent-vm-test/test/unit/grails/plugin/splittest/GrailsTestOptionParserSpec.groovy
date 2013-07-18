package grails.plugin.splittest

import spock.lang.Specification

class  GrailsTestOptionParserSpec extends Specification {
    static int count = 0

    def "successfully parse test phases"(){
       expect: true
        println ("GrailsTestOptionParserSpec Execution Count: " +count++)

    }

}
