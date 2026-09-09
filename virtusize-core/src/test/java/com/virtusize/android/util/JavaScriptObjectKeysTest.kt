package com.virtusize.android.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class JavaScriptObjectKeysTest {
    @Test
    fun sortedLikeJavaScriptObjectKeys_integerKeysFirstAscendingThenInsertionOrder() {
        assertThat(listOf("M", "160", "S", "110", "L", "90").sortedLikeJavaScriptObjectKeys())
            .isEqualTo(listOf("90", "110", "160", "M", "S", "L"))
        assertThat(listOf("140", "130", "110", "160", "120", "150").sortedLikeJavaScriptObjectKeys())
            .isEqualTo(listOf("110", "120", "130", "140", "150", "160"))
        // Non-canonical numbers are not index keys in JavaScript and keep their position
        assertThat(listOf("090", "10", "-1", "1.5").sortedLikeJavaScriptObjectKeys())
            .isEqualTo(listOf("10", "090", "-1", "1.5"))
        assertThat(emptyList<String>().sortedLikeJavaScriptObjectKeys()).isEmpty()
    }
}
