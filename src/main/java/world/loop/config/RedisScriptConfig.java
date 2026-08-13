package world.loop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisScriptConfig {

    @Bean
    RefreshTokenRedisScripts refreshTokenRedisScripts() {
        return new RefreshTokenRedisScripts(
                script("redis/refresh-token-save.lua"),
                script("redis/refresh-token-rotate.lua"),
                script("redis/refresh-token-revoke.lua"),
                script("redis/refresh-token-revoke-all.lua")
        );
    }

    private RedisScript<Long> script(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(path));
        script.setResultType(Long.class);
        return script;
    }

    public record RefreshTokenRedisScripts(
            RedisScript<Long> save,
            RedisScript<Long> rotate,
            RedisScript<Long> revoke,
            RedisScript<Long> revokeAll
    ) {
    }
}
