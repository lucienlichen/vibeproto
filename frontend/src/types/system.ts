export interface SystemConfig {
  id: number
  configKey: string
  configValue: string
  description: string | null
  updatedAt: string
}

export interface SystemConfigUpdatePayload {
  items: Array<{
    configKey: string
    configValue: string
  }>
}
