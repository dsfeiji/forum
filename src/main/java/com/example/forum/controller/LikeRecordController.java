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

    // 删除这两个错误的注入
    // @Autowired
    // private Post post;
    // 
    // @Autowired
    // private IPostService postService;

    /**
     * 点赞文章
     */
    @PostMapping("/like")
    public Result<String> addLike(@RequestBody LikeRecord likeRecord, HttpServletRequest request) {
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
            return likeRecordService.addLike(likeRecord);
        } catch (Exception e) {
            return Result.error("点赞失败：" + e.getMessage());
        }
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/unlike")
    public Result<String> removeLike(@RequestBody LikeRecord likeRecord, HttpServletRequest request) {
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
            
            // 删除这段错误代码
            // Integer currentLikeCount = post.getLikeCount();
            // if (currentLikeCount != null && currentLikeCount > 0) {
            // post.setLikeCount(currentLikeCount - 1);
            // postMapper.updateById(post);
            // }

            likeRecord.setUserId(user.getUserId());
            return likeRecordService.removeLike(likeRecord);
        } catch (Exception e) {
            return Result.error("取消点赞失败：" + e.getMessage());
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
