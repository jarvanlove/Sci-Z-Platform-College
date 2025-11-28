# Cursor 内置 Git 工具使用指南

## 概述

Cursor 内置了图形化的 Git 工具，可以在左侧面板的 "Source Control" 中查看和使用。这个工具可以帮助您：

- 查看修改的文件
- **选择性添加文件到暂存区**
- 查看文件的具体修改内容
- 提交和推送代码

---

## 一、打开 Git 工具面板

### 方法 1：通过左侧活动栏

1. 点击左侧活动栏的 **"Source Control"** 图标（类似分支的图标，通常显示修改数量）
2. 或使用快捷键：**`Ctrl+Shift+G`** (Windows/Linux) 或 **`Cmd+Shift+G`** (Mac)

### 方法 2：通过命令面板

1. 按 **`Ctrl+Shift+P`** (Windows/Linux) 或 **`Cmd+Shift+P`** (Mac) 打开命令面板
2. 输入 "Source Control" 并选择

---

## 二、查看修改的文件

### 1. 查看所有修改

在左侧 "Source Control" 面板中：

- **"更改" (Changes)** 部分会显示所有修改的文件
- 文件旁边会显示状态图标：
  - `M` = 修改的文件 (Modified)
  - `A` = 新增的文件 (Added)
  - `D` = 删除的文件 (Deleted)
  - `U` = 未跟踪的文件 (Untracked)

### 2. 查看文件具体修改内容

**方法 1：点击文件名**

- 点击文件列表中的文件名
- 会在右侧编辑器中打开**对比视图**，显示：
  - 左侧：原文件内容
  - 右侧：修改后的内容
  - 行号前的 `+` 表示新增，`-` 表示删除

**方法 2：右键菜单**

- 右键点击文件名
- 选择 **"Open Changes"** 查看修改
- 选择 **"Open File"** 直接打开文件

---

## 三、选择性添加文件（关键功能）

### 方法 1：使用 "+" 按钮（推荐）

1. **展开 "更改" (Changes) 部分**

   - 点击 "更改" 旁边的展开图标 `>`

2. **查看文件列表**

   - 会看到所有修改的文件列表

3. **逐个添加文件**

   - 将鼠标悬停在文件名上
   - 会显示一个 **"+"** 按钮（在文件名右侧）
   - **点击 "+" 按钮**将该文件添加到暂存区（Staged）

4. **确认已添加的文件**
   - 添加到暂存区的文件会出现在 **"暂存的更改" (Staged Changes)** 部分
   - 如果看不到这个部分，说明还没有文件被添加

### 方法 2：使用右键菜单

1. **右键点击文件名**
2. **选择 "Stage Changes"**（暂存更改）
   - 这将添加该文件到暂存区
   - 如果文件已经在暂存区，会显示 "Unstage Changes"（取消暂存）

### 方法 3：批量操作

**添加所有文件（谨慎使用）：**

- 点击 "更改" 标题旁的 **"+"** 按钮（在标题行）
- 或右键点击 "更改" 标题，选择 **"Stage All Changes"**

**只添加特定类型的文件：**

- 在文件列表中，按住 **`Ctrl`** (Windows) 或 **`Cmd`** (Mac) 键
- 点击多个文件名（多选）
- 右键选择 **"Stage Changes"**

---

## 四、取消暂存文件（误添加后）

### 方法 1：使用 "-" 按钮

1. 在 **"暂存的更改" (Staged Changes)** 部分
2. 将鼠标悬停在文件名上
3. 点击 **"-"** 按钮取消暂存

### 方法 2：使用右键菜单

1. 在 **"暂存的更改" (Staged Changes)** 部分
2. 右键点击文件名
3. 选择 **"Unstage Changes"**（取消暂存）

### 方法 3：取消所有暂存

- 右键点击 **"暂存的更改"** 标题
- 选择 **"Unstage All Changes"**

---

## 五、查看将要提交的文件

### 在提交前确认

在 **"暂存的更改" (Staged Changes)** 部分：

- 查看列表，确认只有想要提交的文件
- 如果看到不想提交的文件（如自动生成的文件），取消暂存

### 查看暂存文件的具体修改

1. **点击文件名**查看修改内容
2. 或在 **"暂存的更改"** 部分，右键选择 **"Open Changes"**

