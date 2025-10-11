// 主题工具 - 处理黑暗模式的动态应用
class ThemeManager {
	constructor() {
		this.currentTheme = 'light';
		this.isDarkMode = false;
		this.listeners = [];
	}

	// 初始化主题管理器
	init() {
		// 获取存储的主题
		const savedTheme = uni.getStorageSync('theme');
		const autoMode = uni.getStorageSync('themeAutoMode');
		
		if (savedTheme) {
			// 如果已保存主题，使用它
			this.setTheme(savedTheme);
		} else if (autoMode !== false) {
			// 如果没有保存的主题但自动模式未关闭，使用系统主题
			uni.getSystemInfo({
				success: (res) => {
					if (res.theme) {
						this.setTheme(res.theme);
					}
				}
			});
		} else {
			// 如果自动模式被关闭，使用light主题
			this.setTheme('light');
		}

		// 监听系统主题变化
		uni.onThemeChange(({ theme }) => {
			const autoMode = uni.getStorageSync('themeAutoMode');
			if (autoMode !== false) {
				this.setTheme(theme);
			}
		});
	}

	// 设置主题
	setTheme(theme) {
		this.currentTheme = theme;
		this.isDarkMode = theme === 'dark';

		// 存储主题设置
		uni.setStorageSync('theme', theme);

		// 应用主题到页面
		this.applyTheme(theme);
		
		// 应用原生主题（导航栏等）
		this.applyNativeTheme(theme);

		// 通知所有监听器
		this.notifyListeners(theme, this.isDarkMode);

		// 触发全局事件
		uni.$emit('themeChange', { theme, isDark: this.isDarkMode });

		console.log('主题管理器 - 主题已设置:', theme, this.isDarkMode ? '黑暗模式' : '浅色模式');
	}

	// 应用主题到当前页面
	applyTheme(theme) {
		// 设置全局CSS变量
		this.setGlobalCSSVariables(theme);
		
		// 获取当前页面栈
		const pages = getCurrentPages();
		pages.forEach(page => {
			if (page.$vm) {
				// 更新页面的主题状态
				if (page.$vm.isDarkMode !== undefined) {
					page.$vm.isDarkMode = this.isDarkMode;
				}

				// 调用页面的主题变化方法
				if (typeof page.$vm.onThemeChange === 'function') {
					page.$vm.onThemeChange(theme);
				}

				// 强制页面重新渲染
				if (page.$vm.$forceUpdate) {
					page.$vm.$forceUpdate();
				}
			}
		});
	}

	// 切换主题
	toggleTheme() {
		const newTheme = this.isDarkMode ? 'light' : 'dark';
		this.setTheme(newTheme);

		// 设置为手动模式
		uni.setStorageSync('themeAutoMode', false);

		return newTheme;
	}

	// 获取当前主题
	getCurrentTheme() {
		return this.currentTheme;
	}

	// 获取当前是否为黑暗模式
	isCurrentDark() {
		return this.isDarkMode;
	}

	// 添加主题变化监听器
	addThemeListener(callback) {
		this.listeners.push(callback);
	}

	// 移除主题变化监听器
	removeThemeListener(callback) {
		const index = this.listeners.indexOf(callback);
		if (index > -1) {
			this.listeners.splice(index, 1);
		}
	}

	// 通知所有监听器
	notifyListeners(theme, isDark) {
		this.listeners.forEach(callback => {
			try {
				callback(theme, isDark);
			} catch (error) {
				console.error('主题监听器执行失败:', error);
			}
		});
	}

	// 获取主题对应的CSS变量值
	getThemeVariable(varName) {
		// 这里可以返回特定主题的变量值
		const themeVariables = {
			light: {
				'--primary-color': '#2563eb',
				'--bg-primary': '#ffffff',
				'--text-primary': '#1f2937'
			},
			dark: {
				'--primary-color': '#3b82f6',
				'--bg-primary': '#1f2937',
				'--text-primary': '#f9fafb'
			}
		};

		return themeVariables[this.currentTheme]?.[varName] || null;
	}

