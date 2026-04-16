package com.global.api.tests.utils.citesting;

import com.global.api.terminals.IRequestIdProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class CiTestingHarness {
    private static final String RESOURCE_DIR = "src/test/resources/citesting";
    private static final String TIMESTAMP_KEY = "_fixedTimestamp";
    private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, String>>() {
    }.getType();
    private final String ciTestingProxyEndpoint;
    private final String targetServiceUrl;
    private final CacheMode cacheMode;
    private final String testName;
    private final Map<String, String> generatedIds;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public CiTestingHarness(String targetServiceUrl, CacheMode cacheMode, String testName) {
        String proxyHost = System.getenv("PROXY_ENDPOINT");
        if (proxyHost == null || proxyHost.isEmpty()) {
            throw new IllegalStateException(
                    "PROXY_ENDPOINT environment variable is not set. "
                            + "Set it to the proxy hostname (e.g. PROXY_ENDPOINT=\"your-proxy-host.example.com\")."
            );
        }
        this.ciTestingProxyEndpoint = "https://" + proxyHost + "/proxy";
        this.targetServiceUrl = targetServiceUrl;
        this.cacheMode = cacheMode;
        this.testName = testName;

        if (cacheMode == CacheMode.Locked) {
            this.generatedIds = loadIds();
        } else {
            this.generatedIds = loadIdsFromFileSystem();
            if (!generatedIds.containsKey(TIMESTAMP_KEY)) {
                generatedIds.put(TIMESTAMP_KEY, Long.toString(System.currentTimeMillis()));
                writeIds();
            }
        }

        DateTimeUtils.setCurrentMillisFixed(Long.parseLong(generatedIds.get(TIMESTAMP_KEY)));
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

    public String getTestingUrl() {
        String cacheReturns = cacheMode == CacheMode.Locked ? "true" : "false";
        String targetHost = targetServiceUrl
                .replaceFirst("^https?://", "https:/");

        return ciTestingProxyEndpoint + "/(cacheReturns:" + cacheReturns + ")/" + targetHost;
    }

    public DateTime getCurrentTime() {
        return DateTime.now();
    }

    public String generateRandomId(String key) {
        if (generatedIds.containsKey(key)) {
            return generatedIds.get(key);
        }

        if (cacheMode == CacheMode.Locked) {
            throw new IllegalStateException(
                    "Harness is locked but no cached ID found for key: " + key
                            + ". Run tests in Unlocked mode first to generate IDs."
            );
        }

        int randomID = new Random().nextInt(99999999 - 1000000) + 1000000;
        String id = Integer.toString(randomID);
        generatedIds.put(key, id);
        writeIds();
        return id;
    }

    public IRequestIdProvider createRequestIdProvider(String key) {
        return new IRequestIdProvider() {
            private int counter = 0;

            @Override
            public int getRequestId() {
                return Integer.parseInt(generateRandomId(key + "_" + counter++));
            }
        };
    }

    public BigDecimal generateRandomBigDecimal(String key, BigDecimal min, BigDecimal max, int scale) {
        if (generatedIds.containsKey(key)) {
            return new BigDecimal(generatedIds.get(key));
        }

        if (cacheMode == CacheMode.Locked) {
            throw new IllegalStateException(
                    "Harness is locked but no cached value found for key: " + key
                            + ". Run tests in Unlocked mode first to generate values."
            );
        }

        BigDecimal randomBigDecimal = min.add(BigDecimal.valueOf(Math.random()).multiply(max.subtract(min)));
        BigDecimal result = randomBigDecimal.setScale(scale, RoundingMode.HALF_UP);
        generatedIds.put(key, result.toString());
        writeIds();
        return result;
    }

    private void writeIds() {
        try {
            Path dir = Paths.get(RESOURCE_DIR);
            Files.createDirectories(dir);
            Path file = dir.resolve(testName + ".json");
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                gson.toJson(generatedIds, writer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write ID file for: " + testName, e);
        }
    }

    private Map<String, String> loadIdsFromFileSystem() {
        Path file = Paths.get(RESOURCE_DIR, testName + ".json");
        if (!Files.exists(file)) {
            return new LinkedHashMap<>();
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, MAP_TYPE);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read ID file: " + file, e);
        }
    }

    private Map<String, String> loadIds() {
        String resourcePath = "/citesting/" + testName + ".json";
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalStateException(
                    "Harness is locked but ID file not found: " + resourcePath
                            + ". Run tests in Unlocked mode first to generate the file."
            );
        }
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, MAP_TYPE);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read ID file: " + resourcePath, e);
        }
    }

    public enum CacheMode {
        /**
         * Unlocked: requests are proxied through to the target cert server and responses are cached.
         * Use this while developing and validating tests against the real API.
         */
        Unlocked,

        /**
         * Locked: requests are served from the cache without hitting the target cert server.
         * Use this once tests are stable and for CI runs.
         */
        Locked
    }
}