---

## 六、提交代码

### 步骤 1：填写提交信息

1. 在 **"消息" (Message)** 输入框中填写提交信息
2. 建议使用规范的提交信息格式：
   ```
   feat: 功能描述
   fix: 修复bug描述
   docs: 文档更新
   style: 代码格式调整
   refactor: 代码重构
   test: 测试相关
   chore: 构建/工具链相关
   ```

### 步骤 2：提交

1. **点击绿色的 "提交" (Commit) 按钮**（带勾号 ✓）
2. 或使用快捷键：
   - **`Ctrl+Enter`** (Windows/Linux)
   - **`Cmd+Enter`** (Mac)
   - 注意：提交信息框需要获得焦点（光标在输入框中）

### 步骤 3：确认提交

- 提交后，**"暂存的更改"** 部分会清空
- **"更改"** 部分会显示剩余的未暂存文件（如果有）

---

## 七、拉取代码 (Pull)

### 项目分支结构

**远程仓库：**

- **gitlab** (公司 GitLab)：

  - `main` - 主分支（生产环境）
  - `dev` - 开发分支（测试环境）
  - `wjw` - 个人开发分支
  - `shang` - 另一位开发人员分支

- **origin** (个人 GitHub)：
  - `main` - 主分支（与 gitlab/main 同步）

### 方法 1：在 Cursor 中拉取代码（推荐）

#### 拉取当前分支的最新代码

1. **点击左下角状态栏的分支名称**（如 "wjw"）
2. 在弹出的菜单中：
   - 如果显示 **"Pull"** 或 **"Pull, Push"**，点击即可
   - 或选择 **"Pull from..."** 然后选择远程分支（如 `gitlab/wjw`）

#### 拉取指定远程分支的代码

1. **按 `Ctrl+Shift+P`** 打开命令面板
2. 输入 **"Git: Pull"**
3. 选择要拉取的远程分支（如 `gitlab/dev`）

### 方法 2：使用命令面板拉取指定分支

1. **按 `Ctrl+Shift+P`** 打开命令面板
2. 输入 **"Git: Pull from..."**
3. 选择远程仓库（如 `gitlab`）
4. 选择分支（如 `dev`、`main` 等）

### 方法 3：在终端中拉取（精确控制）

如果您需要在终端中精确控制拉取操作：

```bash
# 拉取 gitlab 的 wjw 分支
git pull gitlab wjw

# 拉取 gitlab 的 dev 分支
git pull gitlab dev

# 拉取 gitlab 的 main 分支
git pull gitlab main

# 拉取 GitHub 的 main 分支
git pull origin main

# 只拉取远程信息，不合并
git fetch gitlab
git fetch origin
```

### 日常开发流程：同步最新代码

**在 wjw 分支开发前，通常需要：**

1. **拉取个人分支最新代码**

   ```bash
   git pull gitlab wjw
   ```

2. **拉取 dev 分支最新代码并合并**

   ```bash
   git pull gitlab dev
   # 如果有冲突，解决冲突后再继续
   ```

3. **合并 dev 到 wjw**
   ```bash
   git merge gitlab/dev
   # 解决冲突（如果有）
   ```

### 状态栏提示

- **向下箭头 (↓)**：表示远程分支有新的提交需要拉取
- **点击分支名称**：会显示下拉菜单，包含 Pull 选项

---

## 八、解决冲突

### 什么是冲突？

当您和他人同时修改了同一个文件的同一部分时，Git 无法自动合并，就会产生冲突。

### 冲突标识

冲突文件会包含以下标记：

```
<<<<<<< HEAD
您的本地代码
=======
远程分支的代码
>>>>>>> gitlab/dev
```

### 在 Cursor 中解决冲突

#### 方法 1：使用内置的冲突解决工具（推荐）

1. **打开冲突文件**

   - 在 Source Control 面板中，冲突文件会显示 **"!"** 图标
   - 点击冲突文件名，会在编辑器中打开

2. **查看冲突区域**

   - Cursor 会在冲突区域显示：
     - `<<<<<<< HEAD` - 您的本地代码（当前分支）
     - `=======` - 分隔线
     - `>>>>>>> gitlab/dev` - 远程分支的代码

