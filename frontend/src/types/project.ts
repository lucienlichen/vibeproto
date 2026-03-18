export type ProjectType = 'html' | 'vue' | 'react' | 'other'
export type ProjectStatus = 'draft' | 'active' | 'archived'

export interface Project {
  id: number
  name: string
  code: string
  description: string
  projectType: ProjectType
  status: ProjectStatus
  defaultBuildProfileId: number
  currentReleaseId: number | null
  ownerId: number | null
  createdBy: number | null
  currentVersion: string
  createdAt: string
  updatedAt: string
}

export interface ProjectPage {
  total: number
  pageNum: number
  pageSize: number
  records: Project[]
}

export interface ProjectFormPayload {
  name: string
  code: string
  description: string
  projectType: ProjectType
  defaultBuildProfileId: number
}

export interface ProjectUpdatePayload {
  name: string
  description: string
  projectType: ProjectType
  defaultBuildProfileId: number
  status: ProjectStatus
}

export interface ProjectPreviewInfo {
  projectId: number
  projectCode: string
  latestUrl: string
  currentReleaseUrl: string | null
}
