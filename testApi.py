"""
知识库操作
"""
import requests,os,json
import logging
DIFY_API_KEY = "dataset-XfEGbHDJ9C3koNrbyN0sYcrl"
DIFY_BASE_URL = "http://192.168.1.203/v1"
KNOWLEDGE_BASE_ID = "6866e4ef-91ba-492c-a91b-5a76dabea0f9"
"""
上传文档到知识库
"""
def upload_doc(file_path, file_name=None):
    """
    上传文件到Dify知识库
    :param file_path: 本地文件路径
    :param file_name: 可选，自定义文件名
    :return: API响应
    """
    if file_name is None:
        file_name = os.path.basename(file_path)
 
    url = f"{DIFY_BASE_URL}/datasets/{KNOWLEDGE_BASE_ID}/document/create-by-file"
 
    headers = {
        "Authorization": f"Bearer {DIFY_API_KEY}",
    }
 
    #new_data = {"indexing_technique":"high_quality","process_rule":{"rules":{"pre_processing_rules":[{"id":"remove_extra_spaces","enabled":True},{"id":"remove_urls_emails","enabled":True}],"segmentation":{"separator":"###","max_tokens":500}},"mode":"custom"}}
    process_rule = {
        "mode": "automatic"  # 自动处理模式，不包含自定义规则
    }
    files = {
        'file': (file_path, open(file_path, 'rb')),
    }
    data = {}
    #try:
    #    import http.client as http_client
    #except ImportError:
    #    # Python 2
    #    import httplib as http_client
    #http_client.HTTPConnection.debuglevel = 1
    #logging.basicConfig()
    #logging.getLogger().setLevel(logging.DEBUG)
    #requests_log = logging.getLogger("requests.packages.urllib3")
    #requests_log.setLevel(logging.DEBUG)
    #requests_log.propagate = True
    data["indexing_technique"] = 'high_quality'
    data["process_rule"] = process_rule
 
    mydata = {}
    mydata['data'] = json.dumps(data)
    session = requests.Session()
    adapter = requests.adapters.HTTPAdapter()
    #session.mount('http://', adapter)
    #session.mount('https://', adapter)
    try:
        print(f"上传文件: {file_path}")
        print(f"请求URL: {url}")
        print(f"数据配置: {mydata}")
        
        response = session.post(url, headers=headers, files=files, data=mydata)
        print(f"响应状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        
        response.raise_for_status()
        return response.json()
    except requests.exceptions.HTTPError as err:
        print(f"HTTP错误发生: {err}")
        print(f"响应内容: {err.response.text}")
    except FileNotFoundError as err:
        print(f"文件未找到: {err}")
    except Exception as err:
        print(f"其他错误: {err}")
    return None
 
def query_datasets():
    headers = {
        "Authorization": f"Bearer {DIFY_API_KEY}",
    }
    url = f"{DIFY_BASE_URL}/datasets"
    response = requests.get(url, headers=headers)
    print(response.json())
 
if __name__ == '__main__':
    result = upload_doc("工业互联网使用说明.docx",'test')
    #query_datasets()