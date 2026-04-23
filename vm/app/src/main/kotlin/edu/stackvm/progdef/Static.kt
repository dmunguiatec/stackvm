package edu.stackvm.progdef

sealed interface StaticObject

data class StringLiteral(val value: String) : StaticObject
data class FuncDef(val identifier: String, val args_count: Int, val locals_count: Int, val code_address: Int) : StaticObject