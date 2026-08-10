# loop — 지역 기반 실시간 오픈 커뮤니티 서비스 기획안

> **Version**: MVP v1.0  
> **작성 기준일**: 2026-08-10  
> **서비스 형태**: 모바일 퍼스트 웹 → 사용자 반응 검증 후 React Native 앱 확장  
> **핵심 키워드**: 지도 탐색 · 지역 커뮤니티 · 실시간 채팅 · 1:1 DM · 사용자 평판 · 안전한 커뮤니티

---

## 1. 서비스 개요

### 서비스명

**loop** *(가칭)*

### 한 줄 소개

> **지도를 돌아다니며 지금 살아 있는 지역의 대화를 발견하고, 이웃과 자연스럽게 연결되는 실시간 오픈 커뮤니티**

### 서비스 목적

현대인은 바쁜 일상 속에서 새로운 사람과 자연스럽게 대화하거나 지역 사람들과 관계를 만들 기회가 적다.

`loop`는 게시글 중심 커뮤니티나 카테고리 중심 서비스가 아니라, **지역과 실시간 대화 자체에 집중한 커뮤니티**를 목표로 한다.

사용자는 지도를 탐색하면서 특정 지역에서 운영 중인 채팅방을 발견하고, 자유롭게 참여하거나 직접 방을 만들 수 있다.

일회성 모임뿐만 아니라 지속적인 지역 커뮤니티를 운영할 수 있도록 **임시 채팅방과 영구 채팅방을 모두 지원**한다.

---

# 2. 핵심 서비스 철학

서비스의 핵심 흐름은 다음과 같다.

```text
지도 탐색
   ↓
지역 발견
   ↓
활성 커뮤니티 발견
   ↓
오픈채팅 참여
   ↓
사람과 대화
   ↓
1:1 관계 확장
   ↓
상호 평가
   ↓
커뮤니티 평판 축적
```

핵심은 다음 세 가지다.

1. **지도**
2. **실시간 채팅**
3. **커뮤니티 평판**

게시판, 피드, 복잡한 추천 알고리즘 등은 초기 MVP에서 제외한다.

---

# 3. 주요 차별점

## 3.1 지도가 메인 UI

일반적인 커뮤니티처럼 상단 메뉴나 카테고리 목록을 중심으로 탐색하지 않는다.

로그인 후 사용자가 처음 마주하는 화면은 **대한민국 지도**다.

```text
대한민국
   ↓
서울특별시
   ↓
마포구
   ↓
서교동
   ↓
현재 활동 중인 채팅방
```

사용자는 지도를 직접 드래그하고 확대하면서 지역을 탐색한다.

---

## 3.2 지역별 채팅방 개수 표시

네이버 부동산 등의 지도 UI와 유사하게 지역 위에 현재 존재하는 채팅방 수를 표시한다.

예시:

```text
서울 128

경기 96
인천 42

부산 57
대구 38
```

서울을 확대하면:

```text
마포구     23
서대문구   14
성동구     18
강남구     41
```

더 확대하면:

```text
홍대   12
신촌    7
합정    9
연남    5
```

숫자를 클릭하면 해당 지역의 채팅방 목록이 노출된다.

---

## 3.3 지역 Hover / 선택 강조 효과

PC 웹에서 마우스 커서를 특정 지역 위에 올리면 해당 지역이 **살짝 입체적으로 떠오르는 2.5D 효과**를 제공한다.

예시 효과:

- `translateY`
- 아주 약한 `scale`
- drop shadow
- 지역 경계선 강조
- 배경 밝기 변화
- 채팅 활성도 정보 표시

과도한 3D 효과보다는 자연스럽고 부드러운 UI를 지향한다.

모바일에서는 Hover 대신 Tap 인터랙션으로 대체한다.

---

## 3.4 지역 활성도 표시

단순 채팅방 개수뿐 아니라 지역의 실시간 활성도를 표현할 수 있다.

예:

```text
홍대 🔥 32
신촌    12
합정 🔥 19
```

활성도 계산 후보:

- 최근 10분 메시지 수
- 최근 1시간 메시지 수
- 현재 접속 사용자 수
- 활성 채팅방 수

