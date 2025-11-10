export default {
  // App Info
  app: {
    title: 'Generative University Research Management Platform',
    description: 'AIGC-based Generative University Research Management Platform',
    htmlTitle: 'Generative University Research Management Platform'
  },

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
    resetPasswordDesc: 'Enter your registered email and complete the verification to reset your password.',
    welcomeBack: 'Welcome back, please log in to your account',
    noAccount: 'No account yet?',
    registerNow: 'Register now',
    rememberPassword: 'Remember password?',
    backToLogin: 'Back to Login',
    sendCode: 'Send Code',
    emailCodeSent: 'Verification code sent',
    sendCodeFailed: 'Failed to send verification code',
    resetSuccess: 'Password reset successful',
    resetFailed: 'Password reset failed',
    loginSuccess: 'Login Success',
    logoutSuccess: 'Logout Success',
    loginFailed: 'Login Failed',
    invalidCredentials: 'Invalid username or password',
    accountLocked: 'Account is locked',
    captchaRequired: 'Please enter captcha',
    captchaError: 'Invalid captcha'
  },

  // Legal
  legal: {
    agreePrefix: 'I have read and agree to the',
    and: 'and',
    userAgreement: 'User Agreement',
    privacyPolicy: 'Privacy Policy',
    userAgreementContent: `
      <h3>User Agreement</h3>
      <p>Welcome to Sci-Z Platform. This agreement is a legally binding contract between you and the platform regarding registration and use of services.</p>
      <p><strong>1. Account Registration & Security</strong>: Ensure your registration information is authentic, accurate, and complete, and update it promptly. You are responsible for safeguarding your credentials and any losses caused by incorrect information or improper custody.</p>
      <p><strong>2. Platform Usage Rules</strong>: Comply with laws and regulations. Do not upload or transmit illegal, infringing, malicious code or other harmful content, nor engage in behavior that endangers platform security or the rights of others.</p>
      <p><strong>3. Data & Privacy</strong>: We collect and use necessary information while providing services. The scope and usage follow the Privacy Policy.</p>
      <p><strong>4. Intellectual Property</strong>: All content displayed on the platform (including text, images, code, and interfaces) is protected by intellectual property laws. Unauthorized copying, modification, or distribution is prohibited.</p>
      <p><strong>5. Liability & Disclaimer</strong>: The platform is not responsible for service interruption or data loss caused by force majeure, network issues, or third parties. You bear the responsibility for losses arising from violations of laws or this agreement.</p>
      <p><strong>6. Agreement Changes & Termination</strong>: The platform may adjust this agreement based on business needs and will notify you in advance. If you disagree with the changes, you may stop using the services.</p>
      <p>Clicking "Confirm" means you have read and agree to all terms of this agreement.</p>
    `,
    privacyPolicyContent: `
      <h3>Privacy Policy</h3>
      <p>We value your personal information and privacy. This policy describes how Sci-Z Platform collects, uses, stores, and protects your personal information when you use the services.</p>
      <p><strong>1. Information Collection</strong>: To register and verify your identity, we collect the name, email, phone number, department, and other information you provide, as well as logs and device information generated during usage.</p>
      <p><strong>2. Information Use</strong>: We use your information for identity verification, feature implementation, troubleshooting, and security, without exceeding the purposes allowed by law.</p>
      <p><strong>3. Information Sharing & Disclosure</strong>: We do not share your personal information with third parties unless required by law or with your explicit consent. When sharing is necessary, we follow the principle of minimum necessity and ensure security measures.</p>
      <p><strong>4. Information Storage & Security</strong>: We implement encryption and access control to protect information and store it for the minimum period required by law. In case of a security incident, we will notify you promptly and activate contingency plans.</p>
      <p><strong>5. Your Rights</strong>: You may access, update, correct personal information, withdraw authorization, or close your account through the platform features. We will handle requests within a reasonable time according to legal requirements.</p>
      <p><strong>6. Protection of Minors</strong>: If you are a minor, read and use the services under guardian supervision.</p>
      <p><strong>7. Policy Updates</strong>: We may update this policy when necessary and will notify you via in-app messages or other methods. Continued use of the services signifies your acceptance of the updates.</p>
      <p>If you have questions about this policy, please contact us through the channels provided by the platform.</p>
    `
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
