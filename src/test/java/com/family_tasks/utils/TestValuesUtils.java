package com.family_tasks.utils;

import java.security.SecureRandom;

public class TestValuesUtils {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public static String randomNumeric(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }

        return sb.toString();
    }

    public static int randomInt() {
        return randomInt(0, 10000);
    }

    public static int randomInt(int from, int to) {
        return RANDOM.nextInt(from, to);
    }
}