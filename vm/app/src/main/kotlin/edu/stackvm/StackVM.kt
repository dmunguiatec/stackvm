package edu.stackvm

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.ibm.icu.impl.Assert.fail
import edu.stackvm.assembler.Assembler
import edu.stackvm.disassembler.Disassembler
import edu.stackvm.interpreter.Interpreter
import edu.stackvm.progdef.ProgDefDeserializer
import edu.stackvm.progdef.ProgDefSerializer
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.io.path.Path
import kotlin.io.path.extension

private const val DEFAULT_OPERAND_STACK_SIZE = 1024
private const val DEFAULT_CALL_STACK_SIZE = 1024

private const val EXTENSION_STKASM = "stkasm"
private const val EXTENSION_STKBIN = "stk"

private const val MODE_ASSEMBLE = "assemble"
private const val MODE_DISASSEMBLE = "disassemble"
private const val MODE_RUN = "run"

class CommandLine : CliktCommand() {
    val mode by option("--mode", help = "operation mode")
        .choice(MODE_ASSEMBLE, MODE_DISASSEMBLE, MODE_RUN)
        .required()

    val inputFile by option("-i", "--input", help = "input file")
        .path(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)
        .required()
        .validate {
            val ext = it.extension

            if (mode == MODE_ASSEMBLE && ext != EXTENSION_STKASM) {
                fail("--input-file must have .stkasm extension when using --assemble")
            }

            if (mode == MODE_DISASSEMBLE && ext != EXTENSION_STKBIN) {
                fail("--input-file must have .stk extension when using --disassemble")
            }

            if (mode == MODE_RUN && ext != EXTENSION_STKBIN) {
                fail("--input-file must have .stk extension when using --run")
            }
        }
    val outputFile by option("-o", "--output", help = "output file")
        .path(mustExist = false, canBeFile = true, canBeDir = false, mustBeWritable = false)

    val stackSize: Int by option("-s", "--stack-size", help = "vm stack size").int()
        .default(DEFAULT_OPERAND_STACK_SIZE)
    val callStackSize: Int by option("-c", "--call-stack-size", help = "vm call stack size").int()
        .default(DEFAULT_CALL_STACK_SIZE)

    override fun run() {
        if (this.mode == MODE_ASSEMBLE && this.outputFile == null) {
            fail("--output is required when --mode=assemble")
        }

        if (this.mode == MODE_ASSEMBLE) {
            val progDef = Assembler(this.inputFile.toString()).generateBytecode()
            ProgDefSerializer().serialize(progDef, FileOutputStream(this.outputFile.toString()))
        }

        if (this.mode == MODE_DISASSEMBLE) {
            val deserializedProgDef = ProgDefDeserializer()
                .deserialize(FileInputStream(this.inputFile.toString()))
            val disassembler = Disassembler(deserializedProgDef)
            disassembler.decodeInstructions()
        }

        if (this.mode == MODE_RUN) {
            val deserializedProgDef = ProgDefDeserializer()
                .deserialize(FileInputStream(this.inputFile.toString()))

            val interpreter = Interpreter(
                deserializedProgDef, emptyArray(),
                this.stackSize,
                this.callStackSize
            )

            interpreter.run()
        }
    }
}

fun main(args: Array<String>) {
    CommandLine().main(args)
}