초기 MVP에서는 단순화하여 구현하고, 운영 데이터가 쌓인 후 개선한다.

---

# 4. 지도 정책

## 위치 기준

초기에는 사용자의 GPS 정확한 좌표를 다른 사용자에게 공개하지 않는다.

기본 지역 단위:

```text
시 / 도
  ↓
시 / 군 / 구
  ↓
읍 / 면 / 동 (필요 시)
```

사람의 실제 위치가 아니라 **채팅방이 속한 지역만 지도에 표시**한다.

### 원칙

- 사용자 정확한 GPS 위치 공개 금지
- 사람 아이콘을 지도에 직접 표시하지 않음
- 채팅방 또는 지역 단위 정보만 표시
- 위치 노출에 따른 프라이버시 문제 최소화

---

# 5. 채팅방

## 5.1 채팅방 종류

### 영구 커뮤니티

종료 시간이 없는 지속형 채팅방.

예:

```text
홍대 직장인 수다방
신촌 대학원생 이야기방
마포구 주민 자유대화
```

### 임시 커뮤니티

설정한 시간이 지나면 종료되는 일시적 채팅방.

예:

```text
오늘 7시 홍대에서 저녁 먹을 사람
지금 한강 산책할 사람
신촌에서 카공 2시간
```

방 생성 시 사용자가 유형을 선택한다.

---

# 6. 초기 MVP 채팅 기능

초기 버전에서는 **채팅 기능 자체의 완성도에 집중**한다.

## 오픈채팅 N:N

필수 기능:

- 채팅방 생성
- 채팅방 입장
- 채팅방 퇴장
- 실시간 텍스트 메시지
- 이미지 전송
- 과거 메시지 조회
- 채팅방 내 메시지 검색
- 현재 참여자 목록
- 방장 권한
- 사용자 강퇴
- 메시지 삭제
- 신고
- 사용자 차단

후순위 기능:

- 메시지 Reply
- 읽음 표시
- 리액션
- 멘션
- typing indicator
- 음성
- 동영상
- 파일
- 투표

---

# 7. 1:1 Direct Message

사용자 프로필에서 다른 사용자에게 1:1 메시지를 보낼 수 있다.

기능:

- 1:1 채팅방 생성
- 채팅 목록
- 실시간 메시지
- 이미지 전송
- 과거 메시지 조회
- 검색
- 읽지 않은 메시지 수
- 상대방 차단
- 상대방 신고

오픈채팅과 동일한 WebSocket/STOMP 기반 구조를 사용하되 Room Type으로 구분할 수 있다.

예:

```text
ROOM_TYPE
- OPEN
- DIRECT
```

---

# 8. 사용자 프로필

프로필 사진 등록은 **선택 사항**이다.

사진 미등록 시 기본 아바타를 제공한다.

## 기본 프로필 정보

예:

```text
닉네임
프로필 이미지 (선택)
연령대
지역
한 줄 소개
가입일
최근 활동
```

성별 등 민감하거나 불필요한 필드는 초기 MVP에서는 최소화한다.

---

# 9. 커뮤니티 활동 기록

프로필에는 사용자의 커뮤니티 활동 기록을 표시한다.

예:

```text
커뮤니티 참여       63회
만든 커뮤니티        8개
대화한 사용자       214명
받은 평가            46개
활동 기간             11개월
```

단순 자기소개보다 **실제 서비스 내 활동 이력**을 사용자 신뢰 판단의 근거로 사용한다.

---

# 10. 사용자 평판 시스템

가칭:

> **어울림 지수**

당근의 매너온도와 유사한 개념이지만 단순 점수 하나만으로 사람을 단정하지 않는다.

프로필 예:

```text
어울림 지수 82

긍정 평가 97%
평가 참여자 46명

커뮤니티 참여 63회
활동 기간 11개월
```

---

# 11. 상호 평가

사용자가 실제로 함께 커뮤니티 활동을 한 사람을 평가할 수 있다.

## 좋은 평가 예시

- 친절해요
- 대화가 편해요
- 매너가 좋아요
- 약속을 잘 지켜요
- 분위기를 즐겁게 해요
- 대화에 적극적이에요

## 아쉬운 평가 예시

