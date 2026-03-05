import request from '@/utils/request'
import { DASHBOARD_API, HTTP_METHODS } from '../Common/constants'

/**
 * 仪表板统计相关 API
 */

// 仪表板趋势（申报 / 项目趋势）
export const getDashboardTrend = () => {
  return request({
    url: DASHBOARD_API.TREND,
    method: HTTP_METHODS.GET
  })
}

// 申报状态分布
export const getDeclarationStatusStats = () => {
  return request({
    url: DASHBOARD_API.DECLARATION_STATUS,
    method: HTTP_METHODS.GET
  })
}

// 项目状态分布
export const getProjectStatusStats = () => {
  return request({
    url: DASHBOARD_API.PROJECT_STATUS,
    method: HTTP_METHODS.GET
  })
}

// 按学院 / 团队数量
export const getByDepartmentStats = () => {
  return request({
    url: DASHBOARD_API.BY_DEPARTMENT,
    method: HTTP_METHODS.GET
  })
}

// 按项目类型分布
export const getByTypeStats = () => {
  return request({
    url: DASHBOARD_API.BY_TYPE,
    method: HTTP_METHODS.GET
  })
}

// 项目延期预警统计
export const getDelayWarningStats = () => {
  return request({
    url: DASHBOARD_API.DELAY_WARNING,
    method: HTTP_METHODS.GET
  })
}

