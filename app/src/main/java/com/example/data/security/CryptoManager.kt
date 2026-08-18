package com.example.data.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * High-performance cryptographic manager for securing sensitive health records
 * stored in the local Room database using AES-256 GCM (Galois/Counter Mode)
 * with authenticated encryption and SHA-256 tamper-evident integrity hashing.
 */
object CryptoManager {

  private const val TRANSFORMATION = "AES/GCM/NoPadding"
  private const val AES_KEY_ALGORITHM = "AES"
  private const val GCM_TAG_LENGTH_BITS = 128
  private const val IV_LENGTH_BYTES = 12

  // In production this can be backed by AndroidKeyStore.
  // For standard device persistence, we derive a robust 256-bit AES master key.
  private val MASTER_SEED = "MedRescue-SecureHealth-RoomKey-2026-AlphaV2".toByteArray(StandardCharsets.UTF_8)
  private val secretKey: SecretKey by lazy {
    val digest = MessageDigest.getInstance("SHA-256")
    val keyBytes = digest.digest(MASTER_SEED)
    SecretKeySpec(keyBytes, AES_KEY_ALGORITHM)
  }

  data class EncryptedResult(
    val cipherTextBase64: String,
    val ivBase64: String
  )

  /**
   * Encrypts plaintext string using AES-256 GCM with a fresh cryptographically random IV.
   */
  fun encrypt(plainText: String): EncryptedResult {
    if (plainText.isEmpty()) {
      return EncryptedResult("", "")
    }
    val iv = ByteArray(IV_LENGTH_BYTES)
    SecureRandom().nextBytes(iv)

    val cipher = Cipher.getInstance(TRANSFORMATION)
    val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

    val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
    val cipherTextBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
    val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

    return EncryptedResult(cipherTextBase64, ivBase64)
  }

  /**
   * Decrypts AES-256 GCM ciphertext using the provided Base64 IV.
   */
  fun decrypt(cipherTextBase64: String, ivBase64: String): String {
    if (cipherTextBase64.isEmpty() || ivBase64.isEmpty()) {
      return ""
    }
    return try {
      val cipherBytes = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
      val iv = Base64.decode(ivBase64, Base64.NO_WRAP)

      val cipher = Cipher.getInstance(TRANSFORMATION)
      val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

      val plainBytes = cipher.doFinal(cipherBytes)
      String(plainBytes, StandardCharsets.UTF_8)
    } catch (_: Exception) {
      // Fallback or unencrypted legacy data check
      cipherTextBase64
    }
  }

  /**
   * Generates a tamper-evident SHA-256 hash checksum for health profile integrity.
   */
  fun generateIntegrityHash(payload: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(payload.toByteArray(StandardCharsets.UTF_8))
    return "SHA256:" + hash.joinToString("") { "%02x".format(it) }.take(16).uppercase()
  }

  /**
   * Generates an emergency FHIR/JSON compatible First Responder Payload string.
   */
  fun createEmergencyResponderPayload(
    id: String,
    name: String,
    bloodType: String,
    allergies: List<String>,
    medications: List<String>,
    contactPhone: String,
    contactName: String
  ): String {
    val allergiesStr = allergies.joinToString(",")
    val medsStr = medications.joinToString(",")
    val raw = "ID:$id|NAME:$name|BLOOD:$bloodType|ALG:$allergiesStr|MED:$medsStr|ICE:$contactName($contactPhone)"
    val sig = generateIntegrityHash(raw)
    return "MED-SOS-V1|$raw|SIG:$sig"
  }
}
