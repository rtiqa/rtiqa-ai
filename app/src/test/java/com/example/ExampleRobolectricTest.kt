package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rtiqa.mobile.R
import com.rtiqa.mobile.domain.model.UserProfile
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
    assertNotNull(appName)
  }

  @Test
  fun `verify user profile stats in student dashboard`() {
    val userProfile = UserProfile(
      id = "u1",
      name = "أحمد علي",
      email = "ahmed@example.com",
      avatarResName = "img_ai_tutor_avatar_1785095337393",
      level = 3,
      xp = 1450,
      coins = 280,
      streakDays = 5
    )

    assertEquals("أحمد علي", userProfile.name)
    assertEquals(3, userProfile.level)
    assertEquals(1450, userProfile.xp)
    assertEquals(280, userProfile.coins)
    assertEquals(5, userProfile.streakDays)
  }
}

