/**
 * ============================================
 * 喵～ 开发过程自我问答
 * ============================================
 * Q: 为什么封装 fetch 而不是直接用 axios？
 * A: 项目只有一个简单的博客系统，不需要拦截器、请求取消、
 *    转换器等复杂功能。用原生 fetch 封装一层就够了，
 *    少一个依赖，包体积更小喵～
 *
 * Q: 为什么要自动携带 Cookie？
 * A: 后端认证用的是 HttpOnly Cookie（X-User-Token / X-Admin-Token），
 *    浏览器会在同源请求中自动携带这些 Cookie。
 *    但 fetch 默认行为是 same-origin 才带，
 *    我们设置 credentials: 'include' 确保跨域也能带喵！
 * ============================================
 */

/**
 * 基础 GET 请求
 * 自动携带 Cookie，自动解析 JSON 响应
 */
export async function get<T>(url: string): Promise<T> {
  const response = await fetch(url, {
    method: 'GET',
    credentials: 'include',
    headers: {
      'Accept': 'application/json'
    }
  });
  return response.json();
}

/**
 * 基础 POST 请求
 * 自动携带 Cookie，自动序列化 JSON body
 */
export async function post<T>(url: string, body?: unknown): Promise<T> {
  const response = await fetch(url, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    body: body ? JSON.stringify(body) : undefined
  });
  return response.json();
}

/**
 * 基础 PUT 请求
 * 用于更新资源
 */
export async function put<T>(url: string, body?: unknown): Promise<T> {
  const response = await fetch(url, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    body: body ? JSON.stringify(body) : undefined
  });
  return response.json();
}

/**
 * 基础 DELETE 请求
 * 用于删除资源
 */
export async function del<T>(url: string): Promise<T> {
  const response = await fetch(url, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      'Accept': 'application/json'
    }
  });
  return response.json();
}

/**
 * 上传文件（multipart/form-data）
 * 不需要手动设置 Content-Type，浏览器会自动设置
 */
export async function uploadFile<T>(url: string, formData: FormData): Promise<T> {
  const response = await fetch(url, {
    method: 'POST',
    credentials: 'include',
    body: formData
  });
  return response.json();
}