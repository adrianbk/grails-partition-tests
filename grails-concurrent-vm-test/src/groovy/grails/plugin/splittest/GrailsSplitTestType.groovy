package grails.plugin.splittest

import org.apache.commons.io.comparator.CompositeFileComparator
import org.apache.commons.io.comparator.NameFileComparator
import org.apache.commons.io.comparator.SizeFileComparator
import org.codehaus.groovy.grails.test.GrailsTestTargetPattern
import org.codehaus.groovy.grails.test.GrailsTestType
import org.codehaus.groovy.grails.test.GrailsTestTypeResult
import org.codehaus.groovy.grails.test.event.GrailsTestEventPublisher
import org.codehaus.groovy.grails.test.support.GrailsTestTypeSupport
import org.apache.commons.collections.ListUtils

class GrailsSplitTestType implements GrailsTestType{
    @Delegate
    GrailsTestTypeSupport grailsTestTypeSupport
    Integer splitNumber
    Integer totalSplits
    Integer totalShards = 1
    Integer shardNumber = 1


    GrailsSplitTestType(GrailsTestTypeSupport grailsTestTypeSupport, int splitNumber, int totalSplits, int totalShards) {
        this.splitNumber = splitNumber
        this.totalSplits = totalSplits
        this.totalShards = totalShards
        validateSplits()
        this.grailsTestTypeSupport = grailsTestTypeSupport
    }


    @Override
    int prepare(GrailsTestTargetPattern[] testTargetPatterns, File compiledClassesDir, Binding buildBinding) {
        grailsTestTypeSupport.prepare(testTargetPatterns, compiledClassesDir, buildBinding)
    }

    @Override
    GrailsTestTypeResult run(GrailsTestEventPublisher eventPublisher) {
        grailsTestTypeSupport.run(eventPublisher)
    }

    /**
     * This is a bit nasty but in order to control the classes tto run - overide the eachSourceFile closure of
     * every sub class of GrailsTestType
     */
    private void overrideSourceFileCollection() {
        this.grailsTestTypeSupport.metaClass.eachSourceFile = {Closure body ->
            testTargetPatterns.each {testTargetPattern ->
                def allSourceFiles = findSourceFiles(testTargetPattern)
                def splitSourceFiles = getFilesForThisSplit(splitNumber, allSourceFiles)
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

    public List getFilesForThisSplit(splitNumber, allSourceFiles){
       def collated = collateSourceFiles(allSourceFiles)
       return collated.get(splitNumber-1)
    }

    public List getFilesForThisConcurrentShard(int shardNumber, List candidates){


    }

    public List collateSourceFiles(List origSourceFiles){
        if(origSourceFiles && !origSourceFiles.empty){
            //Sort should be as deterministic as possible - size and name
            CompositeFileComparator comparator = new CompositeFileComparator(SizeFileComparator.SIZE_COMPARATOR, NameFileComparator.NAME_COMPARATOR)
            List sorted = comparator.sort(origSourceFiles)
            List buckets = (0 .. totalSplits).collect{[]}
            buckets = distributeToBuckets(buckets, sorted)
            int resultSize = 0
            buckets.each {List l -> resultSize += l.size() }
            //Don't want to loose any tests
            assert resultSize == origSourceFiles.size()
            return buckets
        }
        return [origSourceFiles]
    }

    public List distributeToBuckets(List<List> listOfLists, List list) {
        if (null == listOfLists || list.empty) {
            return [list]
        }
        else if (listOfLists.size() == 1) {
            listOfLists.get(0).addAll(list)
            return listOfLists
        }
        else {
            int bucketCount = listOfLists.size()
            int bucketIndex = 0

            list.each {f ->
                listOfLists.get(bucketIndex).add(f)
                bucketIndex++
                if (bucketIndex == (bucketCount - 1)) {
                    bucketIndex = 0
                }
            }
            return listOfLists
        }
    }
}
