/**
 * ============================================
 * 喵～ 开发过程自我问答
 * ============================================
 * Q: 为什么需要定义类型？
 * A: TypeScript 的类型系统能在编译前发现数据结构不匹配的问题。
 *    比如 API 返回的字段名拼错了，或者少传了必填参数，
 *    类型检查会直接报错，不用等到运行时才发现喵！
 *
 * Q: 这些类型和 Java 后端的 DTO 是什么关系？
 * A: 前端类型和后端 DTO 是一一对应的。
 *    后端 ApiResponse<T>、PostResponse、LoginRequest 等，
 *    在前端都有对应的 TypeScript 接口。
 *    这样就保证了前后端数据结构的一致性喵～
 * ============================================
 */

/** 后端统一的 API 响应格式 */
export interface ApiResponse<T = unknown> {
  success: boolean;
  message: string;
  data: T;
}

/** 登录请求体 */
export interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
}

/** 登录响应数据 */
export interface LoginResponse {
  token: string;
  username: string;
  expiresIn: number;
}

/** 注册请求体 */
export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
}

/** 文章响应数据 */
export interface PostResponse {
  id: number;
  title: string;
  slug: string;
  content: string;
  summary: string;
  tags: string;
  status: 'PUBLISHED' | 'DRAFT';
  authorName: string;
  createdAt: string;
  updatedAt: string;
}

/** 文章创建/更新请求体 */
export interface PostRequest {
  title: string;
  slug?: string;
  content: string;
  summary?: string;
  tags?: string;
  status: 'PUBLISHED' | 'DRAFT';
}

/** 文件/文件夹响应数据 */
export interface FileResponse {
  id: number;
  fileName: string;
  filePath: string;
  fileSize: number;
  contentType: string;
  isFolder: boolean;
  createdAt: string;
}

/** 创建文件夹请求体 */
export interface CreateFolderRequest {
  name: string;
  parentId: number | null;
}

/** 插件信息 */
export interface PluginInfo {
  id: number;
  pluginId: string;
  pluginName: string;
  version: string;
  author: string;
  description: string;
  enabled: boolean;
}

/** 分页数据 */
export interface PageData<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

/** 用户信息 */
export interface UserInfo {
  id: number;
  username: string;
  email: string;
  admin: boolean;
}

/** 文章管理表单数据（用于编辑弹窗） */
export interface PostFormData {
  title: string;
  slug: string;
  content: string;
  summary: string;
  tags: string;
  status: 'PUBLISHED' | 'DRAFT';
}