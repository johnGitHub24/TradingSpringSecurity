package com.trading.security.exception;

import com.trading.security.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 【職責】將驗證失敗、資源衝突、找不到資源與認證失敗統一轉成 {@link ErrorResponse}。
 * 【技巧】以 {@code @RestControllerAdvice} 與多個 {@code @ExceptionHandler} 依例外型別映射 HTTP 狀態碼。
 * 【概念】集中例外轉換可讓 Controller／Service 拋出語意化例外，而不必到處組裝錯誤 JSON。
 * 【邊界】不決定業務層何時拋出何種例外。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 【職責】將 Bean Validation 失敗轉為 {@code 400} 與欄位錯誤對照。
     * 【技巧】遍歷 {@link FieldError}，以 {@code putIfAbsent} 保留每個欄位第一個錯誤訊息。
     * 【概念】回傳結構化 fieldErrors 比單一字串更利於前端逐欄顯示；這是 API 可用性細節。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.validation("欄位驗證失敗", fieldErrors));
    }

    /**
     * 【職責】將訂單冪等鍵衝突轉為 {@code 409 Conflict}。
     * 【技巧】使用穩定錯誤碼 {@code DUPLICATE_ORDER}，方便用戶端依碼分支處理。
     * 【概念】409 表示資源狀態衝突，比 400 更能表達「格式正確但業務上不可建立」。
     */
    @ExceptionHandler(DuplicateOrderException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateOrderException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("DUPLICATE_ORDER", ex.getMessage()));
    }

    /**
     * 【職責】將訂單不存在轉為 {@code 404 Not Found}。
     * 【技巧】映射為 {@code ORDER_NOT_FOUND} 錯誤碼與例外訊息。
     * 【概念】資源型 API 對「找不到」使用 404，可與驗證錯誤、衝突錯誤清楚區分。
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("ORDER_NOT_FOUND", ex.getMessage()));
    }

    /**
     * 【職責】將帳號已存在轉為 {@code 409 Conflict}。
     * 【技巧】使用 {@code USERNAME_EXISTS} 錯誤碼，避免前端只能解析自由文字。
     * 【概念】註冊衝突屬於資源唯一性問題，與登入失敗的 401 語意不同。
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExists(UsernameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("USERNAME_EXISTS", ex.getMessage()));
    }

    /**
     * 【職責】將帳密錯誤或找不到使用者轉為 {@code 401 Unauthorized}。
     * 【技巧】合併處理 {@link BadCredentialsException} 與 {@link UsernameNotFoundException}，回傳相同訊息。
     * 【概念】不區分「帳號不存在」與「密碼錯誤」，可降低帳號枚舉攻擊的資訊洩漏。
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("INVALID_CREDENTIALS", "帳號或密碼錯誤"));
    }
}
