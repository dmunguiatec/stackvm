package edu.stackvm.interpreter

import edu.stackvm.progdef.FuncDef

data class StackFrame(
    val funcDef: FuncDef,
    val returnAddress: Int,
) {
    val locals: Array<Any?> = arrayOfNulls(funcDef.args_count + funcDef.locals_count)
}
