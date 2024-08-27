package com.global.api.logging;

import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.Timestamp;
import java.util.Map;

public abstract class PrettyLogger implements IRequestLogger {
    private static final JsonParser parser = new JsonParser();
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public final String initialLine =  "================================================================================";
    public final String middleLine =   "--------------------------------------------------------------------------------";
    public final String endLine = initialLine;

    public String AppendText(String format, String[] args) {
        if (!StringUtils.isNullOrEmpty(format)) {
            return String.format(format, args);
        }
        return null;
    }

    public static String toPrettyJson(String unPrettyJson) {
        try {
            String unPrettyTrimmedJson = unPrettyJson.trim();

            if (unPrettyTrimmedJson.startsWith("{") && unPrettyTrimmedJson.endsWith("}")) {
                JsonObject json = parser.parse(unPrettyTrimmedJson).getAsJsonObject();
                return gson.toJson(json);
            } else {
                return unPrettyJson;
            }
        } catch (Exception ex) {
            return unPrettyJson;
        }
    }

    public String getTimestamp() {
        return String.valueOf(new Timestamp(System.currentTimeMillis()));
    }

    public static String generateRequestLog(JsonDoc request, Map<String, String> headers) {
        StringBuilder sb = new StringBuilder("Request: ");
        appendFieldAndSpaceIfExists(request, "verb", sb);
        appendFieldAndLineBreakIfExists(request, "url", sb);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                appendHeader(header, sb);
            }
        }

        appendFieldAndLineBreakIfExists(request, "content_length", sb.append("Content-Length: "));
        appendFieldAndLineBreakIfExists(request, "content", sb);

        return sb.toString();
    }

    public static String generateRequestLog(JsonDoc request) {
        StringBuilder sb = new StringBuilder("Request: ");
        appendFieldAndSpaceIfExists(request, "verb", sb);
        appendFieldAndLineBreakIfExists(request, "url", sb);
        appendFieldAndLineBreakIfExists(request, "content_length", sb.append("Content-Length: "));
        appendFieldAndLineBreakIfExists(request, "content", sb);

        return sb.toString();
    }

    public static String generateResponseLog(String response) {
        return "Response: " + toPrettyJson(response);
    }

    private static void appendFieldIfExists(JsonDoc jsonDoc, String fieldName, StringBuilder result) {
        if (jsonDoc.has(fieldName)) {
            String fieldValue = toPrettyJson(jsonDoc.getString(fieldName));
            result.append(fieldValue);
        }
    }

    private static void appendFieldAndLineBreakIfExists(JsonDoc jsonDoc, String fieldName, StringBuilder result) {
        if (jsonDoc.has(fieldName)) {
            String fieldValue = toPrettyJson(jsonDoc.getString(fieldName));
            result.append(fieldValue).append("\n");
        }
    }

    private static void appendFieldAndSpaceIfExists(JsonDoc jsonDoc, String fieldName, StringBuilder result) {
        if (jsonDoc.has(fieldName)) {
            String fieldValue = toPrettyJson(jsonDoc.getString(fieldName));
            result.append(fieldValue).append(" ");
        }
    }

    private static void appendHeader(Map.Entry<String, String> header, StringBuilder result) {
        result.append(header.getKey()).append(": ").append(String.join(", ", header.getValue())).append("\n");
    }
}