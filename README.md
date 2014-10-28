![https://travis-ci.org/rahulsom/ccd-analyze.svg](https://travis-ci.org/rahulsom/ccd-analyze.svg)
ccd-analyze
====

Scripts to analyze CCDs

Usage:
----

```bash
cd src
groovy <SCRIPT> <DATADIR>
```

For example look at the `runall.sh`. It downloads data from CHB's Sample CCDAs
repo and runs over them.

Contributing
----

This is what your script should look like

```groovy
/**
 * This script prints the frequency of each section in the CCD
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
```

1. Give a clear groovydoc on top that explains what the script does.
2. Declare the `@BaseScript` to be `CcdAnalysis`. That lets users run all scripts
in an identical way.
3. Use the property `files` to access all CCDs available to you.
4. Name your script in a meaningful way, e.g. `SectionFrequency.groovy`
5. Optionally if you need static typing, use `@Grab('com.github.rahulsom:ihe-iti:0.8')
`.

For every other aspect, this is just another groovy script.

**Recommended**:
* Try to parallelize script execution. Everyone has a multi core processor these
days, and many people crunch 1000s of CCDs with this.

Creating a pull request
----

1. Fork the repo
2. Clone it
3. Write your script, tweak an existing script
4. Send a pull request
5. Wait for Travis to run some tests
6. Wait for me to merge it
