package com.sciz.server.infrastructure.shared.constant;

/**
 * 系统常量
 *
 * @author JiaWen.Wu
 * @className SystemConstant
 * @date 2025-10-29 10:30
 */
public class SystemConstant {

        /**
         * 系统名称
         */
        public static final String SYSTEM_NAME = "Sci-Z-Platform";

        /**
         * 系统版本
         */
        public static final String SYSTEM_VERSION = "1.0.0";

        /**
         * 默认分页大小
         */
        public static final Integer DEFAULT_PAGE_SIZE = 10;

        /**
         * 最大分页大小
         */
        public static final Integer MAX_PAGE_SIZE = 100;

        /**
         * 默认页码
         */
        public static final Integer DEFAULT_PAGE_NUM = 1;

        /**
         * 默认超时时间（毫秒）
         */
        public static final Long DEFAULT_TIMEOUT = 30000L;

        /**
         * 默认重试次数
         */
        public static final Integer DEFAULT_RETRY_COUNT = 3;

        /**
         * 默认重试间隔（毫秒）
         */
        public static final Long DEFAULT_RETRY_INTERVAL = 1000L;

        /**
         * 默认缓存过期时间（秒）
         */
        public static final Long DEFAULT_CACHE_EXPIRE = 3600L;

        /**
         * 用户会话过期时间（秒）
         */
        public static final Long USER_SESSION_EXPIRE = 7200L;

        /**
         * JWT令牌过期时间（秒）
         */
        public static final Long JWT_TOKEN_EXPIRE = 86400L;

        /**
         * 刷新令牌过期时间（秒）
         */
        public static final Long REFRESH_TOKEN_EXPIRE = 604800L;

        /**
         * 文件预览URL过期时间（秒）- 24小时
         */
        public static final Integer DEFAULT_PREVIEW_EXPIRE_SECONDS = 86400;

        /**
         * 文件上传最大大小（字节）- 300MB
         */
        public static final Long MAX_FILE_SIZE = 314572800L; // 300 * 1024 * 1024 = 300MB

        /**
         * 支持的文件类型（扩展名）
         * 包含：文档、图片、视频、音频、代码、工业文件、日志、压缩包等常见格式
         */
        public static final String[] SUPPORTED_FILE_TYPES = {
                        // Office 文档
                        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
                        "rtf", "odt", "ods", "odp", "pages", "numbers", "key",
                        // 文本文件
                        "txt", "md", "csv", "json", "xml", "yaml", "yml", "ini", "conf", "cfg",
                        "properties", "log", "out", "err",
                        // 代码文件
                        "java", "js", "ts", "jsx", "tsx", "py", "pyw", "cpp", "c", "h", "hpp",
                        "cs", "php", "rb", "go", "rs", "swift", "kt", "scala", "clj",
                        "html", "htm", "css", "scss", "sass", "less", "vue", "svelte",
                        "sh", "bash", "bat", "cmd", "ps1", "sql", "pl", "lua", "r",
                        "dart", "elm", "ex", "exs", "erl", "hrl",
                        // 图片文件
                        "png", "jpg", "jpeg", "gif", "bmp", "webp", "svg", "ico", "tiff", "tif",
                        "heic", "heif", "raw", "cr2", "nef", "orf", "sr2",
                        // 视频文件
                        "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm", "m4v", "3gp", "3g2",
                        "mpg", "mpeg", "vob", "ogv", "asf", "rm", "rmvb", "ts", "mts",
                        // 音频文件
                        "mp3", "wav", "ogg", "aac", "flac", "m4a", "wma", "opus", "amr",
                        "aiff", "au", "ra", "ape", "ac3", "dts",
                        // 压缩文件
                        "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "lz", "lzma",
                        "cab", "iso", "dmg", "pkg", "deb", "rpm",
                        // 工业文件
                        "dwg", "dxf", "step", "stp", "iges", "igs", "3dm", "obj", "stl",
                        "fbx", "dae", "x3d", "ply", "off", "wrl", "vrml",
                        // 其他常见格式
                        "epub", "mobi", "azw", "azw3", "fb2", "lit", "prc",
                        "ps", "eps", "ai", "sketch", "fig", "xd"
        };