3. **使用冲突解决工具**

   - 在冲突区域上方或下方，会显示按钮：
     - **"Accept Current Change"** - 保留您的本地代码
     - **"Accept Incoming Change"** - 使用远程分支的代码
     - **"Accept Both Changes"** - 同时保留两边的代码
     - **"Compare Changes"** - 对比查看两边的修改

4. **手动解决冲突**

   - 如果自动选项不合适，可以手动编辑：
     1. 删除冲突标记（`<<<<<<<`, `=======`, `>>>>>>>`）
     2. 保留需要的代码
     3. 确保代码逻辑正确

5. **标记冲突已解决**
   - 解决冲突后，需要暂存文件（Stage）：
     - 在 Source Control 面板中，点击冲突文件旁的 **"+"** 按钮
     - 或在文件上右键选择 **"Stage Changes"**

#### 方法 2：使用三路对比视图

1. **打开冲突文件**
2. **右键点击冲突标记区域**
3. **选择 "Open Conflicts"** 或 **"Resolve Conflict"**
4. **在对比视图中选择要保留的代码**

### 解决冲突的完整流程

```
1. 拉取代码时出现冲突
   git pull gitlab dev
   # 提示：CONFLICT (content): Merge conflict in xxx.java

2. 查看冲突文件
   - 在 Source Control 面板查看冲突文件（带 ! 图标）

3. 打开冲突文件
   - 点击文件名，在编辑器中打开

4. 解决冲突
   - 使用 "Accept Current Change" / "Accept Incoming Change" / "Accept Both Changes"
   - 或手动编辑，删除冲突标记，保留需要的代码

5. 验证代码
   - 确保代码逻辑正确
   - 确保语法正确

6. 标记冲突已解决
   - 在 Source Control 面板，点击冲突文件旁的 "+" 按钮
   - 文件会从 "Merge Changes" 移动到 "Staged Changes"

7. 完成合并
   - 填写提交信息（通常会自动填充合并信息）
   - 点击 "提交" 按钮
   - 或使用命令：git commit
```

### 常见冲突类型和处理方法

#### 1. 代码冲突

**情况：** 同一行或相邻行被不同人修改了

**解决：**

- 查看两边的代码，确定哪个是正确的
- 或合并两边的代码（如果需要）

#### 2. 文件删除冲突

**情况：** 一个分支删除了文件，另一个分支修改了文件

**解决：**

- 如果文件应该保留：保留修改后的文件
- 如果文件应该删除：删除文件

#### 3. 大型冲突

**情况：** 整个文件或大段代码有冲突

**解决：**

- 仔细对比两边的代码
- 可能需要和团队成员沟通，确定正确的版本

### 冲突解决后的验证

1. **编译检查**：确保代码能正常编译
2. **功能测试**：确保功能正常
3. **运行测试**：运行相关测试用例

### 取消合并（如果冲突太复杂）

如果冲突太复杂，想重新开始：

```bash
# 取消当前的合并操作
git merge --abort
```

---

## 九、推送代码

### 方法 1：在 Cursor 中推送（推荐）

#### 推送当前分支到远程

1. **提交代码后**

   - 提交成功可能会弹出推送提示
   - 点击 **"Push"** 按钮

2. **手动推送**
   - 点击左下角状态栏的**分支名称**（如 "wjw"）
   - 在弹出的菜单中：
     - 如果显示 **"Push"** 或 **"Push, Pull"**，点击即可
     - 或选择 **"Push to..."** 然后选择远程分支（如 `gitlab/wjw`）

#### 推送到指定远程分支

1. **按 `Ctrl+Shift+P`** 打开命令面板
2. 输入 **"Git: Push"**
3. 选择推送选项：
   - **"Push"** - 推送到默认远程分支
   - **"Push to..."** - 推送到指定远程分支
   - **"Force Push"** - 强制推送（谨慎使用）

### 方法 2：在终端中推送（精确控制）

```bash
# 推送当前分支到 gitlab 的对应分支
git push gitlab wjw

# 推送到指定远程分支
git push gitlab wjw:dev  # 将本地 wjw 推送到 gitlab/dev

# 推送 GitHub 的 main 分支
git push origin main

# 强制推送（谨慎使用）
git push gitlab wjw --force
```

