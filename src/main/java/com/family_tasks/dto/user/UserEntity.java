package com.family_tasks.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    private int id;
    private String name;
    private Boolean admin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}