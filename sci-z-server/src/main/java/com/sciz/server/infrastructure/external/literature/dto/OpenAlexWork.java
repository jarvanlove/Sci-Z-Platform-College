package com.sciz.server.infrastructure.external.literature.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * OpenAlex Work 对象
 *
 * @author JiaWen.Wu
 * @className OpenAlexWork
 * @date 2025-01-24 14:30
 */
@Data
public class OpenAlexWork {
    /**
     * 文献ID
     */
    private String id;

    /**
     * DOI
     */
    private String doi;

    /**
     * 标题
     */
    private String title;

    /**
     * 显示名称
     */
    @JsonProperty("display_name")
    private String displayName;

    /**
     * 相关性分数
     */
    @JsonProperty("relevance_score")
    private Double relevanceScore;

    /**
     * 发表年份
     */
    @JsonProperty("publication_year")
    private Integer publicationYear;

    /**
     * 发表日期
     */
    @JsonProperty("publication_date")
    private String publicationDate;

    /**
     * 语言
     */
    private String language;

    /**
     * 类型
     */
    private String type;

    /**
     * 主要位置
     */
    @JsonProperty("primary_location")
    private PrimaryLocation primaryLocation;

    /**
     * 开放获取信息
     */
    @JsonProperty("open_access")
    private OpenAccess openAccess;

    /**
     * 作者信息列表
     */
    private List<Authorship> authorships;

    /**
     * 引用次数
     */
    @JsonProperty("cited_by_count")
    private Integer citedByCount;

    /**
     * 引用标准化百分位
     */
    @JsonProperty("citation_normalized_percentile")
    private Object citationNormalizedPercentile;

    /**
     * 摘要倒排索引
     */
    @JsonProperty("abstract_inverted_index")
    private Map<String, List<Integer>> abstractInvertedIndex;

    /**
     * 主要主题
     */
    @JsonProperty("primary_topic")
    private Topic primaryTopic;

    /**
     * 主题列表
     */
    private List<Topic> topics;

    /**
     * 关键词列表
     */
    private List<Keyword> keywords;

    /**
     * 按年份的引用次数
     */
    @JsonProperty("counts_by_year")
    private List<CountByYear> countsByYear;

    /**
     * 相关文献ID列表
     */
    @JsonProperty("related_works")
    private List<String> relatedWorks;

    /**
     * 引用文献ID列表
     */
    @JsonProperty("referenced_works")
    private List<String> referencedWorks;

    /**
     * 引用文献数量
     */
    @JsonProperty("referenced_works_count")
    private Integer referencedWorksCount;

    /**
     * 机构数量
     */
    @JsonProperty("institutions_distinct_count")
    private Integer institutionsDistinctCount;

    /**
     * 国家数量
     */
    @JsonProperty("countries_distinct_count")
    private Integer countriesDistinctCount;

    /**
     * 机构列表
     */
    private List<Institution> institutions;

    /**
     * 主要位置
     */
    @Data
    public static class PrimaryLocation {
        /**
         * 位置ID
         */
        private String id;

        /**
         * 是否开放获取
         */
        @JsonProperty("is_oa")
        private Boolean isOa;

        /**
         * 落地页URL
         */
        @JsonProperty("landing_page_url")
        private String landingPageUrl;

        /**
         * PDF URL
         */
        @JsonProperty("pdf_url")
        private String pdfUrl;

        /**
         * 来源
         */
        private Source source;
    }

    /**
     * 来源
     */
    @Data
    public static class Source {
        /**
         * 来源ID
         */
        private String id;

        /**
         * 显示名称
         */
        @JsonProperty("display_name")
        private String displayName;

        /**
         * ISSN-L
         */
        @JsonProperty("issn_l")
        private String issnL;

        /**
         * ISSN列表
         */
        private List<String> issn;

        /**
         * 类型
         */
        private String type;
    }

    /**
     * 开放获取信息
     */
    @Data
    public static class OpenAccess {
        /**
         * 是否开放获取
         */
        @JsonProperty("is_oa")
        private Boolean isOa;

        /**
         * OA状态
         */
        @JsonProperty("oa_status")
        private String oaStatus;

        /**
         * OA URL
         */
        @JsonProperty("oa_url")
        private String oaUrl;
    }

    /**
     * 作者信息
     */
    @Data
    public static class Authorship {
        /**
         * 作者位置
         */
        @JsonProperty("author_position")
        private String authorPosition;

        /**
         * 作者
         */
        private Author author;

        /**
         * 是否通讯作者
         */
        @JsonProperty("is_corresponding")
        private Boolean isCorresponding;

        /**
         * 原始作者名称
         */
        @JsonProperty("raw_author_name")
        private String rawAuthorName;
    }

    /**
     * 作者
     */
    @Data
    public static class Author {
        /**
         * 作者ID
         */
        private String id;

        /**
         * 显示名称
         */
        @JsonProperty("display_name")
        private String displayName;
    }

    /**
     * 主题
     */
    @Data
    public static class Topic {
        /**
         * 主题ID
         */
        private String id;

        /**
         * 显示名称
         */
        @JsonProperty("display_name")
        private String displayName;

        /**
         * 分数
         */
        private Double score;

        /**
         * 子领域
         */
        private Subfield subfield;

        /**
         * 领域
         */
        private Field field;

        /**
         * 域
         */
        private Domain domain;
    }

    /**
     * 子领域
     */
    @Data
    public static class Subfield {
        /**
         * 子领域ID
         */
        private String id;

        /**
         * 显示名称
         */
        @JsonProperty("display_name")
        private String displayName;
    }

    /**
     * 领域
     */
    @Data
    public static class Field {
        /**
         * 领域ID
         */
        private String id;

        /**
         * 显示名称
         */
        @JsonProperty("display_name")
        private String displayName;
    }

    /**
     * 域
     */
    @Data
    public static class Domain {
        /**
         * 域ID
         */
        private String id;

        /**
         * 显示名称
         */
        @JsonProperty("display_name")
        private String displayName;
    }

    /**
     * 关键词
     */
    @Data
    public static class Keyword {
        /**
         * 关键词ID
         */
        private String id;

        /**
         * 显示名称
         */
        @JsonProperty("display_name")
        private String displayName;

        /**
         * 分数
         */
        private Double score;
    }

    /**
     * 按年份的引用次数
     */
    @Data
    public static class CountByYear {
        /**
         * 年份
         */
        private Integer year;

        /**
         * 引用次数
         */
        @JsonProperty("cited_by_count")
        private Integer citedByCount;
    }

    /**
     * 机构
     */
    @Data
    public static class Institution {
        /**
         * 机构ID
         */
        private String id;

        /**
         * 显示名称
         */
        @JsonProperty("display_name")
        private String displayName;

        /**
         * ROR ID
         */
        private String ror;

        /**
         * 国家代码
         */
        @JsonProperty("country_code")
        private String countryCode;

        /**
         * 类型
         */
        private String type;
    }
}

