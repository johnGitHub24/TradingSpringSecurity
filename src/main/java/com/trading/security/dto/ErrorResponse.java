package com.trading.security.dto;

import java.time.Instant;
import java.util.Map;

/**
 * 【職責】作為 API 錯誤回應的統一資料契約。
 * 【技巧】以 record 承載錯誤碼、訊息、可選欄位錯誤與時間戳，並提供靜態工廠方法。
 * 【概念】穩定錯誤碼讓用戶端可程式化處理；相較於只回字串，更利於多語系與自動化重試判斷。
 * 【邊界】不決定 HTTP 狀態碼（由 ExceptionHandler 決定）。
 */
public record ErrorResponse(
        String errorCode,
        String message,
        Map<String, String> fieldErrors,
        Instant timestamp
) {
    /**
     * 【職責】建立不含欄位細節的一般錯誤回應。
     * 【技巧】以靜態工廠填入當下時間戳，fieldErrors 設為 null。
     * 【概念】工廠方法比公開多參數建構更清楚表達「一般錯誤」語意。
     * @param errorCode 機器可讀錯誤碼
     * @param message 人類可讀訊息
     * @return ErrorResponse
     */
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message, null, Instant.now());
    }

    /**
     * 【職責】建立欄位驗證失敗的錯誤回應。
     * 【技巧】固定錯誤碼為 {@code VALIDATION_FAILED}，並附上欄位對訊息的 Map。
     * 【概念】把驗證細節結構化回傳，前端才能逐欄提示，而不必自行解析訊息字串。
     * @param message 總覽訊息
     * @param fieldErrors 欄位 → 錯誤訊息
     * @return ErrorResponse
     */
    public static ErrorResponse validation(String message, Map<String, String> fieldErrors) {
        return new ErrorResponse("VALIDATION_FAILED", message, fieldErrors, Instant.now());
    }
}
