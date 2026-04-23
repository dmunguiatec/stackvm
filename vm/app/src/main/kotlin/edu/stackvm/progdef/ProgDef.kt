package edu.stackvm.progdef

data class ProgDef(
    val code: List<UByte>,
    val static: List<StaticObject>,
    val globalsCount: Int,
    val entryPoint: FuncDef
)