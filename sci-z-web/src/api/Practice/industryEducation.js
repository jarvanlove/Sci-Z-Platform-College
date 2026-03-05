import request from '@/utils/request'
import { INDUSTRY_EDUCATION_API, HTTP_METHODS } from '../Common/constants'

/**
 * 产教研智能体 - 匹配团队
 * @param {Object} params
 * @param {string} [params.keyword] - 申报课题关键词
 * @param {number} [params.limit] - 返回条数，默认 6
 * @returns {Promise<Array>} 团队列表
 */
export const matchTeams = (params) => {
  return request({
    url: INDUSTRY_EDUCATION_API.TEAMS_MATCH,
    method: HTTP_METHODS.POST,
    data: params || {}
  })
}

/**
 * 产教研智能体 - 团队明细
 * @param {number} teamId - 团队ID（projectId）
 * @returns {Promise<Object>} 团队详情
 */
export const getTeamDetail = (teamId) => {
  return request({
    url: INDUSTRY_EDUCATION_API.TEAM_DETAIL(teamId),
    method: HTTP_METHODS.GET
  })
}

/**
 * 产教研智能体 - 分发（旧：指定申报ID，创建项目）
 * @param {Object} data
 * @param {number} data.declarationId - 申报ID
 * @param {number} data.targetTeamId - 目标团队ID（projectId）
 * @returns {Promise<number>} 新创建的项目ID
 */
export const assign = (data) => {
  return request({
    url: INDUSTRY_EDUCATION_API.ASSIGN,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 产教研智能体 - 消息驱动分发（领导填写申报基础信息，发站内消息并 WebSocket 推送给负责人）
 * 调用时机：在「分发科研项目」弹窗中点击「确定」时调用本接口（不调用 assign）。
 * 后端仅发送消息、不创建项目；被分发人接受后，后端根据消息中的表单信息创建申报与项目。
 * @param {Object} data
 * @param {string} data.topicLabel - 科研项目描述/研究课题（必填）
 * @param {number} data.targetTeamId - 目标团队ID（projectId）
 * @param {string} [data.department] - 部门
 * @param {string} [data.documentPublishTime] - 红头文件发布时间 YYYY-MM-DD
 * @param {string} [data.projectStartTime] - 项目开始时间
 * @param {string} [data.projectEndTime] - 项目结束时间
 * @param {string} [data.researchTopic] - 研究课题
 * @param {string} [data.researchDirection] - 研究方向
 * @param {string[]} [data.researchFields] - 研究领域列表
 * @returns {Promise<number>} 站内消息ID
 */
export const distribute = (data) => {
  return request({
    url: INDUSTRY_EDUCATION_API.DISTRIBUTE,
    method: HTTP_METHODS.POST,
    data
  })
}

/**
 * 产教研 - 接受分发（负责人点接受后按下发时的申报基础信息创建申报与项目）
 * @param {number} messageId - 消息ID
 * @returns {Promise<number>} 新创建的项目ID
 */
export const acceptDistribute = (messageId) => {
  return request({
    url: INDUSTRY_EDUCATION_API.DISTRIBUTE_ACCEPT(messageId),
    method: HTTP_METHODS.POST
  })
}

/**
 * 产教研 - 拒绝分发（填写原因，下发领导将收到拒绝原因通知）
 * @param {number} messageId - 消息ID
 * @param {Object} data
 * @param {string} data.reason - 拒绝原因
 */
export const rejectDistribute = (messageId, data) => {
  return request({
    url: INDUSTRY_EDUCATION_API.DISTRIBUTE_REJECT(messageId),
    method: HTTP_METHODS.POST,
    data
  })
}
