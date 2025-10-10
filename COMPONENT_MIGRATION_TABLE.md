# DIY-uview-ui 到 uView 2.0 组件迁移映射表

## 基础组件映射

### 布局组件
| DIY-uview-ui 组件 | uView 2.0 组件 | 备注 | 迁移难度 |
|------------------|---------------|------|----------|
| u-row | u-row | 基本相同 | 低 |
| u-col | u-col | 基本相同 | 低 |
| u-grid | u-grid | 基本相同 | 低 |
| u-grid-item | u-grid-item | 基本相同 | 低 |
| u-gap | u-gap | 基本相同 | 低 |

### 基础UI组件
| DIY-uview-ui 组件 | uView 2.0 组件 | 备注 | 迁移难度 |
|------------------|---------------|------|----------|
| u-button | u-button | 基本相同 | 低 |
| u-navbar | u-navbar | 基本相同 | 低 |
| u-icon | u-icon | 基本相同 | 低 |
| u-image | u-image | 基本相同 | 低 |
| u-text | u-text | 新增组件 | 低 |
| u-line | u-line | 基本相同 | 低 |

### 表单组件
| DIY-uview-ui 组件 | uView 2.0 组件 | 备注 | 迁移难度 |
|------------------|---------------|------|----------|
| u-form | u-form | 基本相同 | 中 |
| u-form-item | u-form-item | 基本相同 | 中 |
| u-input | u-input | 基本相同 | 中 |
| u-field | u-field | 基本相同 | 中 |
| u-picker | u-picker | API略有差异 | 中 |
| u-switch | u-switch | 基本相同 | 中 |
| u-checkbox | u-checkbox | 基本相同 | 中 |
| u-radio | u-radio | 基本相同 | 中 |

### 反馈组件
| DIY-uview-ui 组件 | uView 2.0 组件 | 备注 | 迁移难度 |
|------------------|---------------|------|----------|
| u-modal | u-modal | 基本相同 | 中 |
| u-toast | u-toast | 基本相同 | 中 |
| u-popup | u-popup | 基本相同 | 中 |
| u-loading | u-loading | 基本相同 | 中 |
| u-alert-tips | u-alert | 组件名略有差异 | 中 |
| u-notice-bar | u-notice-bar | 基本相同 | 中 |

### 数据展示组件
| DIY-uview-ui 组件 | uView 2.0 组件 | 备注 | 迁移难度 |
|------------------|---------------|------|----------|
| u-card | u-card | 基本相同 | 中 |
| u-list | u-list | 基本相同 | 中 |
| u-cell | u-cell | 基本相同 | 中 |
| u-tag | u-tag | 基本相同 | 中 |
| u-badge | u-badge | 基本相同 | 中 |
| u-avatar | u-avatar | 基本相同 | 中 |
| u-table | u-table | 基本相同 | 中 |
| u-empty | u-empty | 基本相同 | 中 |

### 导航组件
| DIY-uview-ui 组件 | uView 2.0 组件 | 备注 | 迁移难度 |
|------------------|---------------|------|----------|
| u-tabs | u-tabs | 基本相同 | 中 |
| u-tabbar | u-tabbar | 基本相同 | 中 |
| u-steps | u-steps | 基本相同 | 中 |
| u-index-list | u-index-list | 基本相同 | 中 |

## DIY自定义组件处理

### 需要特殊处理的组件
| DIY-uview-ui 组件 | 处理方案 | 备注 | 迁移难度 |
|------------------|----------|------|----------|
| diy-navbar | 使用u-navbar替代 | 功能基本相同 | 中 |
| diy-grid | 使用u-grid替代 | 功能基本相同 | 中 |
| diy-calendar | 寻找第三方日历组件或使用u-calendar | 需要测试功能 | 高 |
| diy-qrcode | 使用第三方二维码组件 | 需要寻找替代方案 | 高 |
| diy-barcode | 使用第三方条形码组件 | 需要寻找替代方案 | 高 |
| diy-signature | 使用第三方签名组件 | 需要寻找替代方案 | 高 |
| diy-dropdown | 使用u-dropdown替代 | 功能基本相同 | 中 |
| diy-color-picker | 使用第三方颜色选择器 | 需要寻找替代方案 | 高 |

## 样式迁移

### 样式变量映射
| DIY-uview-ui 变量 | uView 2.0 变量 | 备注 |
|------------------|---------------|------|
| $u-primary-color | $u-primary | 主题色 |
| $u-success-color | $u-success | 成功色 |
| $u-error-color | $u-error | 错误色 |
| $u-warning-color | $u-warning | 警告色 |
| $u-info-color | $u-info | 信息色 |

## API变化注意事项

### 主要API变化
1. **组件导入方式**：从diy-uview-ui改为uview-ui
2. **样式文件路径**：需要更新样式文件引用路径
3. **组件属性**：部分组件属性名称可能略有调整
4. **事件处理**：大部分事件处理方式保持不变

## 迁移步骤优先级

### 高优先级（立即迁移）
1. 基础布局组件（row, col, grid）
2. 基础UI组件（button, icon, image）
3. 导航组件（navbar, tabs）

### 中优先级（第二批迁移）
1. 表单组件（form, input, picker）
2. 反馈组件（modal, toast, popup）
3. 数据展示组件（card, list, cell）

### 低优先级（最后迁移）
1. 自定义DIY组件
2. 复杂功能组件
3. 样式细节调整

## 测试检查点

### 功能测试
- [ ] 所有页面正常加载
- [ ] 表单提交功能正常
- [ ] 弹窗和交互正常
- [ ] 导航功能正常

### UI测试
- [ ] 样式显示正常
- [ ] 响应式布局正常
- [ ] 主题色彩正常
- [ ] 图标显示正常

### 性能测试
- [ ] 页面加载速度
- [ ] 组件渲染性能
- [ ] 内存使用情况