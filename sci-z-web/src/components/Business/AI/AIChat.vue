<!--
/**
 * @description AI对话页面
 * 与AI助手进行对话交互，支持知识库增强对话
 */
-->
<template>
  <div class="ai-chat-container" ref="containerRef">
    <!-- 对话内容区域（全宽，因为对话列表已在侧边栏） -->
    <div class="chat-content">
      <!-- 未登录提示（仅在未登录且用户尝试操作时显示，初始加载时不显示） -->
      <!-- 已隐藏：初始页面加载时不显示提示 -->

      <!-- 消息区域（可滚动） -->
      <div class="chat-messages-wrapper">
        <!-- 对话界面（无论是否登录都显示） -->
        <div v-if="!currentChat" class="empty-state">
          <div class="empty-greeting">
            <span class="greeting-text">{{ $t('ai.chat.greeting') }}</span>
            <img src="@/assets/images/logo_sciz.svg" alt="Sci-Z Platform" class="greeting-logo" />
          </div>
          
          <!-- 空状态时，输入框居中显示 -->
          <div class="empty-state-input">
            <div class="kb-input-container">
              <!-- 附件预览区域 -->
              <div v-if="attachments.length > 0" class="attachment-preview">
                <div
                  v-for="(attachment, index) in attachments"
                  :key="index"
                  class="attachment-item"
                  :class="{ 'knowledge-file': attachment.type === 'knowledge' }"
                >
                  <span class="attachment-icon">{{ getFileIcon(attachment.type) }}</span>
                  <div class="attachment-info">
                    <div class="attachment-name">{{ attachment.name }}</div>
                    <div v-if="attachment.type === 'knowledge'" class="attachment-source">
                      来自：{{ attachment.knowledgeName }}
                    </div>
                  </div>
                  <div class="attachment-size">{{ attachment.size }}</div>
                  <div
                    class="attachment-remove"
                    @click="removeAttachment(index)"
                  >
                    <el-icon><Close /></el-icon>
                  </div>
                </div>
              </div>

              <textarea
                v-model="inputMessage"
                class="kb-message-input"
                :placeholder="inputPlaceholder"
                @keydown.enter.exact.prevent="handleEnterKey"
                @keydown.up.prevent="navigateKbList('up')"
                @keydown.down.prevent="navigateKbList('down')"
                @keydown.escape="hideKnowledgeBaseList"
                @keydown.backspace="handleBackspace"
                @input="handleInputChange"
                @focus="handleInputFocus"
                ref="messageInput"
              ></textarea>

              <!-- 知识库选择下拉框 -->
              <div
                v-if="showKnowledgeBaseList"
                class="knowledge-base-dropdown"
              >
                <div class="kb-dropdown-header">
                  <span class="kb-dropdown-title">{{ $t('knowledge.sharedKnowledgeBase') }}</span>
                </div>
                <!-- 🔥 新增：搜索框 -->
                <div class="kb-dropdown-search">
                  <span class="search-left-icon">
                    <el-icon><Search /></el-icon>
                  </span>
                  <input
                    class="form-input"
                    v-model="kbSearchQuery"
                    :placeholder="$t('knowledge.searchPlaceholder')"
                    style="width: 100%; padding: 10px 40px 10px 36px"
                    @input="handleKbSearchInput"
                  />
                </div>
                <!-- 🔥 修改：添加滚动监听和加载更多 -->
                <div 
                  class="kb-dropdown-section"
                  @scroll="handleKbDropdownScroll"
                  ref="kbDropdownSectionRef"
                >
                  <div class="kb-list">
                    <div
                      v-for="(kb, index) in filteredKnowledgeBases"
                      :key="kb.id"
                      class="kb-item"
                      :class="{ selected: selectedKbIndex === index }"
                      @click="selectKnowledgeBase(kb)"
                    >
                      <!-- 🔥 新增：显示知识库封面 -->
                      <div class="kb-item-icon">
                        <img
                          v-if="getKbCoverUrl(kb)"
                          :src="getKbCoverUrl(kb)"
                          alt="cover"
                          class="kb-cover-img"
                        />
                        <div v-else class="kb-item-icon-default">
                          <el-icon><Collection /></el-icon>
                        </div>
                      </div>
                      <div class="kb-name">{{ kb.name }}</div>
                      <div v-if="isKbSelected(kb.id)" class="kb-selected-mark">✓</div>
                    </div>
                    <!-- 🔥 新增：加载更多提示 -->
                    <div v-if="kbListPagination.loading" class="kb-loading-more">
                      {{ $t('common.loading') }}...
                    </div>
                    <div v-else-if="!kbListPagination.hasMore && knowledgeBaseList.length > 0" class="kb-no-more">
                      {{ $t('common.noMoreData') }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 底部控制栏 -->
              <div class="kb-input-bottom-bar">
                <!-- 模型选择 -->
                <div
                  class="custom-model-selector"
                  @click.stop="toggleModelDropdown"
                >
                  <div class="model-display">
                    {{ getSelectedModelName(selectedModel) }}
                    <el-icon><ArrowDown /></el-icon>
                  </div>
                  <div v-if="showModelDropdown" class="model-dropdown">
                    <div
                      v-for="model in modelOptions"
                      :key="model.value"
                      class="model-option"
                      :class="{ active: selectedModel === model.value }"
                      @click.stop="selectModel(model.value)"
                    >
                      <div class="model-name">{{ model.name }}</div>
                      <div class="model-desc">{{ model.description }}</div>
                      <div
                        v-if="selectedModel === model.value"
                        class="check-icon"
                      >
                        ✓
                      </div>
                    </div>
                  </div>
                </div>

                <div class="kb-right-controls">
                  <!-- 联网搜索开关 -->
                  <el-tooltip
                    placement="top"
                    effect="light"
                    :show-after="300"
                    :hide-after="0"
                    trigger="hover"
                  >
                    <template #content>
                      <div class="attachment-tooltip-content">
                        {{ enableSearch ? '已开启联网搜索' : '已关闭联网搜索' }}
                      </div>
                    </template>
                    <button
                      class="kb-enable-search-btn"
                      :class="{ active: enableSearch }"
                      @click.stop="toggleEnableSearch"
                    >
                      <img
                        :src="enableSearch ? iconSearchOn : iconSearchOff"
                        alt="联网搜索"
                        class="search-icon"
                      />
                    </button>
                  </el-tooltip>

                  <!-- 附件按钮（和历史对话一样的下拉菜单形式和悬浮主题） -->
                  <el-tooltip
                    placement="top"
                    effect="light"
                    :show-after="300"
                    :hide-after="0"
                    :disabled="showAttachmentDropdown"
                    trigger="hover"
                  >
                    <template #content>
                      <div class="attachment-tooltip-content">
                        <div class="tooltip-item">• {{ $t('ai.chat.attachmentTooltip.supportUpload') }}</div>
                        <div class="tooltip-item">• {{ $t('ai.chat.attachmentTooltip.maxFiles') }}</div>
                        <div class="tooltip-item">• {{ $t('ai.chat.attachmentTooltip.fileTypes') }}</div>
                        <div class="tooltip-item">• {{ $t('ai.chat.attachmentTooltip.maxKbFiles') }}</div>
                      </div>
                    </template>
                    <el-dropdown
                      trigger="click"
                      @command="handleAttachmentCommand"
                      @visible-change="handleAttachmentDropdownVisible"
                      placement="top-end"
                    >
                      <button
                        class="kb-attachment-btn"
                      >
                        <el-icon><Paperclip /></el-icon>
                      </button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="local">
                            <el-icon><Folder /></el-icon>
                            <span style="margin-left: 8px">{{ $t('ai.chat.attachmentLocalFile') }}</span>
                          </el-dropdown-item>
                          <el-dropdown-item command="knowledge">
                            <el-icon><Document /></el-icon>
                            <span style="margin-left: 8px">{{ $t('ai.chat.attachmentKnowledgeFile') }}</span>
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </el-tooltip>

                  <!-- 隐藏的文件输入 -->
                  <input
                    ref="fileInput"
                    type="file"
                    multiple
                    accept=".pdf,.doc,.docx,.xlsx,.ppt,.pptx,.txt,.md,.jpg,.jpeg,.png"
                    style="display: none"
                    @change="handleFileUpload"
                  />

                  <!-- 发送/停止按钮 -->
                  <button
                    v-if="!isGenerating"
                    class="kb-send-btn"
                    :class="{ active: inputMessage.trim() || selectedKnowledgeBases.length > 0 }"
                    :disabled="isSendingMessage || isGenerating"
                    @click.stop.prevent="() => { console.log('[按钮点击] 发送按钮被点击'); sendKbMessage(); }"
                  >
                    <el-icon><ArrowUp /></el-icon>
                  </button>

                  <!-- 停止按钮 -->
                  <button
                    v-if="isGenerating"
                    class="kb-stop-btn"
                    @click="stopGeneration"
                  >
                    <el-icon><Close /></el-icon>
                  </button>
                </div>
              </div>
            </div>
            <div class="input-footer">{{ $t('ai.chat.aiContentHint') }}</div>
          </div>
        </div>

        <template v-else>
          <!-- 对话头部 -->
          <div class="chat-header">
            <div class="chat-title-header">{{ currentChat.title || $t('ai.chat.newChat') }}</div>
          </div>

          <!-- 对话区 -->
          <div
            class="kb-messages-container"
            :class="{ 'has-messages': uniqueMessages.length > 0 }"
            ref="kbMessages"
            @scroll="handleMessagesScroll"
          >
            <!-- 空状态显示 -->
            <div v-if="uniqueMessages.length === 0" class="empty-chat-message">
              {{ $t('ai.chat.welcomeMessage') }}
            </div>

            <!-- 消息列表 -->
            <template v-if="uniqueMessages.length > 0">
              <div class="kb-messages">
                <div
                  v-for="(msg, index) in uniqueMessages"
                  :key="msg.id"
                  class="kb-message"
                  :class="[
                    msg.type,
                    {
                      'is-last-ai-message': msg.type === 'ai' && isLastAiMessage(msg, index, uniqueMessages)
                    }
                  ]"
                  :data-message-id="msg.id"
                >
                  <div class="kb-message-bubble">
                    <!-- 🔥 修复：正常显示状态和编辑状态统一在一个容器中 -->
                    <div class="kb-message-content-wrapper" :class="{ 'is-editing': msg.editing && msg.type === 'user' }">
                      <!-- 正常显示状态 -->
                      <div v-if="!msg.editing || msg.type !== 'user'" class="kb-message-content-display">
                        <div
                          class="kb-message-content"
                          v-html="formatKbContent(msg.content)"
                        ></div>
                        <!-- 文档片段展示 -->
                        <div v-if="msg.documents && msg.documents.length > 0" class="kb-message-documents">
                          <div class="kb-documents-title">{{ $t('ai.chat.referenceDocuments') }}</div>
                          <div class="kb-documents-list">
                            <div
                              v-for="(doc, index) in msg.documents"
                              :key="doc.id || index"
                              class="kb-document-item"
                              :title="doc.content ? doc.content.substring(0, 100) : ''"
                            >
                              <el-icon class="kb-document-icon"><Document /></el-icon>
                              <div class="kb-document-info">
                                <span class="kb-document-name">{{ doc.document_name || doc.name || $t('ai.chat.documentNumber', { index: index + 1 }) }}</span>
                                <span v-if="(doc.dataset_name || doc.datasetName) && (doc.dataset_name || doc.datasetName) !== (doc.document_name || doc.name)" class="kb-document-dataset">（{{ doc.dataset_name || doc.datasetName }}）</span>
                              </div>
                              <span v-if="doc.score" class="kb-document-score">{{ $t('ai.chat.relevance') }} {{ (doc.score * 100).toFixed(0) }}%</span>
                            </div>
                          </div>
                        </div>
                        <!-- 流式生成指示器 -->
                        <div v-if="msg.streaming" class="kb-streaming-indicator">
                          <span class="kb-streaming-dot"></span>
                          <span class="kb-streaming-text">{{ $t('ai.chat.generating') }}</span>
                        </div>
                      </div>
                      
                      <!-- 🔥 修复：编辑状态 - 参考千问设计，可变高度的编辑框 -->
                      <div v-if="msg.editing && msg.type === 'user'" class="kb-message-content-edit">
                        <div class="kb-edit-box">
                          <div
                            class="kb-edit-content"
                            contenteditable="true"
                            @input="(e) => { 
                              msg.editContent = e.target.innerText || e.target.textContent;
                              autoResizeEditBox(e.target);
                            }"
                            @keydown.enter.ctrl.prevent="confirmEditUserMessage(msg)"
                            @keydown.escape.prevent="cancelEditUserMessage(msg)"
                            ref="editContentRef"
                          ></div>
                          <!-- 编辑操作按钮 - 显示在框内右下角 -->
                          <div class="kb-edit-actions-inline">
                            <button
                              class="kb-edit-btn-cancel"
                              @click="cancelEditUserMessage(msg)"
                            >
                              {{ $t('common.cancel') }}
                            </button>
                            <button
                              class="kb-edit-btn-confirm"
                              :disabled="!msg.editContent || !msg.editContent.trim()"
                              @click="confirmEditUserMessage(msg)"
                            >
                              {{ $t('common.confirm') }}
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <div class="kb-message-meta">
                      <div class="kb-message-actions">
                        <!-- AI消息的复制和重试按钮 -->
                        <template v-if="msg.type === 'ai'">
                          <button
                            class="kb-copy-btn"
                            @click="copyKbMessage(msg.content)"
                          >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-right: 4px;">
                              <rect x="9" y="9" width="13" height="13" rx="2" ry="2" stroke="currentColor" stroke-width="2" fill="none"/>
                              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" stroke="currentColor" stroke-width="2" fill="none"/>
                            </svg>
                            <span>{{ $t('ai.chat.copy') }}</span>
                          </button>
                          <button
                            class="kb-retry-btn"
                            @click="retryKbMessage(msg)"
                          >
                            <el-icon><Refresh /></el-icon>
                            <span>{{ $t('ai.chat.regenerate') }}</span>
                          </button>
                        </template>

                        <!-- 用户消息的编辑和复制按钮 -->
                        <template v-if="msg.type === 'user' && !msg.editing">
                          <button
                            class="kb-edit-btn"
                            @click="startEditUserMessage(msg)"
                          >
                            <el-icon><EditPen /></el-icon>
                            <span>{{ $t('ai.chat.edit') }}</span>
                          </button>
                          <button
                            class="kb-copy-btn"
                            @click="copyKbMessage(msg.content)"
                          >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-right: 4px;">
                              <rect x="9" y="9" width="13" height="13" rx="2" ry="2" stroke="currentColor" stroke-width="2" fill="none"/>
                              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" stroke="currentColor" stroke-width="2" fill="none"/>
                            </svg>
                            <span>{{ $t('ai.chat.copy') }}</span>
                          </button>
                        </template>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </template>
      </div>

      <!-- 输入区域（有对话时显示在底部） -->
      <div v-if="currentChat" class="kb-input-area">
          <div class="kb-input-container">
            <!-- 附件预览区域 -->
            <div v-if="attachments.length > 0" class="attachment-preview">
              <div
                v-for="(attachment, index) in attachments"
                :key="index"
                class="attachment-item"
                :class="{ 'knowledge-file': attachment.type === 'knowledge' }"
              >
                <span class="attachment-icon">{{ getFileIcon(attachment.type) }}</span>
                <div class="attachment-info">
                  <div class="attachment-name">{{ attachment.name }}</div>
                  <div v-if="attachment.type === 'knowledge'" class="attachment-source">
                    来自：{{ attachment.knowledgeName }}
                  </div>
                </div>
                <div class="attachment-size">{{ attachment.size }}</div>
                <div
                  class="attachment-remove"
                  @click="removeAttachment(index)"
                >
                  <el-icon><Close /></el-icon>
                </div>
              </div>
            </div>

            <textarea
              v-model="inputMessage"
              class="kb-message-input"
              :placeholder="inputPlaceholder"
              @keydown.enter.exact.prevent="handleEnterKey"
              @keydown.up.prevent="navigateKbList('up')"
              @keydown.down.prevent="navigateKbList('down')"
              @keydown.escape="hideKnowledgeBaseList"
              @keydown.backspace="handleBackspace"
              @input="handleInputChange"
              @focus="handleInputFocus"
              ref="messageInput"
            ></textarea>

            <!-- 知识库选择下拉框 -->
            <div
              v-if="showKnowledgeBaseList"
              class="knowledge-base-dropdown"
            >
              <div class="kb-dropdown-header">
                <span class="kb-dropdown-title">{{ $t('knowledge.sharedKnowledgeBase') }}</span>
              </div>
              <!-- 🔥 新增：搜索框 -->
              <div class="kb-dropdown-search">
                <span class="search-left-icon">
                  <el-icon><Search /></el-icon>
                </span>
                <input
                  class="form-input"
                  v-model="kbSearchQuery"
                  :placeholder="$t('knowledge.searchPlaceholder')"
                  style="width: 100%; padding: 10px 40px 10px 36px"
                  @input="handleKbSearchInput"
                />
              </div>
              <!-- 🔥 修改：添加滚动监听和加载更多 -->
              <div 
                class="kb-dropdown-section"
                @scroll="handleKbDropdownScroll"
                ref="kbDropdownSectionRef2"
              >
                <div class="kb-list">
                  <div
                    v-for="(kb, index) in filteredKnowledgeBases"
                    :key="kb.id"
                    class="kb-item"
                    :class="{ selected: selectedKbIndex === index }"
                    @click="selectKnowledgeBase(kb)"
                  >
                    <!-- 🔥 新增：显示知识库封面 -->
                    <div class="kb-item-icon">
                      <img
                        v-if="getKbCoverUrl(kb)"
                        :src="getKbCoverUrl(kb)"
                        alt="cover"
                        class="kb-cover-img"
                      />
                      <div v-else class="kb-item-icon-default">
                        <el-icon><Collection /></el-icon>
                      </div>
                    </div>
                    <div class="kb-name">{{ kb.name }}</div>
                    <div v-if="isKbSelected(kb.id)" class="kb-selected-mark">✓</div>
                  </div>
                  <!-- 🔥 新增：加载更多提示 -->
                  <div v-if="kbListPagination.loading" class="kb-loading-more">
                    {{ $t('common.loading') }}...
                  </div>
                  <div v-else-if="!kbListPagination.hasMore && knowledgeBaseList.length > 0" class="kb-no-more">
                    {{ $t('common.noMoreData') }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 底部控制栏 -->
            <div class="kb-input-bottom-bar">
              <!-- 模型选择 -->
              <div
                class="custom-model-selector"
                @click.stop="toggleModelDropdown"
              >
                <div class="model-display">
                  {{ getSelectedModelName(selectedModel) }}
                  <el-icon><ArrowDown /></el-icon>
                </div>
                <div v-if="showModelDropdown" class="model-dropdown">
                  <div
                    v-for="model in modelOptions"
                    :key="model.value"
                    class="model-option"
                    :class="{ active: selectedModel === model.value }"
                    @click.stop="selectModel(model.value)"
                  >
                    <div class="model-name">{{ model.name }}</div>
                    <div class="model-desc">{{ model.description }}</div>
                    <div
                      v-if="selectedModel === model.value"
                      class="check-icon"
                    >
                      ✓
                    </div>
                  </div>
                </div>
              </div>

              <div class="kb-right-controls">
                <!-- 联网搜索开关 -->
                <el-tooltip
                  placement="top"
                  effect="light"
                  :show-after="300"
                  :hide-after="0"
                  trigger="hover"
                >
                  <template #content>
                    <div class="attachment-tooltip-content">
                      {{ enableSearch ? '已开启联网搜索' : '已关闭联网搜索' }}
                    </div>
                  </template>
                  <button
                    class="kb-enable-search-btn"
                    :class="{ active: enableSearch }"
                    @click.stop="toggleEnableSearch"
                  >
                    <img
                      :src="enableSearch ? iconSearchOn : iconSearchOff"
                      alt="联网搜索"
                      class="search-icon"
                    />
                  </button>
                </el-tooltip>

                <!-- 附件按钮 -->
                <el-tooltip
                  placement="top"
                  effect="light"
                  :show-after="300"
                  :hide-after="0"
                  :disabled="showAttachmentDropdown"
                  trigger="hover"
                >
                  <template #content>
                    <div class="attachment-tooltip-content">
                      <div class="tooltip-item">• {{ $t('ai.chat.attachmentTooltip.supportUpload') }}</div>
                      <div class="tooltip-item">• {{ $t('ai.chat.attachmentTooltip.maxFiles') }}</div>
                      <div class="tooltip-item">• {{ $t('ai.chat.attachmentTooltip.fileTypes') }}</div>
                      <div class="tooltip-item">• {{ $t('ai.chat.attachmentTooltip.maxKbFiles') }}</div>
                    </div>
                  </template>
                  <el-dropdown
                    trigger="click"
                    @command="handleAttachmentCommand"
                    @visible-change="handleAttachmentDropdownVisible"
                    placement="top-end"
                  >
                    <button
                      class="kb-attachment-btn"
                    >
                      <el-icon><Paperclip /></el-icon>
                    </button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="local">
                          <el-icon><Folder /></el-icon>
                          <span style="margin-left: 8px">{{ $t('ai.chat.attachmentLocalFile') }}</span>
                        </el-dropdown-item>
                        <el-dropdown-item command="knowledge">
                          <el-icon><Document /></el-icon>
                          <span style="margin-left: 8px">{{ $t('ai.chat.attachmentKnowledgeFile') }}</span>
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </el-tooltip>

                <!-- 隐藏的文件输入 -->
                <input
                  ref="fileInput"
                  type="file"
                  multiple
                  accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.csv,.txt,.md"
                  style="display: none"
                  @change="handleFileUpload"
                />

                <!-- 发送/停止按钮 -->
                <button
                  v-if="!isGenerating"
                  class="kb-send-btn"
                  :class="{ active: inputMessage.trim() || selectedKnowledgeBases.length > 0 }"
                  :disabled="isSendingMessage || isGenerating"
                  @click.stop.prevent="() => { console.log('[按钮点击] 发送按钮被点击'); sendKbMessage(); }"
                >
                  <el-icon><ArrowUp /></el-icon>
                </button>

                <!-- 停止按钮 -->
                <button
                  v-if="isGenerating"
                  class="kb-stop-btn"
                  @click="stopGeneration"
                >
                  <el-icon><Close /></el-icon>
                </button>
              </div>
            </div>
          </div>
          <div class="input-footer">{{ $t('ai.chat.aiContentHint') }}</div>
          <!-- 🔥 修复：滚动到底部按钮 - 在输入框区域右下角（还原到原始位置） -->
          <button
            v-if="showScrollToBottom"
            class="scroll-to-bottom-btn-input"
            @click="scrollKbToBottom"
            :title="$t('ai.chat.scrollToBottom') || '滚动到底部'"
          >
            <el-icon><ArrowDown /></el-icon>
          </button>
        </div>
    </div>

    <!-- 知识库选择对话框 -->
    <el-dialog
      v-model="showKnowledgeDialog"
      :title="t('ai.chat.selectKnowledgeBase')"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-loading="loadingKnowledgeList" class="knowledge-dialog-content">
        <!-- 🔥 新增：搜索框 -->
        <div class="search-input-wrap" style="margin: 0 0 12px 0">
          <span class="search-left-icon">
            <el-icon><Search /></el-icon>
          </span>
          <input
            class="form-input"
            v-model="kbSelectSearchQuery"
            :placeholder="$t('knowledge.searchPlaceholder')"
            style="width: 100%; padding: 10px 40px 10px 36px"
            @input="handleKbSelectSearchInput"
          />
        </div>
        <!-- 🔥 修改：添加滚动监听和加载更多 -->
        <div 
          class="knowledge-list-scroll"
          @scroll="handleKbSelectScroll"
          style="max-height: 400px; overflow-y: auto;"
        >
          <div v-if="knowledgeListForSelect.length === 0 && !loadingKnowledgeList" class="empty-state">
            <el-empty :description="t('ai.chat.noKnowledgeBase')" />
          </div>
          <div v-else class="knowledge-list">
            <div
              v-for="kb in knowledgeListForSelect"
              :key="kb.id"
              class="knowledge-item"
              :class="{ selected: selectedKnowledgeForFile?.id === kb.id }"
              @click="selectKnowledgeForFile(kb)"
              @dblclick="handleKnowledgeItemDoubleClick(kb)"
            >
              <!-- 🔥 修改：显示知识库封面 -->
              <div class="knowledge-item-icon">
                <img
                  v-if="getKbCoverUrl(kb)"
                  :src="getKbCoverUrl(kb)"
                  alt="cover"
                  class="kb-cover-img"
                />
                <div v-else class="knowledge-item-icon-default">
                  <el-icon><Collection /></el-icon>
                </div>
              </div>
              <div class="knowledge-info">
                <div class="knowledge-name">{{ kb.name }}</div>
                <div v-if="kb.description" class="knowledge-desc">{{ kb.description }}</div>
              </div>
            </div>
            <!-- 🔥 新增：加载更多提示 -->
            <div v-if="kbSelectPagination.loading" class="kb-loading-more">
              {{ $t('common.loading') }}...
            </div>
            <div v-else-if="!kbSelectPagination.hasMore && knowledgeListForSelect.length > 0" class="kb-no-more">
              {{ $t('common.noMoreData') }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showKnowledgeDialog = false">{{ t('common.cancel') }}</el-button>
          <el-button
            type="primary"
            @click="confirmKnowledgeSelection"
            :disabled="!selectedKnowledgeForFile"
          >
            {{ $t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 文档选择对话框 -->
    <el-dialog
      v-model="showDocumentDialog"
      :title="t('ai.chat.selectDocument')"
      width="800px"
      :close-on-click-modal="false"
      @close="handleDocumentDialogClose"
    >
      <div v-loading="loadingDocuments" class="document-dialog-content">
        <div v-if="selectedKnowledgeForFile" class="selected-knowledge-info">
          <el-icon><Document /></el-icon>
          <span>{{ t('ai.chat.knowledgeBaseLabel') }}{{ selectedKnowledgeForFile.name }}</span>
        </div>
        <div v-if="documentList.length === 0 && !loadingDocuments" class="empty-state">
          <el-empty :description="t('ai.chat.noDocumentsInKb')" />
        </div>
        <div v-else class="document-list">
          <div class="document-limit-hint">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ t('ai.chat.maxKbFilesHint') }}</span>
          </div>
          <el-checkbox-group v-model="selectedDocuments" :max="3">
            <div
              v-for="doc in documentList"
              :key="doc.id"
              class="document-item"
            >
              <el-checkbox :label="doc.id" class="document-checkbox" :disabled="selectedDocuments.length >= 3 && !selectedDocuments.includes(doc.id)">
                <div class="document-info">
                  <div class="document-name">{{ doc.fileName || doc.name || t('ai.chat.unnamedDocument') }}</div>
                  <div v-if="doc.fileSize" class="document-size">{{ formatFileSize(doc.fileSize) }}</div>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <span class="selected-count">
            {{ t('ai.chat.selectedDocumentsCount', { count: selectedDocuments.length }) }}
            <span v-if="selectedDocuments.length >= 3" class="limit-warning">{{ t('ai.chat.limitReached') }}</span>
          </span>
          <div>
            <el-button @click="handleDocumentDialogCancel">{{ t('common.cancel') }}</el-button>
            <el-button
              type="primary"
              @click="confirmDocumentSelection"
              :disabled="selectedDocuments.length === 0"
            >
              {{ t('common.confirm') }}
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted, watch, provide } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import { useLoginModal } from '@/composables/useLoginModal'
import {
  Plus,
  MoreFilled,
  StarFilled,
  Star,
  Edit,
  EditPen,
  Delete,
  Document,
  DocumentCopy,
  Refresh,
  Close,
  ArrowDown,
  ArrowUp,
  Paperclip,
  Folder,
  Check,
  InfoFilled,
  Search,
  Collection
} from '@element-plus/icons-vue'
import {
  getKnowledgeList,
  getKnowledgeListPage,
  getKnowledgeFileRelationList
} from '@/api/Knowledge/knowledge'
import {
  streamKnowledgeChatbot
} from '@/api/Knowledge/knowledge'
import {
  runWorkflowStream
} from '@/api/AI/ai'
import {
  // 新接口
  createAiConversation,
  updateAiConversation,
  deleteAiConversation,
  pageAiConversations,
  listAiConversations,
  listAiMessages,
  createAiMessage,
  updateAiConversationPinnedStatus,
  // 旧接口（保留兼容）
  getConversations,
  createConversation,
  updateConversation,
  deleteConversation,
  getConversationMessages
} from '@/api/AI/ai'
import { createLogger } from '@/utils/simpleLogger'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import iconSearchOn from '@/assets/images/open_connect.png'
import iconSearchOff from '@/assets/images/close_connect.png'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const logger = createLogger('AIChat')
const { openLoginModal } = useLoginModal()

// 配置 marked 选项
marked.setOptions({
  breaks: true, // 支持 GitHub 风格的换行
  gfm: true, // 启用 GitHub Flavored Markdown
  headerIds: false, // 不生成标题 ID
  mangle: false // 不混淆邮箱地址
})

// 提供判断当前对话是否为空的方法给侧边栏使用
const isCurrentChatEmpty = () => {
  // 如果没有当前对话，返回 false（可以创建新对话）
  if (!currentChat.value) {
    return false
  }
  // 如果当前对话没有消息，返回 true（空对话）
  return !messages.value || messages.value.length === 0
}

// 提供创建新对话的方法给侧边栏使用
const handleCreateNewChatFromSidebar = async () => {
  try {
    // 🔥 修复：检查用户是否已登录
    if (!authStore.isLoggedIn) {
      ElMessage.warning(t('user.pleaseLogin'))
      openLoginModal()
      return
    }
    
    // 🔥 修复：检查是否已经是新建对话状态（与侧边栏的 isNewChatActive 逻辑一致）
    // 条件：currentChat为null 且 没有消息 且 sessionStorage中没有对话ID
    const storedId = sessionStorage.getItem('currentConversationId')
    const hasNoMessages = !messages.value || messages.value.length === 0
    const hasNoStoredId = !storedId || storedId === '' || storedId === 'null' || storedId === 'undefined'
    const hasNoCurrentChat = !currentChat.value
    
    // 如果满足新建对话的所有条件，提示用户（已经是新建对话状态）
    if (hasNoCurrentChat && hasNoMessages && hasNoStoredId) {
      logger.info('当前已是最新对话状态，提示用户', { 
        hasNoCurrentChat, 
        hasNoMessages, 
        hasNoStoredId,
        storedId 
      })
      ElMessage.warning(t('ai.chat.alreadyNewChat') || '当前已是最新对话')
      return
    }
    
    // 如果当前有对话但标记为新对话且没有消息，也需要检查
    if (currentChat.value && currentChat.value.isNew && hasNoMessages) {
      logger.info('当前已是最新对话状态（isNew=true），提示用户')
      ElMessage.warning(t('ai.chat.alreadyNewChat') || '当前已是最新对话')
      return
    }
    
    // 🔥 修复：否则，清除当前状态，切换到新建对话状态
    logger.info('切换到新建对话状态', { 
      hasCurrentChat: !!currentChat.value,
      messagesCount: messages.value?.length || 0,
      storedId 
    })
    // 创建新对话（不调用后端接口，只切换状态）
    await createNewChat()
  } catch (error) {
    console.error('创建新对话失败', error)
    ElMessage.error(t('ai.chat.operationFailed'))
  }
}

// 提供刷新侧边栏的方法
const refreshSidebar = () => {
  // 这个方法会被侧边栏注入使用
  // 可以通过事件总线或直接调用侧边栏方法
}

// 通过 provide 暴露方法给侧边栏
provide('aiChatMethods', {
  isCurrentChatEmpty,
  handleCreateNewChatFromSidebar,
  refreshSidebar
})

// 响应式数据
const chats = ref([])
const currentChat = ref(null)
const messages = ref([])
const inputMessage = ref('')
const kbMessages = ref(null)
const messageInput = ref(null) // 添加缺失的 messageInput ref
const containerRef = ref(null) // 容器引用
const isGenerating = ref(false)
// 🔥 修复：滚动到底部按钮显示状态
const showScrollToBottom = ref(false)
const isSendingMessage = ref(false) // 发送消息的锁，防止重复调用
const currentAbortController = ref(null)
const currentConversationId = ref(null)
const sendingMessageKey = ref(null) // 当前正在发送的消息唯一标识（内容），用于防重复
const lastUserMessageId = ref(null) // 最后发送的用户消息ID，用于清除重复
// 🔥 修复：防重复加载标记，避免 chatSelected 事件和定时器检查同时触发
const isHandlingChatSelected = ref(false)

// 对话列表相关
const contextMenu = ref({ chat: null, x: 0, y: 0 })

// 知识库选择功能
const showKnowledgeBaseList = ref(false)
const selectedKnowledgeBases = ref([])
const selectedKbIndex = ref(-1)
const knowledgeBaseList = ref([])
const kbSearchQuery = ref('')
// 🔥 新增：知识库列表分页相关（用于@符号下拉框）
const kbListPagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  hasMore: true,
  loading: false
})
const kbListSearchTimer = ref(null)

