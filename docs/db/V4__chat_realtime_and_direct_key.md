# V4 Chat Realtime and Direct Key

작성일: 2026-08-13

실시간 오픈채팅과 중복 없는 1:1 채팅방 생성을 위해 기존 채팅 테이블을 실제 서비스 로직에 연결한다.

## 변경 사항

### `chat_rooms`

- `direct_key VARCHAR(50) NULL` 추가
- `direct_key` Unique 인덱스 추가
- 1:1 대화 참여자 ID 두 개를 오름차순으로 정렬한 `{작은 ID}:{큰 ID}` 값을 저장한다.
- `OPEN` 채팅방은 `direct_key`를 사용하지 않는다.

```sql
ALTER TABLE chat_rooms
    ADD COLUMN direct_key VARCHAR(50) NULL,
    ADD CONSTRAINT uk_chat_rooms_direct_key UNIQUE (direct_key);
```

## 기존 테이블 사용 정책

- `chat_room_members.left_at IS NULL`: 현재 참여 중인 사용자
- `chat_room_members.last_read_at`: 대화 목록의 읽지 않은 메시지 수 기준
- `chat_messages(room_id, created_at)`: 과거 메시지 커서 조회와 최신 메시지 조회
- `chat_messages.deleted_at`: 원본 본문과 이미지 URL은 보존하고 삭제 시각만 기록하는 소프트 삭제
- `chat_rooms.expires_at`: 임시방 종료 시각
- `chat_rooms.status=CLOSED`: 만료된 임시방 또는 종료된 방

## 운영 전 마이그레이션

현재 로컬 개발은 `spring.jpa.hibernate.ddl-auto=update`가 컬럼과 Unique 인덱스를 반영한다. 운영 배포 전에는 위 SQL을 Flyway 마이그레이션으로 전환하고, 기존 DIRECT 방이 존재한다면 참여자 ID를 기준으로 `direct_key`를 먼저 채운 후 Unique 제약조건을 적용한다.
