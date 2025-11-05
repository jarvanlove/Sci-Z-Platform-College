// 文件处理工具函数

// 格式化文件大小
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 获取文件扩展名
export const getFileExtension = (filename) => {
  return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2)
}

// 检查文件类型
export const checkFileType = (file, allowedTypes) => {
  const fileType = file.type
  const fileName = file.name
  const extension = getFileExtension(fileName)
  
  return allowedTypes.includes(fileType) || allowedTypes.includes(extension)
}

// 检查文件大小
export const checkFileSize = (file, maxSize) => {
  return file.size <= maxSize
}

// 生成文件预览URL
export const createFilePreviewUrl = (file) => {
  return URL.createObjectURL(file)
}

// 下载文件
export const downloadFile = (url, filename) => {
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 图片压缩
export const compressImage = (file, quality = 0.8, maxWidth = 1920) => {
  return new Promise((resolve) => {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()
    
    img.onload = () => {
      const { width, height } = img
      const ratio = Math.min(maxWidth / width, maxWidth / height)
      
      canvas.width = width * ratio
      canvas.height = height * ratio
      
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height)
      
      canvas.toBlob(resolve, file.type, quality)
    }
    
    img.src = URL.createObjectURL(file)
  })
}
