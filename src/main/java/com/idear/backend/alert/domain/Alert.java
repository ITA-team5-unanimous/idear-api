package com.idear.backend.alert.domain;

import com.idear.backend.idea.domain.IdeaFile;
import com.idear.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alertId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Column(length = 500)
    private String content;

    @Column(nullable = false)
    @Builder.Default()
    private Boolean isRead = false;

    @Column
    private Long ideaId; // 알림 대상 ideaId

    @Column
    private Long ideaFileId; // 알림 대상 ideaFileId

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static Alert ofRegistration(String content, IdeaFile ideaFile){
        return Alert.builder()
                .alertType(AlertType.REGISTRATION)
                .content(content)
                .ideaId(ideaFile.getIdea().getIdeaId())
                .ideaFileId(ideaFile.getIdeaFileId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Alert ofNotification(String content){
        return Alert.builder()
                .alertType(AlertType.NOTIFICATION)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void read(){
        this.isRead = true;
    }
}