- 불편한 표현을 했어요
- 반복적으로 방해했어요
- 과도하게 연락해요
- 약속을 지키지 않았어요

초기 MVP에서는 **자유입력 공개 후기 기능을 사용하지 않는다.**

이유:

- 악의적인 평판 테러
- 명예훼손 문제
- 보복성 평가
- 인신공격
- 서비스 운영 부담

---

# 12. 평가 자격

아무 사용자나 다른 사용자를 평가할 수 없도록 제한한다.

예시 조건:

```text
같은 채팅방 참여
       +
일정 시간 이상 함께 활동
       또는
일정 수 이상의 실제 대화 발생
       ↓
평가 권한 획득
```

추가 제한:

- 동일 사용자에 대한 반복 평가 제한
- 일정 기간 내 1회만 평가
- 차단된 사용자의 악의적 반복 평가 방지
- 운영자 판단에 따른 평가 무효화 가능

---

# 13. 신규 사용자 처리

평가가 거의 없는 신규 사용자에게 임의의 낮거나 높은 점수를 보여주지 않는다.

예:

```text
아직 평가가 부족해요.
평가 2개
```

평가 표본 수를 반드시 함께 표시한다.

예:

```text
어울림 지수 91
183명이 평가했어요.
```

---

# 14. 신고와 평가는 분리

평가와 신고는 목적이 다르다.

```text
평가
 ↓
커뮤니티 평판

신고
 ↓
관리자 검토
 ↓
경고 / 이용제한 / 영구정지
```

폭력, 성희롱, 사기, 개인정보 노출 등의 심각한 문제는 평가가 아니라 신고 기능을 사용한다.

---

# 15. 방장 기능

방장은 참여자의 프로필과 평판을 확인할 수 있다.

예:

```text
새로운 참여자

닉네임: 고라니
어울림 지수: 87
활동기간: 7개월
커뮤니티 참여: 31회
긍정 평가: 96%

[프로필]
[1:1 대화]
[강퇴]
[신고]
```

최근 부정 평가가 많은 사용자의 경우 중립적인 경고를 제공할 수 있다.

예:

> 최근 커뮤니티 활동에서 부정적인 평가가 여러 건 확인되었습니다.

단,

> 위험한 사람입니다.

처럼 사람을 단정하는 표현은 사용하지 않는다.

---

# 16. 회원가입 / 로그인

지원 방식:

1. Google OAuth2
2. 이메일 인증 기반 자체 회원가입

초기 OAuth 제공자는 Google 하나만 지원한다.

---

# 17. 자체 이메일 회원가입

기본 흐름:

```text
이메일 입력
   ↓
이메일 형식 검사
   ↓
Disposable Email 검사
   ↓
인증번호 발송
   ↓
인증 코드 확인
   ↓
비밀번호 설정
   ↓
회원가입
```

## Temporary Mail 대응

다음 방식들을 함께 적용한다.

- Disposable Email Domain blacklist
- 이메일 인증 필수
- IP 기반 가입 Rate Limit
- 이메일 인증 요청 Rate Limit
- 비정상 가입 패턴 감지
- 필요 시 CAPTCHA 도입

단순 temp-mail blacklist 하나만으로 완벽한 차단이 가능하다고 가정하지 않는다.

---

# 18. 이메일 인증

Redis TTL을 이용할 수 있다.

예:

```text
email:verify:user@example.com
→ 583921

TTL = 5분
```

인증 완료 후 즉시 삭제한다.

---

# 19. 인증 구조

인증 방식:

```text
Google OAuth2
        또는
Email + Password
          ↓
     Spring Security
          ↓
       JWT 발급
```

## Access Token

- JWT 사용
- 상대적으로 짧은 TTL
- 예: 15~30분
- API 요청 인증

## Refresh Token

- Redis 저장
- Access Token보다 긴 TTL
- 서버에서 세션 강제 폐기 가능

---

# 20. Refresh Token 세션 관리

단순히 User 단위 하나의 Refresh Token만 저장하지 않는다.

예:

```text
refresh:{userId}:{sessionId}
```

저장 정보 예:

```text
tokenHash
device
createdAt
expiresAt
```

이를 통해 다음 기능을 구현할 수 있다.

