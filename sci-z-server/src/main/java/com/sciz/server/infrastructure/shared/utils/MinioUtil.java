package com.sciz.server.infrastructure.shared.utils;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * MinIO 工具
 *
 * <p>
 * 对 MinIO 客户端常用操作进行封装，统一参数校验与异常抛出，便于业务代码复用。
 * </p>
 *
 * @author JiaWen.Wu
 * @className MinioUtil
 * @date 2025-10-29 17:00
 */
public final class MinioUtil {

    private static final ConcurrentMap<String, Boolean> BUCKET_CACHE = new ConcurrentHashMap<>();

    private MinioUtil() {
    }

    private static void validateClient(MinioClient client) {
        Assert.notNull(client, "MinioClient 不能为空");
    }

    private static void validateBucket(String bucket) {
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalArgumentException("bucket 名称不能为空");
        }
    }

    private static void validateObjectName(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }
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
        validateClient(client);
        validateBucket(bucket);
        validateObjectName(objectName);
        Objects.requireNonNull(stream, "文件流不能为空");
        if (size <= 0) {
            throw new IllegalArgumentException("文件大小必须大于 0");
        }
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
        validateClient(client);
        validateBucket(bucket);
        validateObjectName(objectName);
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
        validateClient(client);
        validateBucket(bucket);
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
        validateClient(client);
        validateBucket(bucket);
        if (Boolean.TRUE.equals(BUCKET_CACHE.get(bucket))) {
            return;
        }
        if (!bucketExists(client, bucket)) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        BUCKET_CACHE.put(bucket, Boolean.TRUE);
    }

    /**
     * 获取对象内容（需调用方负责关闭流）
     *
     * @param client     MinioClient 客户端
     * @param bucket     String 桶名
     * @param objectName String 对象名
     * @return GetObjectResponse 对象流
     */
    public static GetObjectResponse download(MinioClient client, String bucket, String objectName) throws Exception {
        validateClient(client);
        validateBucket(bucket);
        validateObjectName(objectName);
        return client.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
    }

    /**
     * 删除对象
     *
     * @param client     MinioClient 客户端
     * @param bucket     String 桶名
     * @param objectName String 对象名
     */
    public static void deleteObject(MinioClient client, String bucket, String objectName) throws Exception {
        validateClient(client);
        validateBucket(bucket);
        validateObjectName(objectName);
        client.removeObject(RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
    }

    /**
     * 获取对象元信息
     *
     * @param client     MinioClient 客户端
     * @param bucket     String 桶名
     * @param objectName String 对象名
     * @return StatObjectResponse 对象信息
     */
    public static StatObjectResponse statObject(MinioClient client, String bucket, String objectName)
            throws Exception {
        validateClient(client);
        validateBucket(bucket);
        validateObjectName(objectName);
        return client.statObject(StatObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
    }
}
