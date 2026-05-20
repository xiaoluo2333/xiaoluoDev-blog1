/**
 * ============================================
 * 喵～ 开发过程自我问答
 * ============================================
 * Q: 文章详情页的核心功能是什么？
 * A: 从 URL 路径中解析 slug，调用 GET /api/posts/{slug} 获取文章，
 *    用 marked.js 把 Markdown 渲染成 HTML，再用 highlight.js
 *    给代码块做语法高亮。同时显示附件列表喵～
 *
 * Q: marked 和 highlight.js 怎么在 TypeScript 里用？
 * A: 这两个库是通过 CDN 在 HTML 中加载的（script 标签），
 *    所以它们是全局变量。在 TS 里用 declare var 声明类型，
 *    告诉 TypeScript 编译器这些变量是外部提供的喵～
 *
 * Q: 为什么附件列表单独调用 /api/files？
 * A: 因为文章 API 返回的 PostResponse 不包含附件信息，
 *    需要另外调用 /api/files 获取所有文件列表。
 *    未来可以优化为按文章 ID 筛选附件喵～
 * ============================================
 */

import { createApp, ref, computed, onMounted, nextTick } from 'vue';
import { get } from '../api/index';
import type { ApiResponse, PostResponse, FileResponse } from '../types/index';

/* marked.js 和 highlight.js 是通过 CDN 加载的全局变量 */
declare var marked: {
  parse(content: string): string;
};
declare var hljs: {
  highlightElement(block: Element): void;
};

/**
 * 初始化文章详情页 Vue 应用
 */
export function initPostPage(): void {
  const app = createApp({
    setup() {
      const post = ref<PostResponse | null>(null);
      const files = ref<FileResponse[]>([]);
      const loading = ref(true);
      const error = ref('');

      /* 从 URL 路径中提取 slug */
      const slug = window.location.pathname.replace(/^\/post\//, '').replace(/\/$/, '');

      /*
       * 计算属性：把 Markdown 内容渲染为 HTML
       * 使用 marked.js 库进行渲染
       */
      const renderedContent = computed(() => {
        if (!post.value || !post.value.content) {
          return '<p class="text-gray-300 text-sm">暂无内容喵～</p>';
        }
        return marked.parse(post.value.content);
      });

      /*
       * 获取文章详情
       * API: GET /api/posts/{slug}
       * 渲染完成后对代码块做语法高亮
       */
      async function fetchPost(): Promise<void> {
        loading.value = true;
        error.value = '';
        try {
          const result = await get<ApiResponse<PostResponse>>(
            `/api/posts/${encodeURIComponent(slug)}`
          );
          if (result.success) {
            post.value = result.data;
            await nextTick();
            document.querySelectorAll('.markdown-body pre code').forEach((block) => {
              hljs.highlightElement(block);
            });
          } else {
            error.value = result.message || '文章没找到喵～';
          }
        } catch {
          error.value = '网络开小差了喵～请稍后重试的说！';
        } finally {
          loading.value = false;
        }
      }

      /* 获取附件列表 */
      async function fetchFiles(): Promise<void> {
        try {
          const result = await get<ApiResponse<FileResponse[]>>('/api/files');
          if (result.success) {
            files.value = result.data || [];
          }
        } catch {
          /* 附件加载失败不影响文章展示 */
        }
      }

      /* 格式化日期 */
      function formatDate(dateStr: string): string {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
      }

      /* 格式化文件大小 */
      function formatFileSize(bytes: number): string {
        if (!bytes) return '';
        const units = ['B', 'KB', 'MB', 'GB'];
        let size = bytes;
        let unitIndex = 0;
        while (size >= 1024 && unitIndex < units.length - 1) {
          size /= 1024;
          unitIndex++;
        }
        return `${size.toFixed(1)} ${units[unitIndex]}`;
      }

      onMounted(() => {
        if (slug) {
          fetchPost();
          fetchFiles();
        } else {
          error.value = '文章没找到喵～';
          loading.value = false;
        }
      });

      return {
        post, files, loading, error, renderedContent, formatDate, formatFileSize
      };
    }
  });

  app.mount('#app');
}