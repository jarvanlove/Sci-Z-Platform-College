
# 1. 获取当前草稿（可选，用于获取hash）
GET /console/api/apps/{app_id}/workflows/draft

# 2. 更新草稿配置
POST /console/api/apps/{app_id}/workflows/draft
{
  "graph": { /* 更新后的节点配置 */ },
  "features": { /* 特性配置 */ },
  "environment_variables": [],
  "conversation_variables": [],
  "hash": "从步骤1获取的hash"
}

# 3. 发布（可选）
POST /console/api/apps/{app_id}/workflows/publish
{
  "marked_name": "v1.0",
  "marked_comment": "更新节点配置"
}

http://192.168.1.203/console/api/apps/158014eb-6b53-417e-898c-e5feba1de72e/workflows/draft  获取工作流节点详情
{
    "id": "06793598-f21e-41f4-84b9-4ddaaae2aef4",
    "graph": {
        "nodes": [
            {
                "data": {
                    "desc": "",
                    "selected": false,
                    "title": "开始",
                    "type": "start",
                    "variables": [
                        {
                            "label": "请输入需要生成的科技报告",
                            "max_length": 120,
                            "options": [],
                            "required": true,
                            "type": "text-input",
                            "variable": "technology_report"
                        },
                        {
                            "variable": "SetCongfig",
                            "label": "SetCongfig",
                            "type": "text-input",
                            "max_length": 48,
                            "required": true,
                            "options": []
                        }
                    ]
                },
                "height": 116,
                "id": "1759113302853",
                "position": {
                    "x": -2195.8325861880107,
                    "y": -1105.6595115567497
                },
                "positionAbsolute": {
                    "x": -2195.8325861880107,
                    "y": -1105.6595115567497
                },
                "selected": false,
                "sourcePosition": "right",
                "targetPosition": "left",
                "type": "custom",
                "width": 244
            },
            {
                "data": {
                    "context": {
                        "enabled": true,
                        "variable_selector": [
                            "1761012476346",
                            "result"
                        ]
                    },
                    "default_value": [
                        {
                            "key": "text",
                            "type": "string",
                            "value": "科技报告生成失败，请检查输入或稍后再试。"
                        }
                    ],
                    "desc": "科技报告生成",
                    "error_strategy": "default-value",
                    "model": {
                        "completion_params": {},
                        "mode": "chat",
                        "name": "qwen3-next-80b-a3b-instruct",
                        "provider": "langgenius/tongyi/tongyi"
                    },
                    "prompt_config": {
                        "jinja2_variables": []
                    },
                    "prompt_template": [
                        {
                            "edition_type": "basic",
                            "id": "6ddca7e6-7cf1-4257-8b63-102cff9c9833",
                            "role": "system",
                            "text": "## 角色：\n1.科研项目科技报告编写专家"
                        },
                        {
                            "id": "8339592c-fbb7-4953-b1a0-034825b0eb60",
                            "role": "user",
                            "text": "## 技能：\n1.你是一位专业的科研项目科技报告资深顾问，擅长科技报告编写、润色、排版、建议、发散思维。\n2.你根据用户输入的需要生成科技报告{{#1759113302853.technology_report#}}和知识库检索结果{{#context#}}，开始编写科技报告，总字数不得低于10000字。\n3.科技报告模板要求（生成的报告要严格按照该要求）\n1. 封面  \n   ‑ 含项目名称、编号、承担单位、项目负责人、完成时间、密级、报告编号（学校机构代码+课题编号+顺序号）。  \n2. 基本信息表（辑要页）  \n   ‑ 摘要、关键词（3–8个）、总经费、起止时间、合作单位等。  \n3. 目录、插图清单、附表清单  \n   ‑ 图/表≥5个时必须单列清单，便于专家快速定位数据。  \n4. 符号/缩略语说明（可选）  \n   ‑ 若文中出现大量自定义符号或跨学科缩写，建议集中给出。\n5. 引言（0号章）  \n   ‑ 研究背景、国内外现状、项目来源、合同任务书目标、技术路线概述。  \n6. 主体（可分2–4章，标题自拟）  \n   6.1 研究内容与技术方案  \n        ‑ 对应任务书“研究内容”逐条展开，写明关键科学问题、技术难点。  \n   6.2 实验（理论）方法与过程  \n        ‑ 实验材料、装置、软件、参数、质量控制措施；附现场照片、流程图。  \n   6.3 结果与讨论  \n        ‑ 用图、表、照片展示原始数据；对数据可信度、误差来源、与国内外同类结果对比进行讨论。  \n   6.4 创新点与成果  \n        ‑ 逐条列出“新理论/新方法/新工艺/新装置”，并给出支撑材料（专利号、论文DOI、检测报告、用户证明）。  \n7. 结论  \n   ‑ 回答“合同指标是否全部达成”，用量化句式：“……使××效率由×%提高到×%，优于合同≥×%的要求”。  \n8. 建议（可选但推荐）  \n   ‑ 指出技术成果下一步转化路径、后续研究方向、潜在风险。  \n9. 参考文献  \n   ‑ 建议≥15篇，其中近5年外文文献占30%以上，体现跟踪前沿。\n10. 附录  \n    ‑ 原始数据表、问卷、源程序、设备校准证书、伦理/环境安全审批表等，方便专家溯源。  \n11. 致谢  \n    ‑ 对合作实验室、企业提供的数据、资金匹配、学生贡献等予以说明，体现科研诚信。  \n12. 承诺书与签字页  \n    ‑ 项目负责人、主要完成人手写签名，加盖学校公章，扫描后插入PDF；承诺“内容真实、数据无篡改”，否则验收一票否决。"
                        },
                        {
                            "role": "assistant",
                            "text": "",
                            "id": "43ffb5cb-7679-43f5-931d-94fb88ddaeba"
                        }
                    ],
                    "retry_config": {
                        "max_retries": 3,
                        "retry_enabled": true,
                        "retry_interval": 1000
                    },
                    "selected": false,
                    "structured_output_enabled": false,
                    "title": "LLM-科技报告生成",
                    "type": "llm",
                    "variables": [],
                    "vision": {
                        "enabled": false
                    }
                },
                "height": 183,
                "id": "1759128942642",
                "position": {
                    "x": -1513.461745485927,
                    "y": -1105.6595115567497
                },
                "positionAbsolute": {
                    "x": -1513.461745485927,
                    "y": -1105.6595115567497
                },
                "selected": false,
                "sourcePosition": "right",
                "targetPosition": "left",
                "type": "custom",
                "width": 244
            },
            {
                "data": {
                    "desc": "转换为最终DOCX文档",
                    "is_team_authorization": true,
                    "output_schema": null,
                    "paramSchemas": [
                        {
                            "auto_generate": null,
                            "default": null,
                            "form": "llm",
                            "human_description": {
                                "en_US": "The Markdown content to be converted to Word document",
                                "ja_JP": "Word ドキュメントに変換する Markdown コンテンツ",
                                "pt_BR": "O conteúdo Markdown a ser convertido para documento Word",
                                "zh_Hans": "要转换为Word文档的Markdown内容"
                            },
                            "label": {
                                "en_US": "Markdown Content",
                                "ja_JP": "Markdown コンテンツ",
                                "pt_BR": "Conteúdo Markdown",
                                "zh_Hans": "Markdown内容"
                            },
                            "llm_description": "The Markdown content that will be converted to a Word document",
                            "max": null,
                            "min": null,
                            "name": "markdown_content",
                            "options": [],
                            "placeholder": null,
                            "precision": null,
                            "required": true,
                            "scope": null,
                            "template": null,
                            "type": "string"
                        },
                        {
                            "auto_generate": null,
                            "default": null,
                            "form": "llm",
                            "human_description": {
                                "en_US": "The title of the Word document",
                                "ja_JP": "Word ドキュメントのタイトル",
                                "pt_BR": "O título do documento Word",
                                "zh_Hans": "Word文档的标题"
                            },
                            "label": {
                                "en_US": "Document Title",
                                "ja_JP": "ドキュメントタイトル",
                                "pt_BR": "Título do Documento",
                                "zh_Hans": "文档标题"
                            },
                            "llm_description": "The title that will be displayed at the top of the Word document",
                            "max": null,
                            "min": null,
                            "name": "title",
                            "options": [],
                            "placeholder": null,
                            "precision": null,
                            "required": false,
                            "scope": null,
                            "template": null,
                            "type": "string"
                        }
                    ],
                    "params": {
                        "markdown_content": "",
                        "title": ""
                    },
                    "provider_id": "stvlynn/doc/doc",
                    "provider_name": "stvlynn/doc/doc",
                    "provider_type": "builtin",
                    "retry_config": {
                        "max_retries": 3,
                        "retry_enabled": true,
                        "retry_interval": 1000
                    },
                    "selected": false,
                    "title": "Markdown转DOCX转换器",
                    "tool_configurations": {},
                    "tool_description": "将Markdown内容转换为DOCX（Word）文档",
                    "tool_label": "Markdown转DOCX转换器",
                    "tool_name": "markdown_to_docx_converter",
                    "tool_node_version": "2",
                    "tool_parameters": {
                        "markdown_content": {
                            "type": "mixed",
                            "value": "{{#1759128942642.text#}}"
                        },
                        "title": {
                            "type": "mixed",
                            "value": "科技报告"
                        }
                    },
                    "type": "tool"
                },
                "height": 111,
                "id": "1759138644279",
                "position": {
                    "x": -1179.2037978736244,
                    "y": -1105.6595115567497
                },
                "positionAbsolute": {
                    "x": -1179.2037978736244,
                    "y": -1105.6595115567497
                },
                "selected": false,
                "sourcePosition": "right",
                "targetPosition": "left",
                "type": "custom",
                "width": 244
            },
            {
                "data": {
                    "desc": "结束并输出",
                    "outputs": [
                        {
                            "value_selector": [
                                "1759138644279",
                                "files"
                            ],
                            "value_type": "array[file]",
                            "variable": "files"
                        },
                        {
                            "value_selector": [
                                "1759138644279",
                                "text"
                            ],
                            "value_type": "string",
                            "variable": "text"
                        },
                        {
                            "value_selector": [
                                "1759138644279",
                                "json"
                            ],
                            "value_type": "array[object]",
                            "variable": "json"
                        }
                    ],
                    "selected": false,
                    "title": "结束",
                    "type": "end"
                },
                "height": 170,
                "id": "1759139101073",
                "position": {
                    "x": -825.9838352985095,
                    "y": -1105.6595115567497
                },
                "positionAbsolute": {
                    "x": -825.9838352985095,
                    "y": -1105.6595115567497
                },
                "selected": false,
                "sourcePosition": "right",
                "targetPosition": "left",
                "type": "custom",
                "width": 244
            },
            {
                "id": "1761012476346",
                "type": "custom",
                "data": {
                    "type": "knowledge-retrieval",
                    "title": "科技报告知识库检索",
                    "desc": "",
                    "query_variable_selector": [
                        "1759113302853",
                        "technology_report"
                    ],
                    "dataset_ids": [
                        "6866e4ef-91ba-492c-a91b-5a76dabea0f9",
                        "4bf1dcb5-145c-41de-862c-4eaa57f2e8d7"
                    ],
                    "retrieval_mode": "multiple",
                    "multiple_retrieval_config": {
                        "top_k": 4,
                        "reranking_mode": "reranking_model",
                        "reranking_model": {
                            "provider": "langgenius/tongyi/tongyi",
                            "model": "gte-rerank"
                        },
                        "reranking_enable": true
                    },
                    "selected": true
                },
                "position": {
                    "x": -1826.4824404102512,
                    "y": -1105.6595115567497
                },
                "targetPosition": "left",
                "sourcePosition": "right",
                "positionAbsolute": {
                    "x": -1826.4824404102512,
                    "y": -1105.6595115567497
                },
                "width": 244,
                "height": 120,
                "selected": true
            }
        ],
        "edges": [
            {
                "data": {
                    "isInLoop": false,
                    "sourceType": "tool",
                    "targetType": "end"
                },
                "id": "1759138644279-source-1759139101073-target",
                "source": "1759138644279",
                "sourceHandle": "source",
                "target": "1759139101073",
                "targetHandle": "target",
                "type": "custom",
                "zIndex": 0,
                "selected": false
            },
            {
                "id": "1759113302853-source-1761012476346-target",
                "type": "custom",
                "source": "1759113302853",
                "sourceHandle": "source",
                "target": "1761012476346",
                "targetHandle": "target",
                "data": {
                    "sourceType": "start",
                    "targetType": "knowledge-retrieval",
                    "isInIteration": false,
                    "isInLoop": false
                },
                "zIndex": 0,
                "selected": false
            },
            {
                "id": "1761012476346-source-1759128942642-target",
                "type": "custom",
                "source": "1761012476346",
                "sourceHandle": "source",
                "target": "1759128942642",
                "targetHandle": "target",
                "data": {
                    "sourceType": "knowledge-retrieval",
                    "targetType": "llm",
                    "isInIteration": false,
                    "isInLoop": false
                },
                "zIndex": 0,
                "selected": false
            },
            {
                "id": "1759128942642-source-1759138644279-target",
                "type": "custom",
                "source": "1759128942642",
                "target": "1759138644279",
                "sourceHandle": "source",
                "targetHandle": "target",
                "data": {
                    "sourceType": "llm",
                    "targetType": "tool",
                    "isInLoop": false
                },
                "zIndex": 0
            }
        ],
        "viewport": {
            "x": 1165.0621784912569,
            "y": 789.1743079844156,
            "zoom": 0.5732403491451359
        }
    },
    "features": {
        "opening_statement": "",
        "suggested_questions": [],
        "suggested_questions_after_answer": {
            "enabled": false
        },
        "text_to_speech": {
            "enabled": false,
            "language": "",
            "voice": ""
        },
        "speech_to_text": {
            "enabled": false
        },
        "retriever_resource": {
            "enabled": true
        },
        "sensitive_word_avoidance": {
            "enabled": false
        },
        "file_upload": {
            "image": {
                "enabled": false,
                "number_limits": 3,
                "transfer_methods": [
                    "local_file",
                    "remote_url"
                ]
            },
            "enabled": false,
            "allowed_file_types": [
                "image"
            ],
            "allowed_file_extensions": [
                ".JPG",
                ".JPEG",
                ".PNG",
                ".GIF",
                ".WEBP",
                ".SVG"
            ],
            "allowed_file_upload_methods": [
                "remote_url",
                "local_file"
            ],
            "number_limits": 3,
            "fileUploadConfig": {
                "file_size_limit": 50,
                "batch_count_limit": 100,
                "image_file_size_limit": 10,
                "video_file_size_limit": 100,
                "audio_file_size_limit": 50,
                "workflow_file_upload_limit": 500
            }
        }
    },
    "hash": "6de3117e91e2f94968750c361b0b2f176262a83d418d544c8dd4bd1309820c4c",
    "version": "draft",
    "marked_name": "",
    "marked_comment": "",
    "created_by": {
        "id": "21342683-6e52-419b-8d73-2bcc327d3fae",
        "name": "admin",
        "email": "15114874206@163.com"
    },
    "created_at": 1760950283,
    "updated_by": {
        "id": "21342683-6e52-419b-8d73-2bcc327d3fae",
        "name": "admin",
        "email": "15114874206@163.com"
    },
    "updated_at": 1764922236,
    "tool_published": true,
    "environment_variables": [],
    "conversation_variables": []
}