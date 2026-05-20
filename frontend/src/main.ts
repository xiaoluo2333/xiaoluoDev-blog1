/**
 * ============================================
 * 喵～ 开发过程自我问答
 * ============================================
 * Q: 为什么要用动态导入加载页面模块？
 * A: 虽然所有页面代码都在同一个 bundle 里，
 *    但用动态导入可以在未来做代码分割，
 *    让每个页面只加载自己需要的代码喵～
 *
 * Q: 入口文件的核心职责是什么？
 * A: 1. 检测当前页面 URL，判断加载哪个 Vue App
 *    2. 导入全局样式 (main.css)
 *    3. 调用对应页面的初始化函数
 *    4. 提供一些全局可用的工具函数
 * ============================================
 */

/* 导入全局基础样式和所有页面样式 */
import '../css/main.css';
import '../css/index.css';
import '../css/login.css';
import '../css/post.css';
import '../css/admin.css';

/**
 * 检测当前页面类型，返回对应的页面名称
 * 根据 window.location.pathname 判断：
 * - / 或 /index → index
 * - /login → login
 * - /post/* → post
 * - /admin → admin
 */
function detectPage(): string {
  const path = window.location.pathname;

  if (path === '/' || path === '/index') {
    return 'index';
  }
  if (path === '/login') {
    return 'login';
  }
  if (path.startsWith('/post/')) {
    return 'post';
  }
  if (path === '/admin') {
    return 'admin';
  }
  return 'index';
}

/**
 * 应用入口
 * 检测当前页面后，动态导入对应的页面模块并初始化
 */
async function main(): Promise<void> {
  const page = detectPage();

  try {
    switch (page) {
      case 'index':
        const indexModule = await import('./pages/index');
        indexModule.initIndexPage();
        break;

      case 'login':
        const loginModule = await import('./pages/login');
        loginModule.initLoginPage();
        break;

      case 'post':
        const postModule = await import('./pages/post');
        postModule.initPostPage();
        break;

      case 'admin':
        const adminModule = await import('./pages/admin');
        adminModule.initAdminPage();
        break;

      default:
        const defaultModule = await import('./pages/index');
        defaultModule.initIndexPage();
    }
  } catch (err) {
    console.error('喵呜… 页面初始化失败:', err);
  }
}

/* DOM 加载完成后启动应用 */
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', main);
} else {
  main();
}