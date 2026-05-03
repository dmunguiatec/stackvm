package edu.stackvm.interpreter

import edu.stackvm.bytes.WORD_SIZE
import edu.stackvm.bytes.toCharBigEndian
import edu.stackvm.bytes.toFloatBigEndian
import edu.stackvm.bytes.toIntBigEndian
import edu.stackvm.bytes.toWordBigEndian
import edu.stackvm.progdef.Bytecode
import edu.stackvm.progdef.Bytecode.*
import edu.stackvm.progdef.FuncDef
import edu.stackvm.progdef.ProgDef
import edu.stackvm.progdef.StringLiteral

private const val ENTRYPOINT_IDENTIFIER = "main"

private const val REG_UNDEFINED = -1
private const val BOOLEAN_TRUE = 1
private const val BOOLEAN_FALSE = 0

private const val DUMP_BYTE_COUNT = 16

class Interpreter(
    val progDef: ProgDef,
    val args: Array<Any>,
    operandStackSize: Int,
    callStackSize: Int
) {
    val opStack: Array<Any?> = arrayOfNulls(operandStackSize)
    val callStack: Array<StackFrame?> = arrayOfNulls(callStackSize)
    val globals: Array<Any?> = arrayOfNulls(this.progDef.globalsCount)

    var fp = REG_UNDEFINED
    var sp = REG_UNDEFINED
    var ip = REG_UNDEFINED

    fun run() {
        try {
            callEntrypoint()
        } catch (e: Exception) {
            e.message?.let { println("Error: $it") }
            coreDump()
        }
    }

    private fun callEntrypoint() {
        val entryPoint = this.progDef.entryPoint
        val entryPointCall = StackFrame(entryPoint, 0)

        if (entryPointCall.funcDef.args_count != args.size) {
            error("Expected ${entryPointCall.funcDef.args_count} arguments, got ${args.size}")
        }

        repeat(entryPointCall.funcDef.args_count) { i ->
            entryPointCall.locals[i] = args[i]
        }

        callStack[++this.fp] = entryPointCall
        this.ip = entryPointCall.funcDef.code_address
        cpu()
    }

    private fun cpu() {
        var bytecode = Bytecode.fromBytecode(this.progDef.code[this.ip])

        while (bytecode != HALT && this.ip < this.progDef.code.size) {
            this.ip++

            when (bytecode) {
                IADD -> iadd()
                ISUB -> isub()
                IMUL -> imul()
                ILT -> ilt()
                IEQ -> ieq()
                FADD -> fadd()
                FSUB -> fsub()
                FMUL -> fmul()
                FLT -> flt()
                FEQ -> feq()
                ITOF -> itof()
                CALL -> call()
                RET -> ret()
                JMP -> jmp()
                JMPT -> jmpt()
                JMPF -> jmpf()
                CCONST -> cconst()
                ICONST -> iconst()
                FCONST -> fconst()
                SCONST -> sconst()
                LOAD -> load()
                STORE -> store()
                GLOAD -> gload()
                GSTORE -> gstore()
                PRINT -> print()
                NULL -> nullop()
                POP -> pop()
                DUP -> dup()
                SWAP -> swap()
                ASSERT -> assertop()

                else -> error("Unknown instruction: $bytecode at address ${this.ip - 1}.")
            }

            bytecode = Bytecode.fromBytecode(this.progDef.code[this.ip])
        }
    }

    private fun coreDump() {
        println("Core dump:")
        if (this.progDef.static.isNotEmpty()) dumpStatic()
        if (this.globals.isNotEmpty()) dumpGlobals()
        dumpCodeMemory()
    }

    private fun dumpStatic() {
        println(".static")
        this.progDef.static.forEachIndexed { address, staticObject ->
            if (staticObject is StringLiteral) {
                System.out.printf("%04d: \"%s\"\n", address, staticObject.value)
            } else {
                System.out.printf("%04d: %s\n", address, staticObject)
            }
        }
        println()
    }

    private fun dumpGlobals() {
        println(".globals")
        globals.forEachIndexed { address, global ->
            if (global != null) {
                System.out.printf(
                    "%04d: %s <%s>\n",
                    address, global, global.javaClass.getSimpleName()
                )
            } else {
                System.out.printf("%04d: <null>\n", address)
            }
        }
        println()
    }

    private fun dumpCodeMemory() {
        println(".registers")
        System.out.printf("fp: %d\n", this.fp)
        System.out.printf("sp: %d\n", this.sp)
        System.out.printf("ip: %d\n", this.ip)
        println("Operand stack: [${this.opStack.filterNotNull().joinToString(", ")}]")
        println("Call stack: [${this.callStack.filterNotNull().joinToString(", ") { it.funcDef.identifier }}]")
        println()

        println(".code")
        println("${this.progDef.code.size} bytes")
        var i = 0
        while (i < this.progDef.code.size) {
            if (i % DUMP_BYTE_COUNT == 0 && i != 0) println()
            if (i % DUMP_BYTE_COUNT == 0) System.out.printf("%04d:", i)
            print("%02X ".format(this.progDef.code[i].toInt()))
            i++
        }
        println()
    }

    internal fun consumeInstructionOperand(): UByteArray {
        val word = UByteArray(WORD_SIZE) { i ->
            this.progDef.code[this.ip + i]
        }
        this.ip += WORD_SIZE

        return word
    }
}

