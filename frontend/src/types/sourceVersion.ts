export type SourceType = 'zip' | 'git' | 'html'

export interface SourceVersion {
  id: number
  projectId: number
  versionNo: string
  sourceType: SourceType
  sourceName: string
  filePath: string | null
  gitUrl: string | null
  gitBranch: string | null
  commitHash: string | null
  htmlContentPath: string | null
  remark: string | null
  createdBy: number | null
  createdAt: string
}

export interface SourceVersionHtmlPayload {
  projectId: number
  sourceName: string
  htmlContent: string
  remark: string
}

export interface SourceVersionGitPayload {
  projectId: number
  gitUrl: string
  gitBranch: string
  commitHash: string
  remark: string
}
