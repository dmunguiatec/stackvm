package edu.stackvm.progdef

import edu.stackvm.bytes.toWordBigEndian
import java.io.FileOutputStream

class ProgDefSerializer {

    fun serialize(progDef: ProgDef, outputStream: FileOutputStream) {
        outputStream.use { out ->
            writeEntrypointStaticAddress(progDef, out)
            writeGlobalsCount(progDef, out)
            writeStatic(progDef, out)
            writeCode(progDef, out)
        }
    }

    private fun writeEntrypointStaticAddress(progDef: ProgDef, out: FileOutputStream) {
        val staticAddress = progDef.static.indexOfFirst {
            it is FuncDef && it.identifier == progDef.entryPoint.identifier
        }

        out.write(staticAddress.toWordBigEndian().toByteArray())
    }

    private fun writeGlobalsCount(progDef: ProgDef, out: FileOutputStream) {
        out.write(progDef.globalsCount.toWordBigEndian().toByteArray())
    }

    private fun writeStatic(progDef: ProgDef, out: FileOutputStream) {
        writeStaticCount(progDef, out)
        writeStringLiterals(progDef, out)
        writeFuncDefs(progDef, out)
    }

    private fun writeStaticCount(progDef: ProgDef, out: FileOutputStream) {
        out.write(progDef.static.size.toWordBigEndian().toByteArray())
    }

    private fun writeStringLiterals(progDef: ProgDef, out: FileOutputStream) {
        data class AddressedStringLiteral(val stringLiteral: StringLiteral, val staticAddress: Int)
        val addressedStringLiterals = mutableListOf<AddressedStringLiteral>()
        progDef.static.forEachIndexed { index, staticObject ->
            if (staticObject is StringLiteral) {
                addressedStringLiterals.add(AddressedStringLiteral(staticObject, index))
            }
        }

        out.write(addressedStringLiterals.size.toWordBigEndian().toByteArray())

        addressedStringLiterals.forEach {
            writeStringLiteral(it.stringLiteral, it.staticAddress, out)
        }

    }

    private fun writeStringLiteral(stringLiteral: StringLiteral, staticAddress: Int, out: FileOutputStream) {
        out.write(staticAddress.toWordBigEndian().toByteArray())
        out.write(stringLiteral.value.length.toWordBigEndian().toByteArray())

        stringLiteral.value.toCharArray().forEach {
            out.write(it.toWordBigEndian().toByteArray())
        }
    }

    private fun writeFuncDefs(progDef: ProgDef, out: FileOutputStream) {
        data class AddressedFuncDef(val funcDef: FuncDef, val staticAddress: Int)
        val addressedFuncDefs = mutableListOf<AddressedFuncDef>()
        progDef.static.forEachIndexed { index, staticObject ->
            if (staticObject is FuncDef) {
                addressedFuncDefs.add(AddressedFuncDef(staticObject, index))
            }
        }

        out.write(addressedFuncDefs.size.toWordBigEndian().toByteArray())

        addressedFuncDefs.forEach {
            writeFuncDef(it.funcDef, it.staticAddress, out)
        }
    }

    private fun writeFuncDef(funcDef: FuncDef, staticAddress: Int, out: FileOutputStream) {
        out.write(staticAddress.toWordBigEndian().toByteArray())
        writeFuncDefIdentifier(funcDef.identifier, out)

        out.write(funcDef.args_count.toWordBigEndian().toByteArray())
        out.write(funcDef.locals_count.toWordBigEndian().toByteArray())
        out.write(funcDef.code_address.toWordBigEndian().toByteArray())
    }

    private fun writeFuncDefIdentifier(identifier: String, out: FileOutputStream) {
        out.write(identifier.length.toWordBigEndian().toByteArray())
        identifier.toCharArray().forEach {
            out.write(it.toWordBigEndian().toByteArray())
        }
    }

    private fun writeCode(progDef: ProgDef, out: FileOutputStream) {
        out.write(progDef.code.toUByteArray().toByteArray())
    }
}
