package com.sciz.server.domain.pojo.dto.response.practice;

/**
 * 产教研智能体 - 项目负责人项（姓名、学院、职务）
 *
 * @param realName 真实姓名
 * @param college  学院/部门名称
 * @param position 职务
 */
public record ProjectLeaderResp(
        String realName,
        String college,
        String position) {
}
