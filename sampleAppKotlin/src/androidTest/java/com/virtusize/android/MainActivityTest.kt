package com.virtusize.android

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.*
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.virtusize.android.util.ViewVisibilityIdlingResource
import org.hamcrest.CoreMatchers.containsString
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

    @Before
    fun setUp() {
        activityScenarioRule.scenario.onActivity {
            checkSizeButtonIdlingResource = ViewVisibilityIdlingResource(
                it.findViewById(R.id.exampleVirtusizeButton),
                View.VISIBLE
            )
        }

        IdlingRegistry.getInstance().register(checkSizeButtonIdlingResource)

        onView(withId(R.id.exampleVirtusizeButton)).perform(click())

        onWebView().forceJavascriptEnabled()

        // Wait for launching the Virtusize integration
        Thread.sleep(5000)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(checkSizeButtonIdlingResource)
    }

    @Test
    fun welcomeScreen_isDisplayed() {
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "._root_ee791._reset_825c8._h1_ee791._inverse_ee791._larger_ee791._regular_ee791._whiteSpacePreLine_ee791._hello_10b7d"))
            .check(webMatches(getText(), containsString("Hello")))
    }

    @Test
    fun clickIAlreadyHaveAnAccount_loginScreenIsDisplayed() {
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "._root_ee791._reset_825c8._center_ee791._blockLevel_ee791._normal_ee791._regular_ee791._whiteSpacePreLine_ee791._text_934b2"))
            .perform(webClick())

        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "._root_ee791._reset_825c8._h1_ee791._inverse_ee791._larger_ee791._regular_ee791._whiteSpacePreLine_ee791"))
            .check(webMatches(getText(), containsString("Log in")))
    }
}