package grails.plugin.partitiontests

import grails.test.AbstractCliTestCase

/**
 * Run with: test-app --other
 * See target/cli-output for any output generated by the script
 * CLI script testing
 * http://www.cacoethes.co.uk/blog/groovyandgrails/testing-your-grails-scripts
 */
class SplitTestScriptTests extends AbstractCliTestCase{

    def scriptName = 'partition-test'

    @Override
    protected void setUp() {
        timeout = 10000
    }

    @Override
    protected void tearDown() {
        println "--------------------------------Script output---------------------${output}"
    }

    void testShouldSoThisTestTypeDoesntExplodeGrailsTestApp(){
    }

    void testShouldRequireBothArgs(){
        executeAndWait '--skip'
        assert output.contains('split and totalSplits must be suppplied')
    }

    void testShouldRequireCurrentSplitLessThanTotalSplits(){
        executeAndWait '--skip', "--split=2",  "--totalSplits=1"
        assert output.contains(' must not be greater than totalSplits')
    }

    void testShouldNotAllowNegativeSplits(){
        executeAndWait '--skip', "--split=-1",  "--totalSplits=-3"
        assert output.contains('Split arguments must not be negative!')
    }

    void testValidSplits(){
        executeAndWait '--skip', "--split=1",  "--totalSplits=3"
        assert output.contains("** Running Tests in partition mode. Split (1) of (3) split's **")
    }

    private void executeAndWait(String... args) {
        execute([scriptName] + (args as List))
        assert 1 == waitForProcess()
        verifyHeader()
    }
}
