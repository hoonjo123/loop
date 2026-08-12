# V1 Initial Schema

작성일: 2026-08-12

`spring.jpa.hibernate.ddl-auto=update`가 로컬 MySQL에 아래 테이블과 인덱스를 생성 또는 보완한다. 이 문서는 추후 Flyway 등 명시적 마이그레이션으로 전환할 때의 기준 이력이다.

## Tables

| Table | Purpose |
| --- | --- |
| `users` | 이메일/Google 로그인, 프로필, 권한, 이용 상태 |
| `chat_rooms` | 오픈 및 1:1 채팅방, 영구/임시 구분, 대략적 위치 |
| `chat_room_members` | 방 참여, 방장 권한, 입장/퇴장, 마지막 읽음 시각 |
| `chat_messages` | 텍스트·이미지·시스템 메시지와 삭제 시각 |
| `user_blocks` | 사용자 간 차단 관계 |
| `user_reviews` | 함께 참여한 방을 근거로 한 사용자 평가 |
| `reports` | 사용자·방·메시지 신고와 운영 처리 상태 |
| `user_suggestions` | 프론트 건의함의 건의 유형·제목·내용 |

## Indexes and Constraints

- `users.email`, `users.nickname`: unique
- `chat_rooms(region_label, status)`, `chat_rooms(expires_at)`
- `chat_room_members(room_id, user_id)`: unique; `chat_room_members(user_id, joined_at)`
- `chat_messages(room_id, created_at)`
- `user_blocks(blocker_id, blocked_id)`: unique; `user_blocks(blocked_id)`
- `user_reviews(reviewer_id, reviewee_id, room_id)`: unique; `user_reviews(reviewee_id, created_at)`
- `reports(status, created_at)`, `reports(reported_user_id, created_at)`, `reports(reported_room_id, created_at)`, `reports(reported_message_id, created_at)`

## Notes

- 지도와 방 목록에는 정확한 사용자의 위치가 아닌 방의 `region_label`과 대략화된 좌표만 저장한다.
- `DIRECT` 방은 위치/제목/소개/기간 값이 없을 수 있다. 화면 표기는 상대 사용자 프로필에서 구성한다.
- 신고 대상은 `reported_user_id`, `reported_room_id`, `reported_message_id` 외래 키로 연결한다.
