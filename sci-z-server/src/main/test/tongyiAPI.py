import os
from dashscope import Generation
import dashscope

# 设置 API 基础地址
dashscope.base_http_api_url = 'https://dashscope.aliyuncs.com/api/v1'

# 方式1：从环境变量读取（推荐）
# 使用前需要设置环境变量：export DASHSCOPE_API_KEY="sk-e94faf3bbc924ab3bdc6817f27b3851c"
# Windows PowerShell: $env:DASHSCOPE_API_KEY="sk-e94faf3bbc924ab3bdc6817f27b3851c"
# Windows CMD: set DASHSCOPE_API_KEY=sk-e94faf3bbc924ab3bdc6817f27b3851c
api_key = os.getenv("DASHSCOPE_API_KEY")

# 方式2：如果环境变量未设置，使用硬编码的 API Key（不推荐，仅用于测试）
if not api_key:
    api_key = "sk-e94faf3bbc924ab3bdc6817f27b3851c"

messages = [
    {"role": "system", "content": "You are a helpful assistant."},
    {"role": "user", "content": "你是谁？"},
]

response = Generation.call(
    api_key=api_key,
    model="qwen3-max",
    messages=messages,
    result_format="message",
)

print(response.output.choices[0].message.content)