package com.example.forum.Dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PostPublishDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String postTitle;
    private String postText;
    private List<String> contentImages; // 文章内容图片路径集合
    private List<String> coverImages;   // 封面图片路径集合
}