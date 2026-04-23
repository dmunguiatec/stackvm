package edu.stackvm.assembler

data class Label(
    val label: String,
    val address: Int,
    val isDefined: Boolean = true,
    val isForwardReference: Boolean = false,
    val forwardReferences: MutableList<Int> = mutableListOf()
)