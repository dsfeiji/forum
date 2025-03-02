package com.example.forum.service;

import com.example.forum.Dto.PostDto;
import com.example.forum.entity.Post;
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
public interface IPostService extends IService<Post> {
    Result<String> release(PostDto postDto, String token);
    Result<String> delete(PostDto postDto);
    Result<List<PostDto>> find(Post post);
    Result<String> revise(PostDto postDto);
    Result<PostDto> getPostDetail(Integer postId);
    Result<List<PostDto>> getPostList(Integer pageNum, Integer pageSize);
    Result<String> likePost(Integer postId, String token);
    Result<List<PostDto>> getPostMind(Integer pageNum, Integer pageSize,Integer userId);
}
