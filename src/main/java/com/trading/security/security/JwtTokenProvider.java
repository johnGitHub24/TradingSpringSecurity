package com.trading.security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 【職責】簽發、驗證並解析 JWT 中的使用者與角色宣告。
 * 【技巧】以 JJWT 與 HMAC 密鑰建立簽章，並將 roles 放在自訂 claim。
 * 【概念】JWT 讓 API 可無狀態驗證身分；相較於伺服器 Session，水平擴展時不必共享登入狀態。
 * 【邊界】不讀取 HTTP Header；敏感密鑰由設定注入，正式環境應以環境變數覆寫。
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    /**
     * 【職責】以設定檔密鑰與過期毫秒數初始化簽章金鑰。
     * 【技巧】使用 {@link Keys#hmacShaKeyFor(byte[])} 將字串密鑰轉為 HMAC {@link SecretKey}。
     * 【概念】金鑰長度需符合演算法要求；過短密鑰會在啟動時失敗，避免弱金鑰上線。
     * @param secret HMAC 密鑰字串
     * @param expirationMs Token 有效毫秒數
     */
    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * 【職責】簽發含 username 與 roles 的 compact JWT。
     * 【技巧】以 JJWT builder 設定 subject、claim、簽發與過期時間後簽章。
     * 【概念】把授權資訊放進 Token，後續請求不必再查庫即可做粗粒度授權。
     * @param username 主體帳號
     * @param roles 角色字串列表（通常含 ROLE_ 前綴）
     * @return compact JWT 字串
     */
    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 【職責】驗證 Token 簽章與有效期是否可接受。
     * 【技巧】以 try／catch 包住解析；失敗僅記錄 debug 並回傳 false，不向上拋出。
     * 【概念】過濾器需要布林結果決定是否建立認證；把例外轉成 false 可讓請求繼續走到授權規則。
     * @param token JWT 字串
     * @return 有效為 true；無效為 false
     */
    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT 驗證失敗: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * 【職責】自 Token 取出 subject（帳號）。
     * 【技巧】解析簽章後的 Claims 並讀取 subject。
     * 【概念】subject 是 JWT 慣用的主體識別欄位，對應本系統的 username。
     * @param token JWT 字串
     * @return username
     */
    public String getUsername(String token) {
        return parse(token).getSubject();
    }

    /**
     * 【職責】自 Token 取出 roles claim。
     * 【技巧】以型別檢查確保 claim 為 List，否則回傳空列表避免 ClassCastException。
     * 【概念】缺漏或型別不符時採安全預設，可避免舊 Token 或惡意內容直接打垮過濾器。
     * @param token JWT 字串
     * @return 角色列表；缺漏時回空列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object roles = parse(token).get("roles");
        return roles instanceof List ? (List<String>) roles : List.of();
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