### 项目实际工作流程：推送代码

#### 场景 1：在 wjw 分支开发完成后

```bash
# 1. 确保在 wjw 分支
git checkout wjw

# 2. 提交代码（在 Cursor 中操作）
# 3. 推送到 gitlab 的 wjw 分支
git push gitlab wjw
```

#### 场景 2：将 dev 分支推送到 GitHub main

```bash
# 1. 切换到 main 分支
git checkout main

# 2. 拉取 gitlab main 最新代码
git pull gitlab main

# 3. 推送 GitHub main
git push origin main
```

### 状态栏提示

- **向上箭头 (↑)**：表示本地分支有新的提交需要推送
- **点击分支名称**：会显示下拉菜单，包含 Push 选项

### 推送失败的处理

#### 1. 远程分支有新的提交（非冲突）

**情况：** 远程分支有新的提交，您的本地提交落后了

**解决：**

```bash
# 先拉取远程代码
git pull gitlab wjw

# 如果有冲突，解决冲突后
git push gitlab wjw
```

#### 2. 权限问题

**情况：** 没有推送到远程分支的权限

**解决：**

- 联系管理员授予权限
- 或创建 Merge Request 请求合并

#### 3. 网络问题

**情况：** 网络连接失败

**解决：**

- 检查网络连接
- 稍后重试
- 检查 Git 配置（用户名、密码等）

---

## 十、合并代码

### 项目分支合并流程

**标准工作流程：**

```
wjw (个人分支) → dev (开发分支) → main (主分支) → origin/main (GitHub)
```

### 方法 1：在 Cursor 中合并（推荐）

#### 合并 wjw 到 dev 分支

1. **切换到 dev 分支**

   - 点击左下角状态栏的分支名称
   - 选择 **"dev"** 分支
   - 或在命令面板输入 **"Git: Checkout to..."** 选择 dev

2. **拉取 dev 分支最新代码**

   - 点击分支名称 → **"Pull"**
   - 或命令面板 → **"Git: Pull"**

3. **合并 wjw 分支**

   - 按 `Ctrl+Shift+P` 打开命令面板
   - 输入 **"Git: Merge Branch..."**
   - 选择 **"wjw"** 分支
   - 如果出现冲突，按照"八、解决冲突"的方法处理

4. **推送合并结果**
   - 合并成功后，推送 dev 分支：
     - 点击分支名称 → **"Push"**
     - 或命令面板 → **"Git: Push"**

#### 合并 dev 到 main 分支

1. **切换到 main 分支**

   - 点击分支名称 → 选择 **"main"**

2. **拉取 main 分支最新代码**

   - 点击分支名称 → **"Pull"**

3. **合并 dev 分支**

   - 命令面板 → **"Git: Merge Branch..."**
   - 选择 **"dev"** 分支
   - 解决冲突（如果有）

4. **推送 main 分支**
   - 推送到 gitlab：`git push gitlab main`
   - 推送到 GitHub：`git push origin main`

### 方法 2：在终端中合并（精确控制）

#### 合并 wjw 到 dev

```bash
# 1. 切换到 dev 分支
git checkout dev

# 2. 拉取 dev 最新代码
git pull gitlab dev

# 3. 合并 wjw 分支
git merge wjw

# 4. 解决冲突（如果有）
# ... 解决冲突 ...
git add .
git commit -m "merge: 合并 wjw 分支到 dev"

# 5. 推送到 gitlab
git push gitlab dev
```

#### 合并 dev 到 main

```bash
# 1. 切换到 main 分支
git checkout main

# 2. 拉取 main 最新代码
git pull gitlab main

# 3. 拉取 dev 最新代码
git pull gitlab dev

# 4. 合并 dev 分支
git merge gitlab/dev

# 5. 解决冲突（如果有）
# ... 解决冲突 ...

# 6. 推送到 gitlab
git push gitlab main

# 7. 同步到 GitHub
git push origin main
```

### 合并时的注意事项

1. **合并前确保代码是最新的**

   - 先拉取目标分支的最新代码
   - 确保本地分支也是最新的

2. **解决冲突时要仔细**

   - 不要盲目选择某一方的代码
   - 要理解两边的修改意图
   - 确保合并后的代码逻辑正确

