package com.manufactureos.util;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates short, human-readable, collision-resistant business identifiers such as
 * "BOM-20260401-7K2Q" for records that need a document number distinct from the
 * numeric primary key. Not a strict sequence - uniqueness comes from the millisecond
 * timestamp plus a random suffix, which is enough entropy for this system's volumes
 * and avoids a shared counter that would itself need locking.
 */
public final class CodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private CodeGenerator() {}

    public static String next(String prefix) {
        long millis = Instant.now().toEpochMilli();
        StringBuilder suffix = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 4; i++) {
            suffix.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return prefix + "-" + Long.toString(millis, 36).toUpperCase() + "-" + suffix;
    }
}
