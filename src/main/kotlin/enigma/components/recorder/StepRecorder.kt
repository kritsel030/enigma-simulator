package enigma.components.recorder

abstract class StepRecorder(


) {
    abstract fun toStringSimple(useCharacters:Boolean): String
    abstract fun toStringVerbose(useCharacters: Boolean): String

}
