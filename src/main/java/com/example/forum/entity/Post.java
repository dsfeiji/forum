package com.example.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author DSfeiji
 * @since 2025-02-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "post_id", type = IdType.AUTO)
    private Integer postId;

    @TableField(value = "user_id")
    private Integer userId;

    private String postTitle;
    private String postText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(value = "view_count", fill = FieldFill.INSERT)
    private Integer viewCount = 0;

    @TableField(value = "like_count", fill = FieldFill.INSERT)
    private Integer likeCount = 0;

    @TableField(value = "comment_count", fill = FieldFill.INSERT)
    private Integer commentCount = 0;

    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean isDeleted = false;

}
