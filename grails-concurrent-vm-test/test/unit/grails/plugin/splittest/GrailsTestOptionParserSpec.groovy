package grails.plugin.splittest

import spock.lang.Specification

class  GrailsTestOptionParserSpec extends Specification {


    def "successfully parse test phases"(){
        given:
        GrailsTestOptionsParser parser = new GrailsTestOptionsParser()

        when:
            parser.doParse(params)

        then:
            parser.testPhases == expected

        where:
        params | expected
        []     | ['unit', 'integration']
    }

}
