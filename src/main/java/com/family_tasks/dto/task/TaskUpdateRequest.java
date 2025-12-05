package com.family_tasks.dto.task;

import lombok.*;
import java.util.Set;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateRequest {
    private String status;
    private String name;
    private String description;
    private String priority;
    private Set<Integer> executorIds;
    private Boolean confidential;
    private Integer rewardsPoints;
    private String deadline;
}
