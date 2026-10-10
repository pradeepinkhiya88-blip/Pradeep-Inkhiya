package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.YouTubeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AutoClip AI", appName)
    }

    @Test
    fun `test youtube url parsing`() {
        val standardUrl = "https://www.youtube.com/watch?v=UF8uR6Z6KLc"
        val shortUrl = "https://youtu.be/UF8uR6Z6KLc"
        val shortsUrl = "https://www.youtube.com/shorts/UF8uR6Z6KLc"

        assertEquals("UF8uR6Z6KLc", YouTubeHelper.extractVideoId(standardUrl))
        assertEquals("UF8uR6Z6KLc", YouTubeHelper.extractVideoId(shortUrl))
        assertEquals("UF8uR6Z6KLc", YouTubeHelper.extractVideoId(shortsUrl))
    }
}
