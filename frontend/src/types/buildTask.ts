export type BuildStatus = 'pending' | 'running' | 'success' | 'failed' | 'canceled'

export interface BuildTask {
  id: number
  projectId: number
  sourceVersionId: number
  buildProfileId: number
  taskNo: string
  status: BuildStatus
  logPath: string | null
  startTime: string | null
  endTime: string | null
  durationMs: number | null
  errorMessage: string | null
  triggeredBy: number | null
  createdAt: string
  projectName: string | null
  sourceVersionNo: string | null
  buildProfileName: string | null
}

export interface BuildTaskCreatePayload {
  projectId: number
  sourceVersionId: number
  buildProfileId: number
}
