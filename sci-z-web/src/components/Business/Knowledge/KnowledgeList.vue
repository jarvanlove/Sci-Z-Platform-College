<!--
/**
 * @description 知识库列表页面
 * 展示所有知识库，支持搜索、筛选、分页等功能
 * 三栏布局：左侧知识库列表、中间内容区域、右侧AI助手
 */
-->
<template>
  <div class="knowledge-base-container">
    <!-- 左侧导航栏 -->
    <div class="kb-list-sidebar">
      <div class="kb-list-header">
        <div class="kb-header-title">
          {{ $t('knowledge.sharedKnowledgeBase') }}
        </div>
        <div class="kb-header-actions">
          <!-- 搜索按钮 -->
          <BaseTooltip :content="$t('knowledge.searchKb')" placement="top">
            <button
              class="action-btn btn-info"
              @click="toggleKbListSearch"
            >
              <el-icon><Search /></el-icon>
            </button>
          </BaseTooltip>
          <!-- 🔥 统一按钮主题：与卡片预览按钮保持一致（灰色主题） -->
          <BaseTooltip :content="$t('knowledge.newSharedKnowledgeBase')" placement="top">
            <button
              class="action-btn btn-info"
              @click="showCreateDialog = true"
            >
              <el-icon><Plus /></el-icon>
            </button>
          </BaseTooltip>
        </div>
      </div>

      <!-- 搜索框 -->
      <div
        v-if="showKbSearch"
        class="search-input-wrap"
        style="margin: 12px 16px;"
      >
        <span class="search-left-icon">
          <el-icon><Search /></el-icon>
        </span>
        <input
          class="form-input"
          v-model="kbListSearchQuery"
          :placeholder="$t('knowledge.searchPlaceholder')"
          style="width: 100%; padding: 10px 40px 10px 36px"
          @input="handleKbListSearchInput"
        />
        <div
          class="search-dismiss"
          :title="$t('knowledge.closeSearch')"
          @click="toggleKbListSearch"
        >
          <el-icon><Close /></el-icon>
        </div>
      </div>

      <!-- 知识库类型 Tab：全部 / 个人知识库 / 项目知识库 -->
      <div class="kb-type-tabs">
        <button
          type="button"
          class="kb-type-tab"
          :class="{ active: activeKbType === 'all' }"
          @click="switchKbType('all')"
        >
          {{ $t('knowledge.kbTypeAll') }}
        </button>
        <button
          type="button"
          class="kb-type-tab"
          :class="{ active: activeKbType === 'personal' }"
          @click="switchKbType('personal')"
        >
          {{ $t('knowledge.personalKnowledgeBase') }}
        </button>
        <button
          type="button"
          class="kb-type-tab"
          :class="{ active: activeKbType === 'project' }"
          @click="switchKbType('project')"
        >
          {{ $t('knowledge.projectKnowledgeBase') }}
        </button>
      </div>

      <div class="kb-list">
        <!-- 使用项目封装的 BaseScrollbar 组件，统一滚动条样式 -->
        <BaseScrollbar 
          ref="kbListScrollRef"
          class="kb-list-scroll" 
          size="small"
          @scroll="handleKbListScroll"
        >
          <div
            v-for="kb in knowledgeBases"
            :key="kb.id"
            class="kb-item"
            :class="{ active: selectedKnowledgeBase && selectedKnowledgeBase.id === kb.id }"
            @click="selectKnowledgeBase(kb)"
          >
            <div class="kb-item-icon" @click.stop="openEditFromList(kb)">
              <img
                v-if="getCoverUrl(kb)"
                :src="getCoverUrl(kb)"
                alt="cover"
              />
              <!-- 🔥 更换为更现代的图标：Collection（集合图标，更适合知识库） -->
              <div v-else class="kb-item-icon-default">
                <el-icon><Collection /></el-icon>
              </div>
            </div>
            <div class="kb-item-info">
              <BaseTooltip
                :content="kb.name"
                placement="top"
              >
                <div class="kb-item-name">{{ truncateKbName(kb.name) }}</div>
              </BaseTooltip>
              <!-- 仅「全部」Tab 时显示类型标识；仅当 personal 且 isShared=1 时显示「共享人：ownerName」 -->
              <div class="kb-item-tags">
                <span
                  v-if="activeKbType === 'all'"
                  class="kb-type-tag"
                  :class="isProjectKb(kb) ? 'tag-project' : 'tag-personal'"
                >
                  {{ isProjectKb(kb) ? $t('knowledge.projectKnowledgeBase') : $t('knowledge.personalKnowledgeBase') }}
                </span>
                <span
                  v-if="!isProjectKb(kb) && kb.isShared === 1"
                  class="kb-type-tag tag-shared"
                >
                  {{ $t('knowledge.sharedByOwnerName', { name: kb.ownerName || '-' }) }}
                </span>
              </div>
            </div>
            <!-- 更多菜单：owner 或 admin 显示（资料修改；个人：权限管理、删除） -->
            <div v-if="canOperateKb(kb)" class="kb-item-actions" @click.stop>
              <el-dropdown
                trigger="click"
                placement="bottom-end"
                popper-class="kb-more-dropdown-popper"
                @command="(cmd) => handleKbMenuCommand(cmd, kb)"
              >
                <button class="kb-action-icon" aria-haspopup="true">
                  <el-icon><MoreFilled /></el-icon>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-if="canOperateKb(kb)"
                      :command="'edit'"
                    >
                      <el-icon><Edit /></el-icon>
                      <span>{{ $t('knowledge.editData') }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="!isProjectKb(kb) && canOperateKb(kb)"
                      :command="'permission'"
                    >
                      <el-icon><Lock /></el-icon>
                      <span>{{ $t('knowledge.permissionManagement') }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="!isProjectKb(kb) && canOperateKb(kb)"
                      :command="'delete'"
                      divided
                    >
                      <el-icon><Delete /></el-icon>
                      <span>{{ $t('knowledge.deleteKnowledgeBase') }}</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div v-if="knowledgeBases.length === 0" class="kb-empty-tip">
            {{ $t('knowledge.noKnowledgeBase') }}
          </div>
          
          <!-- 🔥 显示更多按钮：当有更多数据时显示，列表为空时不显示 -->
          <div 
            v-if="kbListPagination.hasMore && !kbListPagination.loading && knowledgeBases.length > 0" 
            class="kb-load-more"
            @click="handleLoadMore"
          >
            <el-icon class="kb-load-more-icon"><ArrowDown /></el-icon>
            <span class="kb-load-more-text">{{ $t('knowledge.showMore') }}</span>
          </div>
        </BaseScrollbar>
      </div>
    </div>

    <!-- 中间内容区域 -->
    <transition name="main-content-fade">
      <div v-show="isContentExpanded && knowledgeBases.length > 0" class="main-content">
        <!-- 知识库信息头部区域（点击进入资料修改，与资料修改按钮同权：无权限不生效、仅打日志） -->
        <div class="content-header" @click="openSettingsDialogWithPermission">
          <div class="content-title">
            <div class="content-icon" @click.stop="openCoverUploadDialog">
              <img
                v-if="getCoverUrl(selectedKnowledgeBase)"
                :src="getCoverUrl(selectedKnowledgeBase)"
                alt="cover"
                class="content-icon-img"
              />
              <el-icon v-else><Collection /></el-icon>
            </div>
            <div>
              <div class="content-title-text">
                {{ selectedKnowledgeBase ? (selectedKnowledgeBase.shortName || selectedKnowledgeBase.name) : $t('knowledge.research') }}
              </div>
              <div class="content-meta">
                <div class="user-info-wrapper">
                  <span>{{ selectedKnowledgeBase?.ownerName || $t('knowledge.unknownOwner') }} | {{ selectedKnowledgeBase ? getCurrentFileCount() : 0 }}{{ $t('knowledge.contentCount') }}</span>
                </div>
              </div>
              <div class="content-description">
                {{ selectedKnowledgeBase ? selectedKnowledgeBase.description : $t('knowledge.researchProjectKb') }}
              </div>
            </div>
          </div>
        </div>
        <!-- 头像直接选封面：无弹窗，仅有权限时可用；无权限仅打日志 -->
        <input
          ref="coverUploadInputDirect"
          type="file"
          accept="image/*"
          style="position: absolute; width: 0; height: 0; opacity: 0; pointer-events: none;"
          @change="handleCoverUploadSelect"
        />

          <div class="content-body">
            <div v-if="selectedKnowledgeBase" class="content-section">
              <div class="section-header">
                <div class="section-title">
                  <template v-if="currentFolder">
                    <span class="kb-breadcrumb" :class="{ 'dragging-active': draggedFile || (draggedFiles && draggedFiles.length > 0) }">
                      <!-- 根目录（内容）支持拖拽 -->
                      <span 
                        class="kb-crumb-link"
                        :class="{ 
                          'drag-over': dragOverFolderId === 'root',
                          'can-drop': draggedFile && dragOverFolderId !== 'root' && canDropToFolder(null)
                        }"
                        @click="backToRoot"
                        @dragover.prevent="handleBreadcrumbDragOver($event, 'root', null)"
                        @dragenter.prevent="handleBreadcrumbDragEnter($event, 'root', null)"
                        @dragleave.prevent="handleBreadcrumbDragLeave($event, 'root')"
                        @drop.prevent="handleBreadcrumbDrop($event, 'root', null)"
                      >
                        <el-icon v-if="(draggedFile || (draggedFiles && draggedFiles.length > 0)) && canDropToFolder(null)" class="drag-hint-icon">
                          <ArrowUp />
                        </el-icon>
                        {{ $t('knowledge.content') }}
                      </span>
                      <span 
                        v-for="(folder, index) in folderPathStack" 
                        :key="`breadcrumb-${folder.id}-${index}`"
                        class="kb-breadcrumb-item"
                      >
                        <span>></span>
                        <span 
                          class="kb-crumb-link" 
                          :class="{ 
                            'kb-crumb-current': index === folderPathStack.length - 1,
                            'drag-over': dragOverFolderId === folder.id,
                            'can-drop': (draggedFile || (draggedFiles && draggedFiles.length > 0)) && dragOverFolderId !== folder.id && canDropToFolder(folder.id)
                          }"
                          @click="navigateToFolder(folder, index)"
                          @dragover.prevent="handleBreadcrumbDragOver($event, folder.id, folder)"
                          @dragenter.prevent="handleBreadcrumbDragEnter($event, folder.id, folder)"
                          @dragleave.prevent="handleBreadcrumbDragLeave($event, folder.id)"
                          @drop.prevent="handleBreadcrumbDrop($event, folder.id, folder)"
                        >
                          <el-icon v-if="(draggedFile || (draggedFiles && draggedFiles.length > 0)) && canDropToFolder(folder.id)" class="drag-hint-icon">
                            <ArrowUp />
                          </el-icon>
                          {{ folder.name }}
                        </span>
                      </span>
                    </span>
                  </template>
                  <template v-else>
                    {{ $t('knowledge.content') }}{{ getCurrentFileCount() > 0 ? `(${getCurrentFileCount()})` : '' }}
                  </template>
                </div>
                <div class="section-actions">
                  <BaseTooltip :content="$t('knowledge.query')" placement="top">
                    <button
                      class="action-btn btn-info"
                      @click="toggleSearch"
                    >
                      <el-icon><Search /></el-icon>
                    </button>
                  </BaseTooltip>
                  <BaseTooltip v-if="selectedKnowledgeBase?.canEdit === true" :content="$t('knowledge.createFolder')" placement="top">
                    <button
                      class="action-btn btn-warning"
                      @click="createFolder"
                    >
                      <el-icon><FolderAdd /></el-icon>
                    </button>
                  </BaseTooltip>
                  <BaseTooltip v-if="selectedKnowledgeBase?.canEdit === true" :content="$t('knowledge.uploadLocalFile')" placement="top">
                    <button
                      class="action-btn btn-success"
                      @click="openUploadDialog"
                    >
                      <el-icon><Upload /></el-icon>
                    </button>
                  </BaseTooltip>
                  <!-- 批量移动文件模式切换 - 仅在有编辑权限时显示 -->
                  <BaseTooltip v-if="selectedKnowledgeBase?.canEdit === true" :content="isSelectMode ? $t('knowledge.exitBatchMove') : $t('knowledge.batchMove')" placement="top">
                    <button
                      class="action-btn"
                      :class="{ 'btn-primary': isSelectMode, 'btn-info': !isSelectMode }"
                      @click="toggleSelectMode"
                    >
                      <el-icon><Box /></el-icon>
                    </button>
                  </BaseTooltip>
                </div>
              </div>

              <div
                v-if="showSearch"
                class="search-input-wrap"
                style="margin: 0 0 12px 0"
              >
                <span class="search-left-icon">
                  <el-icon><Search /></el-icon>
                </span>
                <input
                  class="form-input"
                  v-model="kbSearchQuery"
                  :placeholder="$t('knowledge.searchInKb')"
                  style="width: 100%; padding: 10px 40px 10px 36px"
                />
                <div
                  class="search-dismiss"
                  :title="$t('knowledge.closeSearch')"
                  @click="toggleSearch"
                >
                  <el-icon><Close /></el-icon>
                </div>
              </div>

              <!-- 🔥 卡片视图：参考报告列表页面的卡片样式 -->
              <div class="content-card-grid">
                <div
                  v-for="item in currentKbDisplayItems"
                  :key="item.id"
                  class="content-card-item"
                  :class="{ 
                    'file-card': item.type === 'file', 
                    'folder-card': item.type !== 'file',
                    'dragging': item.type === 'file' && (draggedFile?.id === item.id || (draggedFiles?.length > 0 && draggedFiles.some(f => f.id === item.id))),
                    'selected': isSelectMode && selectedFileIds.has(item.id)
                  }"
                  :draggable="item.type === 'file' && selectedKnowledgeBase?.canEdit === true"
                  @dragstart="item.type === 'file' && selectedKnowledgeBase?.canEdit ? handleDragStart($event, item) : null"
                  @dragend="item.type === 'file' && selectedKnowledgeBase?.canEdit ? handleDragEnd() : null"
                  @click="item.type !== 'file' ? enterFolder(item) : null"
                >
                  <!-- 文件类型卡片 -->
                  <template v-if="item.type === 'file'">
                    <div class="card-header" :class="{ 'select-mode': isSelectMode }">
                      <!-- 🔥 重新设计批量选择模式下的布局：复选框在左上角，logo和文件类型标签在右侧 -->
                      <div v-if="isSelectMode" class="select-mode-layout">
                        <!-- 复选框放在左上角 -->
                        <div
                          class="file-select-checkbox-wrapper"
                          @click.stop="toggleFileSelection(item.id, !selectedFileIds.has(item.id))"
                        >
                          <el-checkbox
                            :model-value="selectedFileIds.has(item.id)"
                            @change="(checked) => toggleFileSelection(item.id, checked)"
                            @click.stop
                            class="file-select-checkbox"
                          />
                        </div>
                        <!-- logo和文件类型标签在右侧 -->
                        <div class="file-icon-group">
                          <FileTypeIcon
                            :extension="item.ext"
                            :fileName="item.name"
                            size="large"
                          />
                          <div class="file-type-badge">{{ item.ext?.toUpperCase() || 'FILE' }}</div>
                        </div>
                      </div>
                      <!-- 非批量选择模式：保持原有布局 -->
                      <template v-else>
                        <FileTypeIcon
                          :extension="item.ext"
                          :fileName="item.name"
                          size="large"
                        />
                        <div class="file-type-badge">{{ item.ext?.toUpperCase() || 'FILE' }}</div>
                      </template>
                    </div>
                    
                    <div class="card-content">
                      <!-- 🔥 使用 BaseTooltip 显示完整附件名称，与知识库名称的悬浮提示主题一致 -->
                      <!-- 🔥 移除 title 属性，避免与 BaseTooltip 冲突，防止显示白色背景的原生 tooltip -->
                      <BaseTooltip
                        :content="item.name"
                        placement="top"
                      >
                        <h3 class="card-title">{{ item.name }}</h3>
                      </BaseTooltip>
                    </div>
                    
                    <div class="card-meta">
                      <div class="meta-item">
                        <p class="meta-label">{{ $t('knowledge.fileSize') }}</p>
                        <p class="meta-value">{{ formatFileSize(item.fileSize) }}</p>
                      </div>
                      <div class="meta-item">
                        <p class="meta-label">{{ $t('knowledge.uploadTime') }}</p>
                        <p class="meta-value">{{ formatUploadTime(item.time) }}</p>
                      </div>
                    </div>
                    
                    <div class="card-actions" @click.stop>
                      <BaseTooltip :content="$t('common.preview')" placement="top">
                        <button
                          class="action-btn btn-info"
                          @click.stop="handlePreviewFile(item)"
                        >
                          <el-icon><View /></el-icon>
                        </button>
                      </BaseTooltip>
                      <BaseTooltip v-if="canShowFileDelete(selectedKnowledgeBase)" :content="$t('knowledge.renameTitle')" placement="top">
                        <button
                          class="action-btn btn-warning"
                          @click.stop="renameItem(item)"
                        >
                          <el-icon><Edit /></el-icon>
                        </button>
                      </BaseTooltip>
                      <BaseTooltip v-if="canShowFileDelete(selectedKnowledgeBase)" :content="$t('common.delete')" placement="top">
                        <button
                          class="action-btn btn-danger"
                          @click.stop="deleteItem(item)"
                        >
                          <el-icon><Delete /></el-icon>
                        </button>
                      </BaseTooltip>
                    </div>
                  </template>

                  <!-- 文件夹类型卡片 -->
                  <template v-else>
                    <div 
                      class="card-header"
                      :class="{ 'drag-over': dragOverFolderId === item.id }"
                      @dragover.prevent="handleDragOver($event, item)"
                      @dragenter.prevent="handleDragEnter($event, item)"
                      @dragleave.prevent="handleDragLeave($event, item)"
                      @drop.prevent="handleDrop($event, item)"
                    >
                      <div class="card-icon-wrapper folder-icon-wrapper">
                        <el-icon class="card-icon folder-icon"><Folder /></el-icon>
                      </div>
                      <div class="folder-badge">{{ $t('knowledge.folder') || '文件夹' }}</div>
                    </div>
                    
                    <div class="card-content">
                      <h3 class="card-title">{{ item.name }}</h3>
                    </div>
                    
                    <div class="card-meta">
                      <div class="meta-item">
                        <p class="meta-label">
                          {{ (item.fileCount !== undefined ? item.fileCount : ((item.files || []).length || 0)) + ' ' + $t('knowledge.fileCount') }}
                        </p>
                        <p class="meta-value"></p>
                      </div>
                    </div>
                    
                    <div class="card-actions" @click.stop>
                      <BaseTooltip v-if="canShowFileDelete(selectedKnowledgeBase)" :content="$t('knowledge.renameTitle')" placement="top">
                        <button
                          class="action-btn btn-warning"
                          @click.stop="renameItem(item)"
                        >
                          <el-icon><Edit /></el-icon>
                        </button>
                      </BaseTooltip>
                      <BaseTooltip v-if="canShowFileDelete(selectedKnowledgeBase)" :content="$t('common.delete')" placement="top">
                        <button
                          class="action-btn btn-danger"
                          @click.stop="deleteItem(item)"
                        >
                          <el-icon><Delete /></el-icon>
                        </button>
                      </BaseTooltip>
                    </div>
                  </template>
                </div>
              </div>

              <div
                v-if="currentFolder && currentKbDisplayItems.length === 0"
                class="content-empty"
              >
                <div>{{ $t('knowledge.emptyFolder') }}</div>
              </div>
              <div
                v-else-if="!currentFolder && currentKbDisplayItems.length === 0"
                class="content-empty"
              >
                {{ $t('knowledge.noMoreContent') }}
              </div>

              <!-- 分页组件：使用统一封装的 BasePagination -->
              <div v-if="pagination.total > 0" class="pagination-wrapper">
                <BasePagination
                  :current="pagination.currentPage"
                  :size="pagination.pageSize"
                  :total="pagination.total"
                  :pages="pagination.pages"
                  :page-sizes="[10, 20, 50, 100]"
                  :show-info="true"
                  :show-size-selector="true"
                  :show-jumper="true"
                  @change="handlePageChange"
                  @size-change="handlePageSizeChange"
                />
              </div>
            </div>

            <div v-else class="empty-knowledge-base">
              <div class="empty-icon-large"></div>
              <div class="empty-title">{{ $t('knowledge.emptyKnowledgeBase') }}</div>
              <div class="empty-description">
                {{ $t('knowledge.uploadHint') }}
              </div>
              <button class="upload-files-btn" @click="showCreateDialog = true">
                <el-icon><Plus /></el-icon>
                {{ $t('knowledge.createKnowledgeBase') }}
              </button>
            </div>
          </div>
      </div>
    </transition>

    <!-- 创建知识库弹窗 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="$t('knowledge.createSharedKnowledgeBase')"
      width="700px"
      @close="resetCreateForm"
    >
      <el-form :model="newKbForm" label-width="100px">
        <el-form-item :label="$t('knowledge.nameLabel')" required>
          <el-input
            v-model="newKbForm.name"
            :placeholder="$t('knowledge.namePlaceholder')"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item :label="$t('knowledge.coverLabel')">
          <div class="cover-upload">
            <div class="cover-preview" @click="triggerCoverUpload">
              <img
                v-if="newKbForm.coverUrl"
                :src="newKbForm.coverUrl"
                :alt="$t('knowledge.coverAlt')"
              />
              <el-icon v-else><Picture /></el-icon>
              <div class="cover-edit-icon">
                <el-icon><Edit /></el-icon>
              </div>
            </div>
            <div class="cover-upload-text">
              {{ $t('knowledge.coverUploadHint') }}
            </div>
            <input
              ref="coverInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleCoverSelect"
            />
          </div>
        </el-form-item>

        <el-form-item :label="$t('knowledge.descriptionLabel')">
          <el-input
            v-model="newKbForm.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('knowledge.sharedKbDescriptionPlaceholder')"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">{{ $t('knowledge.cancel') }}</el-button>
        <el-button
          type="primary"
          @click="createKnowledgeBase"
          :disabled="!newKbForm.name.trim()"
        >
          {{ $t('knowledge.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 知识库设置弹窗 -->
    <el-dialog
      v-model="showSettingsDialog"
      :title="$t('knowledge.knowledgeBaseSettings')"
      width="700px"
      @close="resetSettingsForm"
    >
      <el-form :model="settingsForm" label-width="100px">
        <el-form-item :label="$t('knowledge.nameLabel')" required>
          <el-input
            v-model="settingsForm.name"
            :placeholder="$t('knowledge.namePlaceholder')"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item :label="$t('knowledge.coverLabel')">
          <div class="cover-upload">
            <div class="cover-preview" @click="triggerSettingsCoverUpload">
              <img
                v-if="settingsForm.coverUrl"
                :src="settingsForm.coverUrl"
                :alt="$t('knowledge.coverAlt')"
              />
              <el-icon v-else><Picture /></el-icon>
              <div class="cover-edit-icon">
                <el-icon><Edit /></el-icon>
              </div>
            </div>
            <div class="cover-upload-text">
              {{ $t('knowledge.coverUploadHint') }}
            </div>
            <input
              ref="settingsCoverInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleSettingsCoverSelect"
            />
          </div>
        </el-form-item>

        <el-form-item :label="$t('knowledge.descriptionLabel')">
          <el-input
            v-model="settingsForm.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('knowledge.sharedKbDescriptionPlaceholder')"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSettingsDialog = false">{{ $t('knowledge.cancel') }}</el-button>
        <el-button
          type="primary"
          @click="updateKnowledgeBaseSettings"
          :disabled="!settingsForm.name.trim()"
        >
          {{ $t('knowledge.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 重命名弹窗 -->
    <el-dialog
      v-model="showRenameDialog"
      :title="$t('knowledge.renameTitle')"
      width="500px"
    >
      <el-form>
        <el-form-item :label="$t('knowledge.nameLabel')" required>
          <el-input
            v-model="renameForm"
            :placeholder="$t('knowledge.renamePlaceholder')"
            @keydown.enter.prevent="confirmRename"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelRename">{{ $t('knowledge.cancel') }}</el-button>
        <el-button
          type="primary"
          @click="confirmRename"
          :disabled="!renameForm.trim()"
        >
          {{ $t('knowledge.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 🔥 新增：只用于上传封面的对话框 -->
    <el-dialog
      v-model="showCoverUploadDialog"
      :title="$t('knowledge.uploadCover')"
      width="500px"
      @close="coverUploadInput && (coverUploadInput.value = '')"
    >
      <div style="text-align: center; padding: 20px 0">
        <div style="margin-bottom: 20px">
          <el-button type="primary" @click="triggerCoverUploadInput">
            {{ $t('knowledge.selectCoverImage') }}
          </el-button>
        </div>
        <div style="font-size: 14px; color: #6b7280">
          {{ $t('knowledge.coverUploadTip') }}
        </div>
      </div>
      <input
        ref="coverUploadInput"
        type="file"
        accept="image/*"
        style="display: none"
        @change="handleCoverUploadSelect"
      />
      <template #footer>
        <el-button @click="showCoverUploadDialog = false">{{ $t('knowledge.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog
      v-model="showDeleteDialog"
      :title="$t('knowledge.deleteConfirmTitle')"
      width="500px"
    >
      <div style="text-align: center; padding: 20px 0">
        <div style="font-size: 16px; color: #374151; margin-bottom: 8px">
          {{ deletingKnowledgeBase ? $t('knowledge.deleteKbConfirm', { name: deletingKnowledgeBase.name }) : $t('knowledge.deleteItemConfirm') }}
        </div>
        <div style="font-size: 14px; color: #6b7280">
          {{ deletingKnowledgeBase ? $t('knowledge.deleteKbWarning') : $t('knowledge.deleteItemWarning') }}
        </div>
      </div>
      <template #footer>
        <el-button @click="cancelDelete">{{ $t('knowledge.cancel') }}</el-button>
        <el-button
          type="danger"
          @click="confirmDelete"
        >
          {{ $t('common.delete') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 权限设置弹窗：公开可见 / 转为私密（仅个人知识库） -->
    <el-dialog
      v-model="showPermissionDialog"
      :title="$t('knowledge.permissionSettings')"
      width="460px"
      class="permission-settings-dialog"
      @close="closePermissionDialog"
    >
      <el-radio-group v-model="permissionRadio" class="permission-radio-group">
        <el-radio :label="1" class="permission-option">
          <div class="permission-option-content">
            <span class="permission-option-title">{{ $t('knowledge.permissionPublicVisible') }}</span>
            <span class="permission-option-desc">{{ $t('knowledge.permissionPublicVisibleDesc') }}</span>
          </div>
        </el-radio>
        <el-radio :label="0" class="permission-option">
          <div class="permission-option-content">
            <span class="permission-option-title">{{ $t('knowledge.permissionPrivate') }}</span>
            <span class="permission-option-desc">{{ $t('knowledge.permissionPrivateDesc') }}</span>
          </div>
        </el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="closePermissionDialog">{{ $t('knowledge.cancel') }}</el-button>
        <el-button type="primary" :loading="permissionSaving" @click="confirmPermissionDialog">
          {{ $t('knowledge.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 创建文件夹弹窗 -->
    <el-dialog
      v-model="showCreateFolderDialog"
      :title="$t('knowledge.createFolder')"
      width="500px"
      @close="cancelCreateFolder"
    >
      <el-form :model="createFolderForm" label-width="100px">
        <el-form-item :label="$t('knowledge.folderName')" required>
          <el-input
            v-model="createFolderForm.name"
            :placeholder="$t('knowledge.folderNamePlaceholder')"
            maxlength="50"
            show-word-limit
            @keydown.enter.prevent="confirmCreateFolder"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelCreateFolder">{{ $t('knowledge.cancel') }}</el-button>
        <el-button
          type="primary"
          @click="confirmCreateFolder"
          :disabled="!createFolderForm.name.trim()"
        >
          {{ $t('knowledge.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 文件预览弹窗 - 已改为新窗口打开，保留组件以防需要 -->
    <!-- <FilePreview
      v-model="showPreviewDialog"
      :file-info="previewFileInfo"
      @close="closePreview"
    /> -->

    <!-- 文件上传对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      :title="$t('knowledge.uploadLocalFile')"
      width="800px"
      :close-on-click-modal="false"
      @close="closeUploadDialog"
    >
      <!-- 上传进度列表（上传中或上传完成时显示） -->
      <FileUploadProgressList
        v-if="showUploadProgress && uploadResults.length > 0"
        :upload-results="uploadResults"
        @close="handleUploadProgressClose"
      />
      
      <!-- 文件选择区域（未开始上传时显示） -->
      <template v-else>
        <!-- Dify 知识库不支持的文件类型提示 -->
        <el-alert
          :title="$t('knowledge.unsupportedFileTypesTitle')"
          :description="$t('knowledge.unsupportedFileTypesDescription')"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
        />
        <FileUpload
          v-model="pendingUploadFiles"
          :mode="'batch'"
          :max-batch-count="10"
          :multiple="true"
          :drag="true"
          :show-file-list="true"
          :show-tips="true"
          :max-size="200"
          :button-text="$t('knowledge.selectFiles')"
          :upload-tip="$t('knowledge.uploadTip')"
          :tips-title="$t('knowledge.uploadTipsTitle')"
          :tips-description="$t('knowledge.uploadTipsDescription')"
          @batch-upload="handleBatchUpload"
        />
      </template>
      
      <template #footer>
        <el-button v-if="!showUploadProgress" @click="closeUploadDialog">{{ $t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  Search,
  FolderAdd,
  Upload,
  Close,
  Edit,
  Delete,
  Folder,
  Picture,
  MoreFilled,
  View,
  Loading,
  Warning,
  Collection,
  Box,
  Files,
  Lock
} from '@element-plus/icons-vue'
// import FilePreview from '@/components/Common/FilePreview.vue' // 已改为新窗口打开，不再需要
import { BaseTooltip, FileTypeIcon, BasePagination, BaseScrollbar } from '@/components/Common'
import { FileUpload } from '@/components/Business/Form'
import FileUploadProgressList from './FileUploadProgressList.vue'
import {
  getKnowledgeList,
  getKnowledgeListPage,
  createKnowledge,
  updateKnowledge,
  getKnowledgeFiles,
  getKnowledgeFolderTree,
  getKnowledgeFolders,
  getKnowledgeFolderDetail,
  createKnowledgeFolder,
  updateKnowledgeFolder,
  deleteKnowledgeFolder,
  getKnowledgeFoldersFiles,
  uploadKnowledgeFile,
  uploadFileToKnowledge,
  uploadFilesToKnowledge,
  renameKnowledgeFile,
  deleteKnowledgeFile,
  deleteKnowledge,
  searchKnowledge,
  uploadKnowledgeCover,
  // 文件关联接口
  getKnowledgeFileRelationList,
  createKnowledgeFileRelation,
  updateKnowledgeFileRelation,
  deleteKnowledgeFileRelation
} from '@/api/Knowledge/knowledge'
import { createLogger } from '@/utils/simpleLogger'
import { formatDate } from '@/utils/date'
import { openFilePreviewInNewWindow } from '@/utils/file'
import { uploadAvatar } from '@/api/User'
import { useAuthStore } from '@/store/modules/auth'
import {
  ATTACHMENT_RELATION,
  ATTACHMENT_CATEGORY,
  DEFAULT_AVATAR_MAX_SIZE_MB,
  validateFileSize
} from '@/constants/attachment'

const logger = createLogger('KnowledgeList')
const { t } = useI18n()
const authStore = useAuthStore()

// 🔥 新增：处理知识库封面URL的计算属性
const getCoverUrl = (kb) => {
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
    // 假设这是 MinIO 路径，需要通过文件ID访问
    if (coverFileId) {
      return `/api/file/preview/${coverFileId}`
    }
  }
  
  // 其他情况返回原值
  return coverUrl || null
}

// 响应式数据
const isContentExpanded = ref(true)
const knowledgeBases = ref([])
const selectedKnowledgeBase = ref(null)
const kbItems = ref([])
const kbContents = ref({}) // kbId -> items
const currentFolder = ref(null)
// 🔥 修复：维护文件夹路径栈，用于显示完整的面包屑导航
const folderPathStack = ref([]) // 存储文件夹路径：[{ id, name }, { id, name }, ...]
const kbSearchQuery = ref('')
const showSearch = ref(false)
const kbUploadInput = ref(null)
// 🔥 修复：知识库列表滚动容器引用
const kbListScrollRef = ref(null)

// 🔥 修复：截断知识库名称，只显示8个字符
const truncateKbName = (name) => {
  if (!name) return ''
  return name.length > 8 ? name.substring(0, 8) : name
}

// 知识库类型 Tab：all=全部, personal=个人知识库, project=项目知识库
const activeKbType = ref('all')

// 知识库列表搜索相关
const showKbSearch = ref(false)
const kbListSearchQuery = ref('')
const kbListSearchTimer = ref(null)

// 知识库列表分页相关
const kbListPagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  hasMore: true,
  loading: false
})

// 分页相关
const pagination = ref({
  currentPage: 1,
  pageSize: 10, // 根目录每页显示10条
  total: 0,
  pages: 0 // 🔥 新增：总页数（使用后端返回的 pages 字段）
})

// 文件上传对话框相关
const showUploadDialog = ref(false)
const pendingUploadFiles = ref([])
const showUploadProgress = ref(false) // 是否显示上传进度
const uploadResults = ref([]) // 上传结果列表

// 创建知识库相关
const showCreateDialog = ref(false)
const newKbForm = ref({
  name: '',
  description: '',
  coverUrl: ''
})
const coverInput = ref(null)
const newKbCoverFileId = ref(null)

// 知识库设置相关
const showSettingsDialog = ref(false)
const settingsForm = ref({
  name: '',
  description: '',
  coverUrl: ''
})
const settingsCoverInput = ref(null)
const settingsCoverFileId = ref(null)

// 重命名和删除相关
const showRenameDialog = ref(false)
const showDeleteDialog = ref(false)
const editingItem = ref(null)
const deletingItem = ref(null)
const deletingKnowledgeBase = ref(null) // 要删除的知识库
const renameForm = ref('')

// 权限设置弹窗（公开可见 / 转为私密）
const showPermissionDialog = ref(false)
const permissionKb = ref(null)
const permissionRadio = ref(0) // 0=转为私密，1=公开可见，默认转为私密
const permissionSaving = ref(false)

// 文件预览相关 - 使用 FilePreview 组件
const showPreviewDialog = ref(false)
const previewFileInfo = ref(null)

// 拖拽相关
const draggedFile = ref(null)
const draggedFiles = ref([]) // 批量拖拽的文件列表
// 批量选择相关
const selectedFileIds = ref(new Set())
const isSelectMode = ref(false)
const dragOverFolderId = ref(null)

// 创建文件夹相关
const showCreateFolderDialog = ref(false)
const createFolderForm = ref({
  name: '',
  parentId: 0
})

// 计算属性
const currentKbDisplayItems = computed(() => {
  const q = kbSearchQuery.value.trim().toLowerCase()
  const list = currentFolder.value
    ? currentFolder.value.files || []
    : kbItems.value
  if (!q) return list
  return list.filter((it) =>
    (it.name || '').toLowerCase().includes(q)
  )
})

// 方法
const loadKnowledgeBases = async (isLoadMore = false) => {
  if (kbListPagination.value.loading) {
    return
  }

  try {
    kbListPagination.value.loading = true
    
    if (!isLoadMore) {
      // 重置分页
      kbListPagination.value.pageNo = 1
      kbListPagination.value.hasMore = true
      knowledgeBases.value = []
    }

    logger.info('加载知识库列表', { 
      pageNo: kbListPagination.value.pageNo,
      pageSize: kbListPagination.value.pageSize,
      keyword: kbListSearchQuery.value,
      isLoadMore
    })

    const response = await getKnowledgeListPage({
      pageNo: kbListPagination.value.pageNo,
      pageSize: kbListPagination.value.pageSize,
      keyword: kbListSearchQuery.value || undefined,
      kbType: activeKbType.value === 'all' ? undefined : activeKbType.value
    })

    if (response.code === 200 && response.data) {
      // 🔥 修正：使用后端实际返回的字段名 records
      const list = response.data.records || []
      // 🔥 修正：直接使用后端返回的字段，移除不存在的字段兼容处理
      const newList = list.map((kb) => ({
        ...kb,
        // 后端已直接返回 coverUrl 和 coverFileId，无需兼容处理
        coverUrl: kb.coverUrl,
        coverFileId: kb.coverFileId
      }))

      if (isLoadMore) {
        // 追加数据
        knowledgeBases.value.push(...newList)
      } else {
        // 替换数据
        knowledgeBases.value = newList
      }

      // 🔥 修正：使用后端实际返回的分页字段 total
      kbListPagination.value.total = response.data.total || 0
      // 🔥 修复：正确判断是否还有更多数据
      // 判断逻辑：已加载数量 < 总数，或者当前页返回的数据量等于pageSize（说明可能还有下一页）
      const loadedCount = knowledgeBases.value.length
      const currentPageSize = list.length
      const total = kbListPagination.value.total
      
      // 如果有总数，直接比较；如果没有总数，根据当前页数据量判断
      if (total > 0) {
        kbListPagination.value.hasMore = loadedCount < total
      } else {
        // 如果没有返回总数，根据当前页数据量判断（如果等于pageSize，可能还有更多）
        kbListPagination.value.hasMore = currentPageSize === kbListPagination.value.pageSize
      }
      
      logger.info('分页信息更新', {
        loadedCount,
        total,
        currentPageSize,
        pageSize: kbListPagination.value.pageSize,
        hasMore: kbListPagination.value.hasMore,
        responseTotal: response.data.total,
        responseCurrent: response.data.current,
        responseSize: response.data.size,
        responsePages: response.data.pages
      })

      logger.info('知识库列表加载成功', { 
        count: knowledgeBases.value.length,
        total: kbListPagination.value.total,
        hasMore: kbListPagination.value.hasMore,
        items: knowledgeBases.value.map(kb => ({
          id: kb.id,
          name: kb.name,
          difyKnowdataId: kb.difyKnowdataId,
          difyKbId: kb.difyKbId
        }))
      })
      
      // 默认选中第一条数据（仅在首次加载时）
      if (!isLoadMore && knowledgeBases.value.length > 0) {
        selectKnowledgeBase(knowledgeBases.value[0])
      }
    } else {
      ElMessage.warning(t('knowledge.getKbListFailed'))
      if (!isLoadMore) {
        knowledgeBases.value = []
      }
    }
  } catch (error) {
    logger.error('加载知识库列表失败', error)
    // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
    if (!error._messageShown) {
      ElMessage.error(t('knowledge.loadKbListFailed'))
    }
    if (!isLoadMore) {
      knowledgeBases.value = []
    }
  } finally {
    kbListPagination.value.loading = false
  }
}

// 处理知识库列表搜索输入（防抖）
const handleKbListSearchInput = () => {
  if (kbListSearchTimer.value) {
    clearTimeout(kbListSearchTimer.value)
  }
  
  kbListSearchTimer.value = setTimeout(() => {
    // 重置分页并重新加载
    kbListPagination.value.pageNo = 1
    loadKnowledgeBases(false)
  }, 500) // 500ms 防抖
}

// 切换知识库列表搜索框显示
const toggleKbListSearch = () => {
  showKbSearch.value = !showKbSearch.value
  if (!showKbSearch.value) {
    // 关闭搜索框时清空搜索关键字并重新加载
    kbListSearchQuery.value = ''
    kbListPagination.value.pageNo = 1
    loadKnowledgeBases(false)
  }
}

// 处理知识库列表滚动事件（滚动加载更多）
const handleKbListScroll = (event) => {
  const { scrollTop, scrollHeight, clientHeight } = event.target
  const scrollBottom = scrollHeight - scrollTop - clientHeight
  
  // 距离底部 50px 时加载更多
  if (scrollBottom < 50 && kbListPagination.value.hasMore && !kbListPagination.value.loading) {
    kbListPagination.value.pageNo++
    loadKnowledgeBases(true)
  }
}

// 🔥 处理"显示更多"按钮点击事件
const handleLoadMore = () => {
  if (kbListPagination.value.hasMore && !kbListPagination.value.loading) {
    kbListPagination.value.pageNo++
    loadKnowledgeBases(true)
  }
}

const selectKnowledgeBase = (kb) => {
  selectedKnowledgeBase.value = kb
  const kbId = Number(kb.id)
  if (!kbContents.value[kbId]) {
    kbContents.value[kbId] = []
  }
  kbItems.value = kbContents.value[kbId]
  currentFolder.value = null
  // 🔥 修复：清空文件夹路径栈
  folderPathStack.value = []
  kbSearchQuery.value = ''
  
  // 记录选中的知识库信息，用于调试
  logger.info('选中知识库', {
    id: kb.id,
    name: kb.name,
    difyKnowdataId: kb.difyKnowdataId,
    difyKbId: kb.difyKbId,
    allFields: Object.keys(kb) // 记录所有字段，便于调试
  })
  
  // 检查 Dify KB ID 是否存在
  const difyKbId = kb.difyKnowdataId || kb.difyKbId
  if (!difyKbId) {
    logger.warn('选中的知识库缺少 Dify KB ID', {
      kbId: kb.id,
      kbName: kb.name,
      allFields: Object.keys(kb),
      fieldValues: {
        difyKnowdataId: kb.difyKnowdataId,
        difyKbId: kb.difyKbId,
        dify_knowdata_id: kb.dify_knowdata_id,
        dify_kb_id: kb.dify_kb_id
      }
    })
    ElMessage.warning(t('knowledge.kbMissingDifyId'))
  }
  
  // 重置分页并加载知识库文件列表
  pagination.value.currentPage = 1
  pagination.value.pageSize = 10 // 根目录每页显示10条
  loadKnowledgeFiles(kbId, null, pagination.value.currentPage, pagination.value.pageSize)
}

/**
 * 加载知识库文件和文件夹列表（支持分页）
 * 🔥 优化：统一使用 folders-files 接口，后端完成混合分页，前端无需计算
 * @param {number} knowledgeId - 知识库ID
 * @param {number} [folderId] - 文件夹ID，可选，null表示根目录
 * @param {number} [page] - 页码，默认为1
 * @param {number} [size] - 每页大小，默认为10
 */
const loadKnowledgeFiles = async (knowledgeId, folderId = null, page = 1, size = 10) => {
  if (!knowledgeId) return

  try {
    // 确保 ID 是数字类型
    const kbId = Number(knowledgeId)
    const fId = folderId !== null ? Number(folderId) : null // null表示根目录
    
    logger.info('加载知识库文件和文件夹列表', { knowledgeId: kbId, folderId: fId, page, size })
    
    // 🔥 优化：统一使用 folders-files 接口，后端完成混合分页
    const pageResponse = await getKnowledgeFoldersFiles(kbId, { 
      folderId: fId, 
      page, 
      size 
    })
    
    if (pageResponse.code !== 200 || !pageResponse.data) {
      throw new Error(pageResponse.message || '获取文件和文件夹列表失败')
    }
    
    const pageData = pageResponse.data
    const records = pageData.records || pageData.list || []
    
    // 🔥 修复：从接口响应中获取知识库封面信息并更新 selectedKnowledgeBase
    if (records.length > 0 && records[0]) {
      const firstItem = records[0]
      if (firstItem.coverUrl || firstItem.coverFileId) {
        if (selectedKnowledgeBase.value && selectedKnowledgeBase.value.id === kbId) {
          if (firstItem.coverUrl) {
            selectedKnowledgeBase.value.coverUrl = firstItem.coverUrl
          }
          if (firstItem.coverFileId) {
            selectedKnowledgeBase.value.coverFileId = firstItem.coverFileId
          }
        }
      }
    }
    
    // 🔥 优化：统一处理根目录和文件夹内的响应
    const items = []
    
    if (fId === null || fId === 0) {
      // ========== 根目录：混合列表（文件夹在前，文件在后） ==========
      records.forEach(item => {
        if (item.type === 'folder') {
          // 文件夹
          items.push({
            id: item.folderId,
            type: 'folder',
            name: item.folderName || '未命名文件夹',
            parentId: 0,
            knowledgeId: kbId,
            fileCount: item.fileCount || 0,
            files: (item.files || []).map(record => ({
              id: record.id,
              type: 'file',
              name: record.fileName || t('knowledge.unnamedFile'),
              attachmentId: record.attachmentId,
              folderId: record.folderId,
              sortOrder: record.sortOrder,
              time: record.createdTime || record.updatedTime || '',
              ext: (record.fileName || '').split('.').pop() || '',
              fileSize: record.fileSize,
              callback: record.callback ? (typeof record.callback === 'string' ? JSON.parse(record.callback) : record.callback) : null
            }))
          })
        } else if (item.type === 'file') {
          // 独立的文件（未分类的文件）
          items.push({
            id: item.fileId,
            type: 'file',
            name: item.fileName || t('knowledge.unnamedFile'),
            attachmentId: item.attachmentId,
            folderId: item.folderId,
            sortOrder: 0,
            time: item.createdTime || item.updatedTime || '',
            ext: item.ext || (item.fileName ? item.fileName.split('.').pop() : ''),
            fileSize: item.fileSize,
            callback: item.callback ? (typeof item.callback === 'string' ? JSON.parse(item.callback) : item.callback) : null
          })
        }
      })
      
      // 更新根目录列表
      kbContents.value[kbId] = items
      kbItems.value = items
    } else {
      // ========== 文件夹内：混合列表（文件夹在前，文件在后） ==========
      records.forEach(item => {
        if (item.type === 'folder') {
          // 文件夹
          items.push({
            id: item.folderId,
            type: 'folder',
            name: item.folderName || '未命名文件夹',
            parentId: fId,
            knowledgeId: kbId,
            fileCount: item.fileCount || 0
          })
        } else if (item.type === 'file') {
          // 文件
          items.push({
            id: item.fileId,
            type: 'file',
            name: item.fileName || t('knowledge.unnamedFile'),
            attachmentId: item.attachmentId,
            folderId: item.folderId,
            sortOrder: 0,
            time: item.createdTime || item.updatedTime || '',
            ext: item.ext || (item.fileName ? item.fileName.split('.').pop() : ''),
            fileSize: item.fileSize,
            callback: item.callback ? (typeof item.callback === 'string' ? JSON.parse(item.callback) : item.callback) : null
          })
        }
      })
      
      // 更新当前文件夹的 files 属性
      if (currentFolder.value) {
        currentFolder.value.files = items
      }
      kbItems.value = items
    }
    
    // 🔥 修复：使用后端返回的分页信息（包括 pages 总页数）
    pagination.value.total = pageData.total || 0
    pagination.value.currentPage = pageData.current || page
    pagination.value.pageSize = pageData.size || size
    pagination.value.pages = pageData.pages || Math.ceil((pageData.total || 0) / (pageData.size || size))
    
    logger.info('文件和文件夹列表加载成功', { 
      knowledgeId: kbId,
      folderId: fId,
      itemCount: items.length,
      total: pageData.total,
      current: pageData.current,
      size: pageData.size,
      pages: pageData.pages,
      page: page,
      size: size
    })
  } catch (error) {
    // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
    if (!error._messageShown) {
      ElMessage.error(error.message || t('knowledge.loadFilesError'))
    }
    const kbId = Number(knowledgeId)
    const fId = folderId !== null ? Number(folderId) : 0
    if (fId === 0) {
      kbContents.value[kbId] = []
      kbItems.value = []
    } else {
      if (currentFolder.value) {
        currentFolder.value.files = []
      }
    }
  }
}

const createKnowledgeBase = async () => {
  if (!newKbForm.value.name.trim()) {
    ElMessage.warning(t('knowledge.enterKbName'))
    return
  }

  // 如果选择了封面但尚未上传到服务器，提示用户先上传
  if (newKbForm.value.coverUrl && !newKbCoverFileId.value) {
    logger.warn('创建知识库时检测到本地封面未上传')
    // 这里不强制阻止创建，只做日志提示；真正的封面以上传接口返回为准
  }

  try {
    logger.info('创建知识库', newKbForm.value)
    const response = await createKnowledge({
      name: newKbForm.value.name,
      description: newKbForm.value.description,
      permission: 'only_me',
      indexingTechnique: 'high_quality',
      coverFileId: newKbCoverFileId.value,
      coverUrl: newKbForm.value.coverUrl
    })

    if (response.code === 200 && response.data) {
      // 🔥 修正：使用后端实际返回的字段 coverUrl、coverFileId、ownerName 等，避免右侧显示「未知创建人」
      const coverUrl = response.data.coverUrl
      const coverFileId = response.data.coverFileId
      
      const newKb = {
        id: response.data.id,
        name: response.data.name,
        description: response.data.description,
        coverUrl: coverUrl,
        coverFileId: coverFileId,
        docCount: 0,
        updatedAt: new Date(),
        shortName: response.data.name,
        difyKnowdataId: response.data.difyKnowdataId,
        difyKbId: response.data.difyKbId,
        ownerId: response.data.ownerId,
        ownerName: response.data.ownerName ?? authStore.userInfo?.realName ?? '',
        kbType: response.data.kbType ?? 'personal',
        isShared: response.data.isShared ?? 0,
        // 新建知识库的当前用户即为创建人，拥有编辑权限，保证「...」菜单和资料修改等入口立即可见，无需刷新
        canEdit: response.data.canEdit ?? true
      }
      logger.info('创建知识库成功', {
        id: newKb.id,
        name: newKb.name,
        coverUrl: newKb.coverUrl,
        coverFileId: newKb.coverFileId,
        difyKnowdataId: newKb.difyKnowdataId,
        difyKbId: newKb.difyKbId
      })
      
      // 🔥 修复：添加到列表并选中，封面会自动显示在左侧列表和详情区域
      knowledgeBases.value.unshift(newKb)
      kbContents.value[newKb.id] = []
      selectedKnowledgeBase.value = newKb // 选中新创建的知识库，封面会显示在详情区域
      kbItems.value = []
      showCreateDialog.value = false
      resetCreateForm()
      ElMessage.success(t('knowledge.knowledgeCreated'))
    } else {
      ElMessage.error(response.message || t('knowledge.createKbFailed'))
    }
  } catch (error) {
    logger.error('创建知识库失败', error)
    // 检查是否是知识库名称重复错误（错误码 6002）
    const errorCode = error.response?.data?.code || error.code
    if (errorCode === 6002) {
      ElMessage.error(t('knowledge.kbExists'))
    } else {
      const errorMessage = error.response?.data?.message || error.message || t('knowledge.createKbFailedRetry')
      ElMessage.error(errorMessage)
    }
  }
}

const resetCreateForm = () => {
  newKbForm.value = {
    name: '',
    description: '',
    coverUrl: ''
  }
  newKbCoverFileId.value = null
}

const triggerCoverUpload = () => {
  coverInput.value && coverInput.value.click()
}

// 🔥 新增：只用于上传封面的对话框（从弹窗内「选择图片」用）
const showCoverUploadDialog = ref(false)
const coverUploadInput = ref(null)
// 头像直接选封面用（无弹窗，点击即唤起本地文件选择）
const coverUploadInputDirect = ref(null)

// 点击头像：有权限则直接唤起本地文件选择，无权限不生效、仅打日志；不弹窗
const openCoverUploadDialog = () => {
  if (!selectedKnowledgeBase.value) return
  if (!canOperateKb(selectedKnowledgeBase.value)) {
    logger.debug('[权限] 无封面修改权限，点击头像不生效', {
      kbId: selectedKnowledgeBase.value.id,
      kbName: selectedKnowledgeBase.value.name,
      ownerId: selectedKnowledgeBase.value.ownerId
    })
    return
  }
  coverUploadInputDirect.value && coverUploadInputDirect.value.click()
}

// 触发封面上传输入框点击
const triggerCoverUploadInput = () => {
  coverUploadInput.value && coverUploadInput.value.click()
}

// 处理封面上传选择（仅上传封面）
const handleCoverUploadSelect = async (e) => {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning(t('knowledge.selectImageFile'))
    return
  }
  
  try {
    await uploadCoverFile(file, true)
    showCoverUploadDialog.value = false
    if (e.target) e.target.value = ''
  } catch (error) {
    logger.error('封面上传失败', error)
    // 错误已在 uploadCoverFile 中处理
  }
}

/** 与「资料修改」按钮同权：仅 owner 或 admin 可打开；无权限不生效、不提示、仅打日志 */
const openSettingsDialogWithPermission = () => {
  if (!selectedKnowledgeBase.value) return
  if (!canOperateKb(selectedKnowledgeBase.value)) {
    logger.debug('[权限] 无资料修改权限，点击不生效', {
      kbId: selectedKnowledgeBase.value.id,
      kbName: selectedKnowledgeBase.value.name,
      ownerId: selectedKnowledgeBase.value.ownerId
    })
    return
  }
  openSettingsDialog()
}

/** 左侧列表项图标点击：有编辑权限则进入资料修改；无编辑权限仅选中知识库以查看内容 */
const openEditFromList = (kb) => {
  if (!kb) return
  selectKnowledgeBase(kb)
  if (kb.canEdit === true) nextTick(() => openSettingsDialog())
}

// 打开知识库设置弹窗
const openSettingsDialog = () => {
  if (!selectedKnowledgeBase.value) {
    return
  }
  
  // 填充表单数据，确保封面字段正确回显
  settingsForm.value = {
    name: selectedKnowledgeBase.value.name || '',
    description: selectedKnowledgeBase.value.description || '',
    coverUrl: selectedKnowledgeBase.value.coverUrl || ''
  }
  
  // 重置封面文件ID（如果需要上传新封面，会重新设置）
  settingsCoverFileId.value = selectedKnowledgeBase.value.coverFileId || null
  
  showSettingsDialog.value = true
  
  logger.info('打开知识库设置弹窗', {
    id: selectedKnowledgeBase.value.id,
    name: selectedKnowledgeBase.value.name,
    coverUrl: selectedKnowledgeBase.value.coverUrl,
    coverFileId: selectedKnowledgeBase.value.coverFileId
  })
}

// 重置设置表单
const resetSettingsForm = () => {
  settingsForm.value = {
    name: '',
    description: '',
    coverUrl: ''
  }
  settingsCoverFileId.value = null
}

// 触发设置弹窗的封面上传
const triggerSettingsCoverUpload = () => {
  settingsCoverInput.value && settingsCoverInput.value.click()
}

// 处理设置弹窗的封面选择
const COVER_MAX_SIZE_MB = DEFAULT_AVATAR_MAX_SIZE_MB

const uploadCoverFile = async (file, isSettings = false) => {
  const sizeValidation = validateFileSize(file, COVER_MAX_SIZE_MB)
  if (!sizeValidation.passed) {
    ElMessage.error(sizeValidation.reason)
    return
  }

  // 🔥 修复：创建知识库时使用通用上传接口，设置弹窗中使用专用封面上传接口
  if (isSettings && selectedKnowledgeBase.value?.id) {
    // 知识库设置弹窗：使用专用的封面上传接口
    try {
      logger.info('上传知识库封面', {
        knowledgeId: selectedKnowledgeBase.value.id,
        fileName: file.name,
        fileSize: file.size
      })
      
      const response = await uploadKnowledgeCover(selectedKnowledgeBase.value.id, file)
      
      if (response.code === 200 && response.data) {
        // 🔥 修正：使用后端实际返回的字段 coverUrl 和 coverFileId
        const coverUrl = response.data.coverUrl
        const coverFileId = response.data.coverFileId
        
        if (!coverUrl) {
          throw new Error(t('knowledge.coverUploadNoUrl'))
        }
        
        // 🔥 修复：立即更新设置表单（弹窗回显）
        settingsForm.value.coverUrl = coverUrl
        settingsCoverFileId.value = coverFileId
        
        // 🔥 修复：立即更新选中的知识库数据（详情区域回显）
        selectedKnowledgeBase.value.coverUrl = coverUrl
        selectedKnowledgeBase.value.coverFileId = coverFileId
        
        // 🔥 修复：立即更新知识库列表中的数据（左侧列表回显）
        const kbIndex = knowledgeBases.value.findIndex(
          kb => kb.id === selectedKnowledgeBase.value.id
        )
        if (kbIndex > -1) {
          knowledgeBases.value[kbIndex].coverUrl = coverUrl
          knowledgeBases.value[kbIndex].coverFileId = coverFileId
        }
        
        logger.info('知识库封面上传成功并已同步更新所有位置', {
          knowledgeId: selectedKnowledgeBase.value.id,
          coverUrl,
          coverFileId
        })
        
        ElMessage.success(t('knowledge.coverUploadSuccess'))
      } else {
        throw new Error(response.message || t('knowledge.coverUploadFailed'))
      }
    } catch (error) {
      logger.error('上传知识库封面失败', error)
      ElMessage.error(error.message || t('knowledge.coverUploadFailedRetry'))
      throw error
    }
  } else {
    // 创建知识库：使用通用上传接口
    const formData = new FormData()
    formData.append('file', file)
    formData.append('relationType', ATTACHMENT_RELATION.KNOWLEDGE)
    formData.append('attachmentType', ATTACHMENT_CATEGORY.IMAGE)
    formData.append('isPublic', '0')

    try {
      const response = await uploadAvatar(formData)
      const payload = response?.data?.data || response?.data || response || {}

      const url = payload.previewUrl || payload.avatar || payload.avatarUrl || payload.url || payload.fileUrl
      const fileId = payload.avatarFileId || payload.fileId || payload.attachmentId || payload.id || null

      if (!url) {
        throw new Error(t('knowledge.coverUploadNoUrl'))
      }

      // 🔥 修复：立即更新创建表单（弹窗回显）
      newKbForm.value.coverUrl = url
      newKbCoverFileId.value = fileId
      
      logger.info('创建知识库封面上传成功', {
        coverUrl: url,
        coverFileId: fileId
      })
      
      ElMessage.success(t('knowledge.coverUploadSuccess'))
    } catch (error) {
      logger.error('上传知识库封面失败', error)
      ElMessage.error(error.message || t('knowledge.coverUploadFailedRetry'))
      throw error
    }
  }
}

const handleSettingsCoverSelect = async (e) => {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning(t('knowledge.selectImageFile'))
    return
  }
  await uploadCoverFile(file, true)
}

// 更新知识库设置
const updateKnowledgeBaseSettings = async () => {
  if (!settingsForm.value.name.trim()) {
    ElMessage.warning(t('knowledge.enterKbName'))
    return
  }

  if (!selectedKnowledgeBase.value) {
    ElMessage.warning('请先选择知识库')
    return
  }

  try {
    logger.info('更新知识库设置', {
      id: selectedKnowledgeBase.value.id,
      form: settingsForm.value,
      coverFileId: settingsCoverFileId.value
    })
    
    const response = await updateKnowledge(selectedKnowledgeBase.value.id, {
      name: settingsForm.value.name,
      description: settingsForm.value.description,
      coverFileId: settingsCoverFileId.value ?? selectedKnowledgeBase.value.coverFileId,
      coverUrl: settingsForm.value.coverUrl
    })

    if (response.code === 200 && response.data) {
      // 🔥 修正：使用后端实际返回的字段 coverUrl 和 coverFileId
      const updatedCoverUrl = response.data.coverUrl
      const updatedCoverFileId = response.data.coverFileId
      
      // 🔥 修复：更新选中的知识库数据（详情区域回显）
      selectedKnowledgeBase.value.name = response.data.name || settingsForm.value.name
      selectedKnowledgeBase.value.description = response.data.description || settingsForm.value.description
      selectedKnowledgeBase.value.coverUrl = updatedCoverUrl
      selectedKnowledgeBase.value.coverFileId = updatedCoverFileId
      
      // 🔥 修复：更新知识库列表中的数据（左侧列表回显）
      const kbIndex = knowledgeBases.value.findIndex(
        kb => kb.id === selectedKnowledgeBase.value.id
      )
      if (kbIndex > -1) {
        knowledgeBases.value[kbIndex].name = selectedKnowledgeBase.value.name
        knowledgeBases.value[kbIndex].description = selectedKnowledgeBase.value.description
        knowledgeBases.value[kbIndex].coverUrl = updatedCoverUrl
        knowledgeBases.value[kbIndex].coverFileId = updatedCoverFileId
      }
      
      showSettingsDialog.value = false
      resetSettingsForm()
      ElMessage.success('知识库设置已更新')
      logger.info('知识库设置更新成功，所有位置已同步', { 
        id: selectedKnowledgeBase.value.id,
        coverUrl: updatedCoverUrl,
        coverFileId: updatedCoverFileId
      })
      
      // 🔥 修复：更新知识库信息后，刷新当前页的数据
      // 由于排序是按更新时间倒序，更新后的知识库会移动到第一页
      // 为了保持用户体验，我们刷新当前页的数据（保持在当前页），而不是重置到第一页
      // 如果用户想看到更新后的知识库，可以手动滚动到第一页
      // 注意：这里不重置分页，保持用户当前浏览位置
      const currentPageNo = kbListPagination.value.pageNo
      kbListPagination.value.pageNo = currentPageNo // 保持当前页
      await loadKnowledgeBases(false) // 刷新当前页数据
    } else {
      // 如果响应码不是200，抛出错误让catch统一处理，避免重复提示
      throw new Error(response.message || '更新知识库设置失败')
    }
  } catch (error) {
    logger.error('更新知识库设置失败', error)
    const errorMessage = error.response?.data?.message || error.message || '更新知识库设置失败，请重试'
    ElMessage.error(errorMessage)
  }
}

// 截断文件名
const truncateFileName = (fileName, maxLength = 20) => {
  if (!fileName) return ''
  if (fileName.length <= maxLength) return fileName
  return fileName.substring(0, maxLength) + '...'
}

const handleCoverSelect = async (e) => {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning(t('knowledge.selectImageFile'))
    return
  }
  await uploadCoverFile(file, false)
}

/**
 * 切换批量选择模式
 */
const toggleSelectMode = () => {
  isSelectMode.value = !isSelectMode.value
  if (!isSelectMode.value) {
    // 退出选择模式时清空选中状态
    selectedFileIds.value.clear()
    draggedFiles.value = []
    draggedFile.value = null
  }
}

/**
 * 切换文件选中状态
 */
const toggleFileSelection = (fileId, checked) => {
  if (checked) {
    selectedFileIds.value.add(fileId)
  } else {
    selectedFileIds.value.delete(fileId)
  }
}

const toggleSearch = () => {
  showSearch.value = !showSearch.value
  if (!showSearch.value) {
    kbSearchQuery.value = ''
  }
}

const toggleContentCollapse = () => {
  isContentExpanded.value = !isContentExpanded.value
}

// 🔥 格式化上传时间：将 ISO 格式转换为 YYYY-MM-DD HH:mm
const formatUploadTime = (timeStr) => {
  if (!timeStr) return '-'
  try {
    return formatDate(timeStr, 'YYYY-MM-DD HH:mm')
  } catch (e) {
    logger.warn('时间格式化失败', { timeStr, error: e })
    return timeStr
  }
}

// 格式化文件大小（字节 → KB/MB）
const formatFileSize = (sizeBytes) => {
  if (!sizeBytes || sizeBytes <= 0) return '-'
  const kb = sizeBytes / 1024
  if (sizeBytes < 1024 * 1024) {
    // 小于 1MB 显示为 KB
    return `${Math.round(kb)} KB`
  }
  const mb = sizeBytes / (1024 * 1024)
  return `${mb.toFixed(1)} MB`
}

const handleContentAction = (command, item) => {
  if (command === 'preview') {
    handlePreviewFile(item)
  } else if (command === 'rename') {
    renameItem(item)
  } else if (command === 'delete') {
    deleteItem(item)
  }
}

/**
 * 预览文件 - 在新窗口打开
 * @param {Object} item - 文件项
 */
const handlePreviewFile = async (item) => {
  if (!item || !item.attachmentId) {
    ElMessage.warning(t('knowledge.fileIdNotExists'))
    return
  }

  try {
    await openFilePreviewInNewWindow(item.attachmentId, item.name)
  } catch (error) {
    // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
    if (!error._messageShown) {
      ElMessage.error(error.message || t('knowledge.previewFailed') || '预览失败，请稍后重试')
    }
  }
}

/**
 * 拖拽开始 - 文件（支持单个和批量）
 */
const handleDragStart = (event, file) => {
  if (file.type !== 'file') {
    return
  }
  if (!selectedKnowledgeBase.value?.canEdit) {
    event.preventDefault()
    return
  }
  // 检查是否有选中的文件（批量拖拽）
  const filesToDrag = selectedFileIds.value.size > 0 && selectedFileIds.value.has(file.id)
    ? Array.from(selectedFileIds.value).map(id => {
        const item = currentKbDisplayItems.value.find(item => item.id === id && item.type === 'file')
        return item
      }).filter(Boolean)
    : [file]
  
  // 保存拖拽的文件列表
  draggedFiles.value = filesToDrag
  // 使用第一个文件作为代表（用于UI显示）
  draggedFile.value = filesToDrag[0]
  
  // 设置拖拽数据
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/plain', JSON.stringify({ 
    fileIds: filesToDrag.map(f => f.id),
    fileNames: filesToDrag.map(f => f.name),
    isBatch: filesToDrag.length > 1
  }))
  logger.info('开始拖拽文件', { 
    fileCount: filesToDrag.length,
    fileIds: filesToDrag.map(f => f.id),
    isBatch: filesToDrag.length > 1
  })
}

/**
 * 拖拽结束
 */
const handleDragEnd = () => {
  draggedFile.value = null
  draggedFiles.value = []
  dragOverFolderId.value = null
  // 拖拽结束后不清空选中状态，方便用户继续操作
}

/**
 * 拖拽悬停 - 文件夹
 */
const handleDragOver = (event, folder) => {
  if (!selectedKnowledgeBase.value?.canEdit || folder.type === 'file' || !draggedFile.value) {
    return
  }
  event.dataTransfer.dropEffect = 'move'
}

/**
 * 拖拽进入 - 文件夹
 */
const handleDragEnter = (event, folder) => {
  if (!selectedKnowledgeBase.value?.canEdit || folder.type === 'file' || !draggedFile.value) {
    return
  }
  // 不能将文件拖到自己所在的文件夹
  if (draggedFile.value.folderId === folder.id) {
    return
  }
  dragOverFolderId.value = folder.id
  logger.info('拖拽进入文件夹', { folderId: folder.id, folderName: folder.name })
}

/**
 * 拖拽离开 - 文件夹
 */
const handleDragLeave = (event, folder) => {
  // 检查是否真的离开了文件夹（而不是进入子元素）
  const rect = event.currentTarget.getBoundingClientRect()
  const x = event.clientX
  const y = event.clientY
  if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
    return // 仍在文件夹内
  }
  if (dragOverFolderId.value === folder.id) {
    dragOverFolderId.value = null
  }
}

/**
 * 拖拽放下 - 文件夹（支持单个和批量）
 */
const handleDrop = async (event, folder) => {
  if (!selectedKnowledgeBase.value?.canEdit || folder.type === 'file' || !draggedFile.value) {
    return
  }

  // 获取要移动的文件列表（批量或单个）
  const filesToMove = draggedFiles.value.length > 0 ? draggedFiles.value : [draggedFile.value]
  
  // 过滤掉已经在目标文件夹的文件
  const validFiles = filesToMove.filter(file => {
    const currentFolderId = file.folderId || null
    const targetFolderId = folder.id
    return currentFolderId !== targetFolderId
  })
  
  if (validFiles.length === 0) {
    dragOverFolderId.value = null
    ElMessage.warning('所选文件已在目标文件夹中')
    return
  }
  
  dragOverFolderId.value = null
  
  logger.info('拖拽放下文件到文件夹', {
    fileCount: validFiles.length,
    fileIds: validFiles.map(f => f.id),
    targetFolderId: folder.id,
    targetFolderName: folder.name,
    isBatch: validFiles.length > 1
  })
  
  try {
    // 批量移动文件
    const movePromises = validFiles.map(file => 
      updateKnowledgeFileRelation(file.id, {
        folderId: folder.id
      })
    )
    
    await Promise.all(movePromises)
    
    const successMessage = validFiles.length > 1
      ? `已成功移动 ${validFiles.length} 个文件到 ${folder.name}`
      : t('knowledge.moveFileSuccess', { folderName: folder.name }) || `文件已移动到 ${folder.name}`
    
    ElMessage.success(successMessage)
    
    // 🔥 优化：刷新列表 - 同时刷新当前文件夹和根目录（以立即更新文件夹数量）
    if (selectedKnowledgeBase.value) {
      const kbId = Number(selectedKnowledgeBase.value.id)
      const folderId = currentFolder.value ? currentFolder.value.id : null
      
      // 刷新当前文件夹列表
      await loadKnowledgeFiles(kbId, folderId, pagination.value.currentPage, pagination.value.pageSize)
      
      // 刷新根目录列表以更新所有文件夹的文件数量显示（包括目标文件夹）
      await loadKnowledgeFiles(kbId, null, 1, pagination.value.pageSize)
    }
    
    // 清空选中状态并退出批量选择模式
    selectedFileIds.value.clear()
    draggedFiles.value = []
    isSelectMode.value = false
    
    logger.info('文件移动成功', { 
      fileCount: validFiles.length,
      fileIds: validFiles.map(f => f.id),
      targetFolderId: folder.id
    })
  } catch (error) {
    // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
    if (!error._messageShown) {
      ElMessage.error(error.message || t('knowledge.moveFileFailed') || '移动文件失败，请稍后重试')
    }
  } finally {
    draggedFile.value = null
    draggedFiles.value = []
  }
}

/**
 * 检查文件是否可以拖拽到指定文件夹（支持批量）
 * @param {number|null} targetFolderId - 目标文件夹ID，null表示根目录
 * @returns {boolean} 是否可以拖拽
 */
const canDropToFolder = (targetFolderId) => {
  // 无编辑权限时不允许拖拽移动文件（个人知识库共享用户、项目知识库项目成员）
  if (!selectedKnowledgeBase.value?.canEdit) {
    return false
  }
  const filesToCheck = draggedFiles.value.length > 0 ? draggedFiles.value : (draggedFile.value ? [draggedFile.value] : [])
  if (filesToCheck.length === 0) {
    return false
  }
  
  // 检查是否有至少一个文件可以移动到目标文件夹
  const canMove = filesToCheck.some(file => {
    const currentFileFolderId = file.folderId || null
    // 不能将文件拖到自己所在的文件夹
    if (currentFileFolderId === targetFolderId || 
        (currentFileFolderId === null && targetFolderId === null) ||
        (currentFileFolderId === 0 && targetFolderId === null)) {
      return false
    }
    return true
  })
  
  return canMove
}

/**
 * 面包屑拖拽悬停
 */
const handleBreadcrumbDragOver = (event, folderId, folder) => {
  if (!selectedKnowledgeBase.value?.canEdit || !draggedFile.value) {
    return
  }
  event.dataTransfer.dropEffect = 'move'
}

/**
 * 面包屑拖拽进入
 */
const handleBreadcrumbDragEnter = (event, folderId, folder) => {
  if (!selectedKnowledgeBase.value?.canEdit || !draggedFile.value) {
    return
  }
  // 根目录：folderId 为 'root'，targetFolderId 为 0 或 null
  // 其他文件夹：folderId 为文件夹ID
  const targetFolderId = folderId === 'root' ? null : folderId
  
  // 不能将文件拖到自己所在的文件夹
  const currentFileFolderId = draggedFile.value.folderId
  if (currentFileFolderId === targetFolderId || 
      (currentFileFolderId === null && targetFolderId === null) ||
      (currentFileFolderId === 0 && targetFolderId === null)) {
    return
  }
  
  dragOverFolderId.value = folderId
  logger.info('拖拽进入面包屑文件夹', { folderId, folderName: folder?.name || '根目录' })
}

/**
 * 面包屑拖拽离开
 */
const handleBreadcrumbDragLeave = (event, folderId) => {
  // 检查是否真的离开了（而不是进入子元素）
  const rect = event.currentTarget.getBoundingClientRect()
  const x = event.clientX
  const y = event.clientY
  if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
    return // 仍在元素内
  }
  if (dragOverFolderId.value === folderId) {
    dragOverFolderId.value = null
  }
}

/**
 * 面包屑拖拽放下 - 支持跨层级拖拽（支持批量）
 */
const handleBreadcrumbDrop = async (event, folderId, folder) => {
  if (!selectedKnowledgeBase.value?.canEdit || !draggedFile.value) {
    return
  }
  // 获取要移动的文件列表（批量或单个）
  const filesToMove = draggedFiles.value.length > 0 ? draggedFiles.value : [draggedFile.value]
  
  // 根目录：folderId 为 'root'，targetFolderId 为 0 或 null
  // 其他文件夹：folderId 为文件夹ID
  const targetFolderId = folderId === 'root' ? null : folderId
  const targetFolderName = folderId === 'root' ? t('knowledge.content') : (folder?.name || '')
  
  // 过滤掉已经在目标文件夹的文件
  const validFiles = filesToMove.filter(file => {
    const currentFileFolderId = file.folderId || null
    if (currentFileFolderId === targetFolderId || 
        (currentFileFolderId === null && targetFolderId === null) ||
        (currentFileFolderId === 0 && targetFolderId === null)) {
      return false
    }
    return true
  })
  
  if (validFiles.length === 0) {
    dragOverFolderId.value = null
    ElMessage.warning('所选文件已在目标文件夹中')
    return
  }
  
  dragOverFolderId.value = null
  
  logger.info('拖拽放下文件到面包屑文件夹（跨层级）', {
    fileCount: validFiles.length,
    fileIds: validFiles.map(f => f.id),
    targetFolderId: targetFolderId,
    targetFolderName: targetFolderName,
    isBatch: validFiles.length > 1
  })
  
  try {
    // 批量移动文件
    const movePromises = validFiles.map(file => 
      updateKnowledgeFileRelation(file.id, {
        folderId: targetFolderId !== null ? targetFolderId : 0
      })
    )
    
    await Promise.all(movePromises)
    
    const successMessage = validFiles.length > 1
      ? `已成功移动 ${validFiles.length} 个文件到 ${targetFolderName}`
      : t('knowledge.moveFileSuccess', { folderName: targetFolderName }) || `文件已移动到 ${targetFolderName}`
    
    ElMessage.success(successMessage)
    
    // 🔥 优化：刷新列表 - 同时刷新当前文件夹和根目录（以立即更新文件夹数量）
    if (selectedKnowledgeBase.value) {
      const kbId = Number(selectedKnowledgeBase.value.id)
      const folderId = currentFolder.value ? currentFolder.value.id : null
      
      // 刷新当前文件夹列表
      await loadKnowledgeFiles(kbId, folderId, pagination.value.currentPage, pagination.value.pageSize)
      
      // 刷新根目录列表以更新所有文件夹的文件数量显示（包括目标文件夹）
      await loadKnowledgeFiles(kbId, null, 1, pagination.value.pageSize)
    }
    
    // 清空选中状态并退出批量选择模式
    selectedFileIds.value.clear()
    draggedFiles.value = []
    isSelectMode.value = false
    
    logger.info('文件移动成功（跨层级）', { 
      fileCount: validFiles.length,
      fileIds: validFiles.map(f => f.id),
      targetFolderId: targetFolderId
    })
  } catch (error) {
    logger.error('文件移动失败（跨层级）', error)
    ElMessage.error(error.message || t('knowledge.moveFileFailed') || '移动文件失败，请稍后重试')
  } finally {
    draggedFile.value = null
    draggedFiles.value = []
  }
}

const createFolder = () => {
  if (!selectedKnowledgeBase.value) {
    ElMessage.warning(t('knowledge.selectKbFirst'))
    return
  }
  
  // 🔥 修复：设置父文件夹ID（当前文件夹的ID，如果是根目录则为0）
  createFolderForm.value.parentId = currentFolder.value ? currentFolder.value.id : 0
  createFolderForm.value.name = ''
  showCreateFolderDialog.value = true
}

const confirmCreateFolder = async () => {
  if (!createFolderForm.value.name.trim()) {
    ElMessage.warning(t('knowledge.folderNamePlaceholder'))
    return
  }
  
  if (!selectedKnowledgeBase.value) {
    ElMessage.warning(t('knowledge.selectKbFirst'))
    return
  }
  
  try {
    const kbId = Number(selectedKnowledgeBase.value.id)
    const parentId = createFolderForm.value.parentId
    
    const response = await createKnowledgeFolder(kbId, {
      folderName: createFolderForm.value.name.trim(), // 后端字段是 folderName
      knowledgeId: kbId,
      parentId: parentId
    })
    
    if (response.code === 200 && response.data) {
      ElMessage.success(t('knowledge.folderCreateSuccess'))
      showCreateFolderDialog.value = false
      createFolderForm.value.name = ''
      
      // 🔥 修复：刷新当前文件夹的内容（新创建的文件夹应该显示在当前文件夹下）
      // 使用 currentFolder.value.id 来刷新，确保刷新的是用户当前所在的文件夹
      const folderIdToRefresh = currentFolder.value ? Number(currentFolder.value.id) : null
      
      // 🔥 优化：只刷新当前文件夹，不刷新根目录（减少请求）
      // 如果是在根目录，刷新根目录；如果是在子文件夹，只刷新子文件夹
      await loadKnowledgeFiles(kbId, folderIdToRefresh, pagination.value.currentPage, pagination.value.pageSize)
    } else {
      // 🔥 修复：统一错误处理，避免重复提示
      // 如果响应中有错误码，优先使用错误码判断
      const errorCode = response.code
      const errorMessage = response.message || t('knowledge.folderCreateFailed')
      
      if (errorCode === 6006) {
        ElMessage.error(t('knowledge.folderNameExists'))
      } else {
        ElMessage.error(errorMessage)
      }
    }
  } catch (error) {
    // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
    if (error._messageShown) {
      // 响应拦截器已经显示过错误消息，不再重复显示
      return
    }
    
    // 🔥 修复：统一错误处理，避免重复提示
    const errorCode = error.response?.data?.code || error.code
    const errorMessage = error.response?.data?.message || error.message
    
    // 如果错误信息已经包含业务错误信息，直接使用；否则使用通用错误信息
    if (errorCode === 6006) {
      ElMessage.error(t('knowledge.folderNameExists'))
    } else if (errorMessage && !errorMessage.includes('Network Error') && !errorMessage.includes('timeout')) {
      // 如果有具体的错误信息且不是网络错误，显示具体错误
      ElMessage.error(errorMessage)
    } else {
      // 网络错误或其他未知错误，显示通用错误信息
      ElMessage.error(t('knowledge.folderCreateFailed'))
    }
  }
}

const cancelCreateFolder = () => {
  showCreateFolderDialog.value = false
  createFolderForm.value.name = ''
}

// 分页处理方法
const handlePageChange = (page) => {
  if (!selectedKnowledgeBase.value) return
  const kbId = Number(selectedKnowledgeBase.value.id)
  const folderId = currentFolder.value ? currentFolder.value.id : null
  pagination.value.currentPage = page
  loadKnowledgeFiles(kbId, folderId, page, pagination.value.pageSize)
}

const handlePageSizeChange = (size) => {
  if (!selectedKnowledgeBase.value) return
  const kbId = Number(selectedKnowledgeBase.value.id)
  const folderId = currentFolder.value ? currentFolder.value.id : null
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
  loadKnowledgeFiles(kbId, folderId, 1, size)
}

// 打开上传对话框
const openUploadDialog = () => {
  if (!selectedKnowledgeBase.value) {
    ElMessage.warning(t('knowledge.selectKbFirst'))
    return
  }
  pendingUploadFiles.value = []
  showUploadDialog.value = true
}

// 关闭上传对话框
const closeUploadDialog = () => {
  // 如果正在显示上传进度，不允许直接关闭（需要点击确认按钮）
  if (showUploadProgress.value) {
    return
  }
  showUploadDialog.value = false
  pendingUploadFiles.value = []
  uploadResults.value = []
}

// Dify 知识库不支持的文件扩展名
const unsupportedFileExtensions = [
  // PPT 文件
  'ppt', 'pptx',
  // 图片文件
  'jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico', 'tiff', 'tif'
]

// 获取文件扩展名
const getFileExtension = (fileName) => {
  if (!fileName) return ''
  const parts = fileName.split('.')
  return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : ''
}

// 处理批量上传（使用封装好的 FileUpload 组件）
const handleBatchUpload = async (files) => {
  if (!files.length || !selectedKnowledgeBase.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  // 验证文件类型：检查是否包含不支持的文件类型
  const unsupportedFiles = []
  const validFiles = []

  files.forEach(file => {
    const ext = getFileExtension(file.name)
    if (unsupportedFileExtensions.includes(ext)) {
      unsupportedFiles.push(file.name)
    } else {
      validFiles.push(file)
    }
  })

  // 如果有不支持的文件类型，提示用户
  if (unsupportedFiles.length > 0) {
    const unsupportedTypes = [...new Set(unsupportedFiles.map(name => getFileExtension(name)))]
    ElMessage.warning(t('knowledge.unsupportedFilesWarning', {
      types: unsupportedTypes.map(ext => `.${ext}`).join(', ')
    }))
  }

  // 如果没有有效文件可上传，直接返回
  if (validFiles.length === 0) {
    ElMessage.warning(t('knowledge.noValidFilesToUpload'))
    return
  }

  // 获取当前文件夹ID（如果有），确保是数字类型
  const folderId = currentFolder.value ? Number(currentFolder.value.id) : 0
  const knowledgeId = String(selectedKnowledgeBase.value.id) // 后端接口使用 String 类型的知识库ID

  try {
    logger.info('开始批量上传文件', { 
      fileCount: validFiles.length,
      unsupportedCount: unsupportedFiles.length,
      knowledgeId,
      folderId
    })

    // 🔥 修复：立即显示上传进度弹窗，初始化上传结果列表
    showUploadProgress.value = true
    // 初始化上传结果列表，每个文件都设置为"上传中"状态
    uploadResults.value = validFiles.map(file => ({
      fileName: file.name,
      success: false,
      errorMessage: null,
      attachmentId: null,
      fileSize: file.size || 0,
      stage: 1, // 初始阶段：MinIO上传中
      stageDescription: t('knowledge.stageMinIO') || '正在上传到存储...'
    }))

    // 🔥 修复：使用支持进度监听的上传函数，实时更新上传进度
    const response = await uploadFilesToKnowledge(
      knowledgeId, 
      validFiles, 
      folderId,
      (loaded, total) => {
        // 进度回调：实时更新上传进度
        // 计算整体进度百分比
        const progressPercent = total > 0 ? Math.round((loaded / total) * 100) : 0
        
        // 根据进度更新每个文件的状态
        uploadResults.value = uploadResults.value.map((result, index) => {
          // 计算当前文件应该处于的阶段
          let stage = 1
          let stageDescription = t('knowledge.stageMinIO') || '正在上传到存储...'
          
          if (progressPercent >= 100) {
            // 上传完成，等待后端处理
            stage = 4
            stageDescription = t('knowledge.stageProcessing') || '处理中...'
          } else if (progressPercent >= 80) {
            stage = 3
            stageDescription = t('knowledge.stageDify') || '正在上传到Dify...'
          } else if (progressPercent >= 40) {
            stage = 2
            stageDescription = t('knowledge.stageMinIOComplete') || '存储上传完成'
          }
          
          return {
            ...result,
            stage,
            stageDescription
          }
        })
      }
    )
    
    if (response.code === 200) {
      // 🔥 处理后端返回的详细上传结果
      const results = response.data || []
      
      // 将后端返回的结果映射到前端格式，并合并到现有的上传结果中
      // 根据文件名匹配，更新对应的文件状态
      uploadResults.value = uploadResults.value.map(existingResult => {
        const backendResult = results.find(r => r.fileName === existingResult.fileName)
        if (backendResult) {
          // 如果后端返回了该文件的结果，使用后端的结果
          return {
            fileName: backendResult.fileName || existingResult.fileName,
            success: backendResult.success || false,
            errorMessage: backendResult.errorMessage || null,
            attachmentId: backendResult.attachmentId || null,
            fileSize: backendResult.fileSize || existingResult.fileSize || 0,
            stage: backendResult.success ? 5 : 0, // 成功为阶段5，失败为阶段0
            stageDescription: backendResult.stageDescription || (backendResult.success ? t('knowledge.uploadComplete') : t('knowledge.uploadFailed'))
          }
        }
        // 如果后端没有返回该文件的结果，保持原状态（可能是上传中）
        return existingResult
      })
      
      // 统计成功和失败数量
      const successCount = uploadResults.value.filter(r => r.success).length
      const failedCount = uploadResults.value.filter(r => r.errorMessage).length
      
      logger.info('文件批量上传完成', { 
        total: uploadResults.value.length,
        success: successCount,
        failed: failedCount
      })
      
      // 如果有成功的文件，刷新文件列表
      if (successCount > 0) {
        await loadKnowledgeFiles(Number(selectedKnowledgeBase.value.id), folderId || null, pagination.value.currentPage, pagination.value.pageSize)
      }
      
      // 显示结果提示（只在全部完成时显示，避免重复提示）
      if (failedCount === 0 && successCount > 0) {
        ElMessage.success(t('knowledge.uploadSuccess', { count: successCount }))
      } else if (successCount === 0 && failedCount > 0) {
        // 全部失败的情况已经在 catch 中处理，这里不需要再显示
      } else if (failedCount > 0 && successCount > 0) {
        ElMessage.warning(t('knowledge.uploadPartialSuccess', { success: successCount, total: uploadResults.value.length }))
      }
    } else {
      throw new Error(response.message || t('knowledge.uploadFailed'))
    }
  } catch (error) {
    logger.error('批量上传文件失败', error)
    // 🔥 修复：上传失败时，检查是否有部分成功的结果
    if (error.response && error.response.data && Array.isArray(error.response.data)) {
      // 如果错误响应中包含了部分结果，合并到上传结果中
      const partialResults = error.response.data
      uploadResults.value = uploadResults.value.map(existingResult => {
        const backendResult = partialResults.find(r => r.fileName === existingResult.fileName)
        if (backendResult) {
          return {
            fileName: backendResult.fileName || existingResult.fileName,
            success: backendResult.success || false,
            errorMessage: backendResult.errorMessage || error.message || t('knowledge.uploadFailedRetry'),
            attachmentId: backendResult.attachmentId || null,
            fileSize: backendResult.fileSize || existingResult.fileSize || 0,
            stage: backendResult.success ? 5 : 0,
            stageDescription: backendResult.stageDescription || (backendResult.success ? t('knowledge.uploadComplete') : t('knowledge.uploadFailed'))
          }
        }
        // 没有后端结果的文件，标记为失败
        return {
          ...existingResult,
          success: false,
          errorMessage: error.message || t('knowledge.uploadFailedRetry'),
          stage: 0,
          stageDescription: t('knowledge.uploadFailed') || '上传失败'
        }
      })
    } else {
      // 完全失败，更新所有文件的状态为失败
      uploadResults.value = uploadResults.value.map(result => ({
        ...result,
        success: false,
        errorMessage: error.message || t('knowledge.uploadFailedRetry'),
        stage: 0,
        stageDescription: t('knowledge.uploadFailed') || '上传失败'
      }))
    }
    // 🔥 修复：检查错误是否已经在响应拦截器中显示过，避免重复提示
    if (!error._messageShown) {
      ElMessage.error(error.message || t('knowledge.uploadFailedRetry'))
    }
  }
}

// 🔥 处理上传进度关闭
const handleUploadProgressClose = () => {
  // 清空待上传文件列表并关闭对话框
  pendingUploadFiles.value = []
  showUploadDialog.value = false
  showUploadProgress.value = false
  uploadResults.value = []
}

const enterFolder = async (folder) => {
  if (!folder || folder.type !== 'folder') return
  
  // 🔥 修复：将当前文件夹添加到路径栈
  folderPathStack.value.push({
    id: folder.id,
    name: folder.name
  })
  
  currentFolder.value = folder
  
  // 重置分页并加载文件夹内的文件列表
  if (selectedKnowledgeBase.value) {
    pagination.value.currentPage = 1
    pagination.value.pageSize = 10
    await loadKnowledgeFiles(Number(selectedKnowledgeBase.value.id), Number(folder.id), 1, 10)
  }
}

// 🔥 修复：导航到路径栈中的指定文件夹
const navigateToFolder = async (folder, index) => {
  // 截断路径栈到指定位置
  folderPathStack.value = folderPathStack.value.slice(0, index + 1)
  
  // 设置当前文件夹（需要从路径栈中获取完整的文件夹对象）
  currentFolder.value = {
    id: folder.id,
    name: folder.name,
    type: 'folder',
    files: []
  }
  
  // 重置分页并加载文件夹内的文件列表
  if (selectedKnowledgeBase.value) {
    pagination.value.currentPage = 1
    pagination.value.pageSize = 10
    await loadKnowledgeFiles(Number(selectedKnowledgeBase.value.id), Number(folder.id), 1, 10)
  }
}

// 🔥 修复：返回根目录
const backToRoot = async () => {
  currentFolder.value = null
  // 🔥 修复：清空文件夹路径栈
  folderPathStack.value = []
  
  // 返回根目录时重新加载文件列表，重置分页
  if (selectedKnowledgeBase.value) {
    pagination.value.currentPage = 1
    pagination.value.pageSize = 10
    await loadKnowledgeFiles(Number(selectedKnowledgeBase.value.id), null, 1, 10)
  }
}

// 🔥 保留backToParent作为向后兼容（返回上一级）
const backToParent = async () => {
  if (folderPathStack.value.length > 0) {
    // 移除最后一个文件夹
    folderPathStack.value.pop()
    
    if (folderPathStack.value.length > 0) {
      // 导航到上一级文件夹
      const parentFolder = folderPathStack.value[folderPathStack.value.length - 1]
      currentFolder.value = {
        id: parentFolder.id,
        name: parentFolder.name,
        type: 'folder',
        files: []
      }
      
      if (selectedKnowledgeBase.value) {
        pagination.value.currentPage = 1
        pagination.value.pageSize = 10
        await loadKnowledgeFiles(Number(selectedKnowledgeBase.value.id), Number(parentFolder.id), 1, 10)
      }
    } else {
      // 返回根目录
      await backToRoot()
    }
  } else {
    // 如果路径栈为空，返回根目录
    await backToRoot()
  }
}

/**
 * 提取文件名（不含扩展名）
 * @param {string} fileName - 完整文件名（可能包含扩展名）
 * @returns {string} 文件名（不含扩展名）
 */
const getFileNameWithoutExtension = (fileName) => {
  if (!fileName) return ''
  const lastDotIndex = fileName.lastIndexOf('.')
  // 如果没有点，或者点在第一个位置（如 .gitignore），返回原文件名
  if (lastDotIndex <= 0) return fileName
  return fileName.substring(0, lastDotIndex)
}

const renameItem = (item) => {
  editingItem.value = item
  // 🔥 只显示文件名（不含扩展名），方便用户重命名
  renameForm.value = getFileNameWithoutExtension(item.name)
  showRenameDialog.value = true
}

const confirmRename = async () => {
  if (!renameForm.value.trim() || !editingItem.value) {
    ElMessage.warning(t('knowledge.enterNewName'))
    return
  }

  try {
    if (editingItem.value.type === 'file') {
      // 🔥 对于文件，需要将用户输入的文件名重新加上扩展名
      const newFileName = renameForm.value.trim()
      // 从原始文件名中提取扩展名（更可靠）
      const originalExt = getFileExtension(editingItem.value.name) || editingItem.value.ext || ''
      // 如果用户输入的文件名已经包含扩展名，使用用户输入的；否则自动添加原扩展名
      const finalFileName = newFileName.includes('.') 
        ? newFileName 
        : (newFileName + (originalExt ? '.' + originalExt : ''))
      
      // 使用文件关联接口更新文件名
      await updateKnowledgeFileRelation(editingItem.value.id, {
        fileName: finalFileName
      })
      editingItem.value.name = finalFileName
      ElMessage.success(t('knowledge.renameSuccess'))
      
      // 刷新列表（使用当前分页参数）
      if (selectedKnowledgeBase.value) {
        const kbId = Number(selectedKnowledgeBase.value.id)
        const folderId = currentFolder.value ? currentFolder.value.id : null
        await loadKnowledgeFiles(kbId, folderId, pagination.value.currentPage, pagination.value.pageSize)
      }
    } else {
      // 重命名文件夹
      try {
        const finalFolderName = renameForm.value.trim()
        await updateKnowledgeFolder(editingItem.value.id, {
          folderName: finalFolderName
        })
        editingItem.value.name = finalFolderName
        ElMessage.success(t('knowledge.folderRenameSuccess'))
        
        // 刷新列表
        if (selectedKnowledgeBase.value) {
          const kbId = Number(selectedKnowledgeBase.value.id)
          const folderId = currentFolder.value ? currentFolder.value.id : null
          await loadKnowledgeFiles(kbId, folderId, pagination.value.currentPage, pagination.value.pageSize)
        }
      } catch (error) {
        logger.error('重命名文件夹失败', error)
        const errorCode = error.response?.data?.code || error.code
        if (errorCode === 6006) {
          ElMessage.error(t('knowledge.folderNameExists'))
        } else {
          ElMessage.error(error.response?.data?.message || error.message || t('knowledge.folderRenameFailed'))
        }
        throw error // 重新抛出错误，阻止关闭对话框
      }
    }
    showRenameDialog.value = false
    editingItem.value = null
    renameForm.value = ''
  } catch (error) {
    logger.error('重命名失败', error)
    ElMessage.error(error.message || t('knowledge.renameFailed'))
  }
}

const cancelRename = () => {
  showRenameDialog.value = false
  editingItem.value = null
  renameForm.value = ''
}

const deleteItem = (item) => {
  deletingItem.value = item
  deletingKnowledgeBase.value = null
  showDeleteDialog.value = true
}

const cancelDelete = () => {
  showDeleteDialog.value = false
  deletingItem.value = null
  deletingKnowledgeBase.value = null
}

/** 是否为项目知识库（kbType 或 projectId 判断，兼容老数据） */
const isProjectKb = (kb) => {
  if (!kb) return false
  if (kb.kbType === 'project') return true
  return kb.projectId != null && kb.projectId !== ''
}

/** 是否显示文件/文件夹的编辑、删除按钮：仅当知识库 canEdit 为 true 时显示（个人：创建人/管理员；共享后他人仅查看；项目：创建人/负责人/管理员，项目成员仅查看） */
const canShowFileDelete = (kb) => kb?.canEdit === true

/** 是否为当前用户创建（用于显示编辑/权限/删除入口） */
const isKbOwner = (kb) => {
  if (!kb || !authStore.userInfo?.id) return false
  return Number(kb.ownerId) === Number(authStore.userInfo.id)
}

/** 是否为 admin 角色（admin 可修改/权限管理/删除个人知识库，可修改项目知识库） */
const isAdmin = computed(() => {
  const roles = authStore.roles
  return Array.isArray(roles) && roles.includes('admin')
})

/** 是否可操作该知识库（资料修改、权限管理、删除知识库等）：由后端 canEdit 控制，创建人/管理员/项目负责人可操作，共享用户与项目成员仅查看 */
const canOperateKb = (kb) => kb?.canEdit === true

/** 更多菜单命令：edit=资料修改，permission=权限管理，delete=删除 */
const handleKbMenuCommand = (cmd, kb) => {
  if (cmd === 'edit') {
    selectKnowledgeBase(kb)
    nextTick(() => openSettingsDialog())
  } else if (cmd === 'permission') {
    openPermissionDialog(kb)
  } else if (cmd === 'delete') {
    deleteKnowledgeBase(kb)
  }
}

/** 打开权限设置弹窗（公开可见 / 转为私密） */
const openPermissionDialog = (kb) => {
  if (!kb || isProjectKb(kb)) return
  permissionKb.value = kb
  permissionRadio.value = kb.isShared === 1 ? 1 : 0
  showPermissionDialog.value = true
}

const closePermissionDialog = () => {
  showPermissionDialog.value = false
  permissionKb.value = null
  permissionRadio.value = 0
}

/** 确认权限设置：调用更新接口提交 isShared */
const confirmPermissionDialog = async () => {
  const kb = permissionKb.value
  if (!kb) return
  permissionSaving.value = true
  try {
    await updateKnowledge(kb.id, { isShared: permissionRadio.value })
    // 更新本地列表与选中项
    const item = knowledgeBases.value.find((k) => k.id === kb.id)
    if (item) item.isShared = permissionRadio.value
    if (selectedKnowledgeBase.value?.id === kb.id) {
      selectedKnowledgeBase.value = { ...selectedKnowledgeBase.value, isShared: permissionRadio.value }
    }
    ElMessage.success(t('knowledge.knowledgeUpdated'))
    closePermissionDialog()
  } catch (error) {
    logger.error('更新知识库权限失败', error)
    ElMessage.error(error.response?.data?.message ?? error.message ?? t('knowledge.renameFailed'))
  } finally {
    permissionSaving.value = false
  }
}

/** 切换知识库类型 Tab */
const switchKbType = (type) => {
  if (activeKbType.value === type) return
  activeKbType.value = type
  loadKnowledgeBases(false)
}

/**
 * 删除知识库
 * @param {Object} kb - 知识库对象
 */
const deleteKnowledgeBase = (kb) => {
  // 关联项目的知识库不允许删除（projectId 存在或有 kbType=project）
  if (isProjectKb(kb)) {
    ElMessage.warning(t('knowledge.deleteKbForbiddenByProject', { name: kb.name || '' }))
    return
  }
  deletingKnowledgeBase.value = kb
  showDeleteDialog.value = true
}

/**
 * 确认删除（知识库或文件）
 */
const confirmDelete = async () => {
  // 删除知识库
  if (deletingKnowledgeBase.value) {
    try {
      await deleteKnowledge(deletingKnowledgeBase.value.id)
      
      // 从列表中移除
      const idx = knowledgeBases.value.findIndex(
        (kb) => kb.id === deletingKnowledgeBase.value.id
      )
      if (idx > -1) {
        knowledgeBases.value.splice(idx, 1)
      }
      
      // 如果删除的是当前选中的知识库
      if (selectedKnowledgeBase.value && selectedKnowledgeBase.value.id === deletingKnowledgeBase.value.id) {
        // 🔥 如果还有其他知识库，自动选中第一个
        if (knowledgeBases.value.length > 0) {
          nextTick(() => {
            selectKnowledgeBase(knowledgeBases.value[0])
          })
        } else {
          // 只有当所有知识库都删除后，才清空选中状态
          selectedKnowledgeBase.value = null
          kbItems.value = []
          currentFolder.value = null
          kbContents.value = {}
        }
      }
      
      // 从缓存中移除
      delete kbContents.value[deletingKnowledgeBase.value.id]
      
      ElMessage.success(t('knowledge.knowledgeDeleted'))
      logger.info('删除知识库成功', { id: deletingKnowledgeBase.value.id })
    } catch (error) {
      logger.error('删除知识库失败', error)
      const msg = error.response?.data?.message ?? error.message ?? t('knowledge.deleteFailed')
      ElMessage.error(msg)
    }
    showDeleteDialog.value = false
    deletingKnowledgeBase.value = null
    return
  }
  
  // 删除文件（原有逻辑）
  if (!deletingItem.value) return

  try {
    if (deletingItem.value.type === 'file') {
      // 使用文件关联接口删除文件
      await deleteKnowledgeFileRelation(deletingItem.value.id)
      
      // 从列表中移除
      const targetList = currentFolder.value
        ? currentFolder.value.files
        : kbItems.value
      const idx = targetList.findIndex(
        (it) => it.id === deletingItem.value.id
      )
      if (idx > -1) {
        targetList.splice(idx, 1)
      }
      
      // 如果是在文件夹内，也需要更新根目录的缓存
      if (currentFolder.value && selectedKnowledgeBase.value) {
        const rootItems = kbContents.value[selectedKnowledgeBase.value.id] || []
        const rootIdx = rootItems.findIndex(
          (it) => it.id === deletingItem.value.id
        )
        if (rootIdx > -1) {
          rootItems.splice(rootIdx, 1)
        }
      }
      
      ElMessage.success(t('knowledge.deleted'))
      
      // 刷新列表（使用当前分页参数）
      if (selectedKnowledgeBase.value) {
        const kbId = Number(selectedKnowledgeBase.value.id)
        const folderId = currentFolder.value ? currentFolder.value.id : null
        await loadKnowledgeFiles(kbId, folderId, pagination.value.currentPage, pagination.value.pageSize)
      }
    } else {
      // 删除文件夹
      try {
        await deleteKnowledgeFolder(deletingItem.value.id)
        ElMessage.success(t('knowledge.folderDeleteSuccess'))
        
        // 刷新列表
        if (selectedKnowledgeBase.value) {
          const kbId = Number(selectedKnowledgeBase.value.id)
          const folderId = currentFolder.value ? currentFolder.value.id : null
          await loadKnowledgeFiles(kbId, folderId, pagination.value.currentPage, pagination.value.pageSize)
        }
      } catch (error) {
        logger.error('删除文件夹失败', error)
        const errorCode = error.response?.data?.code || error.code
        if (errorCode === 6007) {
          ElMessage.error(t('knowledge.folderHasSubfolders'))
        } else if (errorCode === 6008) {
          ElMessage.error(t('knowledge.folderHasFiles'))
        } else {
          ElMessage.error(error.response?.data?.message || error.message || t('knowledge.folderDeleteFailed'))
        }
      }
    }
    showDeleteDialog.value = false
    deletingItem.value = null
  } catch (error) {
    logger.error('删除失败', error)
    ElMessage.error(error.message || t('knowledge.deleteFailed'))
  }
}

const getCurrentFileCount = () => {
  if (!selectedKnowledgeBase.value) return 0
  const currentItems = kbContents.value[selectedKnowledgeBase.value.id]
  return currentItems ? currentItems.length : 0
}


onMounted(() => {
  loadKnowledgeBases()
})
</script>

<style lang="scss" scoped>
// 知识库页面主容器 - 完全按照原型图实现：100vh 固定高度，内部模块独立滚动
.knowledge-base-container {
  display: flex;
  min-height: 0;
  height: 100vh; // 原型图使用 100vh
  width: 100%;
  max-width: 100vw;
  background: var(--bg);
  overflow: hidden; // 避免横向滚动条
  position: relative; // 原型图使用 relative
}

/* 左侧导航栏 - 共享知识库列表 - 完全按照原型图实现 */
/* 🔥 保持固定宽度，不受自适应布局影响 */
.kb-list-sidebar {
  width: 280px;
  min-width: 280px; // 🔥 固定最小宽度，防止被压缩
  max-width: 280px; // 🔥 固定最大宽度，防止被拉伸
  flex-shrink: 0; // 🔥 不允许收缩，保持固定宽度
  background: var(--surface);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  overflow: hidden; // 原型图：容器本身不滚动
}

.kb-list-header {
  padding: 14px 20px 14px 12px; // 对齐原型图
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0; // 防止被压缩
}

.kb-header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-primary);
}

.kb-header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

// 🔥 统一"+"按钮样式：与卡片预览按钮保持一致的主题和悬浮样式（灰色）
.kb-header-actions .action-btn {
  min-width: 32px;
  height: 32px;
  padding: 4px;
  border: 1px solid transparent;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  background: none;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  
  .el-icon {
    font-size: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  // 🔥 改为 btn-info（灰色），与卡片预览按钮一致
  &.btn-info {
    color: var(--text-3);
    border-color: var(--text-3);
    
    &:hover:not(:disabled) {
      background: var(--text-3);
      color: var(--surface);
    }
  }
}

/* 知识库类型 Tab：全部 / 个人知识库 / 项目知识库 */
.kb-type-tabs {
  display: flex;
  gap: 4px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}
.kb-type-tab {
  flex: 1;
  padding: 6px 8px;
  font-size: 12px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--surface);
  color: var(--text-2);
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.kb-type-tab:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
}
.kb-type-tab.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

// .kb-list-sidebar:not(.is-open) .kb-toggle-text {
//   display: none;
// }
//
// .kb-list-sidebar:not(.is-open) .kb-toggle-button {
//   justify-content: center;
//   padding: 10px;
// }
//
// .kb-list-sidebar:not(.is-open) .kb-toggle-arrow {
//   transform: rotate(-90deg);
// }
// Legacy sidebar collapse selectors removed

.kb-section {
  margin-bottom: 24px;
  margin-top: 16px;
}

.kb-section-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-3);
  margin-bottom: 12px;
  margin-left: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.kb-list {
  flex: 1;
  min-width: 0; // 防止 flex 子元素溢出
  width: 100%; // 保持宽度不变
  box-sizing: border-box; // 包含 padding
  // 防止容器大小变化导致字体变形
  min-height: 0; // 允许 flex 子元素收缩
  max-width: 100%; // 限制最大宽度
}

// 滚动条组件容器
.kb-list-scroll {
  height: 100%;
  padding: 0 20px;
  box-sizing: border-box;
}

// .kb-collapse-enter-active,
// .kb-collapse-leave-active {
//   transition: opacity 0.2s ease, transform 0.2s ease;
// }

// .kb-collapse-enter-from,
// .kb-collapse-leave-to {
//   opacity: 0;
//   transform: translateY(-8px);
// }
// Transition helpers for the old sidebar collapse removed

.kb-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  margin-bottom: 8px;
  background: var(--surface);
  transition: all 0.2s ease;
  width: 100%; // 占满父容器宽度
  box-sizing: border-box; // 包含 padding 和 border
  min-width: 0; // 允许 flex 子元素收缩
  max-width: 100%; // 限制最大宽度，防止溢出
  // 防止字体变形
  font-size: 14px; // 固定字体大小
  line-height: 1.5; // 固定行高
  // 防止缩放时布局变形
  transform: scale(1); // 确保缩放基准
  transform-origin: left center; // 设置缩放原点

  &:hover {
    background: var(--hover-light);
    border-color: var(--border-hover);
  }

  &.active {
    background: var(--hover);
    border-color: var(--color-primary);
  }
}

.kb-item-icon {
  width: 32px;
  height: 32px;
  min-width: 32px; // 固定最小宽度，防止缩放时变小
  min-height: 32px; // 固定最小高度，防止缩放时变小
  max-width: 32px; // 固定最大宽度，防止缩放时变大
  max-height: 32px; // 固定最大高度，防止缩放时变大
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0; // 防止图标被压缩
  box-sizing: border-box; // 包含边框

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block; // 防止图片下方出现空隙
  }
}

// 🔥 现代化透明 logo：知识库列表默认图标
.kb-item-icon-default {
  width: 100%;
  height: 100%;
  min-width: 32px; // 固定最小宽度
  min-height: 32px; // 固定最小高度
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  // 🔥 现代化透明渐变背景：从蓝色到紫色的渐变，带透明度
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
  // 🔥 半透明边框，增加层次感
  border: 1px solid rgba(59, 130, 246, 0.2);
  // 🔥 图标颜色：使用主题蓝色，带透明度
  color: rgba(59, 130, 246, 0.8);
  // 固定图标大小，不受缩放影响
  font-size: 18px;
  line-height: 1;
  // 🔥 添加微妙的阴影，增加立体感
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
  transition: all 0.3s ease;

  :deep(.el-icon) {
    font-size: 18px;
    width: 18px;
    height: 18px;
    flex-shrink: 0;
  }

  // 🔥 悬浮时增强效果
  .kb-item:hover & {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.25) 0%, rgba(139, 92, 246, 0.25) 100%);
    border-color: rgba(59, 130, 246, 0.4);
    color: rgba(59, 130, 246, 1);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
    transform: translateY(-1px);
  }
}

.kb-item-info {
  flex: 1;
  min-width: 0; // 允许 flex 子元素收缩
  overflow: hidden; // 防止内容溢出
}

.kb-item-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  overflow: hidden; // 防止文本溢出
  white-space: nowrap;
  min-width: 0;
  flex-shrink: 1;
  max-width: 100%;
  display: block;
}
.kb-item-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}
.kb-type-tag {
  display: inline-block;
  font-size: 11px;
  padding: 0 6px;
  border-radius: 6px;
  line-height: 1.4;
}
.kb-type-tag.tag-personal {
  background: rgba(59, 130, 246, 0.12);
  color: var(--color-primary);
}
.kb-type-tag.tag-project {
  background: rgba(30, 94, 58, 0.15);
  color: #166534;
}
.kb-type-tag.tag-shared {
  background: rgba(30, 58, 138, 0.12);
  color: var(--color-primary);
}

.kb-item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.kb-item:hover .kb-item-actions {
  opacity: 1;
}

// 删除按钮禁用态（项目知识库不可删除）
.kb-action-icon:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

// 🔥 统一删除按钮样式：与卡片删除按钮保持一致的主题和悬浮样式
.kb-action-icon {
  width: 32px;
  height: 32px;
  min-width: 32px;
  min-height: 32px;
  padding: 4px;
  border: 1px solid transparent;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  background: none;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  flex-shrink: 0; // 防止按钮被压缩
  
  .el-icon {
    font-size: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  // 🔥 删除按钮主题：与卡片删除按钮一致（红色）
  &.btn-danger {
    color: #ef4444;
    border-color: #ef4444;
    
    &:hover:not(:disabled) {
      background: #ef4444;
      color: var(--surface);
    }
  }
}

.kb-empty-tip {
  padding: 20px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}

// 🔥 显示更多按钮样式
.kb-load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  margin: 8px 0;
  border: 1px solid var(--text-3);
  border-radius: 8px;
  background: var(--surface);
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-3);
  font-size: 14px;
  
  // 🔥 与"新建共享知识库"按钮的hover样式保持一致
  &:hover {
    background: var(--text-3);
    color: var(--surface);
  }
  
  &:active {
    opacity: 0.8;
  }
}

.kb-load-more-icon {
  font-size: 16px;
  transition: transform 0.2s ease;
}

.kb-load-more:hover .kb-load-more-icon {
  transform: translateY(2px);
}

.kb-load-more-text {
  font-weight: 500;
}

/* 中间内容区域 - 知识库详情 - 完全按照原型图实现 */
.main-content {
  // 使用 flex:1 自动吃满剩余空间，TOC 布局/响应式更稳定
  flex: 1 1 auto;
  min-width: 0; // 允许内容区收缩，避免横向溢出
  background: var(--surface);
  display: flex;
  flex-direction: column;
}

.content-header {
  padding: 24px;
  border-bottom: 1px solid var(--border);
  // 🔥 背景颜色与知识库列表选中背景保持一致
  background: var(--hover);
  flex-shrink: 0; // 防止被压缩
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: var(--hover-light);
  }
}