private fun Interpreter.iadd() {
    val operandB = this.opStack[this.sp--] as Int
    val operandA = this.opStack[this.sp--] as Int
    this.opStack[++this.sp] = operandA + operandB
}

private fun Interpreter.isub() {
    val operandB = this.opStack[this.sp--] as Int
    val operandA = this.opStack[this.sp--] as Int
    this.opStack[++this.sp] = operandA - operandB
}

private fun Interpreter.imul() {
    val operandB = this.opStack[this.sp--] as Int
    val operandA = this.opStack[this.sp--] as Int
    this.opStack[++this.sp] = operandA * operandB
}

private fun Interpreter.ilt() {
    val operandB = this.opStack[this.sp--] as Int
    val operandA = this.opStack[this.sp--] as Int
    this.opStack[++this.sp] = if (operandA < operandB) BOOLEAN_TRUE else BOOLEAN_FALSE
}

private fun Interpreter.ieq() {
    val operandB = this.opStack[this.sp--] as Int
    val operandA = this.opStack[this.sp--] as Int
    this.opStack[++this.sp] = if (operandA == operandB) BOOLEAN_TRUE else BOOLEAN_FALSE
}

private fun Interpreter.fadd() {
    val operandB = this.opStack[this.sp--] as Float
    val operandA = this.opStack[this.sp--] as Float
    this.opStack[++this.sp] = operandA + operandB
}

private fun Interpreter.fsub() {
    val operandB = this.opStack[this.sp--] as Float
    val operandA = this.opStack[this.sp--] as Float
    this.opStack[++this.sp] = operandA - operandB
}

private fun Interpreter.fmul() {
    val operandB = this.opStack[this.sp--] as Float
    val operandA = this.opStack[this.sp--] as Float
    this.opStack[++this.sp] = operandA * operandB
}

private fun Interpreter.flt() {
    val operandB = this.opStack[this.sp--] as Float
    val operandA = this.opStack[this.sp--] as Float
    this.opStack[++this.sp] = if (operandA < operandB) BOOLEAN_TRUE else BOOLEAN_FALSE
}

private fun Interpreter.feq() {
    val operandB = this.opStack[this.sp--] as Float
    val operandA = this.opStack[this.sp--] as Float
    this.opStack[++this.sp] = if (operandA == operandB) BOOLEAN_TRUE else BOOLEAN_FALSE
}

