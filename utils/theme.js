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
		if (savedTheme) {
			this.setTheme(savedTheme);
		} else {
			// 检测系统主题
			uni.getSystemInfo({
				success: (res) => {
					if (res.theme) {
						this.setTheme(res.theme);
					}
				}
			});
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

		// 通知所有监听器
		this.notifyListeners(theme, this.isDarkMode);

		// 触发全局事件
		uni.$emit('themeChange', { theme, isDark: this.isDarkMode });

		console.log('主题管理器 - 主题已设置:', theme, this.isDarkMode ? '黑暗模式' : '浅色模式');
	}

	// 应用主题到当前页面
	applyTheme(theme) {
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
}

// 创建全局主题管理器实例
const themeManager = new ThemeManager();

export default themeManager;