# 崔博小程序

一个基于 UniApp + Vue 3 开发的跨平台健康管理小程序，支持多端部署（微信小程序、APP、H5）。

## 📋 项目概述

崔博小程序是一个专业的健康管理平台，提供电子报告查询、个人报告管理、扫码授权等功能。项目采用现代化的 UI 设计，支持深色模式，为用户提供流畅的使用体验。

### 主要功能

- 🔐 **用户登录** - 安全的用户认证系统
- 📊 **电子报告查询** - 支持多条件查询健康报告
- 📁 **个人报告管理** - 查看、下载、删除个人报告
- 📱 **扫码授权** - 二维码扫描验证身份
- 🌙 **深色模式** - 支持浅色/深色主题切换
- 📈 **数据统计** - 报告数量和统计信息展示

### 技术栈

- **框架**: UniApp (Vue 3)
- **UI组件**: 自定义现代化UI框架
- **图标**: Font Awesome
- **样式**: SCSS + CSS Variables
- **主题**: 支持深色/浅色模式
- **开发工具**: HBuilderX

## 🚀 快速开始

### 环境要求

- Node.js 16+
- HBuilderX IDE
- Android Studio (Android开发)
- 微信开发者工具 (小程序开发)

### 安装步骤

1. **克隆项目**
   ```bash
   git clone [项目地址]
   cd uview-cbxcx
   ```

2. **使用 HBuilderX 打开**
   - 启动 HBuilderX
   - 文件 → 打开目录 → 选择项目文件夹

3. **配置项目**
   - 检查 `manifest.json` 中的应用配置
   - 更新 `siteinfo.js` 中的 API 地址

4. **运行项目**
   - 在 HBuilderX 中点击"运行"按钮
   - 选择目标平台（手机模拟器、微信开发者工具等）

## 📱 支持平台

| 平台 | 状态 | 说明 |
|------|------|------|
| Android APP | ✅ 完全支持 | 主要目标平台 |
| 微信小程序 | ✅ 支持 | 需要配置小程序AppID |
| H5 | ✅ 支持 | Web端访问 |
| iOS APP | ✅ 支持 | 需要Apple开发者账号 |

## 🎨 UI 特性

### 现代化设计
- 基于现代设计原则的UI界面
- 流畅的动画和过渡效果
- 响应式布局适配不同屏幕

### 深色模式
- 系统级深色模式支持
- 手动主题切换
- 主题状态持久化

### 图标系统
- 使用 Font Awesome 图标字体
- 矢量图标，无损缩放
- 丰富的图标选择

## 📁 项目结构

```
uview-cbxcx/
├── pages/                   # 页面文件
│   ├── index.vue           # 登录页面
│   ├── home.vue            # 主页面
│   ├── report.vue          # 报告查询
│   └── myreport.vue        # 我的报告
├── components/              # 组件文件
│   └── theme-toggle.vue    # 主题切换组件
├── styles/                  # 样式文件
│   ├── modern-ui.scss      # 现代化UI样式
│   └── font-awesome.scss   # Font Awesome图标
├── utils/                   # 工具文件
│   └── theme.js            # 主题管理器
├── common/                  # 公共文件
│   ├── HttpService.js      # HTTP服务
│   ├── Session.js          # 会话管理
│   └── Tools.js            # 工具函数
├── static/                  # 静态资源
├── manifest.json           # 应用配置
├── pages.json              # 页面配置
├── theme.json              # 主题配置
└── siteinfo.js             # 站点信息
```

## ⚙️ 配置说明

### 应用配置 (manifest.json)
```json
{
  "name": "崔博小程序",
  "appid": "__UNI__595054E",
  "versionName": "1.0.9.4",
  "versionCode": 100
}
```

### API配置 (siteinfo.js)
```javascript
{
  basePath: 'https://cbapp.wjtjyy.top:12634',
  fileBasePath: 'https://cbapp.wjtjyy.top:12634',
  title: '崔博小程序',
  debug: false,
  appid: '339871'
}
```

### 主题配置 (theme.json)
```json
{
  "light": {
    "navBgColor": "#07c160",
    "navTxtStyle": "white"
  },
  "dark": {
    "navBgColor": "#1a1a1a",
    "navTxtStyle": "white"
  }
}
```

## 🔧 开发指南

### 页面开发
- 使用 Vue 3 语法
- 遵循 UniApp 开发规范
- 支持条件编译处理平台差异

### 样式开发
- 使用 SCSS 预处理器
- 基于 CSS Variables 的主题系统
- 遵循 BEM 命名规范

### 状态管理
- 全局状态通过 App.vue 的 globalData 管理
- 会话管理通过 Session.js 处理
- 主题状态通过 theme.js 管理

### API 请求
```javascript
// GET 请求
this.$http.getData(params, url)

// POST 请求
this.$http.postData(params, url)
```

## 🎯 核心功能

### 用户认证
- 基于令牌的认证系统
- 自动登录状态检查
- 安全的密码传输

### 报告管理
- 多条件报告查询
- 报告详情展示
- 报告下载和删除

### 扫码功能
- 二维码扫描
- 身份验证授权
- 相机权限管理

## 📦 构建发布

### Android APP
1. 在 HBuilderX 中选择"发行" → "原生App-云打包"
2. 配置 Android 证书和签名
3. 选择打包平台 (Android)
4. 下载生成的 APK 文件

### 微信小程序
1. 配置小程序 AppID
2. 在 HBuilderX 中选择"发行" → "小程序-微信"
3. 使用微信开发者工具上传代码

### H5
1. 在 HBuilderX 中选择"发行" → "网站-H5手机版"
2. 配置网站域名
3. 部署到 Web 服务器

## 🐛 常见问题

### 图标显示问题
- 项目已使用 Font Awesome 图标字体
- 确保网络连接正常（图标从 CDN 加载）
- 检查 CSS 文件是否正确引入

### 主题切换问题
- 检查 theme.json 配置是否正确
- 确保 manifest.json 中启用了深色模式
- 清除应用缓存后重试

### API 请求失败
- 检查 siteinfo.js 中的 API 地址配置
- 确认网络连接正常
- 检查服务器端状态

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

### 版权信息

Copyright © 2024 崔博小程序. All Rights Reserved.

**许可证摘要:**
- ✅ 商业使用
- ✅ 修改
- ✅ 分发
- ✅ 私人使用
- ⚠️ 需要包含许可证和版权声明
- ⚠️ 不提供质量保证
- ⚠️ 不承担法律责任

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 邮箱: [your-email@example.com]
- 项目地址: [https://github.com/your-username/uview-cbxcx]

## 🙏 致谢

感谢以下开源项目的支持：
- [UniApp](https://uniapp.dcloud.net.cn/) - 跨平台开发框架
- [Vue.js](https://vuejs.org/) - 渐进式 JavaScript 框架
- [Font Awesome](https://fontawesome.com/) - 图标字体库