private fun Interpreter.itof() {
    val operand = this.opStack[this.sp--] as Int
    this.opStack[++this.sp] = operand.toFloat()
}

private fun Interpreter.call() {
    val staticAddress = consumeInstructionOperand().toIntBigEndian()
    val funcDef = this.progDef.static[staticAddress] as FuncDef
    val stackFrame = StackFrame(funcDef, this.ip)

    for (arg_index in funcDef.args_count - 1 downTo 0) {
        stackFrame.locals[arg_index] = opStack[this.sp--]
    }

    callStack[++this.fp] = stackFrame
    this.ip = funcDef.code_address
}

private fun Interpreter.ret() {
    val stackFrame = callStack[this.fp--]
    this.ip = stackFrame!!.returnAddress
    // return value on top of operand stack
}

private fun Interpreter.jmp() {
    val address = consumeInstructionOperand().toIntBigEndian()
    this.ip = address
}

private fun Interpreter.jmpt() {
    val address = consumeInstructionOperand().toIntBigEndian()
    val operand = this.opStack[this.sp--] as Int
    if (operand == BOOLEAN_TRUE) {
        this.ip = address
    }
}

private fun Interpreter.jmpf() {
    val address = consumeInstructionOperand().toIntBigEndian()
    val operand = this.opStack[this.sp--] as Int
    if (operand == BOOLEAN_FALSE) {
        this.ip = address
    }
}

private fun Interpreter.cconst() {
    this.opStack[++this.sp] = consumeInstructionOperand().toCharBigEndian()
}

private fun Interpreter.iconst() {
    this.opStack[++this.sp] = consumeInstructionOperand().toIntBigEndian()
}

private fun Interpreter.fconst() {
    this.opStack[++this.sp] = consumeInstructionOperand().toFloatBigEndian()
}

private fun Interpreter.sconst() {
    val staticAddress = consumeInstructionOperand().toIntBigEndian()
    this.opStack[++this.sp] = (this.progDef.static[staticAddress] as StringLiteral).value
}

private fun Interpreter.load() {
    val local_address = consumeInstructionOperand().toIntBigEndian()
    this.opStack[++this.sp] = this.callStack[this.fp]!!.locals[local_address]
}

private fun Interpreter.store() {
    val local_address = consumeInstructionOperand().toIntBigEndian()
    this.callStack[this.fp]!!.locals[local_address] = this.opStack[this.sp--]
}

private fun Interpreter.gload() {
    val global_address = consumeInstructionOperand().toIntBigEndian()
    this.opStack[++this.sp] = this.globals[global_address]
}

private fun Interpreter.gstore() {
    val global_address = consumeInstructionOperand().toIntBigEndian()
    this.globals[global_address] = this.opStack[this.sp--]
}

private fun Interpreter.print() {
    println(this.opStack[this.sp--])
}

private fun Interpreter.nullop() {
    this.opStack[++this.sp] = null
}

private fun Interpreter.pop() {
    --this.sp
}

private fun Interpreter.dup() {
    val operand = this.opStack[this.sp]
    this.opStack[++this.sp] = operand
}

private fun Interpreter.swap() {
    val operand = this.opStack[this.sp - 1]
    this.opStack[this.sp - 1] = this.opStack[this.sp]
    this.opStack[this.sp] = operand
}

private fun Interpreter.assertop() {
    val actual = this.opStack[this.sp--]
    val expected = when (actual) {
        is Int -> consumeInstructionOperand().toIntBigEndian()
        is Float -> consumeInstructionOperand().toFloatBigEndian()
        is Char -> consumeInstructionOperand().toCharBigEndian()
        is String -> consumeInstructionOperand().toIntBigEndian().let {
            if (it < this.progDef.static.size)
                    (this.progDef.static[it] as StringLiteral).value
            else
                it
        }
        else -> false
    }

    if (actual != expected) {
        throw Exception("Assertion failed: $actual != $expected")
    }
}