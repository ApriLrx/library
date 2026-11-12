package org.example.library.domain.user.repository;

import org.example.library.common.PageResult;
import org.example.library.domain.user.model.User;
import org.example.library.domain.user.model.UserName;
import org.example.library.domain.user.model.UserStatus;

import java.util.Optional;

/** 用户仓储接口（领域层定义，基础设施层实现）。 */
public interface UserRepository {

    Optional<User> findByUserName(UserName userName);

    Optional<User> findById(Long id);

    void save(User user);

    PageResult<User> findAll(int page, int pageSize);

    /** 启用/禁用账号。 */
    void updateStatus(Long id, UserStatus status);
}
