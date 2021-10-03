package enigma.components

// based on https://en.wikipedia.org/wiki/Enigma_rotor_details
enum class ReflectorType(val encryptionTable: String) {

    B ("YRUHQSLDPXNGOKMIEBFZCWVJAT"),

    C ("FVPJIAOYEDRZXWGCTKUQSBNMHL");
}