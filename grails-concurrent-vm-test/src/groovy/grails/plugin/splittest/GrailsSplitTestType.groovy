package grails.plugin.splittest

import org.apache.commons.io.comparator.CompositeFileComparator
import org.apache.commons.io.comparator.NameFileComparator
import org.apache.commons.io.comparator.SizeFileComparator
import org.codehaus.groovy.grails.test.GrailsTestTargetPattern
import org.codehaus.groovy.grails.test.GrailsTestType
import org.codehaus.groovy.grails.test.GrailsTestTypeResult
import org.codehaus.groovy.grails.test.event.GrailsTestEventPublisher
import org.codehaus.groovy.grails.test.support.GrailsTestTypeSupport

class GrailsSplitTestType implements GrailsTestType{
    @Delegate
    GrailsTestTypeSupport grailsTestTypeSupport
    Integer splitNumber
    Integer totalSplits


    GrailsSplitTestType(GrailsTestTypeSupport grailsTestTypeSupport, int splitNumber, int totalSplits) {
        this.splitNumber = splitNumber
        this.totalSplits = totalSplits
        validateSplits()
        this.grailsTestTypeSupport = grailsTestTypeSupport
        overrideSourceFileCollection()
    }


    @Override
    int prepare(GrailsTestTargetPattern[] testTargetPatterns, File compiledClassesDir, Binding buildBinding) {
        grailsTestTypeSupport.prepare(testTargetPatterns, compiledClassesDir, buildBinding)
    }

    @Override
    GrailsTestTypeResult run(GrailsTestEventPublisher eventPublisher) {
        grailsTestTypeSupport.run(eventPublisher)
    }

    private void overrideSourceFileCollection() {
        this.grailsTestTypeSupport.metaClass.eachSourceFile = {Closure body ->
            testTargetPatterns.each {testTargetPattern ->
                def allSourceFiles = findSourceFiles(testTargetPattern)
                def splitSourceFiles = collateSourceFiles(allSourceFiles)
                splitSourceFiles.each {sourceFile ->
                    body(testTargetPattern, sourceFile)
                }
            }
        }
    }

    private void validateSplits(){
        if(null == totalSplits || null == splitNumber){
            throw new IllegalArgumentException("Both splitNumber and totalSplits must not be null")
        } else if(splitNumber > totalSplits){
            throw new IllegalArgumentException("Current splitNumber must not exceed totalSplits")
        }
    }

    public List collateSourceFiles(List origSourceFiles){
        if(origSourceFiles && !origSourceFiles.empty){
            //Sort should be as deterministic as possible - size and name
            CompositeFileComparator comparator = new CompositeFileComparator(SizeFileComparator.SIZE_COMPARATOR, NameFileComparator.NAME_COMPARATOR)
            List sorted = comparator.sort(origSourceFiles)

//            int collateSize = calculateCollateSize(sorted.size())

            List collated = sorted
            return collated
        }
        return origSourceFiles
    }

    public Integer calculateCollateSize(Integer listSize){

        if(listSize <= totalSplits){
            return totalSplits
        } else{
            params.max = Math.min(max ?: 10, 100)

//            def l = (1..10)
//
//            Integer bucketSize = 4
//            Integer size = l.size()
//            int colS = size / bucketSize
//
//            println size + ' ' +  colS
//            l.collate(colS, true)
            return listSize / totalSplits
        }
    }


}