	// 设置全局CSS变量
	setGlobalCSSVariables(theme) {
		const themeVariables = {
			light: {
				'--primary-gradient': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
				'--primary-color': '#2563eb',
				'--primary-dark': '#1d4ed8',
				'--success-color': '#10b981',
				'--warning-color': '#f59e0b',
				'--error-color': '#ef4444',
				'--bg-primary': '#ffffff',
				'--bg-secondary': '#f8fafc',
				'--bg-tertiary': '#f1f5f9',
				'--text-primary': '#1f2937',
				'--text-secondary': '#6b7280',
				'--text-tertiary': '#9ca3af',
				'--border-color': '#e2e8f0',
				'--shadow-sm': '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
				'--shadow-md': '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
				'--shadow-lg': '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
				'--shadow-xl': '0 20px 25px -5px rgba(0, 0, 0, 0.1)',
				'--radius-sm': '4rpx',
				'--radius-md': '8rpx',
				'--radius-lg': '12rpx',
				'--radius-xl': '16rpx',
				'--spacing-xs': '10rpx',
				'--spacing-sm': '16rpx',
				'--spacing-md': '24rpx',
				'--spacing-lg': '32rpx',
				'--spacing-xl': '48rpx',
				'--spacing-2xl': '64rpx'
			},
			dark: {
				'--primary-gradient': 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)',
				'--primary-color': '#3b82f6',
				'--primary-dark': '#2563eb',
				'--success-color': '#10b981',
				'--warning-color': '#f59e0b',
				'--error-color': '#ef4444',
				'--bg-primary': '#1e293b',
				'--bg-secondary': '#0f172a',
				'--bg-tertiary': '#334155',
				'--text-primary': '#f1f5f9',
				'--text-secondary': '#94a3b8',
				'--text-tertiary': '#64748b',
				'--border-color': '#334155',
				'--shadow-sm': '0 1px 3px 0 rgba(0, 0, 0, 0.3)',
				'--shadow-md': '0 4px 6px -1px rgba(0, 0, 0, 0.3)',
				'--shadow-lg': '0 10px 15px -3px rgba(0, 0, 0, 0.3)',
				'--shadow-xl': '0 20px 25px -5px rgba(0, 0, 0, 0.3)',
				'--radius-sm': '4rpx',
				'--radius-md': '8rpx',
				'--radius-lg': '12rpx',
				'--radius-xl': '16rpx',
				'--spacing-xs': '10rpx',
				'--spacing-sm': '16rpx',
				'--spacing-md': '24rpx',
				'--spacing-lg': '32rpx',
				'--spacing-xl': '48rpx',
				'--spacing-2xl': '64rpx'
			}
		};

		const variables = themeVariables[theme];
		
			// 在H5环境中设置CSS变量
		// #ifdef H5
		if (typeof document !== 'undefined' && document.documentElement) {
			const root = document.documentElement;
			Object.keys(variables).forEach(key => {
				root.style.setProperty(key, variables[key]);
			});
		}
		// #endif
		
		// 在APP环境中通过动态样式注入来设置CSS变量
		// #ifdef APP-PLUS
		this.setAppCSSVariables(variables);
		// #endif
	}
	
	// APP端设置CSS变量
	setAppCSSVariables(variables) {
		// 确保document存在
		// #ifdef APP-PLUS
		if (typeof document !== 'undefined' && document && document.head) {
			// 创建或更新全局样式
			const styleId = 'theme-variables';
			let styleElement = document.getElementById(styleId);
			
			if (!styleElement) {
				styleElement = document.createElement('style');
				styleElement.id = styleId;
				document.head.appendChild(styleElement);
			}
			
			// 构建CSS变量样式
			let cssText = ':root {';
			Object.keys(variables).forEach(key => {
				cssText += `${key}: ${variables[key]};`;
			});
			cssText += '}';
			
			styleElement.innerHTML = cssText;
		}
		// #endif
	}
	
	// 应用原生主题
	applyNativeTheme(theme) {
		// 设置导航栏主题
		// #ifdef APP-PLUS || H5
		uni.setNavigationBarColor({
			frontColor: theme === 'dark' ? '#ffffff' : '#000000',
			backgroundColor: theme === 'dark' ? '#1a1a1a' : '#07c160', // 使用与theme.json一致的配色
			animation: {
				duration: 300,
				timingFunc: 'easeIn'
			}
		});
		// #endif
		
		// 设置状态栏主题
		// #ifdef APP-PLUS
		plus.navigator.setStatusBarStyle(theme === 'dark' ? 'light' : 'dark');
		// #endif
	}
}

// 创建全局主题管理器实例
const themeManager = new ThemeManager();

export default themeManager;