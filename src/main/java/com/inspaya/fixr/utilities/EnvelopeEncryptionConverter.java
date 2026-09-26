package com.inspaya.fixr.utilities;

import com.inspaya.fixr.encryption.EncryptionService;
import jakarta.persistence.AttributeConverter;

public class EnvelopeEncryptionConverter implements AttributeConverter<String, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(String s) {
        return EncryptionService.encryptData(s);
    }

    @Override
    public String convertToEntityAttribute(byte[] bytes) {
        return EncryptionService.decryptData(bytes);
    }
}
