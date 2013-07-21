package grails.plugin.partitiontests

import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Matcher

class GrailsTestSplitterSpec extends Specification {
    @Unroll
    def "Files are distributed correctly to the first partition"() {
      given: 'a list of files'
        def files = []
        (0..<totalFiles).each {
            def f = new File("Name ${it + 1}")
            files.add(f)
        }
        Collections.shuffle(files)
        files = files.reverse()

      when:
        GrailsTestSplitter grailsTestSplitter = new GrailsTestSplitter(1, totalSplits)
        List splitResults = grailsTestSplitter.collateSourceFiles(files, totalSplits)

      then:
        splitResults.size() == expectedSplit.size()
        if (splitResults) {
            splitResults.eachWithIndex {l, i ->
                assert l.collect {it.name} == expectedSplit.get(i).collect {it.get('n')}
            }
        }

      where:
        totalFiles | totalSplits | expectedSplit
        2          | 0           | []
        0          | 5           | [[], [], [], [], []]
        1          | 1           | [[[n: 'Name 1']]]
        1          | 2           | [[[n: 'Name 1']], []]
        2          | 2           | [[[n: 'Name 1']], [[n: 'Name 2']]]
        3          | 2           | [[[n: 'Name 1'], [n: 'Name 3']], [[n: 'Name 2']]] //1,2,3 -> [1,3], [2]
        4          | 2           | [[[n: 'Name 1'], [n: 'Name 3']], [[n: 'Name 2'], [n: 'Name 4']]]
        7          | 3           | [[[n: 'Name 1'], [n: 'Name 4'], [n: 'Name 7']], [[n: 'Name 2'], [n: 'Name 5']], [[n: 'Name 3'], [n: 'Name 6']]] //Split = 1,2,3,4,5,6,7 -> [1,4,7], [2,5], [3,6]|

    }

    @Unroll
    def "files of the the same name are distributed based on file path"() {

      given: 'a list of files'

        def files = []
        (0..<totalFiles).each {
            def f = new File("/p${it}/name")
            files.add(f)
        }
        Collections.shuffle(files)
        files = files.reverse()

      when:
        GrailsTestSplitter grailsTestSplitter = new GrailsTestSplitter(split, totalSplits)
        List splitResults = grailsTestSplitter.getFilesForThisSplit(files)

      then:
        splitResults.size() == expectedFiles.size()
        expectedFiles.eachWithIndex { f, i ->
            assert fs(f) == splitResults.get(i).getPath()
        }

      where:
        totalFiles | totalSplits | split | expectedFiles
        2          | 1           | 1     | ['/p0/name', '/p1/name']
        9          | 2           | 2     | ['/p1/name', '/p3/name', '/p5/name', '/p7/name']

    }

    def fs = {String fileRepresentation ->
        if (fileRepresentation) {
            return fileRepresentation.replaceAll("/", Matcher.quoteReplacement(File.separator))
        }
        return fileRepresentation
    }
}
