/*
 Navicat Premium Dump SQL

 Source Server         : postgres
 Source Server Type    : PostgreSQL
 Source Server Version : 150013 (150013)
 Source Host           : localhost:5432
 Source Catalog        : sci_z_platform_dev
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 150013 (150013)
 File Encoding         : 65001

 Date: 30/10/2025 13:55:30
*/


-- ----------------------------
-- Table structure for dify_api_keys
-- ----------------------------
DROP TABLE IF EXISTS "public"."dify_api_keys";
CREATE TABLE "public"."dify_api_keys" (
  "id" int8 NOT NULL DEFAULT nextval('dify_api_keys_id_seq'::regclass),
  "user_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "key_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "resource_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "api_key" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "key_name" varchar(100) COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "is_active" bool DEFAULT true,
  "created_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "created_by" varchar(100) COLLATE "pg_catalog"."default",
  "updated_by" varchar(100) COLLATE "pg_catalog"."default",
  "deleted" int8
)
;
COMMENT ON COLUMN "public"."dify_api_keys"."id" IS '主键ID';
COMMENT ON COLUMN "public"."dify_api_keys"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."dify_api_keys"."key_type" IS '密钥类型: dataset, workflow';
COMMENT ON COLUMN "public"."dify_api_keys"."resource_id" IS '资源ID: 知识库ID或工作流ID';
COMMENT ON COLUMN "public"."dify_api_keys"."api_key" IS 'API密钥';
COMMENT ON COLUMN "public"."dify_api_keys"."key_name" IS '密钥名称';
COMMENT ON COLUMN "public"."dify_api_keys"."description" IS '密钥描述';
COMMENT ON COLUMN "public"."dify_api_keys"."is_active" IS '是否激活';
COMMENT ON COLUMN "public"."dify_api_keys"."created_time" IS '创建时间';
COMMENT ON COLUMN "public"."dify_api_keys"."updated_time" IS '更新时间';
COMMENT ON COLUMN "public"."dify_api_keys"."created_by" IS '创建者';
COMMENT ON COLUMN "public"."dify_api_keys"."updated_by" IS '更新者';
COMMENT ON COLUMN "public"."dify_api_keys"."deleted" IS '逻辑删除';
COMMENT ON TABLE "public"."dify_api_keys" IS 'Dify API 密钥管理表';

-- ----------------------------
-- Records of dify_api_keys
-- ----------------------------
INSERT INTO "public"."dify_api_keys" VALUES (1, 'admin', 'dataset', 'knowledge_base_001', 'dataset-MwOxGbIDhZmg6bUdHEid0rhX', '默认知识库密钥', '管理员默认知识库API密钥', 'f', '2025-10-29 07:41:21.626793', '2025-10-29 07:43:45.512979', 'system', NULL, 0);
INSERT INTO "public"."dify_api_keys" VALUES (2, 'admin', 'workflow', 'workflow_001', 'app-TsSdGbD50r8fDt1shM3RBSMi', '默认工作流密钥', '管理员默认工作流API密钥', 'f', '2025-10-29 07:41:21.626793', '2025-10-29 07:43:45.518924', 'system', NULL, 0);
INSERT INTO "public"."dify_api_keys" VALUES (3, 'user001', 'dataset', 'knowledge_base_002', 'dataset-User001Key123456789', '用户001知识库密钥', '用户001的知识库API密钥', 'f', '2025-10-29 07:41:21.626793', '2025-10-29 07:43:45.521126', 'user001', NULL, 0);
INSERT INTO "public"."dify_api_keys" VALUES (4, 'user001', 'workflow', 'workflow_002', 'app-User001WorkflowKey123', '用户001工作流密钥', '用户001的工作流API密钥', 'f', '2025-10-29 07:41:21.626793', '2025-10-29 07:43:45.523834', 'user001', NULL, 0);

-- ----------------------------
-- Indexes structure for table dify_api_keys
-- ----------------------------
CREATE INDEX "idx_active" ON "public"."dify_api_keys" USING btree (
  "is_active" "pg_catalog"."bool_ops" ASC NULLS LAST
);
CREATE INDEX "idx_key_type" ON "public"."dify_api_keys" USING btree (
  "key_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_resource_id" ON "public"."dify_api_keys" USING btree (
  "resource_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_user_id" ON "public"."dify_api_keys" USING btree (
  "user_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table dify_api_keys
-- ----------------------------
CREATE TRIGGER "public"."update_dify_api_keys_updated_at" BEFORE UPDATE ON "public"."dify_api_keys"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Uniques structure for table dify_api_keys
-- ----------------------------
ALTER TABLE "public"."dify_api_keys" ADD CONSTRAINT "uk_user_resource_type" UNIQUE ("user_id", "resource_id", "key_type");

-- ----------------------------
-- Primary Key structure for table dify_api_keys
-- ----------------------------
ALTER TABLE "public"."dify_api_keys" ADD CONSTRAINT "dify_api_keys_pkey" PRIMARY KEY ("id");