- 현재 로그인된 기기 확인
- 특정 기기 로그아웃
- 전체 기기 로그아웃
- 의심 세션 강제 삭제

Refresh Token 원문 저장 대신 해시 저장을 고려한다.

---

# 21. Refresh Token Rotation

보안 강화를 위해 Refresh Token Rotation을 적용한다.

초기 로그인:

```text
Access Token A
Refresh Token A
```

재발급:

```text
Refresh A 사용
      ↓
Access B 발급
Refresh B 발급
      ↓
Refresh A 즉시 폐기
```

이미 폐기된 Refresh Token이 다시 사용되면 **탈취 가능성**으로 판단할 수 있다.

예:

```text
폐기된 Refresh Token 재사용
          ↓
Reuse Detection
          ↓
해당 Session 또는 Token Family 폐기
          ↓
강제 로그아웃
          ↓
재로그인 요구
```

---

# 22. Redis 활용

Redis 사용 목적:

### 인증

- Refresh Token
- Refresh Rotation
- 이메일 인증번호
- 세션 무효화

### 실시간 서비스

- WebSocket Scale-out 대비
- Redis Pub/Sub
- 접속 상태
- 지역 활성도
- Rate Limit

### TTL 데이터

- 인증번호
- Temporary Room 관련 데이터
- Rate Limit
- 일시적인 상태 값

---

# 23. 실시간 채팅

Polling 방식은 사용하지 않는다.

기본 기술:

- WebSocket
- STOMP
- Spring Messaging

구조:

```text
Client
   ↓
STOMP SEND
   ↓
Spring WebSocket
   ↓
메시지 검증
   ↓
DB 저장
   ↓
Broadcast
```

Scale-out 시:

```text
Client
   ↓
Spring Instance A
   ↓
Redis Pub/Sub
   ↓
Spring Instance B
   ↓
다른 WebSocket Client
```

---

# 24. STOMP 인증

WebSocket 연결 시 JWT 인증을 수행한다.

예:

```text
STOMP CONNECT

Authorization: Bearer {accessToken}
          ↓
ChannelInterceptor
          ↓
JWT 검증
          ↓
Authentication / Principal 생성
```

메시지 전송 시에도 단순 roomId만 신뢰하지 않는다.

서버에서 반드시:

```text
현재 사용자
     ↓
해당 Room Member 여부 확인
     ↓
전송 권한 검증
```

을 수행한다.

---

# 25. REST API와 WebSocket 역할 분리

## REST API

- 회원가입
- 로그인
- 프로필
- 채팅방 생성
- 채팅방 검색
- 채팅방 입장/퇴장
- 과거 메시지 조회
- 지도 조회
- 신고
- 사용자 평가
- 이미지 Presigned URL
- 관리자 기능

## WebSocket

- 실시간 메시지
- 실시간 입장/퇴장 이벤트
- 접속 인원
- 읽음 이벤트 (추후)
- typing event (추후)

모든 API를 WebSocket으로 구현하지 않는다.

---

# 26. 메시지 저장

WebSocket은 실시간 전달 채널이다.

대화 기록 보존을 위해 메시지는 DB에 저장한다.

예시:

```text
CHAT_MESSAGE

message_id
room_id
sender_id
message_type
content
image_url
created_at
deleted_at
```

메시지 조회는 REST API + Cursor Pagination을 고려한다.

---

# 27. 메시지 검색

MVP 초기에는 MySQL 기반으로 시작한다.

예:

```text
GET /api/chat-rooms/{roomId}/messages/search?q=강남
```

운영 데이터가 증가하고 실제 성능 문제가 발생하면:

- MySQL FULLTEXT
- Elasticsearch
- OpenSearch

등으로 확장한다.

초기부터 Elasticsearch를 넣지 않는다.

---

# 28. 이미지 업로드

이미지는 Spring Server를 경유하지 않고 **S3 Presigned URL**을 이용한다.

구조:

```text
Client
   ↓
Spring Boot
   ↓
Presigned URL 발급
   ↓
Client
   ↓
S3 직접 Upload
```

장점:

- Backend 트래픽 감소
- 서버 메모리 사용 감소
- Web / React Native 공통 사용 가능

