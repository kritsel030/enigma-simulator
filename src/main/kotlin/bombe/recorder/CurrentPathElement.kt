package bombe.recorder



class CurrentPathElement (val label:String, val componentType: String, val inputContact: Char?, val outputContact: Char, previous: CurrentPathElement?, var root: CurrentPathElement?) {
    companion object{
        fun createRoot(outputContact: Char) : CurrentPathElement {
            var rootElement = CurrentPathElement("root", "root",null, outputContact, null, null)
            rootElement.root = rootElement
            return rootElement
        }
    }

    val nexts = mutableListOf<CurrentPathElement>()

    fun addNext(pathElement: CurrentPathElement) {
        nexts.add(pathElement)
    }

    fun print() {
        print(0)
    }

    fun print(level:Int) {
        for (i in 0..level) {
            print(" ")
        }
        println("$label: $inputContact --> $outputContact")
        for (n in nexts) {
            n.print(level+1)
        }
    }

}