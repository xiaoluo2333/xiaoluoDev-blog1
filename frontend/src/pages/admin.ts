/**
 * ============================================
 * 喵～ 开发过程自我问答
 * ============================================
 * Q: Admin 后台的核心功能是什么？
 * A: 一个功能完整的 SPA 后台管理系统，包含：
 *    1. 管理员登录/登出（基于 X-Admin-Token Cookie）
 *    2. Dashboard 概览面板
 *    3. 文章管理（CRUD + Vditor 编辑器）
 *    4. 文件管理（文件夹树 + 上传/创建）
 *    5. 插件管理（已加载插件列表）
 *
 * Q: 为什么用 Vditor 编辑器？
 * A: Vditor 支持 Markdown 和 WYSIWYG 两种模式，
 *    界面简洁，API 友好，适合博客系统的文章编辑场景。
 *    通过 CDN 加载，不增加包体积喵～
 *
 * Q: Admin 页面怎么处理未登录状态？
 * A: 页面加载时先调用 GET /api/auth/admin/me 检查登录状态。
 *    如果返回 success=false，显示管理员登录界面。
 *    登录后才显示真正的后台管理界面喵～
 * ============================================
 */

import { createApp, ref, reactive, onMounted, onUnmounted, nextTick } from 'vue';
import { get, post, put, del, uploadFile } from '../api/index';
import type {
  ApiResponse, PageData, PostResponse, PostRequest, PostFormData,
  FileResponse, PluginInfo
} from '../types/index';

/* Vditor 是通过 CDN 加载的全局变量 */
declare var Vditor: {
  new(id: string, options: Record<string, unknown>): VditorInstance;
};
interface VditorInstance {
  getValue(): string;
  destroy(): void;
}

/**
 * 初始化 Admin 后台 Vue 应用
 */
