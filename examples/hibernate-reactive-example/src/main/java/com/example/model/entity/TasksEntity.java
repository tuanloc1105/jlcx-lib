package com.example.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import vn.com.lcx.common.utils.DateTimeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tasks", schema = "todo")
public class TasksEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_seq")
    @SequenceGenerator(name = "tasks_seq", sequenceName = "tasks_seq", schema = "todo", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "task_title", nullable = false)
    private String taskTitle;

    @Column(name = "task_detail")
    @ColumnDefault("''")
    private String taskDetail;

    @ColumnDefault("false")
    private Boolean finished;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private UsersEntity user;

    @PrePersist
    public void prePersis() {
        final var currentTime = DateTimeUtils.generateCurrentTimeDefault();
        createdAt = currentTime;
        updatedAt = currentTime;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = DateTimeUtils.generateCurrentTimeDefault();
    }

}
