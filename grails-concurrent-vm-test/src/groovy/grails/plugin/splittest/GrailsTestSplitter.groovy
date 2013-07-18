package grails.plugin.splittest

import org.apache.commons.io.comparator.CompositeFileComparator
import org.apache.commons.io.comparator.SizeFileComparator
import org.apache.commons.io.comparator.NameFileComparator

class GrailsTestSplitter {
    Integer currentSplit
    Integer totalSplits
    List candidateFiles
    List splitFiles

    GrailsTestSplitter(Integer currentSplit, Integer totalSplits) {
        this.currentSplit = currentSplit
        this.totalSplits = totalSplits
    }

    public List getFilesForThisSplit(int split, allSourceFiles) {
        def collated = collateSourceFiles(allSourceFiles, totalSplits)
        return collated.get(split - 1)
    }

    public List collateSourceFiles(List candidates, splitCount) {
        //Sort should be as deterministic as possible - size and name
        if (splitCount > 0) {
            CompositeFileComparator comparator = new CompositeFileComparator(SizeFileComparator.SIZE_COMPARATOR, NameFileComparator.NAME_COMPARATOR)
            List sorted = comparator.sort(candidates)
            List buckets = distributeToBuckets(splitCount, sorted)
            int resultSize = 0
            buckets.each {List l -> resultSize += l.size() }
            //Don't want to loose any tests
            assert resultSize == candidates.size()
            return buckets
        } else {
            []
        }
    }

    public List distributeToBuckets(Integer bucketSize, List list) {
        if (null == bucketSize || bucketSize == 0) {
            throw new IllegalArgumentException("Bucket size not specified")
        }
        else {
            List buckets = (0..<bucketSize).collect {[]}
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

    def eachSourceFileHotReplace = {Closure body ->
        testTargetPatterns.each { testTargetPattern ->
            println("Getting sources files for Split: ${currentSplit} of ${totalSplits} | Test Type: ${getName()} | Test Target Pattern: ${testTargetPattern}")
            def allFiles = findSourceFiles(testTargetPattern)
            println("All source files: ${allFiles}")
            def splitSourceFiles = getFilesForThisSplit(currentSplit, allFiles)
            println("Split source files: ${splitSourceFiles}")
            splitSourceFiles.each { sourceFile ->
                body(testTargetPattern, sourceFile)
            }
        }
    }
}
