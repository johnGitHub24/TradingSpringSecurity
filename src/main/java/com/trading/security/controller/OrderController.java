package com.trading.security.controller;

import com.trading.security.dto.CreateOrderRequest;
import com.trading.security.dto.OrderResponse;
import com.trading.security.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【職責】提供受 JWT 保護的訂單 CRUD HTTP API，接收請求後委派 {@link OrderService} 並轉為 HTTP 回應。
 * 【技巧】結合 Spring MVC 的參數綁定、Bean Validation、{@link ResponseEntity} 與 OpenAPI 註解，讓端點契約可驗證且可產生文件。
 * 【概念】薄 Controller 只處理傳輸協定：同一份訂單規則集中在 Service，能避免每個端點各自進行冪等檢查或資料存取而產生不一致。
 * 【邊界】不直接操作 Repository、不決定訂單狀態轉移，亦不自行驗證 JWT 權限。
 */
@Tag(name = "Order", description = "訂單 CRUD（需 JWT）")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 【職責】以目前已認證使用者身分建立訂單，並回傳新資源的 {@code 201 Created} 結果。
     * 【技巧】由 Spring Security 注入 {@link Authentication}，以 {@code @Valid} 驗證請求 DTO，再委派服務執行交易。
     * 【概念】使用認證主體而非客戶端傳入的 username，可防止呼叫端偽造下單者；Controller 只轉交身份，業務服務才決定建立規則。
     * 【邊界】不自行檢查 clientOrderId 是否重複，也不直接寫入資料庫。
     *
     * @param request 已通過格式驗證的建立訂單資料
     * @param authentication Security Context 中的目前登入使用者
     * @return 含新訂單資料的 {@code 201 Created} 回應
     */
    @Operation(summary = "建立訂單")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
                                                Authentication authentication) {
        OrderResponse response = orderService.createOrder(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 【職責】依訂單主鍵取得單筆訂單 API 回應。
     * 【技巧】以 {@code @PathVariable} 綁定路徑參數，並由服務層統一將不存在情況轉為領域例外。
     * 【概念】查找失敗不在每個端點手動組裝錯誤；集中拋出例外後由 Advice 對應 HTTP 狀態，可維持錯誤格式一致。
     * 【邊界】不直接查詢 Repository，也不在此決定找不到時的錯誤本文。
     *
     * @param id 要查詢的訂單主鍵
     * @return 含訂單內容的 {@code 200 OK} 回應
     */
    @Operation(summary = "查詢單筆訂單")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    /**
     * 【職責】以分頁方式回傳訂單列表。
     * 【技巧】讓 Spring Data 將請求參數綁定為 {@link Pageable}，並原樣交由服務與 Repository 套用分頁及排序。
     * 【概念】列表 API 以 Page 而非一次傳回全部資料，可限制單次資料量並提供總頁數等巡覽資訊，適合資料量成長後的查詢。
     * 【邊界】不在 Controller 實作資料排序、篩選或 DTO 映射。
     *
     * @param pageable 呼叫端提供的頁碼、頁大小與排序條件
     * @return 含訂單 DTO 與分頁中繼資料的 {@code 200 OK} 回應
     */
    @Operation(summary = "分頁查詢訂單列表")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(orderService.listOrders(pageable));
    }

    /**
     * 【職責】要求服務層取消指定訂單並回傳更新後資料。
     * 【技巧】使用 {@code @PatchMapping} 表達局部狀態更新，實際狀態轉移封裝在服務層的交易中。
     * 【概念】取消不是刪除資料，而是改變生命週期狀態；保留訂單可支援後續查詢與稽核，狀態規則也不應散落在 API 層。
     * 【邊界】不直接設定 Entity 欄位或儲存 Entity。
     *
     * @param id 要取消的訂單主鍵
     * @return 含取消後訂單資料的 {@code 200 OK} 回應
     */
    @Operation(summary = "取消訂單")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    /**
     * 【職責】刪除指定訂單並回傳 {@code 204 No Content}。
     * 【技巧】DELETE 的 ADMIN 授權由 {@link com.trading.security.config.SecurityConfig} 的 Filter Chain 在進入 Controller 前強制執行。
     * 【概念】權限規則集中在安全設定，而非各 Controller 手動 if 判斷，才能確保新增端點時使用相同的授權模型。
     * 【邊界】不自行檢查角色、不直接呼叫 Repository，也不處理訂單不存在的 HTTP 轉換。
     *
     * @param id 要刪除的訂單主鍵
     * @return 無回應本文的 {@code 204 No Content}
     */
    @Operation(summary = "刪除訂單（限 ADMIN）")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
