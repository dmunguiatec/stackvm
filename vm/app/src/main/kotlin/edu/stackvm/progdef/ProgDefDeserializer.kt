package edu.stackvm.progdef

import edu.stackvm.bytes.WORD_SIZE
import edu.stackvm.bytes.Word
import edu.stackvm.bytes.toCharBigEndian
import edu.stackvm.bytes.toIntBigEndian
import java.io.InputStream

class ProgDefDeserializer {

    private val buffer = ByteArray(WORD_SIZE)

    fun deserialize(inputStream: InputStream): ProgDef {
        inputStream.use { input ->
            val entrypointStaticAddress = readEntrypointStaticAddress(input)
            val globalsCount = readGlobalsCount(input)
            val static: List<StaticObject> = readStatic(input)
            val code: List<UByte> = readCode(input)

            return ProgDef(code, static, globalsCount, static[entrypointStaticAddress] as FuncDef)
        }
    }

    private fun readWord(input: InputStream): Word {
        val read = input.read(buffer)

        if (read != WORD_SIZE) {
            error("expected to read $WORD_SIZE bytes, but read $read")
        }

        return buffer.toUByteArray()
    }

    private fun readEntrypointStaticAddress(input: InputStream): Int {
        return readWord(input).toIntBigEndian()
    }

    private fun readGlobalsCount(input: InputStream): Int {
        return readWord(input).toIntBigEndian()
    }

    private fun readStatic(input: InputStream): List<StaticObject> {
        val staticCount = readWord(input).toIntBigEndian()
        val static: Array<StaticObject?> = arrayOfNulls(staticCount)

        readStringLiterals(static, input)
        readFuncDefs(static, input)

        val staticObjectList = mutableListOf<StaticObject>()
        static.forEach {
            if (it == null) {
                error("expected to read static object, but read null")
            } else {
                staticObjectList.add(it)
            }
        }

        return staticObjectList
    }

    private fun readStringLiterals(static: Array<StaticObject?>, input: InputStream) {
        val stringLiteralCount = readWord(input).toIntBigEndian()

        repeat(stringLiteralCount) {
            val stringLiteralStaticAddress = readWord(input).toIntBigEndian()
            val stringLiteral = readStringLiteral(input)
            static[stringLiteralStaticAddress] = stringLiteral
        }
    }

    private fun readStringLiteral(input: InputStream): StringLiteral {
        val stringLiteralLength = readWord(input).toIntBigEndian()
        val chars = CharArray(stringLiteralLength)
        repeat(stringLiteralLength) { i ->
            chars[i] = readWord(input).toCharBigEndian()
        }

        return StringLiteral(String(chars))
    }

    private fun readFuncDefs(static: Array<StaticObject?>, input: InputStream) {
        val funcDefCount = readWord(input).toIntBigEndian()

        repeat(funcDefCount) { i ->
            val funcDefStaticAddress = readWord(input).toIntBigEndian()
            val funcDef = readFuncDef(input)
            static[funcDefStaticAddress] = funcDef
        }
    }

    private fun readFuncDef(input: InputStream): FuncDef {
        val funcDefIdentifier = readFuncDefIdentifier(input)
        val funcDefArgsCount = readWord(input).toIntBigEndian()
        val funcDefLocalsCount = readWord(input).toIntBigEndian()
        val funcDefCodeAddress = readWord(input).toIntBigEndian()

        return FuncDef(
            funcDefIdentifier,
            funcDefArgsCount,
            funcDefLocalsCount,
            funcDefCodeAddress
        )
    }

    private fun readFuncDefIdentifier(input: InputStream): String {
        val funcDefIdentifierLength = readWord(input).toIntBigEndian()
        val identifier = CharArray(funcDefIdentifierLength)

        repeat(funcDefIdentifierLength) { i ->
            identifier[i] = readWord(input).toCharBigEndian()
        }

        return String(identifier)
    }

    private fun readCode(input: InputStream): List<UByte> {
        return input.readAllBytes().toUByteArray().toList()
    }
}
