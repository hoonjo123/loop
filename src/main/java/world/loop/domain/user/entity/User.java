package world.loop.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import world.loop.common.BaseTimeEntity;
import world.loop.domain.report.entity.Report;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 12)
    private String nickname;

    @Column(name = "nickname_configured", nullable = false)
    private boolean nicknameConfigured;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(length = 100)
    private String introduction;

    @Column(name = "activity_area", length = 100)
    private String activityArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @OneToMany(mappedBy = "reporter")
    private List<Report> submittedReports = new ArrayList<>();

    @OneToMany(mappedBy = "reportedUser")
    private List<Report> receivedReports = new ArrayList<>();

    @Builder
    private User(
            String email,
            String passwordHash,
            String nickname,
            boolean nicknameConfigured,
            AuthProvider authProvider
    ) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.nicknameConfigured = nicknameConfigured;
        this.authProvider = authProvider;
    }

    public void configureNickname(String nickname) {
        this.nickname = nickname;
        this.nicknameConfigured = true;
    }

    public void updateProfile(
            String nickname,
            String introduction,
            String activityArea,
            String profileImageUrl
    ) {
        this.nickname = nickname;
        this.introduction = introduction;
        this.activityArea = activityArea;
        this.profileImageUrl = profileImageUrl;
    }
}
