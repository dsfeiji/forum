package com.example.forum.service;

import com.example.forum.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.forum.utils.Result;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-02-18
 */
public interface ICommentService extends IService<Comment> {
    Result<String> addComment(Comment comment);
    Result<String> deleteComment(Integer commentId, String token);
    Result<String> updateComment(Comment comment, String token);
    Result<List<Comment>> getCommentsByPostId(Integer postId, Integer pageNum, Integer pageSize);
    
    // 获取评论的所有回复
    Result<List<Comment>> getRepliesByCommentId(Integer commentId, Integer pageNum, Integer pageSize);
    
    // 添加回复
    Result<String> addReply(Comment comment, String token);
}
