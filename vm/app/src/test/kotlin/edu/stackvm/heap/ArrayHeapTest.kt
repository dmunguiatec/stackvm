package edu.stackvm.heap

import edu.stackvm.bytes.toWordBigEndian
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArrayHeapTest {

    @Test
    fun allocate_returns_first_free_block_address() {
        val heap = ArrayHeap(32)

        val address = heap.allocate(4)

        assertEquals(0, address)
    }

    @Test
    fun allocate_returns_next_available_block_after_previous_allocation() {
        val heap = ArrayHeap(32)

        val first = heap.allocate(4)
        val second = heap.allocate(3)

        assertEquals(0, first)
        assertEquals(5, second)
    }

    @Test
    fun free_reuses_freed_block() {
        val heap = ArrayHeap(32)

        val first = heap.allocate(4)
        heap.allocate(3)

        heap.free(first)

        val reused = heap.allocate(4)

        assertEquals(first, reused)
    }

    @Test
    fun allocate_throws_when_out_of_memory() {
        val heap = ArrayHeap(8)

        heap.allocate(6)

        val exception = assertFailsWith<IllegalStateException> {
            heap.allocate(2)
        }

        assertEquals(
            "Heap: Out of memory error, no free block of size 2",
            exception.message
        )
    }

    @Test
    fun free_coalesces_adjacent_blocks() {
        val heap = ArrayHeap(32)

        val first = heap.allocate(4)
        val second = heap.allocate(3)

        heap.free(first)
        heap.free(second)

        val large = heap.allocate(8)

        assertEquals(0, large)
    }

    @Test
    fun free_non_adjacent_blocks_do_not_coalesce() {
        val heap = ArrayHeap(32)

        val first = heap.allocate(4)
        heap.allocate(2)
        val third = heap.allocate(3)

        heap.free(first)
        heap.free(third)

        val reusedFirst = heap.allocate(4)

        assertEquals(first, reusedFirst)
    }

    @Test
    fun multiple_allocations_and_frees_preserve_heap_reusability() {
        val heap = ArrayHeap(64)

        val a = heap.allocate(4)
        val b = heap.allocate(5)
        val c = heap.allocate(6)

        heap.free(b)
        heap.free(a)

        val d = heap.allocate(4)
        val e = heap.allocate(5)

        assertEquals(a, d)
        assertEquals(b, e)

        // Remaining allocation should still succeed.
        val f = heap.allocate(6)

        assertEquals(c + 7, f)
    }

    @Test
    fun allocate_exact_remaining_block_size_succeeds() {
        val heap = ArrayHeap(16)

        val first = heap.allocate(4)
        val second = heap.allocate(10)

        assertEquals(0, first)
        assertEquals(5, second)
    }

    @Test
    fun freeing_blocks_in_reverse_order_allows_large_reallocation() {
        val heap = ArrayHeap(64)

        val a = heap.allocate(5)
        val b = heap.allocate(5)
        val c = heap.allocate(5)

        heap.free(c)
        heap.free(b)
        heap.free(a)

        val large = heap.allocate(17)

        assertEquals(0, large)
    }

    @Test
    fun store_writes_value_at_offset_zero() {
        val heap = ArrayHeap(32)
        val address = heap.allocate(4)

        val value = 1234.toWordBigEndian()

        heap.store(address, 0, value)

        val loaded = heap.load(address, 0)

        assertContentEquals(value, loaded)
    }

    @Test
    fun store_writes_value_at_non_zero_offset() {
        val heap = ArrayHeap(32)
        val address = heap.allocate(4)

        val value = 5678.toWordBigEndian()

        heap.store(address, 2, value)

        val loaded = heap.load(address, 2)

        assertContentEquals(value, loaded)
    }

    @Test
    fun store_overwrites_previous_value() {
        val heap = ArrayHeap(32)
        val address = heap.allocate(4)

        val first = 1111.toWordBigEndian()
        val second = 2222.toWordBigEndian()

        heap.store(address, 1, first)
        heap.store(address, 1, second)

        val loaded = heap.load(address, 1)

        assertContentEquals(second, loaded)
    }

    @Test
    fun store_does_not_modify_other_offsets() {
        val heap = ArrayHeap(32)
        val address = heap.allocate(4)

        val first = 100.toWordBigEndian()
        val second = 200.toWordBigEndian()

        heap.store(address, 0, first)
        heap.store(address, 1, second)

        assertContentEquals(first, heap.load(address, 0))
        assertContentEquals(second, heap.load(address, 1))
    }

    @Test
    fun store_throws_when_offset_exceeds_block_size() {
        val heap = ArrayHeap(32)
        val address = heap.allocate(4)

        val exception = assertFailsWith<IllegalStateException> {
            heap.store(address, 4, 1.toWordBigEndian())
        }

        assertEquals(
            "Heap: Invalid access error, address: 0, offset: 4, block size: 4",
            exception.message
        )
    }

    @Test
    fun store_throws_when_address_plus_offset_exceeds_heap_size() {
        val heap = ArrayHeap(8)
        val address = heap.allocate(2)

        val exception = assertFailsWith<IllegalStateException> {
            heap.store(7, 2, 1.toWordBigEndian())
        }

        assertEquals(
            "Heap: Invalid access error, address: 7, offset: 2, heap size: 8",
            exception.message
        )
    }

    @Test
    fun load_returns_zero_initialized_word() {
        val heap = ArrayHeap(32)
        val address = heap.allocate(4)

        val loaded = heap.load(address, 0)

        assertContentEquals(0.toWordBigEndian(), loaded)
    }

    @Test
    fun load_returns_value_stored_at_offset_zero() {
        val heap = ArrayHeap(32)
        val address = heap.allocate(4)

        val value = 999.toWordBigEndian()
        heap.store(address, 0, value)

        val loaded = heap.load(address, 0)

        assertContentEquals(value, loaded)
    }

    @Test
    fun load_returns_value_stored_at_non_zero_offset() {
        val heap = ArrayHeap(32)
        val address = heap.allocate(4)

        val value = 4321.toWordBigEndian()
        heap.store(address, 3, value)

        val loaded = heap.load(address, 3)

        assertContentEquals(value, loaded)
    }

    @Test
    fun load_returns_distinct_values_for_distinct_offsets() {
        val heap = ArrayHeap(32)
        val address = heap.allocate(4)

        val first = 10.toWordBigEndian()
        val second = 20.toWordBigEndian()

        heap.store(address, 0, first)
        heap.store(address, 2, second)

        assertContentEquals(first, heap.load(address, 0))
        assertContentEquals(second, heap.load(address, 2))
    }

    @Test
    fun load_throws_when_offset_exceeds_block_size() {
        val heap = ArrayHeap(32)
        val address = heap.allocate(4)

        val exception = assertFailsWith<IllegalStateException> {
            heap.load(address, 4)
        }

        assertEquals(
            "Heap: Invalid access error, address: 0, offset: 4, block size: 4",
            exception.message
        )
    }

    @Test
    fun load_throws_when_address_plus_offset_exceeds_heap_size() {
        val heap = ArrayHeap(8)

        val exception = assertFailsWith<IllegalStateException> {
            heap.load(7, 2)
        }

        assertEquals(
            "Heap: Invalid access error, address: 7, offset: 2, heap size: 8",
            exception.message
        )
    }

    @Test
    fun load_returns_zero_after_block_is_freed_and_reallocated() {
        val heap = ArrayHeap(32)

        val address = heap.allocate(4)

        heap.store(address, 0, 123.toWordBigEndian())

        heap.free(address)

        val reused = heap.allocate(4)

        val loaded = heap.load(reused, 0)

        assertContentEquals(0.toWordBigEndian(), loaded)
    }
}