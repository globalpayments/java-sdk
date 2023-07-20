package com.example.jsonreader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

public class JsonReaderHelper {

    private static final Gson GSON = new Gson();

    public JsonObject getRequestBodyAsJson(HttpServletRequest req) throws IOException {

        BufferedReader bodyReader = req.getReader();
        return GSON.fromJson(bodyReader, JsonElement.class).getAsJsonObject();
    }

    public JsonObject getJsonObject(BufferedReader bodyReader) {

        return GSON.fromJson(bodyReader, JsonElement.class).getAsJsonObject();
    }

}
