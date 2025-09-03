package vp

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class VerifyVpTest {
    @Test
    fun `vp verification currently fails (reproducer)`() = runBlocking {
        // This documents the current behavior: overallSuccess() is false
        assertFalse(verifyVP(), "Expected VP verification to currently be false (reproducer).")
    }

    @Disabled("Enable once the VP verification is fixed.")
    @Test
    fun `vp verification should succeed eventually`() = runBlocking {
        assertTrue(verifyVP(), "Expected VP verification to succeed after the fix.")
    }
}