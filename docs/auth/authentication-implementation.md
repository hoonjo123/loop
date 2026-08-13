# 인증 구현 기술 문서

## 1. 목적과 구성

loop의 인증은 이메일/비밀번호 로그인과 Google OAuth2 로그인을 지원한다. 두 로그인 방식은 최종적으로 동일한 JWT 발급 흐름을 사용하며, Redis는 이메일 인증과 Refresh Token 세션 관리에 사용한다.

```text
이메일 로그인 또는 Google OAuth2 로그인
                ↓
             사용자 식별
                ↓
     Access Token + Refresh Token 발급
                ↓
Access Token: API 요청 인증
Refresh Token: Access Token 재발급 및 세션 관리
```

## 2. 환경변수 관리

민감한 값은 루트 `.env`에만 둔다. `.env`는 Git에서 제외되어야 하며, `application.yml`, 기술 문서, 로그, 커밋 메시지에 실제 비밀값을 기록하지 않는다.

| 환경변수 | 용도 |
| --- | --- |
| `JWT_SECRET` | JWT 서명 및 검증 키 |
| `MAIL_HOST`, `MAIL_PORT` | SMTP 서버 정보 |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP 발신 계정과 앱 비밀번호 |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | Google OAuth2 클라이언트 자격증명 |
| `FRONTEND_URL` | OAuth 성공 후 이동할 프론트 주소 |

## 3. 이메일 회원가입

### 3.1 API 흐름

| 순서 | 메서드 | 경로 | 설명 |
| --- | --- | --- | --- |
| 1 | `POST` | `/api/auth/email-verifications` | 6자리 인증번호 발송 |
| 2 | `POST` | `/api/auth/email-verifications/confirm` | 인증번호 확인 |
| 3 | `POST` | `/api/auth/sign-up` | 이메일 인증 완료 후 회원 생성 및 토큰 발급 |

### 3.2 Redis 이메일 인증 키

| 키 | 값 | TTL | 역할 |
| --- | --- | --- | --- |
| `auth:email:code:{email}` | 6자리 인증번호 | 5분 | 사용자 입력값과 비교 |
| `auth:email:cooldown:{email}` | `1` | 1분 | 짧은 시간 내 재발송 제한 |
| `auth:email:verified:{email}` | `1` | 5분 | 회원가입 직전 이메일 인증 완료 여부 |

인증번호가 맞으면 인증번호 키는 즉시 삭제한다. 대신 짧은 TTL의 인증 완료 키를 저장한다. 회원가입 시 인증 완료 키가 존재해야 하며, 가입 성공 시에도 즉시 삭제한다. 이 방식은 인증번호를 알고 있는 것만으로 장기간 가입 권한이 유지되는 문제를 줄인다.

비밀번호는 평문으로 저장하지 않는다. 회원가입 전 `BCryptPasswordEncoder`로 해시한 뒤 `users.password_hash`에 저장한다.

## 4. JWT 구조

### 4.1 Access Token

- 기본 유효기간: 10분
- `sub`: 사용자 ID
- `type`: `access`
- 용도: 보호된 API 요청의 사용자 인증

클라이언트는 API 요청 시 다음 형식으로 전달할 수 있다.

```http
Authorization: Bearer {accessToken}
```

OAuth 로그인 흐름에서는 Access Token을 `loop_access_token` HttpOnly 쿠키로도 전달한다. JWT 필터는 Authorization 헤더를 우선 확인하고, 없으면 해당 쿠키를 확인한다.

### 4.2 Refresh Token

- 기본 유효기간: 14일
- `sub`: 사용자 ID
- `sid`: 세션 ID(UUID)
- `type`: `refresh`
- 용도: Access Token 만료 후 토큰 쌍 재발급 및 기기별 세션 구분

Refresh Token은 API 인증에 사용하면 안 된다. JWT 필터는 `type=access`인 토큰만 SecurityContext에 인증 정보로 등록한다.

## 5. JWT 필터 동작

`JwtAuthenticationFilter`는 `OncePerRequestFilter`를 상속한다. 요청마다 한 번 실행되며 다음 순서로 동작한다.

1. `Authorization: Bearer ...` 헤더에서 토큰을 찾는다.
2. 헤더가 없으면 `loop_access_token` 쿠키에서 토큰을 찾는다.
3. JWT 서명과 만료 시각을 검증한다.
4. 토큰의 `type`이 `access`인지 확인한다.
5. 사용자 ID를 principal로 하여 Spring Security의 `SecurityContext`에 인증 정보를 저장한다.
6. 토큰이 없거나 유효하지 않으면 인증 정보를 비운 채 다음 필터로 넘긴다.

