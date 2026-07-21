package com.trading.security.service;

import com.trading.security.domain.OrderStatus;
import com.trading.security.dto.CreateOrderRequest;
import com.trading.security.dto.OrderResponse;
import com.trading.security.entity.OrderEntity;
import com.trading.security.exception.DuplicateOrderException;
import com.trading.security.exception.OrderNotFoundException;
import com.trading.security.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 【職責】集中執行訂單建立、查詢、取消與刪除的業務用例，並保護 clientOrderId 的冪等性。
 * 【技巧】使用 Spring {@code @Transactional} 劃定寫入交易與唯讀交易，透過 Spring Data Repository 存取 Entity，再映射為 API DTO。
 * 【概念】Service 是業務規則的唯一入口；Controller 不需知道 JPA Entity 或交易細節，因此可讓不同 API 入口重用相同規則並降低資料一致性風險。
 * 【邊界】不組裝 HTTP 狀態碼、不解析 JWT，也不承擔資料庫連線與 SQL 實作。
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * 【職責】驗證 clientOrderId 尚未使用後，建立初始為 {@link OrderStatus#NEW} 的訂單。
     * 【技巧】先以 Repository 檢查冪等鍵，再在同一交易內建立並儲存 Builder 產生的 Entity，最後以 DTO 隔離持久化模型。
     * 【概念】clientOrderId 是呼叫端可重試的唯一識別；先拒絕重複鍵可避免網路重送造成兩筆訂單，而 Entity 不直接外流則避免 API 與資料表結構耦合。
     * 【邊界】不從 HTTP 請求取得使用者；下單者由呼叫端已驗證的身份傳入。
     *
     * @param request 已通過格式驗證的建立訂單內容
     * @param username 已認證的下單帳號
     * @return 新建且已持久化的訂單回應
     * @throws DuplicateOrderException 當 clientOrderId 已被使用時
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String username) {
        if (orderRepository.existsByClientOrderId(request.clientOrderId())) {
            throw new DuplicateOrderException(request.clientOrderId());
        }
        OrderEntity entity = OrderEntity.builder()
                .clientOrderId(request.clientOrderId())
                .symbol(request.symbol())
                .side(request.side())
                .quantity(request.quantity())
                .price(request.price())
                .status(OrderStatus.NEW)
                .username(username)
                .build();
        OrderEntity saved = orderRepository.save(entity);
        return OrderResponse.from(saved);
    }

    /**
     * 【職責】依主鍵讀取訂單並轉換為 API 回應。
     * 【技巧】使用 {@code @Transactional(readOnly = true)} 宣告唯讀存取，並重用私有查找方法統一不存在時的例外。
     * 【概念】唯讀交易可向框架表達不會寫入資料的意圖；集中查找邏輯使各用例的「訂單不存在」語意保持一致。
     * 【邊界】不處理 HTTP 404 回應，該轉換由全域例外處理器負責。
     *
     * @param id 要查詢的訂單主鍵
     * @return 與持久化 Entity 對應的訂單 DTO
     * @throws OrderNotFoundException 當指定訂單不存在時
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return OrderResponse.from(findOrThrow(id));
    }

    /**
     * 【職責】依呼叫端指定的分頁與排序條件列出訂單。
     * 【技巧】使用 Spring Data 的 {@link Pageable} 與 {@link Page#map(java.util.function.Function)}，在保留分頁中繼資料的同時將 Entity 轉成 DTO。
     * 【概念】資料分頁應在 Repository 層執行而不是先取出所有資料再切割，這可限制資料庫與記憶體負擔，且使總筆數與頁面資訊正確一致。
     * 【邊界】不在此實作依使用者或狀態的額外篩選規則。
     *
     * @param pageable 頁碼、頁大小與排序條件
     * @return 保留分頁資訊的訂單 DTO 頁面
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }

    /**
     * 【職責】將指定訂單的狀態改為 {@link OrderStatus#CANCELLED} 並回傳更新結果。
     * 【技巧】在交易中載入受管理的 JPA Entity、更新狀態，再呼叫 Repository 儲存以取得明確的持久化結果。
     * 【概念】取消採狀態轉移而非直接刪除，可保留交易歷程供查詢或稽核；狀態變更集中於 Service，方便未來補上合法轉移檢查。
     * 【邊界】目前不處理成交狀態等更細緻的取消資格判斷。
     *
     * @param id 要取消的訂單主鍵
     * @return 更新後的訂單 DTO
     * @throws OrderNotFoundException 當指定訂單不存在時
     */
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        OrderEntity entity = findOrThrow(id);
        entity.setStatus(OrderStatus.CANCELLED);
        return OrderResponse.from(orderRepository.save(entity));
    }

    /**
     * 【職責】刪除指定訂單。
     * 【技巧】先使用共用查找方法取得 Entity，再在同一交易中交由 Repository 刪除。
     * 【概念】先確認資源存在可讓刪除與查詢使用一致的「不存在」錯誤語意，而交易界線可確保刪除作業完整提交或回滾。
     * 【邊界】不檢查呼叫者權限；授權已由安全過濾鏈在進入 API 前處理。
     *
     * @param id 要刪除的訂單主鍵
     * @throws OrderNotFoundException 當指定訂單不存在時
     */
    @Transactional
    public void deleteOrder(Long id) {
        OrderEntity entity = findOrThrow(id);
        orderRepository.delete(entity);
    }

    private OrderEntity findOrThrow(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
