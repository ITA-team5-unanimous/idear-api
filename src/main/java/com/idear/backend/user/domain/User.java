package com.idear.backend.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String providerInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

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

    public void deleteUser(){
        this.deletedAt = LocalDateTime.now();
    }
}
