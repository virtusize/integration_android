package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.fixtures.TestFixtures
import org.junit.Test

class UserBodyProfileJsonParserTest {

    @Test
    fun test_parseValidUserBodyResponse_returnExpectedUserBodyProfile() {
        val actualUserBodyProfile = UserBodyProfileJsonParser().parse(TestFixtures.USER_BODY_JSONObject)
        assertThat(actualUserBodyProfile).isEqualTo(TestFixtures.userBodyProfile)
    }

    @Test
    fun test_parseNullUserProfileResponse() {
        val actualUserBodyProfile = UserBodyProfileJsonParser().parse(TestFixtures.NULL_USER_BODY_PROFILE)

        assertThat(actualUserBodyProfile).isNull()
    }

    @Test
    fun test_parseNotFoundResponse() {
        val actualUserBodyProfile = UserBodyProfileJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(actualUserBodyProfile).isNull()
    }
}
