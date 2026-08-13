package world.loop.domain.mail.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmailVerificationTemplateTest {

    private final EmailVerificationTemplate template = new EmailVerificationTemplate();

    @Test
    void rendersVerificationCodeAndTtl() {
        String html = template.render("583921", 5);

        assertThat(html)
                .contains("583921")
                .contains("5분 동안 유효합니다")
                .doesNotContain("{{CODE}}", "{{TTL_MINUTES}}");
    }
}
