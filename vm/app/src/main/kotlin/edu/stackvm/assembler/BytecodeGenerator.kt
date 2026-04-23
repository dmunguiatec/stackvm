package edu.stackvm.assembler

import AssemblerParser
import edu.stackvm.bytes.WORD_LSB
import edu.stackvm.bytes.WORD_MID_HIGH
import edu.stackvm.bytes.WORD_MID_LOW
import edu.stackvm.bytes.WORD_MSB
import edu.stackvm.bytes.Word
import edu.stackvm.bytes.toWordBigEndian
import edu.stackvm.progdef.Bytecode
import edu.stackvm.progdef.FuncDef
import edu.stackvm.progdef.ProgDef
import edu.stackvm.progdef.StaticObject
import edu.stackvm.progdef.StringLiteral
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.TokenStream

private const val ENTRYPOINT_IDENTIFIER = "main"
private const val INDEX_NOT_FOUND = -1
private const val TOKEN_CHAR_POSITION = 1

class BytecodeGenerator(input: TokenStream) : AssemblerParser(input) {

    private val code = mutableListOf<UByte>()
    private val static = mutableListOf<StaticObject>()
    private val symbolTable = mutableMapOf<String, Label>()

    private var globalsCount: Int = 0
    private var entryPoint: FuncDef? = null
    private var ip = 0

    override fun gen(instrToken: Token?) {
        val instruction = instrToken?.text
        if (instruction == null) {
            error("[line ${instrToken?.line}] missing instruction")
        }
        val bytecode = Bytecode.fromInstruction(instruction)
        if (bytecode == null) {
            error("[line ${instrToken.line}] unknown instruction: $instruction")
        }

        this.ip++
        this.code.add(bytecode.code)
    }

    override fun gen(instrToken: Token?, operandToken: Token?) {
        gen(instrToken)

        val word: Word = when (operandToken?.type) {
            INT -> operandToken.text.toInt().toWordBigEndian()
            FLOAT -> operandToken.text.toFloat().toWordBigEndian()
            CHAR -> operandToken.text[TOKEN_CHAR_POSITION].toWordBigEndian()
            STRING -> getStringLiteralAddress(operandToken.text).toWordBigEndian()
            ID -> getLabelAddress(operandToken.text).toWordBigEndian()
            FUNC -> getFunctionAddress(operandToken.text).toWordBigEndian()
            else -> error("[line ${operandToken?.line}] unexpected operand type: ${operandToken?.type}")
        }

        this.ip += word.size
        this.code.addAll(word.asList())

    }

    override fun defineDataSize(n: Int) {
        this.globalsCount = n
    }

    override fun defineFunction(idToken: Token?, nargs: Int, nlocals: Int) {
        if (idToken == null) {
            error(".def with missing function identifier")
        }

        val funcDef = FuncDef(idToken.text, nargs, nlocals, this.ip)

        if (funcDef.identifier == ENTRYPOINT_IDENTIFIER) {
            this.entryPoint = funcDef
        }

        val staticAddress = this.static.indexOfFirst { it is FuncDef && it.identifier == funcDef.identifier }
        if (staticAddress != INDEX_NOT_FOUND) {
            this.static[staticAddress] = funcDef
        } else {
            this.static.add(funcDef)
        }
    }

    private fun getFunctionAddress(identifier: String): Int {
        var staticAddress = this.static.indexOfFirst { it is FuncDef && it.identifier == identifier }
        if (staticAddress == INDEX_NOT_FOUND) {
            this.static.add(FuncDef(identifier, 0, 0, 0))
            staticAddress = this.static.size - 1
        }
        return staticAddress
    }

    override fun defineLabel(idToken: Token?) {
        if (idToken == null) {
            error("expecting label identifier")
        }

        val forwardReferenceLabel = this.symbolTable[idToken.text]
        if (forwardReferenceLabel != null) {
            if (forwardReferenceLabel.isDefined) {
                error("[line ${idToken.line}] label ${forwardReferenceLabel.label} already defined at address ${forwardReferenceLabel.address}")
            }
            if (forwardReferenceLabel.isForwardReference) {
                resolveLabelForwardReferences(Label(idToken.text, this.ip, forwardReferences = forwardReferenceLabel.forwardReferences))
            }
        }
        this.symbolTable[idToken.text] = Label(idToken.text, this.ip)

    }

    private fun getLabelAddress(identifier: String): Int {
        var address = 0
        val label = this.symbolTable[identifier]
        if (label == null) {
            this.symbolTable[identifier] =
                Label(
                    label = identifier,
                    address = 0,
                    isDefined = false,
                    isForwardReference = true,
                    forwardReferences = mutableListOf(this.ip)
                )
        } else if (label.isForwardReference) {
            label.forwardReferences.add(this.ip)
        } else {
            address = label.address
        }

        return address
    }

    private fun resolveLabelForwardReferences(label: Label) {
        val word = label.address.toWordBigEndian()
        label.forwardReferences.forEach { address ->
            this.code[address + WORD_MSB]      = word[WORD_MSB]
            this.code[address + WORD_MID_HIGH] = word[WORD_MID_HIGH]
            this.code[address + WORD_MID_LOW]  = word[WORD_MID_LOW]
            this.code[address + WORD_LSB]      = word[WORD_LSB]
        }
    }

    override fun checkForUnresolvedReferences() {
        symbolTable.forEach { (_, label) ->
            if (!label.isDefined) {
                error("unresolved label: ${label.label} at address ${label.address}")
            }
        }
    }

    private fun getStringLiteralAddress(value: String): Int {
        val stringLiteral = StringLiteral(value)
        if (stringLiteral in this.static) {
            return this.static.indexOf(stringLiteral)
        } else {
            this.static.add(stringLiteral)
            return this.static.size - 1
        }
    }

    fun buildProgDef(): ProgDef {
        if (this.entryPoint == null) {
            val funcDef = FuncDef(ENTRYPOINT_IDENTIFIER, 0, 0, 0)
            this.static.add(funcDef)
            this.entryPoint = funcDef
        }

        return ProgDef(
            this.code,
            this.static,
            this.globalsCount,
            this.entryPoint!!
        )
    }
}
