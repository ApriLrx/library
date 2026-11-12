package org.example.library.infra.auth;

import org.example.library.domain.user.model.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口角色访问控制：修饰 Controller 类或方法，表示仅允许指定角色访问。
 * 方法级注解优先于类级注解；未标注则仅要求登录。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    Role[] value();
}
