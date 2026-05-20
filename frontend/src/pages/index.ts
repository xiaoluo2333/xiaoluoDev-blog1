/**
 * ============================================
 * 喵～ 开发过程自我问答
 * ============================================
 * Q: 主页的核心功能是什么？
 * A: 从 /api/posts 获取文章列表，分页展示。
 *    每个文章卡片显示标题、摘要、日期和标签。
 *    点击卡片跳转到文章详情页喵～
 *
 * Q: 分页逻辑是怎么实现的？
 * A: 后端返回 Page 对象，包含 content（当前页数据）、
 *    totalPages（总页数）、totalElements（总数）等字段。
 *    前端通过 currentPage 控制页码，点击分页按钮重新请求。
 *    页码数量超过 7 页时显示省略号，保持按钮数量可控喵！
 * ============================================
 */

import { createApp, ref, computed, onMounted } from 'vue';
import { get } from '../api/index';
import type { ApiResponse, PageData, PostResponse } from '../types/index';

/**
 * 初始化主页 Vue 应用
 * 挂载到 #app 元素上
 */
export function initIndexPage(): void {
  const app = createApp({
    setup() {
      /* 响应式数据 */
      const posts = ref<PostResponse[]>([]);
      const loading = ref(true);
      const error = ref('');
      const currentPage = ref(0);
      const totalPages = ref(0);
      const totalElements = ref(0);
      const pageSize = 10;

      /*
       * 计算分页按钮列表
       * 总页数 ≤ 7：全部显示
       * 总页数 > 7：首尾固定，中间显示当前页附近
       */
      const pageNumbers = computed(() => {
        const pages: (number | string)[] = [];
        const total = totalPages.value;
        const current = currentPage.value;

        if (total <= 7) {
          for (let i = 0; i < total; i++) pages.push(i);
        } else {
          pages.push(0);
          if (current > 2) pages.push('...');
          const start = Math.max(1, current - 1);
          const end = Math.min(total - 2, current + 1);
          for (let i = start; i <= end; i++) pages.push(i);
          if (current < total - 3) pages.push('...');
          pages.push(total - 1);
        }
        return pages;
      });

      /*
       * 从后端获取文章列表
       * API: GET /api/posts?page=&size=
       */
      async function fetchPosts(): Promise<void> {
        loading.value = true;
        error.value = '';
        try {
          const result = await get<ApiResponse<PageData<PostResponse>>>(
            `/api/posts?page=${currentPage.value}&size=${pageSize}`
          );
          if (result.success) {
            posts.value = result.data.content || [];
            totalPages.value = result.data.totalPages || 0;
            totalElements.value = result.data.totalElements || 0;
          } else {
            error.value = result.message || '获取文章列表失败喵～';
          }
        } catch {
          error.value = '网络开小差了喵～请稍后重试的说！';
        } finally {
          loading.value = false;
        }
      }

      /* 跳转到指定页码 */
      function goToPage(page: number): void {
        if (page < 0 || page >= totalPages.value) return;
        currentPage.value = page;
        fetchPosts();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }

      /* 点击文章卡片跳转到详情页 */
      function goToPost(slug: string): void {
        if (slug) {
          window.location.href = `/post/${slug}`;
        }
      }

      /* 格式化日期为 YYYY-MM-DD */
      function formatDate(dateStr: string): string {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
      }

      onMounted(() => {
        fetchPosts();
      });

      return {
        posts, loading, error, currentPage, totalPages, totalElements,
        pageNumbers, goToPage, goToPost, formatDate, fetchPosts
      };
    }
  });

  app.mount('#app');
}