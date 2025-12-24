package com.sciz.server.application.service.literature.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.application.service.literature.LiteratureSearchService;
import com.sciz.server.domain.pojo.dto.request.literature.LiteratureSearchReq;
import com.sciz.server.domain.pojo.dto.response.literature.LiteratureDetailResp;
import com.sciz.server.domain.pojo.dto.response.literature.LiteratureSearchPageResp;
import com.sciz.server.domain.pojo.dto.response.literature.LiteratureSearchResp;
import com.sciz.server.infrastructure.external.literature.dto.OpenAlexSearchReq;
import com.sciz.server.infrastructure.external.literature.dto.OpenAlexSearchResp;
import com.sciz.server.infrastructure.external.literature.dto.OpenAlexWork;
import com.sciz.server.infrastructure.external.translation.TranslationService;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文献搜索服务实现
 *
 * @author JiaWen.Wu
 * @className LiteratureSearchServiceImpl
 * @date 2025-01-24 14:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiteratureSearchServiceImpl implements LiteratureSearchService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TranslationService translationService;

    /**
     * OpenAlex API 基础URL
     */
    private static final String OPENALEX_BASE_URL = "https://api.openalex.org";

    @Override
    public LiteratureSearchPageResp search(LiteratureSearchReq request) {
        // 默认使用 openalex
        String dataSource = StringUtils.hasText(request.getDataSource()) 
            ? request.getDataSource() 
            : "openalex";

        if ("openalex".equalsIgnoreCase(dataSource)) {
            return searchOpenAlex(request);
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的数据源: " + dataSource);
        }
    }



    /**
     * 调用 OpenAlex API 搜索文献
     */
    private LiteratureSearchPageResp searchOpenAlex(LiteratureSearchReq request) {
        try {
            // 构建 OpenAlex 请求参数
            OpenAlexSearchReq openAlexReq = buildOpenAlexRequest(request);

            // 构建 URL
            String url = buildOpenAlexUrl(openAlexReq);

            log.info(String.format("调用 OpenAlex API 搜索文献: url=%s", url));

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error(String.format("OpenAlex API 调用失败: status=%s, body=%s", 
                    response.getStatusCode(), response.getBody()));
                throw new BusinessException(ResultCode.EXTERNAL_SERVICE_ERROR, "OpenAlex API 调用失败");
            }

            // 解析响应
            OpenAlexSearchResp openAlexResp = objectMapper.readValue(
                response.getBody(),
                OpenAlexSearchResp.class
            );

            // 转换为统一格式
            List<LiteratureSearchResp> literatureList = convertToLiteratureSearchRespList(openAlexResp.getResults());
            
            // 构建分页响应
            OpenAlexSearchResp.Meta meta = openAlexResp.getMeta();
            Integer current = meta != null && meta.getPage() != null ? meta.getPage() : 1;
            Integer size = meta != null && meta.getPerPage() != null ? meta.getPerPage() : 10;
            Long total = meta != null && meta.getCount() != null ? meta.getCount() : 0L;
            Integer pages = total > 0 ? (int) Math.ceil((double) total / size) : 0;
            
            // 为每个文献添加搜索元数据
            for (LiteratureSearchResp resp : literatureList) {
                LiteratureSearchResp.SearchMeta searchMeta = LiteratureSearchResp.SearchMeta.builder()
                    .totalResults(total)
                    .processingTimeMs(meta != null && meta.getDbResponseTimeMs() != null ? meta.getDbResponseTimeMs().longValue() : null)
                    .currentPage(current)
                    .build();
                resp.setSearchMeta(searchMeta);
            }
            
            return LiteratureSearchPageResp.builder()
                .records(literatureList)
                .total(total)
                .current(current)
                .size(size)
                .pages(pages)
                .build();

        } catch (JsonProcessingException e) {
            log.error("解析 OpenAlex 响应失败", e);
            throw new BusinessException(ResultCode.EXTERNAL_SERVICE_ERROR, "解析 OpenAlex 响应失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用 OpenAlex API 失败", e);
            throw new BusinessException(ResultCode.EXTERNAL_SERVICE_ERROR, "调用 OpenAlex API 失败: " + e.getMessage());
        }
    }

    /**
     * 获取 OpenAlex 文献详情
     */
    private LiteratureDetailResp getOpenAlexDetail(String id) {
        try {
            // 构建 URL（OpenAlex 使用 /works/{id} 获取详情）
            String url = OPENALEX_BASE_URL + "/works/" + id;

            log.info(String.format("调用 OpenAlex API 获取文献详情: url=%s", url));

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error(String.format("OpenAlex API 调用失败: status=%s, body=%s", 
                    response.getStatusCode(), response.getBody()));
                throw new BusinessException(ResultCode.EXTERNAL_SERVICE_ERROR, "OpenAlex API 调用失败");
            }

            // 解析响应
            OpenAlexWork work = objectMapper.readValue(
                response.getBody(),
                OpenAlexWork.class
            );

            // 转换为统一格式
            return convertToLiteratureDetailResp(work);

        } catch (JsonProcessingException e) {
            log.error("解析 OpenAlex 响应失败", e);
            throw new BusinessException(ResultCode.EXTERNAL_SERVICE_ERROR, "解析 OpenAlex 响应失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用 OpenAlex API 失败", e);
            throw new BusinessException(ResultCode.EXTERNAL_SERVICE_ERROR, "调用 OpenAlex API 失败: " + e.getMessage());
        }
    }

    /**
     * 构建 OpenAlex 请求参数
     */
    private OpenAlexSearchReq buildOpenAlexRequest(LiteratureSearchReq request) {
        OpenAlexSearchReq.OpenAlexSearchReqBuilder builder = OpenAlexSearchReq.builder();

        if (StringUtils.hasText(request.getSearch())) {
            builder.search(request.getSearch());
        }

        // 构建 filter 参数
        if (StringUtils.hasText(request.getPublicationYearFilter())) {
            builder.filter("publication_year:" + request.getPublicationYearFilter());
        }

        if (request.getPerPage() != null) {
            builder.perPage(request.getPerPage());
        } else {
            builder.perPage(10); // 默认10条
        }

        if (request.getPage() != null) {
            builder.page(request.getPage());
        } else {
            builder.page(1); // 默认第1页
        }

        return builder.build();
    }

    /**
     * 构建 OpenAlex API URL (手动拼接方式)
     */
    private String buildOpenAlexUrl(OpenAlexSearchReq request) {
        StringBuilder urlBuilder = new StringBuilder(OPENALEX_BASE_URL + "/works?");
        boolean hasParams = false;

        if (StringUtils.hasText(request.getSearch())) {
             urlBuilder.append("search=").append(request.getSearch());
            hasParams = true;
        }

        if (request.getPerPage() != null) {
            if (hasParams) {
                urlBuilder.append("&");
            }
            urlBuilder.append("per-page=").append(request.getPerPage());
            hasParams = true;
        }

        if (request.getPage() != null) {
            if (hasParams) {
                urlBuilder.append("&");
            }
            urlBuilder.append("page=").append(request.getPage());
        }

        return urlBuilder.toString();
    }

    /**
     * 将 OpenAlex 结果列表转换为统一的文献搜索响应列表
     */
    private List<LiteratureSearchResp> convertToLiteratureSearchRespList(List<OpenAlexWork> works) {
        if (works == null || works.isEmpty()) {
            return new ArrayList<>();
        }

        return works.stream()
            .map(this::convertToLiteratureSearchResp)
            .collect(Collectors.toList());
    }

    /**
     * 将 OpenAlex Work 转换为统一的文献搜索响应
     */
    private LiteratureSearchResp convertToLiteratureSearchResp(OpenAlexWork work) {
        LiteratureSearchResp.LiteratureSearchRespBuilder builder = LiteratureSearchResp.builder();

        // 构建论文基本信息
        LiteratureSearchResp.PaperInfo.PaperInfoBuilder paperInfoBuilder = LiteratureSearchResp.PaperInfo.builder();
        if (work.getId() != null) {
            // 提取短ID（如从 https://openalex.org/W4383560187 提取 W4383560187）
            String shortId = work.getId();
            if (shortId.contains("/")) {
                shortId = shortId.substring(shortId.lastIndexOf("/") + 1);
            }
            paperInfoBuilder.id(shortId)
                .globalId(work.getId());
        }
        paperInfoBuilder.doi(work.getDoi())
            .title(work.getTitle())
            .publicationYear(work.getPublicationYear())
            .publicationDate(work.getPublicationDate())
            .language(work.getLanguage())
            .type(work.getType());

        // 处理摘要（从倒排索引转换为文本）
        String abstractText = null;
        if (work.getAbstractInvertedIndex() != null) {
            abstractText = convertAbstractFromInvertedIndex(work.getAbstractInvertedIndex());
            paperInfoBuilder.abstractText(abstractText);
        }

        // 翻译标题和摘要（如果源语言不是中文，则翻译为中文）
        String sourceLanguage = work.getLanguage() != null ? work.getLanguage() : "en";
        if (!"zh".equalsIgnoreCase(sourceLanguage) && !"zh-CN".equalsIgnoreCase(sourceLanguage)) {
            // 翻译标题
            if (StringUtils.hasText(work.getTitle())) {
                String titleTranslated = translationService.translate(work.getTitle(), sourceLanguage, "zh");
                paperInfoBuilder.titleTranslated(titleTranslated);
            }
//            // 翻译摘要
//            if (StringUtils.hasText(abstractText)) {
//                String abstractTranslated = translationService.translate(abstractText, sourceLanguage, "zh");
//                paperInfoBuilder.abstractTranslated(abstractTranslated);
//            }
        }

        builder.paperInfo(paperInfoBuilder.build());

        // 构建来源信息
        if (work.getPrimaryLocation() != null && work.getPrimaryLocation().getSource() != null) {
            OpenAlexWork.Source source = work.getPrimaryLocation().getSource();
            LiteratureSearchResp.SourceInfo sourceInfo = LiteratureSearchResp.SourceInfo.builder()
                .journalName(source.getDisplayName())
                .issn(source.getIssnL())
                .type(source.getType())
                .publisher(null) // OpenAlex 不直接提供 publisher
                .build();
            builder.sourceInfo(sourceInfo);
        }

        // 构建作者信息
        if (work.getAuthorships() != null && !work.getAuthorships().isEmpty()) {
            List<LiteratureSearchResp.AuthorInfo> authorsInfo = work.getAuthorships().stream()
                .map(authorship -> {
                    LiteratureSearchResp.AuthorInfo.AuthorInfoBuilder authorBuilder = 
                        LiteratureSearchResp.AuthorInfo.builder()
                        .isCorresponding(authorship.getIsCorresponding())
                        .position(authorship.getAuthorPosition());

                    if (authorship.getAuthor() != null) {
                        authorBuilder.name(authorship.getAuthor().getDisplayName())
                            .id(authorship.getAuthor().getId());
                    } else if (StringUtils.hasText(authorship.getRawAuthorName())) {
                        authorBuilder.name(authorship.getRawAuthorName());
                    }

                    return authorBuilder.build();
                })
                .collect(Collectors.toList());
            builder.authorsInfo(authorsInfo);
        }

        // 构建影响指标
        LiteratureSearchResp.ImpactMetrics.ImpactMetricsBuilder impactBuilder = 
            LiteratureSearchResp.ImpactMetrics.builder()
            .citationCount(work.getCitedByCount());

        // 处理高被引标识（根据 citation_normalized_percentile 判断）
        Double percentile = extractCitationPercentile(work.getCitationNormalizedPercentile());
        if (percentile != null) {
            impactBuilder.isTop1Percent(percentile >= 99.0);
            impactBuilder.isHighlyCited(percentile >= 95.0);
        }

        // 处理按年份的引用次数
        if (work.getCountsByYear() != null && !work.getCountsByYear().isEmpty()) {
            List<LiteratureSearchResp.YearlyCitation> yearlyCitations = work.getCountsByYear().stream()
                .map(count -> LiteratureSearchResp.YearlyCitation.builder()
                    .year(count.getYear())
                    .count(count.getCitedByCount())
                    .build())
                .collect(Collectors.toList());
            impactBuilder.yearlyCitations(yearlyCitations);
        }
        builder.impactMetrics(impactBuilder.build());

        // 构建分类信息
        LiteratureSearchResp.Taxonomy.TaxonomyBuilder taxonomyBuilder = 
            LiteratureSearchResp.Taxonomy.builder();

        // 处理主要主题
        if (work.getPrimaryTopic() != null) {
            taxonomyBuilder.topic(work.getPrimaryTopic().getDisplayName());
            if (work.getPrimaryTopic().getField() != null) {
                taxonomyBuilder.primaryField(work.getPrimaryTopic().getField().getDisplayName());
            }
            if (work.getPrimaryTopic().getSubfield() != null) {
                taxonomyBuilder.subField(work.getPrimaryTopic().getSubfield().getDisplayName());
            }
        }

        // 处理关键词
        if (work.getKeywords() != null && !work.getKeywords().isEmpty()) {
            List<String> keywords = work.getKeywords().stream()
                .map(OpenAlexWork.Keyword::getDisplayName)
                .collect(Collectors.toList());
            taxonomyBuilder.keywords(keywords);
        }

        // 处理概念（从 topics 中提取）
        if (work.getTopics() != null && !work.getTopics().isEmpty()) {
            List<LiteratureSearchResp.Concept> concepts = work.getTopics().stream()
                .map(topic -> LiteratureSearchResp.Concept.builder()
                    .name(topic.getDisplayName())
                    .score(topic.getScore())
                    .level(0) // OpenAlex topics 没有明确的 level
                    .build())
                .collect(Collectors.toList());
            taxonomyBuilder.concepts(concepts);
        }
        builder.taxonomy(taxonomyBuilder.build());

        // 构建访问信息
        LiteratureSearchResp.AccessInfo.AccessInfoBuilder accessBuilder = 
            LiteratureSearchResp.AccessInfo.builder();

        if (work.getOpenAccess() != null) {
            accessBuilder.isOpenAccess(work.getOpenAccess().getIsOa())
                .oaStatus(work.getOpenAccess().getOaStatus());
        }

        if (work.getPrimaryLocation() != null) {
            accessBuilder.pdfLink(work.getPrimaryLocation().getPdfUrl())
                .landingPage(work.getPrimaryLocation().getLandingPageUrl());
        }
        builder.accessInfo(accessBuilder.build());

        return builder.build();
    }

    /**
     * 将 OpenAlex Work 转换为统一的文献详情响应
     */
    private LiteratureDetailResp convertToLiteratureDetailResp(OpenAlexWork work) {
        LiteratureDetailResp.LiteratureDetailRespBuilder builder = LiteratureDetailResp.builder();

        builder.id(work.getId())
            .title(work.getTitle())
            .displayName(work.getDisplayName())
            .doi(work.getDoi())
            .publicationYear(work.getPublicationYear())
            .publicationDate(work.getPublicationDate())
            .citedByCount(work.getCitedByCount())
            .relevanceScore(work.getRelevanceScore())
            .type(work.getType())
            .language(work.getLanguage())
            .dataSource("openalex")
            .applicationUrl(work.getId()); // OpenAlex 的文献详情页URL就是ID

        // 处理摘要（从倒排索引转换为文本）
        if (work.getAbstractInvertedIndex() != null) {
            builder.abstractText(convertAbstractFromInvertedIndex(work.getAbstractInvertedIndex()));
        }

        // 处理主要位置信息
        if (work.getPrimaryLocation() != null) {
            OpenAlexWork.PrimaryLocation location = work.getPrimaryLocation();
            builder.landingPageUrl(location.getLandingPageUrl())
                .pdfUrl(location.getPdfUrl());

            // 处理来源信息
            if (location.getSource() != null) {
                LiteratureSearchResp.SourceInfo sourceInfo = LiteratureSearchResp.SourceInfo.builder()
                    .journalName(location.getSource().getDisplayName())
                    .issn(location.getSource().getIssnL())
                    .type(location.getSource().getType())
                    .publisher(null)
                    .build();
                builder.source(sourceInfo);
            }
        }

        // 处理作者信息
        if (work.getAuthorships() != null && !work.getAuthorships().isEmpty()) {
            List<LiteratureSearchResp.AuthorInfo> authors = work.getAuthorships().stream()
                .map(authorship -> {
                    LiteratureSearchResp.AuthorInfo.AuthorInfoBuilder authorBuilder = 
                        LiteratureSearchResp.AuthorInfo.builder()
                        .isCorresponding(authorship.getIsCorresponding());

                    if (authorship.getAuthor() != null) {
                        authorBuilder.name(authorship.getAuthor().getDisplayName());
                    } else if (StringUtils.hasText(authorship.getRawAuthorName())) {
                        authorBuilder.name(authorship.getRawAuthorName());
                    }

                    return authorBuilder.build();
                })
                .collect(Collectors.toList());
            builder.authors(authors);
        }

        // 处理关键词
        if (work.getKeywords() != null && !work.getKeywords().isEmpty()) {
            List<LiteratureDetailResp.KeywordInfo> keywords = work.getKeywords().stream()
                .map(keyword -> LiteratureDetailResp.KeywordInfo.builder()
                    .id(keyword.getId())
                    .name(keyword.getDisplayName())
                    .score(keyword.getScore())
                    .build())
                .collect(Collectors.toList());
            builder.keywords(keywords);
        }

        // 处理主题
        if (work.getTopics() != null && !work.getTopics().isEmpty()) {
            List<LiteratureDetailResp.TopicInfo> topics = work.getTopics().stream()
                .map(topic -> {
                    LiteratureDetailResp.TopicInfo.TopicInfoBuilder topicBuilder = 
                        LiteratureDetailResp.TopicInfo.builder()
                        .id(topic.getId())
                        .name(topic.getDisplayName())
                        .score(topic.getScore());

                    if (topic.getSubfield() != null) {
                        topicBuilder.subfield(topic.getSubfield().getDisplayName());
                    }
                    if (topic.getField() != null) {
                        topicBuilder.field(topic.getField().getDisplayName());
                    }
                    if (topic.getDomain() != null) {
                        topicBuilder.domain(topic.getDomain().getDisplayName());
                    }

                    return topicBuilder.build();
                })
                .collect(Collectors.toList());
            builder.topics(topics);
        }

        // 处理相关文献和引用文献
        builder.relatedWorks(work.getRelatedWorks())
            .referencedWorks(work.getReferencedWorks())
            .referencedWorksCount(work.getReferencedWorksCount());

        // 处理按年份的引用次数统计
        if (work.getCountsByYear() != null && !work.getCountsByYear().isEmpty()) {
            List<LiteratureDetailResp.CitationByYear> citationByYear = work.getCountsByYear().stream()
                .map(count -> LiteratureDetailResp.CitationByYear.builder()
                    .year(count.getYear())
                    .citedByCount(count.getCitedByCount())
                    .build())
                .collect(Collectors.toList());
            builder.citationByYear(citationByYear);
        }

        // 处理引用百分位（可能为 null、数字或对象）
        builder.citationPercentile(extractCitationPercentile(work.getCitationNormalizedPercentile()));

        // 处理机构和国家数量
        builder.institutionsCount(work.getInstitutionsDistinctCount())
            .countriesCount(work.getCountriesDistinctCount());

        // 处理机构列表
        if (work.getInstitutions() != null && !work.getInstitutions().isEmpty()) {
            List<LiteratureDetailResp.InstitutionInfo> institutions = work.getInstitutions().stream()
                .map(inst -> LiteratureDetailResp.InstitutionInfo.builder()
                    .id(inst.getId())
                    .name(inst.getDisplayName())
                    .ror(inst.getRor())
                    .countryCode(inst.getCountryCode())
                    .type(inst.getType())
                    .build())
                .collect(Collectors.toList());
            builder.institutions(institutions);
        }

        return builder.build();
    }

    /**
     * 将倒排索引格式的摘要转换为文本
     */
    private String convertAbstractFromInvertedIndex(Map<String, List<Integer>> invertedIndex) {
        if (invertedIndex == null || invertedIndex.isEmpty()) {
            return null;
        }

        // 创建一个位置到单词的映射（可能有多个单词在同一位置，需要处理）
        Map<Integer, List<String>> positionToWords = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : invertedIndex.entrySet()) {
            String word = entry.getKey();
            for (Integer position : entry.getValue()) {
                positionToWords.computeIfAbsent(position, k -> new ArrayList<>()).add(word);
            }
        }

        // 按位置排序并拼接（如果同一位置有多个单词，取第一个）
        return positionToWords.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                List<String> words = entry.getValue();
                // 如果同一位置有多个单词，通常取第一个
                return words.isEmpty() ? "" : words.get(0);
            })
            .filter(word -> !word.isEmpty())
            .collect(Collectors.joining(" "));
    }

    /**
     * 从 citation_normalized_percentile 字段中提取 Double 值
     */
    private Double extractCitationPercentile(Object percentile) {
        if (percentile == null) {
            return null;
        }

        // 如果已经是 Double 类型，直接返回
        if (percentile instanceof Double) {
            return (Double) percentile;
        }

        // 如果是 Integer 类型，转换为 Double
        if (percentile instanceof Integer) {
            return ((Integer) percentile).doubleValue();
        }

        // 如果是 Number 类型，转换为 Double
        if (percentile instanceof Number) {
            return ((Number) percentile).doubleValue();
        }

        // 如果是 Map 对象，尝试提取常见的数值字段
        if (percentile instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) percentile;
            // 尝试提取常见的字段名
            Object value = map.get("value");
            if (value == null) {
                value = map.get("percentile");
            }
            if (value == null) {
                value = map.get("score");
            }
            if (value != null) {
                return extractCitationPercentile(value); // 递归处理
            }
        }

        // 无法提取，返回 null
        log.warn(String.format("无法提取 citation_normalized_percentile 值: %s", percentile));
        return null;
    }
}

