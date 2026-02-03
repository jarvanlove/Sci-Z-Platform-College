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

/**
 * 检测文件类型
 * @param {string} fileName - 文件名
 * @param {string} fileUrl - 文件URL（可选）
 * @returns {string} 文件类型：pdf, image, word, word-old, excel, ppt, text, unsupported
 */
const detectFileType = (fileName, fileUrl = '') => {
  if (fileName) {
    const name = fileName.toLowerCase()
    
    if (name.endsWith('.pdf')) return 'pdf'
    
    const imageExts = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg']
    if (imageExts.some(ext => name.endsWith(ext))) return 'image'
    
    if (name.endsWith('.docx')) return 'word'
    if (name.endsWith('.doc')) return 'word-old'
    
    const excelExts = ['.xls', '.xlsx']
    if (excelExts.some(ext => name.endsWith(ext))) return 'excel'
    
    const pptExts = ['.ppt', '.pptx']
    if (pptExts.some(ext => name.endsWith(ext))) return 'ppt'
    
    const textExts = ['.txt', '.md', '.json', '.xml', '.csv']
    if (textExts.some(ext => name.endsWith(ext))) return 'text'
  }
  
  if (fileUrl) {
    const url = fileUrl.toLowerCase()
    const urlMatch = url.match(/\.(pdf|jpg|jpeg|png|gif|bmp|webp|svg|doc|docx|xls|xlsx|ppt|pptx|txt|md|json|xml|csv)(\?|$|#)/i)
    if (urlMatch) {
      const ext = urlMatch[1].toLowerCase()
      if (ext === 'pdf') return 'pdf'
      if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext)) return 'image'
      if (ext === 'docx') return 'word'
      if (ext === 'doc') return 'word-old'
      if (['xls', 'xlsx'].includes(ext)) return 'excel'
      if (['ppt', 'pptx'].includes(ext)) return 'ppt'
      if (['txt', 'md', 'json', 'xml', 'csv'].includes(ext)) return 'text'
    }
  }
  
  return 'unsupported'
}

/**
 * 在新窗口打开文件预览
 * @param {number} attachmentId - 文件附件ID
 * @param {string} fileName - 文件名（可选，用于显示）
 * @param {Object} options - 可选参数
 * @param {number} options.expireSeconds - URL有效期（秒），默认3600
 * @returns {Promise<void>}
 */
