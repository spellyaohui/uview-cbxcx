// 主题混入 - 为页面组件提供黑暗模式支持
export default {
	data() {
		return {
			isDarkMode: false
		};
	},

	mounted() {
		// 初始化主题状态
		this.updateThemeStatus();

		// 监听主题变化
		uni.$on('themeChange', this.handleThemeChange);
	},

	onShow() {
		// 页面显示时获取最新的主题设置
		this.updateThemeFromAPI();
	},

	beforeDestroy() {
		// 移除主题变化监听
		uni.$off('themeChange', this.handleThemeChange);
	},

	methods: {
		// 更新主题状态
		updateThemeStatus() {
			const app = getApp();
			if (app && app.globalData && app.globalData.themeManager) {
				this.isDarkMode = app.globalData.themeManager.isCurrentDark();
			}
		},

		// 处理主题变化
		handleThemeChange({ theme, isDark }) {
			this.isDarkMode = isDark;
			this.onThemeChange && this.onThemeChange(theme);

			// 强制组件重新渲染以确保样式更新
			this.$forceUpdate && this.$forceUpdate();
		},

		// 主题变化回调（子组件可重写）
		onThemeChange(theme) {
			// 子组件可以重写此方法来响应主题变化
			console.log('页面主题变化:', theme);
		},

		// 从API更新主题
		async updateThemeFromAPI() {
			try {
				const app = getApp();
				if (app && app.globalData && app.globalData.themeManager) {
					// 检查用户是否已经手动设置了主题
					if (app.globalData.themeManager.isUserManualMode()) {
						console.log('用户已手动设置主题，跳过API更新');
						return;
					}

					// 调用主题管理器的fetchThemeFromAPI方法
					const apiTheme = await app.globalData.themeManager.fetchThemeFromAPI();
					if (apiTheme) {
						// 设置新的主题（不会标记为用户手动设置）
						app.globalData.themeManager.setTheme(apiTheme);
					}
				}
			} catch (error) {
				console.error('从API更新主题失败:', error);
			}
		}
	}
};