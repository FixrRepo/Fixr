package com.inspaya.fixr.converter;

import com.inspaya.fixr.encryption.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public class DataEncryptionConverter implements AttributeConverter<String, byte[]> {
    @Override
    public byte[] convertToDatabaseColumn(String s) {
        return EncryptionService.encryptData(s);
    }

    @Override
    public String convertToEntityAttribute(byte[] bytes) {
        return EncryptionService.decryptData(bytes);
    }
}
