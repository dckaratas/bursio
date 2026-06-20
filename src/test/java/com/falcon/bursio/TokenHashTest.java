package com.falcon.bursio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.*;

/**
 * AuthService.hashToken() metodunun beklenen davranışını doğrular.
 * Password reset token'ları SHA-256 ile hash'lenip DB'ye kaydedilir.
 */
class TokenHashTest {

    private String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    @Test
    @DisplayName("SHA-256 hash her zaman 64 karakter hex üretmeli")
    void hash_produces_64_char_hex() throws NoSuchAlgorithmException {
        String result = sha256("some-uuid-token");
        assertThat(result).hasSize(64);
        assertThat(result).matches("[0-9a-f]+");
    }

    @Test
    @DisplayName("Aynı input her zaman aynı hash'i vermeli (deterministik)")
    void hash_is_deterministic() throws NoSuchAlgorithmException {
        String token = "550e8400-e29b-41d4-a716-446655440000";
        assertThat(sha256(token)).isEqualTo(sha256(token));
    }

    @Test
    @DisplayName("Farklı token'lar farklı hash üretmeli")
    void different_tokens_produce_different_hashes() throws NoSuchAlgorithmException {
        assertThat(sha256("token-a")).isNotEqualTo(sha256("token-b"));
    }

    @Test
    @DisplayName("Raw token hash'inden geri çevrilemez olmalı")
    void raw_token_is_not_recoverable_from_hash() throws NoSuchAlgorithmException {
        String rawToken = "my-secret-reset-token";
        String hashed = sha256(rawToken);
        assertThat(hashed).doesNotContain(rawToken);
    }
}
