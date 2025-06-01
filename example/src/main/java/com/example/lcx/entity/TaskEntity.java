package com.example.lcx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import vn.com.lcx.jpa.entity.BaseEntity;

import java.math.BigInteger;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "task", schema = "lcx")
@EqualsAndHashCode(callSuper = false)
public class TaskEntity extends BaseEntity {
    private static final long serialVersionUID = 782212564767904813L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq")
    @SequenceGenerator(name = "task_seq", sequenceName = "task_sequence", schema = "lcx", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private BigInteger id;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "task_detail")
    @ColumnDefault("''")
    private String taskDetail;

    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @Column(name = "finished")
    @ColumnDefault("false")
    private Boolean finished;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;

}
