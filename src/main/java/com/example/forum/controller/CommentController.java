package com.example.forum.controller;

import com.example.forum.Dto.CommentResponseDto;
import com.example.forum.entity.Comment;
import com.example.forum.entity.User;
import com.example.forum.service.ICommentService;
import com.example.forum.service.IUserService;
import com.example.forum.utils.JwtUtils;
import com.example.forum.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comment")
public class CommentController {
    
    @Autowired
    private ICommentService commentService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private IUserService userService;

    @PostMapping("/add")
    public Result<CommentResponseDto> addComment(@RequestBody Comment comment, HttpServletRequest request) {
        try {
            // 从请求头获取 Token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            token = token.substring(7);

            // 从 Token 获取邮箱
            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                return Result.error("Token无效");
            }

            // 通过邮箱查询用户
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return Result.error("用户不存在");
            }

            comment.setUserId(user.getUserId());
            return commentService.addComment(comment);
        } catch (Exception e) {
            log.error("发布评论失败: {}", e.getMessage());
            return Result.error("发布评论失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{commentId}")
    public Result<String> deleteComment(@PathVariable Integer commentId, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            token = token.substring(7);
            
            return commentService.deleteComment(commentId, token);
        } catch (Exception e) {
            log.error("删除评论失败: {}", e.getMessage());
            return Result.error("删除评论失败：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<String> updateComment(@RequestBody Comment comment, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            token = token.substring(7);
            
            return commentService.updateComment(comment, token);
        } catch (Exception e) {
            log.error("修改评论失败: {}", e.getMessage());
            return Result.error("修改评论失败：" + e.getMessage());
        }
    }

    @GetMapping("/list/{postId}")
    public Result<List<Comment>> getCommentsByPostId(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return commentService.getCommentsByPostId(postId, pageNum, pageSize);
    }

    @PostMapping("/reply")
    public Result<CommentResponseDto> addReply(@RequestBody Comment comment, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            token = token.substring(7);
            
            // 从 Token 获取邮箱
            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                return Result.error("Token无效");
            }

            // 通过邮箱查询用户
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return Result.error("用户不存在");
            }

            comment.setUserId(user.getUserId());
            return commentService.addReply(comment, token);
        } catch (Exception e) {
            log.error("发布回复失败: {}", e.getMessage());
            return Result.error("发布回复失败：" + e.getMessage());
        }
    }

    @GetMapping("/replies/{commentId}")
    public Result<List<Comment>> getReplies(
            @PathVariable Integer commentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return commentService.getRepliesByCommentId(commentId, pageNum, pageSize);
    }
}
