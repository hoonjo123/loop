# V2 Normalize Reports

작성일: 2026-08-12

신고 도메인을 `domain/report`로 분리하고, 다형 `target_type`/`target_id`를 외래 키로 정규화했다.

## Change

- `user_reports` 제거
- `reports` 추가
  - `reporter_id` → `users.id` (신고자)
  - `reported_user_id` → `users.id` (사용자 신고 대상)
  - `reported_room_id` → `chat_rooms.id` (방 신고 대상)
  - `reported_message_id` → `chat_messages.id` (메시지 신고 대상)
- `User.submittedReports`, `User.receivedReports`로 사용자 기준 신고 이력을 양방향 1:N 매핑

## Local Development Migration

현재는 로컬 개발 스키마이며 `user_reports`에 데이터가 없다는 것을 확인한 뒤 적용한다.

```sql
DROP TABLE user_reports;
```

`ddl-auto=update`가 다음 애플리케이션 기동에서 `reports` 및 외래 키/인덱스를 생성한다. 운영 환경에서 데이터가 존재하면 삭제 대신 기존 행의 신고 대상을 판별하여 대응하는 외래 키 열로 사전 이관해야 한다.
