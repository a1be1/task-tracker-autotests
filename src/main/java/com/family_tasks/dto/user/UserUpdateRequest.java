package com.family_tasks.dto.user;

import lombok.*;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String name;
    private Boolean admin;
    private Integer groupId;
}
