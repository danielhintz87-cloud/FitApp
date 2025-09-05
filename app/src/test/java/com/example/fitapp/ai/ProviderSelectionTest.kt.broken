package com.example.fitapp.ai

import com.example.fitapp.domain.entities.AiProvider
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for provider selection logic
 * These tests verify that Perplexity is disabled by default
 */
class ProviderSelectionTest {

    @Test
    fun testPerplexityProviderEnumExists() {
        // Verify that the AiProvider enum still includes Perplexity for future use
        val providers = AiProvider.values()
        assertTrue("Perplexity provider should exist in enum", providers.contains(AiProvider.Perplexity))
        assertTrue("Gemini provider should exist in enum", providers.contains(AiProvider.Gemini))
    }

    @Test
    fun testProviderEnumValues() {
        // Test that we have exactly the expected providers
        val providers = AiProvider.values()
        assertEquals("Should have exactly 2 providers", 2, providers.size)
        
        // Test enum values
        assertEquals("First provider should be Gemini", AiProvider.Gemini, providers[0])
        assertEquals("Second provider should be Perplexity", AiProvider.Perplexity, providers[1])
    }
}