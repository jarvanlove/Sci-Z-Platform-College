export default {
  // Common
  common: {
    confirm: 'Confirm',
    cancel: 'Cancel',
    save: 'Save',
    delete: 'Delete',
    edit: 'Edit',
    add: 'Add',
    search: 'Search',
    reset: 'Reset',
    submit: 'Submit',
    back: 'Back',
    next: 'Next',
    previous: 'Previous',
    loading: 'Loading...',
    noData: 'No Data',
    success: 'Success',
    error: 'Error',
    warning: 'Warning',
    info: 'Info'
  },

  // Navigation Menu
  menu: {
    dashboard: 'Dashboard',
    declaration: 'Declaration',
    declarationList: 'Declaration List',
    declarationCreate: 'Create Declaration',
    project: 'Project',
    projectList: 'Project List',
    projectDetail: 'Project Detail',
    projectProgress: 'Project Progress',
    report: 'Report',
    reportList: 'Report List',
    reportGenerate: 'Generate Report',
    knowledge: 'Knowledge Base',
    knowledgeList: 'Knowledge List',
    ai: 'AI Assistant',
    user: 'User Center',
    userProfile: 'Profile',
    userSecurity: 'Security',
    system: 'System',
    systemUser: 'User Management',
    systemRole: 'Role & Permission',
    systemConfig: 'System Config',
    systemLogs: 'Log Management'
  },

  // Authentication
  auth: {
    login: 'Login',
    logout: 'Logout',
    register: 'Register',
    username: 'Username',
    password: 'Password',
    email: 'Email',
    phone: 'Phone',
    captcha: 'Captcha',
    rememberMe: 'Remember Me',
    forgotPassword: 'Forgot Password',
    resetPassword: 'Reset Password',
    loginSuccess: 'Login Success',
    logoutSuccess: 'Logout Success',
    loginFailed: 'Login Failed',
    invalidCredentials: 'Invalid username or password',
    accountLocked: 'Account is locked',
    captchaRequired: 'Please enter captcha',
    captchaError: 'Invalid captcha'
  },

  // User
  user: {
    profile: 'Profile',
    security: 'Security',
    avatar: 'Avatar',
    realName: 'Real Name',
    department: 'Department',
    industry: 'Industry',
    role: 'Role',
    permissions: 'Permissions',
    lastLogin: 'Last Login',
    changePassword: 'Change Password',
    oldPassword: 'Old Password',
    newPassword: 'New Password',
    confirmPassword: 'Confirm Password',
    passwordMismatch: 'Passwords do not match',
    passwordChanged: 'Password changed successfully'
  },

  // Declaration
  declaration: {
    title: 'Declaration Title',
    description: 'Description',
    researchDirection: 'Research Direction',
    researchField: 'Research Field',
    status: 'Status',
    statusDraft: 'Draft',
    statusPending: 'Pending',
    statusApproved: 'Approved',
    statusRejected: 'Rejected',
    workflowStatus: 'Workflow Status',
    createDeclaration: 'Create Declaration',
    editDeclaration: 'Edit Declaration',
    deleteDeclaration: 'Delete Declaration',
    declarationCreated: 'Declaration created successfully',
    declarationUpdated: 'Declaration updated successfully',
    declarationDeleted: 'Declaration deleted successfully'
  },

  // Project
  project: {
    name: 'Project Name',
    description: 'Description',
    status: 'Status',
    statusPending: 'Pending',
    statusInProgress: 'In Progress',
    statusCompleted: 'Completed',
    statusCancelled: 'Cancelled',
    startDate: 'Start Date',
    endDate: 'End Date',
    progress: 'Progress',
    members: 'Members',
    leader: 'Leader',
    coreMember: 'Core Member',
    member: 'Member',
    addMember: 'Add Member',
    removeMember: 'Remove Member',
    projectCreated: 'Project created successfully',
    projectUpdated: 'Project updated successfully',
    projectDeleted: 'Project deleted successfully'
  },

  // Report
  report: {
    title: 'Report Title',
    type: 'Report Type',
    typeTech: 'Technical Report',
    typeSelf: 'Self Assessment',
    status: 'Status',
    statusGenerating: 'Generating',
    statusCompleted: 'Completed',
    statusFailed: 'Failed',
    config: 'Report Config',
    style: 'Style',
    detailLevel: 'Detail Level',
    detailBrief: 'Brief',
    detailNormal: 'Normal',
    detailDetailed: 'Detailed',
    includeCharts: 'Include Charts',
    generateReport: 'Generate Report',
    exportReport: 'Export Report',
    reportGenerated: 'Report generated successfully',
    reportExported: 'Report exported successfully'
  },

  // Knowledge Base
  knowledge: {
    name: 'Knowledge Base Name',
    description: 'Description',
    type: 'Type',
    typeProject: 'Project Knowledge',
    typeIndependent: 'Independent Knowledge',
    visibility: 'Visibility',
    visibilityPrivate: 'Private',
    visibilityPublic: 'Public',
    syncStatus: 'Sync Status',
    syncPending: 'Pending',
    syncSyncing: 'Syncing',
    syncCompleted: 'Completed',
    syncFailed: 'Failed',
    files: 'Files',
    uploadFile: 'Upload File',
    createKnowledge: 'Create Knowledge Base',
    editKnowledge: 'Edit Knowledge Base',
    deleteKnowledge: 'Delete Knowledge Base',
    knowledgeCreated: 'Knowledge base created successfully',
    knowledgeUpdated: 'Knowledge base updated successfully',
    knowledgeDeleted: 'Knowledge base deleted successfully'
  },

  // AI Assistant
  ai: {
    chat: 'AI Assistant',
    newChat: 'New Chat',
    sendMessage: 'Send Message',
    inputPlaceholder: 'Please enter your question...',
    thinking: 'AI is thinking...',
    sources: 'Sources',
    confidence: 'Confidence',
    noConversation: 'No conversation history',
    conversationCreated: 'Conversation created successfully',
    messageSent: 'Message sent successfully'
  },

  // System Management
  system: {
    userManagement: 'User Management',
    roleManagement: 'Role Management',
    permissionManagement: 'Permission Management',
    configManagement: 'Config Management',
    logManagement: 'Log Management',
    userName: 'Username',
    userEmail: 'Email',
    userRole: 'Role',
    userStatus: 'Status',
    userActive: 'Active',
    userInactive: 'Inactive',
    roleName: 'Role Name',
    roleCode: 'Role Code',
    roleDescription: 'Description',
    permissionName: 'Permission Name',
    permissionCode: 'Permission Code',
    permissionType: 'Permission Type',
    configKey: 'Config Key',
    configValue: 'Config Value',
    configDescription: 'Description',
    logType: 'Log Type',
    logContent: 'Log Content',
    logTime: 'Log Time',
    userCreated: 'User created successfully',
    userUpdated: 'User updated successfully',
    userDeleted: 'User deleted successfully',
    roleCreated: 'Role created successfully',
    roleUpdated: 'Role updated successfully',
    roleDeleted: 'Role deleted successfully'
  },

  // File
  file: {
    upload: 'Upload File',
    download: 'Download File',
    preview: 'Preview File',
    delete: 'Delete File',
    fileName: 'File Name',
    fileSize: 'File Size',
    fileType: 'File Type',
    uploadTime: 'Upload Time',
    uploadSuccess: 'File uploaded successfully',
    uploadFailed: 'File upload failed',
    fileDeleted: 'File deleted successfully',
    fileNotFound: 'File not found',
    fileTooLarge: 'File too large',
    fileTypeNotSupported: 'File type not supported'
  },

  // Language
  language: {
    zhCN: '中文',
    enUS: 'English',
    koKR: '한국어',
    jaJP: '日本語',
    switchLanguage: 'Switch Language'
  },

  // Theme
  theme: {
    light: 'Light Theme',
    dark: 'Dark Theme',
    switchTheme: 'Switch Theme'
  }
}
