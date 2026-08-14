# 채팅 기능 구현 문서

작성일: 2026-08-13

## 구현 범위

- 그룹채팅·1:1 모집방 생성, 목록, 상세, 참여
- 그룹채팅의 입장·퇴장과 1:1 모집방별 개별 DIRECT 대화
- 참여자 목록, 방장 표시, 방장 강퇴
- 1:1 채팅방 생성과 중복 방 방지
- 내 채팅 목록, 최신 메시지, 읽지 않은 메시지 수
- WebSocket/STOMP 실시간 텍스트 메시지
- 메시지 DB 저장, 과거 조회, 검색, 소프트 삭제
- 사용자·채팅방·메시지 신고
- 사용자 차단 및 차단 관계의 1:1 대화 제한
- `IMAGE` 메시지 타입과 `image_url` 저장 구조 유지

실제 이미지 업로드와 S3 Presigned URL 발급은 스토리지 설정이 준비될 때 연결한다. 프론트의 이미지 버튼은 현재 비활성 상태다.

## WebSocket 인증 원리

Access Token은 `HttpOnly` Cookie에 저장된다. `HttpOnly`는 브라우저 JavaScript가 토큰 문자열을 읽을 수 없게 만들지만, 동일 사이트 요청에서 브라우저가 Cookie를 전송하는 동작은 막지 않는다.

```text
브라우저
  -> GET /ws (HTTP Upgrade + loop_access_token Cookie)
  -> Spring Security JWT Filter
  -> WebSocket Handshake Interceptor
  -> JWT 서명, 타입, 만료, Redis 폐기 여부 검증
  -> userId, tokenId, expiration을 WebSocket session에 저장
```

연결 이후에도 WebSocket 세션이 영구 인증으로 취급되지 않는다.

- STOMP `SEND`와 `SUBSCRIBE`마다 Access Token 만료 시각과 Redis 폐기 여부를 재검사한다.
- `/topic/chat/rooms/{roomId}` 구독 시 현재 사용자가 활성 Room Member인지 검사한다.
- 메시지 전송 시에도 서비스 계층에서 멤버십과 채팅방 활성 상태를 다시 검사한다.
- 프론트는 Access Token 만료 전에 Refresh Cookie로 토큰을 회전하고 WebSocket을 재연결한다.
- 로그아웃으로 Access Token이 Redis 폐기 목록에 들어가면 기존 WebSocket의 다음 메시지 동작도 거부된다.

따라서 클라이언트가 전달한 `roomId`나 STOMP 목적지만 신뢰하지 않는다.

## 읽지 않은 메시지 실시간 배지

프론트는 내 채팅 목록에 포함된 모든 방을 하나의 STOMP 연결에서 구독한다. 현재 열어둔 방이 아닌 곳에서 다른 사용자의 메시지가 수신되면 해당 대화의 `unreadCount`와 전체 내 채팅 배지를 즉시 증가시킨다.

- 현재 열어둔 방의 메시지는 즉시 읽음 처리되므로 배지를 증가시키지 않는다.
- 본인이 보낸 메시지와 SYSTEM 메시지는 읽지 않은 수에서 제외한다.
- 구독 중인 방이 추가·삭제되면 기존 연결을 유지하면서 해당 방 구독만 동기화한다.
- 30초 API 재조회는 WebSocket 재연결 구간의 누락 가능성을 보정하는 용도로 유지한다.
- Access Token 갱신 시 Cookie를 회전한 뒤 STOMP 연결과 전체 방 구독을 다시 구성한다.

## 실시간 메시지 흐름

```text
STOMP SEND /app/chat/rooms/{roomId}/messages
  -> WebSocket 세션 토큰 검증
  -> 사용자 Room Member 여부 검증
  -> 방 상태와 실제 메시지 대화방 여부 검증
  -> 메시지 타입과 본문 검증
  -> MySQL 저장
  -> /topic/chat/rooms/{roomId} Broadcast
```

메시지는 실시간 전달 전에 DB에 먼저 저장한다. 구독자는 서버가 반환한 메시지 ID를 기준으로 중복 메시지를 제거한다.

