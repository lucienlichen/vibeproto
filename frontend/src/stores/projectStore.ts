import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import { archiveProject, createProject, deleteProject, fetchProjects, updateProject } from '@/api/project'
import type { Project, ProjectFormPayload, ProjectUpdatePayload } from '@/types/project'

export const useProjectStore = defineStore('project', () => {
  const projects = ref<Project[]>([])
  const loading = ref(false)
  const pagination = reactive({
    pageNum: 1,
    pageSize: 10,
    total: 0
  })
  const filters = reactive({
    name: '',
    projectType: '',
    status: ''
  })

  async function loadProjects() {
    loading.value = true
    try {
      const response = await fetchProjects({
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        name: filters.name || undefined,
        projectType: filters.projectType || undefined,
        status: filters.status || undefined
      })
      projects.value = response.data.records
      pagination.total = response.data.total
      pagination.pageNum = response.data.pageNum
      pagination.pageSize = response.data.pageSize
    } finally {
      loading.value = false
    }
  }

  async function createProjectAction(payload: ProjectFormPayload) {
    await createProject(payload)
    await loadProjects()
  }

  async function updateProjectAction(id: number, payload: ProjectUpdatePayload) {
    await updateProject(id, payload)
    await loadProjects()
  }

  async function deleteProjectAction(id: number) {
    await deleteProject(id)
    await loadProjects()
  }

  async function archiveProjectAction(id: number) {
    await archiveProject(id)
    await loadProjects()
  }

  function resetFilters() {
    filters.name = ''
    filters.projectType = ''
    filters.status = ''
    pagination.pageNum = 1
  }

  return {
    projects,
    loading,
    pagination,
    filters,
    loadProjects,
    createProjectAction,
    updateProjectAction,
    deleteProjectAction,
    archiveProjectAction,
    resetFilters
  }
})
