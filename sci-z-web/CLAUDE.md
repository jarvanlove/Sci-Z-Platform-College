# Sci-Z Web 前端开发规范

> **注意**: 本文档整合了前端项目的所有开发规则，Cursor 2.0 会自动识别并应用此文件中的规则。

---

## 一、角色定位与前置条件

### 角色定位

Vue 3 + TypeScript 专家、UI 设计师

### 回复规范

#### 1. 理解需求

- 仔细分析用户提出的问题或需求
- 识别核心技术点和关键问题
- 考虑前后端交互的完整流程
- 参考原型图设计和视觉要求

#### 2. 回复结构

- 回复结果始终为中文
- 显示当前回复使用的模型信息
- 列出一个执行计划然后依次执行
- 每次回复之前都要将你理解的需求向我描述一遍，我确认后才可以进行编码
- 提供清晰的解决方案
- 简明扼要地总结问题

---

## 二、提示词模板使用指南

为确保各业务模块开发过程中的沟通一致、效率更高，请在开始实现前参考 `相关文档/提示词模板` 目录下的模板文件。各模块对应关系如下：

- 认证模块：`相关文档/提示词模板/认证模块/`
- 申报模块：`相关文档/提示词模板/申报模块/`
- 项目模块：`相关文档/提示词模板/项目模块/`
- 验收模块：`相关文档/提示词模板/验收模块/`
- 系统管理：`相关文档/提示词模板/系统管理/`
- 用户中心：`相关文档/提示词模板/用户中心/`
- 仪表板：`相关文档/提示词模板/仪表板/`
- AI 助手：`相关文档/提示词模板/AI助手/`
- 通用页面开发：`相关文档/提示词模板/统一页面开发提示词.md`
- 组件库设计：`相关文档/提示词模板/组件库设计提示词.md`

### 使用建议

- 在分析需求时先阅读对应模块的提示词模板，明确交互规范、接口约定与测试要点。
- 若模块涉及跨域调用或复合业务场景，可结合“通用页面开发”与具体模块模板交叉参考。
- 如需新增模块提示词，请保持目录结构一致，并在本节中同步补充说明。

---

## 三、行业配置与部门标签使用规范

- 服务端接口
  - 行业配置：`GET /api/system/industry/config`
  - 部门标签：`GET /api/auth/department/label`
- 登录成功后，必须在回调中并行调用 `industryStore.fetchIndustryConfig()` 预热行业信息；成功时缓存 `industryCode`/`industryName` 到 Pinia 与 `localStorage`，失败时回退至默认行业（code=`default`）。
- 所有依赖行业的页面（如注册页）统一使用 `useIndustryStore()`，调用 `fetchDepartmentLabels()` 获取下拉选项；该方法会先保证行业信息存在，不存在时自动触发行业配置接口。
- 注册表单中的“所属院系/科室/部门”文案及部门列表必须根据 `industryStore.departmentLabelKey`、`departmentPlaceholderKey` 动态渲染，确保行业切换后展示正确。
- 若需要支持新的行业类型，需同步扩展 `industryStore` 中的映射以及对应的 i18n 文案。

---

## 四、前端架构落地规范（重点）

> 目标：结合提示词模板 + 本节规范，能够按照现有架构快速、准确地实现任意页面/功能。

### 1. 目录与职责划分
- `views/` 只负责路由挂载，通常写成极薄包装层（仅引入业务组件或布置页面布局），禁止在此编写复杂业务逻辑。  
  - 若页面需要主布局，直接使用 `MainLayout`，或参考既有页面的使用方式。
- `components/Business/<模块>/` 承担页面主体实现：布局、表单、数据交互、状态处理、样式等均放在这里。  
  - 新增功能必须按模块落在对应目录，必要时先确认模块是否存在。  
  - 业务组件内部可自由拆分子组件，但**不能新增目录层级**。
- `components/Common/` 提供通用基础组件（Button/Card/Pagination 等），优先复用，勿重复造轮子。
- `api/<模块>/` 包含接口封装，`store/modules/` 为 Pinia 仓库，`utils/` 为通用工具。业务组件需要调用接口/状态时请从对应模块导入。

### 2. 开发流程
1. **阅读提示词模板**：确认交互流程、接口、校验与测试要点。  
2. **业务组件实现**：
   - 使用 `<script setup>` + Composition API。  
   - 引用 Common 组件构建 UI，引用 Business 子组件（若有）组合页面。  
   - 接口调用统一通过 `api/<模块>`，异步需 `try/catch` + `ElMessage` 或日志反馈。  
   - 状态引用 Pinia：`useAuthStore`、`useIndustryStore` 等。涉及跨模块数据务必在业务组件处理，不要在 view 层处理。
3. **视图包装**：在 `views/<模块>/<Page>.vue` 中仅引入业务组件并渲染，必要时再加布局/路由逻辑。  
4. **国际化**：所有文案用 `$t()`，若新增键值需同步 `locales/` 下四种语言（zh-CN/en-US/ja-JP/ko-KR），保持含义一致。  
5. **样式**：采用 `lang="scss" scoped`，优先使用设计系统变量（`var(--gap-*)` 等）。响应式参考现有写法。

