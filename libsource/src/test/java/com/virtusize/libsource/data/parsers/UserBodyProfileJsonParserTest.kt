package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.remote.Measurement
import com.virtusize.libsource.data.remote.UserBodyProfile
import com.virtusize.libsource.fixtures.TestFixtures
import org.junit.Test

class UserBodyProfileJsonParserTest {

    @Test
    fun test_parseValidUserBodyResponse_returnExpectedUserBodyProfile() {
        val actualUserBodyProfile = UserBodyProfileJsonParser().parse(TestFixtures.USER_BODY_JSONObject)

        val expectedUserBodyProfile = UserBodyProfile(
            "female",
            32,
            1630,
            "50.00",
            mutableSetOf(
                Measurement("hip", 830),
                Measurement("hip", 830),
                Measurement("bust", 755),
                Measurement("neck", 300),
                Measurement("rise", 215),
                Measurement("bicep", 220),
                Measurement("thigh", 480),
                Measurement("waist", 630),
                Measurement("inseam", 700),
                Measurement("sleeve", 720),
                Measurement("shoulder", 370),
                Measurement("hipWidth", 300),
                Measurement("bustWidth", 245),
                Measurement("hipHeight", 750),
                Measurement("headHeight", 215),
                Measurement("kneeHeight", 395),
                Measurement("waistWidth", 225),
                Measurement("waistHeight", 920),
                Measurement("armpitHeight", 1130),
                Measurement("sleeveLength", 520),
                Measurement("shoulderWidth", 340),
                Measurement("shoulderHeight", 1240)
            )
        )

        assertThat(actualUserBodyProfile).isEqualTo(expectedUserBodyProfile)
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