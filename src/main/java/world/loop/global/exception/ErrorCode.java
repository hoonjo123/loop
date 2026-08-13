package world.loop.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_001", "이미 등록된 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_002", "이미 사용 중인 닉네임입니다."),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "AUTH_003", "인증 정보가 만료되었습니다. 다시 로그인해주세요."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자 정보를 찾을 수 없습니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_CLOSED(HttpStatus.CONFLICT, "CHAT_002", "종료된 채팅방입니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT_003", "채팅방에 참여한 사용자만 이용할 수 있습니다."),
    CHAT_ROOM_OWNER_REQUIRED(HttpStatus.FORBIDDEN, "CHAT_004", "방장만 이용할 수 있는 기능입니다."),
    CHAT_ROOM_OWNER_CANNOT_LEAVE(HttpStatus.CONFLICT, "CHAT_005", "방장은 채팅방을 나갈 수 없습니다."),
    CHAT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_006", "채팅방 참여자를 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_007", "메시지를 찾을 수 없습니다."),
    CHAT_MESSAGE_DELETE_DENIED(HttpStatus.FORBIDDEN, "CHAT_008", "자신이 보낸 메시지만 삭제할 수 있습니다."),
    DIRECT_CHAT_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CHAT_009", "자기 자신과는 1:1 대화를 만들 수 없습니다."),
    INVALID_CHAT_MESSAGE(HttpStatus.BAD_REQUEST, "CHAT_010", "메시지 내용을 확인해주세요."),
    BLOCKED_USER_INTERACTION(HttpStatus.FORBIDDEN, "CHAT_011", "차단 관계인 사용자와는 1:1 대화를 이용할 수 없습니다."),
    INVALID_REPORT_TARGET(HttpStatus.BAD_REQUEST, "REPORT_001", "신고 대상을 하나만 선택해주세요."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값을 다시 확인해주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
