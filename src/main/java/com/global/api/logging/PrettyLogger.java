package com.global.api.logging;

import com.global.api.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.Timestamp;

public abstract class PrettyLogger implements IRequestLogger {
    private static final JsonParser parser = new JsonParser();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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

}