# uView 2.0 样式变量完整列表

## 📋 变量分类说明

本文档列出了为解决diy-uview-ui到uView 2.0迁移问题而定义的所有样式变量。

## 🎨 基础颜色变量

### 主要颜色
```scss
$u-primary-color: #2979ff !default;
$u-success-color: #19be6b !default;
$u-error-color: #fa3534 !default;
$u-warning-color: #ff9900 !default;
$u-info-color: #909399 !default;
```

### 类型颜色映射
```scss
$u-type-primary: $u-primary-color !default;
$u-type-success: $u-success-color !default;
$u-type-error: $u-error-color !default;
$u-type-warning: $u-warning-color !default;
$u-type-info: $u-info-color !default;

// 浅色版本
$u-type-primary-light: lighten($u-primary-color, 10%) !default;
$u-type-success-light: lighten($u-success-color, 10%) !default;
$u-type-error-light: lighten($u-error-color, 10%) !default;
$u-type-warning-light: lighten($u-warning-color, 10%) !default;
$u-type-info-light: lighten($u-info-color, 10%) !default;

// 背景色版本
$u-type-primary-bg: rgba($u-primary-color, 0.1) !default;
$u-type-success-bg: rgba($u-success-color, 0.1) !default;
$u-type-error-bg: rgba($u-error-color, 0.1) !default;
$u-type-warning-bg: rgba($u-warning-color, 0.1) !default;
$u-type-info-bg: rgba($u-info-color, 0.1) !default;

// 边框色版本
$u-type-primary-border: rgba($u-primary-color, 0.3) !default;
$u-type-success-border: rgba($u-success-color, 0.3) !default;
$u-type-error-border: rgba($u-error-color, 0.3) !default;
$u-type-warning-border: rgba($u-warning-color, 0.3) !default;
$u-type-info-border: rgba($u-info-color, 0.3) !default;

// 文本色版本
$u-type-primary-text: $u-primary-color !default;
$u-type-success-text: $u-success-color !default;
$u-type-error-text: $u-error-color !default;
$u-type-warning-text: $u-warning-color !default;
$u-type-info-text: $u-info-color !default;
```

## 🔧 功能性变量

### 主要功能颜色
```scss
$u-main-color: $u-primary-color !default;
$u-content-color: #303133 !default;
$u-tips-color: #909399 !default;
$u-light-color: #c0c4cc !default;
$u-border-color: #e4e7ed !default;
$u-bg-color: #f3f4f6 !default;
$u-disabled-color: #c8c9cc !default;
```

### 间距变量
```scss
$u-spacing-xs: 10rpx !default;
$u-spacing-sm: 20rpx !default;
$u-spacing-md: 30rpx !default;
$u-spacing-lg: 40rpx !default;
$u-spacing-xl: 60rpx !default;
```

### 字体变量
```scss
$u-font-xs: 22rpx !default;
$u-font-sm: 26rpx !default;
$u-font-md: 28rpx !default;
$u-font-lg: 32rpx !default;
$u-font-xl: 36rpx !default;

// 扩展字体变量
$u-font-size-base: $u-font-md !default;
$u-font-size-small: $u-font-sm !default;
$u-font-size-large: $u-font-lg !default;
$u-font-size-mini: $u-font-xs !default;
$u-font-size-extra-large: $u-font-xl !default;
```

### 圆角变量
```scss
$u-radius-xs: 4rpx !default;
$u-radius-sm: 8rpx !default;
$u-radius-md: 12rpx !default;
$u-radius-lg: 16rpx !default;
$u-radius-xl: 24rpx !default;
```

## 📝 表单相关变量

### 表单项变量
```scss
$u-form-item-border-color: $u-border-color !default;
$u-form-item-border-width: 1px !default;
$u-form-item-border-style: solid !default;
$u-form-item-border: $u-form-item-border-width $u-form-item-border-style $u-form-item-border-color !default;

// 表单标签
$u-form-label-color: $u-content-color !default;
$u-form-label-font-size: $u-font-md !default;
$u-form-label-font-weight: normal !default;
$u-form-label-margin-right: $u-spacing-sm !default;

// 表单验证
$u-form-error-color: $u-type-error !default;
$u-form-error-font-size: $u-font-sm !default;
$u-form-error-margin-top: $u-spacing-xs !default;
```

### 输入框变量
```scss
$u-input-border-color: $u-border-color !default;
$u-input-border-width: 1px !default;
$u-input-border-style: solid !default;
$u-input-border: $u-input-border-width $u-input-border-style $u-input-border-color !default;
$u-input-border-radius: $u-radius-sm !default;
$u-input-height: 70rpx !default;
$u-input-font-size: $u-font-md !default;
$u-input-color: $u-content-color !default;
$u-input-placeholder-color: $u-tips-color !default;
$u-input-background-color: #ffffff !default;
$u-input-disabled-color: $u-disabled-color !default;
$u-input-disabled-background-color: $u-bg-color !default;
```

