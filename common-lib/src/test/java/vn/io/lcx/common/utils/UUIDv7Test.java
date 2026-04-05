package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UUIDv7Test {

    @Test
    void randomUUID_returnsValidUUID() {
        UUID uuid = UUIDv7.randomUUID();
        assertNotNull(uuid);
        // UUID string format: 8-4-4-4-12 hex characters
        String uuidString = uuid.toString();
        assertTrue(uuidString.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
                "UUID should match standard UUID format: " + uuidString);
    }

    @Test
    void randomUUID_uniqueness() {
        Set<UUID> uuids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            uuids.add(UUIDv7.randomUUID());
        }
        assertEquals(1000, uuids.size(), "All 1000 generated UUIDs should be unique");
    }

    @Test
    void randomUUID_version7() {
        UUID uuid = UUIDv7.randomUUID();
        // Version is encoded in bits 48-51 of the most significant long
        // For version 7, the version nibble should be 0x7
        int version = uuid.version();
        assertEquals(7, version, "UUID version should be 7");

        // Variant should be 2 (RFC 4122)
        int variant = uuid.variant();
        assertEquals(2, variant, "UUID variant should be 2 (RFC 4122)");
    }

    @Test
    void randomBytes_correctLength() {
        byte[] bytes = UUIDv7.randomBytes();
        assertNotNull(bytes);
        assertEquals(16, bytes.length, "UUIDv7 random bytes should be 16 bytes long");

        // Verify version bits (byte 6, upper nibble should be 0x7)
        int versionNibble = (bytes[6] & 0xF0) >> 4;
        assertEquals(0x7, versionNibble, "Version nibble should be 7");

        // Verify variant bits (byte 8, upper 2 bits should be 10)
        int variantBits = (bytes[8] & 0xC0) >> 6;
        assertEquals(0x2, variantBits, "Variant bits should be 10 (binary)");
    }
}
