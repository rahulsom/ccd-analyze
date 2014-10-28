/**
 * Lists all sections present in CCD with their (guessed) name and frequency.
 */
import groovy.transform.BaseScript

@BaseScript CcdAnalysis script

def <K, V> Map<K, V> combineMaps(Map<K, List<V>> accum, Map<K, V> b) {
    accum + b.collectEntries { k, v -> [k, (accum[k] ?: []) + v] }
}

def result = files.parallelStream().map { File file ->
    def xml = new XmlSlurper().parse(file.newInputStream())
    def sections = xml.component.structuredBody.component.section

    sections.collectEntries { section ->
        [section.code.@code.text(), section.title.text()]
    }
}.reduce([:]) {accum, newMap -> combineMaps(accum, newMap) }.collect {k, v ->
    [k, [count: v.size(), title: v.head()] ]
}

result.each {k, v -> println "$k - ${v.count} - ${v.title}" }