3. **合并后要测试**
   - 确保代码能正常编译
   - 运行相关测试
   - 进行功能验证

### 使用 Merge Request（推荐方式）

**在 GitLab 网页上创建 Merge Request：**

1. 访问 GitLab 项目页面
2. 点击 **"Merge Requests"** → **"New merge request"**
3. 选择源分支（如 `wjw`）和目标分支（如 `dev`）
4. 填写 MR 描述
5. 提交 Merge Request
6. 等待代码审查（如果有）
7. 审查通过后合并

**优点：**

- 代码审查机制
- 记录合并历史
- 可以讨论和评论

---

## 十一、完整工作流程示例

### 方法 1：提交后立即推送

1. 提交成功后，可能会弹出提示
2. 点击 **"推送" (Push)** 按钮
3. 或在状态栏点击推送图标

### 方法 2：手动推送

1. 点击左下角状态栏的**分支名称**（如 "wjw"）
2. 选择 **"Push"** 或 **"Push to..."**
3. 选择目标远程分支（如 `gitlab/wjw`）

### 方法 3：使用命令面板

1. 按 **`Ctrl+Shift+P`** 打开命令面板
2. 输入 "Git: Push"
3. 选择推送选项

---

## 十二、完整工作流程示例

### 场景 1：在 wjw 分支开发并提交代码

**目标：** 在个人分支上开发功能，选择性提交文件

```
1. 切换到 wjw 分支（在 Cursor 中）
   - 点击左下角分支名称 → 选择 "wjw"
   - 或命令面板 → "Git: Checkout to..." → 选择 "wjw"

2. 拉取最新代码（在终端中）
   git pull gitlab wjw        # 拉取 wjw 分支最新代码
   git pull gitlab dev        # 拉取 dev 分支最新代码
   git merge gitlab/dev       # 合并 dev 到 wjw（如有冲突需解决）

3. 开发代码（在 Cursor 中）
   - 修改文件
   - 编写代码

4. 选择性提交文件（在 Cursor 中）
   - 打开 Source Control (Ctrl+Shift+G)
   - 展开 "更改" 部分
   - 逐个点击要提交的文件旁的 "+" 按钮
   - 确认 "暂存的更改" 部分只有要提交的文件
   - 填写提交信息：feat: 功能描述
   - 点击 "提交" 按钮（或按 Ctrl+Enter）

5. 推送到 gitlab wjw 分支（在终端中）
   git push gitlab wjw
```

### 场景 2：合并 wjw 到 dev 分支

**目标：** 将个人分支的代码合并到开发分支

```
1. 切换到 dev 分支（在 Cursor 中）
   - 点击分支名称 → 选择 "dev"

2. 拉取 dev 最新代码（在终端中）
   git pull gitlab dev

3. 合并 wjw 分支（在 Cursor 中）
   - 命令面板 (Ctrl+Shift+P) → "Git: Merge Branch..."
   - 选择 "wjw" 分支
   - 如果有冲突，按照"八、解决冲突"的方法处理

4. 推送 dev 分支（在终端中）
   git push gitlab dev
```

### 场景 3：合并 dev 到 main 并同步到 GitHub

**目标：** dev 分支测试通过后，合并到 main 并同步到 GitHub

```
1. 切换到 main 分支（在 Cursor 中）
   - 点击分支名称 → 选择 "main"

2. 拉取 main 最新代码（在终端中）
   git pull gitlab main
   git pull gitlab dev

3. 合并 dev 分支（在终端中）
   git merge gitlab/dev
   # 解决冲突（如果有）

4. 推送到 gitlab main（在终端中）
   git push gitlab main

5. 同步到 GitHub main（在终端中）
   git push origin main
```

### 场景 4：修改了 5 个文件，但只想提交 3 个

