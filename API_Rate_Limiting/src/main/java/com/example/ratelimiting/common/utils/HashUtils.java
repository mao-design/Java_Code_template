package com.example.ratelimiting.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// 把 userId、username、IP 等身份值转换成固定长度 Hash。

public final class HashUtils {
    private HashUtils() {

    }

    public static String sha256(String value) {
        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance("SHA-256");

            byte[] digest = messageDigest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat
                    .of()
                    .formatHex(digest)
                    .substring(0, 32);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 算法不可用",
                    e
            );
        }
    }
}
