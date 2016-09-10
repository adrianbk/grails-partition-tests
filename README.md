[![Build Status](https://travis-ci.org/adrianbk/grails-partition-tests.png)](https://travis-ci.org/adrianbk/grails-partition-tests)
# Grails Partition Tests Plugin #

Allows for the division of Grails tests into partitions with a view to running each partition on a separate machine or process.

## Overview ##
As the number of tests in a Grails application increases, build times can become excessively long - particularly with a large number of functional tests. A typical strategy to overcome this, on a continuous integration build, is to divide the build/test process into splits and have each split run on separate slaves in parallel. Grails provides a way to run specific test phases, test types and test patterns, it does not however provide a straightforward way to run partitioned tests i.e. “run half of all functional tests’. This plugin facilitates partitioning of tests for any given ‘grails test-app’ command by supplying 2 extra arguments: ‘split’ and ‘totalSplits’

## Installing ##
Add a dependency for the plugin in BuildConfig.groovy:

    plugins {
       ...
       test ":partition-tests:0.1"
    }

### Usage ###
The partition-test command takes all of the same arguments that test-app takes with the addition of the arguments: ‘split’ and ‘totalSplits’ (both are required)


Run all tests across all test phases with some optional test-app arguments 
```shell
grails test partition-test "--split=1" "--totalSplits=1" --verbose --echoOut --stacktrace
```
identiacal to :
```shell 
grails test-app --verbose --echoOut --stacktrace
```

Run the 1st half of all of the applications tests for all test phases and test types
```shell 
grails test partition-test "--split=1" "--totalSplits=2"
```

Run the 1st third of all spock tests in the unit test phase
```shell 
grails test partition-test unit:spock "--split=1" "--totalSplits=3"
```
Run the 2nd fiftieth of all spock test in the functional test phase
```shell 
grails test partition-test functional:spock "--split=2" "--totalSplits=50"
```

### Deterministic splits ###
The test files are distributed across partitions based on a composite sort in the following order
1. Test file size
2. Test file path

i.e. the 1st file in the 1st partition will be the largest file, the 1st file in the second partition will be the 2nd largest file and so on..

If two test files have the same size and name (same file names but in different packages) the files path is used as the secondary qualifier when distributing across partitions. 


### Limitations ###
1. Relies on any custom test types in your application to extend from GrailsTestTypeSupport (as is the standard Grails way to add additional test types).
	* The default Grails test type is: JUnit4GrailsTestType
	* Spock uses :GrailsSpecTestType
2. All test types must use the `GrailsTestTypeSupport.eachSourceFile(Closure body) {..}` closure to locate it's test source files
3. Grails environment must be specified as setting scriptEnv="test" in PartitionTest.groovy does not set the environment to test as expected
    * `grails test partition-test .... `


### Development
The root directory contains 2 grails projects. `grails-partition-tests` and `app`. `grails-partition-tests` is the plugin source
and `app` is a basic grails app with some tests which can be use to verify the plugin. `app` is configured with `grails.plugin.location.'partition-tests'`
  which means the plugin is used "in-place". See [ci.sh](ci.sh) for the grails commands used to test both the app and plugin.
