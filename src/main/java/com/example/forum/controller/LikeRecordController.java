package com.example.forum.controller;

import com.example.forum.entity.LikeRecord;
import com.example.forum.entity.Post;
import com.example.forum.entity.User;
import com.example.forum.service.ILikeRecordService;
import com.example.forum.service.IUserService;
import com.example.forum.utils.JwtUtils;
import com.example.forum.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-03-04
 */
@RestController
@RequestMapping("/like-record")
public class LikeRecordController {

    @Autowired
    private ILikeRecordService likeRecordService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private IUserService userService;

    /**
     * 点赞/取消点赞切换
     */
    @PostMapping("/toggle")
    public Result<String> toggleLike(@RequestBody LikeRecord likeRecord, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            token = token.substring(7);

            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                return Result.error("Token无效");
            }

            User user = userService.getUserByEmail(email);
            if (user == null) {
                return Result.error("用户不存在");
            }

            likeRecord.setUserId(user.getUserId());
            return likeRecordService.toggleLike(likeRecord);
        } catch (Exception e) {
            return Result.error("点赞操作失败：" + e.getMessage());
        }
    }

    /**
     * 查询用户是否点赞了某篇文章
     */
    @GetMapping("/check")
    public Result<Boolean> checkUserLiked(@RequestParam Integer postId, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            token = token.substring(7);

            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                return Result.error("Token无效");
            }

            User user = userService.getUserByEmail(email);
            if (user == null) {
                return Result.error("用户不存在");
            }

            return likeRecordService.checkUserLiked(user.getUserId(), postId);
        } catch (Exception e) {
            return Result.error("查询点赞状态失败：" + e.getMessage());
        }
    }

    /**
     * 查询用户点赞的所有文章
     */
    @GetMapping("/user/{userId}")
    public Result<List<Post>> getLikedPostsByUserId(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return likeRecordService.getLikedPostsByUserId(userId, pageNum, pageSize);
    }

    /**
     * 查询文章被哪些用户点赞
     */
    @GetMapping("/post/{postId}")
    public Result<List<User>> getLikedUsersByPostId(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return likeRecordService.getLikedUsersByPostId(postId, pageNum, pageSize);
    }

    /**
     * 获取文章点赞总数
     */
    @GetMapping("/count/{postId}")
    public Result<Integer> getPostLikeCount(@PathVariable Integer postId) {
        return likeRecordService.getPostLikeCount(postId);
    }
}
