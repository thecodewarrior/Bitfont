package games.thecodewarrior.bitfonteditor.util

/**
 * Creates an iterator over all the combinations of the elements of passed sequences. The returned sequence iterates
 * over the passed sequences as if it was using a depth-first search with later sequences being deeper.
 *
 * e.g. passing `[A, B, C], [1, 2, 3]` will iterate in the following order: `A1, A2, A3, B1, B2, B3, C1, C2, C3`
 */
fun <T> combinationsOf(sequences: Array<Sequence<T>>): Sequence<List<T>> {
    return CombinationSequence(sequences.toList()) { ArrayList(it) }
}

private class CombinationSequence<T>(val sequences: List<out Sequence<*>>, val constructor: (List<*>) -> Any): Sequence<T> {
    override fun iterator(): Iterator<T> {
        return CombinationIterator()
    }

    inner class CombinationIterator: Iterator<T> {
        private val iterators = sequences.map { it.iterator() }.toMutableList()
        init {
            if(!iterators.all { it.hasNext() })
                throw IllegalArgumentException("All iterators must contain at least one element")
        }
        private val values = iterators.map { it.next() }.toMutableList()

        override fun hasNext(): Boolean {
            return iterators.any { it.hasNext() }
        }

        @Suppress("UNCHECKED_CAST")
        override fun next(): T {
            nextElement(iterators.lastIndex)
            return constructor(values) as T
        }

        private fun nextElement(i: Int) {
            if(i < 0) return // recursion escape
            if(!iterators[i].hasNext()) {
                iterators[i] = sequences[i].iterator()
                nextElement(i-1)
            }
            if(!iterators[i].hasNext())
                throw IllegalArgumentException("All iterators must always contain at least one element")
            values[i] = iterators[i].next()
        }
    }
}

// ============== GENERATED ================


/**
 * Creates an iterator over all the combinations of the elements of passed sequences. The returned sequence iterates
 * over the passed sequences as if it was using a depth-first search with later sequences being deeper.
 *
 * e.g. passing `[A, B, C], [1, 2, 3]` will iterate in the following order: `A1, A2, A3, B1, B2, B3, C1, C2, C3`
 */
fun <A, B> combinationsOf(iterableA: Iterable<A>, iterableB: Iterable<B>): Sequence<Combination2<A, B>> {
    return combinationsOf(iterableA.asSequence(), iterableB.asSequence())
}

/**
 * Creates an iterator over all the combinations of the elements of passed sequences. The returned sequence iterates
 * over the passed sequences as if it was using a depth-first search with later sequences being deeper.
 *
 * e.g. passing `[A, B, C], [1, 2, 3]` will iterate in the following order: `A1, A2, A3, B1, B2, B3, C1, C2, C3`
 */
fun <A, B> combinationsOf(sequenceA: Sequence<A>, sequenceB: Sequence<B>): Sequence<Combination2<A, B>> {
    return CombinationSequence(listOf(sequenceA, sequenceB)) { Combination2<Any?, Any?>(it) }
}

data class Combination2<A, B>(val a: A, val b: B) {
    @Suppress("UNCHECKED_CAST")
    constructor(list: List<*>) : this(list[0] as A, list[1] as B)
}


/**
 * Creates an iterator over all the combinations of the elements of passed sequences. The returned sequence iterates
 * over the passed sequences as if it was using a depth-first search with later sequences being deeper.
 *
 * e.g. passing `[A, B, C], [1, 2, 3]` will iterate in the following order: `A1, A2, A3, B1, B2, B3, C1, C2, C3`
 */
fun <A, B, C> combinationsOf(iterableA: Iterable<A>, iterableB: Iterable<B>, iterableC: Iterable<C>): Sequence<Combination3<A, B, C>> {
    return combinationsOf(iterableA.asSequence(), iterableB.asSequence(), iterableC.asSequence())
}

/**
 * Creates an iterator over all the combinations of the elements of passed sequences. The returned sequence iterates
 * over the passed sequences as if it was using a depth-first search with later sequences being deeper.
 *
 * e.g. passing `[A, B, C], [1, 2, 3]` will iterate in the following order: `A1, A2, A3, B1, B2, B3, C1, C2, C3`
 */
