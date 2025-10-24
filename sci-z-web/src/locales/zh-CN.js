export default {
  // 通用
  common: {
    confirm: '确认',
    cancel: '取消',
    save: '保存',
    delete: '删除',
    edit: '编辑',
    add: '新增',
    search: '搜索',
    reset: '重置',
    submit: '提交',
    back: '返回',
    next: '下一步',
    previous: '上一步',
    loading: '加载中...',
    noData: '暂无数据',
    success: '操作成功',
    error: '操作失败',
    warning: '警告',
    info: '提示'
  },

  // 导航菜单
  menu: {
    dashboard: '仪表板',
    declaration: '申报管理',
    declarationList: '申报列表',
    declarationCreate: '新建申报',
    project: '项目管理',
    projectList: '项目列表',
    projectDetail: '项目详情',
    projectProgress: '项目进度',
    report: '报告管理',
    reportList: '报告列表',
    reportGenerate: '报告生成',
    knowledge: '知识库',
    knowledgeList: '知识库列表',
    ai: 'AI助手',
    user: '用户中心',
    userProfile: '个人信息',
    userSecurity: '安全设置',
    system: '系统管理',
    systemUser: '用户管理',
    systemRole: '角色权限',
    systemConfig: '系统配置',
    systemLogs: '日志管理'
  },

  // 认证相关
  auth: {
    login: '登录',
    logout: '退出登录',
    register: '注册',
    username: '用户名',
    password: '密码',
    email: '邮箱',
    phone: '手机号',
    captcha: '验证码',
    rememberMe: '记住我',
    forgotPassword: '忘记密码',
    resetPassword: '重置密码',
    loginSuccess: '登录成功',
    logoutSuccess: '退出登录成功',
    loginFailed: '登录失败',
    invalidCredentials: '用户名或密码错误',
    accountLocked: '账户已被锁定',
    captchaRequired: '请输入验证码',
    captchaError: '验证码错误'
  },

  // 用户相关
  user: {
    profile: '个人信息',
    security: '安全设置',
    avatar: '头像',
    realName: '真实姓名',
    department: '部门',
    industry: '行业',
    role: '角色',
    permissions: '权限',
    lastLogin: '最后登录',
    changePassword: '修改密码',
    oldPassword: '原密码',
    newPassword: '新密码',
    confirmPassword: '确认密码',
    passwordMismatch: '两次输入的密码不一致',
    passwordChanged: '密码修改成功'
  },

  // 申报相关
  declaration: {
    title: '申报标题',
    description: '申报描述',
    researchDirection: '研究方向',
    researchField: '研究领域',
    status: '申报状态',
    statusDraft: '草稿',
    statusPending: '待审核',
    statusApproved: '已通过',
    statusRejected: '已拒绝',
    workflowStatus: '工作流状态',
    createDeclaration: '新建申报',
    editDeclaration: '编辑申报',
    deleteDeclaration: '删除申报',
    declarationCreated: '申报创建成功',
    declarationUpdated: '申报更新成功',
    declarationDeleted: '申报删除成功'
  },

  // 项目相关
  project: {
    name: '项目名称',
    description: '项目描述',
    status: '项目状态',
    statusPending: '待开始',
    statusInProgress: '进行中',
    statusCompleted: '已完成',
    statusCancelled: '已取消',
    startDate: '开始日期',
    endDate: '结束日期',
    progress: '项目进度',
    members: '项目成员',
    leader: '负责人',
    coreMember: '核心成员',
    member: '普通成员',
    addMember: '添加成员',
    removeMember: '移除成员',
    projectCreated: '项目创建成功',
    projectUpdated: '项目更新成功',
    projectDeleted: '项目删除成功'
  },

  // 报告相关
  report: {
    title: '报告标题',
    type: '报告类型',
    typeTech: '科技报告',
    typeSelf: '自评报告',
    status: '报告状态',
    statusGenerating: '生成中',
    statusCompleted: '已完成',
    statusFailed: '生成失败',
    config: '报告配置',
    style: '报告风格',
    detailLevel: '详细程度',
    detailBrief: '简要',
    detailNormal: '标准',
    detailDetailed: '详细',
    includeCharts: '包含图表',
    generateReport: '生成报告',
    exportReport: '导出报告',
    reportGenerated: '报告生成成功',
    reportExported: '报告导出成功'
  },

  // 知识库相关
  knowledge: {
    name: '知识库名称',
    description: '知识库描述',
    type: '知识库类型',
    typeProject: '项目知识库',
    typeIndependent: '独立知识库',
    visibility: '可见性',
    visibilityPrivate: '私有',
    visibilityPublic: '公开',
    syncStatus: '同步状态',
    syncPending: '待同步',
    syncSyncing: '同步中',
    syncCompleted: '同步完成',
    syncFailed: '同步失败',
    files: '文件列表',
    uploadFile: '上传文件',
    createKnowledge: '创建知识库',
    editKnowledge: '编辑知识库',
    deleteKnowledge: '删除知识库',
    knowledgeCreated: '知识库创建成功',
    knowledgeUpdated: '知识库更新成功',
    knowledgeDeleted: '知识库删除成功'
  },

  // AI助手相关
  ai: {
    chat: 'AI助手',
    newChat: '新建对话',
    sendMessage: '发送消息',
    inputPlaceholder: '请输入您的问题...',
    thinking: 'AI正在思考...',
    sources: '知识来源',
    confidence: '置信度',
    noConversation: '暂无对话记录',
    conversationCreated: '对话创建成功',
    messageSent: '消息发送成功'
  },

  // 系统管理相关
  system: {
    userManagement: '用户管理',
    roleManagement: '角色管理',
    permissionManagement: '权限管理',
    configManagement: '配置管理',
    logManagement: '日志管理',
    userName: '用户名',
    userEmail: '用户邮箱',
    userRole: '用户角色',
    userStatus: '用户状态',
    userActive: '启用',
    userInactive: '禁用',
    roleName: '角色名称',
    roleCode: '角色编码',
    roleDescription: '角色描述',
    permissionName: '权限名称',
    permissionCode: '权限编码',
    permissionType: '权限类型',
    configKey: '配置键',
    configValue: '配置值',
    configDescription: '配置描述',
    logType: '日志类型',
    logContent: '日志内容',
    logTime: '日志时间',
    userCreated: '用户创建成功',
    userUpdated: '用户更新成功',
    userDeleted: '用户删除成功',
    roleCreated: '角色创建成功',
    roleUpdated: '角色更新成功',
    roleDeleted: '角色删除成功'
  },

  // 文件相关
  file: {
    upload: '上传文件',
    download: '下载文件',
    preview: '预览文件',
    delete: '删除文件',
    fileName: '文件名',
    fileSize: '文件大小',
    fileType: '文件类型',
    uploadTime: '上传时间',
    uploadSuccess: '文件上传成功',
    uploadFailed: '文件上传失败',
    fileDeleted: '文件删除成功',
    fileNotFound: '文件不存在',
    fileTooLarge: '文件过大',
    fileTypeNotSupported: '不支持的文件类型'
  },

  // 语言切换
  language: {
    zhCN: '中文',
    enUS: 'English',
    koKR: '한국어',
    jaJP: '日本語',
    switchLanguage: '切换语言'
  },

  // 主题切换
  theme: {
    light: '浅色主题',
    dark: '深色主题',
    switchTheme: '切换主题'
  }
}
