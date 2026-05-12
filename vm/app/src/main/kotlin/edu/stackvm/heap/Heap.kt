package edu.stackvm.heap

import edu.stackvm.bytes.Word

interface Heap {
    fun allocate(blockSize: Int): Int
    fun free(address: Int)
    fun store(address: Int, offset: Int, value: Word)
    fun load(address: Int, offset: Int): Word
}