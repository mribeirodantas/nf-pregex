@groovy.transform.CompileDynamic
def escapeRegex(String text) {
    return text.replaceAll(/([\\.*+?^${}()\[\]|-])/) { match -> 
        println "Matched: ${match} (class: ${match.getClass()})"
        println "match[0]: ${match[0]}"
        println "match[1]: ${match[1]}"
        '\\' + match[1]
    }
}

def result = escapeRegex("hello")
println "Result: '${result}'"

def result2 = escapeRegex("test.txt")
println "Result2: '${result2}'"
