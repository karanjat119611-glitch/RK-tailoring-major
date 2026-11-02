package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
    assertEquals("RK Tailoring Major", appName)
  }

  @Test
  fun `verify rajputi poshak dress type exists and has correct labels`() {
    val dress = com.example.data.model.DressType.fromCode("rajputi_poshak")
    assertEquals(com.example.data.model.DressType.RAJPUTI_POSHAK, dress)
    assertEquals("राजपूती पोशाक", dress.labelHi)
    assertEquals("Rajputi Poshak", dress.labelEn)
  }
}