fun <A, B, C> combinationsOf(sequenceA: Sequence<A>, sequenceB: Sequence<B>, sequenceC: Sequence<C>): Sequence<Combination3<A, B, C>> {
    return CombinationSequence(listOf(sequenceA, sequenceB, sequenceC)) { Combination3<Any?, Any?, Any?>(it) }
}

data class Combination3<A, B, C>(val a: A, val b: B, val c: C) {
    @Suppress("UNCHECKED_CAST")
    constructor(list: List<*>) : this(list[0] as A, list[1] as B, list[2] as C)
}


/**
 * Creates an iterator over all the combinations of the elements of passed sequences. The returned sequence iterates
 * over the passed sequences as if it was using a depth-first search with later sequences being deeper.
 *
 * e.g. passing `[A, B, C], [1, 2, 3]` will iterate in the following order: `A1, A2, A3, B1, B2, B3, C1, C2, C3`
 */
fun <A, B, C, D> combinationsOf(iterableA: Iterable<A>, iterableB: Iterable<B>, iterableC: Iterable<C>, iterableD: Iterable<D>): Sequence<Combination4<A, B, C, D>> {
    return combinationsOf(iterableA.asSequence(), iterableB.asSequence(), iterableC.asSequence(), iterableD.asSequence())
}

/**
 * Creates an iterator over all the combinations of the elements of passed sequences. The returned sequence iterates
 * over the passed sequences as if it was using a depth-first search with later sequences being deeper.
 *
 * e.g. passing `[A, B, C], [1, 2, 3]` will iterate in the following order: `A1, A2, A3, B1, B2, B3, C1, C2, C3`
 */
fun <A, B, C, D> combinationsOf(sequenceA: Sequence<A>, sequenceB: Sequence<B>, sequenceC: Sequence<C>, sequenceD: Sequence<D>): Sequence<Combination4<A, B, C, D>> {
    return CombinationSequence(listOf(sequenceA, sequenceB, sequenceC, sequenceD)) { Combination4<Any?, Any?, Any?, Any?>(it) }
}

data class Combination4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D) {
    @Suppress("UNCHECKED_CAST")
    constructor(list: List<*>) : this(list[0] as A, list[1] as B, list[2] as C, list[3] as D)
}


/**
 * Creates an iterator over all the combinations of the elements of passed sequences. The returned sequence iterates
 * over the passed sequences as if it was using a depth-first search with later sequences being deeper.
 *
 * e.g. passing `[A, B, C], [1, 2, 3]` will iterate in the following order: `A1, A2, A3, B1, B2, B3, C1, C2, C3`
 */
fun <A, B, C, D, E> combinationsOf(iterableA: Iterable<A>, iterableB: Iterable<B>, iterableC: Iterable<C>, iterableD: Iterable<D>, iterableE: Iterable<E>): Sequence<Combination5<A, B, C, D, E>> {
    return combinationsOf(iterableA.asSequence(), iterableB.asSequence(), iterableC.asSequence(), iterableD.asSequence(), iterableE.asSequence())
}

/**
 * Creates an iterator over all the combinations of the elements of passed sequences. The returned sequence iterates
 * over the passed sequences as if it was using a depth-first search with later sequences being deeper.
 *
 * e.g. passing `[A, B, C], [1, 2, 3]` will iterate in the following order: `A1, A2, A3, B1, B2, B3, C1, C2, C3`
 */
fun <A, B, C, D, E> combinationsOf(sequenceA: Sequence<A>, sequenceB: Sequence<B>, sequenceC: Sequence<C>, sequenceD: Sequence<D>, sequenceE: Sequence<E>): Sequence<Combination5<A, B, C, D, E>> {
    return CombinationSequence(listOf(sequenceA, sequenceB, sequenceC, sequenceD, sequenceE)) { Combination5<Any?, Any?, Any?, Any?, Any?>(it) }
}

data class Combination5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E) {
    @Suppress("UNCHECKED_CAST")
    constructor(list: List<*>) : this(list[0] as A, list[1] as B, list[2] as C, list[3] as D, list[4] as E)
}

