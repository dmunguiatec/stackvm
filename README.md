# stackvm

Simple stack based bytecode virtual machine

---

## Table of Contents

* [Overview](#overview)
* [Setup](#setup)
* [Usage](#usage)
* [Virtual Machine Instructions](#virtual-machine-instructions)
    * [Constants & Literals](#constants--literals)
        * [cconst](#cconst)
        * [iconst](#iconst)
        * [fconst](#fconst)
        * [sconst](#sconst)
        * [null](#null)
      
    * [Arithmetic Operations](#arithmetic-operations)
        * [iadd](#iadd)
        * [isub](#isub)
        * [imul](#imul)
        * [fadd](#fadd)
        * [fsub](#fsub)
        * [fmul](#fmul)
      
    * [Comparisons](#comparisons)
        * [ilt](#ilt)
        * [ieq](#ieq)
        * [flt](#flt)
        * [feq](#feq)
      
    * [Type Conversion](#type-conversion)
        * [itof](#itof)
        * [ctoi](#ctoi)
      
    * [Functions](#functions)
        * [call](#call)
        * [ret](#ret)
      
    * [Control Flow](#control-flow)
        * [jmp](#jmp)
        * [jmpt](#jmpt)
        * [jmpf](#jmpf)
        * [halt](#halt)
      
    * [Memory Access](#memory-access)
        * [load](#load)
        * [store](#store)
        * [gload](#gload)
        * [gstore](#gstore)
        * [alloc](#alloc)
        * [free](#free)
        * [hstore](#hstore)
        * [hiload](#hiload)
        * [hfload](#hfload)
        * [hcload](#hcload)
        * [hsload](#hsload)
      
    * [String Operations](#string-operations)
        * [slen](#slen)
        * [sget](#sget)
    
    * [Input / Output](#input--output)
        * [print](#print)
      
    * [Stack Manipulation](#stack-manipulation)
        * [pop](#pop)
        * [dup](#dup)
        * [swap](#swap)

---

### Overview

stackvm is a simple stack-based virtual machine for assembling, disassembling, and executing custom bytecode programs.

It provides:

* A bytecode assembler
* A disassembler
* A runtime execution environment
* A small instruction set for arithmetic, control flow, memory access, and function calls

### Setup

Assuming you are in the root directory of the project, to build run

```bash
cd vm && ./gradlew clean :app:generateGrammarSource installDist && cd ..
```

After building, you can install the executable by running

```bash
sudo ./stackvm-installer.sh install
```

To uninstall,

```bash
sudo ./stackvm-installer.sh uninstall
```

---

### Usage

After installing, you can run the assembler to generate a bytecode executable as follows

```bash
stackvm --mode assemble --input <.stkasm file> --output <.stk file>
```

To disassemble a binary bytecode file

```bash
stackvm --mode disassemble --input <.stk file>
```

To execute a binary bytecode file

```bash
stackvm --mode run --input <.stk file>
```

---

### Virtual Machine Instructions

---

## Constants & Literals

#### cconst

Pushes a literal character value onto the stack

```
cconst 'a' 
```

#### iconst

Pushes a literal integer value onto the stack

```
iconst 10
```

#### fconst

Pushes a literal floating point value onto the stack

```
fconst 1.1
```

#### sconst

Pushes a literal string value onto the stack

```
sconst "Hello World" 
```

#### null

Pushes a `null` value onto the stack.

```
null
print ; prints "null\n" to stdout
```

---

## Arithmetic Operations

#### iadd

Integer addition of two operands

```
iconst 10
iconst 20
iadd ; pushes 30 onto the stack
```

Pops operand B and operand A from the stack and pushes A+B onto the stack

#### isub

Integer subtraction of two operands

```
iconst 5
iconst 2
isub ; pushes 3 onto the stack
```

Pops operand B and operand A from the stack and pushes A-B onto the stack

#### imul

Multiplication of two operands

```
iconst 3
iconst 5
imul ; pushes 15 onto the stack
```

Pops operand B and operand A from the stack and pushes A*B onto the stack

#### fadd

Floating point addition of two operands

```
fconst 1.1
fconst 2.0
fadd ; pushes 3.1 onto the stack
```

Pops operand B and operand A from the stack and pushes A+B onto the stack

#### fsub

Floating point subtraction of two operands

```
fconst 3.1
fconst 2.0
fsub ; pushes 1.1 onto the stack
```

Pops operand B and operand A from the stack and pushes A-B onto the stack

#### fmul

Floating point multiplication of two operands

```
fconst 5.0
fconst 0.5
fmul ; pushes 2.5 onto the stack
```

Pops operand B and operand A from the stack and pushes A*B onto the stack

---

## Comparisons

#### ilt

Determines the "less than" relationship of two integer operands

```
iconst 10
iconst 20
ilt ; pushes 1 onto the stack
```

Pops operand B and operand A from the stack and pushes 1 if A < B otherwise pushes 0.

#### ieq

Determines the "equality" comparison of two integer operands

```
iconst 10
iconst 10
ieq ; pushes 1 onto the stack
```

Pops operand B and operand A from the stack and pushes 1 if A equals B otherwise pushes 0.

#### flt

Determines the "less than" relationship of two floating point operands

```
fconst 0.1
fconst 2.0
flt ; pushes 1 onto the stack
```

Pops operand B and operand A from the stack and pushes 1 if A < B otherwise pushes 0.

#### feq

Determines the "equality" comparison of two floating point operands

```
fconst 0.1
fconst 0.1
feq ; pushes 1 onto the stack
```

Pops operand B and operand A from the stack and pushes 1 if A equals B otherwise pushes 0.

---

## Type Conversion

#### itof

Converts an integer operand to a floating point operand

```
iconst 10
itof ; pushes 10.0 onto the stack
```
Pops operand A and pushes A as its corresponding floating point value onto the stack

#### ctoi

Converts a character operand to an integer operand

```
cconst 'a'
ctoi ; pushes 97 onto the stack 
```
Pops operand A and pushes A as its corresponding floating point value onto the stack

---

## Functions

#### call

Calls a function reading its arguments from the stack.

```
iconst 5
call fact() ; calls the fact(n) function with n = 5 and pushes the return value 120 onto the stack
```

When the function is defined, it's required to specify the number of arguments it takes.

```
.def identity: args=1, locals=0
    load 0; loads the first argument onto the stack
    ret
```

The function can then be called by pushing the arguments and using the instrucion `call`.

```
iconst 5
call identity() ; calls identity(5) and pushes the return value 5 onto the stack
```

#### ret

Returns control to the caller of the current function

```
.def inc: args=1, locals=0
    load 0 ; push n onto the stack
    iconst 1
    iadd ; n + 1
    ret ; returns control to the caller of the current function
```

The caller finds the function's return value at the top of the stack.

```
iconst 5
call inc()
print ; the call returns to this instruction and prints the return value 6 to stdout
```

---

## Control Flow

#### jmp

Jumps to the given label

```
loop:
    call process()
    jmp loop
```

#### jmpt

Jumps to the given label if the top of the stack is `1`

```
loop:
    iconst 1
    jmpt loop
```

#### jmpf

Jumps to the given label if the top of the stack is `0`

```
loop:
    iconst 0
    jmpf loop
```

#### halt

Stops execution immediately.

```
halt
```

---

## Memory Access

#### load

Loads the argument or local variable value addressed at the given index onto the stack.

Every function definition specifies the number of arguments and local variables it takes.

```
.def foo: args=3, locals=2
```

The vm allocates `args + locals`  slots in the stack frame,
and initializes the values of the arguments from the stack. So in the above example, the vm allocates 5 slots in the stack frame.
The first 3 slots are used for the arguments, and the last 2 slots are used for the local variables.

```
.def foo: args=3, locals=2
; locals:
; 0: arg_1
; 1: arg_2
; 2: arg_3
; 3: local_1
; 4: local_2
```

If the function has no arguments, then the local variables are indexed from 0.

So in the function code if we want to access the value of the first argument we can use the index `0`.

```
load 0 ; pushes the value of arg_1 onto the stack
load 4 ; pushes the value of local_2 onto the stack
```

#### store

(See `load` for more details)

Stores the value of the top of the stack at the local addressed by the given index.

```
iconst 10
store 3 ; locals[3] <- 10
```

#### gload

Loads the global variable value addressed at the given index onto the stack.

The number of global variables is specified in the header of the bytecode file using the directive `.globals` followed by and integer representing the number of global variables.

```
.globals 3
```

The above example specifies that there are 3 global variables. The global variables are indexed from 0.

```
gload 0 ; loads the value of global_0 onto the stack
```

#### gstore

(See `gload` for more details)

Stores the value of the top of the stack at the global addressed by the given index.

```
iconst 10
gstore 2 ; globals[2] <- 10
```

#### alloc

Allocates a memory block of the given size in the heap and pushes a pointer to the allocated memory.

```
; locals:
; 0: array_ptr

iconst 10
alloc ; pushes a pointer to the allocated memory onto the stack
store 0 ; array_ptr <- pointer to the allocated memory
```

#### free

Frees the heap memory block pointed to by the top of the stack.

```
; locals:
; 0: array_ptr

load 0 ; assuming array_ptr contains the pointer to the allocated memory, pushes it onto the stack
free 
```

#### hstore

Stores a value at the given offset in the heap memory block pointed to by the top of the stack.

```
; locals:
; 0: array_ptr

iconst 10
alloc ; allocates a memory block of size 10
store 0 ; array_ptr <- pointer to the allocated memory

fconst 3.14159 ; the value to be stored
iconst 0 ; offset i = 0
hstore 0 ; array_ptr[0] <- 3.14159
```

#### hiload

Loads a value from an allocated heap memory block at the given offset.

```
; locals:
; 0: array_ptr

iconst 10
alloc ; allocates a memory block of size 10
store 0 ; array_ptr <- pointer to the allocated memory

iconst 3; offset i = 3
hiload 0 ; pushes array_ptr[3] as an integer onto the stack
```

#### hfload

Loads a value from an allocated heap memory block at the given offset.

```
; locals:
; 0: array_ptr

iconst 10
alloc ; allocates a memory block of size 10
store 0 ; array_ptr <- pointer to the allocated memory

iconst 3; offset i = 3
hfload 0 ; pushes array_ptr[3] as a floating point number onto the stack
```

#### hcload

Loads a value from an allocated heap memory block at the given offset.

```
; locals:
; 0: array_ptr

iconst 10
alloc ; allocates a memory block of size 10
store 0 ; array_ptr <- pointer to the allocated memory

iconst 3; offset i = 3
hcload 0 ; pushes array_ptr[3] as a character onto the stack
```

#### hsload

Loads a value from an allocated heap memory block at the given offset.

```
; locals:
; 0: array_ptr

iconst 10
alloc ; allocates a memory block of size 10
store 0 ; array_ptr <- pointer to the allocated memory

iconst 3; offset i = 3
hsload 0 ; pushes array_ptr[3] as a string onto the stack
```
---
## String Operations

#### slen

Calculates the length of a string value on the stack.

```
sconst "Hello World"
slen ; pushes 11 onto the stack  
```

#### sget

Calculates the character at the given zero-based index in a string value on the stack.

```
sconst "Hello World"
iconst 6
sget ; pushes character 'W' onto the stack 
```
---

## Input / Output

#### print

Prints the value of the top of the stack to `stdout`. The print instruction always includes a newline character.

```
.def main: args=0, locals=0
    sconst "Hola mundo!"
    print ; prints "Hola mundo!\n" to stdout

    iconst 5
    print ; prints "5\n" to stdout

    fconst 3.14159
    print ; prints "3.14159\n" to stdout

    cconst 'x'
    print ; prints "x\n" to stdout

    null
    print ; prints "null\n" to stdout

    halt
```

The instruction allows values of any type to be printed.

---

## Stack Manipulation

#### pop

Removes the top of the stack. The value at the top of the stack is discarded.

```
iconst 10
iconst 20
pop
print ; prints "10\n" to stdout
```

#### dup

Copies the value at the top of the stack onto the top of the stack.

```
iconst 1
dup ; pushes 1 onto the stack
iadd ; pushes 2 onto the stack
```

#### swap

Swaps the order of the top two values on the stack.

```
iconst 1
cconst 'a' ; stack top is 'a' followed by 1
swap ; stack top is 1 followed by 'a'
```
