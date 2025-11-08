import { defineStore } from 'pinia'
import { getIndustryConfig } from '@/api/System'
import { getDepartmentLabels } from '@/api/Auth'
import { createLogger } from '@/utils/simpleLogger'

const industryLogger = createLogger('Industry')
const INDUSTRY_STORAGE_KEY = 'platform_industry'

const DEFAULT_INDUSTRY = {
  code: 'default',
  name: '默认行业'
}

const INDUSTRY_FIELD_MAP = {
  education: {
    labelKey: 'auth.register.departmentLabelEducation',
    placeholderKey: 'auth.register.departmentPlaceholderEducation'
  },
  medical: {
    labelKey: 'auth.register.departmentLabelMedical',
    placeholderKey: 'auth.register.departmentPlaceholderMedical'
  },
  healthcare: {
    labelKey: 'auth.register.departmentLabelMedical',
    placeholderKey: 'auth.register.departmentPlaceholderMedical'
  },
  power: {
    labelKey: 'auth.register.departmentLabelPower',
    placeholderKey: 'auth.register.departmentPlaceholderPower'
  },
  energy: {
    labelKey: 'auth.register.departmentLabelPower',
    placeholderKey: 'auth.register.departmentPlaceholderPower'
  },
  default: {
    labelKey: 'auth.register.departmentLabelDefault',
    placeholderKey: 'auth.register.departmentPlaceholderDefault'
  }
}

const parseIndustry = (raw) => {
  if (!raw) return {}
  try {
    if (typeof raw === 'string') {
      return JSON.parse(raw)
    }
    return raw
  } catch (error) {
    industryLogger.warn('解析行业缓存失败', { error: error.message })
    return {}
  }
}

const normalizeIndustryCode = (code) => {
  if (!code) return ''
  return String(code).trim().toLowerCase()
}

export const useIndustryStore = defineStore('industry', {
  state: () => {
    const cached = parseIndustry(localStorage.getItem(INDUSTRY_STORAGE_KEY))
    return {
      industryCode: normalizeIndustryCode(cached?.industryCode || cached?.code || cached?.type),
      industryName: cached?.industryName || cached?.name || '',
      fetched: !!cached?.industryCode
    }
  },
  getters: {
    fieldConfig(state) {
      const mapKey = state.industryCode && INDUSTRY_FIELD_MAP[state.industryCode]
        ? state.industryCode
        : 'default'
      return INDUSTRY_FIELD_MAP[mapKey]
    },
    departmentLabelKey(state) {
      return this.fieldConfig.labelKey
    },
    departmentPlaceholderKey(state) {
      return this.fieldConfig.placeholderKey
    }
  },
  actions: {
    persist() {
      const payload = {
        industryCode: this.industryCode,
        industryName: this.industryName
      }
      localStorage.setItem(INDUSTRY_STORAGE_KEY, JSON.stringify(payload))
    },
    setIndustry({ code, name }) {
      this.industryCode = normalizeIndustryCode(code)
      this.industryName = name || ''
      this.fetched = true
      this.persist()
      industryLogger.info('行业信息已更新', {
        code: this.industryCode,
        name: this.industryName
      })
    },
    async fetchIndustryConfig(force = false) {
      if (!force && this.industryCode) {
        return this.industryCode
      }
      try {
        industryLogger.info('请求行业配置')
        const response = await getIndustryConfig()
        const data = response?.data?.data || response?.data || {}
        const code = data?.industryCode || data?.industry || data?.code || data?.type
        const name = data?.industryName || data?.industryLabel || data?.name
        if (!code) {
          throw new Error('未返回有效行业编码')
        }
        this.setIndustry({ code, name })
      } catch (error) {
        industryLogger.warn('获取行业配置失败，使用默认配置', { error: error.message })
        this.setIndustry(DEFAULT_INDUSTRY)
      }
      return this.industryCode
    },
    async ensureIndustry() {
      if (!this.industryCode) {
        await this.fetchIndustryConfig()
      }
      return this.industryCode
    },
    async fetchDepartmentLabels(params = {}) {
      const industry = await this.ensureIndustry()
      try {
        const requestParams = { ...params }
        if (industry && industry !== 'default') {
          requestParams.industry = industry
        }
        const response = await getDepartmentLabels(requestParams)
        const payload = response?.data?.data || response?.data || {}
        const list = Array.isArray(payload) ? payload : payload.list || payload.items || payload.records || []
        const normalized = Array.isArray(list) ? list : []
        industryLogger.info('部门标签获取成功', {
          industry,
          count: normalized.length
        })
        return normalized
      } catch (error) {
        industryLogger.error('部门标签获取失败', { error: error.message })
        throw error
      }
    }
  }
})

