package world.loop.domain.mail.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailVerificationTemplate {

    private static final String TEMPLATE_PATH = "templates/mail/email-verification.html";

    public String render(String code, long ttlMinutes) {
        return loadTemplate()
                .replace("{{CODE}}", code)
                .replace("{{TTL_MINUTES}}", String.valueOf(ttlMinutes));
    }

    private String loadTemplate() {
        try {
            return new ClassPathResource(TEMPLATE_PATH)
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException("이메일 인증 템플릿을 읽을 수 없습니다.", exception);
        }
    }
}
