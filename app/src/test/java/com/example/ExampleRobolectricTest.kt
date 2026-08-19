package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.presentation.navigation.Screen
import com.example.presentation.navigation.patientBottomTabs
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MediBridge+", appName)
    }

    @Test
    fun `verify bottom navigation has exactly 7 tabs in exact order`() {
        assertEquals(7, patientBottomTabs.size)

        val expectedTitles = listOf(
            "Home",
            "Appt",
            "Medi",
            "AI",
            "Documents",
            "Health",
            "Settings"
        )

        val actualTitles = patientBottomTabs.map { it.title }
        assertEquals(expectedTitles, actualTitles)

        assertEquals("home", patientBottomTabs[0].route)
        assertEquals("appointments", patientBottomTabs[1].route)
        assertEquals("medicines", patientBottomTabs[2].route)
        assertEquals("ai_assistant", patientBottomTabs[3].route)
        assertEquals("documents", patientBottomTabs[4].route)
        assertEquals("health", patientBottomTabs[5].route)
        assertEquals("api_config", patientBottomTabs[6].route)
    }
}
