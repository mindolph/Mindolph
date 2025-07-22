package com.mindolph.mindmap.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public final class CryptoUtils {

    private static final Logger log = LoggerFactory.getLogger(CryptoUtils.class);

    public static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static String encrypt(String pass, String text) {
        if (pass == null || pass.isEmpty()) {
            return text;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
            byte[] textHash = sha256(textBytes);

            outputStream.write(textHash);
            outputStream.write(textBytes);

            byte[] originalData = outputStream.toByteArray();

            byte[] key = sha256(pass.getBytes(StandardCharsets.UTF_8));
            Key aesKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encodedData = cipher.doFinal(originalData);
            if (Arrays.equals(originalData, encodedData)) {
                throw new IllegalStateException(
                        "Data can't be encrypted! Check encryption provider and settings!");
            }
            return new String(Base64.getEncoder().encode(encodedData));
        } catch (NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException |
                 BadPaddingException | IOException ex) {
            log.error("Failed to encrypt text", ex);
            return null;
        }
    }

    public static boolean decrypt(String pass, String text,
                                  StringBuilder output) {
        try {
            byte[] key = sha256(pass.getBytes(StandardCharsets.UTF_8));
            Key aesKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] decrypted = Base64.getDecoder().decode(text);
            if (decrypted.length < 32) {
                return false;
            }
            decrypted = cipher.doFinal(decrypted);
            byte[] sha256data = Arrays.copyOfRange(decrypted, 0, 32);
            byte[] textPart = Arrays.copyOfRange(decrypted, 32, decrypted.length);
            byte[] calculatedHash = sha256(textPart);
            if (!Arrays.equals(sha256data, calculatedHash)) {
                return false;
            }
            output.append(new String(textPart, StandardCharsets.UTF_8));
            return true;
        } catch (NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException |
                 BadPaddingException ex) {
            log.error("Failed to decrypt text", ex);
            return false;
        }
    }

    public static byte[] base64decode(String text) throws IOException {
        return Base64.getDecoder().decode(text);
    }

    public static String base64encode(byte[] data) {
        return new String(Base64.getEncoder().encode(data));
    }
}
