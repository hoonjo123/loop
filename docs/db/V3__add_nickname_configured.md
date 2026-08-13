# V3 Add Nickname Configured

작성일: 2026-08-13

Google OAuth2 최초 가입 사용자가 로그인 이후 직접 닉네임을 설정하도록 `users` 테이블에 온보딩 상태를 추가한다.

## Change

- `users.nickname_configured` 추가
  - `true`: 사용자가 닉네임 설정을 완료함
  - `false`: Google 최초 가입 후 닉네임 설정이 필요함
- 기존 `LOCAL` 사용자는 이미 가입 시 닉네임 중복확인을 완료했으므로 `true`로 이관한다.
- 기존 `GOOGLE` 사용자는 다음 로그인 시 닉네임 설정 화면을 거치도록 `false`로 이관한다.

```sql
ALTER TABLE users
    ADD COLUMN nickname_configured BIT NOT NULL DEFAULT 0 AFTER nickname;

UPDATE users
SET nickname_configured = 1
WHERE auth_provider = 'LOCAL';
```

Google 신규 사용자는 Unique 제약조건을 충족하기 위한 내부 임시 닉네임으로 생성된다. 실제 서비스 화면 진입 전 `/api/users/me/nickname`에서 중복검사를 다시 수행하고 사용자가 선택한 닉네임으로 교체한다.
