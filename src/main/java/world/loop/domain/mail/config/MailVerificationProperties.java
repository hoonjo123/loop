package world.loop.domain.mail.config;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "auth.verification")
public record MailVerificationProperties(Duration codeTtl, Duration requestCooldown) { }
