package com.example.lcx.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.com.lcx.jpa.entity.BaseEntity;

import java.math.BigInteger;
import java.util.List;

@NoArgsConstructor
@Data
@Entity
@Table(name = "user", schema = "lcx")
@EqualsAndHashCode(callSuper = false)
public class UserEntity extends BaseEntity {
    private static final long serialVersionUID = 3771285585431109603L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_sequence", schema = "lcx", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private BigInteger id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL})
    private List<TaskEntity> tasks;

}