// 附件功能
const attachments = ref([])
const fileInput = ref(null)
// 🔥 修复：跟踪附件下拉菜单的显示状态，用于控制 tooltip 的显示
const showAttachmentDropdown = ref(false)

// 知识库文件选择功能
const showKnowledgeDialog = ref(false)
const showDocumentDialog = ref(false)
// 🔥 新增：标记是否正在确认文档选择（用于区分确认和取消操作）
const isConfirmingDocumentSelection = ref(false)
const knowledgeListForSelect = ref([])
const loadingKnowledgeList = ref(false)
// 🔥 新增：附件按钮知识库选择的分页相关
const kbSelectPagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  hasMore: true,
  loading: false
})
const kbSelectSearchQuery = ref('')
const kbSelectSearchTimer = ref(null)
const selectedKnowledgeForFile = ref(null)
const documentList = ref([])
const loadingDocuments = ref(false)
const selectedDocuments = ref([])

// 模型选择
const showModelDropdown = ref(false)
const selectedModel = ref('qwen3-max')

// 联网搜索开关
const enableSearch = ref(false)
// 🔥 修复：使用 computed 属性根据当前语言返回翻译后的模型选项
const modelOptions = computed(() => [
  {
    value: 'qwen3-max',
    name: t('ai.chat.models.qwen3Max.name'),
    description: t('ai.chat.models.qwen3Max.description')
  },
  {
    value: 'deepseek-v3.1',
    name: t('ai.chat.models.deepseekV31.name'),
    description: t('ai.chat.models.deepseekV31.description')
  },
  {
    value: 'deepseek-r1',
    name: t('ai.chat.models.deepseekR1.name'),
    description: t('ai.chat.models.deepseekR1.description')
  }
])

// 计算属性
// 🔥 修改：移除前端过滤，因为搜索已由后端接口处理
const filteredKnowledgeBases = computed(() => {
  // 过滤掉无效的元素（防止 undefined 或 null）
  return knowledgeBaseList.value.filter((kb) => kb && kb.name)
})

const hasKnowledgeBaseSelected = computed(() => {
  return selectedKnowledgeBases.value.length > 0
})

// 动态 placeholder：未登录时显示"尽管问"，已登录时显示带知识库的提示
const inputPlaceholder = computed(() => {
  if (!authStore.isLoggedIn) {
    return t('ai.chat.inputPlaceholderGuest')
  }
  return t('ai.chat.inputPlaceholderWithKb')
})

// 去重消息列表（基于ID和内容去重，保留最后一个）
const uniqueMessages = computed(() => {
  const seenById = new Map() // 基于ID去重
  const seenByContent = new Map() // 基于内容和时间戳去重（防止相同内容重复）
  const result = []
  
  // 从后往前遍历，保留最后一个出现的消息（最新的）
  for (let i = messages.value.length - 1; i >= 0; i--) {
    const msg = messages.value[i]
    
    // 首先检查ID是否重复
    if (seenById.has(msg.id)) {
      logger.warn('发现重复ID的消息，已过滤', { 
        id: msg.id, 
        type: msg.type,
        content: msg.content?.substring(0, 50) 
      })
      continue
    }
    
    // 对于用户消息，还要检查内容和时间戳（防止相同内容在短时间内重复）
    if (msg.type === 'user' && msg.content) {
      const timestamp = msg.timestamp ? new Date(msg.timestamp).getTime() : 0
      const timeWindow = Math.floor(timestamp / 1000) // 按秒分组
      const contentKey = `${msg.type}-${msg.content}-${timeWindow}`
      if (seenByContent.has(contentKey)) {
        logger.warn('发现重复内容的用户消息，已过滤', { 
          id: msg.id, 
          content: msg.content?.substring(0, 50),
          timestamp: msg.timestamp
        })
        continue
      }
      seenByContent.set(contentKey, true)
    }
    
    seenById.set(msg.id, true)
    result.unshift(msg) // 使用 unshift 保持顺序
  }
  
  return result
})

// 🔥 修复：判断是否是最后一条AI消息
const isLastAiMessage = (msg, currentIndex, allMessages) => {
  if (msg.type !== 'ai') {
    return false
  }
  // 从当前位置往后查找，看是否还有AI消息
  for (let i = currentIndex + 1; i < allMessages.length; i++) {
    if (allMessages[i].type === 'ai') {
      return false
    }
  }
  return true
}

// 方法

/**
 * 更新本地对话列表中的对话信息（不调用API）
 * @param {string|number} conversationId 对话ID
 * @param {object} updates 要更新的字段（如 lastMessage, updatedAt 等）
 */
const updateLocalChat = (conversationId, updates = {}) => {
  if (!conversationId) return
  
  const chatIndex = chats.value.findIndex(c => String(c.id) === String(conversationId))
  if (chatIndex !== -1) {
    // 更新对话信息
    Object.assign(chats.value[chatIndex], updates)
    // 重新排序（确保更新的对话排在前面）
    sortChats()
    // 保存到本地存储
    saveChatsToStorage()
    logger.debug('本地更新对话信息', { conversationId, updates })
  } else {
    logger.debug('对话不存在于列表中，跳过本地更新', { conversationId })
  }
}

const loadChats = async () => {
  // 未登录用户不需要加载对话列表，静默跳过
  if (!authStore.isLoggedIn) {
    chats.value = []
    return
  }

  // 🔥 修复：如果正在加载（本地或全局），跳过重复调用
  if (isLoadingChats.value || window.__isLoadingConversations) {
    logger.debug('对话列表正在加载中，跳过重复调用', { 
      local: isLoadingChats.value, 
      global: window.__isLoadingConversations 
    })
    return
  }

  isLoadingChats.value = true
  window.__isLoadingConversations = true
  try {
    logger.info('加载对话列表')
    // 🔥 优化：使用与 ToCSidebar 相同的接口（带 sortBy: 'pinned' 参数），避免重复调用
    // 虽然客户端也会排序，但使用服务端排序可以减少客户端处理
    const response = await pageAiConversations({ pageNo: 1, pageSize: 100, sortBy: 'pinned', sortOrder: 'DESC' })
    if (response.code === 200 && response.data) {
      chats.value = response.data.records || response.data.list || []
      // 按置顶状态和更新时间排序
      sortChats()
      logger.info('对话列表加载成功', chats.value.length)
    } else {
      chats.value = []
    }
  } catch (error) {
    // 如果是401未授权错误，静默处理（退出登录后正常情况）
    const isUnauthorized = error?.message?.includes('未授权') || 
                          error?.message?.includes('Unauthorized') ||
                          error?.response?.status === 401 ||
                          error?.code === 401
    
    if (isUnauthorized && !authStore.isLoggedIn) {
      // 退出登录后的401错误是正常的，静默处理
      chats.value = []
      return
    }
    
    logger.error('加载对话列表失败', error)
    // 如果新接口失败，尝试使用旧接口
    try {
      const fallbackResponse = await getConversations({ page: 1, size: 100 })
      if (fallbackResponse.code === 200 && fallbackResponse.data) {
        chats.value = fallbackResponse.data.records || fallbackResponse.data.list || []
        sortChats()
      } else {
        chats.value = []
      }
    } catch (fallbackError) {
      // 如果是401未授权错误，静默处理
      const isFallbackUnauthorized = fallbackError?.message?.includes('未授权') || 
                                    fallbackError?.message?.includes('Unauthorized') ||
                                    fallbackError?.response?.status === 401 ||
                                    fallbackError?.code === 401
      
      if (isFallbackUnauthorized && !authStore.isLoggedIn) {
        chats.value = []
        return
      }
      
      logger.error('使用旧接口加载对话列表也失败', fallbackError)
      chats.value = []
    }
  } finally {
    // 🔥 修复：清除加载标记（本地和全局）
    isLoadingChats.value = false
    window.__isLoadingConversations = false
  }
}

const sortChats = () => {
  chats.value.sort((a, b) => {
    // 置顶的排在前面
    if (a.pinned && !b.pinned) return -1
    if (!a.pinned && b.pinned) return 1
    // 同置顶状态下按更新时间排序
    return new Date(b.updatedAt) - new Date(a.updatedAt)
  })
}

const loadKnowledgeBases = async (isLoadMore = false) => {
  // 未登录用户不需要加载知识库列表，静默跳过
  if (!authStore.isLoggedIn) {
    knowledgeBaseList.value = []
    return
  }

  // 🔥 修复：如果正在加载，跳过重复调用
  if (isLoadingKnowledgeBases.value || kbListPagination.value.loading) {
    logger.debug('知识库列表正在加载中，跳过重复调用')
    return
  }

  isLoadingKnowledgeBases.value = true
  kbListPagination.value.loading = true
  
  if (!isLoadMore) {
    // 重置分页
    kbListPagination.value.pageNo = 1
    kbListPagination.value.hasMore = true
    knowledgeBaseList.value = []
  }

  try {
    logger.info('加载知识库列表', {
      pageNo: kbListPagination.value.pageNo,
      pageSize: kbListPagination.value.pageSize,
      keyword: kbSearchQuery.value,
      isLoadMore
    })
    
    // 🔥 替换为新接口：使用分页查询接口
    const response = await getKnowledgeListPage({
      pageNo: kbListPagination.value.pageNo,
      pageSize: kbListPagination.value.pageSize,
      keyword: kbSearchQuery.value || undefined
    })
    
    if (response.code === 200 && response.data) {
      const list = response.data.records || response.data.list || []
      
      if (isLoadMore) {
        // 追加数据
        knowledgeBaseList.value.push(...list)
      } else {
        // 替换数据
        knowledgeBaseList.value = list
      }
      
      // 更新分页信息
      kbListPagination.value.total = response.data.total || 0
      kbListPagination.value.hasMore = 
        knowledgeBaseList.value.length < kbListPagination.value.total
      
      logger.info('知识库列表加载成功', {
        count: knowledgeBaseList.value.length,
        total: kbListPagination.value.total,
        hasMore: kbListPagination.value.hasMore,
        items: knowledgeBaseList.value.map(kb => ({
          id: kb.id,
          name: kb.name,
          hasName: !!kb.name
        }))
      })
    } else {
      logger.warn('知识库列表响应异常', { code: response.code, data: response.data })
      if (!isLoadMore) {
        knowledgeBaseList.value = []
      }
    }
  } catch (error) {
    // 如果是401未授权错误，静默处理（未登录用户正常情况）
    const isUnauthorized = error?.message?.includes('未授权') || 
                          error?.message?.includes('Unauthorized') ||
                          error?.response?.status === 401 ||
                          error?.code === 401
    
    if (isUnauthorized && !authStore.isLoggedIn) {
      // 未登录时的401错误是正常的，静默处理
      knowledgeBaseList.value = []
      return
    }
    
    // 其他错误才记录为错误日志
    logger.error('加载知识库列表失败', error)
    if (!isLoadMore) {
      knowledgeBaseList.value = []
    }
  } finally {
    // 🔥 修复：清除加载标记
    isLoadingKnowledgeBases.value = false
    kbListPagination.value.loading = false
  }
}

