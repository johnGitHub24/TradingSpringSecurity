package com.trading.security.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 測試輔助：自 {@code docs/test-data} 載入 JSON fixture。
 * <p>
 * 僅供測試使用；不參與正式執行路徑。
 */
public final class SecurityTestFixtures {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path TEST_DATA_ROOT = Paths.get("docs", "test-data");

    private SecurityTestFixtures() {
    }

    /**
     * 載入相對路徑的 JSON 節點。
     *
     * @param relativePath 相對於 docs/test-data 的路徑
     * @return JsonNode
     */
    public static JsonNode loadJson(String relativePath) {
        Path path = TEST_DATA_ROOT.resolve(relativePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Fixture not found: " + path.toAbsolutePath());
        }
        try {
            return MAPPER.readTree(Files.readString(path));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read fixture: " + path, ex);
        }
    }

    /**
     * 載入相對路徑的 JSON 字串。
     *
     * @param relativePath 相對於 docs/test-data 的路徑
     * @return JSON 字串
     */
    public static String loadJsonString(String relativePath) {
        return loadJson(relativePath).toString();
    }
}
