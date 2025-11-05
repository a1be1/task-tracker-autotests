package com.family_tasks.dto.user;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String name;
    private Boolean admin;
}