package com.mobileprism.fishing.ui.home.detail

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DeleteDialogStringTest {

    private val stringsXml: String by lazy {
        val candidates = listOf(
            "src/commonMain/composeResources/values/strings.xml",
            "shared/src/commonMain/composeResources/values/strings.xml"
        )
        val file = candidates
            .map { java.io.File(it) }
            .firstOrNull { it.exists() }
            ?: error("strings.xml not found from CWD ${java.io.File(".").absolutePath}")
        file.readText()
    }

    private fun valueOf(name: String): String {
        val regex = Regex("<string name=\"$name\"[^>]*>(.*?)</string>", RegexOption.DOT_MATCHES_ALL)
        return regex.find(stringsXml)?.groupValues?.get(1)
            ?: error("string '$name' not found")
    }

    @Test
    fun deleteCatchDialog_usesSingleCanonicalPlaceholder() {
        val value = valueOf("delete_catch_dialog")
        assertTrue(value.contains("%1\$s"), "delete_catch_dialog must contain %1\$s, was: $value")
        assertFalse(Regex("(?<!%1)%s").containsMatchIn(value), "delete_catch_dialog must not contain bare %s")
    }

    @Test
    fun deletePlaceDialog_usesSingleCanonicalPlaceholder() {
        val value = valueOf("delete_place_dialog")
        assertTrue(value.contains("%1\$s"), "delete_place_dialog must contain %1\$s, was: $value")
        assertFalse(Regex("(?<!%1)%s").containsMatchIn(value), "delete_place_dialog must not contain bare %s")
    }
}
