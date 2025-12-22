package com.sciz.server.domain.pojo.dto.response.literature;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 文献详情响应
 *
 * @author JiaWen.Wu
 * @className LiteratureDetailResp
 * @date 2025-01-24 14:30
 */
@Data
@Builder
public class LiteratureDetailResp {
    /**
     * 文献ID
     */
    private String id;

    /**
     * 标题
     */
    private String title;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * DOI
     */
    private String doi;

    /**
     * 发表年份
     */
    private Integer publicationYear;

    /**
     * 发表日期
     */
    private String publicationDate;

    /**
     * 引用次数
     */
    private Integer citedByCount;

    /**
     * 相关性分数
     */
    private Double relevanceScore;

    /**
     * 类型
     */
    private String type;

    /**
     * 语言
     */
    private String language;

    /**
     * 数据源
     */
    private String dataSource;

    /**
     * 应用URL
     */
    private String applicationUrl;

    /**
     * 摘要
     */
    private String abstractText;

    /**
     * 落地页URL
     */
    private String landingPageUrl;

    /**
     * PDF URL
     */
    private String pdfUrl;

    /**
     * 来源信息
     */
    private LiteratureSearchResp.SourceInfo source;

    /**
     * 作者列表
     */
    private List<LiteratureSearchResp.AuthorInfo> authors;

    /**
     * 关键词列表
     */
    private List<KeywordInfo> keywords;

    /**
     * 主题列表
     */
    private List<TopicInfo> topics;

    /**
     * 相关文献ID列表
     */
    private List<String> relatedWorks;

    /**
     * 引用文献ID列表
     */
    private List<String> referencedWorks;

    /**
     * 引用文献数量
     */
    private Integer referencedWorksCount;

    /**
     * 按年份的引用次数
     */
    private List<CitationByYear> citationByYear;

    /**
     * 引用百分位
     */
    private Double citationPercentile;

    /**
     * 机构数量
     */
    private Integer institutionsCount;

    /**
     * 国家数量
     */
    private Integer countriesCount;

    /**
     * 机构列表
     */
    private List<InstitutionInfo> institutions;

    /**
     * 关键词信息
     */
    @Data
    @Builder
    public static class KeywordInfo {
        /**
         * 关键词ID
         */
        private String id;

        /**
         * 关键词名称
         */
        private String name;

        /**
         * 分数
         */
        private Double score;
    }

    /**
     * 主题信息
     */
    @Data
    @Builder
    public static class TopicInfo {
        /**
         * 主题ID
         */
        private String id;

        /**
         * 主题名称
         */
        private String name;

        /**
         * 分数
         */
        private Double score;

        /**
         * 子领域
         */
        private String subfield;

        /**
         * 领域
         */
        private String field;

        /**
         * 域
         */
        private String domain;
    }

    /**
     * 按年份的引用次数
     */
    @Data
    @Builder
    public static class CitationByYear {
        /**
         * 年份
         */
        private Integer year;

        /**
         * 引用次数
         */
        private Integer citedByCount;
    }

    /**
     * 机构信息
     */
    @Data
    @Builder
    public static class InstitutionInfo {
        /**
         * 机构ID
         */
        private String id;

        /**
         * 机构名称
         */
        private String name;

        /**
         * ROR ID
         */
        private String ror;

        /**
         * 国家代码
         */
        private String countryCode;

        /**
         * 类型
         */
        private String type;
    }
}

