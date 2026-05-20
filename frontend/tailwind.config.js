/**
 * ============================================
 * 喵～ TailwindCSS 配置
 * ============================================
 * 极简主义风格，主色 #3b82f6（蓝色），
 * 强调色 #f59e0b（琥珀色），
 * 字体系列使用 "Noto Sans SC" + system fonts
 * ============================================
 */
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    '../backend/src/main/resources/templates/**/*.html'
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a'
        },
        accent: {
          50: '#fffbeb',
          100: '#fef3c7',
          200: '#fde68a',
          300: '#fcd34d',
          400: '#fbbf24',
          500: '#f59e0b',
          600: '#d97706',
          700: '#b45309',
          800: '#92400e',
          900: '#78350f'
        }
      },
      fontFamily: {
        sans: [
          '"Noto Sans SC"',
          '-apple-system',
          'BlinkMacSystemFont',
          '"Segoe UI"',
          'Roboto',
          '"Helvetica Neue"',
          'Arial',
          '"PingFang SC"',
          '"Microsoft YaHei"',
          'sans-serif'
        ]
      },
      borderRadius: {
        xl: '8px'
      }
    }
  },
  plugins: []
};