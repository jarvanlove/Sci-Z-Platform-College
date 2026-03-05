<!--
  产教研智能体：申报课题匹配团队、团队明细、分发
  设计原则：领导手持一套项目，需查看匹配到的各团队的负责人、项目基本信息、成员履历、荣誉、
  以往项目不同维度指标（效率图表/效率指标），以便决策将项目分发给谁。UI 遵循可访问性、触控目标与加载反馈。
-->
<template>
  <div class="industry-education-page">
    <div class="page-header">
      <BackButton :tooltip="$t('practice.backToPractice')" @click="handleBack" />
      <div class="header-text">
        <h1 class="page-title">{{ $t('practice.industryEducation.pageTitle') }}</h1>
        <p class="page-subtitle">{{ $t('practice.industryEducation.pageSubtitle') }}</p>
      </div>
    </div>

    <div class="input-section">
      <el-input
        id="ie-keyword"
        v-model="keyword"
        type="textarea"
        :placeholder="$t('practice.industryEducation.keywordPlaceholder')"
        :rows="3"
        maxlength="500"
        show-word-limit
        class="keyword-input"
        :aria-label="$t('practice.industryEducation.keywordPlaceholder')"
      />
      <div class="action-row">
        <el-button
          type="primary"
          :loading="matchLoading"
          :disabled="matchLoading"
          class="match-btn"
          @click="handleMatch"
        >
          {{ $t('practice.industryEducation.matchButton') }}
        </el-button>
      </div>
    </div>

    <div v-if="matchError" class="error-tip" role="alert">
      {{ matchError }}
    </div>

    <div v-if="!matchLoading && teams.length === 0 && hasSearched" class="empty-state">
      <p>{{ $t('practice.industryEducation.noResultHint') }}</p>
    </div>
    <div v-else class="teams-grid">
      <el-card
        v-for="team in teams"
        :key="team.teamId"
        class="team-card"
        shadow="hover"
        @click="openDetail(team.teamId)"
      >
        <div class="team-card-body">
          <div class="team-name-row">
            <h3 class="team-name">{{ team.teamName || '—' }}</h3>
            <el-tag v-if="team.matchType === 'semantic'" type="info" size="small" class="semantic-tag">
              {{ $t('practice.industryEducation.semanticMatch') }}
            </el-tag>
          </div>
          <dl class="team-meta">
            <dt>{{ $t('practice.industryEducation.teamCard.leader') }}</dt>
            <dd>{{ team.leaderName || '—' }}</dd>
            <dt>{{ $t('practice.industryEducation.teamCard.members') }}</dt>
            <dd>{{ team.memberCount }}</dd>
            <dt>{{ $t('practice.industryEducation.teamCard.participantProjects') }}</dt>
            <dd>{{ team.participantProjectCount ?? 0 }}</dd>
            <dt>{{ $t('practice.industryEducation.teamCard.efficiency') }}</dt>
            <dd>{{ team.efficiencyPlaceholder || '—' }}</dd>
          </dl>
          <div class="team-actions">
            <el-button size="default" class="action-btn action-btn-outline" @click.stop="openDetail(team.teamId)">
              {{ $t('practice.industryEducation.teamCard.viewDetail') }}
            </el-button>
            <el-button type="primary" size="default" class="action-btn action-btn-primary" @click.stop="openAssignDialog(team.teamId)">
              {{ $t('practice.industryEducation.teamCard.assign') }}
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 团队项目介绍：抽屉仅保留项目明细（项目名+状态、负责人/成员姓名学院职务、效率图表、荣誉）及底部效率指标 -->
    <!-- 领导视图：抽屉展开占当前屏幕 2/3，主区约 1/3 -->
    <el-drawer
      v-model="drawerVisible"
      direction="rtl"
      size="66%"
      class="detail-drawer drawer-report"
    >
      <template #header>
        <span class="drawer-title-theme">{{ $t('practice.industryEducation.detail.titleTeamProjectIntro') }}</span>
      </template>
      <template v-if="detail">
        <div class="drawer-body">
          <!-- 项目明细（无「项目明细」4字标题）：默认选中第一个项目，展示项目名+状态、负责人/成员、效率图表、荣誉 -->
          <template v-if="drawerProject">
            <!-- 项目名与状态同一行：名称左，状态右，圆角标签（遵循页面修改.md 表单设计） -->
            <section class="detail-card project-detail-inline detail-card-hero drawer-form-by-doc">
              <div class="project-hero-row">
                <h3 class="project-name-hero">{{ drawerProject.projectName || '—' }}</h3>
                <el-tag size="small" type="info" class="status-tag-inline status-tag-by-doc" round>{{ drawerProject.statusDesc || '—' }}</el-tag>
              </div>
            </section>
            <section class="detail-card project-detail-inline drawer-form-by-doc">
              <div class="info-label">{{ $t('practice.industryEducation.detail.projectLeader') }}</div>
              <p class="info-value leader-member-line">
                {{ formatLeaderMember(drawerProject.leader) }}
              </p>
            </section>
            <section class="detail-card project-detail-inline drawer-form-by-doc">
              <div class="info-label">{{ $t('practice.industryEducation.detail.projectMembers') }}</div>
              <ul class="member-list">
                <li v-for="(m, i) in (drawerProject.members || [])" :key="i" class="member-item">
                  <span class="info-value member-name">{{ m.realName || m.userName || '—' }}</span>
                  <span class="info-value member-meta">{{ m.college || '—' }} · {{ m.position || m.role || '—' }}</span>
                </li>
                <li v-if="!drawerProject.members || drawerProject.members.length === 0" class="no-data info-value">{{ $t('common.noData') }}</li>
              </ul>
            </section>
            <!-- 荣誉：展示于效率图表上方；后端暂无荣誉表时占位「—」，后续可接业务表 -->
            <section class="detail-card project-detail-inline drawer-form-by-doc">
              <div class="info-label">{{ $t('practice.industryEducation.projectDetail.honors') }}</div>
              <div v-if="projectHonorsListFromDrawer.length" class="honors-timeline">
                <div v-for="(h, idx) in projectHonorsListFromDrawer" :key="idx" class="honors-item">
                  <span class="honors-dot"></span>
                  <div class="honors-content info-value">
                    <span class="honors-title">{{ h.title || h.name }}</span>
                    <span v-if="h.level" class="honors-level">{{ h.level }}</span>
                    <span v-if="h.date" class="honors-date">{{ h.date }}</span>
                  </div>
                </div>
              </div>
              <p v-else class="info-value detail-intro">{{ drawerProject.honorsPlaceholder ?? '—' }}</p>
            </section>
            <!-- 效率图表：项目维度柱状图 -->
            <section class="detail-card chart-card project-detail-inline drawer-form-by-doc">
              <div class="info-label">{{ $t('practice.industryEducation.projectDetail.efficiencyChart') }}</div>
              <EfficiencyChart :data="projectEfficiencyDataFromDrawer" chart-type="bar" />
            </section>
          </template>
          <p v-else-if="detail.projects && detail.projects.length" class="project-select-hint">
            {{ $t('practice.industryEducation.detail.clickProjectToShowDetail') }}
          </p>
            <!-- 效率指标：综合维度折线图，与上方项目维度柱状图区分，便于领导多维度对比 -->
          <section class="detail-card chart-card drawer-form-by-doc">
            <div class="info-label detail-card-title-with-dot">{{ $t('practice.industryEducation.detail.efficiency') }}</div>
            <EfficiencyChart :data="detailEfficiencyData" chart-type="line" />
          </section>
          <!-- 非责任参与项目：负责人作为成员参与的其他项目汇总，不同图表风格 -->
          <section v-if="nonLeadParticipantChartData" class="detail-card chart-card drawer-form-by-doc chart-non-lead">
            <div class="info-label detail-card-title-with-dot">{{ $t('practice.industryEducation.detail.nonLeadParticipant') }}</div>
            <EfficiencyChart :data="nonLeadParticipantChartData" chart-type="line" class="efficiency-chart-non-lead" />
          </section>
        </div>
        <div class="drawer-footer">
          <el-button type="primary" class="assign-btn" @click="openAssignDialog(detail.teamId)">
            {{ $t('practice.industryEducation.detail.assign') }}
          </el-button>
        </div>
      </template>
      <div v-else-if="detailLoading" class="detail-loading">{{ $t('common.loading') }}</div>
      <div v-else class="detail-loading">{{ $t('common.noData') }}</div>
    </el-drawer>

    <el-dialog
      v-model="assignDialogVisible"
      :title="$t('practice.industryEducation.assign.title')"
      width="720px"
      class="assign-dialog"
      @close="onAssignDialogClose"
    >
      <!-- 分发科研项目：仅调用消息驱动接口 distribute，不创建项目；被分发人接受后按此处表单信息创建申报 -->
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignFormRules" label-width="140px" class="assign-form assign-form-by-doc">
        <div class="form-group">
          <div class="group-title">{{ $t('declaration.basicInfo') }}</div>
          <div class="form-row form-row-leader">
            <el-form-item :label="$t('practice.industryEducation.assign.projectLeaderColon')" class="form-item-readonly">
              <span class="info-value">{{ currentTeamInfo()?.leaderName || '—' }}</span>
            </el-form-item>
            <div class="form-item-empty"></div>
          </div>
          <div class="form-row">
            <el-form-item :label="$t('declaration.department')" prop="department" required>
              <el-select
                v-model="assignForm.department"
                :placeholder="$t('declaration.departmentPlaceholder')"
                style="width: 100%"
                clearable
              >
                <el-option
                  v-for="opt in assignDepartmentOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('declaration.documentPublishTime')" prop="documentPublishTime" required>
              <BaseDatePicker
                v-model="assignForm.documentPublishTime"
                type="date"
                :placeholder="$t('declaration.documentPublishTimePlaceholder')"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item :label="$t('declaration.projectStartTime')" prop="projectStartTime" required>
              <BaseDatePicker
                v-model="assignForm.projectStartTime"
                type="date"
                :placeholder="$t('declaration.projectStartTimePlaceholder')"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item :label="$t('declaration.projectEndTime')" prop="projectEndTime" required>
              <BaseDatePicker
                v-model="assignForm.projectEndTime"
                type="date"
                :placeholder="$t('declaration.projectEndTimePlaceholder')"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>
        </div>

        <div class="form-group">
          <div class="group-title">{{ $t('declaration.researchInfo') }}</div>
          <el-form-item :label="$t('declaration.topic')" prop="researchTopic" class="form-item-full" required>
            <el-input
              v-model="assignForm.researchTopic"
              type="textarea"
              :rows="3"
              :placeholder="$t('declaration.topicPlaceholder')"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item :label="$t('declaration.direction')" prop="researchDirection" class="form-item-full" required>
            <el-input
              v-model="assignForm.researchDirection"
              type="textarea"
              :rows="3"
              :placeholder="$t('declaration.directionPlaceholder')"
            />
          </el-form-item>
          <el-form-item :label="$t('declaration.fields')" prop="researchField" class="form-item-full research-field-item" required>
            <div class="research-field-box">
              <div class="tag-input-container">
                <span
                  v-for="(tag, index) in assignForm.researchField"
                  :key="index"
                  class="tag-item"
                >
                  {{ tag }}
                  <span class="tag-remove" @click="removeAssignTag(index)">×</span>
                </span>
                <input
                  v-model="assignTagInput"
                  class="tag-input"
                  :placeholder="$t('declaration.fieldPlaceholder')"
                  @blur="addAssignTag"
                  @keydown.enter.prevent="addAssignTag"
                  @keydown.backspace="handleAssignBackspace"
                />
              </div>
            </div>
            <p class="tag-hint">{{ $t('declaration.fieldCount', { current: assignForm.researchField.length, max: 10 }) }}</p>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="assignLoading" @click="confirmAssign">
          {{ $t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { BackButton, BaseDatePicker } from '@/components/Common'
import EfficiencyChart from '@/components/Practice/EfficiencyChart.vue'
import { matchTeams as apiMatchTeams, getTeamDetail, distribute as apiDistribute } from '@/api/Practice/industryEducation'
import { DECLARATION_DEPARTMENT_OPTIONS } from '@/utils/constants'

const router = useRouter()
const { t } = useI18n()

function handleBack() {
  router.push('/practice')
}

const keyword = ref('')
const matchLoading = ref(false)
const matchError = ref('')
const hasSearched = ref(false)
const teams = ref([])

const drawerVisible = ref(false)
const detail = ref(null)
const detailLoading = ref(false)
const currentTeamId = ref(null)

/** 当前抽屉内展示的项目（同一抽屉内切换为项目明细视图，null 表示团队明细视图） */
const drawerProject = ref(null)

const assignDialogVisible = ref(false)
const assignLoading = ref(false)
const assignFormRef = ref(null)
const assignDepartmentOptions = DECLARATION_DEPARTMENT_OPTIONS
const assignTagInput = ref('')

const assignForm = reactive({
  department: '',
  documentPublishTime: '',
  projectStartTime: '',
  projectEndTime: '',
  researchTopic: '',
  researchDirection: '',
  researchField: []
})

/** 分发表单校验规则：除项目负责人外均为必填 */
const assignFormRules = {
  department: [{ required: true, message: () => t('declaration.departmentPlaceholder') || '请选择部门', trigger: 'change' }],
  documentPublishTime: [{ required: true, message: () => t('declaration.documentPublishTimePlaceholder') || '请选择发布时间', trigger: 'change' }],
  projectStartTime: [{ required: true, message: () => t('declaration.projectStartTimePlaceholder') || '请选择开始时间', trigger: 'change' }],
  projectEndTime: [{ required: true, message: () => t('declaration.projectEndTimePlaceholder') || '请选择结束时间', trigger: 'change' }],
  researchTopic: [{ required: true, message: () => t('declaration.topicPlaceholder') || '请输入研究课题', trigger: 'blur' }],
  researchDirection: [{ required: true, message: () => t('declaration.directionPlaceholder') || '请输入研究方向', trigger: 'blur' }],
  researchField: [
    { type: 'array', required: true, min: 1, message: () => t('declaration.fieldPlaceholder') || '请至少添加一个研究领域', trigger: 'change' }
  ]
}

async function handleMatch() {
  matchError.value = ''
  matchLoading.value = true
  hasSearched.value = true
  try {
    const res = await apiMatchTeams({
      keyword: keyword.value?.trim() || undefined,
      limit: 6
    })
    teams.value = Array.isArray(res) ? res : (res?.data ?? [])
  } catch (e) {
    matchError.value = e?.message || t('common.error')
    teams.value = []
  } finally {
    matchLoading.value = false
  }
}

function openDetail(teamId) {
  currentTeamId.value = teamId
  detail.value = null
  drawerProject.value = null
  drawerVisible.value = true
  detailLoading.value = true
  getTeamDetail(teamId)
    .then((res) => {
      const data = res?.data ?? res
      detail.value = data
      // 默认选中第一个关联项目，在同一个折叠窗内直接展示其项目明细
      const projects = data?.projects
      drawerProject.value = Array.isArray(projects) && projects.length ? projects[0] : null
    })
    .catch(() => {
      detail.value = null
      drawerProject.value = null
    })
    .finally(() => {
      detailLoading.value = false
    })
}

function openAssignDialog(teamId) {
  currentTeamId.value = teamId
  assignForm.researchTopic = keyword.value?.trim() || ''
  assignForm.researchDirection = ''
  assignForm.researchField = []
  assignForm.department = ''
  assignForm.documentPublishTime = ''
  assignForm.projectStartTime = ''
  assignForm.projectEndTime = ''
  assignTagInput.value = ''
  assignDialogVisible.value = true
}

function onAssignDialogClose() {
  assignTagInput.value = ''
}

function addAssignTag() {
  const raw = (assignTagInput.value || '').trim()
  if (!raw) return
  const parts = raw.split(/[,，、\s]+/).map((s) => s.trim()).filter(Boolean)
  for (const p of parts) {
    if (assignForm.researchField.length >= 10) break
    if (p && !assignForm.researchField.includes(p)) assignForm.researchField.push(p)
  }
  assignTagInput.value = ''
  assignFormRef.value?.validateField('researchField').catch(() => {})
}

function removeAssignTag(index) {
  assignForm.researchField.splice(index, 1)
  assignFormRef.value?.validateField('researchField').catch(() => {})
}

function handleAssignBackspace() {
  if (!assignTagInput.value && assignForm.researchField.length) assignForm.researchField.pop()
}


/** 当前选中团队信息（用于弹窗展示负责人等） */
function currentTeamInfo() {
  const id = currentTeamId.value
  return teams.value.find((t) => t.teamId === id) || null
}

/** 团队明细 - 效率图表数据（后端可返回 { x: string[], y: number[] }，暂无则 null 显示占位图） */
const detailEfficiencyData = computed(() => {
  const d = detail.value
  if (!d || !d.efficiencyChartData) return null
  const { x, y } = d.efficiencyChartData
  return Array.isArray(x) && Array.isArray(y) ? { x, y } : null
})

/** 非责任参与项目汇总图表数据（负责人作为成员参与的其他项目维度聚合） */
const nonLeadParticipantChartData = computed(() => {
  const d = detail.value
  if (!d || !d.nonLeadParticipantChartData) return null
  const { x, y } = d.nonLeadParticipantChartData
  return Array.isArray(x) && Array.isArray(y) ? { x, y } : null
})

/** 团队明细 - 荣誉列表（后端可返回 honors: [{ title, level, date }]，暂无则 []） */
const detailHonorsList = computed(() => {
  const d = detail.value
  if (!d || !d.honors) return []
  return Array.isArray(d.honors) ? d.honors : []
})

/** 抽屉内项目明细 - 效率图表数据 */
const projectEfficiencyDataFromDrawer = computed(() => {
  const p = drawerProject.value
  if (!p || !p.efficiencyChartData) return null
  const { x, y } = p.efficiencyChartData
  return Array.isArray(x) && Array.isArray(y) ? { x, y } : null
})

/** 抽屉内项目明细 - 荣誉列表 */
const projectHonorsListFromDrawer = computed(() => {
  const p = drawerProject.value
  if (!p || !p.honors) return []
  return Array.isArray(p.honors) ? p.honors : []
})

/** 项目负责人单行展示：姓名、学院、职务 */
function formatLeaderMember(leader) {
  if (!leader) return '—'
  const name = leader.realName ?? leader.userName ?? '—'
  const college = leader.college ?? '—'
  const position = leader.position ?? leader.role ?? '—'
  return `${name}　${college}　${position}`
}

/** 确定分发：调用消息驱动接口 distribute（不发 assign），表单信息写入消息；被分发人接受后后端按该信息创建申报 */
async function confirmAssign() {
  if (!assignFormRef.value || !currentTeamId.value) return
  try {
    await assignFormRef.value.validate()
  } catch {
    return
  }
  assignLoading.value = true
  try {
    await apiDistribute({
      topicLabel: assignForm.researchTopic?.trim(),
      targetTeamId: currentTeamId.value,
      department: assignForm.department || undefined,
      documentPublishTime: assignForm.documentPublishTime || undefined,
      projectStartTime: assignForm.projectStartTime || undefined,
      projectEndTime: assignForm.projectEndTime || undefined,
      researchTopic: assignForm.researchTopic?.trim() || undefined,
      researchDirection: assignForm.researchDirection?.trim() || undefined,
      researchFields: assignForm.researchField?.length ? assignForm.researchField : undefined
    })
    ElMessage.success(t('practice.industryEducation.assign.success'))
    assignDialogVisible.value = false
    drawerVisible.value = false
  } catch (e) {
    ElMessage.error(e?.message || t('common.error'))
  } finally {
    assignLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.industry-education-page {
  min-height: 100%;
  padding: 24px 48px 48px;
  background: var(--bg, #f5f7fa);
}

.page-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 24px;
}

.header-text {
  flex: 1;
  min-width: 0;
}

.page-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-1, #1f2937);
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--text-2, #6b7280);
}

.input-section {
  margin-bottom: 20px;
  padding: 20px;
  background: var(--bg-card, #fff);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.keyword-input {
  margin-bottom: 16px;
}

.keyword-input :deep(.el-textarea__inner) {
  min-height: 80px;
  font-size: 16px;
}

.action-row {
  display: flex;
  gap: 12px;
}

.match-btn,
.action-btn,
.assign-btn {
  min-height: 44px;
  min-width: 44px;
}

.error-tip {
  margin-bottom: 16px;
  padding: 12px 16px;
  font-size: 14px;
  color: var(--el-color-danger);
  background: var(--el-color-danger-light-9);
  border-radius: 8px;
}

.empty-state {
  padding: 48px 24px;
  text-align: center;
  color: var(--text-2, #6b7280);
  font-size: 15px;
}

.teams-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.team-card {
  cursor: pointer;
  border-radius: 16px;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.25s ease, border-color 0.2s, transform 0.2s ease;
  overflow: hidden;

  &:hover {
    box-shadow: 0 12px 28px rgba(59, 130, 246, 0.15);
    border-color: rgba(59, 130, 246, 0.35);
    transform: translateY(-2px);
  }
}

.team-card-body {
  padding: 22px;
  background: linear-gradient(165deg, #ffffff 0%, var(--el-fill-color-blank, #f8fafc) 100%);
  border-left: 4px solid transparent;
  transition: border-left-color 0.2s;
}
.team-card:hover .team-card-body {
  border-left-color: var(--el-color-primary);
}

.team-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.team-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-1, #1f2937);
  line-height: 1.35;
}

.semantic-tag {
  flex-shrink: 0;
}

.team-meta {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 6px 14px;
  margin: 0 0 18px;
  font-size: 14px;

  dt {
    color: var(--text-2, #6b7280);
    font-weight: 500;
  }
  dd {
    margin: 0;
    color: var(--text-1, #1f2937);
  }
}

.team-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.team-actions .action-btn {
  border-radius: 10px;
  font-weight: 500;
  padding: 8px 18px;
  transition: all 0.2s ease;
}
.action-btn-outline {
  border-width: 1.5px;
  color: var(--el-color-primary);
}
.action-btn-outline:hover {
  background: rgba(59, 130, 246, 0.08);
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}
.action-btn-primary {
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}
.action-btn-primary:hover {
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
  transform: translateY(-1px);
}

/* 汇报风格抽屉：卡片分区 + 图表 */
.drawer-back-row {
  margin-bottom: 12px;
}

.drawer-report .drawer-body {
  padding: 0 4px 16px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}

.detail-card {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.detail-card-hero {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(59, 130, 246, 0.02) 100%);
  border-color: rgba(59, 130, 246, 0.2);
}

.detail-card-title {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1, #1f2937);
  letter-spacing: 0.02em;
}

/* 效率指标等标题前的淡蓝色圆点 */
.detail-card-title-with-dot {
  display: flex;
  align-items: center;
  gap: 8px;
  &::before {
    content: '';
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: rgba(59, 130, 246, 0.5);
    flex-shrink: 0;
  }
}

/* 团队项目介绍抽屉标题：学术蓝主题色 */
:deep(.detail-drawer .el-drawer__header) {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.drawer-title-theme {
  font-size: 18px;
  font-weight: 600;
  color: #1e3a8a; /* 学术蓝主题色 */
}

.project-hero-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.project-name-hero {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-1, #1f2937);
  line-height: 1.3;
  flex: 1;
  min-width: 0;
}

.status-tag-inline {
  flex-shrink: 0;
  border-radius: 12px;
}

/* 按 doc/页面修改.md：状态标签 12px 圆角、内边距 4px 8px、字体 12px、字重 500 */
.status-tag-by-doc {
  padding: 4px 8px !important;
  font-size: 12px !important;
  font-weight: 500 !important;
}

.status-tag {
  margin-top: 4px;
}

/* 抽屉内表单与 doc/页面修改.md 一致：标签 .info-label、内容 .info-value */
.drawer-form-by-doc {
  .info-label {
    font-size: 14px;
    color: var(--text-2, #4b5563);
    font-weight: 600;
    margin-bottom: var(--gap-xs, 6px);
  }
  .info-value {
    font-size: 14px;
    color: var(--text-3, #6b7280);
    font-weight: 400;
    line-height: 1.6;
    margin: 0;
  }
  .member-item .member-name,
  .member-item .member-meta {
    font-size: 14px;
    color: var(--text-3, #6b7280);
    font-weight: 400;
  }
}

.detail-intro {
  margin: 0;
  font-size: 14px;
  color: var(--text-2, #6b7280);
  line-height: 1.6;
}

.member-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.member-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 14px;

  &:last-child {
    border-bottom: none;
  }
}

.member-name {
  font-weight: 500;
  color: var(--text-1, #1f2937);
}

.member-role {
  color: var(--text-2, #6b7280);
}

.member-meta {
  display: block;
  margin-top: 2px;
  font-size: 13px;
  color: var(--text-2, #6b7280);
}

.leader-member-line {
  white-space: pre-wrap;
}

.member-intro {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--text-2, #6b7280);
  line-height: 1.5;
}

.no-data {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.project-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);

  &:last-of-type {
    border-bottom: none;
  }
}

.project-row-selectable {
  cursor: pointer;
  padding: 12px 10px;
  margin: 0 -10px;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.is-selected {
    background: rgba(59, 130, 246, 0.08);
    border-radius: 8px;
  }
}

.project-row-arrow {
  flex-shrink: 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.project-name {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: var(--text-1, #1f2937);
}

.project-detail-inline {
  border-left: 3px solid var(--el-color-primary-light-5);
}

.project-select-hint {
  margin: 0 0 16px;
  padding: 12px 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.project-detail-btn {
  flex-shrink: 0;
}

.chart-card {
  padding-bottom: 8px;
}

/* 非责任参与项目图表：与责任项目图表区分，使用绿色系边框与背景 */
.chart-non-lead {
  border-left: 3px solid var(--el-color-success);
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.05) 0%, transparent 50%);
}
.chart-non-lead .detail-card-title-with-dot::before {
  background: var(--el-color-success);
}

.honors-timeline {
  padding-left: 4px;
}

.honors-item {
  position: relative;
  padding: 10px 0 10px 18px;
  border-left: 2px solid var(--el-color-primary-light-5);
  margin-left: 6px;

  &:not(:last-child) {
    border-left-color: var(--el-border-color-lighter);
  }
}

.honors-dot {
  position: absolute;
  left: -6px;
  top: 16px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--el-color-primary);
}

.honors-content {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px;
  font-size: 14px;
}

.honors-title {
  font-weight: 500;
  color: var(--text-1, #1f2937);
}

.honors-level,
.honors-date {
  font-size: 12px;
  color: var(--text-2, #6b7280);
}

.drawer-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color);
}

.detail-loading {
  padding: 24px;
  text-align: center;
  color: var(--text-2);
}

.assign-form {
  max-height: 70vh;
  overflow-y: auto;
}

/* 按 doc/页面修改.md：表单标签与内容样式与申报详情一致 */
.assign-form-by-doc {
  :deep(.el-form-item__label) {
    font-size: 14px;
    color: var(--text-2, #4b5563);
    font-weight: 600;
  }
  .info-value {
    font-size: 14px;
    color: var(--text-3, #6b7280);
    font-weight: 400;
  }
}

.assign-form .form-group {
  margin-bottom: 20px;
}

.assign-form .group-title {
  margin-bottom: 12px;
  padding-left: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1, #1f2937);
  border-left: 3px solid var(--el-color-primary);
}

.assign-form .form-row {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  .el-form-item { flex: 1; min-width: 0; }
}

.assign-form .form-row-leader .el-form-item {
  flex: 1;
  min-width: 0;
}

.assign-form .form-item-empty {
  flex: 1;
  min-width: 0;
}

.assign-form .form-item-full .el-form-item__content {
  width: 100%;
}

.assign-form .form-item-readonly .el-form-item__content {
  line-height: 32px;
}

.assign-form .required-mark {
  color: var(--el-color-danger);
}

/* 研究领域：与研究方向、研究课题左对齐，边框与上方 el-input 一致；“已添加 x/10” 放在输入框下方 */
.assign-form .research-field-item {
  .el-form-item__content {
    width: 100%;
    display: block;
    margin-left: 0 !important; /* 与上方 form-item-full 对齐 */
  }
}

/* 研究领域外层框：与研究课题/研究方向 textarea 一致——同色边框、同宽、同圆角，默认灰框 */
.assign-form .research-field-box {
  width: 100%;
  min-height: 80px;
  box-sizing: border-box;
  border: 1px solid var(--el-border-color);
  border-radius: var(--el-border-radius-base, 4px);
  background: var(--el-fill-color-blank);
  transition: border-color 0.2s, box-shadow 0.2s;
}
.assign-form .research-field-box:focus-within {
  border-color: var(--el-border-color-hover);
  box-shadow: 0 0 0 1px var(--el-border-color-hover);
}

.assign-form .tag-input-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 5px 11px;
  min-height: 32px;
  border: none;
  border-radius: 0;
  background: transparent;
  transition: none;
  &:focus-within {
    outline: none;
  }
}

/* “已添加 0/10 个研究领域” 放在研究领域输入框下面，不挤在框内 */
.assign-form .research-field-item .tag-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

/* 研究领域标签：输入值后圆角 + 淡蓝色，与图2要求一致 */
.assign-form .tag-item {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  font-size: 13px;
  color: #1e6fbb;
  background: rgba(30, 111, 187, 0.12);
  border-radius: 12px;
  .tag-remove {
    margin-left: 6px;
    cursor: pointer;
    color: var(--text-2);
    &:hover { color: var(--el-color-danger); }
  }
}

.assign-form .tag-input {
  flex: 1;
  min-width: 80px;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent;
}

.assign-form .tag-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

</style>
