package com.example.forum.Dto;


public class UserDto {
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserInfoDto{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", verificationCode='" + verificationCode + '\'' +
                '}';
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    /**
     * 用户名
     */
    private String username;
    /**
     * 用户ID
     */

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;
    /**
     * 验证码
     */
    private String verificationCode;

    public String getUserRole() {
        return userRole;
    }


    public void setUserRole(String userrole) {
        this.userRole = userrole;
    }

    private String userRole;
}
