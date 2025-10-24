export default {
  // 共通
  common: {
    confirm: '確認',
    cancel: 'キャンセル',
    save: '保存',
    delete: '削除',
    edit: '編集',
    add: '追加',
    search: '検索',
    reset: 'リセット',
    submit: '送信',
    back: '戻る',
    next: '次へ',
    previous: '前へ',
    loading: '読み込み中...',
    noData: 'データなし',
    success: '成功',
    error: 'エラー',
    warning: '警告',
    info: '情報'
  },

  // ナビゲーションメニュー
  menu: {
    dashboard: 'ダッシュボード',
    declaration: '申請管理',
    declarationList: '申請一覧',
    declarationCreate: '申請作成',
    project: 'プロジェクト管理',
    projectList: 'プロジェクト一覧',
    projectDetail: 'プロジェクト詳細',
    projectProgress: 'プロジェクト進捗',
    report: 'レポート管理',
    reportList: 'レポート一覧',
    reportGenerate: 'レポート生成',
    knowledge: 'ナレッジベース',
    knowledgeList: 'ナレッジベース一覧',
    ai: 'AIアシスタント',
    user: 'ユーザーセンター',
    userProfile: '個人情報',
    userSecurity: 'セキュリティ設定',
    system: 'システム管理',
    systemUser: 'ユーザー管理',
    systemRole: 'ロール・権限',
    systemConfig: 'システム設定',
    systemLogs: 'ログ管理'
  },

  // 認証
  auth: {
    login: 'ログイン',
    logout: 'ログアウト',
    register: '登録',
    username: 'ユーザー名',
    password: 'パスワード',
    email: 'メールアドレス',
    phone: '電話番号',
    captcha: '認証コード',
    rememberMe: 'ログイン状態を保持',
    forgotPassword: 'パスワードを忘れた',
    resetPassword: 'パスワードリセット',
    loginSuccess: 'ログイン成功',
    logoutSuccess: 'ログアウト成功',
    loginFailed: 'ログイン失敗',
    invalidCredentials: 'ユーザー名またはパスワードが間違っています',
    accountLocked: 'アカウントがロックされています',
    captchaRequired: '認証コードを入力してください',
    captchaError: '認証コードが間違っています'
  },

  // ユーザー
  user: {
    profile: '個人情報',
    security: 'セキュリティ設定',
    avatar: 'アバター',
    realName: '実名',
    department: '部署',
    industry: '業界',
    role: 'ロール',
    permissions: '権限',
    lastLogin: '最終ログイン',
    changePassword: 'パスワード変更',
    oldPassword: '現在のパスワード',
    newPassword: '新しいパスワード',
    confirmPassword: 'パスワード確認',
    passwordMismatch: 'パスワードが一致しません',
    passwordChanged: 'パスワードが正常に変更されました'
  },

  // 申請
  declaration: {
    title: '申請タイトル',
    description: '説明',
    researchDirection: '研究方向',
    researchField: '研究分野',
    status: 'ステータス',
    statusDraft: '下書き',
    statusPending: '承認待ち',
    statusApproved: '承認済み',
    statusRejected: '却下',
    workflowStatus: 'ワークフローステータス',
    createDeclaration: '申請作成',
    editDeclaration: '申請編集',
    deleteDeclaration: '申請削除',
    declarationCreated: '申請が正常に作成されました',
    declarationUpdated: '申請が正常に更新されました',
    declarationDeleted: '申請が正常に削除されました'
  },

  // プロジェクト
  project: {
    name: 'プロジェクト名',
    description: '説明',
    status: 'ステータス',
    statusPending: '開始待ち',
    statusInProgress: '進行中',
    statusCompleted: '完了',
    statusCancelled: 'キャンセル',
    startDate: '開始日',
    endDate: '終了日',
    progress: '進捗',
    members: 'メンバー',
    leader: 'リーダー',
    coreMember: 'コアメンバー',
    member: 'メンバー',
    addMember: 'メンバー追加',
    removeMember: 'メンバー削除',
    projectCreated: 'プロジェクトが正常に作成されました',
    projectUpdated: 'プロジェクトが正常に更新されました',
    projectDeleted: 'プロジェクトが正常に削除されました'
  },

  // レポート
  report: {
    title: 'レポートタイトル',
    type: 'レポートタイプ',
    typeTech: '技術レポート',
    typeSelf: '自己評価',
    status: 'ステータス',
    statusGenerating: '生成中',
    statusCompleted: '完了',
    statusFailed: '失敗',
    config: 'レポート設定',
    style: 'スタイル',
    detailLevel: '詳細レベル',
    detailBrief: '簡潔',
    detailNormal: '標準',
    detailDetailed: '詳細',
    includeCharts: 'チャートを含む',
    generateReport: 'レポート生成',
    exportReport: 'レポートエクスポート',
    reportGenerated: 'レポートが正常に生成されました',
    reportExported: 'レポートが正常にエクスポートされました'
  },

  // ナレッジベース
  knowledge: {
    name: 'ナレッジベース名',
    description: '説明',
    type: 'タイプ',
    typeProject: 'プロジェクトナレッジ',
    typeIndependent: '独立ナレッジ',
    visibility: '可視性',
    visibilityPrivate: 'プライベート',
    visibilityPublic: 'パブリック',
    syncStatus: '同期ステータス',
    syncPending: '同期待ち',
    syncSyncing: '同期中',
    syncCompleted: '同期完了',
    syncFailed: '同期失敗',
    files: 'ファイル一覧',
    uploadFile: 'ファイルアップロード',
    createKnowledge: 'ナレッジベース作成',
    editKnowledge: 'ナレッジベース編集',
    deleteKnowledge: 'ナレッジベース削除',
    knowledgeCreated: 'ナレッジベースが正常に作成されました',
    knowledgeUpdated: 'ナレッジベースが正常に更新されました',
    knowledgeDeleted: 'ナレッジベースが正常に削除されました'
  },

  // AIアシスタント
  ai: {
    chat: 'AIアシスタント',
    newChat: '新しいチャット',
    sendMessage: 'メッセージ送信',
    inputPlaceholder: '質問を入力してください...',
    thinking: 'AIが考えています...',
    sources: '知識ソース',
    confidence: '信頼度',
    noConversation: '会話履歴がありません',
    conversationCreated: '会話が正常に作成されました',
    messageSent: 'メッセージが正常に送信されました'
  },

  // システム管理
  system: {
    userManagement: 'ユーザー管理',
    roleManagement: 'ロール管理',
    permissionManagement: '権限管理',
    configManagement: '設定管理',
    logManagement: 'ログ管理',
    userName: 'ユーザー名',
    userEmail: 'メールアドレス',
    userRole: 'ロール',
    userStatus: 'ステータス',
    userActive: 'アクティブ',
    userInactive: '非アクティブ',
    roleName: 'ロール名',
    roleCode: 'ロールコード',
    roleDescription: '説明',
    permissionName: '権限名',
    permissionCode: '権限コード',
    permissionType: '権限タイプ',
    configKey: '設定キー',
    configValue: '設定値',
    configDescription: '説明',
    logType: 'ログタイプ',
    logContent: 'ログ内容',
    logTime: 'ログ時間',
    userCreated: 'ユーザーが正常に作成されました',
    userUpdated: 'ユーザーが正常に更新されました',
    userDeleted: 'ユーザーが正常に削除されました',
    roleCreated: 'ロールが正常に作成されました',
    roleUpdated: 'ロールが正常に更新されました',
    roleDeleted: 'ロールが正常に削除されました'
  },

  // ファイル
  file: {
    upload: 'ファイルアップロード',
    download: 'ファイルダウンロード',
    preview: 'ファイルプレビュー',
    delete: 'ファイル削除',
    fileName: 'ファイル名',
    fileSize: 'ファイルサイズ',
    fileType: 'ファイルタイプ',
    uploadTime: 'アップロード時間',
    uploadSuccess: 'ファイルが正常にアップロードされました',
    uploadFailed: 'ファイルアップロードに失敗しました',
    fileDeleted: 'ファイルが正常に削除されました',
    fileNotFound: 'ファイルが見つかりません',
    fileTooLarge: 'ファイルが大きすぎます',
    fileTypeNotSupported: 'サポートされていないファイルタイプです'
  },

  // 言語
  language: {
    zhCN: '中文',
    enUS: 'English',
    koKR: '한국어',
    jaJP: '日本語',
    switchLanguage: '言語切り替え'
  },

  // テーマ
  theme: {
    light: 'ライトテーマ',
    dark: 'ダークテーマ',
    switchTheme: 'テーマ切り替え'
  }
}
