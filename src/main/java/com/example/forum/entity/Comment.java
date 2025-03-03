package com.example.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "comment_id", type = IdType.AUTO)
    private Integer commentId;

    private String commentText;

    private Integer userId;

    private LocalDateTime commentDate;

    private Integer postId;

    // 添加父评论ID字段，为空表示是一级评论
    private Integer parentId;
    
    // 添加回复目标用户ID
    private Integer replyToUserId;

    // 非数据库字段，用于前端显示
    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private Integer replyCount;

    @TableField(exist = false)
    private String replyToUsername;

}
