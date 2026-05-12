package edu.stackvm.progdef

enum class Bytecode(val code: UByte, val instruction: String, val operandCount: Int = 0) {

    IADD(1u,"iadd"),     // integer addition
    ISUB(2u,"isub"),     // integer substraction
    IMUL(3u,"imul"),     // integer multiplication
    ILT(4u,"ilt"),      // integer less than
    IEQ(5u,"ieq"),      // integer equality
    FADD(6u,"fadd"),     // floating point addition
    FSUB(7u,"fsub"),     // floating point substraction
    FMUL(8u,"fmul"),     // floating point multiplication
    FLT(9u,"flt"),      // floating point less than
    FEQ(10u,"feq"),     // floating point equality
    ITOF(11u,"itof"),    // integer to float conversion
    CALL(12u,"call", operandCount = 1),    // function call
    RET(13u,"ret"),     // function return
    JMP(14u,"jmp", operandCount = 1),    // jump to label
    JMPT(15u,"jmpt", operandCount = 1),   // jump if top is true (non-zero)
    JMPF(16u,"jmpf", operandCount = 1),   // jump if top is false (zero)
    CCONST(17u,"cconst", operandCount = 1),  // push character constant to stack
    ICONST(18u,"iconst", operandCount = 1),  // push integer constant to stack
    FCONST(19u,"fconst", operandCount = 1),  // push floating point constant to stack
    SCONST(20u,"sconst", operandCount = 1),  // push string constant to stack
    LOAD(21u,"load", operandCount = 1),    // push local variable to stack
    GLOAD(22u,"gload", operandCount = 1),   // push global variable to stack
    STORE(23u,"store", operandCount = 1),   // set a local variable value from the stack pop
    GSTORE(24u,"gstore", operandCount = 1),  // set a global variable value from the stack pop
    PRINT(25u,"print"),   // print stack pop to stdout
    NULL(26u,"null"),    // push a null pointer to stack
    POP(27u,"pop"),     // stack pop and throw away value
    HALT(28u,"halt"),    // stop program execution
    DUP(29u, "dup"),    // duplicates the top of the stack
    SWAP(30u, "swap"),  // swaps the top two operands on the stack
    ASSERT(31u, "assert", operandCount = 1), // asserts that the top of the stack is equal to the given operand
    ALLOC(32u, "alloc"), // allocates a new memory block on the heap
    FREE(33u, "free"), // frees a memory block on the heap
    HSTORE(34u, "hstore", operandCount = 1), // stores a value in a heap block
    HILOAD(35u, "hiload", operandCount = 1), // loads an integer value from a heap block
    HFLOAD(36u, "hfload", operandCount = 1), // loads a floating point value from a heap block
    HCLOAD(37u, "hcload", operandCount = 1), // loads a character value from a heap block
    HSLOAD(38u, "hsload", operandCount = 1); // loads a string value from a heap block


    companion object {
        private val instructionMap = entries.associateBy(Bytecode::instruction)
        private val bytecodeMap = entries.associateBy { it.code }

        fun fromInstruction(instruction: String) = instructionMap[instruction]
        fun fromBytecode(bytecode: UByte) = bytecodeMap[bytecode]
    }

}