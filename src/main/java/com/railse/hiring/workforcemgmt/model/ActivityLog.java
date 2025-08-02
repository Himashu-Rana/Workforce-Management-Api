package com.railse.hiring.workforcemgmt.model;

import lombok.Data;

import java.time.Instant;

@Data
public class ActivityLog {
    private Long id;
    private Long taskId;
    private String description;
    private Long userId;
    private String username;
    private Instant timestamp;
}
