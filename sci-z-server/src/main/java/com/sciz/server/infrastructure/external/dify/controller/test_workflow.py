#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dify Workflow 阻塞式测试脚本
测试 /v1/workflows/run 接口 - 阻塞模式
"""

import io
import json
import requests
import sys

# 修复 Windows 控制台编码问题
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# ========================================
# 🔧 基本配置
# ========================================

BASE_URL = "http://192.168.1.203"  # ⚠️ 不要加 /v1
API_TOKEN = "app-TsSdGbD50r8fDt1shM3RBSMi"  # 请替换为您的实际 token

# ========================================
# 🎯 测试配置
# ========================================
TEST_INPUTS = {
    "technology_report": "请总结这段文字：人工智能技术在工业互联网平台中的应用越来越广泛，它能够帮助企业实现智能化生产、优化资源配置、提高运营效率。通过机器学习算法，系统可以自动分析生产数据，预测设备故障，优化生产流程，从而降低运营成本，提升产品质量。"
}

def test_workflow_blocking(inputs, user_id="workflow_user_001"):
    """测试 workflow 阻塞式响应"""
    
    print("=" * 60)
    print("🚀 Dify Workflow 阻塞式测试")
    print("=" * 60)
    print(f"🌐 服务器: {BASE_URL}")
    print(f"👤 用户ID: {user_id}")
    print()
    
    # 准备请求
    url = f"{BASE_URL}/v1/workflows/run"
    headers = {
        "Authorization": f"Bearer {API_TOKEN}",
        "Content-Type": "application/json"
    }
    
    data = {
        "inputs": inputs,
        "response_mode": "blocking",
        "user": user_id
    }
    
    print("📋 请求配置:")
    print(json.dumps(data, indent=2, ensure_ascii=False))
    print()
    
    # 发送请求
    print("🚀 正在发送请求...")
    try:
        response = requests.post(url, headers=headers, json=data, timeout=120)
        
        print(f"📊 响应状态码: {response.status_code}")
        print()
        
        if response.status_code == 200:
            print("✅ 请求成功!")
            result = response.json()
            
            print("\n" + "=" * 60)
            print("📄 响应结果:")
            print(json.dumps(result, indent=2, ensure_ascii=False))
            print("=" * 60)
            
        else:
            print(f"❌ 请求失败: HTTP {response.status_code}")
            try:
                error_info = response.json()
                print(json.dumps(error_info, indent=2, ensure_ascii=False))
            except:
                print(response.text)
        
    except requests.exceptions.RequestException as e:
        print(f"\n❌ 请求异常: {e}")
        if hasattr(e, 'response') and e.response is not None:
            print(f"响应内容: {e.response.text}")
    except Exception as e:
        print(f"\n❌ 未知错误: {e}")
    
    print()

def main():
    """主函数"""
    
    print("🔧 Dify Workflow 阻塞式测试工具")
    print("=" * 60)
    
    # 检查配置
    if API_TOKEN == "app-XXXXXXXXXXXXXX":
        print("❌ 错误: 请先配置正确的 API Token")
        print("💡 请在脚本中修改 API_TOKEN 变量")
        return
    
    print("✅ 配置检查通过")
    print()

    
    # 直接使用默认测试数据
    print("🚀 开始测试...")
    test_workflow_blocking(TEST_INPUTS)

if __name__ == "__main__":
    main()
