package com.example.forum.Dto;

import lombok.Data;

@Data
public class CommentResponseDto {
    private String message;
    private Integer totalComments;
}