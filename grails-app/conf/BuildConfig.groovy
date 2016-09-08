grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
        mavenRepo "https://repo.grails.org/grails/repo"
    }

    dependencies {
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
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
