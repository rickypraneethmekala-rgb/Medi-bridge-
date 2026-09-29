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
    fun `verify bottom navigation has exactly 5 tabs in exact order`() {
        assertEquals(5, patientBottomTabs.size)

        val expectedRoutes = listOf(
            "home",
            "health_summary",
            "emergency_info",
            "recent_activity",
            "api_config"
        )

        val actualRoutes = patientBottomTabs.map { it.route }
        assertEquals(expectedRoutes, actualRoutes)
    }

    @Test
    fun `verify strict gmail validation accepts only gmail addresses`() {
        val gmailRegex = Regex("^[a-zA-Z0-9._%+-]+@gmail\\.com$", RegexOption.IGNORE_CASE)
        assertEquals(true, gmailRegex.matches("patient@gmail.com"))
        assertEquals(true, gmailRegex.matches("USER.TEST+1@GMAIL.COM"))
        assertEquals(false, gmailRegex.matches("user@yahoo.com"))
        assertEquals(false, gmailRegex.matches("user@medibridge.org"))
        assertEquals(false, gmailRegex.matches("user@gmail.com.co"))
        assertEquals(false, gmailRegex.matches("@gmail.com"))
    }
}
