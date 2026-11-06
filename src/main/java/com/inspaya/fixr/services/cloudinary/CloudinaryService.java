package com.inspaya.fixr.services.cloudinary;

import com.cloudinary.Cloudinary;
import com.inspaya.fixr.configurations.properties.CloudinaryProperties;
import com.inspaya.fixr.dtos.MediaUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    /**
     * Generates a signature for frontend client for upload of files
     */
    public MediaUploadResponse generateSignedUploadKey(String subFolder){
        final long timestamp = Instant.now().getEpochSecond();
        final long expiresAt = Instant.now().plusSeconds(20 * 60).getEpochSecond();
        final String folder = String.format("%s/%s", properties.folderName(), subFolder);

        final Map<String, Object> params = Map.of(
                "timestamp", timestamp,
                "folder", folder
        );

        final String signature = cloudinary.apiSignRequest(params, properties.apiSecret());

        return new MediaUploadResponse(
                signature, timestamp,
                expiresAt, properties.apiKey(),
                properties.cloudName(), folder
        );

    }
}