export const openFilePreviewInNewWindow = async (attachmentId, fileName = '', options = {}) => {
  if (!attachmentId) {
    throw new Error('文件ID不存在，无法预览')
  }

  try {
    // 动态导入 previewFile API，避免循环依赖
    const { previewFile } = await import('@/api/File/file')
    
    // 调用预览接口获取预签名 URL
    const response = await previewFile(attachmentId, {
      expireSeconds: options.expireSeconds || 3600
    })
    
    if (!response?.data) {
      throw new Error(response?.message || '获取预览链接失败')
    }
    
    const previewUrl = response.data
    const fileType = detectFileType(fileName, previewUrl)
    
    // 根据文件类型选择预览方式
    if (fileType === 'pdf' || fileType === 'image') {
      // PDF、图片文件可以直接在新窗口打开
      window.open(previewUrl, '_blank')
    } else if (fileType === 'text') {
      // 🔥 修复：文本文件需要创建预览页面，正确设置字符编码，避免乱码
      const textPreviewHtml = createTextPreviewPage(previewUrl, fileName)
      const blob = new Blob([textPreviewHtml], { type: 'text/html;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      window.open(url, '_blank')
    } else if (fileType === 'excel') {
      // 🔥 新增：Excel 文件使用 SheetJS 在浏览器中直接预览（支持内网地址）
      const excelPreviewHtml = createExcelPreviewPage(previewUrl, fileName)
      const blob = new Blob([excelPreviewHtml], { type: 'text/html;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      window.open(url, '_blank')
    } else if (fileType === 'ppt') {
      // PPT/PPTX 文件预览
      const isLocalhost = /^(https?:\/\/)?(localhost|127\.0\.0\.1|0\.0\.0\.0|::1)/i.test(previewUrl)
      const isPrivateIP = /^(https?:\/\/)?(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/i.test(previewUrl)
      
      if (isLocalhost || isPrivateIP) {
        // 🔥 内网地址：创建友好的提示页面（PPT 格式复杂，浏览器预览效果有限）
        const pptPreviewHtml = createPptPreviewPage(previewUrl, fileName, false)
        const blob = new Blob([pptPreviewHtml], { type: 'text/html;charset=utf-8' })
        const url = URL.createObjectURL(blob)
        window.open(url, '_blank')
      } else {
        // 公网地址：尝试使用 Office Online Viewer
        const encodedUrl = encodeURIComponent(previewUrl)
        const viewerUrl = `https://view.officeapps.live.com/op/embed.aspx?src=${encodedUrl}`
        window.open(viewerUrl, '_blank')
      }
    } else if (fileType === 'word-old') {
      // Word (.doc) 使用 Office Online Viewer（仅公网地址）
      const isLocalhost = /^(https?:\/\/)?(localhost|127\.0\.0\.1|0\.0\.0\.0|::1)/i.test(previewUrl)
      const isPrivateIP = /^(https?:\/\/)?(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/i.test(previewUrl)
      
      if (isLocalhost || isPrivateIP) {
        // 内网地址无法使用 Office Online Viewer，创建友好的错误提示页面
        const errorHtml = createOfficePreviewErrorPage(fileName, 'Word', previewUrl)
        const blob = new Blob([errorHtml], { type: 'text/html;charset=utf-8' })
        const url = URL.createObjectURL(blob)
        window.open(url, '_blank')
        return
      }
      
      const encodedUrl = encodeURIComponent(previewUrl)
      const viewerUrl = `https://view.officeapps.live.com/op/embed.aspx?src=${encodedUrl}`
      window.open(viewerUrl, '_blank')
    } else if (fileType === 'word') {
      // Word (.docx) 使用 docx-preview，需要创建预览页面
      // 使用 data URL 创建一个包含预览逻辑的 HTML 页面
      const previewHtml = createDocxPreviewPage(previewUrl, fileName)
      const blob = new Blob([previewHtml], { type: 'text/html' })
      const url = URL.createObjectURL(blob)
      window.open(url, '_blank')
      // 注意：URL.createObjectURL 创建的 URL 会在页面关闭后自动释放
    } else {
      // 不支持的类型，尝试直接打开
      window.open(previewUrl, '_blank')
    }
  } catch (error) {
    console.error('打开文件预览失败:', error)
    throw error
  }
}

/**
 * 创建文本文件预览页面 HTML（修复乱码问题）
 * @param {string} fileUrl - 文件URL
 * @param {string} fileName - 文件名
 * @returns {string} HTML 内容
 */
const createTextPreviewPage = (fileUrl, fileName) => {
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${fileName || '文本预览'}</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', 'SimSun', monospace;
      background: #f5f5f5;
      padding: 20px;
    }
    .container {
      max-width: 1400px;
      margin: 0 auto;
      background: white;
      padding: 40px 60px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-radius: 8px;
    }
    .header {
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid #e0e0e0;
    }
    .header h1 {
      font-size: 18px;
      color: #333;
      margin-bottom: 5px;
    }
    .header .file-name {
      font-size: 14px;
      color: #666;
    }
    .loading {
      text-align: center;
      padding: 40px;
      color: #666;
    }
    .error {
      text-align: center;
      padding: 40px;
      color: #f56c6c;
    }
    .text-content {
      background: #fff;
      padding: 20px;
      border: 1px solid #e0e0e0;
      border-radius: 4px;
      overflow-x: auto;
      white-space: pre-wrap;
      word-wrap: break-word;
      font-size: 14px;
      line-height: 1.6;
      color: #333;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    }
    .text-content code {
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>文本文件预览</h1>
      <div class="file-name">文件名: ${fileName || '未知文件'}</div>
    </div>
    <div id="loading" class="loading">正在加载文件内容...</div>
    <div id="error" class="error" style="display: none;"></div>
    <div id="text-content" class="text-content" style="display: none;"></div>
  </div>
  <script>
    (async function() {
      const loadingEl = document.getElementById('loading')
      const errorEl = document.getElementById('error')
      const contentEl = document.getElementById('text-content')
      
      try {
        // 🔥 修复：使用 fetch 获取文件内容，并正确设置字符编码
        const response = await fetch('${fileUrl}')
        if (!response.ok) {
          throw new Error('下载文件失败: ' + response.statusText)
        }
        
        // 🔥 修复：使用 text() 方法并指定 UTF-8 编码，避免乱码
        // 如果服务器返回的 Content-Type 没有指定编码，浏览器可能会使用默认编码
        // 我们通过设置 response.text() 来确保使用 UTF-8 编码
        const text = await response.text()
        
        // 显示内容
        contentEl.textContent = text
        loadingEl.style.display = 'none'
        contentEl.style.display = 'block'
      } catch (error) {
        loadingEl.style.display = 'none'
        errorEl.textContent = '预览失败: ' + error.message
        errorEl.style.display = 'block'
      }
    })()
  </script>
</body>
</html>`
}

/**
 * 创建 Office 文件预览错误提示页面 HTML（内网地址无法使用 Office Online Viewer）
 * @param {string} fileName - 文件名
 * @param {string} fileTypeName - 文件类型名称（Excel/Word/PPT）
 * @param {string} fileUrl - 文件URL（用于下载）
 * @returns {string} HTML 内容
 */
const createOfficePreviewErrorPage = (fileName, fileTypeName, fileUrl) => {
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${fileTypeName} 文件预览</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
      background: #f5f5f5;
      padding: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
    }
    .container {
      max-width: 600px;
      background: white;
      padding: 40px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-radius: 8px;
      text-align: center;
    }
    .icon {
      font-size: 64px;
      color: #f56c6c;
      margin-bottom: 20px;
    }
    h1 {
      font-size: 24px;
      color: #333;
      margin-bottom: 10px;
    }
    .file-name {
      font-size: 14px;
      color: #666;
      margin-bottom: 20px;
      word-break: break-all;
    }
    .message {
      font-size: 16px;
      color: #666;
      line-height: 1.6;
      margin-bottom: 30px;
    }
    .actions {
      display: flex;
      gap: 15px;
      justify-content: center;
    }
    .btn {
      padding: 12px 24px;
      border: none;
      border-radius: 4px;
      font-size: 14px;
      cursor: pointer;
      text-decoration: none;
      display: inline-block;
      transition: all 0.3s;
    }
    .btn-primary {
      background: #409eff;
      color: white;
    }
    .btn-primary:hover {
      background: #66b1ff;
    }
    .btn-secondary {
      background: #f0f0f0;
      color: #333;
    }
    .btn-secondary:hover {
      background: #e0e0e0;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="icon">⚠️</div>
    <h1>无法在线预览</h1>
    <div class="file-name">文件名: ${fileName || '未知文件'}</div>
    <div class="message">
      <p>由于文件地址为本地或内网地址，无法使用 Office Online Viewer 在线预览服务。</p>
      <p style="margin-top: 10px;">请下载文件后使用本地 Office 软件打开查看。</p>
    </div>
    <div class="actions">
      <a href="${fileUrl}" download="${fileName || 'file'}" class="btn btn-primary">下载文件</a>
      <button onclick="window.close()" class="btn btn-secondary">关闭窗口</button>
    </div>
  </div>
</body>
</html>`
}

/**
 * 创建 Excel (.xlsx/.xls) 预览页面 HTML
 * @param {string} fileUrl - 文件URL
 * @param {string} fileName - 文件名
 * @returns {string} HTML 内容
 */
const createExcelPreviewPage = (fileUrl, fileName) => {
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${fileName || 'Excel 预览'}</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
      background: #f5f5f5;
      padding: 20px;
    }
    .container {
      max-width: 1400px;
      margin: 0 auto;
      background: white;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-radius: 8px;
    }
    .header {
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid #e0e0e0;
    }
    .header h1 {
      font-size: 18px;
      color: #333;
      margin-bottom: 5px;
    }
    .header .file-name {
      font-size: 14px;
      color: #666;
    }
    .loading {
      text-align: center;
      padding: 40px;
      color: #666;
    }
    .error {
      text-align: center;
      padding: 40px;
      color: #f56c6c;
    }
    .sheet-tabs {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
      flex-wrap: wrap;
      border-bottom: 2px solid #e0e0e0;
    }
    .sheet-tab {
      padding: 10px 20px;
      background: #f0f0f0;
      border: none;
      border-radius: 4px 4px 0 0;
      cursor: pointer;
      font-size: 14px;
      color: #666;
      transition: all 0.3s;
    }
    .sheet-tab:hover {
      background: #e0e0e0;
    }
    .sheet-tab.active {
      background: #409eff;
      color: white;
    }
    .excel-container {
      overflow-x: auto;
      overflow-y: auto;
      max-height: calc(100vh - 200px);
      border: 1px solid #e0e0e0;
      border-radius: 4px;
    }
    .excel-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 14px;
    }
    .excel-table th,
    .excel-table td {
      border: 1px solid #ddd;
      padding: 8px 12px;
      text-align: left;
      white-space: nowrap;
    }
    .excel-table th {
      background: #f5f5f5;
      font-weight: 600;
      position: sticky;
      top: 0;
      z-index: 10;
    }
    .excel-table tr:nth-child(even) {
      background: #fafafa;
    }
    .excel-table tr:hover {
      background: #f0f7ff;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>Excel 文件预览</h1>
      <div class="file-name">文件名: ${fileName || '未知文件'}</div>
    </div>
    <div id="loading" class="loading">正在加载 Excel 文件...</div>
    <div id="error" class="error" style="display: none;"></div>
    <div id="sheet-tabs" class="sheet-tabs" style="display: none;"></div>
    <div id="excel-container" class="excel-container" style="display: none;"></div>
  </div>
  <!-- 使用 SheetJS 库预览 Excel 文件 -->
  <script src="https://cdn.sheetjs.com/xlsx-0.20.3/package/dist/xlsx.full.min.js"></script>
  <script>
    (async function() {
      const loadingEl = document.getElementById('loading')
      const errorEl = document.getElementById('error')
      const sheetTabsEl = document.getElementById('sheet-tabs')
      const containerEl = document.getElementById('excel-container')
      
      // 等待 SheetJS 加载
      const waitForLibrary = () => {
        return new Promise((resolve, reject) => {
          let attempts = 0
          const maxAttempts = 100
          const checkLibrary = () => {
            attempts++
            if (typeof XLSX !== 'undefined') {
              resolve()
            } else if (attempts >= maxAttempts) {
              reject(new Error('SheetJS 库加载超时，请检查网络连接'))
            } else {
              setTimeout(checkLibrary, 100)
            }
          }
          checkLibrary()
        })
      }
      
      try {
        await waitForLibrary()
        
        // 下载文件
        const response = await fetch('${fileUrl}')
        if (!response.ok) {
          throw new Error('下载文件失败: ' + response.statusText)
        }
        const arrayBuffer = await response.arrayBuffer()
        
        // 读取 Excel 文件
        const workbook = XLSX.read(arrayBuffer, { type: 'array' })
        
        if (workbook.SheetNames.length === 0) {
          throw new Error('Excel 文件中没有工作表')
        }
        
        // 创建工作表标签
        workbook.SheetNames.forEach((sheetName, index) => {
          const tab = document.createElement('button')
          tab.className = 'sheet-tab' + (index === 0 ? ' active' : '')
          tab.textContent = sheetName
          tab.onclick = () => {
            // 切换工作表
            document.querySelectorAll('.sheet-tab').forEach(t => t.classList.remove('active'))
            tab.classList.add('active')
            renderSheet(workbook, sheetName)
          }
          sheetTabsEl.appendChild(tab)
        })
        
        // 渲染第一个工作表
        const renderSheet = (wb, sheetName) => {
          const worksheet = wb.Sheets[sheetName]
          const html = XLSX.utils.sheet_to_html(worksheet, { id: 'excel-table' })
          containerEl.innerHTML = html
          
          // 添加样式类
          const table = containerEl.querySelector('table')
          if (table) {
            table.className = 'excel-table'
          }
        }
        
        renderSheet(workbook, workbook.SheetNames[0])
        
        loadingEl.style.display = 'none'
        sheetTabsEl.style.display = 'flex'
        containerEl.style.display = 'block'
      } catch (error) {
        loadingEl.style.display = 'none'
        errorEl.textContent = '预览失败: ' + error.message
        errorEl.style.display = 'block'
      }
    })()
  </script>
</body>
</html>`
}

/**
 * 创建 PPT/PPTX 预览页面 HTML
 * @param {string} fileUrl - 文件URL
 * @param {string} fileName - 文件名
 * @param {boolean} useOnlineViewer - 是否使用在线预览（已废弃，保留兼容性）
 * @returns {string} HTML 内容
 */
const createPptPreviewPage = (fileUrl, fileName, useOnlineViewer = false) => {
  const isPptx = fileName.toLowerCase().endsWith('.pptx')
  
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${fileName || 'PPT 预览'}</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
      background: #1a1a1a;
      padding: 20px;
      overflow: hidden;
    }
    .container {
      max-width: 1400px;
      margin: 0 auto;
      background: #2a2a2a;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 4px 12px rgba(0,0,0,0.3);
      height: calc(100vh - 40px);
      display: flex;
      flex-direction: column;
    }
    .header {
      padding: 15px 20px;
      background: #333;
      color: white;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .header h1 {
      font-size: 16px;
      font-weight: 500;
    }
    .header .file-name {
      font-size: 12px;
      color: #aaa;
    }
    .loading {
      text-align: center;
      padding: 40px;
      color: #aaa;
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .error {
      text-align: center;
      padding: 40px;
      color: #f56c6c;
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
    }
    .ppt-container {
      flex: 1;
      overflow: hidden;
      position: relative;
      background: #000;
    }
    .slide-container {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
    }
    .slide {
      max-width: 90%;
      max-height: 90%;
      background: white;
      box-shadow: 0 4px 20px rgba(0,0,0,0.5);
      border-radius: 4px;
      overflow: hidden;
    }
    .slide iframe {
      width: 100%;
      height: 100%;
      border: none;
    }
    .controls {
      padding: 15px 20px;
      background: #333;
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 15px;
    }
    .btn {
      padding: 8px 16px;
      background: #409eff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      transition: background 0.3s;
    }
    .btn:hover {
      background: #66b1ff;
    }
    .btn:disabled {
      background: #666;
      cursor: not-allowed;
    }
    .slide-info {
      color: white;
      font-size: 14px;
      margin: 0 20px;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <div>
        <h1>PPT 文件预览</h1>
        <div class="file-name">文件名: ${fileName || '未知文件'}</div>
      </div>
    </div>
    <div id="loading" class="loading">正在加载 PPT 文件...</div>
    <div id="error" class="error" style="display: none;"></div>
  </div>
  <script>
    (async function() {
      const loadingEl = document.getElementById('loading')
      const errorEl = document.getElementById('error')
      
      // PPT/PPTX 文件格式复杂，浏览器预览效果有限
      // 提供友好的提示和下载选项
      loadingEl.style.display = 'none'
      const fileTypeName = ${isPptx ? "'PPTX'" : "'PPT'"}
      errorEl.innerHTML = \`
        <div style="max-width: 600px; text-align: center;">
          <div style="font-size: 48px; margin-bottom: 20px;">📊</div>
          <h2 style="color: #333; margin-bottom: 15px;">\${fileTypeName} 文件预览</h2>
          <p style="color: #666; line-height: 1.6; margin-bottom: 20px;">
            由于 \${fileTypeName} 文件格式复杂，包含动画、图表、多媒体等丰富内容，<br>
            浏览器预览无法完整展示所有效果。
          </p>
          <p style="color: #666; line-height: 1.6; margin-bottom: 30px;">
            建议下载文件后使用 Microsoft PowerPoint 或 WPS 等专业软件打开查看完整内容。
          </p>
          <div style="display: flex; gap: 15px; justify-content: center;">
            <a href="${fileUrl}" download="${fileName || 'file'}" style="padding: 12px 24px; background: #409eff; color: white; text-decoration: none; border-radius: 4px; display: inline-block; transition: background 0.3s;">下载文件</a>
            <button onclick="window.close()" style="padding: 12px 24px; background: #f0f0f0; color: #333; border: none; border-radius: 4px; cursor: pointer; transition: background 0.3s;">关闭窗口</button>
          </div>
        </div>
      \`
      errorEl.style.display = 'flex'
    })()
  </script>
</body>
</html>`
}

/**
 * 创建 Word (.docx) 预览页面 HTML（优化版：更美观、信息更丰富）
 * @param {string} fileUrl - 文件URL
 * @param {string} fileName - 文件名
 * @returns {string} HTML 内容
 */
const createDocxPreviewPage = (fileUrl, fileName) => {
  const fileSize = 0 // 文件大小（字节），可以从响应头获取
  const uploadDate = new Date().toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
  
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${fileName || '文档预览'}</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', 'PingFang SC', 'Hiragino Sans GB', sans-serif;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      padding: 0;
      min-height: 100vh;
    }
    
    /* 主容器 */
    .main-container {
      padding: 24px;
      max-width: 1400px;
      margin: 0 auto;
    }
    .document-wrapper {
      background: white;
      border-radius: 12px;
      box-shadow: 0 8px 32px rgba(0,0,0,0.12);
      overflow: hidden;
      margin-bottom: 24px;
    }
    .document-container {
      padding: 80px 100px;
      min-height: 800px;
      background: #fff;
    }
    
    /* 加载和错误状态 */
    .loading, .error {
      text-align: center;
      padding: 80px 40px;
      color: #666;
      background: white;
      border-radius: 12px;
      box-shadow: 0 8px 32px rgba(0,0,0,0.12);
      margin: 24px;
    }
    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 20px;
    }
    .loading-spinner {
      width: 48px;
      height: 48px;
      border: 4px solid #f3f3f3;
      border-top: 4px solid #667eea;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
    .error {
      color: #f56c6c;
    }
    
    /* 文档内容样式优化 */
    #docx-container {
      width: 100%;
    }
    /* 🔥 去掉 docx-preview 库生成的黑色边框 */
    #docx-container .docx-wrapper {
      border: none !important;
      box-shadow: none !important;
      background: transparent !important;
    }
    .docx-preview {
      font-family: 'Microsoft YaHei', 'SimSun', 'PingFang SC', 'Hiragino Sans GB', 'Arial', sans-serif;
      line-height: 1.8;
      color: #333;
      font-size: 15px;
      border: none !important;
    }
    .docx-preview p {
      margin: 12px 0;
      text-align: justify;
      word-spacing: 0.1em;
    }
    .docx-preview h1, .docx-preview h2, .docx-preview h3,
    .docx-preview h4, .docx-preview h5, .docx-preview h6 {
      margin: 24px 0 16px 0;
      font-weight: 600;
      color: #2c3e50;
      line-height: 1.4;
    }
    .docx-preview h1 {
      font-size: 28px;
      border-bottom: 2px solid #667eea;
      padding-bottom: 12px;
    }
    .docx-preview h2 {
      font-size: 24px;
      border-bottom: 1px solid #e0e0e0;
      padding-bottom: 8px;
    }
    .docx-preview h3 {
      font-size: 20px;
    }
    .docx-preview ul, .docx-preview ol {
      margin: 16px 0;
      padding-left: 32px;
    }
    .docx-preview li {
      margin: 8px 0;
      line-height: 1.8;
    }
    .docx-preview blockquote {
      margin: 20px 0;
      padding: 16px 24px;
      background: #f8f9fa;
      border-left: 4px solid #667eea;
      border-radius: 4px;
      font-style: italic;
      color: #555;
    }
    /* 🔥 表格样式优化 - 确保表格正常渲染和排版 */
    .docx-preview table {
      border-collapse: collapse !important;
      border-spacing: 0 !important;
      width: 100% !important;
      margin: 24px 0 !important;
      display: table !important;
      table-layout: auto !important;
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);
      border-radius: 6px;
      overflow: hidden;
      background: #fff;
    }
    .docx-preview table thead {
      display: table-header-group !important;
    }
    .docx-preview table tbody {
      display: table-row-group !important;
    }
    .docx-preview table tr {
      display: table-row !important;
    }
    .docx-preview table th {
      display: table-cell !important;
      background: #f5f5f5;
      color: #333;
      font-weight: 600;
      padding: 10px 12px !important;
      text-align: left;
      border: 1px solid #ddd !important;
      vertical-align: middle;
      white-space: nowrap;
    }
    .docx-preview table td {
      display: table-cell !important;
      padding: 10px 12px !important;
      border: 1px solid #ddd !important;
      vertical-align: middle;
      word-wrap: break-word;
      word-break: break-all;
    }
    .docx-preview table tr:nth-child(even) {
      background: #fafafa;
    }
    .docx-preview table tr:hover {
      background: #f0f4ff;
      transition: background 0.2s;
    }
    /* 确保表格单元格内容正确对齐 */
    .docx-preview table td[align="center"],
    .docx-preview table th[align="center"] {
      text-align: center !important;
    }
    .docx-preview table td[align="right"],
    .docx-preview table th[align="right"] {
      text-align: right !important;
    }
    .docx-preview table td[align="left"],
    .docx-preview table th[align="left"] {
      text-align: left !important;
    }
    .docx-preview img {
      max-width: 100%;
      height: auto;
      border-radius: 6px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      margin: 20px 0;
    }
    .docx-preview code {
      background: #f4f4f4;
      padding: 2px 6px;
      border-radius: 3px;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 14px;
      color: #e83e8c;
    }
    .docx-preview pre {
      background: #2d2d2d;
      color: #f8f8f2;
      padding: 16px;
      border-radius: 6px;
      overflow-x: auto;
      margin: 20px 0;
    }
    .docx-preview pre code {
      background: transparent;
      color: inherit;
      padding: 0;
    }
    
    /* 响应式设计 */
    @media (max-width: 1024px) {
      .document-container {
        padding: 40px 50px;
      }
    }
    @media (max-width: 768px) {
      .main-container {
        padding: 12px;
      }
      .document-container {
        padding: 30px 24px;
      }
      .docx-preview {
        font-size: 14px;
      }
      .docx-preview h1 {
        font-size: 24px;
      }
      .docx-preview h2 {
        font-size: 20px;
      }
      /* 移动端表格横向滚动 */
      .docx-preview table {
        display: block !important;
        overflow-x: auto !important;
        white-space: nowrap;
      }
    }
  </style>
</head>
<body>
  <!-- 主内容区域 -->
  <div class="main-container">
    <div class="document-wrapper">
      <div class="document-container">
        <div id="loading" class="loading">
          <div class="loading-spinner"></div>
          <div>正在加载文档内容...</div>
        </div>
        <div id="error" class="error" style="display: none;"></div>
        <div id="docx-container" style="display: none;"></div>
      </div>
    </div>
  </div>
  
  <!-- 🔥 修复：先加载 JSZip 依赖，docx-preview 需要它来处理 docx 文件（docx 本质上是 ZIP 压缩包） -->
  <script src="https://unpkg.com/jszip@3.10.1/dist/jszip.min.js" onload="window.jszipLoaded = true"></script>
  <script src="https://unpkg.com/docx-preview@0.1.4/dist/docx-preview.min.js" onload="window.docxLoaded = true"></script>
  <script>
    (async function() {
      const loadingEl = document.getElementById('loading')
      const errorEl = document.getElementById('error')
      const containerEl = document.getElementById('docx-container')
      const fileUrl = '${fileUrl}'
      
      // 🔥 修复：等待 JSZip 和 docx 库加载完成（最多等待 10 秒）
      const waitForLibraries = () => {
        return new Promise((resolve, reject) => {
          let attempts = 0
          const maxAttempts = 100 // 10秒 (100 * 100ms)
          
          const checkLibraries = () => {
            attempts++
            if (typeof JSZip !== 'undefined' && typeof docx !== 'undefined') {
              resolve()
            } else if (attempts >= maxAttempts) {
              reject(new Error('库加载超时，请检查网络连接。JSZip: ' + (typeof JSZip !== 'undefined' ? '已加载' : '未加载') + ', docx: ' + (typeof docx !== 'undefined' ? '已加载' : '未加载')))
            } else {
              setTimeout(checkLibraries, 100)
            }
          }
          
          checkLibraries()
        })
      }
      
      try {
        // 等待库加载完成
        await waitForLibraries()
        
        // 下载文件
        const response = await fetch(fileUrl)
        if (!response.ok) {
          throw new Error('下载文件失败: ' + response.statusText)
        }
        
        const arrayBuffer = await response.arrayBuffer()
        
        // 🔥 渲染文档 - 优化表格渲染选项
        await docx.renderAsync(arrayBuffer, containerEl, null, {
          className: 'docx-preview',
          inWrapper: false,
          ignoreWidth: false,
          ignoreHeight: false,
          ignoreFonts: false,
          breakPages: true,
          // 🔥 确保表格正确渲染
          experimental: true
        })
        
        // 🔥 修复表格渲染问题：确保表格元素正确显示
        setTimeout(() => {
          const tables = containerEl.querySelectorAll('table')
          tables.forEach(table => {
            // 确保表格使用正确的 display 属性
            table.style.display = 'table'
            table.style.borderCollapse = 'collapse'
            table.style.width = '100%'
            
            // 修复表头
            const thead = table.querySelector('thead')
            if (thead) {
              thead.style.display = 'table-header-group'
            }
            
            // 修复表体
            const tbody = table.querySelector('tbody')
            if (tbody) {
              tbody.style.display = 'table-row-group'
            }
            
            // 修复所有行
            const rows = table.querySelectorAll('tr')
            rows.forEach(row => {
              row.style.display = 'table-row'
            })
            
            // 修复所有单元格
            const cells = table.querySelectorAll('th, td')
            cells.forEach(cell => {
              cell.style.display = 'table-cell'
              // 确保边框显示
              if (!cell.style.border || cell.style.border === 'none') {
                cell.style.border = '1px solid #ddd'
              }
            })
          })
        }, 100)
        
        loadingEl.style.display = 'none'
        containerEl.style.display = 'block'
      } catch (error) {
        loadingEl.style.display = 'none'
        errorEl.innerHTML = '<div style="font-size: 18px; margin-bottom: 12px;">❌ 预览失败</div><div>' + error.message + '</div>'
        errorEl.style.display = 'block'
      }
    })()
  </script>
</body>
</html>`
}