```
1. 打开 Source Control 面板 (Ctrl+Shift+G)

2. 展开 "更改" (Changes) 部分
   ├─ sci-z-server/.../UserServiceImpl.java [M]
   ├─ sci-z-web/.../UserProfile.vue [M]
   ├─ sci-z-server/.../UserController.java [M]
   ├─ sci-z-web/components.d.ts [M] ← 这是自动生成的，不提交
   └─ sci-z-web/auto-imports.d.ts [M] ← 这是自动生成的，不提交

3. 查看文件修改内容（可选）
   - 点击文件名查看修改内容
   - 确认修改是否正确

4. 选择性添加文件
   - 点击 UserServiceImpl.java 旁的 "+" 按钮 ✅
   - 点击 UserProfile.vue 旁的 "+" 按钮 ✅
   - 点击 UserController.java 旁的 "+" 按钮 ✅
   - 不点击 components.d.ts 和 auto-imports.d.ts 的 "+" 按钮 ❌

5. 查看 "暂存的更改" (Staged Changes) 部分
   - 确认只有上面3个文件

6. 填写提交信息
   - 在 "消息" 输入框中输入：feat: 用户管理功能优化

7. 提交
   - 点击绿色的 "提交" 按钮
   - 或按 Ctrl+Enter

8. 推送（如果需要）
   - 点击推送按钮
   - 或点击状态栏的分支名称 → Push
```

---

## 十三、状态栏信息解读

### 左下角状态栏显示：

- **`wjw*`** - 当前分支名称，`*` 表示有未提交的更改
- **云图标 + 数字** - Git 同步状态
  - `13 ↑ 54 ↓ 35` 表示：
    - 本地有 13 个未提交的更改
    - 有 54 个提交要推送（ahead）
    - 有 35 个提交要拉取（behind）

### 点击分支名称可以：

- 查看和切换分支
- 推送/拉取代码
- 创建新分支
- 查看提交历史

---

## 十四、常用快捷键

| 操作                | Windows/Linux  | Mac           |
| ------------------- | -------------- | ------------- |
| 打开 Source Control | `Ctrl+Shift+G` | `Cmd+Shift+G` |
| 提交                | `Ctrl+Enter`   | `Cmd+Enter`   |
| 查看文件修改        | 点击文件名     | 点击文件名    |
| 暂存文件            | 点击 "+" 按钮  | 点击 "+" 按钮 |
| 取消暂存            | 点击 "-" 按钮  | 点击 "-" 按钮 |
| 命令面板            | `Ctrl+Shift+P` | `Cmd+Shift+P` |

---

## 十五、最佳实践

### 1. 提交前检查清单

- ✅ 查看 "暂存的更改" 部分，确认文件列表正确
- ✅ 点击文件名查看修改内容，确认修改正确
- ✅ 排除自动生成的文件（components.d.ts, auto-imports.d.ts 等）
- ✅ 使用规范的提交信息格式

### 2. 避免全量提交

- ❌ **不要**点击 "更改" 标题旁的 "+" 按钮（会添加所有文件）
- ✅ **应该**逐个点击文件旁的 "+" 按钮
- ✅ **应该**仔细检查每个文件是否需要提交

### 3. 处理自动生成的文件

如果误添加了自动生成的文件：

- 立即在 "暂存的更改" 部分取消暂存
- 考虑将文件添加到 `.gitignore`：
  ```
  # 在项目根目录的 .gitignore 文件中添加：
  components.d.ts
  auto-imports.d.ts
  ```

### 4. 提交粒度建议

- **按功能提交**：一次提交一个完整功能
- **按文件类型提交**：后端和后端分开，前端和前端分开
- **避免混合提交**：不要在一个提交中混合多个不相关的修改

---

## 十六、常见问题

### Q1: 看不到 "暂存的更改" 部分？

**A:** 说明还没有文件被添加到暂存区。点击文件旁的 "+" 按钮后，这个部分会自动出现。

### Q2: 如何查看文件的修改内容？

**A:** 点击文件名，会在右侧打开对比视图，显示修改前后的差异。

### Q3: 提交后文件还在 "更改" 部分？

**A:** 说明这些文件没有被添加到暂存区，所以不会提交。如果不需要提交，可以保留在工作区。

### Q4: 如何撤销提交？

**A:**

- 如果是最后一次提交且还没推送：
  - 命令面板 → "Git: Undo Last Commit"
- 如果已经推送：
  - 需要创建新的提交来修复

### Q5: 如何只查看某个文件的修改？

**A:**

- 在文件列表中点击文件名
- 或右键选择 "Open Changes"
- 会在右侧编辑器打开对比视图

### Q6: 如何拉取指定分支的代码？

**A:**