.content-title {
  // 🔥 移除标题的字体样式，由子元素控制
  margin-bottom: 8px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex: 1;
}

// 🔥 按照页面修改.md规范：标题文本样式（info-label）
.content-title-text {
  font-size: 14px; // 按照规范：14px
  font-weight: 600; // 按照规范：加粗
  color: var(--text-2); // 按照规范：var(--text-2) 或 #4b5563
  margin-bottom: 8px;
}

// 🔥 现代化透明 logo：知识库详情默认图标（增大尺寸）
.content-icon {
  width: 72px;
  height: 72px;
  // 🔥 现代化透明渐变背景：从蓝色到紫色的渐变，带透明度
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
  // 🔥 半透明边框，增加层次感
  cursor: pointer; // 🔥 添加鼠标指针样式，表示可点击
  position: relative; // 🔥 为悬浮效果做准备
  
  .content-icon-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 12px;
  }
  
  &:hover {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.25) 0%, rgba(139, 92, 246, 0.25) 100%);
  }
  border: 1.5px solid rgba(59, 130, 246, 0.25);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  // 🔥 图标颜色：使用主题蓝色，带透明度
  color: rgba(59, 130, 246, 0.9);
  font-size: 36px;
  // 🔥 添加微妙的阴影，增加立体感
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  transition: all 0.3s ease;
  flex-shrink: 0;

  &:hover {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.25) 0%, rgba(139, 92, 246, 0.25) 100%);
    border-color: rgba(59, 130, 246, 0.4);
    color: rgba(59, 130, 246, 1);
    box-shadow: 0 6px 16px rgba(59, 130, 246, 0.25);
    transform: translateY(-2px);
  }
}

