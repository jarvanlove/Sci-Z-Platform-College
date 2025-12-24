package com.sciz.server.domain.pojo.dto.response.literature;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 文献搜索响应
 *
 * @author JiaWen.Wu
 * @className LiteratureSearchResp
 * @date 2025-01-24 14:30
 */
@Data
@Builder
public class LiteratureSearchResp {
    /**
     * 论文基本信息
     */
    private PaperInfo paperInfo;

    /**
     * 来源信息
     */
    private SourceInfo sourceInfo;

    /**
     * 作者信息列表
     */
    private List<AuthorInfo> authorsInfo;

    /**
     * 影响指标
     */
    private ImpactMetrics impactMetrics;

    /**
     * 分类信息
     */
    private Taxonomy taxonomy;

    /**
     * 访问信息
     */
    private AccessInfo accessInfo;

    /**
     * 搜索元数据
     */
    private SearchMeta searchMeta;

    /**
     * 论文基本信息
     */
    @Data
    @Builder
    public static class PaperInfo {
        /**
         * 文献ID（短ID）
         */
        private String id;

        /**
         * 全局ID（完整URL）
         */
        private String globalId;

        /**
         * DOI
         */
        private String doi;

        /**
         * 标题
         */
        private String title;

        /**
         * 标题（中文翻译）
         */
        private String titleTranslated;

        /**
         * 发表年份
         */
        private Integer publicationYear;

        /**
         * 发表日期
         */
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
         * 摘要
         */
        private String abstractText;

        /**
         * 摘要（中文翻译）
         */
        private String abstractTranslated;
    }

    /**
     * 来源信息
     */
    @Data
    @Builder
    public static class SourceInfo {
        /**
         * 期刊名称
         */
        private String journalName;

        /**
         * ISSN
         */
        private String issn;

        /**
         * 类型
         */
        private String type;

        /**
         * 出版商
         */
        private String publisher;
    }

    /**
     * 作者信息
     */
    @Data
    @Builder
    public static class AuthorInfo {
        /**
         * 作者ID
         */
        private String id;

        /**
         * 作者姓名
         */
        private String name;

        /**
         * 是否通讯作者
         */
        private Boolean isCorresponding;

        /**
         * 作者位置
         */
        private String position;
    }

    /**
     * 影响指标
     */
    @Data
    @Builder
    public static class ImpactMetrics {
        /**
         * 引用次数
         */
        private Integer citationCount;

        /**
         * FWCI分数
         */
        private Double fwciScore;

        /**
         * 是否前1%
         */
        private Boolean isTop1Percent;

        /**
         * 是否高被引
         */
        private Boolean isHighlyCited;

        /**
         * 按年份的引用次数
         */
        private List<YearlyCitation> yearlyCitations;
    }

    /**
     * 按年份的引用次数
     */
    @Data
    @Builder
    public static class YearlyCitation {
        /**
         * 年份
         */
        private Integer year;

        /**
         * 引用次数
         */
        private Integer count;
    }

    /**
     * 分类信息
     */
    @Data
    @Builder
    public static class Taxonomy {
        /**
         * 主题
         */
        private String topic;

        /**
         * 主要领域
         */
        private String primaryField;

        /**
         * 子领域
         */
        private String subField;

        /**
         * 关键词列表
         */
        private List<String> keywords;

        /**
         * 概念列表
         */
        private List<Concept> concepts;
    }

    /**
     * 概念信息
     */
    @Data
    @Builder
    public static class Concept {
        /**
         * 概念名称
         */
        private String name;

        /**
         * 分数
         */
        private Double score;

        /**
         * 级别
         */
        private Integer level;
    }

    /**
     * 访问信息
     */
    @Data
    @Builder
    public static class AccessInfo {
        /**
         * 是否开放获取
         */
        private Boolean isOpenAccess;

        /**
         * OA状态
         */
        private String oaStatus;

        /**
         * PDF链接
         */
        private String pdfLink;

        /**
         * 落地页链接
         */
        private String landingPage;
    }

    /**
     * 搜索元数据
     */
    @Data
    @Builder
    public static class SearchMeta {
        /**
         * 总结果数
         */
        private Long totalResults;

        /**
         * 处理时间（毫秒）
         */
        private Long processingTimeMs;

        /**
         * 当前页码
         */
        private Integer currentPage;
    }
}

