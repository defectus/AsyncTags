grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'
    repositories {
        mavenLocal()
        grailsCentral()
        mavenCentral()
    }

    dependencies {
        test 'org.spockframework:spock-spring:1.0-groovy-2.4'
    }

    plugins {
        build(':release:3.1.2', ':rest-client-builder:2.1.1') {
            export = false
        }
    }
}