        /**
         * 支持的 MIME 类型
         * 包含：文档、图片、视频、音频、代码、工业文件、日志、压缩包等常见格式的 MIME 类型
         */
        public static final String[] SUPPORTED_MIME_TYPES = {
                        // Office 文档
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "application/rtf",
                        "application/vnd.oasis.opendocument.text",
                        "application/vnd.oasis.opendocument.spreadsheet",
                        "application/vnd.oasis.opendocument.presentation",
                        "application/vnd.apple.pages",
                        "application/vnd.apple.numbers",
                        "application/vnd.apple.keynote",
                        // 文本文件
                        "text/plain",
                        "text/csv",
                        "text/markdown",
                        "text/x-markdown",
                        "application/json",
                        "application/xml",
                        "text/xml",
                        "application/x-yaml",
                        "text/yaml",
                        "text/x-ini",
                        "text/x-properties",
                        "text/x-log",
                        // 代码文件
                        "text/x-java-source",
                        "text/javascript",
                        "application/javascript",
                        "text/typescript",
                        "application/typescript",
                        "text/x-python",
                        "text/x-c++src",
                        "text/x-csrc",
                        "text/x-chdr",
                        "text/x-c++hdr",
                        "text/x-csharp",
                        "text/x-php",
                        "application/x-ruby",
                        "text/x-go",
                        "text/x-rust",
                        "text/x-swift",
                        "text/x-kotlin",
                        "text/x-scala",
                        "text/x-clojure",
                        "text/html",
                        "text/css",
                        "text/x-scss",
                        "text/x-sass",
                        "text/less",
                        "text/x-vue",
                        "text/x-shellscript",
                        "application/x-sh",
                        "application/x-bat",
                        "application/x-sql",
                        "text/x-perl",
                        "text/x-lua",
                        "text/x-r",
                        "text/x-dart",
                        "text/x-elm",
                        "text/x-erlang",
                        // 图片文件
                        "image/png",
                        "image/jpeg",
                        "image/gif",
                        "image/bmp",
                        "image/webp",
                        "image/svg+xml",
                        "image/x-icon",
                        "image/tiff",
                        "image/heic",
                        "image/heif",
                        "image/x-canon-cr2",
                        "image/x-nikon-nef",
                        "image/x-olympus-orf",
                        "image/x-sony-sr2",
                        // 视频文件
                        "video/mp4",
                        "video/x-msvideo",
                        "video/quicktime",
                        "video/x-ms-wmv",
                        "video/x-flv",
                        "video/x-matroska",
                        "video/webm",
                        "video/3gpp",
                        "video/mpeg",
                        "video/dvd",
                        "video/ogg",
                        "video/x-ms-asf",
                        "application/vnd.rn-realmedia",
                        "video/mp2t",
                        // 音频文件
                        "audio/mpeg",
                        "audio/x-wav",
                        "audio/wav",
                        "audio/ogg",
                        "audio/aac",
                        "audio/flac",
                        "audio/mp4",
                        "audio/x-ms-wma",
                        "audio/opus",
                        "audio/amr",
                        "audio/x-aiff",
                        "audio/basic",
                        "audio/vnd.rn-realaudio",
                        "audio/x-ape",
                        "audio/ac3",
                        "audio/vnd.dts",
                        // 压缩文件
                        "application/zip",
                        "application/x-rar-compressed",
                        "application/x-7z-compressed",
                        "application/x-tar",
                        "application/gzip",
                        "application/x-bzip2",
                        "application/x-xz",
                        "application/x-lzip",
                        "application/x-lzma",
                        "application/vnd.ms-cab-compressed",
                        "application/x-iso9660-image",
                        "application/x-apple-diskimage",
                        "application/x-debian-package",
                        "application/x-rpm",
                        // 工业文件
                        "application/acad",
                        "application/dxf",
                        "application/step",
                        "application/iges",
                        "model/obj",
                        "model/stl",
                        "model/x3d+xml",
                        "model/vrml",
                        // 电子书
                        "application/epub+zip",
                        "application/x-mobipocket-ebook",
                        "application/vnd.amazon.ebook",
                        "application/x-fictionbook+xml",
                        "application/x-ms-reader",
                        // 其他
                        "application/postscript",
                        "application/illustrator",
                        "application/vnd.sketchup.skp",
                        "application/fig",
                        "application/vnd.adobe.xd"
        };

        /**
         * 默认用户角色编码
         * 优先使用 normal_users，如果不存在则回退到 user（向后兼容）
         */
        public static final String DEFAULT_USER_ROLE_CODE = "normal_users";
        
        /**
         * 备用默认用户角色编码（向后兼容）
         */
        public static final String FALLBACK_USER_ROLE_CODE = "user";

        /**
         * 日期时间格式
         */
        public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

        /**
         * 时间戳格式
         */
        public static final String TIMESTAMP_FORMATTER = "yyyyMMddHHmmss";

        /**
         * 邮件标题：重置密码验证码
         */
        public static final String EMAIL_SUBJECT_RESET_PASSWORD = "Sci-Z Platform 账号重置验证码";

        /**
         * 邮件标题：通用验证码
         */
        public static final String EMAIL_SUBJECT_VERIFICATION = "Sci-Z Platform 验证码";

        /**
         * 日期格式
         */
        public static final String DATE_FORMAT = "yyyy-MM-dd";

        /**
         * 时间格式
         */
        public static final String TIME_FORMAT = "HH:mm:ss";

        /**
         * 登录用户信息在 Session 中的缓存键
         */
        public static final String LOGIN_USER_SESSION_KEY = "login:user:context";
}
