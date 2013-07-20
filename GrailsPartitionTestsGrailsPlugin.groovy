class GrailsPartitionTestsGrailsPlugin {
    def version = "1.0"
    def grailsVersion = "2.2 > *"
    def pluginExcludes = ["grails-app/views/error.gsp"]
    def title = "Grails Partition Tests Plugin"
    def author = "Adrian Kelly"
    def authorEmail = "adrianbkelly@gmail.com"
    def description = 'Allows for the division of grails tests into partitions with a view to running each partition on a separate machine or process.'
    def documentation = "https://github.com/adrianbk/grails-partition-tests"
    def license = "APACHE"
    def issueManagement = [ system: "GitHub", url: "https://github.com/adrianbk/grails-partition-tests/issues" ]
    def scm = [ url: "https://github.com/adrianbk/grails-partition-tests" ]
}
