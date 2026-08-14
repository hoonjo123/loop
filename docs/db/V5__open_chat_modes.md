# V5 - 오픈채팅 생성 유형 개편

작성일: 2026-08-14

## 변경 목적

기존의 `PERMANENT/TEMPORARY` 수명 구분을 제거하고, 공개 모집방을 카카오톡 오픈채팅과 유사한 `GROUP/ONE_TO_ONE` 대화 방식으로 구분한다.

- `GROUP`: 참여자 전원이 동일한 OPEN 방에서 대화한다.
- `ONE_TO_ONE`: 지도에는 하나의 모집방으로 노출되지만, 참여자마다 방장과 별도의 DIRECT 방이 생성된다.
- ONE_TO_ONE 모집방은 메시지 저장·WebSocket 구독 대상이 아니다.

## `chat_rooms` 변경

```sql
ALTER TABLE chat_rooms
    ADD COLUMN open_chat_type VARCHAR(20) NULL AFTER room_type,
    ADD COLUMN source_room_id BIGINT NULL AFTER direct_key,
    ADD INDEX idx_chat_rooms_open_chat_type (open_chat_type),
    ADD INDEX idx_chat_rooms_source_room_id (source_room_id),
    ADD CONSTRAINT fk_chat_rooms_source_room
        FOREIGN KEY (source_room_id) REFERENCES chat_rooms(id);

UPDATE chat_rooms
SET open_chat_type = 'GROUP'
WHERE room_type = 'OPEN' AND open_chat_type IS NULL;

ALTER TABLE chat_rooms
    DROP INDEX idx_chat_rooms_expires_at,
    DROP COLUMN duration_type,
    DROP COLUMN expires_at;
```

`open_chat_type`은 OPEN 모집방에서 필수이며 DIRECT 대화방에서는 `NULL`이다. `source_room_id`는 ONE_TO_ONE 모집방을 통해 생성된 DIRECT 방에서만 원본 모집방을 가리킨다.

## DIRECT 중복 방지 키

- 프로필에서 시작한 일반 1:1 대화: `{작은 사용자 ID}:{큰 사용자 ID}`
- ONE_TO_ONE 모집방을 통한 대화: `{모집방 ID}:{작은 사용자 ID}:{큰 사용자 ID}`

따라서 같은 모집방에서 같은 두 사용자의 DIRECT 방은 하나만 존재하지만, 서로 다른 모집방에서 만난 동일 사용자 조합은 별도 대화로 관리할 수 있다.

## 로컬 개발 주의사항

현재 `ddl-auto=update`는 새 컬럼과 인덱스를 추가하지만 기존 컬럼을 자동 삭제하지 않는다. 로컬 데이터 보존이 필요하면 위 순서대로 백필 후 제거하고, 운영 배포 전에는 동일 SQL을 Flyway 변경 세트로 전환한다.
