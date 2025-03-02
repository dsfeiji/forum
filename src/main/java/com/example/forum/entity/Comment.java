package com.example.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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


}