const createNewChat = async () => {
  try {
    // 🔥 修复：检查是否已经是新建对话状态（currentChat为null且没有消息）
    const storedId = sessionStorage.getItem('currentConversationId')
    const hasNoMessages = !messages.value || messages.value.length === 0
    const hasNoStoredId = !storedId || storedId === '' || storedId === 'null' || storedId === 'undefined'
    const hasNoCurrentChat = !currentChat.value
    
    if (hasNoCurrentChat && hasNoMessages && hasNoStoredId) {
      // 已经是新建对话状态，提示用户
      ElMessage.info(t('ai.chat.alreadyNewChat') || '当前已是最新对话')
      // 🔥 修复：已经是新建对话状态，不执行任何路由操作，直接返回
      return
    }
    
    // 如果当前有对话且有消息，也需要检查
    if (currentChat.value && currentChat.value.isNew && hasNoMessages) {
      // 已经是新对话且没有消息，提示用户
      ElMessage.info(t('ai.chat.alreadyNewChat') || '当前已是最新对话')
      // 🔥 修复：已经是新建对话状态，不执行任何路由操作，直接返回
      return
    }

    logger.info('创建新对话', {
      hasCurrentChat: !!currentChat.value,
      messagesCount: messages.value?.length || 0,
      storedId
    })
    
    // 🔥 修复：直接设置为空状态，不创建临时对话对象，不调用后端接口
    // 清除当前对话和消息
    currentChat.value = null
    messages.value = []
    
    // 清除 sessionStorage
    sessionStorage.removeItem('currentConversationId')
    
    // 🔥 修复：只有在URL中确实有conversationId参数时才清除，避免路由冗余导航
    // 使用try-catch捕获路由错误，确保不影响主要功能
    // 注意：如果当前路由已经是 /ai/chat 且没有query参数，不要调用router.replace，避免路由冗余导航
    // 只有在确实需要清除URL参数时才调用，且要确保不会导致路由冗余导航
    const hasConversationIdInUrl = route.path === '/ai/chat' && route.query && route.query.conversationId
    if (hasConversationIdInUrl) {
      try {
        await nextTick()
        // 🔥 修复：使用 router.replace 但捕获所有可能的错误，包括路由冗余导航
        await router.replace({ path: '/ai/chat', query: {} }).catch((err) => {
          // 忽略路由冗余导航错误（这是Vue Router的正常优化行为，不影响功能）
          if (err.name !== 'NavigationDuplicated' && err.name !== 'NavigationCancelled') {
            logger.debug('清除URL参数时出现路由错误（可忽略）', err)
          }
        })
      } catch (err) {
        // 忽略路由错误，不影响主要功能
        if (err.name !== 'NavigationDuplicated' && err.name !== 'NavigationCancelled') {
          logger.debug('清除URL参数时出现路由错误（可忽略）', err)
        }
      }
    }
    
    // 🔥 修复：通知侧边栏更新选中状态（清除选中状态，显示"新建对话"为选中）
    // 触发自定义事件，通知侧边栏清除选中状态
    window.dispatchEvent(new CustomEvent('chatCleared', { detail: {} }))
    
    logger.info('已切换到新建对话状态（不调用后端接口）')
  } catch (error) {
    logger.error('创建新对话失败', error)
    ElMessage.error(t('ai.chat.createError'))
  }
}

const selectChat = async (chat, forceLoadFromApi = false) => {
  // 如果选中的是同一个对话，不重复加载（除非强制从API加载）
  if (currentChat.value && currentChat.value.id === chat.id && !forceLoadFromApi) {
    logger.info('已选中相同对话，跳过加载', chat.id)
    return
  }
  
  logger.info('选择对话', { chatId: chat.id, chatTitle: chat.title, isNew: chat.isNew, forceLoadFromApi })
  currentChat.value = chat
  currentConversationId.value = null
  
  // 如果是新对话（没有ID或isNew为true），不加载消息
  if (chat.id && !chat.isNew) {
    logger.info('选择对话，加载消息列表', { chatId: chat.id, forceLoadFromApi })
    await loadMessages(chat.id, forceLoadFromApi)
  } else {
    // 新对话，清空消息列表
    messages.value = []
    logger.info('新对话，清空消息列表')
  }
  
  chat.unreadCount = 0
  saveChatsToStorage()
  
  // 🔥 修复：只使用 sessionStorage 存储对话ID，不在路由中显示参数
  if (chat.id && !chat.isNew) {
    sessionStorage.setItem('currentConversationId', String(chat.id))
    // 清除URL中的conversationId参数（如果存在）
    if (route.query.conversationId) {
      router.replace({ path: '/ai/chat', query: {} })
    }
  } else {
    // 新对话，清除存储的对话ID
    sessionStorage.removeItem('currentConversationId')
    // 清除URL中的conversationId参数（如果存在）
    if (route.query.conversationId) {
      router.replace({ path: '/ai/chat', query: {} })
    }
  }
}

const loadMessages = async (chatId, forceLoadFromApi = false) => {
  // 未登录用户不需要加载消息，静默跳过
  if (!authStore.isLoggedIn) {
    messages.value = []
    return
  }

  try {
    logger.info('加载消息列表', { chatId, forceLoadFromApi })
    // 🔥 修复：支持字符串和数字类型的ID比较
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    
    // 🔥 修复：如果强制从API加载，或者本地没有消息，则从API加载
    if (forceLoadFromApi || !chat || !chat.messages || chat.messages.length === 0) {
      // 尝试从API加载
      try {
        logger.info('从API加载消息列表', chatId)
        // 使用新接口：查询会话的所有消息列表
        const response = await listAiMessages(chatId)
        if (response.code === 200 && response.data) {
          const apiMessages = (response.data.records || response.data.list || response.data || []).map(msg => {
            // 解析 sources 字段，统一字段名格式
            let documents = []
            if (msg.sources) {
              try {
                const sourcesData = typeof msg.sources === 'string' ? JSON.parse(msg.sources) : msg.sources
                if (Array.isArray(sourcesData)) {
                  documents = sourcesData.map(doc => ({
                    id: doc.document_id || doc.id,
                    documentId: doc.document_id || doc.id,
                    name: doc.document_name || doc.name,
                    document_name: doc.document_name || doc.name,
                    datasetName: doc.dataset_name || doc.datasetName,
                    dataset_name: doc.dataset_name || doc.datasetName,
                    content: doc.content,
                    score: doc.score,
                    segmentId: doc.segment_id || doc.segmentId,
                    segment_id: doc.segment_id || doc.segmentId
                  }))
                }
              } catch (e) {
                logger.warn('解析 sources 字段失败', e)
                documents = []
              }
            }
            return {
              id: msg.id,
              type: msg.role === 'user' ? 'user' : 'ai',
              content: msg.content || '',
              originalContent: msg.content || '', // 🔥 修复：保存原始内容，用于编辑时正确显示
              timestamp: new Date(msg.created_time || msg.createdTime || msg.createdAt || msg.timestamp),
              documents: documents,
              conversationId: msg.conversationId
            }
          })
          messages.value = apiMessages
          // 更新本地存储
          if (chat) {
            chat.messages = apiMessages
            saveChatsToStorage()
          }
          logger.info('从API加载消息列表成功', { chatId, messageCount: apiMessages.length })
        } else {
          logger.warn('从API加载消息列表返回空数据', chatId)
          messages.value = []
        }
      } catch (error) {
        // 如果是401未授权错误，静默处理（退出登录后正常情况）
        const isUnauthorized = error?.message?.includes('未授权') || 
                              error?.message?.includes('Unauthorized') ||
                              error?.response?.status === 401 ||
                              error?.code === 401
        
        if (isUnauthorized && !authStore.isLoggedIn) {
          // 退出登录后的401错误是正常的，静默处理
          messages.value = []
          return
        }
        
        logger.warn('从新API加载消息失败，尝试使用旧接口', error)
        // 如果新接口失败，尝试使用旧接口
        try {
          const fallbackResponse = await getConversationMessages(chatId, { page: 1, size: 100 })
          if (fallbackResponse.code === 200 && fallbackResponse.data) {
            const apiMessages = (fallbackResponse.data.records || fallbackResponse.data.list || []).map(msg => ({
              id: msg.id,
              type: msg.role === 'user' ? 'user' : 'ai',
              content: msg.content || '',
              timestamp: new Date(msg.created_time || msg.createdAt || msg.timestamp),
              documents: msg.documents || []
            }))
            messages.value = apiMessages
            if (chat) {
              chat.messages = apiMessages
              saveChatsToStorage()
            }
            logger.info('使用旧接口加载消息列表成功', { chatId, messageCount: apiMessages.length })
          }
        } catch (fallbackError) {
          // 如果是401未授权错误，静默处理
          const isFallbackUnauthorized = fallbackError?.message?.includes('未授权') || 
                                        fallbackError?.message?.includes('Unauthorized') ||
                                        fallbackError?.response?.status === 401 ||
                                        fallbackError?.code === 401
          
          if (isFallbackUnauthorized && !authStore.isLoggedIn) {
            messages.value = []
            return
          }
          
          logger.warn('使用旧接口加载消息也失败，尝试使用本地存储', fallbackError)
          // 如果API都失败，尝试使用本地存储
          if (chat && chat.messages && chat.messages.length > 0) {
            messages.value = [...chat.messages]
            logger.info('使用本地存储的消息', { chatId, messageCount: chat.messages.length })
            // 恢复会话ID（如果有）
            if (chat.messages.length > 0) {
              const lastAiMessage = [...chat.messages].reverse().find(m => m.type === 'ai')
              if (lastAiMessage && lastAiMessage.conversationId) {
                currentConversationId.value = lastAiMessage.conversationId
              }
            }
          } else {
            messages.value = []
          }
        }
      }
    } else {
      // 本地有消息且不强制从API加载，使用本地消息
      messages.value = [...chat.messages]
      logger.info('使用本地存储的消息（不强制从API加载）', { chatId, messageCount: chat.messages.length })
      // 恢复会话ID（如果有）
      if (chat.messages.length > 0) {
        const lastAiMessage = [...chat.messages].reverse().find(m => m.type === 'ai')
        if (lastAiMessage && lastAiMessage.conversationId) {
          currentConversationId.value = lastAiMessage.conversationId
        }
      }
    }
    nextTick(() => {
      scrollKbToBottom()
      // 滚动后检查位置
      setTimeout(() => {
        checkScrollPosition()
      }, 100)
    })
  } catch (error) {
    logger.error('加载消息失败', error)
    messages.value = []
  }
}

const saveChatsToStorage = () => {
  localStorage.setItem('ai-chat-chats', JSON.stringify(chats.value))
}

const loadChatsFromStorage = () => {
  const savedChats = localStorage.getItem('ai-chat-chats')
  if (savedChats) {
    try {
      const parsed = JSON.parse(savedChats)
      chats.value = parsed.map(chat => ({
        ...chat,
        updatedAt: new Date(chat.updatedAt),
        messages: chat.messages || []
      }))
      sortChats()
    } catch (error) {
      logger.error('解析本地存储失败', error)
      chats.value = []
    }
  }
}

