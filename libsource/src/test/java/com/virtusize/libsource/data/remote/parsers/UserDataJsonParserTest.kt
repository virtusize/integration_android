package com.virtusize.libsource.data.remote.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.remote.UserData
import com.virtusize.libsource.TestFixtures
import org.junit.Test


class UserDataJsonParserTest {

    @Test
    fun parse_hasFullInfo_shouldReturnExpectedObject() {
        val actualUserData = UserDataJsonParser().parse(TestFixtures.USER_DATA_WITH_FULL_INFO)

        val expectedUserData = UserData(
            false,
            true,
            true,
            true,
            false
        )

        assertThat(actualUserData).isEqualTo(expectedUserData)
    }

    @Test
    fun parse_hasOneInfo_shouldReturnExpectedObject() {
        val actualUserData = UserDataJsonParser().parse(TestFixtures.USER_DATA_ONLY_WITH_ONE_INFO)

        val expectedUserData = UserData(
            false,
            false,
            false,
            false,
            false
        )

        assertThat(actualUserData).isEqualTo(expectedUserData)
    }
}