<template>
	<view class="theme-toggle">
		<view class="theme-toggle-container" @click="toggleTheme">
			<view class="theme-icon">
				<text class="fas" :class="isDarkMode ? 'fa-sun' : 'fa-moon'"></text>
			</view>
			<view class="theme-text">
				<text class="theme-label">{{ themeLabel }}</text>
				<text class="theme-desc">{{ themeDesc }}</text>
			</view>
			<view class="theme-switch">
				<view class="switch-track" :class="{ active: isDarkMode }">
					<view class="switch-thumb" :class="{ active: isDarkMode }">
						<text class="switch-icon">{{ isDarkMode ? '🌙' : '☀️' }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import themeMixin from '@/mixins/theme.js';

export default {
	name: 'ThemeToggle',
	mixins: [themeMixin],
	computed: {
		themeLabel() {
			return this.isDarkMode ? '浅色模式' : '深色模式';
		},
		themeDesc() {
			return this.isDarkMode ? '切换到浅色主题' : '切换到深色主题';
		}
	},
	methods: {
		toggleTheme() {
			// 调用App.vue的主题切换方法
			const app = getApp();
			if (app && app.toggleTheme) {
				app.toggleTheme();

				// 重新获取主题状态
				this.$nextTick(() => {
					if (this.updateThemeStatus) {
						this.updateThemeStatus();
					}
				});

				// 触觉反馈
				// #ifdef APP-PLUS
				if (typeof plus !== 'undefined' && plus.device) {
					plus.device.vibrate(8);
				}
				// #endif

				// 强制触发页面重新渲染
				// #ifdef APP-PLUS
				setTimeout(() => {
					const pages = getCurrentPages();
					const currentPage = pages[pages.length - 1];
					if (currentPage && currentPage.$vm && currentPage.$vm.$forceUpdate) {
						currentPage.$vm.$forceUpdate();
					}
				}, 100);
				// #endif

				// 显示提示
				uni.showToast({
					title: this.isDarkMode ? '已切换到深色主题' : '已切换到浅色主题',
					icon: 'none',
					duration: 1500
				});
			}
		}
	}
};
</script>

<style lang="scss" scoped>
.theme-toggle {
	margin: var(--spacing-md);
}

.theme-toggle-container {
	display: flex;
	align-items: center;
	justify-content: space-between;
	background: var(--bg-primary);
	border-radius: var(--radius-lg);
	padding: var(--spacing-md);
	box-shadow: var(--shadow-sm);
	border: 1px solid var(--border-color);
	transition: all 0.3s ease;
	cursor: pointer;
}

.theme-toggle-container:hover {
	box-shadow: var(--shadow-md);
	transform: translateY(-2rpx);
}

.theme-toggle-container:active {
	transform: translateY(0);
}

.theme-icon {
	width: 80rpx;
	height: 80rpx;
	border-radius: var(--radius-md);
	display: flex;
	align-items: center;
	justify-content: center;
	background: var(--bg-secondary);
	margin-right: var(--spacing-md);
}

.theme-icon .fas {
	font-size: 40rpx;
	color: var(--text-primary);
}

.theme-text {
	flex: 1;
	margin-right: var(--spacing-md);
}

.theme-label {
	font-size: 28rpx;
	font-weight: 600;
	color: var(--text-primary);
	display: block;
	margin-bottom: var(--spacing-xs);
}

.theme-desc {
	font-size: 22rpx;
	color: var(--text-secondary);
	display: block;
}

.theme-switch {
	display: flex;
	align-items: center;
}

.switch-track {
	width: 100rpx;
	height: 50rpx;
	background: var(--bg-tertiary);
	border-radius: 25rpx;
	position: relative;
	transition: all 0.3s ease;
	border: 2px solid var(--border-color);
}

.switch-track.active {
	background: var(--primary-color);
	border-color: var(--primary-color);
}


.switch-thumb {
	width: 42rpx;
	height: 42rpx;
	background: white;
	border-radius: 50%;
	position: absolute;
	top: 2rpx;
	left: 2rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
}

.switch-thumb.active {
	left: 54rpx;
	background: white;
}


.switch-icon {
	font-size: 20rpx;
	line-height: 1;
}

/* 深色模式下的微调 */
[data-theme="dark"] .switch-track {
	background: var(--bg-tertiary);
}

[data-theme="dark"] .switch-thumb {
	background: var(--bg-primary);
}
</style>