<!--
/**
 * @description 组件测试页面
 * 用于验证所有基础组件是否正常工作
 * 测试完成后可以删除此页面
 */
-->
<template>
  <div class="component-test">
    <div class="test-header">
      <h1>组件库测试页面</h1>
      <p>验证所有基础组件是否正常工作</p>
    </div>

    <!-- 按钮组件测试 -->
    <BaseCard title="按钮组件测试">
      <div class="test-section">
        <h3>按钮类型</h3>
        <div class="button-group">
          <BaseButton type="default">默认按钮</BaseButton>
          <BaseButton type="primary">主要按钮</BaseButton>
          <BaseButton type="success">成功按钮</BaseButton>
          <BaseButton type="warning">警告按钮</BaseButton>
          <BaseButton type="danger">危险按钮</BaseButton>
          <BaseButton type="info">信息按钮</BaseButton>
          <BaseButton type="text">文本按钮</BaseButton>
        </div>

        <h3>按钮尺寸</h3>
        <div class="button-group">
          <BaseButton size="large" type="primary">大按钮</BaseButton>
          <BaseButton size="default" type="primary">默认按钮</BaseButton>
          <BaseButton size="small" type="primary">小按钮</BaseButton>
        </div>

        <h3>按钮状态</h3>
        <div class="button-group">
          <BaseButton type="primary" :loading="true">加载中</BaseButton>
          <BaseButton type="primary" :disabled="true">禁用状态</BaseButton>
          <BaseButton type="primary" block>块级按钮</BaseButton>
        </div>
      </div>
    </BaseCard>

    <!-- 卡片组件测试 -->
    <BaseCard title="卡片组件测试">
      <div class="test-section">
        <h3>基础卡片</h3>
        <BaseCard title="基础卡片" hoverable>
          <p>这是一个基础卡片的内容</p>
        </BaseCard>

        <h3>带操作区的卡片</h3>
        <BaseCard title="带操作区的卡片">
          <template #actions>
            <BaseButton size="small" type="primary">编辑</BaseButton>
            <BaseButton size="small" type="danger">删除</BaseButton>
          </template>
          <p>这个卡片有操作按钮</p>
        </BaseCard>

        <h3>带底部的卡片</h3>
        <BaseCard title="带底部的卡片">
          <p>这个卡片有底部内容</p>
          <template #footer>
            <div style="text-align: right;">
              <BaseButton size="small">取消</BaseButton>
              <BaseButton size="small" type="primary">确定</BaseButton>
            </div>
          </template>
        </BaseCard>
      </div>
    </BaseCard>

    <!-- 弹窗组件测试 -->
    <BaseCard title="弹窗组件测试">
      <div class="test-section">
        <div class="button-group">
          <BaseButton type="primary" @click="showDefaultDialog = true">默认弹窗</BaseButton>
          <BaseButton type="primary" @click="showFormDialog = true">表单弹窗</BaseButton>
          <BaseButton type="primary" @click="showConfirmDialog = true">确认弹窗</BaseButton>
        </div>
      </div>
    </BaseCard>

    <!-- 表格组件测试 -->
    <BaseCard title="表格组件测试">
      <div class="test-section">
        <BaseTable
          :data="tableData"
          :columns="tableColumns"
          :loading="tableLoading"
          :selectable="true"
          :show-index="true"
          :pagination="pagination"
          @selection-change="handleSelectionChange"
        >
          <template #actions>
            <BaseButton size="small" type="primary">编辑</BaseButton>
            <BaseButton size="small" type="danger">删除</BaseButton>
          </template>
        </BaseTable>
      </div>
    </BaseCard>

    <!-- 分页组件测试 -->
    <BaseCard title="分页组件测试">
      <div class="test-section">
        <BasePagination
          v-model:current="pagination.current"
          v-model:size="pagination.size"
          :total="pagination.total"
          @change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </BaseCard>

    <!-- 弹窗组件 -->
    <BaseDialog
      v-model="showDefaultDialog"
      title="默认弹窗"
      width="500px"
    >
      <p>这是一个默认弹窗的内容</p>
      <template #footer>
        <BaseButton @click="showDefaultDialog = false">取消</BaseButton>
        <BaseButton type="primary" @click="showDefaultDialog = false">确定</BaseButton>
      </template>
    </BaseDialog>

    <BaseDialog
      v-model="showFormDialog"
      title="表单弹窗"
      type="form"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <BaseButton @click="showFormDialog = false">取消</BaseButton>
        <BaseButton type="primary" @click="showFormDialog = false">确定</BaseButton>
      </template>
    </BaseDialog>

    <BaseDialog
      v-model="showConfirmDialog"
      title="确认弹窗"
      type="confirm"
      width="400px"
    >
      <p>确定要执行此操作吗？</p>
      <template #footer>
        <BaseButton @click="showConfirmDialog = false">取消</BaseButton>
        <BaseButton type="primary" @click="showConfirmDialog = false">确定</BaseButton>
      </template>
    </BaseDialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { BaseButton, BaseCard, BaseDialog, BaseTable, BasePagination } from '@/components/Common/index.js'

// 弹窗状态
const showDefaultDialog = ref(false)
const showFormDialog = ref(false)
const showConfirmDialog = ref(false)

// 表格数据
const tableLoading = ref(false)
const tableData = ref([
  { id: 1, name: '张三', email: 'zhangsan@example.com', status: '正常' },
  { id: 2, name: '李四', email: 'lisi@example.com', status: '正常' },
  { id: 3, name: '王五', email: 'wangwu@example.com', status: '禁用' }
])

const tableColumns = ref([
  { prop: 'name', label: '姓名', width: 120 },
  { prop: 'email', label: '邮箱', minWidth: 200 },
  { prop: 'status', label: '状态', width: 100 }
])

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 100
})

// 表单数据
const form = reactive({
  name: '',
  email: ''
})

// 事件处理
const handleSelectionChange = (selection) => {
  console.log('选中的行:', selection)
}

const handlePageChange = (page) => {
  console.log('页码变化:', page)
  pagination.current = page
}

const handleSizeChange = (size) => {
  console.log('每页数量变化:', size)
  pagination.size = size
}
</script>

<style lang="scss" scoped>
.component-test {
  padding: var(--gap-lg);
  background: var(--bg);
  min-height: 100vh;
}

.test-header {
  text-align: center;
  margin-bottom: var(--gap-2xl);
  
  h1 {
    color: var(--color-primary);
    margin-bottom: var(--gap-sm);
  }
  
  p {
    color: var(--text-2);
  }
}

.test-section {
  margin-bottom: var(--gap-2xl);
  
  h3 {
    color: var(--text-2);
    margin-bottom: var(--gap-md);
    font-size: var(--font-size-lg);
  }
}

.button-group {
  display: flex;
  flex-wrap: wrap;
  gap: var(--gap-md);
  margin-bottom: var(--gap-lg);
}
</style>
