package com.savage.anime.ui.screens.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun onboarding_shows_skip_button() {
        composeTestRule.setContent {
            OnboardingScreen(onComplete = {})
        }

        composeTestRule.onNodeWithText("Salta").assertIsDisplayed()
    }

    @Test
    fun onboarding_shows_first_page_title() {
        composeTestRule.setContent {
            OnboardingScreen(onComplete = {})
        }

        composeTestRule.onNodeWithText("Benvenuto su OpenAnime").assertIsDisplayed()
    }
}
