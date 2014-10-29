import groovyx.gpars.GParsPool
import groovyx.gpars.GParsPoolUtil

import java.util.concurrent.atomic.AtomicInteger

@Grab("org.codehaus.gpars:gpars:1.2.1")
/**
 * Created by penny.lischer on 10/23/14.
 */

def problemCounters=[ccdWithProblems:0, name:0, date:0]

def arrayOfFiles = new File("/Users/penny.lischer/documents/certify-ccds").listFiles()


def parallelCollection = GParsPool.withPool {

    arrayOfFiles.eachParallel { File file ->

        def xml = new XmlSlurper().parse(file.newInputStream())
        def sections = xml.component.structuredBody.component.section

        sections.each { section ->
            def sectionName = section.code.@code.text()

            synchronized (problemCounters) {

                if (sectionName == "11450-4") {
                    boolean firstTimeIn = false;

                    def entries = section.entry;
                    if (entries.size() != 0) {
                        entries.each { entry ->
                            def observation = entry.act.entryRelationship.observation;
                            def problem = observation.translation.@displayName.text;

                            if (problem) {
                                //Problem Name
                                problemCounters['name']++

                                if (firstTimeIn == false) {
                                    problemCounters['ccdWithProblems']++;
                                    firstTimeIn = true;
                                }

                                def date = observation.effectiveTime.low.@value.text();
                                //Date
                                if (date) {
                                    problemCounters['date']++
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

println 'Number of CCDS with Problems: ' + problemCounters.get('ccdWithProblems');
println 'Percent of Problems per total CCDs: ' + (problemCounters.get('ccdWithProblems')/arrayOfFiles.size()) *100 + '%';
println "Problem Name Total:" + problemCounters.get('name')
println "Date Total:" + problemCounters.get('date')

