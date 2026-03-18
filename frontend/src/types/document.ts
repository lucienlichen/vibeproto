export type DocType = 'prd' | 'page_spec' | 'interaction_spec' | 'tech_note'

export interface ProjectDocument {
  id: number
  projectId: number
  docType: DocType
  title: string
  content: string
  relatedReleaseId: number | null
  createdBy: number | null
  createdAt: string
  updatedAt: string
}

export interface ProjectDocumentCreatePayload {
  projectId: number
  docType: DocType
  title: string
  content: string
  relatedReleaseId: number | null
}

export interface ProjectDocumentUpdatePayload {
  docType: DocType
  title: string
  content: string
  relatedReleaseId: number | null
}
