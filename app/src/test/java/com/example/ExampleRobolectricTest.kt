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
    assertEquals("Shayan Dhara Budget", appName)
  }

  @Test
  fun `format currency formats indian rupee correctly`() {
    val formatted = com.example.ui.viewmodel.formatCurrency(65000.0, "₹")
    assertEquals("₹65,000", formatted)
  }
}