## REST API

| Method | Endpoint | 기능 |
| --- | --- | --- |
| `POST` | `/api/chat-rooms` | `GROUP` 또는 `ONE_TO_ONE` 공개 모집방 생성 |
| `GET` | `/api/chat-rooms?region=` | 활성 오픈채팅방 목록 |
| `GET` | `/api/chat-rooms/{roomId}` | 채팅방 상세 |
| `POST` | `/api/chat-rooms/{roomId}/members` | 그룹방 입장 또는 1:1 모집방의 방장-참여자 DIRECT 방 반환 |
| `DELETE` | `/api/chat-rooms/{roomId}/members/me` | 채팅방 퇴장 |
| `DELETE` | `/api/chat-rooms/{roomId}/members/{userId}` | 방장 강퇴 |
| `DELETE` | `/api/chat-rooms/{roomId}` | 방장의 채팅방 종료 |
| `GET` | `/api/chat-rooms/{roomId}/members` | 참여자 목록 |
| `POST` | `/api/chat-rooms/direct` | 1:1 채팅방 생성 또는 기존 방 반환 |
| `GET` | `/api/chat-rooms/me/conversations` | 내 채팅 목록 |
| `PUT` | `/api/chat-rooms/{roomId}/read` | 마지막 읽은 시각 갱신 |
| `GET` | `/api/chat-rooms/{roomId}/messages` | 과거 메시지 커서 조회 |
| `GET` | `/api/chat-rooms/{roomId}/messages/search?query=` | 메시지 검색 |
| `DELETE` | `/api/chat-rooms/{roomId}/messages/{messageId}` | 본인 메시지 소프트 삭제 |
| `POST` | `/api/users/me/blocks/{userId}` | 사용자 차단 |
| `DELETE` | `/api/users/me/blocks/{userId}` | 사용자 차단 해제 |
| `POST` | `/api/reports` | 사용자·방·메시지 신고 |

## 공개 모집방의 두 가지 대화 방식

- `GROUP`은 모집방과 실제 메시지 방이 동일하다. 생성자는 방장이 되고 참여자는 같은 Room Member 집합에 들어간다.
- `ONE_TO_ONE` 모집방은 지도 탐색과 참여 진입점으로만 사용한다. 참여하면 모집방 ID와 두 사용자 ID로 `direct_key`를 만들고 방장과 참여자만 속한 DIRECT 방을 반환한다.
- 여러 사용자가 같은 ONE_TO_ONE 모집방에 참여해도 서로를 볼 수 없으며 각자 방장과만 대화한다.
- ONE_TO_ONE 모집방 ID로 메시지 조회·검색·전송·WebSocket 구독을 요청하면 서버가 거부한다.
- 영구/임시 수명 구분과 자동 만료 스케줄러는 제거했다. 방은 방장이 종료할 때까지 유지된다.

## 1:1 대화와 차단

프로필에서 시작한 1:1은 두 사용자 ID를, 모집방에서 시작한 1:1은 모집방 ID와 두 사용자 ID를 결합한 `direct_key`에 Unique 제약조건을 적용한다. 어느 한쪽이라도 상대를 차단했다면 신규 1:1 방 생성과 기존 DIRECT 방 메시지 전송을 서버에서 거부한다.

## 메시지 조회와 삭제

- 과거 메시지는 `beforeId` 커서와 최대 100개 제한을 사용한다.
- 검색은 초기 MVP 정책대로 MySQL `LIKE` 검색을 사용하고 최대 100개를 반환한다.
- 삭제는 레코드와 원본 `content`, `image_url`을 보존하고 `deleted_at`만 기록한다.
- 일반 API 응답에서는 삭제된 원문과 이미지 URL을 `null`로 마스킹하여 프론트에 노출하지 않는다.
- 운영자는 추후 신고 조사와 증거 확인을 위한 별도 관리자 권한 API에서 보존된 원문을 확인할 수 있다.
- 프론트에는 삭제된 메시지라는 표시만 노출한다.
