package org.example.library.infra.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RedisKey {
    TOKEN_BLACKLIST("library:token:")
    ;

    private final String key;

    public String getKey(String refId) {
        return this.key + refId;
    }
}
