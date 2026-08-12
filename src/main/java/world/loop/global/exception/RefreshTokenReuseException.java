package world.loop.global.exception;

public class RefreshTokenReuseException extends RuntimeException {
    public RefreshTokenReuseException() {
        super("Refresh token reuse detected. All sessions were revoked.");
    }
}
