package com.trading.security.repository;

import com.trading.security.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 【職責】提供使用者實體的持久化存取。
 * 【技巧】以方法命名查詢支援依 username 查找與存在性檢查。
 * 【概念】帳號唯一性檢查依賴資料存取能力，但衝突如何對使用者呈現仍由服務與例外層決定。
 * 【邊界】不含密碼雜湊或授權規則。
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