// 🔥 按照页面修改.md规范：元信息样式（info-value）
.content-meta {
  font-size: 14px; // 按照规范：14px（与 el-input placeholder 一致）
  color: var(--text-3); // 按照规范：var(--text-3) 或 #6b7280
  font-weight: 400; // 按照规范：normal
  margin-bottom: 8px;
  
  .user-info-wrapper {
    display: flex;
    align-items: center;
  }
}

// 🔥 按照页面修改.md规范：描述样式（info-value）
.content-description {
  font-size: 14px; // 按照规范：14px（与 el-input placeholder 一致）
  color: var(--text-3); // 按照规范：var(--text-3) 或 #6b7280
  font-weight: 400; // 按照规范：normal
  margin-top: 4px;
}

.content-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto; // 原型图：文件内容过多时滚动
}

.content-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}

.kb-breadcrumb {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text);
  
  // 当有文件正在拖拽时，增强面包屑的可见性
  &.dragging-active {
    .kb-crumb-link {
      position: relative;
      
      // 添加一个微妙的背景提示，表明可以拖拽到这里 - 使用主题色
      &.can-drop {
        background: rgba(var(--color-primary-rgb, 64, 158, 255), 0.05);
        border: 1px dashed rgba(var(--color-primary-rgb, 64, 158, 255), 0.3);
        border-radius: 4px;
        padding: 2px 6px;
        animation: pulse-hint 2s ease-in-out infinite;
      }
    }
  }
}

