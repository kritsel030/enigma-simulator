package shared

class Util {
    companion object {
        fun toChar(alphabetIndex: Int): Char {
            return 'A'.plus(alphabetIndex)
        }

        fun toInt(character: Char): Int {
            return character.code - 'A'.code
        }

        fun validate(input:String, alphabetsize: Int) : Boolean {
            var result = true
            for (character in input) {
                result = result && validate(character, alphabetsize)
            }
            return result
        }

        fun validate(input:Char, alphabetsize: Int) :Boolean {
            return 'A'.code <= input.code && input.code <= 'A'.plus(alphabetsize).code
        }

        fun validate(input:Int, alphabetsize:Int) :Boolean {
            return input in 0..alphabetsize
        }

        fun normalize(input: Int, alphabetsize: Int) : Int {
            return (input + 2*alphabetsize) % alphabetsize
        }
    }
}