### 3. 代码规范补充
- **日志**：需要调试输出请使用 `createLogger('模块名')`，避免直接 `console.log`（上线前应清理）。  
- **表单验证**：使用 Element Plus 表单规则 + `utils/validate` 中方法；自定义校验器需提供用户提示。  
- **文件/命名**：继续遵循 `CamelCase/PascalCase` 约定，禁止新增目录。业务组件命名示例：`UserProfile.vue`、`UserSecurity.vue`。  
- **接口返回处理**：所有 API 统一通过 `checkLoginStatus`、`getUserInfo` 等封装；若需新增接口，请先在 `api/<模块>` 中定义方法，再在业务组件里调用。
- **错误处理**：异步错误必须兜底提示（`ElMessage.error`）并在日志中写明上下文；不要静默失败。

### 4. 验证与提交
- 本地手动验证：路由跳转、接口请求、表单校验、国际化、响应式。  
- 保持 `read_lints` 无警告。  
- 完成后在总结里列出涉及的接口与 JSON 结构，方便后端联调。

### 5. 文件上传规范
- **枚举常量**：前端统一使用 `src/constants/attachment.js` 中的 `ATTACHMENT_RELATION`、`ATTACHMENT_CATEGORY`，与后端枚举保持一致，禁止在业务代码中写字符串常量。
- **relationType 使用场景**：当文件需要挂载到具体业务对象（如项目、申报、报告、用户、知识库）时必须传递，对应选择 `ATTACHMENT_RELATION` 中的值；纯个人临时文件可留空。
- **attachmentType 取值**：根据文件内容选择 `document`、`image`、`export`、`other`，与 `ATTACHMENT_CATEGORY` 对齐，便于服务端分类。
- **relationId / relationName**：仅在有业务关联时传递。其中 `relationId` 为业务主键，`relationName` 为可读名辅助展示；若无具体关联对象则不要提交这两个字段。
- **公共字段**：始终通过 `FormData` 上传文件本体（`file`），并根据业务约定补充 `isPublic`、业务自定义参数等。

### 6. 组件目录速览
- **业务组件**：
  - `components/Business/Detail/`：详情页通用组件（时间轴、进度条、信息卡、附件列表等）。
  - `components/Business/Form/`：表单流程组件（区块包装、按钮区、通用文件上传等）。
  - `components/Business/Legacy/`：旧版仪表盘/统计相关组件，逐步迁移但仍保留复用。
  - `components/Business/List/`：列表页基础组件（筛选表单、表格、状态标签、行内操作按钮）。
- **通用组件 (`components/Common/`)**：
  - `BaseButton.vue`：二次封装按钮，统一主题色、尺寸、加载态，覆盖 Element Plus 按钮样式。
  - `BaseCard.vue`：标准卡片容器，提供标题/内容插槽与统一样式。
  - `BaseDialog.vue`：通用弹窗，封装 el-dialog，内置标题、页脚、快捷关闭等逻辑，覆盖 Element Plus 弹窗样式。
  - `BasePagination.vue`：分页器封装，完全自定义实现（不使用 el-pagination），兼容后端分页字段与事件，支持省略号、第一页/最后一页显示，支持国际化。
  - `BaseDatePicker.vue`：通用日期选择器组件，封装 el-date-picker，支持中文显示和多语言切换（zh-CN/en-US/ja-JP/ko-KR），覆盖日历弹窗暗色主题样式。
  - `BaseScrollbar.vue`：自定义滚动容器，统一不同浏览器滚动体验。
  - `BaseTable.vue`：通用表格，封装 el-table，整合列配置、加载态、空状态等，覆盖表格暗色主题样式。
  - `LanguageSwitcher.vue`：语言切换入口，驱动 `i18n` 国际化。
  - `AgreementNotice.vue`：协议提醒/确认组件，常用于登录注册场景。
  
  **注意**：目前 `el-select`、`el-input`、`el-textarea` 等组件尚未封装为自定义组件，但已在 `src/assets/styles/design-system.scss` 中通过全局样式覆盖了 Element Plus 的默认样式，确保暗色主题适配。后续可根据需要创建 `BaseSelect`、`BaseInput` 等封装组件。

### 7. 组件化思想
- **能复用就抽象**：前端实现优先考虑可复用的“组件 + 工具 + 常量”组合，再由页面进行装配；不要在业务页面内散写重复逻辑。
- **单一职责**：组件只承担单一功能（如上传校验、按钮样式、弹窗逻辑），复合场景通过组合多个组件完成。
- **配置驱动**：将枚举、白名单、尺寸等可调参数集中定义（如 `attachment.js`），页面仅引用配置，提高一致性与维护效率。
- **后端兜底**：前端提供基础校验和良好提示，服务端仍需做最终判断，确保安全与一致性。
- **文件上传流程***：
  - `useFileUpload` 组合函数统一处理文件校验（大小默认 150MB、支持传入枚举）、MD5 去重（调用 `/api/file/check-duplicate`）、上传与复用提示。
  - 使用方式：`const uploader = useFileUpload({ maxSizeMB, allowedExtensions, getExtraFormData })` → `await uploader.uploadWithCheck(file)`，返回 `{ fileInfo, reused }`；根据业务需要提示用户保存或落库。
  - 所有上传业务需通过该流程，确保前端校验一致、避免重复上传，业务差异通过入参和文案控制。

---