.kb-crumb-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  position: relative;
  color: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid transparent;
  
  // 拖拽悬停时的面包屑样式 - 增强视觉效果，使用主题色
  &.drag-over {
    background: linear-gradient(135deg, rgba(var(--color-primary-rgb, 64, 158, 255), 0.15) 0%, rgba(var(--color-primary-rgb, 64, 158, 255), 0.25) 100%);
    border: 2px solid var(--color-primary);
    border-radius: 6px;
    padding: 4px 10px;
    box-shadow: 0 2px 8px rgba(var(--color-primary-rgb, 64, 158, 255), 0.3);
    transform: scale(1.05);
    color: var(--color-primary);
    font-weight: 600;
    animation: drag-pulse 0.6s ease-in-out infinite;
    
    .drag-hint-icon {
      color: var(--color-primary);
      animation: bounce-up 0.6s ease-in-out infinite;
    }
  }
  
  // 拖拽提示图标样式
  .drag-hint-icon {
    font-size: 12px;
    color: rgba(64, 158, 255, 0.6);
    transition: all 0.2s ease;
  }

  &:hover {
    color: var(--color-primary, #3b82f6);
    text-decoration: underline;
    
    .drag-hint-icon {
      color: var(--color-primary, #3b82f6);
    }
  }
  
  &.kb-crumb-current {
    color: var(--color-primary, #3b82f6);
    font-weight: 500;
    cursor: default;
    
    &:hover {
      text-decoration: none;
    }
  }
}

// 拖拽时的动画效果
@keyframes drag-pulse {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  }
  50% {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.5);
  }
}

