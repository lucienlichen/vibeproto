export interface ReleaseVersion {
  id: number
  projectId: number
  sourceVersionId: number
  buildTaskId: number
  versionNo: string
  releasePath: string
  previewUrl: string
  isCurrent: number
  releasedBy: number | null
  createdAt: string
}
