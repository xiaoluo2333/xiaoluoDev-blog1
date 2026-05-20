/**
 * ============================================
 * 喵～ 开发过程自我问答
 * ============================================
 * Q: 登录页的核心功能是什么？
 * A: 支持登录/注册两种模式的 Tab 切换。
 *    登录调用 POST /api/auth/login，注册调用 POST /api/auth/register。
 *    登录成功后跳转到主页，注册成功后自动切换到登录模式喵～
 *
 * Q: Token 存储在哪？前端需要处理吗？
 * A: 完全不需要！后端登录成功后在 HTTP 响应头里设置了
 *    HttpOnly Cookie（X-User-Token），浏览器会自动保存。
 *    后续请求浏览器会自动携带，前端完全不用管 token 喵～
 *
 * Q: "记住我"功能是怎么实现的？
 * A: 通过 login 请求的 rememberMe 字段控制。
 *    后端根据这个字段决定 Cookie 的 maxAge：
 *    true → 7 天，false → 1 天。
 *    前端只管传参，存储逻辑全在后端喵！
 * ============================================
 */

import { createApp, ref, reactive } from 'vue';
import { post } from '../api/index';
import type { ApiResponse } from '../types/index';

/**
 * 初始化登录/注册页 Vue 应用
 */
export function initLoginPage(): void {
  const app = createApp({
    setup() {
      /* Tab 和消息状态 */
      const isLogin = ref(true);
      const message = ref('');
      const messageType = ref<'success' | 'error'>('success');
      const loginLoading = ref(false);
      const registerLoading = ref(false);

      /* 登录表单 */
      const loginForm = reactive({
        username: '',
        password: '',
        rememberMe: false
      });

      /* 注册表单 */
      const registerForm = reactive({
        username: '',
        password: '',
        email: ''
      });

      /* 清除提示消息 */
      function clearMessage(): void {
        message.value = '';
      }

      /*
       * 处理登录
       * 调用 POST /api/auth/login
       * 成功后跳转到主页
       */
      async function handleLogin(): Promise<void> {
        clearMessage();
        loginLoading.value = true;
        try {
          const result = await post<ApiResponse>('/api/auth/login', {
            username: loginForm.username,
            password: loginForm.password,
            rememberMe: loginForm.rememberMe
          });
          if (result.success) {
            message.value = '登录成功喵～欢迎回来！(ฅ´ω`ฅ)';
            messageType.value = 'success';
            setTimeout(() => {
              window.location.href = '/';
            }, 800);
          } else {
            message.value = result.message || '登录失败喵～请检查用户名和密码的说！';
            messageType.value = 'error';
          }
        } catch {
          message.value = '网络开小差了喵～请稍后重试的说！';
          messageType.value = 'error';
        } finally {
          loginLoading.value = false;
        }
      }

      /*
       * 处理注册
       * 调用 POST /api/auth/register
       * 成功后切换到登录模式
       */
      async function handleRegister(): Promise<void> {
        clearMessage();
        registerLoading.value = true;
        try {
          const result = await post<ApiResponse>('/api/auth/register', {
            username: registerForm.username,
            password: registerForm.password,
            email: registerForm.email
          });
          if (result.success) {
            message.value = '注册成功喵～欢迎加入的说！(ฅ´ω`ฅ)';
            messageType.value = 'success';
            setTimeout(() => {
              isLogin.value = true;
              loginForm.username = registerForm.username;
              registerForm.username = '';
              registerForm.password = '';
              registerForm.email = '';
              message.value = '注册成功喵～请登录的说！';
              messageType.value = 'success';
            }, 1000);
          } else {
            message.value = result.message || '注册失败喵～请重试的说！';
            messageType.value = 'error';
          }
        } catch {
          message.value = '网络开小差了喵～请稍后重试的说！';
          messageType.value = 'error';
        } finally {
          registerLoading.value = false;
        }
      }

      return {
        isLogin, message, messageType, loginLoading, registerLoading,
        loginForm, registerForm, handleLogin, handleRegister
      };
    }
  });

  app.mount('#app');
}