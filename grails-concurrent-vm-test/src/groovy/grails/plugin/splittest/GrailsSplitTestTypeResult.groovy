package grails.plugin.splittest

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import org.codehaus.groovy.grails.test.GrailsTestTypeResult

class GrailsSplitTestTypeResult implements GrailsTestTypeResult{

    @Delegate
    GrailsTestTypeResult grailsTestTypeResult
    Date start
    Date finish
    String phase
    int split
    int shard
    List shardTestClasses
    List splitTestClasses

    public String timeDuration(){
        TimeDuration td = TimeCategory.minus(finish, start)
        td
    }

    public String toString(){
        return "Phase: ${phase} | Split: ${split} | Shard: ${shard} | Pass Count: ${getPassCount()} " +
                "| Fail Count: ${getFailCount()} | Duration: ${timeDuration()} |" +
                "Shard Classes: ${shardTestClasses} | Split Classes: ${splitTestClasses}"
    }
}
