export default {
  // 앱 정보
  app: {
    title: '생성형 대학 연구 관리 플랫폼',
    description: 'AIGC 기반 생성형 대학 연구 관리 플랫폼',
    htmlTitle: '생성형 대학 연구 관리 플랫폼'
  },

  // 공통
  common: {
    confirm: '확인',
    cancel: '취소',
    save: '저장',
    delete: '삭제',
    edit: '편집',
    add: '추가',
    search: '검색',
    reset: '초기화',
    submit: '제출',
    back: '뒤로',
    next: '다음',
    previous: '이전',
    loading: '로딩 중...',
    noData: '데이터 없음',
    success: '성공',
    error: '오류',
    warning: '경고',
    info: '정보'
  },

  // 네비게이션 메뉴
  menu: {
    dashboard: '대시보드',
    declaration: '신청 관리',
    declarationList: '신청 목록',
    declarationCreate: '신청 생성',
    project: '프로젝트 관리',
    projectList: '프로젝트 목록',
    projectDetail: '프로젝트 상세',
    projectProgress: '프로젝트 진행률',
    report: '보고서 관리',
    reportList: '보고서 목록',
    reportGenerate: '보고서 생성',
    knowledge: '지식베이스',
    knowledgeList: '지식베이스 목록',
    ai: 'AI 어시스턴트',
    user: '사용자 센터',
    userProfile: '개인정보',
    userSecurity: '보안 설정',
    system: '시스템 관리',
    systemUser: '사용자 관리',
    systemRole: '역할 및 권한',
    systemConfig: '시스템 설정',
    systemLogs: '로그 관리'
  },

  // 인증
  auth: {
    login: '로그인',
    logout: '로그아웃',
    register: '회원가입',
    username: '사용자명',
    password: '비밀번호',
    email: '이메일',
    phone: '전화번호',
    captcha: '인증코드',
    rememberMe: '로그인 상태 유지',
    forgotPassword: '비밀번호 찾기',
    resetPassword: '비밀번호 재설정',
    resetPasswordDesc: '등록된 이메일을 입력하고 이중 인증을 완료하면 비밀번호를 재설정할 수 있습니다.',
    welcomeBack: '다시 오신 것을 환영합니다, 계정에 로그인해주세요',
    noAccount: '아직 계정이 없으신가요?',
    registerNow: '지금 가입하기',
    rememberPassword: '비밀번호를 기억하셨나요?',
    backToLogin: '로그인으로 돌아가기',
    sendCode: '코드 전송',
    emailCodeSent: '인증코드가 전송되었습니다',
    sendCodeFailed: '인증코드 전송에 실패했습니다',
    resetSuccess: '비밀번호 재설정이 성공했습니다',
    resetFailed: '비밀번호 재설정에 실패했습니다',
    loginSuccess: '로그인 성공',
    logoutSuccess: '로그아웃 성공',
    loginFailed: '로그인 실패',
    invalidCredentials: '사용자명 또는 비밀번호가 잘못되었습니다',
    accountLocked: '계정이 잠겼습니다',
    captchaRequired: '인증코드를 입력해주세요',
    captchaError: '인증코드가 잘못되었습니다'
  },

  // 법률 관련
  legal: {
    agreePrefix: '다음 약관에 동의합니다',
    and: '및',
    userAgreement: '《이용약관》',
    privacyPolicy: '《개인정보 처리방침》',
    userAgreementContent: `
      <h3>이용약관</h3>
      <p>Sci-Z Platform을 이용해 주셔서 감사합니다. 본 약관은 플랫폼에서의 회원가입 및 서비스 이용과 관련하여 이용자와 Sci-Z Platform 간의 법적 효력을 갖는 계약입니다.</p>
      <p><strong>1. 계정 등록과 보안</strong>: 등록 정보는 정확하고 최신으로 유지해야 하며, 인증 정보는 직접 안전하게 관리해야 합니다. 부정확한 정보나 부주의로 인한 손실은 이용자에게 책임이 있습니다.</p>
      <p><strong>2. 플랫폼 이용 규칙</strong>: 관련 법령을 준수하고, 불법·권리 침해 콘텐츠나 악성 코드를 업로드하거나 배포하지 말아야 합니다. 플랫폼 보안 또는 타인의 권리를 침해하는 행위는 금지됩니다.</p>
      <p><strong>3. 데이터와 개인정보</strong>: 서비스 제공 과정에서 필요한 정보를 수집·이용하며, 구체적인 범위와 사용 방식은 「개인정보 처리방침」을 따릅니다.</p>
      <p><strong>4. 지식재산권</strong>: 플랫폼에 게시된 모든 콘텐츠(텍스트, 이미지, 코드, 인터페이스 등)는 지식재산권으로 보호됩니다. 무단 복제·수정·배포는 금지됩니다.</p>
      <p><strong>5. 책임과 면책</strong>: 천재지변, 네트워크 문제, 제3자 사유로 발생한 서비스 중단이나 데이터 손실에 대해 플랫폼은 책임을 지지 않습니다. 법령이나 본 약관을 위반하여 발생한 손해는 이용자 책임입니다.</p>
      <p><strong>6. 약관 변경과 종료</strong>: 플랫폼은 업무상 필요에 따라 약관을 변경할 수 있으며, 사전에 공지합니다. 변경에 동의하지 않을 경우 서비스 이용을 중단할 수 있습니다.</p>
      <p>"확인"을 클릭하면 본 약관의 모든 내용을 읽고 동의한 것으로 간주됩니다.</p>
    `,
    privacyPolicyContent: `
      <h3>개인정보 처리방침</h3>
      <p>우리는 이용자의 개인정보와 프라이버시 보호를 중요하게 생각합니다. 본 방침은 Sci-Z Platform이 서비스를 제공하는 과정에서 개인정보를 어떻게 수집·이용·보관·보호하는지를 설명합니다.</p>
      <p><strong>1. 정보 수집</strong>: 회원가입 및 본인 확인을 위해 이름, 이메일, 전화번호, 소속 등 이용자가 제공한 정보를 수집하며, 이용 과정에서 생성되는 로그와 기기 정보도 포함됩니다.</p>
      <p><strong>2. 정보 이용</strong>: 본인 확인, 기능 제공, 장애 대응, 보안 강화 등을 위해 정보를 사용하며, 법령이 허용하는 범위를 넘어 이용하지 않습니다.</p>
      <p><strong>3. 정보 공유 및 공개</strong>: 법령에 의한 요구 또는 이용자의 명시적 동의가 없는 한 개인정보를 제3자에게 공유하지 않습니다. 부득이하게 공유해야 하는 경우 최소한의 범위로 제한하고 적절한 보안 조치를 취합니다.</p>
      <p><strong>4. 정보 보관과 보안</strong>: 암호화와 접근 제어 등 보안 조치를 통해 정보를 보호하며, 법령이 정한 최소 기간 동안만 보관합니다. 보안 사고가 발생하면 즉시 통지하고 대응 절차를 시행합니다.</p>
      <p><strong>5. 이용자의 권리</strong>: 플랫폼 기능을 통해 개인정보 열람, 수정, 정정, 동의 철회, 계정 삭제 등을 요청할 수 있으며, 법령이 정한 기간 내에 처리합니다.</p>
      <p><strong>6. 미성년자 보호</strong>: 미성년자는 보호자의 동의 및 지도하에 서비스를 이용해야 합니다.</p>
      <p><strong>7. 방침 변경</strong>: 필요에 따라 본 방침을 업데이트하며, 중요한 변경 사항은 사전에 안내합니다. 서비스를 계속 이용하면 변경 사항에 동의한 것으로 간주됩니다.</p>
      <p>본 방침과 관련한 문의는 플랫폼에 안내된 연락처로 문의해 주시기 바랍니다.</p>
    `
  },

  // 사용자
  user: {
    profile: '개인정보',
    security: '보안 설정',
    avatar: '아바타',
    realName: '실명',
    department: '부서',
    industry: '업종',
    role: '역할',
    permissions: '권한',
    lastLogin: '마지막 로그인',
    changePassword: '비밀번호 변경',
    oldPassword: '기존 비밀번호',
    newPassword: '새 비밀번호',
    confirmPassword: '비밀번호 확인',
    passwordMismatch: '비밀번호가 일치하지 않습니다',
    passwordChanged: '비밀번호가 성공적으로 변경되었습니다'
  },

  // 신청
  declaration: {
    title: '신청 제목',
    description: '설명',
    researchDirection: '연구 방향',
    researchField: '연구 분야',
    status: '상태',
    statusDraft: '초안',
    statusPending: '대기 중',
    statusApproved: '승인됨',
    statusRejected: '거부됨',
    workflowStatus: '워크플로우 상태',
    createDeclaration: '신청 생성',
    editDeclaration: '신청 편집',
    deleteDeclaration: '신청 삭제',
    declarationCreated: '신청이 성공적으로 생성되었습니다',
    declarationUpdated: '신청이 성공적으로 업데이트되었습니다',
    declarationDeleted: '신청이 성공적으로 삭제되었습니다'
  },

  // 프로젝트
  project: {
    name: '프로젝트명',
    description: '설명',
    status: '상태',
    statusPending: '대기 중',
    statusInProgress: '진행 중',
    statusCompleted: '완료됨',
    statusCancelled: '취소됨',
    startDate: '시작일',
    endDate: '종료일',
    progress: '진행률',
    members: '멤버',
    leader: '리더',
    coreMember: '핵심 멤버',
    member: '멤버',
    addMember: '멤버 추가',
    removeMember: '멤버 제거',
    projectCreated: '프로젝트가 성공적으로 생성되었습니다',
    projectUpdated: '프로젝트가 성공적으로 업데이트되었습니다',
    projectDeleted: '프로젝트가 성공적으로 삭제되었습니다'
  },

  // 보고서
  report: {
    title: '보고서 제목',
    type: '보고서 유형',
    typeTech: '기술 보고서',
    typeSelf: '자체 평가',
    status: '상태',
    statusGenerating: '생성 중',
    statusCompleted: '완료됨',
    statusFailed: '실패',
    config: '보고서 설정',
    style: '스타일',
    detailLevel: '상세 수준',
    detailBrief: '간략',
    detailNormal: '일반',
    detailDetailed: '상세',
    includeCharts: '차트 포함',
    generateReport: '보고서 생성',
    exportReport: '보고서 내보내기',
    reportGenerated: '보고서가 성공적으로 생성되었습니다',
    reportExported: '보고서가 성공적으로 내보내기되었습니다'
  },

  // 지식베이스
  knowledge: {
    name: '지식베이스명',
    description: '설명',
    type: '유형',
    typeProject: '프로젝트 지식',
    typeIndependent: '독립 지식',
    visibility: '가시성',
    visibilityPrivate: '비공개',
    visibilityPublic: '공개',
    syncStatus: '동기화 상태',
    syncPending: '대기 중',
    syncSyncing: '동기화 중',
    syncCompleted: '완료됨',
    syncFailed: '실패',
    files: '파일 목록',
    uploadFile: '파일 업로드',
    createKnowledge: '지식베이스 생성',
    editKnowledge: '지식베이스 편집',
    deleteKnowledge: '지식베이스 삭제',
    knowledgeCreated: '지식베이스가 성공적으로 생성되었습니다',
    knowledgeUpdated: '지식베이스가 성공적으로 업데이트되었습니다',
    knowledgeDeleted: '지식베이스가 성공적으로 삭제되었습니다'
  },

  // AI 어시스턴트
  ai: {
    chat: 'AI 어시스턴트',
    newChat: '새 대화',
    sendMessage: '메시지 보내기',
    inputPlaceholder: '질문을 입력해주세요...',
    thinking: 'AI가 생각 중입니다...',
    sources: '지식 출처',
    confidence: '신뢰도',
    noConversation: '대화 기록이 없습니다',
    conversationCreated: '대화가 성공적으로 생성되었습니다',
    messageSent: '메시지가 성공적으로 전송되었습니다'
  },

  // 시스템 관리
  system: {
    userManagement: '사용자 관리',
    roleManagement: '역할 관리',
    permissionManagement: '권한 관리',
    configManagement: '설정 관리',
    logManagement: '로그 관리',
    userName: '사용자명',
    userEmail: '이메일',
    userRole: '역할',
    userStatus: '상태',
    userActive: '활성',
    userInactive: '비활성',
    roleName: '역할명',
    roleCode: '역할 코드',
    roleDescription: '설명',
    permissionName: '권한명',
    permissionCode: '권한 코드',
    permissionType: '권한 유형',
    configKey: '설정 키',
    configValue: '설정 값',
    configDescription: '설명',
    logType: '로그 유형',
    logContent: '로그 내용',
    logTime: '로그 시간',
    userCreated: '사용자가 성공적으로 생성되었습니다',
    userUpdated: '사용자가 성공적으로 업데이트되었습니다',
    userDeleted: '사용자가 성공적으로 삭제되었습니다',
    roleCreated: '역할이 성공적으로 생성되었습니다',
    roleUpdated: '역할이 성공적으로 업데이트되었습니다',
    roleDeleted: '역할이 성공적으로 삭제되었습니다'
  },

  // 파일
  file: {
    upload: '파일 업로드',
    download: '파일 다운로드',
    preview: '파일 미리보기',
    delete: '파일 삭제',
    fileName: '파일명',
    fileSize: '파일 크기',
    fileType: '파일 유형',
    uploadTime: '업로드 시간',
    uploadSuccess: '파일이 성공적으로 업로드되었습니다',
    uploadFailed: '파일 업로드에 실패했습니다',
    fileDeleted: '파일이 성공적으로 삭제되었습니다',
    fileNotFound: '파일을 찾을 수 없습니다',
    fileTooLarge: '파일이 너무 큽니다',
    fileTypeNotSupported: '지원하지 않는 파일 유형입니다'
  },

  // 언어
  language: {
    zhCN: '中文',
    enUS: 'English',
    koKR: '한국어',
    jaJP: '日本語',
    switchLanguage: '언어 변경'
  },

  // 테마
  theme: {
    light: '라이트 테마',
    dark: '다크 테마',
    switchTheme: '테마 변경'
  }
}
