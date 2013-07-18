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
    List shardTestClasses = []
    List splitTestClasses = []

    GrailsSplitTestType(GrailsTestTypeSupport grailsTestTypeSupport, Integer splitNumber, Integer totalSplits, Integer totalShards, Integer shardNumber) {
        this.grailsTestTypeSupport  = grailsTestTypeSupport
        this.splitNumber = splitNumber
        this.totalSplits = totalSplits
        this.totalShards = totalShards
        this.shardNumber = shardNumber
        validateSplits()
    }

    public String toString(){
        return "Split: ${splitNumber} | Shard: ${shardNumber} | Shard Classes: ${shardTestClasses} | Split Classes: ${splitTestClasses}"
    }


  /**
     * This is a bit nasty but in order to control the classes to run - override the eachSourceFile closure of
     * every sub class of GrailsTestType
     * Both Junit and Spock test types use this closure to find the files to run
     */
    private void overrideSourceFileCollection() {
        shardTestClasses  = []
        splitTestClasses = []

        //Note this is usually called twice by TestTypes - once on prepare and again when running
        grailsTestTypeSupport.metaClass.eachSourceFile = {Closure body ->
            testTargetPatterns.each { testTargetPattern ->
                //ToDO = Thhis is always one and one
                println("Getting sources files for Split: ${splitNumber} And Shard: ${shardNumber}")
                def allFiles = findSourceFiles(testTargetPattern)
                def splitSourceFiles = getFilesForThisSplit(splitNumber, allFiles)
                splitTestClasses.addAll(splitSourceFiles)
//                println("splitSourceFiles:" + splitSourceFiles)
                if(splitSourceFiles.size() > 0){
                    def shardedFiles = getFilesForCurrentShard(shardNumber, splitSourceFiles)
//                    println("shardedFiles:" + shardedFiles)

                    shardTestClasses.addAll(shardedFiles)
                    shardedFiles.each { sourceFile ->
                        body(testTargetPattern, sourceFile)
                    }
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

    public List getFilesForThisSplit(int spl, allSourceFiles){
       def collated = collateSourceFiles(allSourceFiles, totalSplits)
       return collated.get(spl-1)
    }

    public List getFilesForCurrentShard(int shd, List candidates){
        def collated = collateSourceFiles(candidates, totalShards)
//        println("sharded Files candidates:" + candidates + "Getting shard: ${shd}: Collated: ${collated}")
        return collated.get(shd-1)
    }

    public  List collateSourceFiles(List candidates, splitCount){
            //Sort should be as deterministic as possible - size and name
            if(splitCount > 0 ){
                CompositeFileComparator comparator = new CompositeFileComparator(SizeFileComparator.SIZE_COMPARATOR, NameFileComparator.NAME_COMPARATOR)
                List sorted = comparator.sort(candidates)
                List buckets = distributeToBuckets(splitCount, sorted)
                int resultSize = 0
                buckets.each {List l -> resultSize += l.size() }
                //Don't want to loose any tests
                assert resultSize == candidates.size()
                return buckets
            }else{
                []
            }
    }

    public List distributeToBuckets(Integer bucketSize, List list) {
        if (null == bucketSize || bucketSize == 0) {
            throw new IllegalArgumentException("Bucket size not specified")
        }
        else  {
            List buckets = (0 ..< bucketSize).collect{[]}
            int bucketIndex = 0
            list.each {f ->
                buckets.get(bucketIndex).add(f)
                bucketIndex++
                if (bucketIndex == bucketSize) {
                    bucketIndex = 0
                }
            }
            return buckets
        }
    }


}