Security 설정에서 인증 관련 공개 경로를 제외한 나머지 경로는 인증이 필요하다. 따라서 보호 API는 JWT 필터가 SecurityContext에 인증 정보를 넣은 경우에만 통과한다.

## 6. Redis Refresh Token 세션 설계

각 로그인마다 새로운 세션 ID를 발급한다. 사용자당 토큰 하나만 관리하지 않으므로, 여러 브라우저나 기기에서 로그인한 상태를 구분할 수 있다.

| Redis 키 | 값 | TTL |
| --- | --- | --- |
| `auth:refresh:{userId}:{sessionId}` | Refresh Token의 SHA-256 해시 | Refresh Token과 동일한 14일 |
| `auth:refresh:user:{userId}` | 해당 사용자의 sessionId 집합(Set) | Refresh Token과 동일한 14일 |

Redis에는 Refresh Token 원문을 저장하지 않는다. Redis 데이터가 노출되어도 원문 토큰으로 즉시 인증할 수 없도록 SHA-256 해시만 저장한다.

## 7. Refresh Token Rotation과 재사용 탐지

Refresh Token은 한 번 사용할 때마다 폐기하고 새 토큰을 발급한다.

회전 과정은 Redis Lua Script로 실행한다. 기존 해시 확인, 기존 세션 삭제, 새 Refresh Token 해시 저장, 사용자 세션 Set 교체를 하나의 원자적 연산으로 묶는다. Redis는 Lua Script가 실행되는 동안 다른 명령을 끼워 넣지 않으므로 동일한 Refresh Token을 동시에 제출해도 한 요청만 회전에 성공한다.

```text
로그인
  └─ Refresh A 발급 및 Redis에 hash(A) 저장

토큰 재발급 요청(Refresh A)
  ├─ JWT 서명, 만료, type=refresh 검증
  ├─ Redis의 hash(A)와 비교
  ├─ 일치: 기존 세션 키 삭제
  └─ Access B + Refresh B 발급 및 Redis에 hash(B) 저장
```

이미 사용된 Refresh A가 다시 제출되면 Redis에 세션 키가 없거나 저장된 해시가 일치하지 않는다. 이는 토큰 탈취 후 재사용 가능성을 의미하므로 다음처럼 처리한다.

```text
Refresh Token 불일치 또는 미존재
        ↓
해당 사용자의 sessionId Set 조회
        ↓
모든 auth:refresh:{userId}:{sessionId} 삭제
        ↓
사용자 세션 Set 삭제
        ↓
RefreshTokenReuseException 발생
        ↓
모든 기기에서 재로그인 필요
```

이 정책은 정상 사용자가 불편을 겪을 수 있지만, 탈취 가능성이 확인된 상황에서 공격자가 남아 있는 다른 세션을 계속 사용하는 위험을 낮춘다.

로그인 시 Refresh 세션을 최초 저장하는 작업과 재사용 탐지 후 전체 세션을 폐기하는 작업도 각각 Lua Script로 수행한다. 세션 키와 사용자 sessionId Set 중 한쪽만 갱신되는 부분 실패를 방지하기 위한 조치다.

## 8. 로그아웃

로그아웃 요청은 Refresh Token에서 사용자 ID와 세션 ID를 읽은 뒤, 해당 세션의 Redis 키와 사용자 세션 Set의 sessionId를 Lua Script 하나에서 삭제한다. 삭제된 Refresh Token은 더 이상 재발급에 사용할 수 없다.

현재 구현에서 OAuth 로그인은 Refresh Token을 HttpOnly 쿠키로 전달한다. 향후 로그아웃 API는 요청 본문 토큰 방식과 쿠키 방식 모두를 일관되게 처리하도록 확장하는 것이 필요하다.

## 9. Google OAuth2 로그인

### 9.1 시작 및 콜백

브라우저는 다음 주소로 이동해 Google 인증을 시작한다.

```text
GET /oauth2/authorization/google
```

Google Cloud Console에는 아래 콜백 URI를 등록해야 한다.

```text
http://localhost:8080/login/oauth2/code/google
```

Google 인증과 동의가 완료되면 Spring Security가 이 콜백을 처리하고, `GoogleOAuth2SuccessHandler`를 호출한다.

### 9.2 성공 처리

