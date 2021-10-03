package enigma.util

class Util {
    companion object {
        fun toChar(alphabetIndex: Int): Char {
            return ('A'.toInt() + alphabetIndex).toChar()
        }

        fun toInt(character: Char): Int {
            return character.toInt() - 'A'.toInt()
        }

        fun validate(input:String) : Boolean {
            var result = true
            for (character in input) {
                result = result && validate(character)
            }
            return result
        }

        fun validate(input:Char) :Boolean {
            return 'A'.toInt() <= input.toInt() && input.toInt() <= 'Z'.toInt()
        }

        fun normalize(input: Int) : Int {
            return (input + 26) % 26
        }
    }
}