package com.example.ui.viewmodel

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object BackupEncryptionHelper {
    // A strong 256-bit key for AES-256
    private val KEY_BYTES = byteArrayOf(
        0x53.toByte(), 0x61.toByte(), 0x6e.toByte(), 0x63.toByte(),
        0x68.toByte(), 0x61.toByte(), 0x79.toByte(), 0x53.toByte(),
        0x65.toByte(), 0x63.toByte(), 0x75.toByte(), 0x72.toByte(),
        0x65.toByte(), 0x42.toByte(), 0x61.toByte(), 0x63.toByte(),
        0x6b.toByte(), 0x75.toByte(), 0x70.toByte(), 0x4b.toByte(),
        0x65.toByte(), 0x79.toByte(), 0x32.toByte(), 0x30.toByte(),
        0x32.toByte(), 0x36.toByte(), 0x21.toByte(), 0x40.toByte(),
        0x23.toByte(), 0x24.toByte(), 0x25.toByte(), 0x5e.toByte()
    ) // "SanchaySecureBackupKey2026!@#$%" (exactly 32 bytes)

    fun encrypt(plainText: String): String {
        return try {
            val keySpec = SecretKeySpec(KEY_BYTES, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            
            // Generate a random 16-byte IV
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // Prepend IV to encrypted bytes to store together
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            
            // Return as Base64 encoded string
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            plainText // Fallback to plaintext if encryption fails
        }
    }

    fun decrypt(encryptedText: String): String {
        val trimmed = encryptedText.trim()
        // If it starts with '{', it's already plain JSON (unencrypted / backward compatible)
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed
        }
        
        return try {
            val combined = Base64.decode(trimmed, Base64.NO_WRAP)
            if (combined.size < 16) {
                return trimmed // invalid encrypted content, return as is
            }
            
            // Extract IV
            val iv = ByteArray(16)
            System.arraycopy(combined, 0, iv, 0, 16)
            
            // Extract encrypted bytes
            val encryptedBytes = ByteArray(combined.size - 16)
            System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.size)
            
            val keySpec = SecretKeySpec(KEY_BYTES, "AES")
            val ivSpec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            trimmed // Return as is on failure so standard parsing handles it
        }
    }
}
