package org.example.casemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDependencyDTO {
    private Long taskId;
    private Long dependentTaskId;
    private String taskTitle;
    private String dependentTaskTitle;
    private String taskStatus;
    private String dependentTaskStatus;
} 