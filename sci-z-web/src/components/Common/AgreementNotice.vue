<!--
  @component AgreementNotice
  @description 通用协议勾选提示组件，用于展示并触发查看《用户协议》《隐私政策》
-->
<template>
  <div class="agreement-notice">
    <el-checkbox :model-value="modelValue" @change="handleChange">
      {{ $t('legal.agreePrefix') }}
      <button class="link-button" type="button" @click="$emit('view-user-agreement')">
        {{ $t('legal.userAgreement') }}
      </button>
      {{ $t('legal.and') }}
      <button class="link-button" type="button" @click="$emit('view-privacy-policy')">
        {{ $t('legal.privacyPolicy') }}
      </button>
    </el-checkbox>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'view-user-agreement', 'view-privacy-policy'])

const handleChange = (val) => {
  emit('update:modelValue', val)
}
</script>

<style scoped lang="scss">
.agreement-notice {
  display: flex;
  align-items: center;

  :deep(.el-checkbox__label) {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 4px;
    color: #475569;
  }
}

.link-button {
  border: none;
  background: transparent;
  color: #1e3a8a;
  cursor: pointer;
  font-weight: 600;
  padding: 0;

  &:hover {
    text-decoration: underline;
  }
}
</style>

