package org.example.library.storage.repository;

import org.example.library.common.DomainException;
import org.example.library.common.ErrorCode;
import org.example.library.common.PageQuery;
import org.example.library.common.PageResult;
import org.example.library.domain.user.model.Password;
import org.example.library.domain.user.model.Role;
import org.example.library.domain.user.model.User;
import org.example.library.domain.user.model.UserName;
import org.example.library.domain.user.model.UserStatus;
import org.example.library.domain.user.repository.UserRepository;
import org.example.library.storage.mapper.UserMapper;
import org.example.library.storage.po.UserPO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** 用户仓储实现（基础设施层）。 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findByUserName(UserName userName) {
        UserPO po = userMapper.selectByUserName(userName.getValue());
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public void save(User user) {
        try {
            userMapper.insert(toPO(user));
        } catch (DuplicateKeyException e) {
            // 用户名唯一索引冲突
            throw new DomainException(ErrorCode.USERNAME_EXISTS);
        }
    }

    @Override
    public PageResult<User> findAll(int page, int pageSize) {
        PageQuery pq = PageQuery.of(page, pageSize);
        long total = userMapper.countAll();
        List<User> items = userMapper.selectAll(pq.getOffset(), pq.getEffectivePageSize()).stream()
                .map(this::toDomain).collect(Collectors.toList());
        return PageResult.of(total, pq.getNormalizedPage(), pq.getEffectivePageSize(), items);
    }

    @Override
    public void updateStatus(Long id, UserStatus status) {
        userMapper.updateStatus(id, status == UserStatus.ACTIVE ? 1 : 0);
    }

    private User toDomain(UserPO po) {
        return User.builder()
                .id(po.getId())
                .userName(new UserName(po.getUserName()))
                .password(new Password(po.getPassword()))
                .role(Role.valueOf(po.getRole()))
                .status(po.getStatus() != null && po.getStatus() == 1 ? UserStatus.ACTIVE : UserStatus.DISABLED)
                .build();
    }

    private UserPO toPO(User user) {
        UserPO po = new UserPO();
        po.setId(user.getId());
        po.setUserName(user.getUserName().getValue());
        po.setPassword(user.getPassword().getHash());
        po.setRole(user.getRole().name());
        po.setStatus(user.isActive() ? 1 : 0);
        return po;
    }
}
