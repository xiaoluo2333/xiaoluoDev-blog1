/**
 * ============================================
 * 喵～ 开发过程自我问答
 * ============================================
 * Q: 为什么要封装 Cookie 工具？
 * A: 虽然 Cookie 是 HttpOnly 的（JS 不能直接读写），
 *    但在某些场景下还是需要在前端判断登录状态。
 *    比如检查是否存在某个 Cookie 来快速判断用户是否登录，
 *    或者在登出时清理前端状态喵～
 *
 * Q: Token 为什么不存 localStorage？
 * A: 后端用的是 HttpOnly Cookie，浏览器会自动携带，
 *    比 localStorage 更安全，能有效防止 XSS 攻击。
 *    前端完全不需要手动管理 token，省心喵！
 * ============================================
 */

/**
 * 获取指定名称的 Cookie 值
 * 虽然 HttpOnly Cookie 无法通过 JS 读取，
 * 但这个方法可以用来读取非 HttpOnly 的 Cookie
 */
export function getCookie(name: string): string | null {
  const cookies = document.cookie.split('; ');
  for (const cookie of cookies) {
    const [key, value] = cookie.split('=');
    if (key === name) {
      return decodeURIComponent(value);
    }
  }
  return null;
}

/**
 * 设置 Cookie
 * 注意：HttpOnly 属性的 Cookie 无法通过 JS 设置，
 * 这个方法用于设置非敏感的前端 Cookie
 */
export function setCookie(name: string, value: string, days = 7): void {
  const expires = new Date();
  expires.setDate(expires.getDate() + days);
  document.cookie = `${name}=${encodeURIComponent(value)}; path=/; expires=${expires.toUTCString()}; SameSite=Lax`;
}

/**
 * 删除 Cookie
 */
export function removeCookie(name: string): void {
  document.cookie = `${name}=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Lax`;
}