사용처:

- 사용자 프로필 이미지
- 채팅 이미지

초기 MVP에서는 이미지 파일만 지원한다.

---

# 29. Frontend 전략

## 1단계

**React 기반 모바일 퍼스트 웹**

목표:

- 빠른 MVP 개발
- 빠른 배포
- 사용자 반응 검증
- 개발 복잡도 최소화

모바일 레이아웃 중심으로 설계한다.

---

# 30. React Native 확장

웹 서비스의 반응을 확인한 이후 Expo + React Native 앱을 개발한다.

Backend는 동일한 REST/WebSocket API를 사용한다.

```text
React Web ───────┐
                 │
                 ├── Spring Boot API
                 │
React Native ────┘
```

재사용 가능한 영역:

- API 호출 구조
- DTO / TypeScript Type
- 상태 관리
- React Query
- Custom Hook
- validation
- WebSocket/STOMP 로직
- Business Logic
- Utility

새로 구현할 영역:

- UI Component
- Navigation
- Secure Storage
- Native OAuth
- Push Notification
- Image Picker
- Mobile Permission

---

# 31. 지도 Frontend 구현 방향

후보 기술:

- SVG + GeoJSON
- Mapbox 계열
- Kakao Map / Naver Map 등 지도 SDK
- 필요 시 WebGL

초기 UI의 핵심이 **행정구역의 인터랙티브 강조**라면 SVG/GeoJSON 기반 구현도 적극 검토한다.

지역 Hover 예:

```css
.region {
  transition:
    transform 180ms ease,
    filter 180ms ease;
}

.region:hover {
  transform: translateY(-4px) scale(1.015);
  filter: drop-shadow(0 7px 5px rgba(0, 0, 0, 0.18));
}
```

---

# 32. 지도 API 예시

Frontend viewport 기반:

```http
GET /api/chat-rooms/map
    ?south=37.48
    &north=37.62
    &west=126.84
    &east=127.10
    &zoom=11
```

응답 예:

```json
[
  {
    "regionCode": "11440",
    "regionName": "마포구",
    "latitude": 37.566,
    "longitude": 126.901,
    "roomCount": 23,
    "activityScore": 82
  }
]
```

Zoom Level에 따라 집계 단위를 다르게 할 수 있다.

```text
대한민국
 ↓
시/도
 ↓
시/군/구
 ↓
읍/면/동
 ↓
개별 채팅방
```

---

# 33. 관리자 페이지

커뮤니티 서비스 특성상 관리자 페이지는 MVP 필수 영역이다.

기능:

- 신고 사용자 조회
- 신고 채팅방 조회
- 신고 메시지 조회
- 사용자 경고
- 일정 기간 이용 정지
- 영구 정지
- 채팅방 블라인드
- 메시지 블라인드
- 사용자 평가 무효화
- 신고 처리 이력 관리

---

# 34. 신고 사유

예:

```text
욕설 / 비방
성희롱
스팸 / 광고
사기 의심
개인정보 노출
위협적 행동
불법 콘텐츠
기타
```

신고는 반드시 운영자 확인 과정을 거친다.

단순 신고 횟수만으로 자동 영구정지를 수행하지 않는다.

---

# 35. 차단 기능

사용자 A가 사용자 B를 차단하면:

- B의 DM 차단
- B의 메시지 숨김 또는 제한
- B의 프로필 상호작용 제한
- 필요 시 같은 방 내 노출 정책 결정

차단 관계는 서버에서 강제한다.

---

# 36. 보안 기본 정책

필수:

- Spring Security
- BCrypt / Argon2 Password Hash
- JWT Validation
- Refresh Token Rotation
- Redis TTL
- Rate Limit
- Email Verification
- Disposable Email Filtering
- CORS 제한
- 입력값 Validation
- XSS 방어
- SQL Injection 방어
- 파일 MIME / Extension 검증
- 업로드 파일 크기 제한
- WebSocket Authorization
- Room Membership 검증
- 관리자 API Role 검증

---

# 37. 기술 스택

## Backend

```text
Java 21
Spring Boot 3.x
Spring Security
Spring Data JPA 또는 MyBatis
WebSocket
STOMP
```

## Frontend

