package edu.stackvm.assembler

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import edu.stackvm.disassembler.Disassembler
import edu.stackvm.progdef.ProgDefDeserializer
import java.io.File
import java.io.FileInputStream

class DisassemblerCommandLine : CliktCommand() {
    val assemblyFile: String by option("-i", "--input", help = "assembly file").required()

    override fun run() {
        if (File(assemblyFile).exists().not()) {
            error("assembly file $assemblyFile does not exist")
        }

        val deserializedProgDef = ProgDefDeserializer().deserialize(FileInputStream(this.assemblyFile))

        val disassembler = Disassembler(deserializedProgDef)
        disassembler.decodeInstructions()
    }
}

fun main(args: Array<String>) {
    DisassemblerCommandLine().main(args)
}
