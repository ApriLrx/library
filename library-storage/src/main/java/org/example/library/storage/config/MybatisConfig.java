package org.example.library.storage.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** MyBatis 配置：扫描 Mapper 接口（XML 位置见 application.yml 的 mapper-locations）。 */
@Configuration
@MapperScan("org.example.library.storage.mapper")
public class MybatisConfig {
}
