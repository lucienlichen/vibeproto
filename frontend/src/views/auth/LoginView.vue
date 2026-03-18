<template>
  <div class="login-page">
    <div class="login-card">
      <div class="intro">
        <p class="eyebrow">VibeCoding Workflow</p>
        <h1>原型项目管理与构建发布平台</h1>
        <p class="desc">统一管理原型、版本、构建和 latest 访问地址。</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" size="large" @keyup.enter="handleSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="submitting" @click="handleSubmit">
          登录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  username: 'admin',
  password: 'Admin@123'
})

const rules: FormRules<typeof form> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    await userStore.loginAction(form)
    ElMessage.success('登录成功')
    router.push('/projects')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 24px;
}

.login-card {
  width: min(100%, 420px);
  padding: 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12);
}

.intro {
  margin-bottom: 28px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.16em;
}

h1 {
  margin: 0 0 10px;
  font-size: 30px;
  line-height: 1.2;
}

.desc {
  margin: 0;
  color: #6b7280;
}

.submit {
  width: 100%;
}
</style>
