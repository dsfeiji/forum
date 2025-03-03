package com.example.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.forum.Dto.CommentResponseDto;
import com.example.forum.entity.Comment;
import com.example.forum.entity.Post;
import com.example.forum.entity.User;
import com.example.forum.mapper.CommentMapper;
import com.example.forum.mapper.PostMapper;
import com.example.forum.mapper.UserMapper;
import com.example.forum.service.ICommentService;
import com.example.forum.utils.JwtUtils;
import com.example.forum.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<CommentResponseDto> addComment(Comment comment) {
        try {
            // 检查评论内容是否为空
            if (comment.getCommentText() == null || comment.getCommentText().trim().isEmpty()) {
                return Result.error("评论内容不能为空");
            }

            // 检查评论长度
            if (comment.getCommentText().length() > 500) {
                return Result.error("评论内容不能超过500字");
            }

            // 检查文章是否存在
            Post post = postMapper.selectById(comment.getPostId());
            if (post == null) {
                return Result.error("文章不存在");
            }

            // 设置评论时间
            comment.setCommentDate(LocalDateTime.now());

            // 保存评论
            if (save(comment)) {
                // 更新文章的评论数
                post.setCommentCount(post.getCommentCount() + 1);
                postMapper.updateById(post);
                
                // 获取文章的总评论数
                LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Comment::getPostId, comment.getPostId());
                int totalComments = (int) count(queryWrapper);
                
                CommentResponseDto responseDto = new CommentResponseDto();
                responseDto.setMessage("评论发布成功");
                responseDto.setTotalComments(totalComments);
                
                return Result.success("发布成功",responseDto);
            }

            return Result.error("评论发布失败");
        } catch (Exception e) {
            log.error("发布评论失败: {}", e.getMessage());
            return Result.error("发布评论失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> deleteComment(Integer commentId, String token) {
        try {
            // 检查评论是否存在
            Comment comment = getById(commentId);
            if (comment == null) {
                return Result.error("评论不存在");
            }

            // 验证用户权限
            String email = jwtUtils.getEmailFromToken(token);
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email));
            
            if (!comment.getUserId().equals(user.getUserId())) {
                return Result.error("无权删除该评论");
            }

            // 删除评论
            if (removeById(commentId)) {
                // 更新文章评论数
                Post post = postMapper.selectById(comment.getPostId());
                post.setCommentCount(post.getCommentCount() - 1);
                postMapper.updateById(post);
                return Result.success("删除成功");
            }
            return Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除评论失败: {}", e.getMessage());
            return Result.error("删除评论失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> updateComment(Comment comment, String token) {
        try {
            // 检查评论是否存在
            Comment existingComment = getById(comment.getCommentId());
            if (existingComment == null) {
                return Result.error("评论不存在");
            }

            // 验证用户权限
            String email = jwtUtils.getEmailFromToken(token);
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email));
            
            if (!existingComment.getUserId().equals(user.getUserId())) {
                return Result.error("无权修改该评论");
            }

            // 检查评论内容
            if (comment.getCommentText() == null || comment.getCommentText().trim().isEmpty()) {
                return Result.error("评论内容不能为空");
            }

            if (comment.getCommentText().length() > 500) {
                return Result.error("评论内容不能超过500字");
            }

            // 更新评论
            existingComment.setCommentText(comment.getCommentText());
            if (updateById(existingComment)) {
                return Result.success("修改成功");
            }
            return Result.error("修改失败");
        } catch (Exception e) {
            log.error("修改评论失败: {}", e.getMessage());
            return Result.error("修改评论失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<Comment>> getCommentsByPostId(Integer postId, Integer pageNum, Integer pageSize) {
        try {
            // 检查文章是否存在
            Post post = postMapper.selectById(postId);
            if (post == null) {
                return Result.error("文章不存在");
            }

            // 分页查询评论
            Page<Comment> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Comment::getPostId, postId)
                    .isNull(Comment::getParentId)  // 只查询一级评论
                    .orderByDesc(Comment::getCommentDate);

            Page<Comment> commentPage = page(page, queryWrapper);
            List<Comment> comments = commentPage.getRecords();

            // 查询每个评论的回复数量和评论者信息
            for (Comment comment : comments) {
                // 设置评论者信息
                User user = userMapper.selectById(comment.getUserId());
                if (user != null) {
                    comment.setUsername(user.getUsername());
                }

                // 查询回复数量
                LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
                replyWrapper.eq(Comment::getParentId, comment.getCommentId());
                int replyCount = (int) count(replyWrapper);
                comment.setReplyCount(replyCount);

                // 如果是回复类型的评论，设置回复目标用户名
                if (comment.getReplyToUserId() != null) {
                    User replyToUser = userMapper.selectById(comment.getReplyToUserId());
                    if (replyToUser != null) {
                        comment.setReplyToUsername(replyToUser.getUsername());
                    }
                }
            }

            return Result.success("查询成功", comments);
        } catch (Exception e) {
            log.error("查询评论失败: {}", e.getMessage());
            return Result.error("查询评论失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<Comment>> getRepliesByCommentId(Integer commentId, Integer pageNum, Integer pageSize) {
        try {
            // 检查父评论是否存在
            Comment parentComment = getById(commentId);
            if (parentComment == null) {
                return Result.error("评论不存在");
            }

            // 分页查询回复
            Page<Comment> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Comment::getParentId, commentId)
                    .orderByAsc(Comment::getCommentDate);

            Page<Comment> commentPage = page(page, queryWrapper);
            List<Comment> replies = commentPage.getRecords();

            // 设置回复的用户信息
            for (Comment reply : replies) {
                // 设置评论者信息
                User user = userMapper.selectById(reply.getUserId());
                if (user != null) {
                    reply.setUsername(user.getUsername());
                }

                // 设置回复目标用户信息
                if (reply.getReplyToUserId() != null) {
                    User replyToUser = userMapper.selectById(reply.getReplyToUserId());
                    if (replyToUser != null) {
                        reply.setReplyToUsername(replyToUser.getUsername());
                    }
                }
            }

            return Result.success("查询成功", replies);
        } catch (Exception e) {
            log.error("查询回复失败: {}", e.getMessage());
            return Result.error("查询回复失败：" + e.getMessage());
        }
    }

    @Override
    public Result<CommentResponseDto> addReply(Comment comment, String token) {
        try {
            // 检查回复内容
            if (comment.getCommentText() == null || comment.getCommentText().trim().isEmpty()) {
                return Result.error("回复内容不能为空");
            }

            if (comment.getCommentText().length() > 500) {
                return Result.error("回复内容不能超过500字");
            }

            // 检查父评论是否存在
            Comment parentComment = getById(comment.getParentId());
            if (parentComment == null) {
                return Result.error("要回复的评论不存在");
            }

            // 设置文章ID
            comment.setPostId(parentComment.getPostId());
            
            // 验证用户权限并设置用户ID
            String email = jwtUtils.getEmailFromToken(token);
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email));
            if (user == null) {
                return Result.error("用户不存在");
            }
            comment.setUserId(user.getUserId());
            
            // 设置回复目标用户ID
            comment.setReplyToUserId(parentComment.getUserId());

            // 设置评论时间
            comment.setCommentDate(LocalDateTime.now());

            // 保存回复
            if (save(comment)) {
                // 更新文章的评论数
                Post post = postMapper.selectById(comment.getPostId());
                post.setCommentCount(post.getCommentCount() + 1);
                postMapper.updateById(post);

                // 获取文章的总评论数
                LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Comment::getPostId, comment.getPostId());
                int totalComments = (int) count(queryWrapper);
                
                CommentResponseDto responseDto = new CommentResponseDto();
                responseDto.setMessage("回复成功");
                responseDto.setTotalComments(totalComments);
                
                return Result.success("回复成功",responseDto);
            }

            return Result.error("回复失败");
        } catch (Exception e) {
            log.error("发布回复失败: {}", e.getMessage());
            return Result.error("发布回复失败：" + e.getMessage());
        }
    }
}
