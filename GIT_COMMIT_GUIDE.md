# Git 提交指南 - 选择性提交文件

## 问题描述

在 Cursor 中自动化提交代码时，默认会提交所有修改的文件。需要能够：

1. **查看将要提交哪些文件**
2. **选择性提交部分文件**

---

## 一、查看修改的文件

### 1. 查看所有修改的文件（未暂存）

```bash
git status
```

**输出说明：**

- `M` = 修改的文件（Modified）
- `A` = 新增的文件（Added）
- `D` = 删除的文件（Deleted）
- `??` = 未跟踪的新文件（Untracked）

### 2. 查看详细的文件修改内容

```bash
# 查看每个文件的具体修改内容
git diff

# 查看特定文件的修改内容
git diff <文件路径>

# 例如：
git diff sci-z-server/src/main/java/com/sciz/server/application/service/user/UserServiceImpl.java
```

### 3. 查看已暂存的文件（将要提交的文件）

```bash
# 查看已暂存的文件
git status
# 已暂存的文件会显示在 "Changes to be committed:" 下面

# 或使用简短格式
git status -sb
```

### 4. 查看已暂存文件的修改内容

```bash
# 查看已暂存的文件修改内容
git diff --cached

# 查看特定已暂存文件的修改内容
git diff --cached <文件路径>
```

---

## 二、选择性提交文件

### 方法 1：逐个添加文件（推荐）

```bash
# 1. 查看所有修改的文件
git status

# 2. 逐个添加要提交的文件
git add <文件路径1>
git add <文件路径2>
git add <文件路径3>

# 例如：
git add sci-z-server/src/main/java/com/sciz/server/application/service/user/UserServiceImpl.java
git add sci-z-web/src/components/Business/User/UserProfile.vue

# 3. 查看已暂存的文件（确认）
git status

# 4. 提交
git commit -m "feat: 提交信息描述"

# 5. 推送
git push gitlab wjw
```

### 方法 2：添加整个目录

```bash
# 只添加某个目录下的所有修改
git add sci-z-server/src/main/java/com/sciz/server/application/service/
git add sci-z-web/src/components/Business/User/
```

### 方法 3：使用交互式添加（git add -i）

```bash
# 进入交互式模式，可以选择文件
git add -i
```

**交互式模式说明：**

- 输入数字选择要操作的文件
- `1` = 暂存
- `2` = 取消暂存
- `3` = 查看差异
- `4` = 查看未跟踪的文件
- `q` = 退出

### 方法 4：使用通配符添加特定类型文件

```bash
# 只添加所有 .java 文件
git add '*.java'

# 只添加 sci-z-server 目录下的所有修改
git add sci-z-server/

# 只添加 sci-z-web 目录下的所有修改
git add sci-z-web/
```

---

## 三、取消暂存文件（误添加后）

```bash
# 取消暂存所有文件
git reset

# 取消暂存特定文件
git reset <文件路径>

# 例如：
git reset sci-z-server/src/main/java/com/sciz/server/application/service/user/UserServiceImpl.java

# 取消暂存但保留修改
git restore --staged <文件路径>
```

---

## 四、完整工作流程示例

### 场景：修改了多个文件，但只想提交部分

```bash
# === 步骤1：查看所有修改 ===
git status
# 输出示例：
# Modified: sci-z-server/.../UserServiceImpl.java
# Modified: sci-z-web/.../UserProfile.vue
# Modified: sci-z-server/.../UserController.java
# Modified: sci-z-web/.../auto-imports.d.ts (这是自动生成的，不想提交)

# === 步骤2：查看具体修改内容（可选）===
git diff sci-z-server/src/main/java/com/sciz/server/application/service/user/UserServiceImpl.java

# === 步骤3：只添加要提交的文件 ===
git add sci-z-server/src/main/java/com/sciz/server/application/service/user/UserServiceImpl.java
git add sci-z-web/src/components/Business/User/UserProfile.vue
git add sci-z-server/src/main/java/com/sciz/server/interfaces/controller/UserController.java
# 注意：不添加 auto-imports.d.ts

# === 步骤4：确认已暂存的文件 ===
git status
# 应该只显示上面3个文件在 "Changes to be committed:" 下面

# === 步骤5：提交 ===
git commit -m "feat: 用户管理功能优化"

# === 步骤6：推送 ===
git push gitlab wjw

# === 步骤7：剩余的文件可以下次再提交 ===
git status
# 现在应该只显示 auto-imports.d.ts 还在修改列表中
```

