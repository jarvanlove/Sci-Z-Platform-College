<!--
/**
 * @description 文件类型图标组件
 * 根据文件扩展名显示不同的现代化透明 logo
 * 支持：图片、PPT、Word、Excel、视频、音频、PDF、文本文件等
 * 通用组件，可在多个地方复用
 */
-->
<template>
  <div class="file-type-icon" :class="[`file-type-${fileType}`, sizeClass]">
    <el-icon :class="['file-icon', `icon-${fileType}`]">
      <component :is="iconComponent" />
    </el-icon>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  Picture,
  Document,
  VideoPlay,
  Headset,
  Files,
  Collection,
  // Office 文档图标
  Edit,
  // Excel 可以用 DataAnalysis 或类似图标
  DataAnalysis,
  // PPT 可以用 Presentation 或类似图标
  Promotion,
  // 文本文件
  Notebook,
  // 其他文件
  Folder
} from '@element-plus/icons-vue'

const props = defineProps({
  // 文件扩展名（如 'pdf', 'docx', 'jpg'）
  extension: {
    type: String,
    default: ''
  },
  // 文件名（可选，用于辅助判断）
  fileName: {
    type: String,
    default: ''
  },
  // 图标大小：small(32px), medium(48px), large(56px)
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
})

// 根据扩展名判断文件类型
const fileType = computed(() => {
  const ext = (props.extension || '').toLowerCase().replace('.', '')
  const name = (props.fileName || '').toLowerCase()
  
  // 图片类型
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico']
  if (imageExts.includes(ext) || imageExts.some(e => name.endsWith(`.${e}`))) {
    return 'image'
  }
  
  // PDF
  if (ext === 'pdf' || name.endsWith('.pdf')) {
    return 'pdf'
  }
  
  // Word 文档
  const wordExts = ['doc', 'docx']
  if (wordExts.includes(ext) || wordExts.some(e => name.endsWith(`.${e}`))) {
    return 'word'
  }
  
  // Excel 文档
  const excelExts = ['xls', 'xlsx', 'csv']
  if (excelExts.includes(ext) || excelExts.some(e => name.endsWith(`.${e}`))) {
    return 'excel'
  }
  
  // PPT 文档
  const pptExts = ['ppt', 'pptx']
  if (pptExts.includes(ext) || pptExts.some(e => name.endsWith(`.${e}`))) {
    return 'ppt'
  }
  
  // 视频文件
  const videoExts = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm', 'm4v']
  if (videoExts.includes(ext) || videoExts.some(e => name.endsWith(`.${e}`))) {
    return 'video'
  }
  
  // 音频文件
  const audioExts = ['mp3', 'wav', 'flac', 'aac', 'ogg', 'wma', 'm4a']
  if (audioExts.includes(ext) || audioExts.some(e => name.endsWith(`.${e}`))) {
    return 'audio'
  }
  
  // 文本文件
  const textExts = ['txt', 'md', 'json', 'xml', 'yaml', 'yml', 'log', 'conf', 'ini']
  if (textExts.includes(ext) || textExts.some(e => name.endsWith(`.${e}`))) {
    return 'text'
  }
  
  // 压缩文件
  const archiveExts = ['zip', 'rar', '7z', 'tar', 'gz', 'bz2']
  if (archiveExts.includes(ext) || archiveExts.some(e => name.endsWith(`.${e}`))) {
    return 'archive'
  }
  
  // 默认：其他文档
  return 'document'
})

// 根据文件类型选择图标组件
const iconComponent = computed(() => {
  switch (fileType.value) {
    case 'image':
      return Picture
    case 'pdf':
      return Document
    case 'word':
      return Edit
    case 'excel':
      return DataAnalysis
    case 'ppt':
      return Promotion
    case 'video':
      return VideoPlay
    case 'audio':
      return Headset
    case 'text':
      return Notebook
    case 'archive':
      return Files
    default:
      return Document
  }
})

// 尺寸类名
const sizeClass = computed(() => `size-${props.size}`)
</script>

