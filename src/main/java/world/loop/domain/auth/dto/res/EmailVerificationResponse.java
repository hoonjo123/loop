package world.loop.domain.auth.dto.res;

public record EmailVerificationResponse(
        long expiresInSeconds,
        long resendAfterSeconds
) {
}
