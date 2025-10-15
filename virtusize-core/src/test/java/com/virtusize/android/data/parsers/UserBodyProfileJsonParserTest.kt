package com.virtusize.android.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.android.fixtures.TestFixtures
import org.junit.Test

class UserBodyProfileJsonParserTest {
    @Test
    fun test_parseValidUserBodyResponse_returnExpectedUserBodyProfile() {
        val actualUserBodyProfile =
            UserBodyProfileJsonParser().parse(TestFixtures.USER_BODY_JSONObject)
        assertThat(actualUserBodyProfile).isEqualTo(TestFixtures.userBodyProfile)
    }

    @Test
    fun test_parseNullUserProfileResponse() {
        val actualUserBodyProfile =
            UserBodyProfileJsonParser().parse(TestFixtures.NULL_USER_BODY_PROFILE)

        assertThat(actualUserBodyProfile).isNull()
    }

    @Test
    fun test_parseNotFoundResponse() {
        val actualUserBodyProfile = UserBodyProfileJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(actualUserBodyProfile).isNull()
    }

    @Test
    fun test_parseValidUserBodyResponseWithBraSize_returnExpectedUserBodyProfile() {
        val actualUserBodyProfile =
            UserBodyProfileJsonParser().parse(TestFixtures.USER_BODY_WITH_BRA_SIZE_JSONObject)
        assertThat(actualUserBodyProfile).isEqualTo(TestFixtures.userBodyProfileWithBraSize)
    }

    @Test
    fun test_parseUserBodyWithBraSize_braSizeIsCorrectlyParsed() {
        val actualUserBodyProfile =
            UserBodyProfileJsonParser().parse(TestFixtures.USER_BODY_WITH_BRA_SIZE_JSONObject)

        assertThat(actualUserBodyProfile).isNotNull()
        assertThat(actualUserBodyProfile?.braSize).isNotNull()
        assertThat(actualUserBodyProfile?.braSize).containsEntry("country", "US")
        assertThat(actualUserBodyProfile?.braSize).containsEntry("cup", "B")
        assertThat(actualUserBodyProfile?.braSize).containsEntry("band", 34)
    }

    @Test
    fun test_parseUserBodyWithoutBraSize_braSizeIsNull() {
        val actualUserBodyProfile =
            UserBodyProfileJsonParser().parse(TestFixtures.USER_BODY_JSONObject)

        assertThat(actualUserBodyProfile).isNotNull()
        assertThat(actualUserBodyProfile?.braSize).isNull()
    }
}
