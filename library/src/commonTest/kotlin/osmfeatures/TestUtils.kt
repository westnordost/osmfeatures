package osmfeatures

import org.junit.Assert.assertTrue

object TestUtils {
    fun <T> assertEqualsIgnoreOrder(a: Collection<T>, b: Collection<T>) {
        assertTrue(a.size == b.size && a.containsAll(b))
    }

    @SafeVarargs
    fun <T> listOf(vararg items: T): List<T> {
        return items.asList()
    }
}
