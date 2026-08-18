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
    assertEquals("MedRescue", appName)
  }

  @Test
  fun `verify crypto encryption and decryption roundtrip`() {
    val plainText = "Patient Blood O+ with Severe Peanut Anaphylaxis"
    val encrypted = com.example.data.security.CryptoManager.encrypt(plainText)
    org.junit.Assert.assertNotEquals(plainText, encrypted.cipherTextBase64)
    org.junit.Assert.assertTrue(encrypted.ivBase64.isNotEmpty())

    val decrypted = com.example.data.security.CryptoManager.decrypt(encrypted.cipherTextBase64, encrypted.ivBase64)
    assertEquals(plainText, decrypted)
  }

  @Test
  fun `verify responder payload format and signature hash`() {
    val payload = com.example.data.security.CryptoManager.createEmergencyResponderPayload(
      id = "MED-AXM-0148",
      name = "Alex Morgan",
      bloodType = "O+",
      allergies = listOf("Asthma"),
      medications = listOf("Albuterol"),
      contactPhone = "+1 415 555 0148",
      contactName = "Jordan Morgan"
    )
    org.junit.Assert.assertTrue(payload.startsWith("MED-SOS-V1|"))
    org.junit.Assert.assertTrue(payload.contains("BLOOD:O+"))
    org.junit.Assert.assertTrue(payload.contains("SIG:SHA256:"))
  }
}
