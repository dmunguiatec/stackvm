package edu.stackvm.disassembler

import edu.stackvm.bytes.WORD_SIZE
import edu.stackvm.bytes.toCharBigEndian
import edu.stackvm.bytes.toFloatBigEndian
import edu.stackvm.bytes.toIntBigEndian
import edu.stackvm.progdef.Bytecode
import edu.stackvm.progdef.FuncDef
import edu.stackvm.progdef.ProgDef
import edu.stackvm.progdef.StringLiteral

class Disassembler(val progDef: ProgDef) {

    fun decodeInstructions() {
        var ip = 0

        println("init ip <- @${progDef.entryPoint.code_address}")

        while (ip < progDef.code.size) {
            ip = decodeInstruction(ip)
        }

        println()
    }

    private fun decodeInstruction(ip: Int): Int {
        var currentIp = ip
        val opcode = progDef.code[currentIp]
        val bytecode = Bytecode.fromBytecode(opcode)
        if (bytecode == null) {
            error("unknown opcode: $opcode at address $currentIp")
        }
        print("%04d:\t%-11s".format(currentIp, bytecode.instruction))
        currentIp++

        if (bytecode.operandCount == 0) {
            println("  ")
            return currentIp
        }

        val operands = mutableListOf<String>()
        repeat (bytecode.operandCount) {
            val word = UByteArray(WORD_SIZE) { i ->
                progDef.code[currentIp + i]
            }
            currentIp += WORD_SIZE

            val operand = when (bytecode) {
                Bytecode.CALL -> word.toIntBigEndian().let { staticAddress ->
                    (progDef.static[staticAddress] as FuncDef).let {
                        "#$staticAddress:${it.identifier}()@${it.code_address}"
                    }
                }
                Bytecode.JMP, Bytecode.JMPT, Bytecode.JMPF -> "@${word.toIntBigEndian()}"
                Bytecode.CCONST -> "'${word.toCharBigEndian()}'"
                Bytecode.ICONST -> word.toIntBigEndian().toString()
                Bytecode.FCONST -> word.toFloatBigEndian().toString()
                Bytecode.SCONST -> word.toIntBigEndian().let { staticAddress ->
                    (progDef.static[staticAddress] as StringLiteral).let {
                        "#$staticAddress:\"${it.value}\""
                    }
                }
                Bytecode.LOAD, Bytecode.GLOAD -> "&${word.toIntBigEndian()}"
                Bytecode.STORE, Bytecode.GSTORE -> "&${word.toIntBigEndian()}"
                else -> error("instruction not expected to have operands: ${bytecode.instruction}")
            }

            operands.add(operand)
        }

        print(operands.joinToString(", "))
        println()

        return currentIp
    }
}