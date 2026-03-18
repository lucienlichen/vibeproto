export interface UserInfo {
  id: number
  username: string
  nickname: string
  roleCode: string | null
}

export interface SysUser {
  id: number
  username: string
  nickname: string
  email: string | null
  status: string
  roleCode: string | null
  roleName: string | null
  createdAt: string
}

export interface SysRole {
  id: number
  roleCode: string
  roleName: string
}

export interface UserCreatePayload {
  username: string
  password: string
  nickname: string
  email?: string
  roleCode: string
}

export interface UserUpdatePayload {
  nickname?: string
  email?: string
  status?: string
  roleCode?: string
}
