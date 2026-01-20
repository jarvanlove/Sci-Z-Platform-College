<!--
  @component AgreementNotice
  @description 通用协议勾选提示组件，用于展示并触发查看《用户协议》《隐私政策》
  @props isSmsLogin - 是否为短信登录，短信登录时显示不同的文字描述
-->
<template>
  <div class="agreement-notice">
    <el-checkbox :model-value="modelValue" @change="handleChange">
      <template v-if="isSmsLogin">
        {{ $t('legal.smsAgreePrefix') }}
        <button class="link-button" type="button" @click="$emit('view-user-agreement')">
          {{ $t('legal.userAgreement') }}
        </button>
        {{ $t('legal.and') }}
        <button class="link-button" type="button" @click="$emit('view-privacy-policy')">
          {{ $t('legal.privacyPolicy') }}
        </button>
      </template>
      <template v-else>
        {{ $t('legal.agreePrefix') }}
        <button class="link-button" type="button" @click="$emit('view-user-agreement')">
          {{ $t('legal.userAgreement') }}
        </button>
        {{ $t('legal.and') }}
        <button class="link-button" type="button" @click="$emit('view-privacy-policy')">
          {{ $t('legal.privacyPolicy') }}
        </button>
      </template>
    </el-checkbox>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  isSmsLogin: {
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
  align-items: flex-start;
  line-height: 1.6;

  :deep(.el-checkbox) {
    align-items: flex-start;
  }

  :deep(.el-checkbox__label) {
    display: inline-block;
    line-height: 1.6;
    color: #475569;
    font-size: 13px;
    word-break: break-word;
    white-space: normal;
  }

  :deep(.el-checkbox__input) {
    margin-top: 2px;
  }
}

.link-button {
  border: none;
  background: transparent;
  color: #1e3a8a;
  cursor: pointer;
  font-weight: 600;
  padding: 0;
  display: inline;
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}
</style>

