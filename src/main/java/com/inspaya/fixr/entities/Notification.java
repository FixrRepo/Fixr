package com.inspaya.fixr.entities;

import com.inspaya.fixr.dtos.NotificationPayload;
import com.inspaya.fixr.enums.NotificationEvents;
import com.inspaya.fixr.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String notificationId;

    @ManyToOne
    private FixrUser fixrUser;

    private NotificationEvents event;
    private NotificationType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private NotificationPayload payload;

    private boolean isRead;

    private ZonedDateTime createdAt;
    private ZonedDateTime sentAt;

}
