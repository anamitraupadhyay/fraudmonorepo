package org.frauddetection.util;

import org.frauddetection.model.FormData;

/*
import jakarta.json.Json;
import jakarta.json.JsonObject;
*/
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

/**
 * Utility class for JSON operations
 */
public class JsonUtils {

    /**
     * Converts a FormData object to JSON string
     * 
     * @param formData The form data object
     * @return JSON string representation
     */
    public static String toJson(FormData formData) {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.toJson(formData);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }
}