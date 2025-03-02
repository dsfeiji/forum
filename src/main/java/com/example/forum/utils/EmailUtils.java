package com.example.forum.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class EmailUtils {

    @Autowired
    private JavaMailSender mailSender;

    // 存储所有验证码的变量，Key 为邮箱，Value 为验证码和过期时间
    private static final Map<String, VerificationCodeInfo> verificationCodes = new ConcurrentHashMap<>();

    // 从配置文件中读取发件人地址
    @Value("${spring.mail.username}")
    private String fromEmail;

    // 定时任务线程池，用于清理过期的验证码
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public EmailUtils() {
        // 启动定时任务，每 1 分钟清理一次过期的验证码
        scheduler.scheduleAtFixedRate(this::cleanExpiredCodes, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 发送验证码邮件
     *
     * @param email 收件人邮箱
     * @return 是否发送成功（如果已经存在未过期的验证码，则返回 false）
     */
    public String sendVerificationCode(String email) {
        // 检查是否已经存在未过期的验证码
        if (hasActiveVerificationCode(email)) {
            System.out.println("请不要重复发送验证码");
            return "发送失败，请不要重复发送"; // 如果存在未过期的验证码，直接返回 false
        }

        // 生成验证码
        String verificationCode = generateVerificationCode();

        try {
            // 创建 MimeMessage 对象
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // 设置发件人、收件人、主题
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("牛马系统验证码来咯！！！");

            // 构建 HTML 内容
            String htmlContent = "<html>"
                    + "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;'>"
                    + "<div style='background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);'>"
                    + "<h2 style='color: #4CAF50;'>牛马系统验证码</h2>"
                    + "<p>你的验证码是：<strong style='color: #FF5722; font-size: 24px;'>" + verificationCode + "</strong></p>"
                    + "<p>请在 5 分钟内使用该验证码完成验证。</p>"
                    + "<p style='font-size: 12px; color: #888;'>此为系统邮件，请勿回复。</p>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            // 设置邮件内容为 HTML
            helper.setText(htmlContent, true);

            // 发送邮件
            mailSender.send(mimeMessage);

            // 存储验证码，设置过期时间为 5 分钟
            storeVerificationCode(email, verificationCode, 5);

            return "发送成功"; // 发送成功
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * 验证验证码是否正确
     *
     * @param email 用户邮箱
     * @param code  用户输入的验证码
     * @return 如果验证码正确且未过期，返回 true；否则返回 false
     */
    public static boolean validateVerificationCode(String email, String code) {
        // 获取存储的验证码信息
        VerificationCodeInfo info = verificationCodes.get(email);

        // 检查验证码是否存在且未过期
        if (info != null && info.getExpirationTime() > System.currentTimeMillis()) {
            // 检查用户输入的验证码是否与存储的验证码匹配
            return info.getCode().equals(code);
        }

        // 验证码不存在或已过期
        return false;
    }

    /**
     * 生成随机验证码
     *
     * @return 6 位随机验证码
     */
    private String generateVerificationCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }

    /**
     * 存储验证码
     *
     * @param email            邮箱
     * @param code             验证码
     * @param expirationMinutes 过期时间（分钟）
     */
    private void storeVerificationCode(String email, String code, int expirationMinutes) {
        long expirationTime = System.currentTimeMillis() + expirationMinutes * 60 * 1000;
        verificationCodes.put(email, new VerificationCodeInfo(code, expirationTime));
    }

    /**
     * 检查是否已经存在未过期的验证码
     *
     * @param email 邮箱
     * @return 如果存在未过期的验证码，返回 true；否则返回 false
     */
    private boolean hasActiveVerificationCode(String email) {
        VerificationCodeInfo info = verificationCodes.get(email);
        return info != null && info.getExpirationTime() > System.currentTimeMillis();
    }

    /**
     * 清理过期的验证码
     */
    private void cleanExpiredCodes() {
        long currentTime = System.currentTimeMillis();
        verificationCodes.entrySet().removeIf(entry -> entry.getValue().getExpirationTime() <= currentTime);
    }

    /**
     * 内部类，用于存储验证码和过期时间
     */
    private static class VerificationCodeInfo {
        private final String code;
        private final long expirationTime;

        public VerificationCodeInfo(String code, long expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
        }

        public String getCode() {
            return code;
        }

        public long getExpirationTime() {
            return expirationTime;
        }
    }
}