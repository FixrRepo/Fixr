package com.inspaya.fixr.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.Embedding;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    /** Reads the embedding fields from a Redis hash. */
    public Optional<Embeddings> getEmbeddings(String key) {
        Map<Object, Object> values = redisTemplate.opsForHash().entries(key);
        if (values.isEmpty()) {
            return Optional.empty();
        }

        Object embedding = values.get("embedding");
        if (embedding == null) {
            throw new IllegalStateException("Redis hash '" + key + "' does not contain required field 'embedding'");
        }

        Object textEmbedding = values.get("textEmbedding");
        Object frameEmbeddings = values.get("frameEmbeddings");
        return Optional.of(new Embeddings(
                parseVector(embedding, "embedding", 0),
                textEmbedding == null ? Optional.empty() : Optional.of(parseVector(textEmbedding, "textEmbedding", 0)),
                frameEmbeddings == null ? Optional.empty() : Optional.of(parseVectors(frameEmbeddings, "frameEmbeddings"))));
    }

    private Embedding parseVector(Object value, String field, int index) {
        if (!(value instanceof List<?> items)) {
            throw new IllegalStateException("Redis field '" + field + "' must be an array of numbers");
        }

        float[] vector = new float[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            if (!(item instanceof Number number)) {
                throw new IllegalStateException("Redis field '" + field + "' contains a non-numeric value");
            }
            vector[i] = number.floatValue();
        }
        return new Embedding(vector, index);
    }

    private List<Embedding> parseVectors(Object value, String field) {
        if (!(value instanceof List<?> items)) {
            throw new IllegalStateException("Redis field '" + field + "' must be an array of vectors");
        }

        List<Embedding> vectors = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            vectors.add(parseVector(items.get(i), field, i));
        }
        return List.copyOf(vectors);
    }

    public record Embeddings(Embedding embedding, Optional<Embedding> textEmbedding,
                             Optional<List<Embedding>> frameEmbeddings) {
    }
}