```text
React
TypeScript
React Query
상태 관리: Zustand 또는 Redux Toolkit
모바일 퍼스트 UI
```

## Mobile

추후:

```text
React Native
Expo
Expo Router
SecureStore
```

## Database

```text
MySQL
```

## Cache / Realtime

```text
Redis
```

## Storage

```text
AWS S3
Presigned URL
```

## Infrastructure

```text
Docker
Docker Compose
```

운영 환경에 따라:

```text
Nginx
CI/CD
Cloud VM
```

등을 추가한다.

---

# 38. MVP에서 의도적으로 제외할 기술

초기에는 다음 기술을 억지로 사용하지 않는다.

- Kafka
- Kubernetes
- MSA
- Elasticsearch
- Service Mesh
- 과도한 CQRS
- 복잡한 Event Sourcing

실제 운영 중 필요성이 생겼을 때 추가한다.

---

# 39. MVP Scope

## 반드시 구현

### Account

- Google OAuth2
- 이메일 회원가입
- 이메일 인증
- Disposable Email Filter
- JWT
- Refresh Token
- Refresh Token Rotation
- 로그아웃

### Profile

- 닉네임
- 프로필 이미지 선택
- 한 줄 소개
- 지역
- 활동 기록
- 어울림 지수

### Map

- 대한민국 지도
- 지역별 채팅방 수
- 지도 확대
- 지역 선택
- Hover/Tap 강조
- 채팅방 리스트

### Open Chat

- 방 생성
- 영구방
- 임시방
- 입장
- 퇴장
- N:N 실시간 메시지
- 이미지
- 과거 메시지
- 메시지 검색
- 방장
- 강퇴

### Direct Message

- 1:1 채팅
- 채팅 목록
- 실시간 메시지
- 이미지

### Community Safety

- 사용자 평가
- 어울림 지수
- 신고
- 차단

### Admin

- 신고 조회
- 신고 처리
- 이용 정지
- 영구 정지
- 채팅방/메시지 블라인드

---

# 40. MVP 이후 후보

사용자 반응이 확인된 후 추가한다.

- React Native
- Push Notification
- PWA
- 메시지 Reply
- 읽음 상태
- typing indicator
- Reaction
- Mention
- 관심 지역
- 즐겨찾는 채팅방
- AI 기반 채팅방 추천
- AI 기반 신고 보조
- 자동 유해 메시지 탐지
- 검색 엔진 도입
- 서버 Scale-out
- Redis Pub/Sub 고도화
- 이벤트 브로커 도입

---

# 41. 운영 관점 핵심 지표

서비스 출시 후 반드시 기록한다.

## 사용자

- 가입자 수
- DAU
- WAU
- MAU
- 재방문율

## 커뮤니티

- 생성된 채팅방 수
- 활성 채팅방 수
- 사용자당 참여 방 수
- 평균 채팅방 참여 인원

## 메시지

- 일일 메시지 수
- 오픈채팅 메시지 수
- DM 메시지 수
- 지역별 메시지 수

## 안전

- 신고 건수
- 신고 처리 시간
- 사용자 차단 수
- 이용정지 수
- 부정 평가 비율

---

# 42. 포트폴리오 목표

이 프로젝트의 최종 목적은 단순히:

> "Spring Boot와 React로 채팅 서비스를 만들었다."

가 아니다.

다음과 같은 **실제 운영 경험**을 확보하는 것이 핵심이다.

예:

```text
실사용자 100+
MAU 50+
채팅방 100+
누적 메시지 10,000+
```

그리고 운영 과정에서 발생한 문제를 기록한다.

예:

- WebSocket 동시 접속 문제
- DB 조회 성능 저하
- 채팅 메시지 증가
- Redis 장애 대응
- S3 비용 관리
- 악성 사용자 대응
- 신고 정책 개선
- Refresh Token 재사용 탐지
- 지역 조회 캐싱
- 채팅방 동시 참여 문제

---

# 43. 이직 포트폴리오에서 강조할 내용

기존:

> Java/Spring 기반 SI 개발자

에서 다음 이미지로 확장한다.

> **실시간 지역 커뮤니티 서비스를 직접 기획·설계·개발·배포·운영한 Backend Developer**