@keyframes bounce-up {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

@keyframes pulse-hint {
  0%, 100% {
    border-color: rgba(64, 158, 255, 0.3);
    background: rgba(64, 158, 255, 0.05);
  }
  50% {
    border-color: rgba(64, 158, 255, 0.5);
    background: rgba(64, 158, 255, 0.1);
  }
}

.section-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
}

// 🔥 统一顶部按钮样式：与卡片按钮保持一致的主题和悬浮样式
.section-actions .action-btn {
  min-width: 32px;
  height: 32px;
  padding: 4px;
  border: 1px solid transparent;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  background: none;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  
  .el-icon {
    font-size: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  &.btn-primary {
    color: var(--surface);
    border-color: var(--color-primary);
    background: var(--color-primary);
    
    &:hover:not(:disabled) {
      background: var(--color-primary);
      color: var(--surface);
      opacity: 0.9;
    }
  }
  
  // 🔥 批量移动文件按钮：未选中时使用 btn-info 样式，保持与其他按钮一致的边框
  // 移除之前的通用悬浮样式，因为现在批量移动按钮使用 btn-info 类
  
  &.btn-info {
    color: var(--text-3);
    border-color: var(--text-3);
    
    &:hover:not(:disabled) {
      background: var(--text-3);
      color: var(--surface);
    }
  }
  
  &.btn-warning {
    color: #f59e0b;
    border-color: #f59e0b;
    
    &:hover:not(:disabled) {
      background: #f59e0b;
      color: var(--surface);
    }
  }
  
  &.btn-success {
    color: #16a34a;
    border-color: #16a34a;
    
    &:hover:not(:disabled) {
      background: #16a34a;
      color: var(--surface);
    }
  }
}

// 🔥 保留旧的 action-icon 样式（用于其他地方）
.action-icon {
  width: 32px;
  height: 32px;
  min-width: 32px; // 固定最小宽度，防止被压缩
  min-height: 32px; // 固定最小高度，防止被压缩
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #374151;
  transition: all 0.2s ease;
  flex-shrink: 0; // 防止按钮被压缩
  // 确保图标大小固定，不受字体影响
  font-size: 16px;
  line-height: 1;

  :deep(.el-icon) {
    font-size: 16px;
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: var(--hover);
    color: var(--color-primary);
    border-color: var(--color-primary);
    box-shadow: 0 2px 6px rgba(30, 58, 138, 0.15);
  }
}

// 🔥 卡片视图：参考报告列表页面的卡片网格布局（自适应）
.content-card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--gap-lg);
  margin-bottom: var(--gap-lg);
  
  @media (max-width: 1400px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

// 🔥 卡片项样式：参考报告列表的 .report-card
.content-card-item {
  background: var(--surface);
  border-radius: 12px;
  padding: var(--gap-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
    border-color: var(--border-hover);
  }

  // 拖拽相关样式
  &[draggable="true"] {
    cursor: move;
    user-select: none;
    
    &:hover {
      opacity: 0.8;
    }
  }
  
  // 拖拽中的文件卡片
  &.dragging {
    opacity: 0.5;
  }
  
  // 选中状态样式
  &.selected {
    border: 2px solid var(--color-primary);
    background: rgba(var(--color-primary-rgb, 64, 158, 255), 0.05);
    box-shadow: 0 2px 8px rgba(var(--color-primary-rgb, 64, 158, 255), 0.2);
  }
  
  &.folder-card {
    // 拖拽悬停时的文件夹样式 - 使用主题色
    .card-header.drag-over {
      background: rgba(var(--color-primary-rgb, 64, 158, 255), 0.1);
      border: 2px dashed var(--color-primary);
      border-radius: var(--radius-md, 8px);
      transition: all 0.2s ease;
    }
    cursor: pointer;
  }

  .card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: var(--gap-md);
    position: relative;
    
    // 批量选择模式下的布局调整 - 重新设计
    &.select-mode {
      position: relative;
      
      // 新的布局容器
      .select-mode-layout {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        width: 100%;
        position: relative;
      }
      
      // 复选框放在左上角
      .file-select-checkbox-wrapper {
        position: absolute;
        top: 0;
        left: 0;
        z-index: 10;
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(255, 255, 255, 0.98);
        border-radius: 6px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        cursor: pointer;
        transition: all 0.2s ease;
        border: 1.5px solid var(--color-primary);
        
        &:hover {
          background: rgba(255, 255, 255, 1);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
          transform: scale(1.1);
          border-color: var(--color-primary);
        }
        
        .file-select-checkbox {
          margin: 0;
          
          :deep(.el-checkbox__input) {
            .el-checkbox__inner {
              width: 18px;
              height: 18px;
              border-color: var(--color-primary);
              
              &::after {
                border-color: var(--color-primary);
              }
            }
            
            &.is-checked .el-checkbox__inner {
              background-color: var(--color-primary);
              border-color: var(--color-primary);
            }
          }
        }
      }
      
      // logo和文件类型标签组，放在右侧（保持原有布局，只是整体向右移动）
      .file-icon-group {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 8px;
        margin-left: auto; // 推到右侧
        padding-left: 36px; // 为复选框留出足够空间，避免重叠
        
        .file-type-badge {
          margin: 0; // 移除默认margin
        }
      }
    }

    // 🔥 文件类型图标容器：使用 FileTypeIcon 组件，样式由组件内部控制
    // FileTypeIcon 组件会处理自己的样式，不需要额外的 wrapper 样式
    
    // 🔥 文件夹图标容器样式
    .card-icon-wrapper {
      width: 56px;
      height: 56px;
      background: linear-gradient(135deg, #dbeafe 0%, #e0f2fe 100%);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      .card-icon {
        font-size: 28px;
        color: var(--color-primary);
      }

      &.folder-icon-wrapper {
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
        
        .folder-icon {
          color: #f59e0b;
        }
      }
    }
    
    // 🔥 保留旧的图标样式（已废弃，但保留以防回退）
    .card-icon-wrapper-old {
      width: 56px;
      height: 56px;
      background: linear-gradient(135deg, #dbeafe 0%, #e0f2fe 100%);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      .card-icon {
        font-size: 28px;
        color: var(--color-primary);
      }

      &.folder-icon-wrapper {
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
        
        .folder-icon {
          color: #f59e0b;
        }
      }
    }

    .file-type-badge,
    .folder-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;
      background: #dbeafe;
      color: var(--color-primary);
    }

    .folder-badge {
      background: #fef3c7;
      color: #f59e0b;
    }
  }

  .card-content {
    margin-bottom: var(--gap-md);
    flex: 1;

    .card-title {
      font-size: 15px; // 🔥 从 14px 稍微增大到 15px
      font-weight: 500;
      color: var(--text, #111827); // 🔥 从 text-2 改为 text，稍微加深颜色
      margin: 0;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
      word-break: break-word;
      cursor: pointer; // 🔥 添加指针样式，表示可交互
      transition: all 0.2s ease; // 🔥 添加过渡效果
      
      // 🔥 移除背景填充悬浮效果，改用 BaseTooltip 显示完整名称（与知识库名称的交互主题一致）
      // 悬浮效果由 BaseTooltip 提供，显示深色背景、白色文字的提示框
    }
    
    // 🔥 确保 BaseTooltip 的包装器不显示白色背景
    :deep(.el-tooltip__trigger) {
      display: inline-block;
      width: 100%;
      background: transparent !important;
      border: none !important;
      box-shadow: none !important;
    }
  }

  .card-meta {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: var(--gap-md);
    margin-bottom: var(--gap-md);
    padding: var(--gap-md) 0;
    border-top: 1px solid var(--border-color, #f3f4f6);
    border-bottom: 1px solid var(--border-color, #f3f4f6);

    .meta-item {
      text-align: center;

      .meta-value {
        font-size: 14px;
        font-weight: 400;
        color: var(--text-3, #6b7280);
        margin: 0;
      }

      .meta-label {
        font-size: 12px;
        font-weight: 600;
        color: var(--color-primary);
        margin: 0 0 4px 0;
      }
    }
  }

  .card-actions {
    display: flex;
    gap: 12px;
    padding-top: var(--gap-md);
    border-top: 1px solid var(--border-color, #f3f4f6);
    box-sizing: border-box;
    width: 100%;
    justify-content: space-between;
    align-items: center;
    
    .action-btn {
      flex: 1;
      min-width: 32px;
      height: 32px;
      padding: 4px;
      border: 1px solid transparent;
      border-radius: 4px;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.2s;
      background: none;
      white-space: nowrap;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0;
      
      .el-icon {
        font-size: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      
      &.btn-info {
        color: var(--text-3);
        border-color: var(--text-3);
        
        &:hover:not(:disabled) {
          background: var(--text-3);
          color: var(--surface);
        }
      }
      
      &.btn-warning {
        color: #f59e0b;
        border-color: #f59e0b;
        
        &:hover:not(:disabled) {
          background: #f59e0b;
          color: var(--surface);
        }
      }
      
      &.btn-danger {
        color: #ef4444;
        border-color: #ef4444;
        
        &:hover:not(:disabled) {
          background: #ef4444;
          color: var(--surface);
        }
      }
    }
  }
}

// 🔥 保留旧的列表样式（已废弃，但保留以防回退）
.content-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-item {
  padding: 16px;
  background: var(--hover-light);
  border: 1px solid var(--border);
  border-radius: 8px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  transition: all 0.2s ease;
  align-items: center;

  &:hover {
    background: #f3f4f6;
    border-color: var(--border-hover);
  }
}

.content-item-icon {
  width: 40px;
  height: 40px;
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1e40af;
  font-size: 18px;

  &.folder-icon {
    background: var(--hover);
    border-color: #dbeafe;
    color: #3b82f6;
  }
}

.content-item-info {
  flex: 1;
  min-width: 0; // 允许 flex 子元素收缩
  overflow: hidden; // 防止内容溢出
}

.content-item-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
  min-width: 0;
}

.content-item-meta {
  font-size: 12px;
  color: var(--text-3);
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap; // 防止按钮换行
  justify-content: flex-end;
  flex-shrink: 0; // 防止按钮区域被压缩
  min-width: 0; // 允许收缩，但不会影响按钮大小
}

.content-actions-dropdown {
  display: none;
}

/* 权限设置弹窗样式已移至下方无 scoped 的 style 块（弹窗 teleport 到 body，scoped 选择器匹配不到） */
.permission-radio-group {
  display: flex;
  flex-direction: column;
  gap: 20px; /* 公开可见 与 转为私密 间距充足，不拥挤 */
}
.permission-option {
  display: flex;
  align-items: flex-start;
  width: 100%;
  margin-right: 0;
  padding: 12px 14px;
  border-radius: 8px;
  box-sizing: border-box;

  :deep(.el-radio__label) {
    width: 100%;
    margin-left: 12px;
  }
}
.permission-option-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.permission-option-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
}
.permission-option-desc {
  font-size: 12px;
  color: var(--text-3);
  line-height: 1.5;
}

.content-empty {
  text-align: center;
  padding: 40px 20px;
  color: #9ca3af;
  font-size: 14px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

.search-input-wrap {
  position: relative;
}

.search-left-icon {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.search-dismiss {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #6b7280;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: #374151;
  }
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  color: var(--text);
  outline: none;
  transition: all 0.2s ease;

  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1);
  }
}

.empty-knowledge-base {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  color: var(--text-3);
}

.empty-icon-large {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 2px dashed #0ea5e9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  font-size: 48px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.empty-description {
  font-size: 14px;
  color: var(--text-3);
  margin-bottom: 24px;
  line-height: 1.5;
}

.upload-files-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(14, 165, 233, 0.15);

  &:hover {
    background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
    box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);
    transform: translateY(-2px);
  }
}

