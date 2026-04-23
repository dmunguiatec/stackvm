package edu.stackvm.bytes

typealias Word = UByteArray

const val WORD_MSB = 0
const val WORD_MID_HIGH = 1
const val WORD_MID_LOW = 2
const val WORD_LSB = 3
const val WORD_SIZE = 4

private const val BYTE_MASK = 0xFF
private const val OFFSET_MSB = 24
private const val OFFSET_MID_HIGH = 16
private const val OFFSET_MID_LOW = 8

fun Int.toWordBigEndian(): Word {
    return ubyteArrayOf(
        ((this shr OFFSET_MSB) and BYTE_MASK).toUByte(),
        ((this shr OFFSET_MID_HIGH) and BYTE_MASK).toUByte(),
        ((this shr OFFSET_MID_LOW) and BYTE_MASK).toUByte(),
        (this and BYTE_MASK).toUByte()
    )
}

fun UByteArray.toIntBigEndian(): Int {
    require(size == 4)
    return (this[WORD_MSB].toInt() shl OFFSET_MSB) or
            (this[WORD_MID_HIGH].toInt() shl OFFSET_MID_HIGH) or
            (this[WORD_MID_LOW].toInt() shl OFFSET_MID_LOW) or
            this[WORD_LSB].toInt()
}

fun Float.toWordBigEndian(): Word {
    val bits = this.toRawBits()
    return ubyteArrayOf(
        ((bits ushr OFFSET_MSB) and BYTE_MASK).toUByte(),
        ((bits ushr OFFSET_MID_HIGH) and BYTE_MASK).toUByte(),
        ((bits ushr OFFSET_MID_LOW) and BYTE_MASK).toUByte(),
        (bits and BYTE_MASK).toUByte()
    )
}

fun UByteArray.toFloatBigEndian(): Float {
    require(size == WORD_SIZE)
    val bits = (this[WORD_MSB].toInt() shl OFFSET_MSB) or
            (this[WORD_MID_HIGH].toInt() shl OFFSET_MID_HIGH) or
            (this[WORD_MID_LOW].toInt() shl OFFSET_MID_LOW) or
            this[WORD_LSB].toInt()

    return Float.fromBits(bits)
}

fun Char.toWordBigEndian(): Word = this.code.toWordBigEndian()

fun UByteArray.toCharBigEndian(): Char = this.toIntBigEndian().toChar()
