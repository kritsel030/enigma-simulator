package shared

// based on https://en.wikipedia.org/wiki/Enigma_rotor_details
enum class RotorType(
    val encryptionTable: String,

    // when the rotor reaches this position, it causes the rotor to its left to step as well
    // https://en.wikipedia.org/wiki/Enigma_machine#Turnover
    val turnoverPosition: Char) {

    I ("EKMFLGDQVZNTOWYHXUSPAIBRCJ",'R'),

    II ("AJDKSIRUXBLHWTMCQGZNPYFVOE", 'F'),

    III ("BDFHJLCPRTXVZNYEIWGAKMUSQO", 'W'),

    IV ("ESOVPZJAYQUIRHXLNFTGKDCMWB", 'K'),

    V ("VZBRGITYUPSDNHLXAWMJQOFECK", 'A'),

//    SHOWCASE_I ("BCDEFA",'B'),
//
//    SHOWCASE_II ("BDFACE", 'D'),
//
//    SHOWCASE_III ("CDEFBE", 'E');

    SHOWCASE_I ("CAFBDE",'A'),

    SHOWCASE_II ("CABEFD", 'D'),

    SHOWCASE_III ("EADFBC", 'E');

    val alphabetsize = encryptionTable.length

}