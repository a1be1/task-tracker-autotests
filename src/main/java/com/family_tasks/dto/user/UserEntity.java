package com.family_tasks.dto.user;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    private int id;
    private String name;
    private Boolean admin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}