package com.virtusize.libsource.data.remote.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.remote.Store
import com.virtusize.libsource.TestFixtures
import org.junit.Test


class StoreJsonParserTest {

    @Test
    fun parse_hasFullInfo_shouldReturnExpectedObject() {
        val actualStore = StoreJsonParser().parse(TestFixtures.STORE_WITH_FULL_INFO)

        val expectedStore = Store(
            2,
            "https://www.survey.com/s/xxxxxx",
            "Virtusize",
            "virtusize",
            2,
            "test_apiKey",
            "2011-01-01T00:00:00Z",
            "2020-04-20T02:33:58Z",
            "2018-05-29 04:32:45",
            false,
            "JP"
        )

        assertThat(actualStore).isEqualTo(expectedStore)
    }

    @Test
    fun parse_hasNullValues_shouldReturnExpectedObject() {
        val actualStore = StoreJsonParser().parse(TestFixtures.STORE_WITH_NULL_VALUES)

        val expectedStore = Store(
            2,
            "https://www.survey.com/s/xxxxxx",
            "Virtusize",
            "virtusize",
            2,
            "test_apiKey",
            "2011-01-01T00:00:00Z",
            "2020-04-20T02:33:58Z",
            "",
            false,
            ""
        )

        assertThat(actualStore).isEqualTo(expectedStore)
    }
}