면접에서 설명할 주요 기술적 의사결정:

1. Polling 대신 WebSocket/STOMP를 선택한 이유
2. Redis를 Refresh Token 저장소로 사용한 이유
3. Refresh Token Rotation / Reuse Detection 설계
4. Presigned URL을 사용한 이유
5. 지도 기반 지역 탐색 API 설계
6. N:N 채팅과 1:1 DM 모델링
7. 사용자 평가 조작 방지
8. 신고 시스템과 평가 시스템 분리
9. DB 메시지 조회/검색 최적화
10. 실제 운영 데이터를 바탕으로 기술을 확장한 경험

---

# 44. 개발 원칙

## 1. 기능보다 출시

완벽하게 만들고 출시하지 않는다.

작게 만들고 빠르게 출시한다.

## 2. 기술보다 문제

기술을 사용하기 위해 문제를 만들지 않는다.

문제가 발생했을 때 필요한 기술을 도입한다.

## 3. AI 적극 활용

AI를 이용해 개발 속도를 최대한 높인다.

단, 사용한 코드와 설계를 개발자가 설명할 수 있어야 한다.

## 4. 사용자 우선

기술적으로 화려한 기능보다 실제 사용자가 원하는 기능을 우선한다.

## 5. 운영 경험을 기록

장애, 개선, 비용, 사용자 행동, 신고 등 모든 운영 경험을 포트폴리오 자료로 남긴다.

---

# 45. 초기 개발 순서 제안

```text
1. Backend 기본 프로젝트
   ↓
2. MySQL / Redis / Docker
   ↓
3. Google OAuth + Email Login
   ↓
4. JWT + Refresh Token Rotation
   ↓
5. Profile
   ↓
6. 지역 데이터 / 지도
   ↓
7. Chat Room
   ↓
8. WebSocket + STOMP
   ↓
9. Message 저장 / 조회
   ↓
10. 1:1 DM
   ↓
11. 이미지 / S3
   ↓
12. 사용자 평가
   ↓
13. 신고 / 차단
   ↓
14. 관리자
   ↓
15. 배포
   ↓
16. 실제 사용자 모집
```

---

# 46. 서비스 핵심 문장

최종적으로 `loop`가 사용자에게 전달해야 하는 경험은 다음과 같다.

> **“지금 이 동네에서는 어떤 사람들이 무슨 이야기를 하고 있을까?”**

사용자가 지도를 움직이고, 살아 있는 지역을 발견하고, 사람들의 대화에 자연스럽게 참여하게 만든다.

그리고 좋은 활동이 쌓일수록 사용자의 **커뮤니티 평판과 관계가 하나의 기록으로 남는 서비스**를 목표로 한다.

---

# 47. 최종 MVP 요약

```text
                     loop

                        🌏
                        │
                    지역 지도
                        │
            ┌───────────┴───────────┐
            │                       │
        영구 커뮤니티           임시 커뮤니티
            │                       │
            └───────────┬───────────┘
                        │
                  WebSocket/STOMP
                        │
              ┌─────────┴─────────┐
              │                   │
           N:N Chat             1:1 DM
              │                   │
              └─────────┬─────────┘
                        │
                      USER
                        │
          ┌─────────────┼─────────────┐
          │             │             │
        Profile       Reputation     Report
                        │             │
                    어울림 지수       │
                                      ↓
                                    Admin
```

---

## 최종 기술 구성

```text
React + TypeScript
        │
 REST / WebSocket(STOMP)
        │
Spring Boot 3.x + Java 21
        │
 ┌──────┼─────────┐
 │      │         │
MySQL  Redis      S3
 │      │
 │      ├─ Refresh Token
 │      ├─ Token Rotation
 │      ├─ Email Verification
 │      ├─ Rate Limit
 │      └─ Pub/Sub
 │
 └─ User / Room / Message / Review / Report

Docker / Docker Compose
```

---

> **MVP의 성공 기준은 기능 수가 아니라 실제 사람이 사용하는가이다.**
>
> 웹으로 먼저 빠르게 출시하고 사용자의 반응을 검증한 뒤, 서비스가 살아남는다면 React Native 기반 모바일 앱으로 확장한다.
