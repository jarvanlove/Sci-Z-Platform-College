#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dify 文件上传 - 最终版本
配置：text-embedding-v3 + gte-rerank + 混合检索 + 高质量索引 + 通用分段
"""

import requests
import json
import os
import sys

# # 修复 Windows 控制台编码问题
# if sys.platform == 'win32':
#     import io
#     sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
#     sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# ========================================
# 🔧 基本配置
# ========================================
# 🔥 云端测试（去掉 /v1，脚本会自动添加）
BASE_URL = "http://192.168.1.203"  # ⚠️ 不要加 /v1
DATASET_ID = "fdfa778d-3d6c-430f-9c55-69234da7994c"
API_KEY = "dataset-XfEGbHDJ9C3koNrbyN0sYcrl"

# 本地测试（切换时取消注释）
# BASE_URL = "http://localhost:5001"
# DATASET_ID = "00472067-44cf-4af7-ba3a-cd2d6cb39239"
# API_KEY = "dataset-AYHOkSjR0iajmQdjYCJH0dkF"
# ========================================
# 📁 文件配置
# ========================================
# 🔥 选择上传方式（二选一）

# 方式1: 使用本地文件（推荐）
USE_LOCAL_FILE = True
LOCAL_FILE_PATH = r"D:\Work文档\工业互联网平台软件部署教程.docx"

# 方式2: 使用测试文本
# USE_LOCAL_FILE = False  # 取消注释这行来使用测试文本

# ========================================
# 🎯 模型配置
# ========================================
# 根据您的要求配置

UPLOAD_CONFIG = {
    # 索引方式：高质量
    "indexing_technique": "high_quality",
    
    # 索引内容形式：文本模式
    "doc_form": "text_model",
    
    # 文档语言
    "doc_language": "Chinese",
    
    # ========== Embedding 模型配置 ==========
    # 🔥 重要：根据您 Dify 中实际配置的模型修改
    "embedding_model": "text-embedding-v3",
    "embedding_model_provider": "tongyi",  # 通义千问
    
    # ========== 检索模型配置 ==========
    "retrieval_model": {
        # 混合检索
        "search_method": "hybrid_search",
        
        # 🔥 暂时禁用 rerank（如果您没有配置 rerank 模型）
        # 如果需要启用，请先在 Dify 控制台配置 rerank 模型
        "reranking_enable": False,
        
        # 如果您已配置 rerank，取消下面的注释并修改 provider
        "reranking_enable": True,
        "reranking_model": {
            # 常见的 rerank providers:
            # - "xinference" (本地部署的 gte-rerank)
            # - "jina" (Jina AI rerank)
            # - "cohere" (Cohere rerank)
            # - "voyage" (Voyage AI)
            "reranking_provider_name": "langgenius/tongyi/tongyi",  # 修改为您实际的 provider
            "reranking_model_name": "gte-rerank"
        },
        
        # 召回数量
        "top_k": 3,
        
        # 分数阈值
        "score_threshold_enabled": False,
        "score_threshold": 0.5
    },
    
    # ========== 处理规则：通用分段（自动模式）==========
    "process_rule": {
        "mode": "automatic"
    }
}

# ========================================
# 📝 可选：如果您的知识库已经配置好，可以使用简化配置
# ========================================
# 取消下面的注释，使用知识库默认配置
# UPLOAD_CONFIG = {
#     "process_rule": {
#         "mode": "automatic"
#     }
# }


def upload_file():
    """上传文件到 Dify"""
    
    print("=" * 60)
    print("📤 Dify 文件上传工具")
    print("=" * 60)
    print(f"🌐 服务器: {BASE_URL}")
    print(f"📦 数据集: {DATASET_ID}")
    print()
    
    # 准备文件
    if USE_LOCAL_FILE:
        if not os.path.exists(LOCAL_FILE_PATH):
            print(f"❌ 错误: 文件不存在 - {LOCAL_FILE_PATH}")
            return
        
        print(f"📁 本地文件: {LOCAL_FILE_PATH}")
        file_size = os.path.getsize(LOCAL_FILE_PATH) / 1024
        print(f"📊 文件大小: {file_size:.2f} KB")
        
        with open(LOCAL_FILE_PATH, 'rb') as f:
            content = f.read()
        
        filename = os.path.basename(LOCAL_FILE_PATH)
        
        # 确定 MIME 类型
        ext = os.path.splitext(filename)[1].lower()
        mime_types = {
            '.txt': 'text/plain',
            '.pdf': 'application/pdf',
            '.doc': 'application/msword',
            '.docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            '.md': 'text/markdown',
        }
        mime_type = mime_types.get(ext, 'application/octet-stream')
        
        files = {'file': (filename, content, mime_type)}

    
    # 准备请求
    url = f"{BASE_URL}/v1/datasets/{DATASET_ID}/document/create-by-file"
    headers = {"Authorization": f"Bearer {API_KEY}"}
    data = {'data': json.dumps(UPLOAD_CONFIG)}
    
    # 打印配置信息
    print("\n📋 上传配置:")
    print(json.dumps(UPLOAD_CONFIG, indent=2, ensure_ascii=False))
    print()
    
    # 发送请求
    print("🚀 正在上传...")
    try:
        response = requests.post(url, headers=headers, files=files, data=data, timeout=300)
        
        print(f"\n📊 响应状态码: {response.status_code}")
        
        if response.status_code == 200:
            print("✅ 上传成功!")
            result = response.json()
            
            if 'document' in result:
                doc = result['document']
                print("\n" + "=" * 60)
                print("📄 文档信息:")
                print(f"  - ID: {doc.get('id')}")
                print(f"  - 名称: {doc.get('name')}")
                print(f"  - 状态: {doc.get('indexing_status')}")
                print(f"  - 批次: {result.get('batch')}")
                print("=" * 60)
                
                # 提供查询索引状态的命令
                print("\n💡 查询索引状态:")
                print(f"curl -X GET \"{BASE_URL}/v1/datasets/{DATASET_ID}/documents/{result.get('batch')}/indexing-status\" \\")
                print(f"  -H \"Authorization: Bearer {API_KEY}\"")
            
            print("\n完整响应:")
            print(json.dumps(result, indent=2, ensure_ascii=False))
            
        elif response.status_code == 400:
            print("❌ 上传失败: 请求参数错误")
            error_info = response.json()
            print(json.dumps(error_info, indent=2, ensure_ascii=False))
            
            # 根据错误提供建议
            if error_info.get('code') == 'provider_not_initialize':
                print("\n💡 解决建议:")
                print("1. 检查 Dify 控制台 → 设置 → 模型供应商")
                print("2. 确保已配置对应的 Embedding 模型")
                print("3. 修改本脚本中的 embedding_model_provider")
                print("   当前配置: " + UPLOAD_CONFIG.get('embedding_model_provider', 'N/A'))
                print("\n常见 Provider 名称:")
                print("  - OpenAI: 'openai'")
                print("  - Azure OpenAI: 'azure_openai'")
                print("  - 智谱 AI: 'zhipuai'")
                print("  - 通义千问: 'tongyi'")
                print("  - Ollama: 'ollama'")
        else:
            print(f"❌ 上传失败: HTTP {response.status_code}")
            print(response.text)
        
    except requests.exceptions.RequestException as e:
        print(f"\n❌ 请求异常: {e}")
        if hasattr(e, 'response') and e.response is not None:
            print(f"响应内容: {e.response.text}")
    except Exception as e:
        print(f"\n❌ 未知错误: {e}")
    
    print()


if __name__ == "__main__":
    upload_file()