// 格式化消息内容 - 使用 marked + DOMPurify 专业 Markdown 渲染
const formatKbContent = (content) => {
  if (!content || typeof content !== 'string') {
    return ''
  }
  
  try {
    // 使用 marked 将 Markdown 转换为 HTML
    const rawHtml = marked.parse(content)
    
    // 使用 DOMPurify 清理 HTML，防止 XSS 攻击
    const cleanHtml = DOMPurify.sanitize(rawHtml, {
      ALLOWED_TAGS: [
        'p', 'br', 'strong', 'em', 'u', 's', 'del', 'code', 'pre',
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
        'ul', 'ol', 'li',
        'table', 'thead', 'tbody', 'tr', 'th', 'td',
        'blockquote', 'hr', 'a', 'img'
      ],
      ALLOWED_ATTR: ['href', 'src', 'alt', 'title', 'class'],
      ALLOW_DATA_ATTR: false
    })
    
    // 为 HTML 元素添加自定义类名，以便应用样式
    // 先临时标记代码块，避免影响行内代码处理
    const codeBlockPlaceholder = '___CODE_BLOCK_PLACEHOLDER___'
    const codeBlocks = []
    let styledHtml = cleanHtml.replace(/<pre><code>([\s\S]*?)<\/code><\/pre>/g, (match, code) => {
      const placeholder = `${codeBlockPlaceholder}${codeBlocks.length}`
      codeBlocks.push(match.replace(/<pre><code>/, '<pre class="md-code-block"><code>'))
      return placeholder
    })
    
    // 处理其他元素
    styledHtml = styledHtml
      // 表格包装和类名
      .replace(/<table>/g, '<div class="markdown-table-wrapper"><table class="markdown-table">')
      .replace(/<\/table>/g, '</table></div>')
      .replace(/<th>/g, '<th class="table-header">')
      .replace(/<td>/g, '<td class="table-cell">')
      // 标题类名
      .replace(/<h1>/g, '<h1 class="md-heading h1">')
      .replace(/<h2>/g, '<h2 class="md-heading h2">')
      .replace(/<h3>/g, '<h3 class="md-heading h3">')
      // 行内代码（此时代码块已被替换为占位符）
      .replace(/<code>/g, '<code class="md-inline-code">')
      // 文本格式
      .replace(/<strong>/g, '<strong class="md-bold">')
      .replace(/<em>/g, '<em class="md-italic">')
      .replace(/<del>/g, '<del class="md-strikethrough">')
      // 列表
      .replace(/<ul>/g, '<ul class="md-list">')
      .replace(/<li>/g, '<li class="md-list-item">')
      // 分隔线
      .replace(/<hr>/g, '<hr class="md-divider">')
    
    // 恢复代码块
    codeBlocks.forEach((codeBlock, index) => {
      styledHtml = styledHtml.replace(`${codeBlockPlaceholder}${index}`, codeBlock)
    })
    
    // 为表格行添加交替行类名
    styledHtml = styledHtml.replace(/<tbody>([\s\S]*?)<\/tbody>/g, (match, tbodyContent) => {
      const rows = tbodyContent.match(/<tr>[\s\S]*?<\/tr>/g) || []
      return '<tbody>' + rows.map((row, index) => 
        row.replace(/<tr>/, `<tr class="${index % 2 === 0 ? 'odd-row' : 'even-row'}">`)
      ).join('') + '</tbody>'
    })
    
    return styledHtml
  } catch (error) {
    logger.error('Markdown 渲染错误', error)
    // 失败时回退到基础处理
    return content
      .replace(/\n/g, '<br>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`(.*?)`/g, '<code>$1</code>')
  }
}

const formatTime = (date) => {
  if (!date) return ''
  
  const d = typeof date === 'string' || typeof date === 'number'
    ? new Date(date)
    : date
  
  // 检查日期是否有效
  if (isNaN(d.getTime())) return ''
  
  const now = new Date()
  const diff = now - d
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  // 格式化时间：HH:mm
  const formatHourMinute = (date) => {
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${hours}:${minutes}`
  }
  
  // 格式化日期：YYYY-MM-DD
  const formatDate = (date) => {
    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    return `${year}-${month}-${day}`
  }
  
  // 判断是否是同一天
  const isSameDay = (date1, date2) => {
    return date1.getFullYear() === date2.getFullYear() &&
           date1.getMonth() === date2.getMonth() &&
           date1.getDate() === date2.getDate()
  }
  
  // 判断是否是昨天
  const isYesterday = (date) => {
    const yesterday = new Date(now)
    yesterday.setDate(yesterday.getDate() - 1)
    return isSameDay(date, yesterday)
  }
  
  // 1分钟内：刚刚
  if (minutes < 1) return '刚刚'
  
  // 1小时内：X分钟前
  if (minutes < 60) return `${minutes}分钟前`
  
  // 今天：显示具体时间（如 14:30）
  if (isSameDay(d, now)) {
    return formatHourMinute(d)
  }
  
  // 昨天：昨天 HH:mm
  if (isYesterday(d)) {
    return `昨天 ${formatHourMinute(d)}`
  }
  
  // 7天内：X天前
  if (days < 7) {
    return `${days}天前`
  }
  
  // 更早：显示完整日期和时间（如 2025-01-15 14:30）
  return `${formatDate(d)} ${formatHourMinute(d)}`
}

// 🔥 修复：滚动到底部函数 - 参考千问设计
const scrollKbToBottom = () => {
  if (kbMessages.value) {
    kbMessages.value.scrollTo({
      top: kbMessages.value.scrollHeight,
      behavior: 'smooth'
    })
    // 滚动到底部后，延迟检查位置（等待滚动动画完成）
    setTimeout(() => {
      checkScrollPosition()
    }, 300)
  }
}

// 🔥 修复：检查滚动位置，决定是否显示滚动到底部按钮 - 参考千问设计
const checkScrollPosition = () => {
  if (!kbMessages.value) {
    showScrollToBottom.value = false
    return
  }
  
  const container = kbMessages.value
  const scrollHeight = container.scrollHeight
  const clientHeight = container.clientHeight
  const scrollTop = container.scrollTop
  
  // 🔥 修复：参考千问设计，判断是否在底部（留20px的容差，更宽松的判断）
  // 计算距离底部的距离
  const distanceFromBottom = scrollHeight - scrollTop - clientHeight
  const isAtBottom = distanceFromBottom < 20
  
  // 🔥 修复：只有当内容高度超过容器高度，且不在底部时才显示按钮
  // 按钮会一直显示直到滚动到底部才消失
  if (scrollHeight > clientHeight && !isAtBottom) {
    showScrollToBottom.value = true
  } else {
    showScrollToBottom.value = false
  }
}

// 🔥 修复：处理滚动事件
const handleMessagesScroll = () => {
  checkScrollPosition()
}
// 使用一个标记来确保函数只执行一次（防止并发调用）
let isExecuting = false
// 发送消息
const sendKbMessage = async () => {
  console.log('[sendKbMessage] 函数被调用', { 
    isSending: isSendingMessage.value, 
    isGenerating: isGenerating.value,
    isExecuting: isExecuting,
    hasInput: !!inputMessage.value?.trim(),
    hasSelectedKb: selectedKnowledgeBases.value.length > 0,
    stack: new Error().stack.split('\n').slice(0, 5).join('\n')
  })
  // 防止重复调用：使用同步锁机制，立即检查（必须在函数最开始）
  if (isExecuting) {
    console.warn('[sendKbMessage] 函数正在执行中，忽略重复请求', { 
      sendingMessageKey: sendingMessageKey.value,
      stack: new Error().stack.split('\n').slice(0, 5).join('\n')
    })
    return
  }
  if (isSendingMessage.value) {
    console.warn('[sendKbMessage] 正在发送消息，忽略重复请求', { 
      sendingMessageKey: sendingMessageKey.value,
      stack: new Error().stack.split('\n').slice(0, 5).join('\n')
    })
    logger.warn('正在发送消息，忽略重复请求', { 
      sendingMessageKey: sendingMessageKey.value,
      stack: new Error().stack 
    })
    return
  }
  // 立即设置执行标记（同步操作，防止并发）
  isExecuting = true
  
  // 🔥 检查用户是否已登录
  if (!authStore.isLoggedIn) {
    console.log('[sendKbMessage] 用户未登录，显示登录弹窗')
    ElMessage.warning(t('user.pleaseLogin'))
    openLoginModal()
    isExecuting = false
    return
  }
  
  if (isGenerating.value) {
    console.warn('[sendKbMessage] 正在生成回答，忽略重复请求')
    ElMessage.warning('正在生成回答，请稍候...')
    isExecuting = false
    return
  }
  
  if (!inputMessage.value.trim() && selectedKnowledgeBases.value.length === 0) {
    console.log('[sendKbMessage] 输入为空且未选择知识库，退出')
    isExecuting = false
    return
  }

  const rawText = inputMessage.value.trim()
  
  // 生成消息唯一标识（仅使用内容，用于防重复）
  const messageKey = rawText
  
  // 检查是否正在发送相同的消息（防止快速重复点击）
  if (sendingMessageKey.value === messageKey) {
    console.warn('[sendKbMessage] 正在发送相同的消息，忽略重复请求', { 
      messageKey, 
      isSending: isSendingMessage.value
    })
    logger.warn('正在发送相同的消息，忽略重复请求', { 
      messageKey, 
      isSending: isSendingMessage.value,
      stack: new Error().stack 
    })
    isExecuting = false
    return
  }
  
  // 立即设置锁和消息标识，防止并发调用（在所有验证通过后立即设置）
  console.log('[sendKbMessage] 设置锁，开始发送', { messageKey, text: rawText.substring(0, 50) })
  isSendingMessage.value = true
  sendingMessageKey.value = messageKey
  
  // 从输入文本中移除所有 @知识库名字 的内容，只保留实际的问题内容
  let text = rawText
  if (selectedKnowledgeBases.value.length > 0) {
    // 移除所有已选择的知识库的 @名字 标记
    for (const kb of selectedKnowledgeBases.value) {
      const kbText = `@${kb.name}`
      // 使用正则表达式移除 @知识库名字（包括前后的空格）
      text = text.replace(new RegExp(`\\s*${kbText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\s*`, 'g'), ' ').trim()
    }
    // 清理多余的空格
    text = text.replace(/\s+/g, ' ').trim()
  }
  
  // 如果没有选择知识库，knowledgeId为null，可以正常对话
  // 如果有选择知识库，支持多个知识库ID（后端接口支持数组）
  let knowledgeId = null
  let knowledgeIds = [] // 用于支持多个知识库ID
  if (selectedKnowledgeBases.value.length > 0) {
    // 收集所有知识库的ID（系统内部ID，用于后端查询Dify ID）
    knowledgeIds = selectedKnowledgeBases.value.map(kb => String(kb.id))
    // 如果有多个知识库，使用第一个的difyKbId作为主要ID（用于文件上传等场景）
    const firstKb = selectedKnowledgeBases.value[0]
    knowledgeId = firstKb.difyKbId || firstKb.difyKnowledgeId
    if (!knowledgeId) {
      ElMessage.warning(t('ai.chat.kbMissingDifyId'))
      // 继续执行，使用普通对话
    }
  }

  // 构建用户消息内容（包含@知识库标记，用于前端显示）
  let userMessageContent = rawText
  // 如果输入框中已经有 @知识库名字，就不需要再拼接了
  // 如果没有，则根据已选择的知识库列表拼接
  if (selectedKnowledgeBases.value.length > 0) {
    const hasKbMention = selectedKnowledgeBases.value.some(kb => {
      const kbText = `@${kb.name}`
      return rawText.includes(kbText)
    })
    if (!hasKbMention) {
      const kbNames = selectedKnowledgeBases.value.map(kb => `@${kb.name}`).join(' ')
      userMessageContent = `${kbNames} ${rawText}`
    }
  }

  // 生成唯一的消息ID（使用时间戳 + 随机数，确保唯一性）
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 10000)
  const userMessageId = `${timestamp}-${random}`
  
  // 检查是否已存在相同的用户消息（防止重复添加）
  const now = Date.now()
  const existingUserMessage = messages.value.find(
    m => m.type === 'user' && m.content === userMessageContent && 
    Math.abs((m.timestamp ? new Date(m.timestamp).getTime() : 0) - now) < 2000 // 2秒内的相同消息视为重复
  )
  
  if (existingUserMessage) {
    console.warn('[sendKbMessage] 检测到重复消息，跳过添加', { 
      text: userMessageContent.substring(0, 50), 
      existingId: existingUserMessage.id,
      timeDiff: Math.abs((existingUserMessage.timestamp ? new Date(existingUserMessage.timestamp).getTime() : 0) - now)
    })
    isSendingMessage.value = false
    sendingMessageKey.value = null
    isExecuting = false
    return
  }
  
  // 检查是否已存在相同ID的消息（防止重复添加）
  const existingUserById = messages.value.find(m => m.id === userMessageId)
  if (existingUserById) {
    console.warn('[sendKbMessage] 检测到重复ID的消息，跳过添加', { userMessageId })
    isSendingMessage.value = false
    sendingMessageKey.value = null
    isExecuting = false
    return
  }
  
  console.log('[sendKbMessage] 准备添加消息', { 
    userMessageId, 
    text: userMessageContent.substring(0, 50),
    currentMessageCount: messages.value.length
  })
  
  // 🔥 修复：确保有会话ID（如果是新建对话状态，在发送第一条消息时创建）
  let conversationId = currentChat.value?.id
  let isNewConversationCreated = false
  
  // 如果当前是新建对话状态（currentChat为null）或没有会话ID，需要先创建会话
  if (!currentChat.value || !conversationId || currentChat.value?.isNew) {
    // 如果没有会话ID或是新会话，需要先创建会话（调用后端接口）
    try {
      logger.info('发送第一条消息，创建新会话')
      const createResp = await createAiConversation({ title: '' })
      if (createResp.code === 200 && createResp.data) {
        conversationId = createResp.data.id
        isNewConversationCreated = true
        
        // 如果当前没有对话对象，创建一个
        if (!currentChat.value) {
          currentChat.value = {
            id: conversationId,
            title: '',
            lastMessage: '',
            updatedAt: new Date(),
            unreadCount: 0,
            isNew: false,
            pinned: createResp.data.isPinned === 1,
            createdAt: createResp.data.createdTime || createResp.data.createdAt,
            messages: []
          }
          // 添加到对话列表
          chats.value.unshift(currentChat.value)
        } else {
          // 如果已有对话对象，更新它
          currentChat.value.id = conversationId
          currentChat.value.isNew = false
          currentChat.value.createdAt = createResp.data.createdTime || createResp.data.createdAt
          currentChat.value.pinned = createResp.data.isPinned === 1
        }
        
        // 🔥 修复：设置 sessionStorage，并立即触发侧边栏刷新和更新选中状态
        sessionStorage.setItem('currentConversationId', String(conversationId))
        // 立即触发 chatCreated 事件，通知侧边栏刷新对话列表并更新选中状态
        window.dispatchEvent(new CustomEvent('chatCreated', { detail: { conversationId } }))
        
        logger.info('创建新会话成功，已通知侧边栏刷新', { conversationId })
      }
    } catch (error) {
      logger.error('创建会话失败', error)
      ElMessage.error(t('ai.chat.conversationFailed'))
      isExecuting = false
      return
    }
  }
  
  // 添加用户消息（只添加一次）
  const userMessage = {
    id: userMessageId,
    type: 'user',
    content: userMessageContent,
    originalContent: userMessageContent, // 🔥 修复：保存原始内容，用于编辑时正确显示
    timestamp: new Date(),
    attachments: attachments.value.length > 0 ? [...attachments.value] : [],
    conversationId: conversationId
  }

  // 再次检查是否已存在（防止并发添加）
  const checkBeforePush = messages.value.find(m => m.id === userMessageId)
  if (!checkBeforePush) {
    console.log('[sendKbMessage] 添加用户消息到列表', { id: userMessageId, content: userMessageContent.substring(0, 50) })
    messages.value.push(userMessage)
    console.log('[sendKbMessage] 用户消息已添加，当前消息数:', messages.value.length)
    logger.info('用户消息已添加', { id: userMessageId, content: userMessageContent.substring(0, 50) })
    // 保存最后发送的用户消息ID，用于后续清除重复
    lastUserMessageId.value = userMessageId
  } else {
    console.warn('[sendKbMessage] 用户消息已存在，跳过添加', { id: userMessageId })
    logger.warn('用户消息已存在，跳过添加', { id: userMessageId })
  }
  
  // 保存用户消息到后端
  if (conversationId) {
    try {
      const messageResp = await createAiMessage({
        conversationId: String(conversationId),
        role: 'user',
        content: text // 保存原始文本，不包含@知识库标记
      })
      if (messageResp.code === 200 && messageResp.data) {
        // 更新消息ID为后端返回的ID
        userMessage.id = messageResp.data.id
        userMessage.timestamp = new Date(messageResp.data.created_time || messageResp.data.createdTime)
        logger.info('保存用户消息成功', { messageId: messageResp.data.id })
      }
    } catch (error) {
      logger.warn('保存用户消息到后端失败', error)
      // 继续执行，不影响前端显示
    }
  }
  
  // 保存到当前对话
  if (currentChat.value) {
    if (!currentChat.value.messages) {
      currentChat.value.messages = []
    }
    currentChat.value.messages.push(userMessage)
    currentChat.value.lastMessage = text
    currentChat.value.updatedAt = new Date()
    
    // 更新对话标题（如果是新对话且第一条消息）
    if (currentChat.value.isNew && !currentChat.value.title && text) {
      currentChat.value.title = text.length > 20 ? text.substring(0, 20) + '...' : text
      currentChat.value.isNew = false
      
      // 调用API更新对话标题
      if (conversationId) {
        try {
          // 使用新接口：更新AI会话
          await updateAiConversation({ id: String(conversationId), title: currentChat.value.title })
        } catch (error) {
          logger.warn('更新对话标题失败，尝试使用旧接口', error)
          // 如果新接口失败，尝试使用旧接口
          try {
            await updateConversation(conversationId, { title: currentChat.value.title })
          } catch (fallbackError) {
            logger.warn('使用旧接口更新对话标题也失败', fallbackError)
          }
        }
      }
    }
    
    saveChatsToStorage()
    
    // 🔥 优化：本地更新对话信息，不调用API刷新整个列表
    if (conversationId) {
      updateLocalChat(conversationId, {
        lastMessage: text,
        updatedAt: new Date()
      })
    }
    
    // 🔥 修复：如果创建了新对话，通知侧边栏刷新对话列表并更新选中状态
    if (isNewConversationCreated && conversationId) {
      // 使用 sessionStorage 存储当前对话ID
      sessionStorage.setItem('currentConversationId', String(conversationId))
      // 触发自定义事件，通知侧边栏刷新对话列表并更新选中状态
      window.dispatchEvent(new CustomEvent('chatCreated', { detail: { conversationId } }))
      // 清除URL中的conversationId参数
      if (route.query.conversationId) {
        router.replace({ path: '/ai/chat', query: {} })
      }
    }
  }

  // 在清空之前先保存文件列表和知识库列表（用于后续接口调用）
  // 区分本地文件和知识库文件
  const localFiles = attachments.value
    .filter(att => att.type !== 'knowledge' && att.file)
    .map(att => att.file)
  // 提取知识库文件的 attachmentId（Long 类型）
  const knowledgeFileAttachmentIds = attachments.value
    .filter(att => att.type === 'knowledge' && att.attachmentId != null)
    .map(att => att.attachmentId)
  
  const filesToUpload = localFiles.length > 0 ? localFiles : null
  const hasFiles = (filesToUpload && filesToUpload.length > 0) || knowledgeFileAttachmentIds.length > 0
  const knowledgeBasesToUse = [...selectedKnowledgeBases.value] // 保存知识库列表副本

  // 清空输入框和附件（但保留副本用于接口调用）
  inputMessage.value = ''
  attachments.value = []
  selectedKnowledgeBases.value = []
  hideKnowledgeBaseList()

  // 创建AI消息占位
  const aiMessageId = `${timestamp + 1}-${random}`
  const aiMessage = {
    id: aiMessageId,
    type: 'ai',
    content: '',
    timestamp: new Date(),
    streaming: true,
    documents: []
  }
  
  // 检查是否已存在相同ID的AI消息（防止重复添加）
  const existingAiById = messages.value.find(m => m.id === aiMessageId)
  if (!existingAiById) {
    messages.value.push(aiMessage)
    logger.info('AI消息占位已添加', { id: aiMessageId })
  } else {
    logger.warn('AI消息占位已存在，跳过添加', { id: aiMessageId })
  }

  nextTick(() => {
    scrollKbToBottom()
  })

  // 设置生成标志（锁已经在前面设置了）
  isGenerating.value = true
  
  console.log('[sendKbMessage] 开始流式问答', { 
    knowledgeId: knowledgeId || '无知识库',
    query: text,
    conversationId: currentConversationId.value || conversationId,
    selectedKbCount: knowledgeBasesToUse.length,
    filesCount: filesToUpload?.length || 0,
    attachmentIdsCount: knowledgeFileAttachmentIds.length
  })

  try {
    logger.info('开始流式问答', { 
      knowledgeId: knowledgeId || '无知识库',
      query: text,
      conversationId: currentConversationId.value || conversationId,
      selectedKbCount: knowledgeBasesToUse.length,
      filesCount: filesToUpload?.length || 0,
      attachmentIdsCount: knowledgeFileAttachmentIds.length
    })

    // 调用新的工作流流式接口
    // 支持四种模式：
    // 1. 单独提问：不传文件和知识库，直接提问（不使用知识库）
    // 2. 根据知识库提问：只传知识库ID，基于知识库提问
    // 3. 根据文件提问：只传文件，执行工作流后再提问（使用 file 类型的 API key）
    // 4. 根据文件和知识库提问：同时传文件和知识库ID，执行工作流后再提问（文件上传到知识库）
    // 注意：所有参数都是可选的，除了 query（用户问题）是必填的
    
    // 调用接口（支持所有组合模式）
    // 如果有多个知识库，传递知识库ID数组（系统内部ID），后端会查询对应的Dify ID
    const knowledgeIdToSend = knowledgeIds.length > 0 ? knowledgeIds : (knowledgeId ? [knowledgeId] : undefined)
    const abortController = await runWorkflowStream({
      query: text, // 必填：用户问题
      knowledgeId: knowledgeIdToSend || undefined, // 可选：知识库ID（支持单个ID或数组，不传则不使用知识库）
      workflowId: undefined, // 可选：工作流ID（暂时不传，由后端决定）
      files: filesToUpload || undefined, // 可选：本地文件列表（不传则不执行工作流）
      attachmentIds: knowledgeFileAttachmentIds.length > 0 ? knowledgeFileAttachmentIds : undefined, // 可选：知识库文件 attachmentId 列表
      conversationId: currentConversationId.value || conversationId || undefined, // 可选：会话ID
      enableSearch: enableSearch.value, // 是否启用联网搜索
      onMessage: (answer) => {
        // 追加回答内容
        const message = messages.value.find(m => m.id === aiMessageId)
        if (message) {
          message.content += answer
          nextTick(() => {
          scrollKbToBottom()
      // 滚动后检查位置
      setTimeout(() => {
        checkScrollPosition()
      }, 100)
          // 滚动后检查位置
          setTimeout(() => {
            checkScrollPosition()
          }, 100)
        })
        }
      },
      onEnd: async (data) => {
        // 消息结束，保存会话ID和文档片段
        const message = messages.value.find(m => m.id === aiMessageId)
        if (message) {
          message.streaming = false
          // 统一文档格式，支持下划线和驼峰命名
          if (data.documents && Array.isArray(data.documents) && data.documents.length > 0) {
            message.documents = data.documents.map(doc => ({
              id: doc.document_id || doc.documentId || doc.id || doc.segment_id || doc.segmentId,
              documentId: doc.document_id || doc.documentId || doc.id,
              name: doc.document_name || doc.name || `文档 ${doc.id || doc.segment_id || ''}`,
              document_name: doc.document_name || doc.name,
              datasetName: doc.dataset_name || doc.datasetName,
              dataset_name: doc.dataset_name || doc.datasetName,
              content: doc.content || '',
              score: doc.score || 0,
              segmentId: doc.segment_id || doc.segmentId,
              segment_id: doc.segment_id || doc.segmentId
            }))
          } else {
            // 如果没有文档数据，尝试从 metadata 中提取
            if (data.metadata?.retriever_resources && Array.isArray(data.metadata.retriever_resources)) {
              message.documents = data.metadata.retriever_resources.map((resource, index) => ({
                id: resource.segment_id || `doc-${index}`,
                documentId: resource.document_id,
                name: resource.document_name || resource.dataset_name || `文档 ${index + 1}`,
                document_name: resource.document_name,
                datasetName: resource.dataset_name,
                dataset_name: resource.dataset_name,
                content: resource.content || '',
                score: resource.score || 0,
                segmentId: resource.segment_id,
                segment_id: resource.segment_id
              }))
            } else {
              message.documents = []
            }
          }
          message.conversationId = data.conversationId || currentChat.value?.id
        }
        
        // 更新会话ID（Dify返回的会话ID）
        if (data.conversationId) {
          currentConversationId.value = data.conversationId
        }
        
        // 🔥 修复：获取当前对话ID（优先使用 currentChat 的ID，如果没有则使用 Dify 返回的ID）
        const conversationId = currentChat.value?.id || data.conversationId
        
        // 🔥 修复：如果当前对话是新创建的（之前没有ID），现在有了ID，需要通知侧边栏
        if (conversationId && (!currentChat.value?.id || currentChat.value?.isNew)) {
          // 更新 currentChat 的ID（如果还没有）
          if (currentChat.value && !currentChat.value.id) {
            currentChat.value.id = conversationId
            currentChat.value.isNew = false
          }
          // 设置 sessionStorage
          sessionStorage.setItem('currentConversationId', String(conversationId))
          // 立即触发事件，通知侧边栏刷新
          window.dispatchEvent(new CustomEvent('chatCreated', { detail: { conversationId } }))
          logger.info('流式问答完成，通知侧边栏刷新对话列表', { conversationId })
        }
        
        // 保存AI消息到后端
        if (conversationId && message && message.content) {
          try {
            // 将文档片段转换为JSON字符串，统一使用下划线命名
            const sourcesJson = message.documents && message.documents.length > 0
              ? JSON.stringify(message.documents.map(doc => ({
                  document_id: doc.document_id || doc.documentId || doc.id,
                  document_name: doc.document_name || doc.name,
                  dataset_name: doc.dataset_name || doc.datasetName,
                  content: doc.content,
                  score: doc.score,
                  segment_id: doc.segment_id || doc.segmentId
                })))
              : null
            
            const messageResp = await createAiMessage({
              conversationId: String(conversationId),
              role: 'assistant',
              content: message.content,
              difyMessageId: data.messageId || null,
              sources: sourcesJson,
              confidence: null
            })
            
            if (messageResp.code === 200 && messageResp.data) {
              // 更新消息ID为后端返回的ID
              message.id = messageResp.data.id
              message.timestamp = new Date(messageResp.data.created_time || messageResp.data.createdTime)
              logger.info('保存AI消息成功', { messageId: messageResp.data.id })
            }
          } catch (error) {
            logger.warn('保存AI消息到后端失败', error)
            // 继续执行，不影响前端显示
          }
        }
        
        isGenerating.value = false
        isSendingMessage.value = false
        sendingMessageKey.value = null
        isExecuting = false
        currentAbortController.value = null
        
        // AI返回消息后，检查并清除重复的用户消息（保留最后一条）
        if (lastUserMessageId.value) {
          const lastUserMessage = messages.value.find(m => m.id === lastUserMessageId.value)
          if (lastUserMessage && lastUserMessage.content) {
            const content = lastUserMessage.content.trim()
            // 找出所有相同内容的用户消息
            const duplicateMessages = messages.value.filter(
              m => m.type === 'user' && m.content?.trim() === content && m.id !== lastUserMessageId.value
            )
            
            if (duplicateMessages.length > 0) {
              // 删除重复的消息（保留最后一条）
              duplicateMessages.forEach(dupMsg => {
                const index = messages.value.findIndex(m => m.id === dupMsg.id)
                if (index > -1) {
                  logger.info('清除重复的用户消息', { 
                    content: content.substring(0, 50), 
                    id: dupMsg.id,
                    index
                  })
                  messages.value.splice(index, 1)
                }
              })
              logger.info('已清除重复的用户消息', { 
                removedCount: duplicateMessages.length,
                content: content.substring(0, 50)
              })
            }
          }
          // 清除标记
          lastUserMessageId.value = null
        }
        
        // 保存消息到当前对话
        if (currentChat.value && currentChat.value.messages) {
          const messageIndex = currentChat.value.messages.findIndex(m => m.id === aiMessageId)
          if (messageIndex !== -1) {
            currentChat.value.messages[messageIndex] = message
            saveChatsToStorage()
          }
        }
        
        // 🔥 优化：本地更新对话信息，不调用API刷新整个列表
        const finalConversationId = conversationId || currentChat.value?.id || data.conversationId
        
        if (finalConversationId) {
          // 获取最后一条AI消息作为最后一条消息
          const lastAiMessage = messages.value.filter(m => m.type === 'ai').pop()
          const lastMessageText = lastAiMessage?.content || currentChat.value?.lastMessage || ''
          
          // 本地更新对话信息
          updateLocalChat(finalConversationId, {
            lastMessage: lastMessageText,
            updatedAt: new Date()
          })
          
          // 🔥 修复：通知侧边栏刷新并选中
          sessionStorage.setItem('currentConversationId', String(finalConversationId))
          // 触发自定义事件，通知侧边栏刷新对话列表并更新选中状态
          window.dispatchEvent(new CustomEvent('chatCreated', { detail: { conversationId: finalConversationId } }))
          logger.info('流式问答完成，已更新本地对话信息并通知侧边栏', { conversationId: finalConversationId })
          
          // 清除URL中的conversationId参数
          if (route.query.conversationId) {
            router.replace({ path: '/ai/chat', query: {} })
          }
        }
        
        nextTick(() => {
          scrollKbToBottom()
      // 滚动后检查位置
      setTimeout(() => {
        checkScrollPosition()
      }, 100)
          // 滚动后检查位置
          setTimeout(() => {
            checkScrollPosition()
          }, 100)
        })
        logger.info('流式问答完成', { 
          conversationId: data.conversationId || conversationId,
          messageId: data.messageId,
          documentCount: (data.documents || []).length
        })
      },
      onError: (error) => {
        isGenerating.value = false
        isSendingMessage.value = false
        sendingMessageKey.value = null
        isExecuting = false
        currentAbortController.value = null
        
        // 即使出错，也清除重复的用户消息
        if (lastUserMessageId.value) {
          const lastUserMessage = messages.value.find(m => m.id === lastUserMessageId.value)
          if (lastUserMessage && lastUserMessage.content) {
            const content = lastUserMessage.content.trim()
            const duplicateMessages = messages.value.filter(
              m => m.type === 'user' && m.content?.trim() === content && m.id !== lastUserMessageId.value
            )
            if (duplicateMessages.length > 0) {
              duplicateMessages.forEach(dupMsg => {
                const index = messages.value.findIndex(m => m.id === dupMsg.id)
                if (index > -1) {
                  messages.value.splice(index, 1)
                }
              })
              logger.info('错误处理中清除重复的用户消息', { removedCount: duplicateMessages.length })
            }
          }
          lastUserMessageId.value = null
        }
        
        // 处理错误
        const message = messages.value.find(m => m.id === aiMessageId)
        if (message) {
          if (error.code === 'CHATBOT_NOT_CREATED') {
            message.content = error.hint || t('ai.chat.chatbotNotCreated')
            // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
            if (!error._messageShown) {
              ElMessage.warning(error.message || t('ai.chat.chatbotNotCreatedShort'))
            }
          } else {
            message.content = t('ai.chat.responseFailed')
            // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
            if (!error._messageShown) {
              ElMessage.error(error.message || t('ai.chat.responseFailedShort'))
            }
          }
          message.streaming = false
        }
        logger.error('流式问答失败', error)
        nextTick(() => {
          scrollKbToBottom()
      // 滚动后检查位置
      setTimeout(() => {
        checkScrollPosition()
      }, 100)
          // 滚动后检查位置
          setTimeout(() => {
            checkScrollPosition()
          }, 100)
        })
      }
    })

    currentAbortController.value = abortController
  } catch (error) {
    isGenerating.value = false
    isSendingMessage.value = false
    sendingMessageKey.value = null
    isExecuting = false
    currentAbortController.value = null
    const message = messages.value.find(m => m.id === aiMessageId)
    if (message) {
      message.content = t('ai.chat.responseFailed')
      message.streaming = false
    }
    
    // 即使异常，也清除重复的用户消息
    if (lastUserMessageId.value) {
      const lastUserMessage = messages.value.find(m => m.id === lastUserMessageId.value)
      if (lastUserMessage && lastUserMessage.content) {
        const content = lastUserMessage.content.trim()
        const duplicateMessages = messages.value.filter(
          m => m.type === 'user' && m.content?.trim() === content && m.id !== lastUserMessageId.value
        )
        if (duplicateMessages.length > 0) {
          duplicateMessages.forEach(dupMsg => {
            const index = messages.value.findIndex(m => m.id === dupMsg.id)
            if (index > -1) {
              messages.value.splice(index, 1)
            }
          })
          logger.info('异常处理中清除重复的用户消息', { removedCount: duplicateMessages.length })
        }
      }
      lastUserMessageId.value = null
    }
    
    logger.error('流式问答异常', error)
    // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
    if (!error._messageShown) {
      ElMessage.error(error.message || t('ai.chat.responseFailedShort'))
    }
    nextTick(scrollKbToBottom)
  }
}

// 停止生成
const stopGeneration = () => {
  if (currentAbortController.value) {
    currentAbortController.value.abort()
    currentAbortController.value = null
  }
  isGenerating.value = false
  
  // 标记当前正在生成的消息为已停止
  const streamingMessage = messages.value.find(m => m.streaming)
  if (streamingMessage) {
    streamingMessage.streaming = false
    streamingMessage.content += `\n\n[${t('ai.chat.stopped')}]`
  }
}

// 复制消息
const copyKbMessage = async (content) => {
  try {
    // 移除HTML标签，获取纯文本
    const textContent = content.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').replace(/<br>/g, '\n')
    
    // 🔥 修复：添加降级方案，兼容非HTTPS环境
    if (navigator.clipboard && navigator.clipboard.writeText) {
      // 优先使用现代 Clipboard API
      try {
        await navigator.clipboard.writeText(textContent)
        ElMessage.success(t('ai.chat.copiedToClipboard'))
        return
      } catch (clipboardError) {
        logger.warn('Clipboard API 失败，尝试降级方案', clipboardError)
      }
    }
    
    // 降级方案：使用传统的 execCommand 方法
    const textArea = document.createElement('textarea')
    textArea.value = textContent
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    textArea.style.top = '-999999px'
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()
    
    try {
      const successful = document.execCommand('copy')
      if (successful) {
        ElMessage.success(t('ai.chat.copiedToClipboard'))
      } else {
        throw new Error('execCommand copy 失败')
      }
    } finally {
      document.body.removeChild(textArea)
    }
  } catch (error) {
    ElMessage.error(t('ai.chat.copyFailed'))
    logger.error('复制失败', error)
  }
}

// 重试消息（基于原用户消息重新生成，不重复创建用户消息）
// 🔥 增强：参考 Grok/Kimi 的重新生成逻辑，删除当前消息及其之后的所有消息
const retryKbMessage = async (msg) => {
  // 找到当前消息在数组中的索引
  const messageIndex = messages.value.findIndex(m => m.id === msg.id)
  if (messageIndex < 0) {
    logger.warn('无法找到对应的消息', { messageId: msg.id })
    return
  }
  
  // 找到对应的用户消息（当前消息的前一条）
  if (messageIndex <= 0) {
    logger.warn('无法找到对应的用户消息', { messageId: msg.id })
    return
  }
  
  const userMessage = messages.value[messageIndex - 1]
  if (!userMessage || userMessage.type !== 'user') {
    logger.warn('前一条消息不是用户消息', { messageId: msg.id, prevMessageType: userMessage?.type })
    return
  }
  
  // 如果正在生成，不允许重试
  if (isGenerating.value) {
    ElMessage.warning('正在生成回答，请稍候...')
    return
  }
  
  // 🔥 增强：删除从当前消息开始到数组末尾的所有消息（包括当前消息）
  // 计算需要删除的消息数量
  const messagesToDelete = messages.value.length - messageIndex
  const deletedMessages = messages.value.splice(messageIndex, messagesToDelete)
  
  logger.info('重新生成：删除当前消息及其之后的所有消息', { 
    messageIndex, 
    deletedCount: deletedMessages.length,
    deletedMessageIds: deletedMessages.map(m => m.id)
  })
  
  // 🔥 增强：同时从持久化存储中移除这些消息
  if (currentChat.value && currentChat.value.messages) {
    // 找到第一个要删除的消息在持久化存储中的索引
    const firstDeletedId = deletedMessages[0]?.id
    if (firstDeletedId) {
      const persistentStartIndex = currentChat.value.messages.findIndex(m => m.id === firstDeletedId)
      if (persistentStartIndex !== -1) {
        // 删除从该索引开始到末尾的所有消息
        const persistentDeletedCount = currentChat.value.messages.length - persistentStartIndex
        currentChat.value.messages.splice(persistentStartIndex, persistentDeletedCount)
        saveChatsToStorage()
        logger.info('重新生成：从持久化存储中删除消息', { 
          persistentStartIndex, 
          persistentDeletedCount 
        })
      }
    }
  }
  
  // 提取用户原始输入（移除@知识库标记）
  let text = userMessage.content
  const kbRegex = /@[\w\s]+/g
  text = text.replace(kbRegex, '').trim()
  
  // 从用户消息中提取知识库信息
  let knowledgeId = null
  let knowledgeIds = []
  const kbNames = userMessage.content.match(/@[\w\s]+/g) || []
  if (kbNames.length > 0) {
    // 从用户消息内容中提取知识库名称，查找对应的知识库ID
    for (const kbName of kbNames) {
      const kbNameClean = kbName.replace('@', '').trim()
      const kb = knowledgeBaseList.value.find(k => k.name === kbNameClean)
      if (kb && kb.id) {
        knowledgeIds.push(kb.id)
      }
    }
    if (knowledgeIds.length > 0) {
      knowledgeId = knowledgeIds[0] // 兼容旧接口
    }
  }
  
  // 从用户消息中提取附件信息
  const userAttachments = userMessage.attachments || []
  const localFiles = userAttachments
    .filter(att => att.type !== 'knowledge' && att.file)
    .map(att => att.file)
  const knowledgeFileAttachmentIds = userAttachments
    .filter(att => att.type === 'knowledge' && att.attachmentId != null)
    .map(att => att.attachmentId)
  
  const filesToUpload = localFiles.length > 0 ? localFiles : null
  const knowledgeIdToSend = knowledgeIds.length > 0 ? knowledgeIds : (knowledgeId ? [knowledgeId] : undefined)
  
  // 获取会话ID
  const conversationId = currentChat.value?.id || currentConversationId.value || userMessage.conversationId
  
  // 创建新的AI消息占位
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 10000)
  const aiMessageId = `${timestamp}-${random}`
  const aiMessage = {
    id: aiMessageId,
    type: 'ai',
    content: '',
    timestamp: new Date(),
    streaming: true,
    documents: []
  }
  
  messages.value.push(aiMessage)
  logger.info('重新生成：AI消息占位已添加', { id: aiMessageId })
  
  nextTick(() => {
    scrollKbToBottom()
  })
  
  // 设置生成标志
  isGenerating.value = true
  currentAbortController.value = new AbortController()
  
  try {
    logger.info('重新生成：开始流式问答', { 
      knowledgeId: knowledgeIdToSend || '无知识库',
      query: text,
      conversationId: conversationId,
      filesCount: filesToUpload?.length || 0,
      attachmentIdsCount: knowledgeFileAttachmentIds.length
    })
    
    // 直接调用流式生成API，不创建新的用户消息
    const abortController = await runWorkflowStream({
      query: text,
      knowledgeId: knowledgeIdToSend || undefined,
      workflowId: undefined,
      files: filesToUpload || undefined,
      attachmentIds: knowledgeFileAttachmentIds.length > 0 ? knowledgeFileAttachmentIds : undefined,
      conversationId: conversationId || undefined,
      enableSearch: enableSearch.value, // 是否启用联网搜索
      onMessage: (answer) => {
        const message = messages.value.find(m => m.id === aiMessageId)
        if (message) {
          message.content += answer
          nextTick(() => {
            scrollKbToBottom()
          })
        }
      },
      onEnd: async (data) => {
        const message = messages.value.find(m => m.id === aiMessageId)
        if (message) {
          message.streaming = false
          if (data.documents && Array.isArray(data.documents) && data.documents.length > 0) {
            message.documents = data.documents.map(doc => ({
              id: doc.document_id || doc.documentId || doc.id || doc.segment_id || doc.segmentId,
              documentId: doc.document_id || doc.documentId || doc.id,
              name: doc.document_name || doc.name || `文档 ${doc.id || doc.segment_id || ''}`,
              document_name: doc.document_name || doc.name,
              datasetName: doc.dataset_name || doc.datasetName,
              dataset_name: doc.dataset_name || doc.datasetName,
              content: doc.content || '',
              score: doc.score || 0,
              segmentId: doc.segment_id || doc.segmentId,
              segment_id: doc.segment_id || doc.segmentId
            }))
          } else {
            message.documents = []
          }
          message.conversationId = data.conversationId || conversationId
        }
        
        if (data.conversationId) {
          currentConversationId.value = data.conversationId
        }
        
        // 保存AI消息到后端
        const finalConversationId = conversationId || data.conversationId
        if (finalConversationId && message && message.content) {
          try {
            const sourcesJson = message.documents && message.documents.length > 0
              ? JSON.stringify(message.documents.map(doc => ({
                  document_id: doc.document_id || doc.documentId || doc.id,
                  document_name: doc.document_name || doc.name,
                  dataset_name: doc.dataset_name || doc.datasetName,
                  content: doc.content,
                  score: doc.score,
                  segment_id: doc.segment_id || doc.segmentId
                })))
              : null
            
            const messageResp = await createAiMessage({
              conversationId: String(finalConversationId),
              role: 'assistant',
              content: message.content,
              difyMessageId: data.messageId || null,
              sources: sourcesJson,
              confidence: null
            })
            
            if (messageResp.code === 200 && messageResp.data) {
              message.id = messageResp.data.id
              message.timestamp = new Date(messageResp.data.created_time || messageResp.data.createdTime)
              logger.info('重新生成：保存AI消息成功', { messageId: messageResp.data.id })
            }
          } catch (error) {
            logger.warn('重新生成：保存AI消息到后端失败', error)
          }
        }
        
        // 保存消息到当前对话
        if (currentChat.value && currentChat.value.messages) {
          const messageIndex = currentChat.value.messages.findIndex(m => m.id === aiMessageId)
          if (messageIndex !== -1) {
            currentChat.value.messages[messageIndex] = message
            saveChatsToStorage()
          }
        }
        
        isGenerating.value = false
        currentAbortController.value = null
        
        nextTick(() => {
          scrollKbToBottom()
        })
        
        logger.info('重新生成：流式问答完成', { 
          conversationId: finalConversationId,
          messageId: data.messageId,
          documentCount: (data.documents || []).length
        })
      },
      onError: (error) => {
        isGenerating.value = false
        currentAbortController.value = null
        
        const message = messages.value.find(m => m.id === aiMessageId)
        if (message) {
          message.streaming = false
          if (!message.content) {
            message.content = t('ai.chat.generateError') || '生成失败，请重试'
          }
        }
        
        logger.error('重新生成：流式问答失败', error)
        ElMessage.error(t('ai.chat.generateError') || '生成失败，请重试')
      }
    })
    
    currentAbortController.value = abortController
  } catch (error) {
    isGenerating.value = false
    currentAbortController.value = null
    
    const message = messages.value.find(m => m.id === aiMessageId)
    if (message) {
      message.streaming = false
      if (!message.content) {
        message.content = t('ai.chat.generateError') || '生成失败，请重试'
      }
    }
    
    logger.error('重新生成：调用流式接口失败', error)
    ElMessage.error(t('ai.chat.generateError') || '生成失败，请重试')
  }
}

// 编辑用户消息相关
// 自动调整编辑输入框高度
// 🔥 修复：自动调整编辑输入框高度 - 参考千问的设计，完全自适应
const autoResizeEditInput = (textarea) => {
  if (textarea) {
    // 重置高度为auto，让内容自然撑开
    textarea.style.height = 'auto'
    textarea.style.maxHeight = 'none'
    textarea.style.overflowY = 'hidden'
    // 设置高度为scrollHeight（内容高度），完全自适应
    textarea.style.height = textarea.scrollHeight + 'px'
  }
}

// 🔥 修复：自动调整编辑框高度 - 参考千问设计
const autoResizeEditBox = (element) => {
  if (element) {
    // 重置高度为auto，让内容自然撑开
    element.style.height = 'auto'
    element.style.maxHeight = 'none'
    element.style.overflowY = 'hidden'
    // 设置高度为scrollHeight（内容高度），完全自适应
    const scrollHeight = element.scrollHeight
    element.style.height = scrollHeight + 'px'
    
    // 同时调整父容器的高度
    const editBox = element.closest('.kb-edit-box')
    if (editBox) {
      editBox.style.height = 'auto'
      editBox.style.minHeight = Math.max(60, scrollHeight + 50) + 'px' // 内容高度 + 按钮区域高度
    }
  }
}

// 🔥 修复：开始编辑用户消息 - 参考千问设计，可变高度的编辑框
const startEditUserMessage = (msg) => {
  if (msg.type !== 'user') return
  
  // 🔥 修复：正确提取原始消息内容
  // 1. 如果消息有原始内容字段，优先使用
  // 2. 否则从 content 中提取纯文本（移除所有HTML标签和格式化）
  let textContent = ''
  
  // 优先使用原始内容（如果存在）
  if (msg.originalContent && typeof msg.originalContent === 'string') {
    textContent = msg.originalContent
  } else if (msg.content && typeof msg.content === 'string') {
    // 创建一个临时DOM元素来提取纯文本，这样可以正确处理所有HTML实体和标签
    const tempDiv = document.createElement('div')
    tempDiv.innerHTML = msg.content
    textContent = tempDiv.textContent || tempDiv.innerText || ''
    
    // 如果提取失败，使用正则表达式作为后备方案
    if (!textContent || textContent.trim() === '') {
      textContent = msg.content
        .replace(/<[^>]*>/g, '') // 移除HTML标签
        .replace(/&nbsp;/g, ' ') // 替换 &nbsp; 为空格
        .replace(/&amp;/g, '&') // 替换 &amp; 为 &
        .replace(/&lt;/g, '<') // 替换 &lt; 为 <
        .replace(/&gt;/g, '>') // 替换 &gt; 为 >
        .replace(/&quot;/g, '"') // 替换 &quot; 为 "
        .replace(/&#39;/g, "'") // 替换 &#39; 为 '
        .replace(/<br\s*\/?>/gi, '\n') // 替换 <br> 为换行
        .replace(/\n+/g, '\n') // 合并多个换行
        .trim()
    }
  }
  
  // 保存原始内容（如果还没有保存）
  if (!msg.originalContent) {
    msg.originalContent = textContent
  }
  
  msg.editing = true
  msg.editContent = textContent
  
  nextTick(() => {
    // 找到当前消息的编辑内容区域
    const messageElement = document.querySelector(`[data-message-id="${msg.id}"]`)
    if (messageElement) {
      const editContent = messageElement.querySelector('.kb-edit-content')
      if (editContent) {
        // 🔥 修复：使用 textContent 设置内容，而不是 Vue 插值
        // 这样可以确保内容正确显示，不会受到HTML转义的影响
        editContent.textContent = textContent
        
        // 重置样式，确保自适应
        editContent.style.height = 'auto'
        editContent.style.maxHeight = 'none'
        editContent.style.overflowY = 'hidden'
        
        editContent.focus()
        // 🔥 修复：不选中所有文本，只将光标定位到文本末尾（更好的用户体验）
        // 对于 contenteditable 元素，使用更简单的方法将光标定位到末尾
        try {
          const selection = window.getSelection()
          const range = document.createRange()
          
          // 将光标定位到文本末尾（不选中文本）
          if (editContent.childNodes.length > 0) {
            // 如果有子节点，定位到最后一个文本节点的末尾
            const lastNode = editContent.childNodes[editContent.childNodes.length - 1]
            if (lastNode.nodeType === Node.TEXT_NODE) {
              range.setStart(lastNode, lastNode.textContent?.length || 0)
              range.collapse(true)
            } else {
              // 如果最后一个节点不是文本节点，定位到元素末尾
              range.selectNodeContents(editContent)
              range.collapse(false)
            }
          } else {
            // 如果没有子节点，直接定位到元素末尾
            range.selectNodeContents(editContent)
            range.collapse(false)
          }
          
          selection.removeAllRanges()
          selection.addRange(range)
        } catch (error) {
          // 如果设置光标位置失败，至少确保元素获得焦点
          console.warn('设置光标位置失败', error)
        }
        
        // 自动调整高度
        autoResizeEditBox(editContent)
      }
    }
  })
}

// 取消编辑用户消息
const cancelEditUserMessage = (msg) => {
  msg.editing = false
  msg.editContent = ''
}

// 确认编辑用户消息（相当于重新发送）
const confirmEditUserMessage = async (msg) => {
  if (!msg.editContent || !msg.editContent.trim()) return
  
  const newContent = msg.editContent.trim()
  
  // 🔥 修复：更新消息内容和原始内容
  msg.content = newContent
  msg.originalContent = newContent // 保存原始内容，确保下次编辑时能正确读取
  msg.editing = false
  msg.editContent = ''
  
  // 更新持久化存储
  if (currentChat.value && currentChat.value.messages) {
    const persistentMsg = currentChat.value.messages.find(m => m.id === msg.id)
    if (persistentMsg) {
      persistentMsg.content = newContent
      persistentMsg.originalContent = newContent // 同时保存原始内容
      saveChatsToStorage()
    }
  }
  
  // 找到当前消息的位置，移除之后的所有AI回复
  const messageIndex = messages.value.findIndex(m => m.id === msg.id)
  if (messageIndex !== -1) {
    // 移除后续的AI回复
    for (let i = messages.value.length - 1; i > messageIndex; i--) {
      if (messages.value[i].type === 'ai') {
        messages.value.splice(i, 1)
      }
    }
    
    // 从持久化存储中移除后续AI回复
    if (currentChat.value && currentChat.value.messages) {
      const persistentIndex = currentChat.value.messages.findIndex(m => m.id === msg.id)
      if (persistentIndex !== -1) {
        for (let i = currentChat.value.messages.length - 1; i > persistentIndex; i--) {
          if (currentChat.value.messages[i].type === 'ai') {
            currentChat.value.messages.splice(i, 1)
          }
        }
        saveChatsToStorage()
      }
    }
  }
  
  // 🔥 修复：从消息内容中提取知识库信息和问题内容，直接调用runWorkflowStream重新生成回复
  // 不调用sendKbMessage，避免创建新的用户消息
  const rawText = newContent
  
  // 从消息内容中提取知识库名（匹配@知识库名格式）
  const kbMatches = rawText.match(/@([^\s@]+)/g) || []
  const matchedKbs = []
  if (kbMatches.length > 0) {
    kbMatches.forEach(kbMatch => {
      const kbName = kbMatch.substring(1) // 移除@符号
      const kb = knowledgeBaseList.value.find(k => k.name === kbName)
      if (kb) {
        matchedKbs.push(kb)
      }
    })
  }
  
  // 移除所有 @知识库名字 的内容，只保留实际的问题内容
  let text = rawText
  if (matchedKbs.length > 0) {
    for (const kb of matchedKbs) {
      const kbText = `@${kb.name}`
      text = text.replace(new RegExp(`\\s*${kbText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\s*`, 'g'), ' ').trim()
    }
    text = text.replace(/\s+/g, ' ').trim()
  }
  
  // 收集知识库ID
  let knowledgeIds = []
  if (matchedKbs.length > 0) {
    knowledgeIds = matchedKbs.map(kb => String(kb.id))
  }
  
  // 获取会话ID
  const conversationId = currentChat.value?.id || currentConversationId.value
  
  // 创建AI消息占位
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 10000)
  const aiMessageId = `${timestamp + 1}-${random}`
  const aiMessage = {
    id: aiMessageId,
    type: 'ai',
    content: '',
    timestamp: new Date(),
    streaming: true,
    documents: []
  }
  
  messages.value.push(aiMessage)
  
  nextTick(() => {
    scrollKbToBottom()
  })
  
  // 设置生成标志
  isGenerating.value = true
  
  try {
    const knowledgeIdToSend = knowledgeIds.length > 0 ? knowledgeIds : undefined
    const abortController = await runWorkflowStream({
      query: text,
      knowledgeId: knowledgeIdToSend,
      workflowId: undefined,
      files: undefined,
      conversationId: conversationId || undefined,
      enableSearch: enableSearch.value, // 是否启用联网搜索
      onMessage: (answer) => {
        const message = messages.value.find(m => m.id === aiMessageId)
        if (message) {
          message.content += answer
          nextTick(() => {
            scrollKbToBottom()
            setTimeout(() => {
              checkScrollPosition()
            }, 100)
          })
        }
      },
      onEnd: async (data) => {
        const message = messages.value.find(m => m.id === aiMessageId)
        if (message) {
          message.streaming = false
          if (data.documents && Array.isArray(data.documents) && data.documents.length > 0) {
            message.documents = data.documents.map(doc => ({
              id: doc.document_id || doc.documentId || doc.id || doc.segment_id || doc.segmentId,
              documentId: doc.document_id || doc.documentId || doc.id,
              name: doc.document_name || doc.name || `文档 ${doc.id || doc.segment_id || ''}`,
              document_name: doc.document_name || doc.name,
              datasetName: doc.dataset_name || doc.datasetName,
              dataset_name: doc.dataset_name || doc.datasetName,
              content: doc.content || '',
              score: doc.score || 0,
              segmentId: doc.segment_id || doc.segmentId,
              segment_id: doc.segment_id || doc.segmentId
            }))
          } else {
            message.documents = []
          }
          message.conversationId = data.conversationId || conversationId
        }
        
        if (data.conversationId) {
          currentConversationId.value = data.conversationId
        }
        
        const finalConversationId = conversationId || data.conversationId
        
        // 保存AI消息到后端
        if (finalConversationId && message && message.content) {
          try {
            const sourcesJson = message.documents && message.documents.length > 0
              ? JSON.stringify(message.documents.map(doc => ({
                  document_id: doc.document_id || doc.documentId || doc.id,
                  document_name: doc.document_name || doc.name,
                  dataset_name: doc.dataset_name || doc.datasetName,
                  content: doc.content,
                  score: doc.score,
                  segment_id: doc.segment_id || doc.segmentId
                })))
              : null
            
            const messageResp = await createAiMessage({
              conversationId: String(finalConversationId),
              role: 'assistant',
              content: message.content,
              difyMessageId: data.messageId || null,
              sources: sourcesJson,
              confidence: null
            })
            
            if (messageResp.code === 200 && messageResp.data) {
              message.id = messageResp.data.id
              message.timestamp = new Date(messageResp.data.created_time || messageResp.data.createdTime)
            }
          } catch (error) {
            logger.warn('保存AI消息到后端失败', error)
          }
        }
        
        // 保存到当前对话
        if (currentChat.value && currentChat.value.messages) {
          const messageIndex = currentChat.value.messages.findIndex(m => m.id === aiMessageId)
          if (messageIndex !== -1) {
            currentChat.value.messages[messageIndex] = message
          } else {
            currentChat.value.messages.push(message)
          }
          saveChatsToStorage()
        }
        
        isGenerating.value = false
        currentAbortController.value = null
        
        nextTick(() => {
          scrollKbToBottom()
          setTimeout(() => {
            checkScrollPosition()
          }, 100)
        })
      },
      onError: (error) => {
        isGenerating.value = false
        currentAbortController.value = null
        logger.error('编辑消息后重新生成回复失败', error)
        ElMessage.error(t('ai.chat.generateFailed') || '生成回复失败')
        // 移除AI消息占位
        const index = messages.value.findIndex(m => m.id === aiMessageId)
        if (index > -1) {
          messages.value.splice(index, 1)
        }
      }
    })
    
    currentAbortController.value = abortController
  } catch (error) {
    isGenerating.value = false
    currentAbortController.value = null
    logger.error('编辑消息后重新生成回复失败', error)
    ElMessage.error(t('ai.chat.generateFailed') || '生成回复失败')
    // 移除AI消息占位
    const index = messages.value.findIndex(m => m.id === aiMessageId)
    if (index > -1) {
      messages.value.splice(index, 1)
    }
  }
}

// 🔥 修复：处理输入框获得焦点事件，如果存在正在编辑的消息，自动取消编辑状态
const handleInputFocus = () => {
  // 如果用户在对话框中获得焦点，且存在正在编辑的消息，自动取消编辑状态
  const editingMessage = messages.value.find(m => m.editing && m.type === 'user')
  if (editingMessage) {
    // 用户开始输入新消息，取消编辑状态
    cancelEditUserMessage(editingMessage)
  }
}

// @知识库选择相关方法
const handleInputChange = (event) => {
  // 自动调整输入框高度
  autoResizeInput(event)
  
  // 🔥 修复：如果用户在对话框中输入消息，且存在正在编辑的消息，自动取消编辑状态
  const editingMessage = messages.value.find(m => m.editing && m.type === 'user')
  if (editingMessage && event.target.value.trim()) {
    // 用户开始输入新消息，取消编辑状态
    cancelEditUserMessage(editingMessage)
  }
  
  // 未登录用户不显示知识库列表
  if (!authStore.isLoggedIn) {
    showKnowledgeBaseList.value = false
    selectedKbIndex.value = -1
    return
  }
  
  // 如果知识库列表为空，不显示下拉框
  const validKbList = knowledgeBaseList.value.filter((kb) => kb && kb.name)
  if (validKbList.length === 0) {
    console.log('[handleInputChange] 知识库列表为空，不显示下拉框', {
      totalCount: knowledgeBaseList.value.length,
      validCount: validKbList.length,
      items: knowledgeBaseList.value.map(kb => ({ id: kb?.id, name: kb?.name, hasName: !!kb?.name }))
    })
    showKnowledgeBaseList.value = false
    selectedKbIndex.value = -1
    return
  }
  
  const value = event.target.value
  const cursorPos = event.target.selectionStart
  
  // 检查光标前是否有@符号且没有空格
  const lastAtIndex = value.lastIndexOf('@', cursorPos - 1)
  if (lastAtIndex !== -1) {
    const textAfterAt = value.substring(lastAtIndex + 1, cursorPos)
    // 如果@后面没有空格或换行，且没有选择知识库，显示列表
    if (!textAfterAt.includes(' ') && !textAfterAt.includes('\n')) {
      // 检查是否已经选择了知识库（添加空值检查）
      const hasSelectedKb = validKbList.some((kb) => {
        const kbText = `@${kb.name}`
        return value.substring(lastAtIndex, cursorPos).includes(kbText)
      })
      
      if (!hasSelectedKb) {
        console.log('[handleInputChange] 显示知识库列表', {
          validKbCount: validKbList.length,
          items: validKbList.map(kb => ({ id: kb.id, name: kb.name }))
        })
        // 🔥 修复：如果知识库列表为空，先加载
        if (knowledgeBaseList.value.length === 0) {
          loadKnowledgeBases(false)
        }
        showKnowledgeBaseList.value = true
        selectedKbIndex.value = 0 // 默认选中第一个
      } else {
        showKnowledgeBaseList.value = false
        selectedKbIndex.value = -1
      }
    } else {
      // @后面有空格，隐藏列表
      showKnowledgeBaseList.value = false
      selectedKbIndex.value = -1
    }
  } else {
    // 光标前没有@符号，隐藏知识库列表
    showKnowledgeBaseList.value = false
    selectedKbIndex.value = -1
  }
}

const selectKnowledgeBase = (kb) => {
  // 安全检查：确保 kb 存在且有 name 属性
  if (!kb || !kb.name) {
    console.warn('[selectKnowledgeBase] 无效的知识库对象', kb)
    return
  }
  
  const textarea = document.querySelector('.kb-message-input')
  if (textarea) {
    const cursorPos = textarea.selectionStart
    const value = textarea.value
    const lastAtIndex = value.lastIndexOf('@', cursorPos - 1)
    
    if (lastAtIndex !== -1) {
      // 检查是否已经选择过这个知识库
      const kbText = `@${kb.name}`
      const alreadySelected = selectedKnowledgeBases.value.find(k => k.id === kb.id)
      
      if (!alreadySelected) {
        // 添加到已选择列表
        selectedKnowledgeBases.value.push(kb)
      }
      
      // 替换@符号为知识库名称
      const newValue = value.substring(0, lastAtIndex) + `${kbText} ` + value.substring(cursorPos)
      inputMessage.value = newValue
      
      // 更新光标位置
      nextTick(() => {
        const newCursorPos = lastAtIndex + kbText.length + 1 // +1 for space
        textarea.setSelectionRange(newCursorPos, newCursorPos)
        textarea.focus()
      })
    }
  }
  
  showKnowledgeBaseList.value = false
  selectedKbIndex.value = -1
  ElMessage.success(t('ai.chat.knowledgeBaseSelected', { name: kb.name }))
}

const isKbSelected = (kbId) => {
  return selectedKnowledgeBases.value.some(kb => kb.id === kbId)
}

const getKbIcon = (kb) => {
  // 可以根据知识库类型返回不同的图标
  return kb.icon || '📚'
}

// 🔥 新增：获取知识库封面URL（复用KnowledgeList.vue的逻辑）
const getKbCoverUrl = (kb) => {
  if (!kb) return null
  
  const coverUrl = kb.coverUrl
  const coverFileId = kb.coverFileId
  
  // 如果有完整的 URL（http/https），直接使用
  if (coverUrl && (coverUrl.startsWith('http://') || coverUrl.startsWith('https://'))) {
    return coverUrl
  }
  
  // 如果是相对路径（以 / 开头），直接使用
  if (coverUrl && coverUrl.startsWith('/')) {
    return coverUrl
  }
  
  // 如果有文件 ID，使用预览接口
  if (coverFileId) {
    return `/api/file/preview/${coverFileId}`
  }
  
  // 如果 coverUrl 是纯数字，可能是文件 ID
  if (coverUrl && /^\d+$/.test(String(coverUrl))) {
    return `/api/file/preview/${coverUrl}`
  }
  
  // 如果 coverUrl 是 MinIO 路径格式（bucketName/filePath），使用预览接口
  if (coverUrl && coverUrl.includes('/') && !coverUrl.startsWith('/')) {
    if (coverFileId) {
      return `/api/file/preview/${coverFileId}`
    }
  }
  
  return null
}

// 🔥 新增：处理知识库搜索输入（防抖）
const handleKbSearchInput = () => {
  if (kbListSearchTimer.value) {
    clearTimeout(kbListSearchTimer.value)
  }
  
  kbListSearchTimer.value = setTimeout(() => {
    // 重置分页并重新加载
    kbListPagination.value.pageNo = 1
    loadKnowledgeBases()
  }, 500) // 500ms 防抖
}

// 🔥 新增：处理知识库下拉框滚动事件（滚动加载更多）
const kbDropdownSectionRef = ref(null)
const kbDropdownSectionRef2 = ref(null)
const handleKbDropdownScroll = (event) => {
  const target = event.target
  const scrollTop = target.scrollTop
  const scrollHeight = target.scrollHeight
  const clientHeight = target.clientHeight
  
  // 距离底部50px时加载更多
  if (scrollHeight - scrollTop - clientHeight < 50) {
    if (kbListPagination.value.hasMore && !kbListPagination.value.loading && !isLoadingKnowledgeBases.value) {
      kbListPagination.value.pageNo++
      loadKnowledgeBases(true) // 传递isLoadMore=true
    }
  }
}

// 键盘导航知识库列表
const navigateKbList = (direction) => {
  if (!showKnowledgeBaseList.value) return
  
  const list = filteredKnowledgeBases.value
  if (direction === 'up') {
    selectedKbIndex.value = selectedKbIndex.value > 0
      ? selectedKbIndex.value - 1
      : list.length - 1
  } else if (direction === 'down') {
    selectedKbIndex.value = selectedKbIndex.value < list.length - 1
      ? selectedKbIndex.value + 1
      : 0
  }
}

// 处理Enter键
const handleEnterKey = (event) => {
  console.log('[handleEnterKey] Enter键被按下', { 
    shiftKey: event.shiftKey,
    isSending: isSendingMessage.value, 
    isGenerating: isGenerating.value 
  })
  
  // 如果按的是 Shift+Enter，允许换行
  if (event.shiftKey) {
    console.log('[handleEnterKey] Shift+Enter，允许换行')
    return
  }
  
  // 阻止默认行为并停止冒泡
  event.preventDefault()
  event.stopPropagation()
  event.stopImmediatePropagation()
  
  if (showKnowledgeBaseList.value && selectedKbIndex.value >= 0) {
    // 如果有选中的知识库，选择它
    const list = filteredKnowledgeBases.value
    if (selectedKbIndex.value < list.length) {
      const selectedKb = list[selectedKbIndex.value]
      if (selectedKb) {
        selectKnowledgeBase(selectedKb)
      }
    }
  } else {
    // 否则发送消息
    console.log('[handleEnterKey] 调用 sendKbMessage')
    sendKbMessage()
  }
  return false // 额外返回 false 确保阻止
}

// 隐藏知识库列表
const hideKnowledgeBaseList = () => {
  showKnowledgeBaseList.value = false
  selectedKbIndex.value = -1
}

// 智能删除处理
const handleBackspace = (event) => {
  const textarea = event.target
  const cursorPos = textarea.selectionStart
  const value = textarea.value
  
  // 检查光标前是否有知识库文本
  for (const kb of selectedKnowledgeBases.value) {
    const kbText = `@${kb.name}`
    const kbStartPos = value.lastIndexOf(kbText, cursorPos - 1)
    
    if (kbStartPos !== -1 && kbStartPos + kbText.length === cursorPos) {
      // 如果光标正好在知识库文本的末尾，整体删除
      event.preventDefault()
      const newValue = value.substring(0, kbStartPos) + value.substring(cursorPos)
      inputMessage.value = newValue
      
      // 从已选择列表中移除
      const index = selectedKnowledgeBases.value.findIndex(k => k.id === kb.id)
      if (index > -1) {
        selectedKnowledgeBases.value.splice(index, 1)
      }
      
      nextTick(() => {
        textarea.setSelectionRange(kbStartPos, kbStartPos)
        textarea.focus()
      })
      return
    }
  }
}

// 自动调整输入框高度
const autoResizeInput = (event) => {
  if (event && event.target) {
    const textarea = event.target
    textarea.style.height = 'auto'
    const scrollHeight = textarea.scrollHeight
    const minHeight = 44
    const maxHeight = 200
    const newHeight = Math.min(Math.max(scrollHeight, minHeight), maxHeight)
    textarea.style.height = newHeight + 'px'
    if (scrollHeight > maxHeight) {
      textarea.style.overflowY = 'auto'
    } else {
      textarea.style.overflowY = 'hidden'
    }
  }
}

// 🔥 修复：模型选择器相关方法 - 有消息时向上显示，没消息时向下显示
// 切换联网搜索开关
const toggleEnableSearch = () => {
  enableSearch.value = !enableSearch.value
  logger.info('切换联网搜索', { enableSearch: enableSearch.value })
}

const toggleModelDropdown = () => {
  showModelDropdown.value = !showModelDropdown.value
  
  if (showModelDropdown.value) {
    // 🔥 修复：根据是否有消息来决定下拉框方向
    // 有消息时向上显示，没消息时向下显示
    nextTick(() => {
      const selector = document.querySelector('.custom-model-selector')
      const dropdown = document.querySelector('.model-dropdown')
      if (selector && dropdown) {
        const rect = selector.getBoundingClientRect()
        const viewportHeight = window.innerHeight
        const dropdownHeight = 300 // max-height
        const spaceBelow = viewportHeight - rect.bottom
        const spaceAbove = rect.top
        
        // 🔥 修复：有消息时向上显示，没消息时向下显示
        const hasMessages = messages.value && messages.value.length > 0
        
        if (hasMessages) {
          // 有消息时，向上显示
          dropdown.style.top = 'auto'
          dropdown.style.bottom = `${viewportHeight - rect.top + 8}px`
          dropdown.style.left = `${rect.left}px`
          dropdown.style.right = 'auto'
          dropdown.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)' // 向上显示时阴影在下方
        } else {
          // 没消息时，向下显示（如果下方空间不足，才向上）
          if (spaceBelow < dropdownHeight && spaceAbove > spaceBelow) {
            dropdown.style.top = 'auto'
            dropdown.style.bottom = `${viewportHeight - rect.top + 8}px`
            dropdown.style.left = `${rect.left}px`
            dropdown.style.right = 'auto'
            dropdown.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)'
          } else {
            // 默认向下显示
            dropdown.style.top = `${rect.bottom + 8}px`
            dropdown.style.bottom = 'auto'
            dropdown.style.left = `${rect.left}px`
            dropdown.style.right = 'auto'
            dropdown.style.boxShadow = '0 -4px 12px rgba(0, 0, 0, 0.15)' // 向下显示时阴影在上方
          }
        }
      }
    })
  }
}

const getSelectedModelName = (modelValue) => {
  const model = modelOptions.value.find(m => m.value === modelValue)
  return model ? model.name : modelValue
}

const selectModel = (model) => {
  selectedModel.value = model
  showModelDropdown.value = false
  ElMessage.success(t('ai.chat.switchedTo', { model: getSelectedModelName(model) }))
}

// 附件相关方法
const handleAttachmentClick = () => {
  // 🔥 修复：检查用户是否已登录
  if (!authStore.isLoggedIn) {
    ElMessage.warning(t('user.pleaseLogin'))
    openLoginModal()
    return
  }
  // 如果已登录，直接触发文件选择
  fileInput.value && fileInput.value.click()
}

// 🔥 修复：处理附件下拉菜单的显示状态变化
const handleAttachmentDropdownVisible = (visible) => {
  showAttachmentDropdown.value = visible
}

const handleAttachmentCommand = (command) => {
  // 🔥 修复：检查用户是否已登录
  if (!authStore.isLoggedIn) {
    ElMessage.warning(t('user.pleaseLogin'))
    openLoginModal()
    return
  }
  
  // 🔥 修复：关闭下拉菜单
  showAttachmentDropdown.value = false
  
  if (command === 'local') {
    // 选择本地文件
    fileInput.value && fileInput.value.click()
  } else if (command === 'knowledge') {
    // 选择知识库文件
    openKnowledgeDialog()
  }
}

// 打开知识库选择对话框
const openKnowledgeDialog = async () => {
  showKnowledgeDialog.value = true
  selectedKnowledgeForFile.value = null
  
  // 重置搜索和分页
  kbSelectSearchQuery.value = ''
  kbSelectPagination.value.pageNo = 1
  
  // 加载知识库列表
  await loadKnowledgeListForSelect(false)
}

// 🔥 新增：处理附件按钮知识库选择的搜索输入（防抖）
const handleKbSelectSearchInput = () => {
  if (kbSelectSearchTimer.value) {
    clearTimeout(kbSelectSearchTimer.value)
  }
  
  kbSelectSearchTimer.value = setTimeout(() => {
    // 重置分页并重新加载
    kbSelectPagination.value.pageNo = 1
    loadKnowledgeListForSelect(false)
  }, 500) // 500ms 防抖
}

// 🔥 新增：处理附件按钮知识库选择对话框的滚动事件（滚动加载更多）
const handleKbSelectScroll = (event) => {
  const target = event.target
  const scrollTop = target.scrollTop
  const scrollHeight = target.scrollHeight
  const clientHeight = target.clientHeight
  
  // 距离底部50px时加载更多
  if (scrollHeight - scrollTop - clientHeight < 50) {
    if (kbSelectPagination.value.hasMore && !kbSelectPagination.value.loading && !loadingKnowledgeList.value) {
      kbSelectPagination.value.pageNo++
      loadKnowledgeListForSelect(true) // 传递isLoadMore=true
    }
  }
}

// 加载知识库列表（用于文件选择）
const loadKnowledgeListForSelect = async (isLoadMore = false) => {
  if (kbSelectPagination.value.loading) {
    return
  }
  
  kbSelectPagination.value.loading = true
  loadingKnowledgeList.value = true
  
  try {
    if (!isLoadMore) {
      // 重置分页
      kbSelectPagination.value.pageNo = 1
      kbSelectPagination.value.hasMore = true
      knowledgeListForSelect.value = []
    }
    
    logger.info('加载知识库列表（用于文件选择）', {
      pageNo: kbSelectPagination.value.pageNo,
      pageSize: kbSelectPagination.value.pageSize,
      keyword: kbSelectSearchQuery.value
    })
    
    // 🔥 替换为新接口：使用分页查询接口
    const response = await getKnowledgeListPage({
      pageNo: kbSelectPagination.value.pageNo,
      pageSize: kbSelectPagination.value.pageSize,
      keyword: kbSelectSearchQuery.value || undefined
    })
    
    if (response.code === 200 && response.data) {
      const list = response.data.records || response.data.list || []
      
      if (isLoadMore) {
        // 追加数据
        knowledgeListForSelect.value.push(...list)
      } else {
        // 替换数据
        knowledgeListForSelect.value = list
      }
      
      // 更新分页信息
      kbSelectPagination.value.total = response.data.total || 0
      kbSelectPagination.value.hasMore = 
        knowledgeListForSelect.value.length < kbSelectPagination.value.total
      
      logger.info('知识库列表加载成功（用于文件选择）', {
        count: knowledgeListForSelect.value.length,
        total: kbSelectPagination.value.total,
        hasMore: kbSelectPagination.value.hasMore
      })
    } else {
      logger.warn('知识库列表响应异常', { code: response.code, data: response.data })
      if (!isLoadMore) {
        knowledgeListForSelect.value = []
      }
    }
  } catch (error) {
    logger.error('加载知识库列表失败', error)
    ElMessage.error(t('ai.chat.loadKnowledgeListFailed'))
    if (!isLoadMore) {
      knowledgeListForSelect.value = []
    }
  } finally {
    kbSelectPagination.value.loading = false
    loadingKnowledgeList.value = false
  }
}

// 选择知识库
const selectKnowledgeForFile = (kb) => {
  selectedKnowledgeForFile.value = kb
}

// 🔥 新增：处理知识库项双击事件（等同于点击确认按钮）
const handleKnowledgeItemDoubleClick = (kb) => {
  // 先选中该知识库
  selectKnowledgeForFile(kb)
  // 然后触发确认操作
  confirmKnowledgeSelection()
}

// 确认知识库选择，打开文档选择对话框
const confirmKnowledgeSelection = async () => {
  if (!selectedKnowledgeForFile.value) {
    ElMessage.warning(t('ai.chat.pleaseSelectKnowledgeBase'))
    return
  }
  
  // 关闭知识库选择弹窗，打开文档选择弹窗
  showKnowledgeDialog.value = false
  showDocumentDialog.value = true
  selectedDocuments.value = []
  
  // 加载该知识库的文档列表
  await loadDocumentsForKnowledge(selectedKnowledgeForFile.value.id)
}

// 加载知识库的文档列表
const loadDocumentsForKnowledge = async (knowledgeId) => {
  loadingDocuments.value = true
  try {
    const response = await getKnowledgeFileRelationList({
      knowledgeId: knowledgeId,
      page: 1,
      size: 1000
    })
    if (response.code === 200 && response.data) {
      documentList.value = response.data.records || response.data.list || []
      logger.info('文档列表加载成功', { knowledgeId, count: documentList.value.length })
    }
  } catch (error) {
    logger.error('加载文档列表失败', error)
    ElMessage.error(t('ai.chat.loadDocumentListFailed'))
    documentList.value = []
  } finally {
    loadingDocuments.value = false
  }
}

// 处理文档选择弹窗关闭（返回知识库选择弹窗）
const handleDocumentDialogClose = () => {
  // 🔥 修复：如果是确认操作，直接关闭，不返回上一层
  if (isConfirmingDocumentSelection.value) {
    isConfirmingDocumentSelection.value = false
    return
  }
  
  // 点击取消或关闭按钮时，返回到知识库选择弹窗
  showDocumentDialog.value = false
  selectedDocuments.value = []
  // 不清空 selectedKnowledgeForFile，以便用户可以重新选择文档
  // 重新打开知识库选择弹窗
  showKnowledgeDialog.value = true
}

// 处理文档选择弹窗取消按钮点击
const handleDocumentDialogCancel = () => {
  handleDocumentDialogClose()
}

// 确认文档选择
const confirmDocumentSelection = () => {
  if (selectedDocuments.value.length === 0) {
    ElMessage.warning(t('ai.chat.pleaseSelectAtLeastOneDocument'))
    return
  }
  
  // 检查知识库文件数量限制（最多3个）
  const currentKnowledgeFileCount = attachments.value.filter(att => att.type === 'knowledge').length
  const newKnowledgeFileCount = selectedDocuments.value.length
  
  if (currentKnowledgeFileCount + newKnowledgeFileCount > 3) {
    ElMessage.warning(t('ai.chat.maxKbFilesWarning', {
      current: currentKnowledgeFileCount,
      remaining: 3 - currentKnowledgeFileCount
    }))
    return
  }
  
  // 将选中的文档添加到附件列表
  const selectedDocList = documentList.value.filter(doc => 
    selectedDocuments.value.includes(doc.id)
  )
  
  let addedCount = 0
  selectedDocList.forEach(doc => {
    // 检查是否已添加
    const exists = attachments.value.some(att => 
      att.type === 'knowledge' && att.id === doc.id
    )
    
    if (!exists) {
      // 检查总文件数量限制（包括本地文件和知识库文件）
      if (attachments.value.length >= 10) {
        ElMessage.warning('最多只能添加10个文件（包括本地文件和知识库文件）')
        return
      }
      
      // 再次检查知识库文件数量限制
      const currentKbCount = attachments.value.filter(att => att.type === 'knowledge').length
      if (currentKbCount >= 3) {
        ElMessage.warning(t('ai.chat.maxKbFilesWarningSimple'))
        return
      }
      
      attachments.value.push({
        id: doc.id,
        name: doc.fileName || doc.name || t('ai.chat.unnamedDocument'),
        size: doc.fileSize ? formatFileSize(doc.fileSize) : '未知大小',
        type: 'knowledge', // 标记为知识库文件
        knowledgeId: selectedKnowledgeForFile.value.id,
        knowledgeName: selectedKnowledgeForFile.value.name,
        attachmentId: doc.attachmentId ? Number(doc.attachmentId) : null, // 使用 attachmentId 字段，转换为数字
        file: null // 知识库文件没有 File 对象
      })
      addedCount++
    }
  })
  
  // 🔥 修复：标记为确认操作，然后关闭所有弹窗
  isConfirmingDocumentSelection.value = true
  // 关闭所有弹窗（先关闭知识库选择弹窗，再关闭文档选择弹窗，避免触发返回逻辑）
  showKnowledgeDialog.value = false
  showDocumentDialog.value = false
  selectedDocuments.value = []
  selectedKnowledgeForFile.value = null
  
  if (addedCount > 0) {
    ElMessage.success(t('ai.chat.documentsAdded', { count: addedCount }))
  }
  
  // 延迟重置标记，确保 close 事件处理完成
  nextTick(() => {
    isConfirmingDocumentSelection.value = false
  })
}

const handleFileUpload = (event) => {
  const files = Array.from(event.target.files || [])
  
  // 检查文件数量限制
  if (attachments.value.length + files.length > 10) {
    ElMessage.warning('最多只能上传10个文件')
    return
  }
  
  files.forEach((file) => {
    // 检查文件大小限制 (100MB)
    if (file.size > 100 * 1024 * 1024) {
      ElMessage.warning(`文件 ${file.name} 超过100MB限制`)
      return
    }
    
    // 检查文件类型
    const allowedTypes = ['.pdf', '.doc', '.docx', '.ppt', '.pptx', '.xls', '.xlsx', '.csv', '.md', '.txt']
    const fileExtension = '.' + file.name.split('.').pop().toLowerCase()
    if (!allowedTypes.includes(fileExtension)) {
      ElMessage.warning(`文件 ${file.name} 格式不支持`)
      return
    }
    
    // 添加到附件列表
    attachments.value.push({
      name: file.name,
      size: formatFileSize(file.size),
      file: file,
      type: fileExtension
    })
  })
  
  // 清空文件输入
  event.target.value = ''
  ElMessage.success(`已添加 ${files.length} 个文件`)
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileIcon = (fileType) => {
  // 知识库文件使用特殊图标
  if (fileType === 'knowledge') {
    return '📚'
  }
  
  const iconMap = {
    '.pdf': '📕',
    '.doc': '📘',
    '.docx': '📘',
    '.ppt': '📗',
    '.pptx': '📗',
    '.xls': '📊',
    '.xlsx': '📊',
    '.csv': '📋',
    '.md': '📝',
    '.txt': '📄'
  }
  return iconMap[fileType] || '📄'
}

const removeAttachment = (index) => {
  attachments.value.splice(index, 1)
  ElMessage.success('已移除附件')
}

// 对话管理相关方法
const handleChatAction = async (command, chat) => {
  switch (command) {
    case 'edit':
      await editChatTitle(chat)
      break
    case 'pin':
      await togglePinChat(chat)
      break
    case 'delete':
      await deleteChatConfirm(chat)
      break
  }
}

const editChatTitle = async (chat) => {
  try {
    const { value: newTitle } = await ElMessageBox.prompt(
      t('ai.chat.enterNewTitle'),
      t('ai.chat.editTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        inputValue: chat.title || ''
      }
    )
    
    if (newTitle && newTitle.trim()) {
      chat.title = newTitle.trim()
      // 调用API更新对话标题
      try {
        // 使用新接口：更新AI会话
        await updateAiConversation({ id: chat.id, title: chat.title })
      } catch (error) {
        logger.warn('更新对话标题失败，尝试使用旧接口', error)
        // 如果新接口失败，尝试使用旧接口
        try {
          await updateConversation(chat.id, { title: chat.title })
        } catch (fallbackError) {
          logger.warn('使用旧接口更新对话标题也失败', fallbackError)
        }
      }
      saveChatsToStorage()
      ElMessage.success(t('ai.chat.titleUpdated'))
    }
  } catch (error) {
    // 用户取消
  }
}

const togglePinChat = async (chat) => {
  chat.pinned = !chat.pinned
  sortChats()
  saveChatsToStorage()
  
  // 调用API更新置顶状态
  try {
    // 使用新接口：更新AI会话置顶状态
    await updateAiConversationPinnedStatus(chat.id, chat.pinned ? 1 : 0)
  } catch (error) {
    logger.warn('更新置顶状态失败，尝试使用旧接口', error)
    // 如果新接口失败，尝试使用旧接口
    try {
      await updateConversation(chat.id, { pinned: chat.pinned })
    } catch (fallbackError) {
      logger.warn('使用旧接口更新置顶状态也失败', fallbackError)
    }
  }
  
  ElMessage.success(chat.pinned ? '对话已置顶' : '已取消置顶')
}

const deleteChatConfirm = async (chat) => {
  try {
    await ElMessageBox.confirm(
      t('ai.chat.deleteChatConfirm'),
      t('ai.chat.deleteChatTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    // 调用API删除对话
    try {
      // 使用新接口：删除AI会话
      await deleteAiConversation(chat.id)
    } catch (error) {
      logger.warn('删除对话API调用失败，尝试使用旧接口', error)
      // 如果新接口失败，尝试使用旧接口
      try {
        await deleteConversation(chat.id)
      } catch (fallbackError) {
        logger.warn('使用旧接口删除对话也失败', fallbackError)
      }
    }
    
    const index = chats.value.findIndex(c => c.id === chat.id)
    if (index !== -1) {
      chats.value.splice(index, 1)
    }
    
    if (currentChat.value && currentChat.value.id === chat.id) {
      currentChat.value = null
      messages.value = []
    }
    
    saveChatsToStorage()
    ElMessage.success(t('ai.chat.chatDeleted'))
  } catch (error) {
    // 用户取消
  }
}

// 点击外部关闭下拉框
const handleClickOutside = (event) => {
  if (!event.target.closest('.custom-model-selector')) {
    showModelDropdown.value = false
  }
  if (!event.target.closest('.knowledge-base-dropdown')) {
    hideKnowledgeBaseList()
  }
}

// 标记是否已经初始化过（避免重复加载）
const hasInitialized = ref(false)
// 标记是否正在加载对话列表（避免重复调用）
const isLoadingChats = ref(false)
// 标记是否正在加载知识库列表（避免重复调用）
const isLoadingKnowledgeBases = ref(false)

// 🔥 修复：创建全局共享的加载状态，避免 ToCSidebar 和 AIChat 同时调用接口
if (!window.__isLoadingConversations) {
  window.__isLoadingConversations = false
}

// 生命周期钩子
onMounted(async () => {
  try {
    logger.info('AI对话页面初始化开始')
    
    // 🔥 修复：初始化滚动位置检查，确保按钮能正确显示
    nextTick(() => {
      checkScrollPosition()
    })
    
    // 🔥 修复：确保组件容器已渲染
    await nextTick()
    
    // 只有登录用户才加载对话列表和知识库
    if (authStore.isLoggedIn) {
      // 标记已初始化，避免 watch 中重复调用
      hasInitialized.value = true
      
      // 加载对话列表（先尝试本地存储，再尝试API）
      loadChatsFromStorage()
      
      // 🔥 修复：确保三个接口都被调用
      // 1. 历史对话列表接口
      // 2. 知识库列表接口
      // 3. 默认选中第一个对话的消息列表接口
      logger.info('开始并行加载对话列表和知识库列表')
      await Promise.all([
        loadChats().catch(err => {
          logger.warn('加载对话列表失败，继续使用本地数据', err)
          // 🔥 修复：即使API失败，也确保页面可以显示
          if (chats.value.length === 0) {
            // 如果本地也没有数据，创建一个新对话
            createNewChat().catch(createErr => {
              logger.error('创建新对话失败', createErr)
            })
          }
        }),
        loadKnowledgeBases().catch(err => {
          logger.warn('加载知识库列表失败，继续初始化', err)
          // 🔥 修复：即使失败也要确保知识库列表被初始化
          knowledgeBaseList.value = []
        })
      ])
      
      logger.info('对话列表和知识库列表加载完成', {
        chatsCount: chats.value.length,
        kbCount: knowledgeBaseList.value.length
      })
      
      // 🔥 修复：只从 sessionStorage 获取对话ID，不在路由中显示参数
      const conversationIdFromStorage = sessionStorage.getItem('currentConversationId')
      
      // 清除URL中的conversationId参数（如果存在，保持URL干净）
      if (route.query.conversationId) {
        router.replace({ path: '/ai/chat', query: {} })
      }
      
      // 🔥 修复：登录后默认选中新建对话，不读取 sessionStorage 中的对话ID
      // 只有当用户主动点击历史对话时，才会设置 sessionStorage
      // 如果 sessionStorage 中有对话ID，说明用户之前选择了某个对话（可能是刷新页面），则加载该对话
      // 如果 sessionStorage 中没有对话ID，说明是新建对话状态，保持空状态
      if (conversationIdFromStorage && conversationIdFromStorage !== 'null' && conversationIdFromStorage !== 'undefined') {
        logger.info('从sessionStorage获取对话ID（可能是刷新页面）', conversationIdFromStorage)
        const chat = chats.value.find(c => String(c.id) === String(conversationIdFromStorage))
        if (chat) {
          logger.info('找到对话，加载消息列表', chat.id)
          await selectChat(chat, true)
        } else {
          // 如果对话列表中找不到，尝试加载该对话的消息
          logger.info('对话列表中找不到，直接加载消息列表', conversationIdFromStorage)
          // 先创建一个临时对话对象
          const tempChat = {
            id: conversationIdFromStorage,
            title: '',
            lastMessage: '',
            updatedAt: new Date(),
            unreadCount: 0,
            isNew: false,
            pinned: false,
            messages: []
          }
          // 添加到对话列表
          chats.value.unshift(tempChat)
          // 加载消息
          await loadMessages(conversationIdFromStorage, true)
          // 设置当前对话
          currentChat.value = tempChat
        }
      } else {
        // 🔥 修复：默认选中新建对话状态
        // 清除当前对话，显示空状态（新建对话界面）
        currentChat.value = null
        messages.value = []
        // 确保 sessionStorage 中没有对话ID
        sessionStorage.removeItem('currentConversationId')
        logger.info('没有对话ID，保持新建对话状态')
      }
      
      // 🔥 修复：添加 chatSelected 事件监听器，监听侧边栏选择对话
      window.addEventListener('chatSelected', handleChatSelected)
      // 🔥 修复：添加 createNewChatRequest 事件监听器，监听侧边栏新建对话请求
      window.addEventListener('createNewChatRequest', handleCreateNewChatRequest)
    } else {
      // 未登录用户：不需要加载知识库列表（需要登录才能使用知识库）
      knowledgeBaseList.value = []
    }
    
    document.addEventListener('click', handleClickOutside)
    
    // 🔥 修复：启动 sessionStorage 检查定时器（作为备用方案）
    if (storageCheckInterval) {
      clearInterval(storageCheckInterval)
    }
    storageCheckInterval = setInterval(() => {
      checkSessionStorageConversationId()
    }, 500)
    
    // 🔥 修复：初始化时检查滚动位置
    nextTick(() => {
      checkScrollPosition()
    })
    
    // 🔥 修复：监听消息变化，检查滚动位置
    watch(() => messages.value.length, () => {
      nextTick(() => {
        checkScrollPosition()
      })
    })
    
    // 🔥 修复：监听窗口大小变化，检查滚动位置
    const handleResize = () => {
      nextTick(() => {
        checkScrollPosition()
      })
    }
    window.addEventListener('resize', handleResize)
    
    // 保存 resize 监听器，用于卸载时移除
    windowResizeHandler = handleResize
    
    logger.info('AI对话页面初始化完成', {
      chatsCount: chats.value.length,
      kbCount: knowledgeBaseList.value.length,
      hasCurrentChat: !!currentChat.value
    })
    
    // 🔥 修复：确保页面可见，添加调试信息
    await nextTick()
    const container = containerRef.value || document.querySelector('.ai-chat-container')
    console.log('[AIChat] 页面初始化完成', {
      containerVisible: container !== null,
      containerElement: !!container,
      containerHeight: container?.offsetHeight || 0,
      containerWidth: container?.offsetWidth || 0,
      chatsCount: chats.value.length,
      currentChat: !!currentChat.value,
      computedStyle: container ? window.getComputedStyle(container) : null
    })
    
    // 🔥 如果容器高度为0，强制设置高度
    if (container && container.offsetHeight === 0) {
      console.warn('[AIChat] 容器高度为0，强制设置高度')
      const parent = container.parentElement
      if (parent) {
        const parentHeight = parent.offsetHeight || window.innerHeight - 56
        container.style.height = `${parentHeight}px`
        container.style.minHeight = `${parentHeight}px`
      }
    }
  } catch (error) {
    logger.error('AI对话页面初始化失败', error)
    console.error('[AIChat] 初始化失败', error)
    ElMessage.error('页面初始化失败，请刷新页面重试')
  }
})

// 🔥 修复：移除路由监听，改为监听 sessionStorage 变化
// 路由中不再显示 conversationId 参数，只使用 sessionStorage

// 🔥 修复：监听登录状态，只在真正登录成功时清除对话ID，刷新页面时保留
watch(() => authStore.isLoggedIn, async (isLoggedIn, wasLoggedIn) => {
  if (!isLoggedIn) {
    // 用户退出登录，清除当前对话和消息
    hasInitialized.value = false // 重置初始化标记
    currentChat.value = null
    messages.value = []
    chats.value = []
    knowledgeBaseList.value = [] // 清除知识库列表
    // 清除路由中的 conversationId 参数
    if (route.path === '/ai/chat' && route.query.conversationId) {
      router.replace({ path: '/ai/chat', query: {} }).catch(() => {
        // 忽略路由冗余导航错误
      })
    }
    // 清除 sessionStorage
    sessionStorage.removeItem('currentConversationId')
  } else if (isLoggedIn && wasLoggedIn === false) {
    // 🔥 修复：只有在真正登录成功时（从未登录变为已登录）才清除对话ID
    // 刷新页面时 wasLoggedIn 可能是 undefined，不应该清除
    // 只有当 wasLoggedIn 明确为 false 时，才是真正的登录成功
    const storedId = sessionStorage.getItem('currentConversationId')
    // 只有在 sessionStorage 中没有对话ID时，才清除（避免清除刷新页面时的状态）
    if (!storedId || storedId === 'null' || storedId === 'undefined') {
      logger.info('用户登录成功，清除之前的对话ID，默认选中新建对话')
      sessionStorage.removeItem('currentConversationId')
      currentChat.value = null
      messages.value = []
    } else {
      logger.info('用户登录成功，保留 sessionStorage 中的对话ID（可能是刷新页面）', storedId)
    }
    
    // 🔥 修复：只有在组件未初始化时才加载数据，避免重复调用
    if (!hasInitialized.value) {
      hasInitialized.value = true
      logger.info('用户登录成功，立即加载知识库列表和对话列表')
      try {
        await Promise.all([
          loadKnowledgeBases().catch(err => {
            logger.warn('登录后加载知识库列表失败', err)
            knowledgeBaseList.value = []
          }),
          loadChats().catch(err => {
            logger.warn('登录后加载对话列表失败', err)
          })
        ])
        
        logger.info('登录后知识库列表和对话列表加载完成', {
          kbCount: knowledgeBaseList.value.length,
          chatsCount: chats.value.length
        })
      } catch (error) {
        logger.error('登录后加载数据失败', error)
      }
    } else {
      logger.info('组件已初始化，跳过重复加载')
    }
  }
  // 🔥 修复：刷新页面时（wasLoggedIn 可能是 undefined），不执行任何操作，保留 sessionStorage 中的状态
}, { immediate: false })

// 🔥 修复：监听 createNewChatRequest 事件，当侧边栏点击新建对话时处理
const handleCreateNewChatRequest = async () => {
  try {
    logger.info('收到createNewChatRequest事件，处理新建对话请求')
    await handleCreateNewChatFromSidebar()
  } catch (error) {
    logger.error('处理新建对话请求失败', error)
  }
}

// 🔥 修复：监听 chatSelected 事件，当侧边栏选择对话时立即加载
const handleChatSelected = async (e) => {
  const chatId = e.detail?.chatId
  if (!chatId || !authStore.isLoggedIn) {
    return
  }
  
  // 🔥 修复：如果正在处理，跳过（防止重复调用）
  if (isHandlingChatSelected.value) {
    logger.debug('正在处理chatSelected事件，跳过重复调用', chatId)
    return
  }
  
  // 🔥 修复：设置标记，防止定时器检查重复加载
  isHandlingChatSelected.value = true
  
  try {
    logger.info('收到chatSelected事件，立即加载对话', chatId)
    const chat = chats.value.find(c => String(c.id) === String(chatId))
    if (chat) {
      await selectChat(chat, true)
    } else {
      // 如果对话列表中找不到，尝试直接加载消息
      logger.info('对话列表中找不到，直接加载消息列表', chatId)
      await loadMessages(chatId, true)
      // 创建临时对话对象
      const tempChat = {
        id: chatId,
        title: '',
        lastMessage: '',
        updatedAt: new Date(),
        unreadCount: 0,
        isNew: false,
        pinned: false,
        messages: []
      }
      chats.value.unshift(tempChat)
      currentChat.value = tempChat
    }
  } finally {
    // 🔥 修复：清除标记，允许后续检查
    setTimeout(() => {
      isHandlingChatSelected.value = false
    }, 1000) // 延迟1秒清除，确保定时器检查不会重复加载
  }
}

// 🔥 修复：监听 sessionStorage 变化，当侧边栏选择对话时自动加载（作为备用方案）
let storageCheckInterval = null
// 🔥 修复：窗口大小变化监听器
let windowResizeHandler = null
const checkSessionStorageConversationId = () => {
  if (!authStore.isLoggedIn) {
    return
  }
  
  // 🔥 修复：如果正在处理 chatSelected 事件，跳过检查（防止重复加载）
  if (isHandlingChatSelected.value) {
    return
  }
  
  // 只从 sessionStorage 获取对话ID
  const storedId = sessionStorage.getItem('currentConversationId')
  
  // 如果URL中有conversationId参数，清除它（保持URL干净）
  if (route.query && route.query.conversationId && route.path === '/ai/chat') {
    router.replace({ path: '/ai/chat', query: {} }).catch(() => {
      // 忽略路由冗余导航错误
    })
  }
  
  if (storedId && storedId !== 'null' && storedId !== 'undefined') {
    const chat = chats.value.find(c => String(c.id) === String(storedId))
    // 🔥 修复：只有在对话ID变化且不是当前对话时才加载（避免重复加载）
    if (chat && (!currentChat.value || String(currentChat.value.id) !== String(storedId))) {
      logger.info('sessionStorage变化，加载对话', storedId)
      selectChat(chat, true)
    }
  } else if (!storedId && currentChat.value && !currentChat.value.isNew) {
    // 如果sessionStorage没有ID但当前有对话（且不是新对话），清除当前对话（切换到新建对话）
    logger.info('sessionStorage没有ID，清除当前对话')
    currentChat.value = null
    messages.value = []
  }
}

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  // 🔥 修复：移除事件监听器
  window.removeEventListener('chatSelected', handleChatSelected)
  window.removeEventListener('createNewChatRequest', handleCreateNewChatRequest)
  // 停止正在进行的请求
  if (currentAbortController.value) {
    currentAbortController.value.abort()
  }
  // 🔥 修复：清除 sessionStorage 检查定时器
  if (storageCheckInterval) {
    clearInterval(storageCheckInterval)
    storageCheckInterval = null
  }
  // 🔥 修复：移除窗口大小变化监听器
  if (windowResizeHandler) {
    window.removeEventListener('resize', windowResizeHandler)
    windowResizeHandler = null
  }
})
</script>

<style lang="scss" scoped>
// AI对话页面需要全屏显示
.ai-chat-container {
  display: flex !important;
  height: 100vh !important;
  min-height: 100vh !important;
  width: 100% !important;
  max-width: 100% !important;
  margin: 0 !important;
  padding: 0 !important;
  background: var(--bg) !important;
  overflow: hidden !important;
  position: relative !important;
  flex: 1 1 auto !important;
  box-sizing: border-box !important;
}

// 🔥 修复：使用更强的选择器优先级，确保在生产环境也能生效
// 覆盖MainLayout的样式限制（使用深度选择器）
// MainLayout 的结构：.main-layout > .layout-content > .main-content > .content-wrapper
.ai-chat-container :deep(.main-content),
:deep(.main-content) {
  padding: 0 !important;
  max-width: 100% !important;
  height: 100% !important;
  overflow: hidden !important;
  
  .content-wrapper {
    max-width: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
    height: 100% !important;
    min-height: 100% !important;
    overflow: hidden !important;
  }
}

// 确保 layout-content 也占满高度
.ai-chat-container :deep(.layout-content),
:deep(.layout-content) {
  height: 100% !important;
  overflow: hidden !important;
}

/* 对话内容区域（全宽） */
.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--surface);
  position: relative;
  min-height: 0;
  overflow: hidden;
  width: 100%;
  height: 100%;
}

// 登录提示横幅
.login-prompt-banner {
  padding: 12px 24px;
  background: var(--warning-light);
  border-bottom: 1px solid var(--border);
  z-index: 10;
  flex-shrink: 0;
}

// 消息区域包装器（可滚动）
.chat-messages-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--surface);
}

.chat-title-header {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-primary);
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-3);
  padding: 40px;
  gap: 48px; // 增加间距，让输入框与上方内容有足够距离
}

.empty-greeting {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.greeting-text {
  font-size: 24px;
  font-weight: 500;
  color: var(--text);
}

.greeting-logo {
  height: 40px;
  width: auto;
  flex-shrink: 0;
}

// 空状态时的输入框（居中显示）
.empty-state-input {
  width: 100%;
  max-width: 800px; // 限制最大宽度，保持美观
  margin-top: 0;
  flex-shrink: 0;
}

// 🔥 修复：消息容器样式 - 统一背景颜色，居中显示，两边留白一致
.kb-messages-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: visible;
  padding: 24px; // 🔥 修复：恢复原始内边距
  background: var(--surface); // 🔥 修复：统一背景颜色为白色，与输入框保持一致
  display: flex;
  flex-direction: column;
  align-items: center; // 🔥 修复：消息居中显示
  justify-content: center;
  position: relative;
  z-index: 1;
  // 🔥 修复：确保容器有最小高度，避免被压缩
  min-height: 0;
  width: 100%;
  box-sizing: border-box;

  &.has-messages {
    align-items: stretch;
    justify-content: flex-start;
  }
}

.kb-messages-container::-webkit-scrollbar {
  width: 4px;
}

.kb-messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.kb-messages-container::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.kb-messages-container:hover::-webkit-scrollbar-thumb {
  background: var(--border);
}

.kb-messages-container::-webkit-scrollbar-thumb:hover {
  background: var(--border-hover);
}

.empty-chat-message {
  font-size: 16px;
  color: var(--text-3);
  text-align: center;
  line-height: 1.6;
}

.kb-messages {
  width: 100%;
  max-width: 1000px; // 🔥 修复：统一最大宽度，居中显示，两边留白一致
  margin-left: auto;
  margin-right: auto;
  overflow: visible;
  padding: 0; // 🔥 修复：确保没有额外的padding，让内容完美居中
}

// 🔥 修复：消息样式 - 参考原版 AIChat_Orgin.vue，保持原有美观布局
.kb-message {
  margin-bottom: 24px;
  display: flex;
  align-items: flex-start;
  gap: 12px;

  &.user {
    flex-direction: row-reverse;
    gap: 16px;
  }

  &.ai {
    justify-content: flex-start;
  }
}

.kb-message-bubble {
  max-width: 75%; // 🔥 修复：AI消息气泡宽度适当增加
  position: relative;
  
  // 🔥 修复：用户消息气泡宽度更小，文本左对齐
  .kb-message.user & {
    max-width: 60%; // 🔥 修复：用户消息气泡宽度缩小到60%
  }
}

// 🔥 修复：AI消息气泡向右侧移动一点，大致在对话框中间位置
.kb-message.ai .kb-message-bubble {
  margin-left: 15%;
}

.kb-message-content-wrapper {
  padding: 14px 18px;
  border-radius: 16px;
  line-height: 1.6;
  word-wrap: break-word;
  word-break: break-word;
  white-space: pre-wrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text);
  position: relative;
  
  // 🔥 修复：编辑状态下为按钮留出空间
  &.is-editing {
    padding-bottom: 45px;
  }
}

.kb-message.user .kb-message-content-wrapper {
  background: var(--hover);
  color: var(--text);
  border-color: var(--border);
  border-radius: 16px 16px 4px 16px;
  text-align: left; // 🔥 修复：用户消息文本左对齐
  // 🔥 修复：参考图2设计，减少内边距，让气泡更紧凑，文字垂直居中
  padding: 8px 14px; // 🔥 修复：减少上下内边距（从14px改为8px），让气泡高度更紧凑
  line-height: 1.5; // 🔥 修复：调整行高，让文字更紧凑，避免文字靠上
}

.kb-message.ai .kb-message-content-wrapper {
  border-radius: 16px 16px 16px 4px;
}

.kb-message-content {
  font-size: 14px;
  white-space: pre-wrap;
  word-wrap: break-word;
  word-break: break-word;
  overflow: visible;
  line-height: 1.7;
  
  // 🔥 修复：用户消息内容样式，确保文字不靠上
  .kb-message.user & {
    line-height: 1.5; // 🔥 修复：用户消息行高更紧凑
    margin: 0; // 🔥 修复：移除默认margin，避免文字靠上
    padding: 0; // 🔥 修复：移除默认padding
  }

  :deep(code) {
    background: rgba(0, 0, 0, 0.1);
    padding: 2px 4px;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
  }

  :deep(strong) {
    font-weight: 600;
  }

  // Markdown 表格样式
  :deep(.markdown-table-wrapper) {
    margin: 16px 0;
    overflow-x: auto;
    border-radius: 8px;
    background: #ffffff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
  }

  :deep(.markdown-table) {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;
    min-width: 100%;
  }

  :deep(.table-header) {
    padding: 12px 16px;
    text-align: left;
    background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
    color: #1e40af;
    font-weight: 600;
    font-size: 13px;
    border-right: 1px solid #e5e7eb;
    border-bottom: 2px solid #3b82f6;
    white-space: nowrap;
  }

  :deep(.table-cell) {
    padding: 12px 16px;
    text-align: left;
    background: #ffffff;
    color: #374151;
    font-size: 14px;
    line-height: 1.6;
    border-right: 1px solid #e5e7eb;
    border-bottom: 1px solid #e5e7eb;
    transition: background-color 0.2s ease;
  }

  :deep(.even-row .table-cell) {
    background: #fafbfc;
  }

  :deep(.odd-row .table-cell:hover) {
    background-color: #f8fafc;
  }

  // Markdown 标题样式
  :deep(.md-heading) {
    font-weight: 700;
    margin: 18px 0 12px;
    border-radius: 6px;
    padding: 12px 16px;
  }

  :deep(.md-heading.h1) {
    font-size: 24px;
    color: #1e3a8a;
    background: linear-gradient(135deg, #eff6ff 0%, #e0e7ff 100%);
    border-left: 6px solid #2563eb;
  }

  :deep(.md-heading.h2) {
    font-size: 20px;
    color: #1e40af;
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    border-bottom: 3px solid #3b82f6;
    padding-bottom: 8px;
  }

  :deep(.md-heading.h3) {
    font-size: 18px;
    color: #374151;
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-left: 4px solid #06b6d4;
  }

  // Markdown 文本格式
  :deep(.md-bold) {
    font-weight: 700;
    color: #1f2937;
    background: rgba(30, 58, 138, 0.08);
    padding: 2px 4px;
    border-radius: 4px;
  }

  :deep(.md-italic) {
    font-style: italic;
    color: #6b7280;
    background: rgba(156, 163, 175, 0.05);
    padding: 1px 3px;
    border-radius: 3px;
  }

  :deep(.md-strikethrough) {
    text-decoration: line-through;
    color: #9ca3af;
    background: rgba(156, 163, 175, 0.08);
    padding: 1px 3px;
    border-radius: 3px;
  }

  // Markdown 代码块
  :deep(.md-code-block) {
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
    color: #38bdf8;
    padding: 16px;
    border-radius: 8px;
    font-family: 'JetBrains Mono', Consolas, 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
    margin: 12px 0;
    border-left: 4px solid #f97316;
    overflow-x: auto;
    display: block;
  }

  :deep(.md-code-block code) {
    background: transparent;
    padding: 0;
    border-radius: 0;
    color: inherit;
  }

  :deep(.md-inline-code) {
    background: #f3f4f6;
    padding: 3px 6px;
    border-radius: 4px;
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 12px;
    color: #d6336c;
    border: 1px solid #e5e7eb;
    margin: 0 2px;
  }

  // Markdown 列表
  :deep(.md-list) {
    margin: 12px 0;
    padding-left: 24px;
    list-style-type: disc;
  }

  :deep(.md-list-item) {
    margin: 6px 0;
    line-height: 1.6;
  }

  // Markdown 分隔线
  :deep(.md-divider) {
    border: none;
    height: 2px;
    background: linear-gradient(90deg, transparent 0%, #93c5fd 50%, transparent 100%);
    margin: 24px 0;
    border-radius: 1px;
  }
}

// 🔥 修复：编辑状态容器 - 简化样式，正常编辑显示
.kb-message-content-edit {
  position: relative;
  width: 100%;
  display: flex;
  justify-content: flex-start;
  flex-direction: column;
  gap: 8px;
}

// 🔥 修复：编辑框 - 简化样式，像普通的文本输入框
.kb-edit-box {
  width: 100%;
}

// 🔥 修复：编辑内容区域 - 使用textarea样式，正常编辑显示
.kb-edit-content {
  width: 100%;
  min-height: 80px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text);
  outline: none;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface);
  resize: vertical;
  overflow-y: auto;
  word-wrap: break-word;
  white-space: pre-wrap;
  font-family: inherit;
  
  &:focus {
    outline: none;
    border-color: var(--color-primary);
    box-shadow: 0 0 0 2px rgba(var(--color-primary-rgb), 0.1);
  }
}

// 🔥 修复：编辑操作按钮 - 显示在编辑框下方，间距适当调整
.kb-edit-actions-inline {
  display: flex;
  gap: 12px; // 🔥 修复：增加按钮间距，不那么紧凑
  justify-content: flex-end;
  margin-top: 12px; // 🔥 修复：增加与编辑框的间距
}

.kb-message.user .kb-message-content {
  color: var(--text) !important; // 🔥 修复：输入消息字体颜色更深一点
}

.kb-message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 11px;
  color: var(--text-3);
  justify-content: space-between;
}

.kb-message-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  
  // 🔥 修复：用户消息的按钮默认隐藏，鼠标悬浮时才显示
  .kb-message.user & {
    opacity: 0; // 🔥 修复：默认隐藏
    transition: opacity 0.2s ease; // 🔥 修复：添加过渡效果
  }
  
  // 🔥 修复：鼠标悬浮在用户消息气泡上时显示按钮
  .kb-message.user:hover & {
    opacity: 1; // 🔥 修复：显示按钮
  }
  
  // 🔥 修复：AI消息的按钮，非最后一条默认隐藏，鼠标悬浮时才显示
  .kb-message.ai:not(.is-last-ai-message) & {
    opacity: 0; // 🔥 修复：非最后一条AI消息的按钮默认隐藏
    transition: opacity 0.2s ease; // 🔥 修复：添加过渡效果
  }
  
  // 🔥 修复：鼠标悬浮在非最后一条AI消息气泡上时显示按钮
  .kb-message.ai:not(.is-last-ai-message):hover & {
    opacity: 1; // 🔥 修复：显示按钮
  }
  
  // 🔥 修复：最后一条AI消息的按钮始终显示
  .kb-message.ai.is-last-ai-message & {
    opacity: 1; // 🔥 修复：最后一条AI消息的按钮始终显示
  }
}

.kb-copy-btn,
.kb-retry-btn,
.kb-edit-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: var(--hover);
  border: 1px solid var(--border);
  border-radius: 6px;
  color: var(--text-3);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: var(--border);
    color: var(--text);
    border-color: var(--border-hover);
  }

  &:active {
    background: var(--border-hover);
  }
}

// 🔥 修复：编辑状态样式 - 参考千问的设计，自适应高度，无最大高度限制
.kb-edit-wrapper {
  padding: 0;
  margin: 0;
  width: 100%;
}

// 🔥 修复：编辑状态样式 - 在原消息内容背景的右下角显示按钮
.kb-message-content-wrapper.is-editing {
  position: relative;
  padding-bottom: 50px; // 为按钮留出空间
}

.kb-message-content-edit {
  position: relative;
  width: 100%;
  min-height: 40px;
}

.kb-edit-content {
  outline: none;
  min-height: 20px;
  
  &:focus {
    outline: none;
  }
  
  &:empty::before {
    content: attr(data-placeholder);
    color: var(--text-3);
  }
}

.kb-edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

// 🔥 修复：编辑按钮样式 - 稍微小一点，圆角多一些，更美观
.kb-edit-btn-cancel,
.kb-edit-btn-confirm {
  padding: 6px 16px; // 🔥 修复：按钮稍微小一点
  border: none;
  border-radius: 16px; // 🔥 修复：圆角多一些
  font-size: 13px; // 🔥 修复：字体稍微小一点
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 60px; // 🔥 修复：最小宽度稍微小一点
  text-align: center;
}

.kb-edit-btn-cancel {
  background: var(--hover);
  color: var(--text-3);
  border: 1px solid var(--border);

  &:hover {
    background: #e5e7eb;
    color: var(--text);
    border-color: #d1d5db;
  }
  
  &:active {
    background: #d1d5db;
  }
}

.kb-edit-btn-confirm {
  background: var(--color-primary);
  color: var(--surface);
  box-shadow: 0 2px 4px rgba(var(--color-primary-rgb), 0.2);

  &:hover:not(:disabled) {
    background: var(--color-primary-dark);
    box-shadow: 0 2px 6px rgba(var(--color-primary-rgb), 0.3);
    transform: translateY(-1px);
  }
  
  &:active:not(:disabled) {
    transform: translateY(0);
    box-shadow: 0 1px 3px rgba(var(--color-primary-rgb), 0.2);
  }

  &:disabled {
    background: var(--border-hover);
    color: var(--text-3);
    cursor: not-allowed;
    box-shadow: none;
  }
}

// 文档片段展示样式（复用知识库页面样式）
.kb-message-documents {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}

.kb-documents-title {
  font-size: 12px;
  color: var(--text-3);
  margin-bottom: 8px;
  font-weight: 500;
}

.kb-documents-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.kb-document-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: var(--hover-light);
  border-radius: 6px;
  font-size: 12px;
  transition: background 0.2s ease;
}

.kb-document-item:hover {
  background: var(--hover);
}

.kb-document-icon {
  color: var(--color-primary-lighter);
  font-size: 14px;
  flex-shrink: 0;
}

.kb-document-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
}

.kb-document-name {
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kb-document-dataset {
  color: var(--text-3);
  font-size: 11px;
  flex-shrink: 0;
}

.kb-document-score {
  color: var(--text-3);
  font-size: 11px;
  flex-shrink: 0;
}

// 流式生成指示器（复用知识库页面样式）
.kb-streaming-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--border);
}

.kb-streaming-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary-lighter);
  animation: kb-streaming-pulse 1.5s ease-in-out infinite;
}

@keyframes kb-streaming-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

.kb-streaming-text {
  font-size: 12px;
  color: var(--text-3);
}

// 🔥 修复：输入区域样式 - 保持与空状态一致的宽度，并确保footer正确显示
.kb-input-area {
  padding: 20px;
  background: var(--surface);
  position: relative; // 🔥 修复：添加 position: relative，让滚动到底部按钮可以绝对定位
  z-index: 10;
  display: flex;
  flex-direction: column; // 🔥 修复：使用flex-direction: column，确保footer在底部
  align-items: center; // 🔥 修复：输入框居中显示
  
  .kb-input-container {
    max-width: 1000px; // 🔥 修复：限制输入框最大宽度，与消息内容保持一致，确保对齐
    width: 100%;
  }
}

.kb-input-container {
  position: relative; // 🔥 修复：添加 position: relative，让知识库下拉框和滚动按钮可以正确绝对定位
  display: flex;
  flex-direction: column;
  background: var(--hover-light);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 0;
  transition: all 0.2s ease;
  overflow: visible;
}

.kb-message-input {
  width: 100%;
  min-height: 40px; // 适中的高度
  max-height: 200px;
  padding: 8px 14px; // ，适中的内边距，确保文本垂直居中且美观
  border: none;
  font-size: 14px;
  line-height: 24px; // 🔥 修复：设置行高为24px，配合40px高度和8px上下padding，实现完美的垂直居中
  resize: none;
  outline: none;
  background: transparent;
  color: var(--text);
  overflow-y: hidden;
  font-family: inherit;
  word-wrap: break-word;
  white-space: pre-wrap;
  box-sizing: border-box; 

  &::placeholder {
    color: var(--text-3);
    line-height: 24px; // 🔥 修复：占位符也使用相同的行高
  }
}

// 附件预览区域
.attachment-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
  padding: 8px;
  background: var(--hover-light);
  border-radius: 8px;
  border: 1px solid var(--border);
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 12px;
  color: var(--text);
  max-width: 200px;

  &.knowledge-file {
    border-color: var(--color-primary-lighter);
    background: var(--hover);
  }
}

.attachment-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.attachment-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.attachment-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}

.attachment-source {
  color: var(--color-primary-lighter);
  font-size: 10px;
}

.attachment-size {
  color: var(--text-3);
  font-size: 11px;
  flex-shrink: 0;
}

.attachment-remove {
  width: 16px;
  height: 16px;
  color: var(--text-3);
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;

  &:hover {
    background: var(--hover);
    color: var(--color-error);
  }
}

// 知识库选择下拉框
.knowledge-base-dropdown {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  z-index: 1002;
  margin-bottom: 8px;
  max-height: 300px;
  overflow: hidden; // 🔥 修复：移除外层滚动，只保留内层滚动
  display: flex;
  flex-direction: column;
}

.kb-dropdown-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  background: var(--surface);
  border-radius: 12px 12px 0 0;
}

.kb-dropdown-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
}

.kb-dropdown-section {
  padding: 8px 0;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0; // 🔥 修复：允许 flex 子元素缩小
}

.kb-list {
  padding: 0 8px;
}

.kb-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  margin: 2px 0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  position: relative;

  &:hover {
    background: var(--hover);
    border-color: var(--border);
  }

  &.selected {
    background: var(--hover);
    border-color: var(--color-primary);
    box-shadow: 0 2px 4px rgba(var(--color-primary-rgb), 0.1);
  }
}

.kb-icon {
  width: 24px;
  height: 24px;
  min-width: 24px;
  background: var(--color-primary);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}

.kb-name {
  font-size: 13px;
  color: var(--text);
  font-weight: 500;
  flex: 1;
  min-width: 0;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kb-selected-mark {
  color: var(--color-primary-lighter);
  font-weight: bold;
  font-size: 14px;
}

// 底部控制栏
.kb-input-bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px 12px 16px;
  border-top: 1px solid var(--border);
  background: transparent;
}

// 模型选择器
.custom-model-selector {
  position: relative;
  min-width: 120px;
  flex-shrink: 0;
  z-index: 1000;
}

.model-display {
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 12px;
  color: var(--text-3);
  background: var(--surface);
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-weight: bold;

  &:hover {
    border-color: var(--border-hover);
    background: var(--hover-light);
  }
}

.model-dropdown {
  position: fixed; // 🔥 修复：使用fixed定位，根据位置动态计算
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 8px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  overflow: hidden;
  max-height: 300px;
  overflow-y: auto;
  width: 320px;
  // 🔥 修复：位置由JavaScript动态计算，根据视口空间决定向上或向下显示
}

.model-option {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  position: relative;
  border-bottom: 1px solid var(--border);

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--hover-light);
  }

  &.active {
    background: var(--hover);
  }
}

.model-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text);
  margin-bottom: 2px;
}

.model-desc {
  font-size: 11px;
  color: var(--text-3);
  line-height: 1.3;
}

.check-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-primary-lighter);
  font-weight: bold;
  font-size: 14px;
}

// 右侧控制按钮组
.kb-right-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 联网搜索按钮
.kb-enable-search-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #ffffff;
  color: var(--text-3);
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover {
    background: #ffffff;
    color: var(--text);
  }

  &:active {
    background: #ffffff;
  }

  &.active {
    background: #ffffff;
    color: var(--surface);

    &:hover {
      background: #ffffff;
      opacity: 0.9;
    }
  }

  .el-icon {
    font-size: 16px;
  }

  .search-icon {
    width: 18px;
    height: 18px;
    display: block;
  }
}

// 附件按钮
.kb-attachment-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--hover-light);
  color: var(--text-3);
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover {
    background: var(--hover);
    color: var(--text);
  }

  &:active {
    background: var(--border);
  }

  &.disabled {
    background: var(--hover-light);
    color: var(--border-hover);
    cursor: not-allowed;
  }

  .attachment-icon-svg {
    width: 18px;
    height: 18px;
    stroke-width: 1.5;
  }
}

// 发送按钮
.kb-send-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--border);
  color: var(--surface);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &.active {
    background: var(--color-primary);
    color: var(--surface);

    &:hover {
      background: var(--color-primary-dark);
    }
  }
}

// 停止按钮
.kb-stop-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--color-primary);
  color: var(--surface);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover {
    background: var(--color-primary-dark);
    transform: scale(1.05);
  }
}

// 🔥 修复：输入框底部提示文字样式 - 确保在对话框底部正确显示
.input-footer {
  margin-top: 12px;
  text-align: center;
  font-size: 12px;
  color: var(--text-3);
  width: 100%;
  max-width: 1000px; // 🔥 修复：与输入框宽度保持一致
  padding: 0 20px; // 🔥 修复：添加内边距，与输入框对齐
}

// 🔥 修复：滚动到底部按钮样式 - 在输入框区域右下角（还原到原始位置）
.scroll-to-bottom-btn-input {
  position: absolute; // 🔥 修复：使用absolute定位，相对于输入框区域
  bottom: 180px; // 🔥 修复：距离底部180px，往上移动一点
  right: calc(50% - 500px + 20px); // 🔥 修复：根据输入框容器最大宽度1000px，居中后右边距20px
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1001; // 🔥 修复：提高z-index，确保按钮在最上层
  transition: opacity 0.3s ease, transform 0.3s ease;
  color: var(--text-3);

  &:hover {
    background: var(--color-primary);
    border-color: var(--color-primary);
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
    color: var(--surface);
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  }

  .el-icon {
    font-size: 18px;
  }
  
  // 🔥 修复：响应式调整，小屏幕时使用固定右边距
  @media (max-width: 1200px) {
    right: 20px;
  }
}


// 知识库选择对话框样式
.knowledge-dialog-content {
  max-height: 500px;
  overflow-y: auto;
}

.knowledge-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.knowledge-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: var(--color-primary-lighter);
    background: var(--hover-light);
  }

  &.selected {
    border-color: var(--color-primary-lighter);
    background: var(--hover);
  }
}

.knowledge-icon {
  font-size: 24px;
  flex-shrink: 0;
}

// 🔥 新增：知识库封面图标样式
.knowledge-item-icon {
  width: 40px;
  height: 40px;
  min-width: 40px;
  min-height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
  
  .kb-cover-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .knowledge-item-icon-default {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .el-icon {
      font-size: 20px;
      color: var(--color-primary);
    }
  }
}

// 🔥 新增：@符号下拉框中的封面图标样式
.kb-item-icon {
  width: 32px;
  height: 32px;
  min-width: 32px;
  min-height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
  
  .kb-cover-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .kb-item-icon-default {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .el-icon {
      font-size: 16px;
      color: var(--color-primary);
    }
  }
}

// 🔥 新增：搜索框样式（复用KnowledgeList.vue的样式）
.search-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
  
  .search-left-icon {
    position: absolute;
    left: 12px;
    z-index: 1;
    color: var(--text-3);
    display: flex;
    align-items: center;
  }
  
  .form-input {
    width: 100%;
    padding: 10px 40px 10px 36px;
    border: 1px solid var(--border);
    border-radius: 8px;
    font-size: 14px;
    background: var(--surface);
    color: var(--text);
    transition: all 0.2s ease;
    
    &:focus {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1);
      outline: none;
    }
  }
}

// 🔥 修复：确保搜索框在 kb-dropdown-search 中也有正确的样式
.kb-dropdown-search .form-input {
  border: 1px solid var(--border);
  transition: all 0.2s ease;
  
  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1);
    outline: none;
  }
}

// 🔥 新增：下拉框搜索区域样式
.kb-dropdown-search {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  position: relative;
  flex-shrink: 0; // 🔥 修复：防止搜索框被压缩
  
  .search-left-icon {
    position: absolute;
    left: 28px;
    top: 50%;
    transform: translateY(-50%);
    z-index: 1;
    color: var(--text-3);
    display: flex;
    align-items: center;
  }
  
  // 🔥 修复：搜索框边框颜色使用主题色
  .form-input {
    border: 1px solid var(--border);
    transition: all 0.2s ease;
    
    &:focus {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1);
      outline: none;
    }
  }
}

// 🔥 新增：加载更多和没有更多提示样式
.kb-loading-more,
.kb-no-more {
  text-align: center;
  padding: 12px;
  color: var(--text-3);
  font-size: 12px;
}

.knowledge-info {
  flex: 1;
  min-width: 0;
}

.knowledge-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  margin-bottom: 4px;
}

.knowledge-desc {
  font-size: 12px;
  color: var(--text-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// 文档选择对话框样式
.document-dialog-content {
  max-height: 500px;
  overflow-y: auto;
}

.selected-knowledge-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--hover);
  border: 1px solid var(--border);
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: var(--color-primary-dark);
  font-weight: 500;
}

.document-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.document-item {
  padding: 12px 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    border-color: var(--color-primary-lighter);
    background: var(--hover-light);
  }
}

.document-checkbox {
  width: 100%;
  margin: 0;

  :deep(.el-checkbox__label) {
    width: 100%;
    padding-left: 8px;
  }
}

.document-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.document-name {
  font-size: 14px;
  color: var(--text);
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.document-size {
  font-size: 12px;
  color: var(--text-3);
  flex-shrink: 0;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-count {
  font-size: 14px;
  color: var(--text-3);
  
  .limit-warning {
    color: var(--color-error);
    font-weight: 500;
    margin-left: 4px;
  }
}

.document-limit-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--warning-light);
  border: 1px solid var(--border);
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: var(--text);
  
  .el-icon {
    color: var(--text-3);
    font-size: 16px;
  }
}

// 覆盖 Element Plus tooltip 样式，设置白色背景
// 注意：tooltip 会被挂载到 body，需要使用深度选择器
:deep(.el-tooltip__popper) {
  background: var(--surface) !important;
  border: 1px solid var(--border) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  color: var(--text) !important;
  
  .el-tooltip__arrow::before {
    background: var(--surface) !important;
    border: 1px solid var(--border) !important;
  }
}

.attachment-tooltip-content {
  padding: 4px 0;
  line-height: 1.8;
  
  .tooltip-item {
    font-size: 12px;
    color: var(--text);
    white-space: nowrap;
    
    &.tooltip-warning {
      color: var(--color-error);
      font-weight: 500;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .chat-list-sidebar {
    width: 0;
    overflow: hidden;
  }

  .kb-message-bubble {
    max-width: 85%;
  }
}
</style>