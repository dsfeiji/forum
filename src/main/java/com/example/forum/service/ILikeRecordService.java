package com.example.forum.service;

import com.example.forum.entity.LikeRecord;
import com.example.forum.entity.Post;
import com.example.forum.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.forum.utils.Result;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-03-04
 */
public interface ILikeRecordService extends IService<LikeRecord> {
    
    /**
     * 添加点赞记录
     */
    Result<String> addLike(LikeRecord likeRecord);
    
    /**
     * 取消点赞
     */
    Result<String> removeLike(LikeRecord likeRecord);
    
    /**
     * 查询用户是否点赞了某篇文章
     */
    Result<Boolean> checkUserLiked(Integer userId, Integer postId);
    
    /**
     * 查询用户点赞的所有文章
     */
    Result<List<Post>> getLikedPostsByUserId(Integer userId, Integer pageNum, Integer pageSize);
    
    /**
     * 查询文章被哪些用户点赞
     */
    Result<List<User>> getLikedUsersByPostId(Integer postId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取文章点赞总数
     */
    Result<Integer> getPostLikeCount(Integer postId);
}
