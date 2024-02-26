package de.westnordost.osmfeatures

import kotlin.test.assertEquals

fun <T> assertEqualsIgnoreOrder(a: Collection<T>, b: Collection<T>) {
    assertEquals(a.toSet(), b.toSet())
}