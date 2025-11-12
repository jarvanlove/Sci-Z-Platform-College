/**
 * @description 通用文件上传流程封装
 * 提供文件大小/类型校验、MD5 去重、上传与复用逻辑
 */

import { ref } from 'vue'
import { uploadFile, checkFileDuplicate } from '@/api/File'
import { validateFileSize, validateFileType } from '@/constants/attachment'
import SparkMD5 from 'spark-md5'
import { ElMessage } from 'element-plus'

const computeFileMd5 = (file) =>
  new Promise((resolve, reject) => {
    const chunkSize = 2 * 1024 * 1024
    const chunks = Math.ceil(file.size / chunkSize)
    let currentChunk = 0
    const spark = new SparkMD5.ArrayBuffer()
    const reader = new FileReader()

    reader.onload = (event) => {
      const result = event?.target?.result
      if (result) {
        spark.append(result)
      }
      currentChunk += 1
      if (currentChunk < chunks) {
        loadNextChunk()
      } else {
        resolve(spark.end())
      }
    }

    reader.onerror = () => reject(new Error('md5 compute failed'))

    const loadNextChunk = () => {
      const start = currentChunk * chunkSize
      const end = Math.min(start + chunkSize, file.size)
      reader.readAsArrayBuffer(file.slice(start, end))
    }

    loadNextChunk()
  })

const DEFAULT_MAX_SIZE_MB = 150

export const useFileUpload = ({
  maxSizeMB = DEFAULT_MAX_SIZE_MB,
  allowedExtensions,
  getExtraFormData
}) => {
  const uploading = ref(false)

  const checkDuplicate = async (file, md5) => {
    if (!md5) return null
    try {
      const response = await checkFileDuplicate({
        md5,
        fileSize: file.size,
        originalName: file.name
      })
      const payload = response?.data ?? response
      const duplicateData = payload?.data ?? payload
      if (duplicateData?.exists && duplicateData.fileInfo) {
        return duplicateData.fileInfo
      }
    } catch (error) {
      // swallow duplicate check errors, fall back to upload
    }
    return null
  }

  const uploadWithCheck = async (file) => {
    const sizeValidation = validateFileSize(file, maxSizeMB)
    if (!sizeValidation.passed) {
      ElMessage.error(sizeValidation.reason)
      return null
    }
    const typeValidation = validateFileType(file, allowedExtensions)
    if (!typeValidation.passed) {
      ElMessage.error(typeValidation.reason)
      return null
    }

    let md5 = ''
    try {
      md5 = await computeFileMd5(file)
      const duplicateInfo = await checkDuplicate(file, md5)
      if (duplicateInfo) {
        ElMessage.success('文件已存在，已复用历史上传记录')
        return { fileInfo: duplicateInfo, reused: true }
      }
    } catch (error) {
      // md5/duplicate check failure should not block upload
    }

    const form = new FormData()
    form.append('file', file)
    if (md5) {
      form.append('md5', md5)
    }

    const extraData = typeof getExtraFormData === 'function' ? getExtraFormData() : null
    if (extraData && typeof extraData === 'object') {
      Object.entries(extraData).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          form.append(key, value)
        }
      })
    }

    try {
      uploading.value = true
      const response = await uploadFile(form)
      const payload = response?.data ?? response ?? {}
      return { fileInfo: payload, reused: false }
    } finally {
      uploading.value = false
    }
  }

  return {
    uploading,
    uploadWithCheck
  }
}