.cover-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cover-preview {
  width: 96px;
  height: 96px;
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  border: 2px dashed #0ea5e9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0ea5e9;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &:hover {
    background: linear-gradient(135deg, #bae6fd 0%, #7dd3fc 100%);
    border-color: #0284c7;
  }
}

.cover-edit-icon {
  position: absolute;
  bottom: -4px;
  right: -4px;
  width: 24px;
  height: 24px;
  background: #0ea5e9;
  border: 2px solid white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
}

.cover-upload-text {
  flex: 1;
  font-size: 14px;
  color: var(--text-3);
}

.main-content-fade-enter-active,
.main-content-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.main-content-fade-enter-from,
.main-content-fade-leave-to {
  opacity: 0;
  transform: translateX(16px);
}

@media (max-width: 1400px) {
  .knowledge-base-container {
    flex-direction: column;
    height: 100%; // 保持 100% 高度，不使用 min-height 避免产生滚动条
    width: 100%;
    max-width: none;
    overflow: hidden; // 确保不产生整体滚动条
  }

  // 🔥 修复：在小屏幕下才改变布局，但保持列表宽度固定（或仅在极小屏幕下才改变）
  // 只在极小屏幕（如手机）下才改变列表宽度，中等屏幕保持固定宽度
  .kb-list-sidebar {
    width: 280px; // 🔥 保持固定宽度，不受自适应影响
    min-width: 280px;
    max-width: 280px;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
    flex-shrink: 0; // 防止被压缩
  }

  .kb-list {
    padding: 0 16px 16px;
    max-height: 300px; // 固定最大高度，避免占用过多空间
    overflow-y: auto; // 内容过多时滚动
  }

  .main-content {
    order: 2;
    width: 100%;
    flex: 1 1 auto;
    min-height: 0; // 确保可以正确滚动
    overflow: hidden; // 容器不滚动
  }
}