### 占位符变量
```scss
$u-placeholder-color: $u-tips-color !default;
$u-placeholder-font-size: $u-font-md !default;
```

## 🔘 按钮相关变量

```scss
$u-button-border-radius: $u-radius-sm !default;
$u-button-height: 80rpx !default;
$u-button-font-size: $u-font-md !default;
$u-button-font-weight: normal !default;
```

## 📱 网格相关变量

```scss
$u-grid-item-border-radius: $u-radius-sm !default;
$u-grid-item-padding: $u-spacing-sm !default;
$u-grid-item-margin: $u-spacing-xs !default;
```

## 🧭 导航栏相关变量

```scss
$u-navbar-height: 88rpx !default;
$u-navbar-background-color: #ffffff !default;
$u-navbar-border-bottom: 1px solid $u-border-color !default;
$u-navbar-title-color: $u-content-color !default;
$u-navbar-title-font-size: $u-font-lg !default;
$u-navbar-title-font-weight: bold !default;
$u-navbar-back-icon-color: $u-content-color !default;
$u-navbar-back-icon-size: 44rpx !default;
```

## 🎨 扩展颜色变量

### 基础颜色
```scss
$u-color-white: #ffffff !default;
$u-color-black: #000000 !default;
$u-color-dark: #333333 !default;
$u-color-gray: #666666 !default;
$u-color-gray-light: #999999 !default;
$u-color-gray-lighter: #cccccc !default;
```

### 背景色扩展
```scss
$u-bg-color-page: #f8f8f8 !default;
$u-bg-color-container: #ffffff !default;
$u-bg-color-white: #ffffff !default;
$u-bg-color-gray: #f5f5f5 !default;
$u-bg-color-gray-light: #fafafa !default;
```

### 边框扩展
```scss
$u-border-light: $u-light-color !default;
$u-border-base: $u-border-color !default;
$u-border-color-base: $u-border-color !default;
$u-border-color-light: $u-light-color !default;
$u-border-color-lighter: #f2f6fc !default;
$u-border-color-extra-light: #ebeef5 !default;
```

### 文本颜色扩展
```scss
$u-text-color: $u-content-color !default;
$u-text-color-base: $u-content-color !default;
$u-text-color-light: $u-tips-color !default;
$u-text-color-lighter: $u-light-color !default;
$u-text-color-extra-light: #f0f0f0 !default;
```

## 🌟 阴影变量

```scss
$u-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1) !default;
$u-shadow-light: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04) !default;
$u-shadow-base: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.24) !default;
$u-box-shadow: $u-shadow !default;
$u-box-shadow-base: $u-shadow-base !default;
$u-box-shadow-light: $u-shadow-light !default;
$u-box-shadow-dark: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.12) !default;
```

## 🔤 线条变量

```scss
$u-line-color: $u-border-color !default;
$u-line-height: 1px !default;
```

## 🎯 状态变量

### 禁用状态
```scss
$u-disabled-color: $u-tips-color !default;
$u-disabled-bg-color: $u-bg-color !default;
$u-disabled-border-color: $u-light-color !default;
```

### 悬停状态
```scss
$u-hover-bg-color: $u-type-primary-bg !default;
$u-active-bg-color: darken($u-type-primary-bg, 5%) !default;
```

## 📋 使用说明

1. **所有变量都使用 `!default` 标志** - 可以被后续变量覆盖
2. **变量命名遵循uView 2.0规范** - 使用 `$u-` 前缀
3. **颜色变量提供多种变体** - 包括基础色、浅色、背景色、边框色、文本色
4. **功能性变量分类清晰** - 便于维护和扩展

## 🔄 变量覆盖顺序

```
1. uni.scss 中的变量定义（最早）
2. uview-ui/theme.scss 中的变量
3. App.vue 中的变量
4. 页面组件中的变量（最晚）
```

## ⚠️ 注意事项

1. **变量依赖** - 部分变量依赖其他变量，确保依赖关系正确
2. **颜色函数** - 使用了 `lighten()` 和 `darken()` 函数
3. **兼容性** - 变量设计考虑了diy-uview-ui的兼容性
4. **扩展性** - 可以根据需要继续添加新的变量

## 🎯 总结

这套变量系统确保了diy-uview-ui组件在uView 2.0环境下的正常运行，提供了：
- 完整的颜色系统
- 一致的间距规范
- 标准化的组件样式
- 良好的扩展性