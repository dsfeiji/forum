package com.example.forum.service;

import com.example.forum.Dto.CommentResponseDto;
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
    Result<CommentResponseDto> addComment(Comment comment);
    Result<String> deleteComment(Integer commentId, String token);
    Result<String> updateComment(Comment comment, String token);
    Result<List<Comment>> getCommentsByPostId(Integer postId, Integer pageNum, Integer pageSize);
    Result<List<Comment>> getRepliesByCommentId(Integer commentId, Integer pageNum, Integer pageSize);
    Result<CommentResponseDto> addReply(Comment comment, String token);
}