@media (max-width: 768px) {
  .content-header {
    padding: 16px;
  }

  // 🔥 卡片视图响应式
  .content-card-grid {
    grid-template-columns: 1fr;
    gap: var(--gap-md);
  }

  .content-card-item {
    padding: var(--gap-md);
    
    .card-header .card-icon-wrapper {
      width: 48px;
      height: 48px;
      
      .card-icon {
        font-size: 24px;
      }
    }
    
    .card-actions {
      flex-wrap: wrap;
      gap: 8px;
      
      .action-btn {
        flex: 0 0 auto;
        min-width: 40px;
      }
    }
  }

  // 🔥 保留旧的列表样式响应式（已废弃）
  .content-item {
    padding: 12px 10px;
  }

  .content-item-meta {
    font-size: 11px;
    flex-wrap: wrap;
    gap: 4px;
  }

  .content-item-actions {
    gap: 4px;
  }

  .action-icon {
    width: 28px;
    height: 28px;
  }

  .content-item-actions .action-icon:not(.more-action) {
    display: none;
  }

  .content-item-actions .action-icon.more-action {
    display: inline-flex;
  }
}
</style>

<!-- 权限设置弹窗、更多下拉等渲染在 body，需全局样式（无 scoped） -->
<style lang="scss">
/* 权限设置弹窗：标题与选中圈统一用学术蓝（弹窗 teleport 到 body，此处无 scoped 才能生效） */
.permission-settings-dialog .el-dialog__header .el-dialog__title,
.permission-settings-dialog .el-dialog__title {
  color: var(--color-primary) !important;
  font-size: 16px;
  font-weight: 600;
}
.permission-settings-dialog .el-dialog__header {
  padding: 20px 24px 12px;
}
.permission-settings-dialog .el-dialog__body {
  padding: 20px 24px 32px; /* 内容区底部留白，分隔线由 footer margin-top 拉开 */
}
.permission-settings-dialog .el-dialog__footer {
  margin-top: 24px; /* 分隔线整体下移，不与「知识库仅自己可见」文字交叉 */
  padding: 16px 24px 20px;
}
.permission-settings-dialog .permission-radio-group {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.permission-settings-dialog .permission-option {
  display: flex;
  align-items: flex-start;
  width: 100%;
  margin-right: 0;
  padding: 16px 20px;
  border-radius: 10px;
  box-sizing: border-box;
}
.permission-settings-dialog .permission-option .el-radio__label {
  width: 100%;
  margin-left: 12px;
}
.permission-settings-dialog .permission-option-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.permission-settings-dialog .permission-option-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
}
.permission-settings-dialog .permission-option-desc {
  font-size: 12px;
  color: var(--text-3);
  line-height: 1.5;
}
.permission-settings-dialog .el-radio__input.is-checked .el-radio__inner {
  border-color: var(--color-primary) !important;
  background-color: var(--color-primary) !important;
  box-shadow: none !important;
}
.permission-settings-dialog .el-radio__input .el-radio__inner:hover,
.permission-settings-dialog .el-radio__input.is-checked .el-radio__inner {
  border-color: var(--color-primary) !important;
}
.permission-settings-dialog .el-radio__input.is-checked .el-radio__inner::after {
  background-color: #fff;
}
.permission-settings-dialog .el-radio__input.is-checked + .el-radio__label {
  color: var(--text);
}
.permission-settings-dialog .el-radio__input.is-focus .el-radio__inner {
  box-shadow: 0 0 0 2px var(--color-primary);
}

/* 与历史对话「…」下拉一致：字体、颜色；字号保持与以前一样 14px */
.kb-more-dropdown-popper {
  .el-dropdown-menu {
    padding: 4px 0 !important;
    border-radius: 8px !important;
    min-width: 120px;
  }
  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 0 20px !important;
    height: 36px !important;
    line-height: 36px !important;
    margin: 0 6px !important;
    border-radius: 6px !important;
    font-family: "Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif !important;
    font-size: 14px !important;
    font-weight: 400 !important;
    letter-spacing: 0.01em !important;
    color: var(--text-2) !important;

    .el-icon {
      font-size: 14px !important;
      margin-right: 0 !important;
      color: var(--text-2) !important;
      transition: color 0.2s ease !important;
    }
    span {
      flex: 1;
    }
    &:hover {
      background-color: var(--hover) !important;
      color: var(--color-primary) !important;
      .el-icon {
        color: var(--color-primary) !important;
      }
    }
    &.is-divided {
      border-top: 1px solid var(--border) !important;
      margin-top: 4px !important;
      padding-top: 4px !important;
    }
  }
}
</style>
