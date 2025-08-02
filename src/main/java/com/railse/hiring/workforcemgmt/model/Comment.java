package com.railse.hiring.workforcemgmt.model;

import lombok.Data;

import java.time.Instant;

@Data
public class Comment {
    private Long id;
    private Long taskId;
    private String text;
    private Long userId;
    private String username;
    private Instant timestamp;
}
