package com.sciz.server.infrastructure.shared.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * 验证码工具类
 * 基于 Java 21 AWT 生成图形验证码
 *
 * @author JiaWen.Wu
 * @className CaptchaUtil
 * @date 2025-11-07 19:00
 */
public final class CaptchaUtil {

    /**
     * 验证码字符集（排除易混淆字符：0, O, 1, I, l）
     */
    private static final String CHAR_SET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";

    /**
     * 默认验证码长度
     */
    private static final int DEFAULT_LENGTH = 4;

    /**
     * 默认图片宽度
     */
    private static final int DEFAULT_WIDTH = 120;

    /**
     * 默认图片高度
     */
    private static final int DEFAULT_HEIGHT = 40;

    private static final Random RANDOM = new Random();

    /**
     * 私有构造方法，防止实例化
     */
    private CaptchaUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 生成验证码文本
     *
     * @return String 验证码文本（4位）
     */
    public static String generateText() {
        return generateText(DEFAULT_LENGTH);
    }

    /**
     * 生成验证码文本
     *
     * @param length int 验证码长度
     * @return String 验证码文本
     */
    public static String generateText(int length) {
        return IntStream.range(0, length)
                .mapToObj(i -> {
                    var index = RANDOM.nextInt(CHAR_SET.length());
                    return String.valueOf(CHAR_SET.charAt(index));
                })
                .collect(java.util.stream.Collectors.joining());
    }

    /**
     * 生成验证码图片（Base64）
     *
     * @param text String 验证码文本
     * @return String Base64 编码的图片（data:image/png;base64,xxx）
     */
    public static String generateImage(String text) {
        return generateImage(text, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成验证码图片（Base64）
     *
     * @param text   String 验证码文本
     * @param width  int 图片宽度
     * @param height int 图片高度
     * @return String Base64 编码的图片
     */
    public static String generateImage(String text, int width, int height) {
        try {
            // 1. 创建图片
            var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var g = image.createGraphics();

            // 2. 设置背景色
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            // 3. 绘制边框
            g.setColor(Color.GRAY);
            g.drawRect(0, 0, width - 1, height - 1);

            // 4. 绘制干扰线
            drawInterferenceLines(g, width, height);

            // 5. 绘制验证码文本
            drawText(g, text, width, height);

            // 6. 释放资源
            g.dispose();

            // 7. 转换为 Base64
            return imageToBase64(image);
        } catch (Exception e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    /**
     * 绘制干扰线
     */
    private static void drawInterferenceLines(Graphics2D g, int width, int height) {
        g.setColor(Color.LIGHT_GRAY);
        IntStream.range(0, 5)
                .forEach(i -> {
                    var x1 = RANDOM.nextInt(width);
                    var y1 = RANDOM.nextInt(height);
                    var x2 = RANDOM.nextInt(width);
                    var y2 = RANDOM.nextInt(height);
                    g.drawLine(x1, y1, x2, y2);
                });
    }

    /**
     * 绘制验证码文本
     */
    private static void drawText(Graphics2D g, String text, int width, int height) {
        g.setFont(new Font("Arial", Font.BOLD, 28));
        var charWidth = width / text.length();

        IntStream.range(0, text.length())
                .forEach(index -> {
                    // 随机颜色
                    g.setColor(new Color(RANDOM.nextInt(100), RANDOM.nextInt(100), RANDOM.nextInt(100)));

                    // 随机旋转角度（-15° ~ 15°）
                    var angle = (RANDOM.nextDouble() - 0.5) * 30;
                    g.rotate(Math.toRadians(angle), charWidth * index + charWidth / 2, height / 2);

                    // 绘制字符
                    g.drawString(String.valueOf(text.charAt(index)), charWidth * index + 10, height / 2 + 10);

                    // 恢复旋转
                    g.rotate(-Math.toRadians(angle), charWidth * index + charWidth / 2, height / 2);
                });
    }

    /**
     * 图片转 Base64
     */
    private static String imageToBase64(BufferedImage image) {
        try (var baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            throw new RuntimeException("图片转 Base64 失败", e);
        }
    }

    /**
     * 验证码文本对比（忽略大小写）
     *
     * @param userInput String 用户输入
     * @param actual    String 实际验证码
     * @return boolean 是否匹配
     */
    public static boolean verify(String userInput, String actual) {
        return Optional.ofNullable(userInput)
                .map(String::trim)
                .flatMap(input -> Optional.ofNullable(actual)
                        .map(String::trim)
                        .map(input::equalsIgnoreCase))
                .orElse(false);
    }
}