<style lang="scss" scoped>
.file-type-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  // 🔥 现代化透明渐变背景
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
  // 🔥 半透明边框
  border: 1.5px solid rgba(59, 130, 246, 0.25);
  // 🔥 微妙的阴影
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  transition: all 0.3s ease;
  
  .file-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    // 🔥 图标颜色：使用主题蓝色，带透明度
    color: rgba(59, 130, 246, 0.9);
    transition: all 0.3s ease;
  }
  
  // 尺寸变体
  &.size-small {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    
    .file-icon {
      font-size: 18px;
    }
  }
  
  &.size-medium {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    
    .file-icon {
      font-size: 24px;
    }
  }
  
  &.size-large {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    
    .file-icon {
      font-size: 28px;
    }
  }
  
  // 🔥 不同文件类型的专属颜色主题
  &.file-type-image {
    background: linear-gradient(135deg, rgba(236, 72, 153, 0.15) 0%, rgba(219, 39, 119, 0.15) 100%);
    border-color: rgba(236, 72, 153, 0.25);
    box-shadow: 0 4px 12px rgba(236, 72, 153, 0.15);
    
    .file-icon {
      color: rgba(236, 72, 153, 0.9);
    }
  }
  
  &.file-type-pdf {
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.15) 0%, rgba(220, 38, 38, 0.15) 100%);
    border-color: rgba(239, 68, 68, 0.25);
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.15);
    
    .file-icon {
      color: rgba(239, 68, 68, 0.9);
    }
  }
  
  &.file-type-word {
    background: linear-gradient(135deg, rgba(37, 99, 235, 0.15) 0%, rgba(29, 78, 216, 0.15) 100%);
    border-color: rgba(37, 99, 235, 0.25);
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
    
    .file-icon {
      color: rgba(37, 99, 235, 0.9);
    }
  }
  
  &.file-type-excel {
    background: linear-gradient(135deg, rgba(34, 197, 94, 0.15) 0%, rgba(22, 163, 74, 0.15) 100%);
    border-color: rgba(34, 197, 94, 0.25);
    box-shadow: 0 4px 12px rgba(34, 197, 94, 0.15);
    
    .file-icon {
      color: rgba(34, 197, 94, 0.9);
    }
  }
  
  &.file-type-ppt {
    background: linear-gradient(135deg, rgba(249, 115, 22, 0.15) 0%, rgba(234, 88, 12, 0.15) 100%);
    border-color: rgba(249, 115, 22, 0.25);
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.15);
    
    .file-icon {
      color: rgba(249, 115, 22, 0.9);
    }
  }
  
  &.file-type-video {
    background: linear-gradient(135deg, rgba(168, 85, 247, 0.15) 0%, rgba(147, 51, 234, 0.15) 100%);
    border-color: rgba(168, 85, 247, 0.25);
    box-shadow: 0 4px 12px rgba(168, 85, 247, 0.15);
    
    .file-icon {
      color: rgba(168, 85, 247, 0.9);
    }
  }
  
  &.file-type-audio {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(37, 99, 235, 0.15) 100%);
    border-color: rgba(59, 130, 246, 0.25);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
    
    .file-icon {
      color: rgba(59, 130, 246, 0.9);
    }
  }
  
  &.file-type-text {
    background: linear-gradient(135deg, rgba(107, 114, 128, 0.15) 0%, rgba(75, 85, 99, 0.15) 100%);
    border-color: rgba(107, 114, 128, 0.25);
    box-shadow: 0 4px 12px rgba(107, 114, 128, 0.15);
    
    .file-icon {
      color: rgba(107, 114, 128, 0.9);
    }
  }
  
  &.file-type-archive {
    background: linear-gradient(135deg, rgba(139, 92, 246, 0.15) 0%, rgba(124, 58, 237, 0.15) 100%);
    border-color: rgba(139, 92, 246, 0.25);
    box-shadow: 0 4px 12px rgba(139, 92, 246, 0.15);
    
    .file-icon {
      color: rgba(139, 92, 246, 0.9);
    }
  }
  
  &.file-type-document {
    // 默认文档类型保持蓝色主题
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
    border-color: rgba(59, 130, 246, 0.25);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
    
    .file-icon {
      color: rgba(59, 130, 246, 0.9);
    }
  }
  
  // 🔥 悬浮效果
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(59, 130, 246, 0.25);
    
    &.file-type-image {
      box-shadow: 0 6px 16px rgba(236, 72, 153, 0.25);
    }
    
    &.file-type-pdf {
      box-shadow: 0 6px 16px rgba(239, 68, 68, 0.25);
    }
    
    &.file-type-word {
      box-shadow: 0 6px 16px rgba(37, 99, 235, 0.25);
    }
    
    &.file-type-excel {
      box-shadow: 0 6px 16px rgba(34, 197, 94, 0.25);
    }
    
    &.file-type-ppt {
      box-shadow: 0 6px 16px rgba(249, 115, 22, 0.25);
    }
    
    &.file-type-video {
      box-shadow: 0 6px 16px rgba(168, 85, 247, 0.25);
    }
    
    &.file-type-audio {
      box-shadow: 0 6px 16px rgba(59, 130, 246, 0.25);
    }
    
    &.file-type-text {
      box-shadow: 0 6px 16px rgba(107, 114, 246, 0.25);
    }
    
    &.file-type-archive {
      box-shadow: 0 6px 16px rgba(139, 92, 246, 0.25);
    }
  }
}
</style>
