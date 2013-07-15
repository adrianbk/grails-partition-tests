package grails.plugin.splittest

import spock.lang.Specification
import spock.lang.Unroll

class GrailsSplitTestTypeSpec extends Specification{

    @Unroll
    def "sort and split files"(){
        given: 'a list of mock files'
        def files = []
        (1..totalFiles).each {
            def f = new File("Name ${it}")
            files.add(f)
        }

        when:
        GrailsSplitTestType grailsSplitTestType = new GrailsSplitTestType(null, splitNumber, totalSplits)
        grailsSplitTestType.metaClass.overrideSourceFileCollection = {}
        def results = grailsSplitTestType.getFilesForThisSplit(splitNumber, files)

        then:
            results.size() == expected.size()
            if(results){
                results?.eachWithIndex {File f, i ->
                    assert f.name == expected.get(i).n
                }
            }

        where:
        totalFiles | totalSplits | splitNumber | expected
        1          | 1           | 1           | [[n: 'Name 1']]
        1          | 10          | 1           | [[n: 'Name 1']]
        1          | 10          | 2           | []
        2          | 1           | 1           | [[n: 'Name 1'], [n: 'Name 2']]
        3          | 2           | 2           | [[n: 'Name 2']]
        4          | 2           | 2           | [[n: 'Name 2'], [n: 'Name 4']]
        4          | 2           | 1           | [[n: 'Name 1'], [n: 'Name 3']]
        7          | 3           | 1           | [[n: 'Name 1'], [n: 'Name 4'], [n: 'Name 7']] //1,2,3,4,5,6,7 -> [1,4,7], [2,5], [3,6]
        7          | 3           | 2           | [[n: 'Name 2'], [n: 'Name 5']]
        7          | 3           | 3           | [[n: 'Name 3'], [n: 'Name 6']]
    }
}
