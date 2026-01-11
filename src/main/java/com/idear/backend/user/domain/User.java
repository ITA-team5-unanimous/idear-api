package com.idear.backend.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String providerInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(length = 500)
    private String publicKey;

    private String profileImageUrl;

    private LocalDateTime deletedAt;

    public static User of(String name, String email, String providerInfo, UserRole role){
        return User.builder()
                .name(name)
                .email(email)
                .providerInfo(providerInfo)
                .role(role)
                .build();
    }

    public String getProvider() {
        return this.providerInfo.split("_")[0];
    }

    public void updateUsername(String name){
        this.name = name;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void registerPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void deleteUser(){
        this.deletedAt = LocalDateTime.now();
    }
}
