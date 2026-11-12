package org.example.library.biz.user;

import org.example.library.common.BusinessException;
import org.example.library.common.ErrorCode;
import org.example.library.common.PageResult;
import org.example.library.domain.user.model.Password;
import org.example.library.domain.user.model.User;
import org.example.library.domain.user.model.UserName;
import org.example.library.domain.user.repository.UserRepository;
import org.example.library.domain.user.service.PasswordEncoder;
import org.example.library.domain.user.service.PasswordPolicy;
import org.example.library.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户应用服务（biz 层）：只做任务编排与事务边界。
 * 用户构建规则、凭证校验分别由 {@link User} 聚合工厂与 {@link UserService} 领域服务承载。
 */
@Service
public class UserApplicationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    private final UserService userService;

    public UserApplicationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = new UserService(passwordEncoder);
    }

    /** 注册：只创建 READER，密码规则 10-20 位且含字母与数字。 */
    @Transactional
    public void register(String rawUserName, String rawPassword) {
        UserName userName = validateUserName(rawUserName);
        try {
            passwordPolicy.validate(rawPassword);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, e.getMessage());
        }

        if (userRepository.findByUserName(userName).isPresent()) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        String hash = passwordEncoder.encode(rawPassword);
        User user = User.createReader(userName, new Password(hash));
        userRepository.save(user);
    }

    /** 登录：校验凭证与状态，任一失败统一返回「用户名或密码错误」。 */
    public User login(String rawUserName, String rawPassword) {
        UserName userName;
        try {
            userName = new UserName(rawUserName);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误"));
        userService.verifyCredentials(user, rawPassword);

        // TODO: 登录失败锁定（readme 需 Redis 计数 5 次/10 分钟，暂未实现）
        return user;
    }

    public PageResult<User> listUsers(int page, int pageSize) {
        return userRepository.findAll(page, pageSize);
    }

    @Transactional
    public void updateStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        if (active) {
            user.enable();
        } else {
            user.disable();
        }
        userRepository.updateStatus(id, user.getStatus());
    }

    private UserName validateUserName(String rawUserName) {
        try {
            return new UserName(rawUserName);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, e.getMessage());
        }
    }
}
