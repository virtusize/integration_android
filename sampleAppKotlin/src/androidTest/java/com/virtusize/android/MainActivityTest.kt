package com.virtusize.android

import android.app.Application
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.virtusize.android.util.ViewVisibilityIdlingResource
import com.virtusize.libsource.data.local.VirtusizeProduct
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    private var checkSizeButtonIdlingResource: ViewVisibilityIdlingResource? = null

    @get:Rule var activityScenarioRule = activityScenarioRule<MainActivity>()

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(checkSizeButtonIdlingResource)
    }

    @Test
    fun checkVirtusizeButtonText() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApplication
        app.setVirtusizeProduct(VirtusizeProduct("694", "12122"))

        activityScenarioRule.scenario.onActivity {
            checkSizeButtonIdlingResource = ViewVisibilityIdlingResource(
                it.findViewById(R.id.exampleVirtusizeButton),
                View.VISIBLE
            )
        }

        IdlingRegistry.getInstance().register(checkSizeButtonIdlingResource)

        onView(withId(R.id.exampleVirtusizeButton))
            .check(matches(withText(R.string.virtusize_button_text)))
    }
}