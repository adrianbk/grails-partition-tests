grails.project.work.dir = 'target'
grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        mavenCentral()
        grailsCentral()
        mavenLocal()
        mavenRepo "https://repo.grails.org/grails/repo"
    }

    dependencies {
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
        test 'org.hamcrest:hamcrest-all:1.3'
        provided 'commons-io:commons-io:2.4'
    }

    plugins {
        test(":spock:0.7") {
            exclude "spock-grails-support"
        }

        build ':release:2.2.1', ':rest-client-builder:1.0.3', {
            export = false
        }
    }
}