- **方法 1（在 Cursor 中）：**
  - 先切换到目标分支：点击分支名称 → 选择分支
  - 然后拉取：点击分支名称 → "Pull"
- **方法 2（命令面板）：**
  - `Ctrl+Shift+P` → "Git: Pull" → 选择分支
- **方法 3（终端）：**
  - `git pull gitlab <分支名>`（如：`git pull gitlab dev`）

### Q7: Pull 代码时出现冲突怎么办？

**A:**

1. **查看冲突文件**：在 Source Control 面板查看带 "!" 图标的文件
2. **打开冲突文件**：点击文件名，在编辑器中打开
3. **解决冲突**：
   - 使用按钮选择：**"Accept Current Change"** / **"Accept Incoming Change"** / **"Accept Both Changes"**
   - 或手动编辑，删除冲突标记，保留需要的代码
4. **标记冲突已解决**：点击冲突文件旁的 "+" 按钮
5. **完成合并**：填写提交信息，点击 "提交" 按钮

**详细步骤参考 "八、解决冲突" 章节**

### Q8: 如何推送代码到远程分支？

**A:**

- **方法 1（在 Cursor 中）：**
  - 提交后可能弹出推送提示，点击 "Push"
  - 或点击分支名称 → "Push"
  - 或命令面板 → "Git: Push"
- **方法 2（终端）：**
  - `git push gitlab <分支名>`（如：`git push gitlab wjw`）
  - `git push origin <分支名>`（如：`git push origin main`）

### Q9: 如何合并代码？

**A:**

- **方法 1（在 Cursor 中）：**

  1. 切换到目标分支（如 `dev`）
  2. 命令面板 → "Git: Merge Branch..."
  3. 选择要合并的分支（如 `wjw`）
  4. 解决冲突（如果有）
  5. 推送合并结果

- **方法 2（终端）：**
  ```bash
  git checkout dev
  git pull gitlab dev
  git merge wjw
  # 解决冲突（如果有）
  git push gitlab dev
  ```

**详细步骤参考 "十、合并代码" 章节**

### Q10: 合并时出现冲突，如何取消合并？

**A:**

- **在终端中执行：**
  ```bash
  git merge --abort
  ```
- 这会取消当前的合并操作，恢复到合并前的状态

### Q11: 状态栏显示箭头是什么意思？

**A:**

- **向上箭头 (↑)**：本地分支有新的提交需要推送（ahead）
- **向下箭头 (↓)**：远程分支有新的提交需要拉取（behind）
- **无箭头**：本地和远程已同步

### Q12: 如何查看分支之间的差异？

**A:**

- **在 Cursor 中：**
  - 点击 Source Control 面板中的 "图形" (Graph) 部分
  - 查看提交历史的可视化图形
- **在终端中：**

  ```bash
  # 查看 wjw 和 dev 的差异
  git diff dev..wjw

  # 查看将要合并的提交
  git log dev..wjw
  ```

### Q13: 如何将本地代码推送到不同的远程分支？

**A:**

- **推送到 GitLab：**
  ```bash
  git push gitlab <本地分支名>:<远程分支名>
  # 例如：git push gitlab wjw:dev
  ```
- **推送到 GitHub：**
  ```bash
  git push origin <本地分支名>:<远程分支名>
  # 例如：git push origin main:main
  ```
- **在 Cursor 中：**
  - 命令面板 → "Git: Push to..." → 选择远程仓库和分支

---

## 十七、界面说明

### Source Control 面板布局

```
Source Control
├─ [消息输入框] "消息(Ctrl+在"wjw"提交)"
├─ [提交按钮] ✓ 提交
├─ > 更改 (Changes) [1]        ← 点击展开查看所有修改
│  ├─ file1.java [M] [+]       ← 点击 + 添加
│  └─ file2.vue [M] [+]
├─ > 暂存的更改 (Staged) [2]    ← 将要提交的文件
│  ├─ file3.java [-]           ← 点击 - 取消暂存
│  └─ file4.vue [-]
└─ > 图形 (Graph)              ← 提交历史图形化显示
```

### 文件状态图标说明

- `[M]` = Modified（修改）
- `[A]` = Added（新增）
- `[D]` = Deleted（删除）
- `[U]` = Untracked（未跟踪）
- `[+]` = 添加到暂存区按钮
- `[-]` = 取消暂存按钮

