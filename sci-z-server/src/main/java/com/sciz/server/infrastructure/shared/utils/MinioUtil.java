package com.sciz.server.infrastructure.shared.utils;

import io.minio.*;
import io.minio.http.Method;

import java.io.InputStream;

/**
 * MinIO 工具
 * 
 * @author JiaWen.Wu
 * @className MinioUtil
 * @date 2025-10-29 17:00
 */
public final class MinioUtil {

    private MinioUtil() {
    }

    /**
     * 上传对象
     *
     * @param client      MinioClient MinIO 客户端
     * @param bucket      String 桶名
     * @param objectName  String 对象名
     * @param stream      InputStream 输入流
     * @param size        long 对象大小
     * @param contentType String 内容类型
     * @return void
     */
    public static void upload(MinioClient client, String bucket, String objectName, InputStream stream, long size,
            String contentType) throws Exception {
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(stream, size, -1)
                .contentType(contentType)
                .build();
        client.putObject(args);
    }

    /**
     * 生成预签名下载地址
     *
     * @param client        MinioClient 客户端
     * @param bucket        String 桶名
     * @param objectName    String 对象名
     * @param expirySeconds int 过期秒数
     * @return String 预签名URL
     */
    public static String presignedGetUrl(MinioClient client, String bucket, String objectName, int expirySeconds)
            throws Exception {
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .expiry(expirySeconds)
                .method(Method.GET)
                .build();
        return client.getPresignedObjectUrl(args);
    }

    /**
     * 判断桶是否存在
     *
     * @param client MinioClient 客户端
     * @param bucket String 桶名
     * @return boolean 是否存在
     */
    public static boolean bucketExists(MinioClient client, String bucket) throws Exception {
        return client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    }

    /**
     * 若桶不存在则创建
     *
     * @param client MinioClient 客户端
     * @param bucket String 桶名
     * @return void
     */
    public static void makeBucketIfAbsent(MinioClient client, String bucket) throws Exception {
        if (!bucketExists(client, bucket)) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