---

## 五、在 Cursor 中的最佳实践

### 1. 提交前检查清单

在提交前，建议执行以下命令查看将要提交的内容：

```bash
# 查看将要提交的文件列表
git status

# 查看将要提交的具体修改
git diff --cached

# 如果修改内容太多，可以查看摘要
git diff --cached --stat
```

### 2. 排除自动生成的文件

**常见的自动生成文件（建议不提交）：**

- `components.d.ts` - Vue 组件类型声明（自动生成）
- `auto-imports.d.ts` - 自动导入类型声明（自动生成）
- `.idea/` - IDE 配置（如果不需要共享）
- `node_modules/` - 依赖包（已通过.gitignore 排除）

**处理方法：**

```bash
# 如果误添加了自动生成的文件，取消暂存
git restore --staged sci-z-web/components.d.ts

# 或添加到 .gitignore（如果还没有）
echo "components.d.ts" >> .gitignore
echo "auto-imports.d.ts" >> .gitignore
```

### 3. 提交信息规范

即使选择性提交，也要使用规范的提交信息：

```bash
git commit -m "feat: 功能描述"
git commit -m "fix: 修复bug描述"
git commit -m "docs: 文档更新"
git commit -m "style: 代码格式调整"
git commit -m "refactor: 代码重构"
git commit -m "test: 测试相关"
git commit -m "chore: 构建/工具链相关"
```

---

## 六、常见场景处理

### 场景 1：只提交后端代码，不提交前端代码

```bash
git add sci-z-server/
git commit -m "feat: 后端功能实现"
git push gitlab wjw
# 前端的修改保留在工作区，下次再提交
```

### 场景 2：只提交特定功能相关的文件

```bash
# 假设修改了用户管理相关的多个文件
git add sci-z-server/src/main/java/com/sciz/server/application/service/user/
git add sci-z-server/src/main/java/com/sciz/server/interfaces/controller/UserController.java
git add sci-z-web/src/components/Business/User/
git commit -m "feat: 用户管理功能"
```

### 场景 3：将大改动拆分成多个小提交

```bash
# 第一次提交：只提交后端的核心逻辑
git add sci-z-server/src/main/java/com/sciz/server/application/service/
git commit -m "feat: 实现用户服务核心逻辑"

# 第二次提交：提交Controller层
git add sci-z-server/src/main/java/com/sciz/server/interfaces/controller/
git commit -m "feat: 添加用户管理接口"

# 第三次提交：提交前端组件
git add sci-z-web/src/components/Business/User/
git commit -m "feat: 用户管理前端组件"
```

---

## 七、快速命令参考

```bash
# 查看状态
git status                    # 详细状态
git status -sb               # 简短状态

# 查看修改
git diff                     # 未暂存的修改
git diff --cached            # 已暂存的修改
git diff --stat              # 修改统计

# 添加文件
git add <文件>               # 添加单个文件
git add <目录>               # 添加整个目录
git add -A                   # 添加所有修改（不推荐用于选择性提交）

# 取消暂存
git reset                    # 取消所有暂存
git restore --staged <文件>  # 取消单个文件暂存

# 提交
git commit -m "提交信息"     # 提交已暂存的文件

# 查看将要提交的内容
git diff --cached            # 查看将要提交的修改
git diff --cached --stat     # 查看将要提交的文件统计
```

---

## 八、注意事项

1. **不要使用 `git add -A` 或 `git add .`**（除非确定要提交所有文件）

   - 这些命令会添加所有修改的文件，包括自动生成的文件

2. **提交前务必检查**

   - 使用 `git status` 查看将要提交的文件
   - 使用 `git diff --cached` 查看将要提交的修改内容

3. **保持提交的原子性**

   - 每次提交应该是一个完整的功能或修复
   - 不要在一个提交中混合多个不相关的修改

4. **自动生成的文件**
   - 建议添加到 `.gitignore`
   - 如果必须提交，确保是必要的配置

---

**文档更新时间：** 2025-01-XX  
**维护人：** wjw