1. Google 사용자 정보에서 이메일을 읽는다.
2. 이메일로 기존 사용자를 조회한다.
3. 사용자가 없으면 Google 제공자(`GOOGLE`) 사용자로 생성한다.
4. Access/Refresh Token을 발급하고 Redis에 Refresh Token 해시를 저장한다.
5. 두 토큰을 HttpOnly 쿠키로 응답에 넣는다.
6. `FRONTEND_URL`로 302 리다이렉트한다.

토큰을 쿼리 파라미터에 넣어 전달하지 않는다. URL은 브라우저 기록, 프록시 로그, 분석 도구, Referer 헤더 등에 남을 수 있기 때문이다.

## 10. HttpOnly 쿠키와 프론트 초기 진입

OAuth 성공 시 생성되는 쿠키는 다음 성질을 가진다.

- `HttpOnly`: JavaScript의 `document.cookie`로 읽을 수 없다.
- `SameSite=Lax`: 일반적인 교차 사이트 요청에서 쿠키 전송을 제한한다.
- `Path=/`: 백엔드 전체 경로에 적용한다.
- `Max-Age`: Access는 10분, Refresh는 14일로 설정한다.

프론트는 리다이렉트 후 `GET /api/auth/session`을 호출한다. 브라우저는 `credentials: include` 옵션으로 HttpOnly 쿠키를 함께 전송한다. 백엔드 JWT 필터가 Access Token을 검증하면 세션 API는 204를 응답하고, 프론트는 이를 로그인 완료 상태로 판단해 초기 지도 화면을 표시한다.

Access Token이 만료되어 세션 확인이 401이면 프론트는 `POST /api/auth/refresh/cookie`를 호출한다. 이 API는 HttpOnly Refresh Token 쿠키를 읽어 Rotation을 수행하고 새 Access/Refresh 쿠키를 응답한다. 갱신 성공 후 프론트는 다시 로그인 상태로 진입한다. Refresh Token도 만료됐거나 재사용이 탐지된 경우에만 로그인 화면으로 전환한다.

## 11. CORS 설정

로컬 프론트(`http://localhost:3000`)와 백엔드(`http://localhost:8080`)는 서로 다른 Origin이다. 백엔드는 다음 CORS 정책을 적용한다.

- 허용 Origin: `http://localhost:3000`
- 허용 메서드: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
- 허용 헤더: `Authorization`, `Content-Type`
- Credential 허용: `true`

Credential을 허용하는 CORS 응답에서 Origin을 `*`로 설정하면 안 된다. 허용 Origin은 정확한 프론트 주소로 제한해야 한다.

## 12. API 목록

| 메서드 | 경로 | 인증 필요 | 설명 |
| --- | --- | --- | --- |
| `POST` | `/api/auth/email-verifications` | 아니오 | 인증번호 발송 |
| `POST` | `/api/auth/email-verifications/confirm` | 아니오 | 인증번호 확인 |
| `POST` | `/api/auth/sign-up` | 아니오 | 회원가입 및 토큰 발급 |
| `POST` | `/api/auth/login` | 아니오 | 이메일 로그인 및 토큰 발급 |
| `POST` | `/api/auth/refresh` | 아니오 | Refresh Token 회전 |
| `POST` | `/api/auth/refresh/cookie` | 아니오 | HttpOnly Refresh Cookie 기반 토큰 회전 |
| `POST` | `/api/auth/logout` | 아니오 | 현재 Refresh 세션 폐기 |
| `GET` | `/api/auth/session` | 예 | HttpOnly Access Token 유효성 확인 |
| `GET` | `/oauth2/authorization/google` | 아니오 | Google 로그인 시작 |

## 13. 운영 전 보완 사항

- 프로덕션에서는 HTTPS를 사용하고 쿠키에 `Secure=true`를 설정한다.
- Refresh Token 갱신과 로그아웃을 HttpOnly 쿠키 기반으로 완전히 통일한다.
- 이메일 인증 요청에 이메일 단위 쿨다운 외 IP 기반 Rate Limit을 추가한다.
- 로그인 실패 횟수 제한 및 계정 잠금 정책을 추가한다.
- JWT 검증 실패, 토큰 재사용 탐지, OAuth 실패를 공통 예외 처리 형식과 보안 감사 로그로 남긴다. 토큰 값과 비밀번호는 로그에 절대 남기지 않는다.
- Google OAuth 신규 가입 시 닉네임 중복 가능성을 데이터베이스 제약조건과 재시도 로직으로 보완한다.
