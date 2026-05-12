package edu.stackvm.heap

import edu.stackvm.bytes.WORD_SIZE
import edu.stackvm.bytes.Word
import edu.stackvm.bytes.toIntBigEndian
import edu.stackvm.bytes.toWordBigEndian

class ArrayHeap(private val size: Int) : Heap {

    data class AddressPair(val previous: Int, val current: Int)

    private val NULL = -1
    private val wordStore: Array<Word> = Array(this.size) { UByteArray(WORD_SIZE) }

    private var freeBlocksHead = 0

    init {
        this.wordStore[this.freeBlocksHead] = (this.size - 1).toWordBigEndian()
        this.wordStore[this.freeBlocksHead + 1] = NULL.toWordBigEndian()
    }

    override fun allocate(blockSize: Int): Int {
        val freeBlockAddressPair = findFreeBlock(blockSize)
        val freeBlockSize = getBlockSize(freeBlockAddressPair.current)

        var newFreeBlockAddress = freeBlockAddressPair.current + blockSize + 1
        if (newFreeBlockAddress < this.size - 1) {
            setBlockSize(newFreeBlockAddress, freeBlockSize - blockSize - 1)
            setNextFreeBlockAddress(
                newFreeBlockAddress,
                getNextFreeBlockAddress(freeBlockAddressPair.current)
            )
        } else {
            newFreeBlockAddress = NULL
        }

        setBlockSize(freeBlockAddressPair.current, blockSize)
        this.clearBlock(freeBlockAddressPair.current)

        if (freeBlockAddressPair.previous == NULL) {
            this.freeBlocksHead = newFreeBlockAddress
        } else {
            setNextFreeBlockAddress(freeBlockAddressPair.previous, newFreeBlockAddress)
        }

        return freeBlockAddressPair.current
    }

    override fun free(address: Int) {
        clearBlock(address)

        setNextFreeBlockAddress(address, this.freeBlocksHead)
        this.freeBlocksHead = address

        coalesceFreeBlocks()
    }

    override fun store(address: Int, offset: Int, value: Word) {
        if (address + offset >= this.size) {
            error("Heap: Invalid access error, address: $address, offset: $offset, heap size: ${this.size}")
        }

        val blockSize = getBlockSize(address)
        if (offset >= blockSize) {
            error("Heap: Invalid access error, address: $address, offset: $offset, block size: $blockSize")
        }

        this.wordStore[address + 1 + offset] = value
    }

    override fun load(address: Int, offset: Int): Word {
        if (address + offset >= this.size) {
            error("Heap: Invalid access error, address: $address, offset: $offset, heap size: ${this.size}")
        }

        val blockSize = getBlockSize(address)
        if (offset >= blockSize) {
            error("Heap: Invalid access error, address: $address, offset: $offset, block size: $blockSize")
        }

        return this.wordStore[address + 1 + offset]
    }

    private fun coalesceFreeBlocks() {
        val freeBlocksSet = freeBlocksListToSet()

        var current = this.freeBlocksHead
        while (current != NULL) {
            val currentBlockSize = getBlockSize(current)

            val contiguous = current + currentBlockSize + 1
            if (contiguous in freeBlocksSet) {
                val newSize = getBlockSize(current) + getBlockSize(contiguous) + 1
                setBlockSize(current, newSize)
                freeBlocksSet.remove(contiguous)

                rewireFreeBlocks(freeBlocksSet.toList().sorted())

                current = this.freeBlocksHead
            } else {
                current = getNextFreeBlockAddress(current)
            }
        }
    }

    private fun rewireFreeBlocks(freeBlocksList: List<Int>) {
        this.freeBlocksHead = freeBlocksList[0]
        var current = this.freeBlocksHead
        for (i in 1 until freeBlocksList.size) {
            setNextFreeBlockAddress(current, freeBlocksList[i])
            current = freeBlocksList[i]
        }

        setNextFreeBlockAddress(current, NULL)
    }

    private fun freeBlocksListToSet(): HashSet<Int> {
        val set = hashSetOf<Int>()
        var current = this.freeBlocksHead
        while (current != NULL) {
            set.add(current)
            current = getNextFreeBlockAddress(current)
        }

        return set
    }

    private fun clearBlock(address: Int) {
        val blockSize = getBlockSize(address)
        for (i in address + 1 until address + blockSize + 1) {
            this.wordStore[i] = 0.toWordBigEndian()
        }
    }

    private fun findFreeBlock(blockSize: Int): AddressPair {
        var previous = NULL
        var current = this.freeBlocksHead

        if (this.freeBlocksHead == NULL) {
            error("Heap: Out of memory error, no free block of size $blockSize")
        }

        while (current != NULL && getBlockSize(current) < blockSize) {
            previous = current
            current = getNextFreeBlockAddress(current)
        }

        if (current == NULL) {
            error("Heap: Out of memory error, no free block of size $blockSize")
        } else {
            return AddressPair(previous, current)
        }
    }

    private fun getBlockSize(freeBlockAddress: Int): Int = this.wordStore[freeBlockAddress].toIntBigEndian()

    private fun setBlockSize(freeBlockAddress: Int, blockSize: Int) {
        this.wordStore[freeBlockAddress] = blockSize.toWordBigEndian()
    }

    private fun getNextFreeBlockAddress(currentFreeBlockAddress: Int): Int =
        this.wordStore[currentFreeBlockAddress + 1].toIntBigEndian()

    private fun setNextFreeBlockAddress(currentFreeBlockAddress: Int, nextFreeBlockAddress: Int) {
        this.wordStore[currentFreeBlockAddress + 1] = nextFreeBlockAddress.toWordBigEndian()
    }

}