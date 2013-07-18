package grails.plugin.splittest

import spock.lang.Specification
import spock.lang.Unroll

class GrailsTestSplitterSpec extends Specification{

    @Unroll
    def "sort and split files"(){
        given: 'a list of mock files'
        def files = []

        (0..<totalFiles).each {
            def f = new File("Name ${it+1}")
            files.add(f)
        }
        Collections.shuffle(files)

        when:

        GrailsTestSplitter grailsTestSplitter = new GrailsTestSplitter(1, totalSplits)
        List splitResults = grailsTestSplitter.collateSourceFiles(files, totalSplits)

        then:
        splitResults.size() == expectedSplit.size()
        if (splitResults) {
            splitResults.eachWithIndex {l, i ->
                assert l.collect {it.name} == expectedSplit.get(i).collect{it.get('n')}
            }
        }

        where:
        totalFiles | totalSplits | expectedSplit
        2          | 0           | []
        0          | 5           | [[],[],[],[],[]]
        1          | 1           | [[[n: 'Name 1']]]
        1          | 2           | [[[n: 'Name 1']], []]
        2          | 2           | [[[n: 'Name 1']], [[n: 'Name 2']]]
        3          | 2           | [[[n: 'Name 1'], [n: 'Name 3']], [[n: 'Name 2']]] //1,2,3 -> [1,3], [2]
        4          | 2           | [[[n: 'Name 1'], [n: 'Name 3']], [[n: 'Name 2'], [n: 'Name 4']]]
        7          | 3           | [ [[n: 'Name 1'], [n: 'Name 4'], [n: 'Name 7']], [[n: 'Name 2'], [n: 'Name 5']], [[n: 'Name 3'], [n: 'Name 6']]  ] //Split = 1,2,3,4,5,6,7 -> [1,4,7], [2,5], [3,6]|

    }
}
