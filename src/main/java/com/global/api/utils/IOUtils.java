package com.global.api.utils;

import java.io.*;
import java.nio.charset.Charset;

public class IOUtils {
    public static String readFully(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        Reader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
        int c;
        while((c = reader.read()) != -1)
            sb.append((char)c);
        return sb.toString();
    }
}
