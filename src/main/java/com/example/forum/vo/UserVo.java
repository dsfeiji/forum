package com.example.forum.vo;

public class UserVo {
    private Integer userId;
    private String username;
    private String email;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @Override
    public String toString() {
        return "UserInfoVo{" +
                "id=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

