package edu.stackvm.assembler

import AssemblerLexer
import edu.stackvm.progdef.ProgDef
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class Assembler(val sourceFilename: String) {

    fun generateBytecode(): ProgDef {
        val input = CharStreams.fromFileName(this.sourceFilename)
        val assemblerLexer = AssemblerLexer(input)
        val tokens = CommonTokenStream(assemblerLexer)
        val bytecodeGenerator = BytecodeGenerator(tokens)
        bytecodeGenerator.program()
        val progDef = bytecodeGenerator.buildProgDef()

        return progDef
    }
}