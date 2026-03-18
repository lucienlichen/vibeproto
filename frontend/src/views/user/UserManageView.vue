<template>
  <div>
    <div class="toolbar">
      <span class="title">用户管理</span>
      <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
    </div>

    <el-table :data="users" v-loading="loading" border style="width: 100%">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="roleName" label="角色" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
            {{ row.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" @click="openPasswordDialog(row)">重置密码</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadUsers"
      />
    </div>

    <!-- Create Dialog -->
    <el-dialog v-model="createVisible" title="新增用户" width="480px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="createForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="createForm.password" type="password" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="createForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="createForm.roleCode" style="width: 100%">
            <el-option v-for="r in roles" :key="r.roleCode" :label="r.roleName" :value="r.roleCode" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog v-model="editVisible" title="编辑用户" width="480px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.roleCode" style="width: 100%">
            <el-option v-for="r in roles" :key="r.roleCode" :label="r.roleName" :value="r.roleCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleEdit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Password Dialog -->
    <el-dialog v-model="passwordVisible" title="重置密码" width="400px">
      <el-form label-width="80px">
        <el-form-item label="新密码">
          <el-input v-model="newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchUsers, createUser, updateUser, deleteUser, changePassword, fetchRoles } from '@/api/user'
import type { SysUser, SysRole } from '@/types/user'

const loading = ref(false)
const submitting = ref(false)
const users = ref<SysUser[]>([])
const roles = ref<SysRole[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const createVisible = ref(false)
const editVisible = ref(false)
const passwordVisible = ref(false)

const createForm = ref({ username: '', password: '', nickname: '', email: '', roleCode: '' })
const editForm = ref({ nickname: '', email: '', roleCode: '', status: '' })
const editingId = ref<number | null>(null)
const passwordUserId = ref<number | null>(null)
const newPassword = ref('')

async function loadUsers() {
  loading.value = true
  try {
    const res = await fetchUsers(pageNum.value, pageSize.value)
    users.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  const res = await fetchRoles()
  roles.value = res.data
}

function openCreateDialog() {
  createForm.value = { username: '', password: '', nickname: '', email: '', roleCode: roles.value[0]?.roleCode || '' }
  createVisible.value = true
}

function openEditDialog(row: SysUser) {
  editingId.value = row.id
  editForm.value = { nickname: row.nickname, email: row.email || '', roleCode: row.roleCode || '', status: row.status }
  editVisible.value = true
}

function openPasswordDialog(row: SysUser) {
  passwordUserId.value = row.id
  newPassword.value = ''
  passwordVisible.value = true
}

async function handleCreate() {
  submitting.value = true
  try {
    await createUser(createForm.value)
    ElMessage.success('用户创建成功')
    createVisible.value = false
    loadUsers()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

async function handleEdit() {
  if (!editingId.value) return
  submitting.value = true
  try {
    await updateUser(editingId.value, editForm.value)
    ElMessage.success('更新成功')
    editVisible.value = false
    loadUsers()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '更新失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: SysUser) {
  await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '删除确认', {
    type: 'warning'
  })
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}

async function handleChangePassword() {
  if (!passwordUserId.value || !newPassword.value) return
  submitting.value = true
  try {
    await changePassword(passwordUserId.value, newPassword.value)
    ElMessage.success('密码重置成功')
    passwordVisible.value = false
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '重置失败')
  } finally {
    submitting.value = false
  }
}

function formatDate(val: string) {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 19)
}

onMounted(async () => {
  await loadRoles()
  await loadUsers()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
