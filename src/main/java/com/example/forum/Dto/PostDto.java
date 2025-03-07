package com.example.forum.Dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PostDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer postId;
    private String postTitle;
    private String postText;
    private String jwt;
    private Integer userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String coverImage; // 添加封面图片URL字段
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private String authorName; // 作者名称
}
