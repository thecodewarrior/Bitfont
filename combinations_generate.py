#!/usr/bin/env python3
chars      = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
lowerChars = "abcdefghijklmnopqrstuvwxyz"

for count in range(2, 6):
    args = ", ".join([chars[i] for i in range(0, count)])
    iterable_params = ", ".join(["iterable" + chars[i] + ": Iterable<" + chars[i] + ">" for i in range(0, count)])
    iterable_convert = ", ".join(["iterable" + chars[i] + ".asSequence()" for i in range(0, count)])
    sequence_params = ", ".join(["sequence" + chars[i] + ": Sequence<" + chars[i] + ">" for i in range(0, count)])
    constructor_params = ", ".join(["sequence" + chars[i] for i in range(0, count)])
    constructor_type_args = ", ".join(["Any?" for i in range(0, count)])
    combination_vals = ", ".join(["val " + lowerChars[i] + ": " + chars[i] for i in range(0, count)])
    combination_constructor_params = ", ".join(["list[" + str(i) + "] as " + chars[i] for i in range(0, count)])

    print(f"""
/**
 * Creates an iterator over all the combinations of the elements of passed sequences. The returned sequence iterates
 * over the passed sequences as if it was using a depth-first search with later sequences being deeper.
 *
 * e.g. passing `[A, B, C], [1, 2, 3]` will iterate in the following order: `A1, A2, A3, B1, B2, B3, C1, C2, C3`
 */
fun <{args}> combinationsOf({iterable_params}): Sequence<Combination{count}<{args}>> {{
    return combinationsOf({iterable_convert})
}}

/**
 * Creates an iterator over all the combinations of the elements of passed sequences. The returned sequence iterates
 * over the passed sequences as if it was using a depth-first search with later sequences being deeper.
 *
 * e.g. passing `[A, B, C], [1, 2, 3]` will iterate in the following order: `A1, A2, A3, B1, B2, B3, C1, C2, C3`
 */
fun <{args}> combinationsOf({sequence_params}): Sequence<Combination{count}<{args}>> {{
    return CombinationSequence(listOf({constructor_params})) {{ Combination{count}<{constructor_type_args}>(it) }}
}}

data class Combination{count}<{args}>({combination_vals}) {{
    @Suppress("UNCHECKED_CAST")
    constructor(list: List<*>) : this({combination_constructor_params})
}}
    """)
