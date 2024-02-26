package de.westnordost.osmfeatures

import kotlin.jvm.JvmOverloads

/** Index that makes finding strings that start with characters very efficient.
 * It sorts the strings into a tree structure with configurable depth.
 *
 * It is threadsafe because it is immutable.
 *
 * For the strings ["f", "foobar", "foo", "fu", "funicular"], the tree may internally look f.e.
 * like this:
 *
 * ```
 * f ->
 *   [ "f" ]
 *   o ->
 *     o ->
 *       [ "foobar", "foo", ...]
 *   u ->
 *     [ "fu", "funicular", ... ]
 * ```
 * */
internal class StartsWithStringTree
/** Create this index with the given strings.
 *
 * The generated tree will have a max depth of maxDepth and another depth is not added to the
 * tree if there are less than minContainerSize strings in one tree node.
 */
@JvmOverloads constructor(
    strings: Collection<String>,
    maxDepth: Int = 16,
    minContainerSize: Int = 16
) {
    private val root: Node = buildTree(
        strings,
        0,
        maxDepth.coerceAtLeast(0),
        minContainerSize.coerceAtLeast(1)
    )

    /** Get all strings which start with the given string  */
    fun getAll(startsWith: String?): List<String> {
        return root.getAll(startsWith, 0)
    }

    private class Node(val children: Map<Char, Node>?, val strings: Collection<String>) {

        /** Get all strings that start with the given string  */
        fun getAll(startsWith: String?, offset: Int): List<String> {
            if (startsWith != null) {
                if (startsWith.isEmpty()) return emptyList()
            }

            val result: MutableList<String> = ArrayList()
            if (children != null) {
                for ((key, value) in children) {
                    if (startsWith != null) {
                        if (startsWith.length <= offset || key == startsWith[offset]) {
                            result.addAll(value.getAll(startsWith, offset + 1))
                        }
                    }
                }
            }
            for (string in strings) {
                if (startsWith?.let { string.startsWith(it) } == true) result.add(string)
            }
            return result
        }
    }

    companion object {
        private fun buildTree(
            strings: Collection<String>,
            currentDepth: Int,
            maxDepth: Int,
            minContainerSize: Int
        ): Node {
            if (currentDepth == maxDepth || strings.size < minContainerSize) return Node(null, strings)

            val stringsByCharacter = getStringsByCharacter(strings, currentDepth)
            var children: HashMap<Char, Node>? = HashMap(stringsByCharacter.size)

            for ((key, value) in stringsByCharacter) {
                val c = key ?: continue
                val child = buildTree(value, currentDepth + 1, maxDepth, minContainerSize)
                children?.set(c, child)
            }
            val remainingStrings: Collection<String> = stringsByCharacter[null].orEmpty()
            if (children != null) {
                if (children.isEmpty()) children = null
            }
            return Node(children, remainingStrings)
        }

        /** returns the given strings grouped by their nth character. Strings whose length is shorter
         * or equal to nth go into the "null" group.  */
        private fun getStringsByCharacter(
            strings: Collection<String>,
            nth: Int
        ): Map<Char?, Collection<String>> {
            val result = HashMap<Char?, MutableCollection<String>>()
            for (string in strings) {
                val c = if (string.length > nth) string[nth] else null
                if (!result.containsKey(c)) result[c] = ArrayList()
                result[c]?.add(string)
            }
            return result
        }
    }
}