---

## 十八、项目实际工作流程总结

### 分支结构

```
gitlab (公司 GitLab):
├── main     - 主分支（生产环境，稳定版本）
├── dev      - 开发分支（测试环境，所有功能在此验证）
├── wjw      - 个人开发分支（您的分支）
└── shang    - 另一位开发人员的分支

origin (个人 GitHub):
└── main     - 主分支（与 gitlab/main 同步）
```

### 完整工作流程

#### 日常开发流程（在 wjw 分支）

```
1. 切换到 wjw 分支
   git checkout wjw

2. 同步最新代码
   git pull gitlab wjw          # 拉取 wjw 最新代码
   git pull gitlab dev          # 拉取 dev 最新代码（dev可能有新功能）
   git merge gitlab/dev         # 合并 dev 到 wjw（如有冲突需解决）

3. 开发代码（在 Cursor 中）
   - 修改文件
   - 编写代码

4. 选择性提交代码（在 Cursor 中）
   - 打开 Source Control (Ctrl+Shift+G)
   - 展开 "更改" 部分
   - 逐个点击要提交的文件旁的 "+" 按钮
   - 填写提交信息：feat: 功能描述
   - 点击 "提交" 按钮

5. 推送到 gitlab wjw 分支
   git push gitlab wjw
```

#### 合并到 dev 分支

```
1. 切换到 dev 分支
   git checkout dev

2. 拉取 dev 最新代码
   git pull gitlab dev

3. 合并 wjw 分支
   git merge gitlab/wjw
   # 解决冲突（如果有）

4. 推送 dev 分支
   git push gitlab dev
```

#### 合并到 main 分支（dev 测试通过后）

```
1. 切换到 main 分支
   git checkout main

2. 拉取 main 和 dev 最新代码
   git pull gitlab main
   git pull gitlab dev

3. 合并 dev 分支
   git merge gitlab/dev
   # 解决冲突（如果有）

4. 推送 gitlab main
   git push gitlab main

5. 同步到 GitHub main
   git push origin main
```

### 关键操作总结

| 操作           | Cursor 方式             | 终端方式                 |
| -------------- | ----------------------- | ------------------------ |
| **查看修改**   | Source Control 面板     | `git status`             |
| **选择性添加** | 点击文件旁的 "+" 按钮   | `git add <文件>`         |
| **提交代码**   | 填写消息 + 点击提交按钮 | `git commit -m "消息"`   |
| **拉取代码**   | 分支名称 → Pull         | `git pull gitlab <分支>` |
| **解决冲突**   | 冲突文件 → 使用按钮选择 | 手动编辑文件             |
| **推送代码**   | 分支名称 → Push         | `git push gitlab <分支>` |
| **合并分支**   | 命令面板 → Merge Branch | `git merge <分支>`       |

### 注意事项

1. **开发前必须同步 dev 分支**：每次开发前都要拉取 dev 最新代码并合并到 wjw
2. **选择性提交文件**：不要使用 `git add -A`，要逐个添加文件
3. **dev 验证通过才能合并到 main**：确保 dev 分支代码没有 bug 后再合并到 main
4. **及时同步到 GitHub**：main 分支更新后，立即同步到个人 GitHub
5. **解决冲突时要仔细**：理解两边的修改意图，确保合并后的代码正确

### 常用命令速查

```bash
# 查看状态
git status                    # 查看修改
git branch -a                 # 查看所有分支
git remote -v                 # 查看远程仓库

# 拉取代码
git pull gitlab wjw          # 拉取 gitlab wjw 分支
git pull gitlab dev          # 拉取 gitlab dev 分支
git fetch gitlab             # 只拉取远程信息，不合并

# 合并代码
git merge gitlab/dev         # 合并 dev 到当前分支

# 提交代码
git add <文件>               # 添加单个文件
git commit -m "消息"         # 提交代码
git push gitlab wjw          # 推送到 gitlab wjw

# 解决冲突
git merge --abort            # 取消合并
git status                   # 查看冲突文件
```

---

**文档更新时间：** 2025-01-XX  
**维护人：** wjw  
**适用项目：** Sci-Z-Platform-College
