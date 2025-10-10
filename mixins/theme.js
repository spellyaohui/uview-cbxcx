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
		}
	}
};