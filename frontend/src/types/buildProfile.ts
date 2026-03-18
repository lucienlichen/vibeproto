export interface BuildProfile {
  id: number
  projectId: number | null
  profileName: string
  nodeVersion: string
  installCommand: string
  buildCommand: string
  outputDir: string
  envJson: string | null
  isDefault: number
  enabled: number
  createdBy: number | null
  createdAt: string
  updatedAt: string
}

export interface BuildProfileCreatePayload {
  projectId: number
  profileName: string
  nodeVersion: string
  installCommand: string
  buildCommand: string
  outputDir: string
  envJson: string
  isDefault: number
  enabled: number
}

export interface BuildProfileUpdatePayload {
  profileName: string
  nodeVersion: string
  installCommand: string
  buildCommand: string
  outputDir: string
  envJson: string
  isDefault: number
  enabled: number
}
