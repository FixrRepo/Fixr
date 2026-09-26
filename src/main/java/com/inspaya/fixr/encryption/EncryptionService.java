package com.inspaya.fixr.encryption;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.UUID;

@Slf4j
public class EncryptionService {

    static class KEKService{
        private static final String plainKEK = UUID.randomUUID().toString();

        static SecretKey getKEK() {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] byteKEK = messageDigest.digest(plainKEK.getBytes(StandardCharsets.UTF_8));
                return new SecretKeySpec(byteKEK, "AES");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

        }
    }
    private static final SecretKey KEK = KEKService.getKEK();

    private static final int IV_LENGTH_BYTES = 32;
    private static final int KEY_SIZE_BYTES = 48; // Key[32 bytes] + Tag[12 bytes]
    private static final int TAG_LENGHT_BITS = 128;


    public static void main(String[] args) throws Exception{
        String plainText = "Jedidiah Sylvanus";
        byte[] cipherText = encryptData(plainText);
        String stringCipherText = Base64.getEncoder().encodeToString(encryptData(plainText));
        String decryptedText = decryptData(cipherText);
        System.out.printf("PlainText: %s\nCipherText:%s\nDecryptedText: %s", plainText, stringCipherText, decryptedText);
    }

    /**
     * */
    public static byte[] encryptData(String plainText){
        // Initialization vector for DEK
        try{
            byte[] DEK_IV = generateIV();
            SecretKey dek = generateDEK();

            // Initialization vector for KEK
            byte[] KEK_IV = generateIV();
            // Encrypting DEK with KEK
            byte[] encryptedDEK = encryptDEK(dek.getEncoded(), KEK_IV);

            // Encrypting Plaintext with DEK
            byte[] plainTextByte = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] cipherText = encrypt(plainTextByte, dek, DEK_IV);

            return ByteBuffer.allocate(IV_LENGTH_BYTES + IV_LENGTH_BYTES + encryptedDEK.length + cipherText.length)
                    .put(DEK_IV)
                    .put(KEK_IV)
                    .put(encryptedDEK)
                    .put(cipherText)
                    .array();
        } catch (RuntimeException | NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException e) {
            log.error("Failed to encrypt data because {}", e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    public static String decryptData(byte[] cipherBytes) {
        try{
            ByteBuffer buffer = ByteBuffer.wrap(cipherBytes);

            byte[] DEK_IV = new byte[IV_LENGTH_BYTES];
            byte[] KEK_IV = new byte[IV_LENGTH_BYTES];
            byte[] encryptedDEK = new byte[KEY_SIZE_BYTES];

            buffer.get(DEK_IV);
            buffer.get(KEK_IV);
            buffer.get(encryptedDEK);

            byte[] encryptedData = new byte[buffer.remaining()];
            buffer.get(encryptedData);

            SecretKey decryptedDEK = new SecretKeySpec(decrypt(encryptedDEK, KEK, KEK_IV), "AES");

            return new String(decrypt(encryptedData, decryptedDEK, DEK_IV), StandardCharsets.UTF_8);
        } catch (RuntimeException | NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            log.error("Failed to decrypt column because {}", e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    private static byte[] encrypt(byte[] input, SecretKey key, byte[] nonce) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGHT_BITS, nonce);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

        return cipher.doFinal(input);
    }

    private static byte[] decrypt(byte[] input, SecretKey key, byte[] nonce) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGHT_BITS, nonce);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

        return cipher.doFinal(input);
    }

    private static byte[] generateIV(){
        byte[] nonce = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    private static SecretKey generateDEK() throws NoSuchAlgorithmException {
        String rawDek = UUID.randomUUID().toString();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] byteDek = messageDigest.digest(rawDek.getBytes(StandardCharsets.UTF_8));

        return new SecretKeySpec(byteDek, "AES");
    }

    private static byte[] encryptDEK(byte[] rawDek, byte[] IV) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        return encrypt(rawDek, KEK, IV);
    }
}
