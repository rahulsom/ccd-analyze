/*
  Created by brian.jong on 10/27/14.

  10157-6 - 2056 - Family History
   Family History
    o Family Member
    o Status
    o Illness

    Result
    Family Member count - 2933
    Illness count - 4515

 */

@Grab("org.codehaus.gpars:gpars:1.2.1")
import groovyx.gpars.GParsPool
import groovy.transform.BaseScript

@BaseScript CcdAnalysis script

int familyMemberCount = 0
int familyStatusCount = 0 //no statuses found... unless StatusCode was it...
int familyIllnessCount = 0

def allTables = [familyMember:[:], familyIllness:[:]]
def arrayOfFiles = files

/*
* The closure passed here gets the benefits of added methods for GPars concurrency models based on executor pools.
*/
GParsPool.withPool {
    arrayOfFiles.eachParallel { File file ->

        def xml = new XmlSlurper().parse(file.newInputStream())
        def sections = xml.component.structuredBody.component.section

        sections.each { section ->
            def sectionName = section.code.@code.text()
            if (sectionName == "10157-6") {
                def entries = section.entry;

                synchronized (allTables) {
                    //History: Family History
                    entries.each { entry ->
                        def relatedSubject = entry.organizer.subject.relatedSubject

                        //look for family member
                        String familyMemberText = relatedSubject.code.@code.text()

                        if (familyMemberText) {
                            familyMemberText = "${relatedSubject.code.@code.text()} - ${relatedSubject.code.@displayName.text()}"
                            def item = familyMemberText.toLowerCase().trim();
                            if (allTables['familyMember'].containsKey(item)) {
                                allTables['familyMember'][item]++
                            } else {
                                allTables['familyMember'][item] = 1
                            }
                        }

                        def observation = entry.organizer.component.observation
                        //look for illness
                        String illnessText = observation.text.text()

                        if (illnessText) {
                            def item = illnessText.toLowerCase().trim();
                            if (allTables['familyIllness'].containsKey(item)) {
                                allTables['familyIllness'][item]++
                            } else {
                                allTables['familyIllness'][item] = 1
                            }
                        }
                    }
                }

            }
        }
    }
}

allTables['familyMember'].each { k, v ->
    familyMemberCount += v
    println "$k - $v"
}
println()
println "Family Member count - ${familyMemberCount}"
println()

allTables['familyIllness'].each { k, v ->
    familyIllnessCount += v
    println "$k - $v"
}
println()
println "Illness count - ${familyIllnessCount}"
