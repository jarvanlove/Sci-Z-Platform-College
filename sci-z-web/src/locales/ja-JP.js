export default {
  // アプリ情報
  app: {
    title: '生成式大学研究管理プラットフォーム',
    description: 'AIGCベースの生成式大学研究管理プラットフォーム',
    htmlTitle: '生成式大学研究管理プラットフォーム'
  },

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
    resetPasswordDesc: '登録したメールアドレスを入力し、二重認証を完了するとパスワードをリセットできます',
    welcomeBack: 'おかえりなさい、アカウントにログインしてください',
    noAccount: 'アカウントをお持ちでない方は？',
    registerNow: '今すぐ登録',
    rememberPassword: 'パスワードを思い出しましたか？',
    backToLogin: 'ログインに戻る',
    sendCode: 'コードを送信',
    emailCodeSent: '認証コードを送信しました',
    sendCodeFailed: '認証コードの送信に失敗しました',
    resetSuccess: 'パスワードリセットが成功しました',
    resetFailed: 'パスワードリセットに失敗しました',
    loginSuccess: 'ログイン成功',
    logoutSuccess: 'ログアウト成功',
    loginFailed: 'ログイン失敗',
    invalidCredentials: 'ユーザー名またはパスワードが間違っています',
    accountLocked: 'アカウントがロックされています',
    captchaRequired: '認証コードを入力してください',
    captchaError: '認証コードが間違っています'
  },

  // 法務関連
  legal: {
    agreePrefix: '以下の内容に同意します',
    and: 'および',
    userAgreement: '「ユーザー規約」',
    privacyPolicy: '「プライバシーポリシー」',
    userAgreementContent: `
      <h3>ユーザー規約</h3>
      <p>Sci-Z Platform をご利用いただきありがとうございます。本規約は、プラットフォームでの登録およびサービス利用に関する利用者と Sci-Z Platform との間の法的拘束力を持つ契約です。</p>
      <p><strong>1. アカウント登録と安全</strong>：登録情報は正確かつ最新の状態に保ち、認証情報はご自身で適切に管理してください。不備や管理不足による損失は利用者の責任となります。</p>
      <p><strong>2. プラットフォーム利用ルール</strong>：法律・規則を遵守し、違法または有害なコンテンツや悪意あるコードをアップロード・配布しないでください。プラットフォームや他者の権利を侵害する行為は禁止されています。</p>
      <p><strong>3. データとプライバシー</strong>：サービス提供に必要な情報を収集・利用します。詳細は「プライバシーポリシー」に従います。</p>
      <p><strong>4. 知的財産権</strong>：プラットフォーム上のコンテンツ（テキスト、画像、コード、UI等）は知的財産権で保護されています。無断での複製・改変・配布は禁止されています。</p>
      <p><strong>5. 免責事項</strong>：不可抗力やネットワーク障害、第三者の理由によるサービス停止やデータ損失について、プラットフォームは責任を負いません。利用者の法令違反による損害は利用者の責任となります。</p>
      <p><strong>6. 規約の変更と終了</strong>：業務上の必要に応じて規約を変更する場合があります。事前に通知しますので、同意できない場合はサービスの利用を停止できます。</p>
      <p>「確認」をクリックすると、本規約のすべての条項に同意したものとみなされます。</p>
    `,
    privacyPolicyContent: `
      <h3>プライバシーポリシー</h3>
      <p>当社はお客様の個人情報とプライバシー保護を重視しています。本ポリシーでは、Sci-Z Platform がサービス提供時に個人情報をどのように収集・利用・保管し、保護するかを説明します。</p>
      <p><strong>1. 情報収集</strong>：登録および本人確認のため、氏名・メールアドレス・電話番号・所属などの情報を収集します。また、利用中に生成されるログやデバイス情報も含まれます。</p>
      <p><strong>2. 情報利用</strong>：本人確認、機能提供、障害対応、安全確保などの目的で情報を利用します。利用目的を超えて使用することはありません。</p>
      <p><strong>3. 情報の共有・開示</strong>：法令で求められる場合や利用者の明示的な同意がある場合を除き、第三者へ個人情報を共有しません。共有が必要な場合は最小限の範囲で適切な安全対策を講じます。</p>
      <p><strong>4. 情報の保管と安全</strong>：暗号化やアクセス制御などの手段で情報を保護し、法令で定められた最短期間のみ保管します。安全上の問題が発生した場合は速やかに通知し、対策を実施します。</p>
      <p><strong>5. 利用者の権利</strong>：プラットフォームの機能を通じて個人情報の閲覧・更新・訂正、権限の取り消し、アカウントの削除などが可能です。法令で定められた期間内に対応します。</p>
      <p><strong>6. 未成年者の保護</strong>：未成年の方は保護者の同意のもとでご利用ください。</p>
      <p><strong>7. ポリシーの更新</strong>：必要に応じて本ポリシーを更新し、重要な変更は通知します。サービスを継続利用することで、更新内容に同意したものとみなされます。</p>
      <p>本ポリシーに関するお問い合わせは、プラットフォームに記載された連絡方法をご利用ください。</p>
    `
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
