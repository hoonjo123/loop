# V6 - 사용자 친구 관계 추가

작성일: 2026-08-14

## 변경 목적

사용자 간 양방향 친구 관계를 저장하고 마이페이지에서 친구 수와 목록을 제공한다.

## 테이블

```sql
CREATE TABLE user_friends (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_one_id BIGINT NOT NULL,
    user_two_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_friends_pair UNIQUE (user_one_id, user_two_id),
    CONSTRAINT fk_user_friends_user_one FOREIGN KEY (user_one_id) REFERENCES users(id),
    CONSTRAINT fk_user_friends_user_two FOREIGN KEY (user_two_id) REFERENCES users(id),
    CONSTRAINT ck_user_friends_distinct CHECK (user_one_id < user_two_id),
    INDEX idx_user_friends_user_two (user_two_id)
);
```

## 저장 규칙

- 두 사용자 ID 중 작은 값을 `user_one_id`, 큰 값을 `user_two_id`에 저장한다.
- Unique 제약조건으로 동일 관계의 방향 중복 저장을 방지한다.
- 자기 자신은 친구로 추가할 수 없다.
- 어느 한쪽이라도 상대를 차단했다면 친구 추가를 거부한다.
- 사용자를 차단하면 기존 친구 관계를 같은 트랜잭션에서 제거한다.

현재 로컬 환경에서는 `ddl-auto=update`가 테이블과 인덱스를 생성한다. 운영 배포 전에는 위 SQL을 Flyway 변경 세트로 전환한다.
