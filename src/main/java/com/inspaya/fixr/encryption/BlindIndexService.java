package com.inspaya.fixr.encryption;

import com.nimbusds.jose.crypto.impl.HMAC;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public class BlindIndexService {
    private static final byte[] INDEX_KEY = new byte[32];

    static {
        new SecureRandom().nextBytes(INDEX_KEY);
    }
    public static String generateBlindIndex(String plainText) throws NoSuchAlgorithmException {

        try{
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(INDEX_KEY, "HmacSHA256");
            hmac.init(secretKeySpec);

            byte[] rawHash = hmac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for(byte b : rawHash){
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String plainText = "Sylvanus Jedidiah";

        System.out.println(generateBlindIndex(plainText));
    }
}