export function initAdminPage(): void {
  const app = createApp({
    setup() {
      /* ==========================================================================
         认证相关状态
         ========================================================================== */
      const isLoggedIn = ref(false);
      const adminInfo = ref<{ username?: string } | null>(null);
      const loginLoading = ref(false);
      const loginError = ref('');
      const adminLoginForm = reactive({
        username: '',
        password: ''
      });

      /* ==========================================================================
         导航状态
         ========================================================================== */
      const currentPanel = ref('dashboard');

      /* ==========================================================================
         文章管理状态
         ========================================================================== */
      const postList = ref<PostResponse[]>([]);
      const postCurrentPage = ref(0);
      const postTotalPages = ref(0);
      const showPostEditor = ref(false);
      const editingPost = ref<PostResponse | null>(null);
      const postSaving = ref(false);
      const postForm = reactive<PostFormData>({
        title: '',
        slug: '',
        content: '',
        summary: '',
        tags: '',
        status: 'DRAFT'
      });
      let vditorInstance: VditorInstance | null = null;

      /* ==========================================================================
         文件管理状态
         ========================================================================== */
      const fileList = ref<FileResponse[]>([]);
      const currentFolderId = ref<number | null>(null);
      const fileUploading = ref(false);
      const showCreateFolderInput = ref(false);
      const newFolderName = ref('');

      /* ==========================================================================
         插件管理状态
         ========================================================================== */
      const pluginList = ref<PluginInfo[]>([]);

      /* ==========================================================================
         认证逻辑
         ========================================================================== */

      /*
       * 检查管理员登录状态
       * API: GET /api/auth/admin/me
       * Cookie 会自动携带 X-Admin-Token
       */
      async function checkAdminAuth(): Promise<void> {
        try {
          const result = await get<ApiResponse<{ username: string }>>('/api/auth/admin/me');
          if (result.success) {
            isLoggedIn.value = true;
            adminInfo.value = result.data;
            loadAllData();
          }
        } catch {
          isLoggedIn.value = false;
        }
      }

      /* 登录后加载所有面板数据 */
      function loadAllData(): void {
        fetchPosts(0);
        fetchFiles();
        fetchPlugins();
      }

      /*
       * 管理员登录
       * API: POST /api/auth/admin/login
       */
      async function handleAdminLogin(): Promise<void> {
        loginLoading.value = true;
        loginError.value = '';
        try {
          const result = await post<ApiResponse>('/api/auth/admin/login', {
            username: adminLoginForm.username,
            password: adminLoginForm.password
          });
          if (result.success) {
            isLoggedIn.value = true;
            adminInfo.value = { username: adminLoginForm.username };
            await nextTick();
            loadAllData();
          } else {
            loginError.value = result.message || '登录失败喵～';
          }
        } catch {
          loginError.value = '网络开小差了喵～';
        } finally {
          loginLoading.value = false;
        }
      }

      /*
       * 管理员登出
       * API: POST /api/auth/admin/logout
       */
      async function handleLogout(): Promise<void> {
        try {
          await post('/api/auth/admin/logout');
        } catch {
          /* 登出失败也清除本地状态 */
        }
        isLoggedIn.value = false;
        adminInfo.value = null;
        postList.value = [];
        fileList.value = [];
        pluginList.value = [];
      }

      /* ==========================================================================
         文章管理逻辑
         ========================================================================== */

      /*
       * 获取后台文章列表
       * API: GET /api/posts/admin/all?page=&size=
       * 需要 Cookie 中有有效的 X-Admin-Token
       */
      async function fetchPosts(page: number): Promise<void> {
        try {
          const result = await get<ApiResponse<PageData<PostResponse>>>(
            `/api/posts/admin/all?page=${page}&size=10`
          );
          if (result.success) {
            postList.value = result.data.content || [];
            postCurrentPage.value = result.data.number || 0;
            postTotalPages.value = result.data.totalPages || 0;
          }
        } catch {
          console.error('获取文章列表失败喵～');
        }
      }

      /* 打开新建文章弹窗 */
      async function openCreatePost(): Promise<void> {
        editingPost.value = null;
        Object.assign(postForm, {
          title: '', slug: '', content: '', summary: '', tags: '', status: 'DRAFT' as const
        });
        showPostEditor.value = true;
        await nextTick();
        initVditor('');
      }

      /* 打开编辑文章弹窗，先获取文章详情 */
      async function openEditPost(post: PostResponse): Promise<void> {
        editingPost.value = post;
        Object.assign(postForm, {
          title: post.title || '',
          slug: post.slug || '',
          summary: post.summary || '',
          tags: post.tags || '',
          status: post.status || 'DRAFT',
          content: post.content || ''
        });
        showPostEditor.value = true;
        await nextTick();

        /* 如果列表接口没返回 content，从详情接口获取 */
        if (!post.content) {
          try {
            const result = await get<ApiResponse<PostResponse>>(`/api/posts/${post.slug}`);
            if (result.success && result.data) {
              postForm.content = result.data.content || '';
            }
          } catch {
            /* 静默处理 */
          }
        }
        initVditor(postForm.content);
      }

      /* 初始化 Vditor 编辑器 */
      function initVditor(content: string): void {
        if (vditorInstance) {
          try { vditorInstance.destroy(); } catch { /* ignore */ }
          vditorInstance = null;
        }

        const container = document.getElementById('vditor-container');
        if (!container) return;

        vditorInstance = new Vditor('vditor-container', {
          value: content || '',
          height: 360,
          mode: 'ir',
          toolbarConfig: { pin: true },
          cache: { enable: false }
        });
      }

      /*
       * 保存文章
       * 新建用 POST /api/posts，编辑用 PUT /api/posts/{id}
       */
      async function savePost(): Promise<void> {
        if (vditorInstance) {
          postForm.content = vditorInstance.getValue();
        }
        if (!postForm.title) {
          alert('标题不能为空喵～');
          return;
        }

        postSaving.value = true;
        try {
          const url = editingPost.value
            ? `/api/posts/${editingPost.value.id}`
            : '/api/posts';
          const method = editingPost.value ? 'PUT' : 'POST';

          const body: PostRequest = {
            title: postForm.title,
            content: postForm.content,
            status: postForm.status
          };
          if (postForm.slug) body.slug = postForm.slug;
          if (postForm.summary) body.summary = postForm.summary;
          if (postForm.tags) body.tags = postForm.tags;

          let result: ApiResponse;
          if (method === 'PUT') {
            result = await put<ApiResponse>(url, body);
          } else {
            result = await post<ApiResponse>(url, body);
          }

          if (result.success) {
            closePostEditor();
            fetchPosts(0);
          } else {
            alert(result.message || '保存失败喵～');
          }
        } catch {
          alert('网络开小差了喵～');
        } finally {
          postSaving.value = false;
        }
      }

      /* 关闭文章编辑弹窗，销毁 Vditor 实例 */
      function closePostEditor(): void {
        showPostEditor.value = false;
        editingPost.value = null;
        if (vditorInstance) {
          try { vditorInstance.destroy(); } catch { /* ignore */ }
          vditorInstance = null;
        }
      }

      /* 删除文章 */
      async function deletePost(id: number): Promise<void> {
        if (!confirm('确定要删除这篇文章喵？删了就不能恢复的说！')) return;
        try {
          const result = await del<ApiResponse>(`/api/posts/${id}`);
          if (result.success) {
            fetchPosts(postCurrentPage.value);
          } else {
            alert(result.message || '删除失败喵～');
          }
        } catch {
          alert('网络开小差了喵～');
        }
      }

      /* ==========================================================================
         文件管理逻辑
         ========================================================================== */

      /* 获取文件列表 */
      async function fetchFiles(): Promise<void> {
        try {
          const url = currentFolderId.value
            ? `/api/files/${currentFolderId.value}`
            : '/api/files';
          const result = await get<ApiResponse<FileResponse[]>>(url);
          if (result.success) {
            fileList.value = result.data || [];
          }
        } catch {
          console.error('获取文件列表失败喵～');
        }
      }

      /* 打开文件夹 */
      function openFolder(folderId: number): void {
        currentFolderId.value = folderId;
        fetchFiles();
      }

      /* 上传文件 */
      async function handleUploadFile(): Promise<void> {
        const fileInput = document.querySelector<HTMLInputElement>('input[type="file"]');
        if (!fileInput || !fileInput.files || !fileInput.files[0]) {
          alert('请选择要上传的文件喵～');
          return;
        }

        fileUploading.value = true;
        try {
          const formData = new FormData();
          formData.append('file', fileInput.files[0]);
          if (currentFolderId.value) {
            formData.append('parentId', String(currentFolderId.value));
          }

          const result = await uploadFile<ApiResponse>('/api/files/upload', formData);
          if (result.success) {
            fileInput.value = '';
            fetchFiles();
          } else {
            alert(result.message || '上传失败喵～');
          }
        } catch {
          alert('网络开小差了喵～');
        } finally {
          fileUploading.value = false;
        }
      }

      /* 新建文件夹 */
      async function createFolder(): Promise<void> {
        if (!newFolderName.value.trim()) {
          alert('文件夹名称不能为空喵～');
          return;
        }
        try {
          const result = await post<ApiResponse>('/api/files/folder', {
            name: newFolderName.value.trim(),
            parentId: currentFolderId.value
          });
          if (result.success) {
            newFolderName.value = '';
            showCreateFolderInput.value = false;
            fetchFiles();
          } else {
            alert(result.message || '创建失败喵～');
          }
        } catch {
          alert('网络开小差了喵～');
        }
      }

      /* 删除文件/文件夹 */
      async function deleteFile(id: number, name: string): Promise<void> {
        if (!confirm(`确定要删除「${name}」喵？删了就不能恢复的说！`)) return;
        try {
          const result = await del<ApiResponse>(`/api/files/${id}`);
          if (result.success) {
            fetchFiles();
          } else {
            alert(result.message || '删除失败喵～');
          }
        } catch {
          alert('网络开小差了喵～');
        }
      }

      /* ==========================================================================
         插件管理逻辑
         ========================================================================== */

      /* 获取已加载的插件列表 */
      async function fetchPlugins(): Promise<void> {
        try {
          const result = await get<ApiResponse<PluginInfo[]>>('/api/plugins');
          if (result.success) {
            pluginList.value = result.data || [];
          }
        } catch {
          pluginList.value = [];
        }
      }

      /* ==========================================================================
         工具函数
         ========================================================================== */

      function formatDate(dateStr: string): string {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
      }

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

      /* ==========================================================================
         生命周期
         ========================================================================== */

      onMounted(() => {
        checkAdminAuth();
      });

      onUnmounted(() => {
        if (vditorInstance) {
          try { vditorInstance.destroy(); } catch { /* ignore */ }
          vditorInstance = null;
        }
      });

      return {
        isLoggedIn, adminInfo, loginLoading, loginError, adminLoginForm,
        handleAdminLogin, handleLogout, currentPanel,
        postList, postCurrentPage, postTotalPages,
        showPostEditor, editingPost, postSaving, postForm,
        fetchPosts, openCreatePost, openEditPost, savePost, closePostEditor, deletePost,
        fileList, currentFolderId, fileUploading,
        showCreateFolderInput, newFolderName,
        fetchFiles, openFolder, handleUploadFile, createFolder, deleteFile,
        pluginList, formatDate, formatFileSize
      };
    }
  });

  app.mount('#app');
}