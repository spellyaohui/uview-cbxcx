// 主题工具 - 处理黑暗模式的动态应用
class ThemeManager {
	constructor() {
		this.currentTheme = 'light';
		this.isDarkMode = false;
		this.listeners = [];
	}

	// 初始化主题管理器
	async init() {
		console.log('初始化主题管理器，冷启动时以API状态为准...');

		// 冷启动时始终调用API获取主题状态
		try {
			const apiTheme = await this.fetchThemeFromAPI();
			console.log('冷启动使用API返回的主题:', apiTheme);
			this.setTheme(apiTheme);
		} catch (error) {
			console.error('API调用失败，使用本地存储的主题:', error);
			// 只有API失败时才使用本地存储的主题
			const savedTheme = uni.getStorageSync('theme');
			this.setTheme(savedTheme || 'light');
		}

		// 注意：冷启动时不检查user_theme，确保每次都以API状态初始化
	}

	// 从API获取主题状态
	async fetchThemeFromAPI() {
		return new Promise((resolve, reject) => {
			uni.request({
				url: 'http://btpanelweb.wjtjyy.top:4000/api/day-night-status',
				method: 'GET',
				success: (res) => {
					console.log('API调用成功，返回数据:', res);
					if (res.statusCode === 200 && res.data && res.data.status) {
						const apiTheme = res.data.status === 'dark' ? 'dark' : 'light';
						console.log('API返回的主题状态:', apiTheme);
						resolve(apiTheme);
					} else {
						console.error('API返回数据格式错误:', res);
						reject(new Error('API返回数据格式错误'));
					}
				},
				fail: (error) => {
					console.error('API调用失败:', error);
					reject(error);
				}
			});
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

				// 尝试更新页面的根数据
				if (page.$vm.$data && typeof page.$vm.$data === 'object') {
					page.$vm.$data.isDarkMode = this.isDarkMode;
				}

				// 强制页面重新渲染
				if (page.$vm.$forceUpdate) {
					page.$vm.$forceUpdate();
				} else if (page.$vm.setData) {
					// 兼容小程序 setData
					page.$vm.setData({
						isDarkMode: this.isDarkMode
					});
				}
			}
		});
	}

	// 切换主题（手动切换）
	toggleTheme() {
		const newTheme = this.isDarkMode ? 'light' : 'dark';
		this.setTheme(newTheme);

		// 标记这是用户手动设置的主题
		uni.setStorageSync('user_theme', newTheme);
		console.log('用户手动切换主题:', newTheme);

		return { theme: newTheme, mode: 'manual' };
	}

	// 强制切换主题（手动切换）
	forceToggleTheme() {
		const newTheme = this.isDarkMode ? 'light' : 'dark';
		this.setTheme(newTheme);

		// 标记这是用户手动设置的主题
		uni.setStorageSync('user_theme', newTheme);
		console.log('用户强制切换主题:', newTheme);

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

	// 重置为API自动模式
	resetToAutoMode() {
		// 清除用户手动设置标记
		uni.removeStorageSync('user_theme');
		console.log('已重置为API自动模式');

		// 立即调用API获取当前主题
		this.fetchThemeFromAPI().then(apiTheme => {
			this.setTheme(apiTheme);
		}).catch(error => {
			console.error('重置自动模式失败:', error);
		});
	}

	// 检查是否为用户手动设置
	isUserManualMode() {
		return !!uni.getStorageSync('user_theme');
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
		
		// 通过全局事件通知所有组件
		uni.$emit('themeChange', { theme, isDark });
		
		// 更新全局状态
		try {
			const app = getApp();
			if (app && app.$store && app.$store.updateTheme) {
				app.$store.updateTheme(isDark, theme);
			} else if (typeof uni !== 'undefined' && uni.$globalStore) {
				uni.$globalStore.updateTheme(isDark, theme);
			}
		} catch (error) {
			console.error('更新全局状态失败:', error);
		}
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

			// 同时添加主题类样式
			if (this.currentTheme === 'dark') {
				cssText += '.theme-dark, [data-theme="dark"] {';
				Object.keys(variables).forEach(key => {
					cssText += `${key}: ${variables[key]};`;
				});
				cssText += '}';
			}

			styleElement.innerHTML = cssText;

			// 立即更新body属性
			this.updateBodyClass(this.currentTheme);
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
		if (typeof plus !== 'undefined' && plus.navigator) {
			plus.navigator.setStatusBarStyle(theme === 'dark' ? 'light' : 'dark');
		}
		// #endif

		// 强制设置原生UI样式
		// #ifdef APP-PLUS
		if (typeof plus !== 'undefined' && plus.nativeUI) {
			plus.nativeUI.setUIStyle(theme === 'dark' ? 'dark' : 'light');
		}
		// #endif

		// 在H5中更新body类
		// #ifdef H5
		this.updateBodyClass(theme);
		// #endif

		// 在App中更新body类
		// #ifdef APP-PLUS
		this.updateBodyClass(theme);
		// #endif
	}
	
	// 更新body类以应用主题
	updateBodyClass(theme) {
		if (typeof document !== 'undefined' && document.body) {
			if (theme === 'dark') {
				document.body.classList.remove('theme-light');
				document.body.classList.add('theme-dark');
				document.body.setAttribute('data-theme', 'dark');
			} else {
				document.body.classList.remove('theme-dark');
				document.body.classList.add('theme-light');
				document.body.setAttribute('data-theme', 'light');
			}
		}
	}
}

// 创建全局主题管理器实例
const themeManager = new ThemeManager();

export default themeManager;

