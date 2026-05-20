/**
 * ============================================
 * 喵～ Webpack 配置
 * ============================================
 * Q: 为什么用 Webpack 而不是 Vite？
 * A: 项目的后端是 Spring Boot + Thymeleaf，
 *    需要把编译产物输出到后端 resources/static 目录下。
 *    Webpack 的配置更灵活，对输出路径的控制更精确喵～
 *
 * Q: 为什么用 MiniCssExtractPlugin 分离 CSS？
 * A: 把 CSS 提取成独立文件，HTML 模板可以直接用 link 引用，
 *    不依赖 JS 来加载样式，加载更快，也方便浏览器缓存喵！
 * ============================================
 */
const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');

/** 后端静态资源目录（Webpack 输出路径） */
const outputPath = path.resolve(__dirname, '..', 'backend', 'src', 'main', 'resources', 'static', 'assets');

module.exports = (env, argv) => {
  const isProduction = argv.mode === 'production';

  return {
    entry: {
      main: './src/main.ts'
    },
    output: {
      path: outputPath,
      filename: '[name].bundle.js',
      clean: true,
      publicPath: '/assets/'
    },
    resolve: {
      extensions: ['.ts', '.js', '.json']
    },
    module: {
      rules: [
        {
          test: /\.ts$/,
          use: 'ts-loader',
          exclude: /node_modules/
        },
        {
          test: /\.css$/,
          use: [
            isProduction ? MiniCssExtractPlugin.loader : 'style-loader',
            'css-loader',
            'postcss-loader'
          ]
        }
      ]
    },
    plugins: [
      new MiniCssExtractPlugin({
        filename: '[name].css',
        chunkFilename: '[id].css'
      })
    ].filter(Boolean),
    optimization: {
      minimizer: [
        '...',
        new CssMinimizerPlugin()
      ],
      splitChunks: {
        cacheGroups: {
          vendor: {
            test: /[\\/]node_modules[\\/]/,
            name: 'vendor',
            chunks: 'all'
          }
        }
      }
    },
    devtool: isProduction ? false : 'source-map',
    performance: {
      hints: false
    }
  };
};