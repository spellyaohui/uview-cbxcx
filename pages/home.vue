<template>
	<view class="home-container">
		<!-- 现代化导航栏 -->
		<view class="modern-navbar">
			<view class="navbar-content">
				<view class="navbar-title">欢迎使用</view>
				<view class="navbar-subtitle">崔博健康管理平台</view>
			</view>
		</view>

		<!-- 主体内容 -->
		<view class="home-content">
			<!-- 欢迎卡片 -->
			<view class="welcome-card modern-card fade-in-up">
				<view class="welcome-header">
					<view class="welcome-avatar">
						<text class="fas fa-user-circle"></text>
					</view>
					<view class="welcome-info">
						<view class="welcome-title">您好，欢迎回来！</view>
						<view class="welcome-desc">专业的健康管理服务</view>
					</view>
				</view>
				<view class="welcome-stats">
					<view class="stat-item">
						<view class="stat-number">{{ stats.totalReports }}</view>
						<view class="stat-label">总报告数</view>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item">
						<view class="stat-number">{{ stats.todayReports }}</view>
						<view class="stat-label">今日报告</view>
					</view>
				</view>
			</view>

			<!-- 功能菜单网格 -->
			<view class="menu-section">
				<view class="section-title">功能服务</view>
				<view class="modern-grid">
					<view
						class="modern-grid-item slide-in-right"
						style="animation-delay: 0.1s"
						@click="() => navigateTo({ type: 'page', url: '/pages/report' })"
					>
						<view class="grid-icon-wrapper" style="background: linear-gradient(135deg, #667eea, #764ba2);">
							<text class="fas fa-file-alt grid-icon-text"></text>
						</view>
						<view class="modern-grid-title">电子报告</view>
						<view class="modern-grid-desc">查询电子报告</view>
					</view>

					<view
						class="modern-grid-item slide-in-right"
						style="animation-delay: 0.2s"
						@click="() => navigateTo({ type: 'page', url: '/pages/myreport' })"
					>
						<view class="grid-icon-wrapper" style="background: linear-gradient(135deg, #f093fb, #f5576c);">
							<text class="fas fa-user grid-icon-text"></text>
						</view>
						<view class="modern-grid-title">我的报告</view>
						<view class="modern-grid-desc">个人报告管理</view>
					</view>

					<view
						class="modern-grid-item slide-in-right"
						style="animation-delay: 0.3s"
						@click="() => navigateTo({ type: 'scanCodeFunction' })"
					>
						<view class="grid-icon-wrapper" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
							<text class="fas fa-qrcode grid-icon-text"></text>
						</view>
						<view class="modern-grid-title">扫码授权</view>
						<view class="modern-grid-desc">扫码验证身份</view>
					</view>
				</view>
			</view>

			<!-- 快捷操作区域 -->
			<view class="quick-actions">
				<view class="section-title">快捷操作</view>
				<view class="action-cards">
					<view class="action-card modern-card" @click="() => navigateTo({ type: 'page', url: '/pages/report' })">
						<view class="action-icon" style="background: rgba(102, 126, 234, 0.1); color: var(--primary-color);">
							<text class="fas fa-search"></text>
						</view>
						<view class="action-content">
							<view class="action-title">查询报告</view>
							<view class="action-desc">快速查询健康报告</view>
						</view>
						<view class="action-arrow">
							<text class="fas fa-chevron-right"></text>
						</view>
					</view>

					<view class="action-card modern-card" @click="() => navigateTo({ type: 'page', url: '/pages/myreport' })">
						<view class="action-icon" style="background: rgba(16, 185, 129, 0.1); color: var(--success-color);">
							<text class="fas fa-user"></text>
						</view>
						<view class="action-content">
							<view class="action-title">我的收藏</view>
							<view class="action-desc">查看收藏的报告</view>
						</view>
						<view class="action-arrow">
							<text class="fas fa-chevron-right"></text>
						</view>
					</view>
				</view>
			</view>

			<!-- 用户信息卡片 -->
			<view class="user-card modern-card">
				<view class="user-header">
					<view class="user-avatar">
						<text class="fas fa-user-circle"></text>
					</view>
					<view class="user-info">
						<view class="user-name">{{ userInfo.username || '用户' }}</view>
						<view class="user-status">在线</view>
					</view>
				</view>
				<view class="logout-section">
					<button class="modern-btn modern-btn-secondary logout-btn" @click="() => navigateTo({ type: 'LogoutFunction' })">
						<text class="fas fa-sign-out-alt"></text>
						<text>退出登录</text>
					</button>
				</view>
			</view>
		</view>

		<!-- 主题切换组件 -->
		<theme-toggle />
	</view>
</template>

<script>
	import checkUpdate from '@/uni_modules/uni-upgrade-center-app/utils/check-update'; //更新方法
	import themeToggle from '@/components/theme-toggle.vue'; // 导入主题切换组件
	import themeMixin from '@/mixins/theme.js'; // 导入主题混入
	export default {
		components: {
			themeToggle
		},
		mixins: [themeMixin],
		data() {
			return {
				//用户全局信息
				userInfo: {},
				//页面传参
				globalOption: {},
				//自定义全局变量
				globalData: {},
				dkzf: {
					ip: '',
					msg: '',
					recordType: '',
					ret: 0,
					ruleKey: ''
				},
				scancode: ``,
				navbarHeight: 0,
				// 统计数据
				stats: {
					totalReports: 0,
					todayReports: 0
				}
			};
		},
		onBackPress(e) {
			// 防止重复调用
			if (this.isExiting) return true;

			this.isExiting = true;

			// #ifdef APP-PLUS
			plus.runtime.quit(); // 直接退出应用
			// #endif

			// H5 端尝试关闭窗口
			// #ifdef H5
			try {
				window.close();
			} catch (e) {
				console.log('无法直接关闭窗口，可能被浏览器阻止');
			}
			// #endif

			return true; // 返回 true 表示阻止默认返回行为
		},
		onReady(e) {
			checkUpdate();
		},
		onShow() {
			this.setCurrentPage(this);
			// 刷新用户信息
			this.loadUserInfo();
		},
		onLoad(option) {
			if (option) {
				this.setData({
					globalOption: this.getOption(option)
				});
			}
			this.setCurrentPage(this);
			this.init();
		},
		methods: {
			async init() {
				await this.dkzfApi();
				await this.loadStats();
			},

			// 加载用户信息
			loadUserInfo() {
				const userData = this.$session.getUser();
				if (userData) {
					this.userInfo = userData;
				}
			},

			// 加载统计数据
			async loadStats() {
				// 这里可以调用API获取统计数据
				// 暂时使用模拟数据
				this.stats = {
					totalReports: Math.floor(Math.random() * 100) + 20,
					todayReports: Math.floor(Math.random() * 10) + 1
				};
			},

			// 提交白名单 API请求方法
			async dkzfApi(param) {
				let thiz = this;
				param = param || {};

				//请求地址及请求数据，可以在加载前执行上面增加自己的代码逻辑
				let http_url = 'https://unraid.wjtjyy.top:16626/cuibo/wl/dkzf';
				let http_data = {};
				let http_header = {
					'Content-Type': 'application/json',
					authorization: 'Basic Y3VpYm86MTk4NjA1MTU='
				};

				try {
					let dkzf = await this.$http.get(http_url, http_data, http_header, 'json');
					this.dkzf = dkzf;
				} catch (error) {
					console.error('提交白名单失败:', error);
				}
			},

			// 扫码授权 API请求方法
			async scancodeApi(param) {
				let thiz = this;
				param = param || {};

				//请求地址及请求数据，可以在加载前执行上面增加自己的代码逻辑
				let http_url = '/sys/scanCode';
				let http_data = {
					code: param.code || ''
				};
				let http_header = {
					'Content-Type': 'application/json'
				};

				try {
					let scancode = await this.$http.post(http_url, http_data, http_header, 'json');
					this.scancode = scancode;

					if (scancode.code == 301) {
						this.$session.clearUser();
						this.showToast('请先登录');
						uni.reLaunch({
							url: '/pages/index'
						});
						return;
					}
					if (scancode.code == 400) {
						this.showToast('无权限操作', 'none');
						return;
					}
					if (scancode.code == 200) {
						this.showToast('授权成功', 'success');
						return;
					}
				} catch (error) {
					console.error('扫码授权失败:', error);
					this.showToast('扫码失败，请重试', 'none');
				}
			},

			// 注销登录 自定义方法
			async LogoutFunction(param) {
				let thiz = this;

				// 显示确认对话框
				uni.showModal({
					title: '确认退出',
					content: '确定要退出登录吗？',
					success: (res) => {
						if (res.confirm) {
							this.$session.clearUser();
							uni.showToast({
								title: '已退出登录',
								icon: 'success',
								duration: 1500
							});
							setTimeout(() => {
								uni.reLaunch({
									url: '/pages/index'
								});
							}, 1500);
						}
					}
				});
			},

			// 扫码授权 自定义方法
			async scanCodeFunction(param) {
				let thiz = this;

				// 检查相机权限
				// #ifdef APP-PLUS
				try {
					const permission = await new Promise((resolve, reject) => {
						plus.android.requestPermissions(
							['android.permission.CAMERA'],
							() => resolve(true),
							() => reject(false)
						);
					});
				} catch (error) {
					this.showToast('请授予相机权限', 'none');
					return;
				}
				// #endif

				// 只允许通过相机扫码
				uni.scanCode({
					onlyFromCamera: true,
					success: function (res) {
						console.log('条码类型：' + res.scanType);
						console.log('条码内容：' + res.result);
						thiz.scancodeApi({
							code: res.result
						});
					},
					fail: function (err) {
						console.error('扫码失败:', err);
						thiz.showToast('扫码失败，请重试', 'none');
					}
				});
			},

			changeNavbarHeight(val) {
				this.navbarHeight = val;
			},

			// 重写主题变化处理方法
			onThemeChange(theme) {
				console.log('主页主题切换到:', theme);
				// 这里可以添加主题切换时的特定处理逻辑
				// 比如重新加载某些数据或更新UI状态
			}
		}
	};
</script>

<style lang="scss" scoped>
.home-container {
	min-height: 100vh;
	background: var(--bg-secondary);
}

/* 现代化导航栏 */
.modern-navbar {
	background: var(--primary-gradient);
	padding: calc(var(--spacing-lg) + var(--status-bar-height)) var(--spacing-lg) var(--spacing-xl);
	border-radius: 0 0 var(--radius-2xl) var(--radius-2xl);
	box-shadow: var(--shadow-lg);
	position: relative;
	overflow: hidden;

	&::before {
		content: '';
		position: absolute;
		top: -50%;
		right: -10%;
		width: 300rpx;
		height: 300rpx;
		background: rgba(255, 255, 255, 0.1);
		border-radius: 50%;
		filter: blur(60rpx);
	}
}

.navbar-content {
	position: relative;
	z-index: 2;
	text-align: center;
}

.navbar-title {
	font-size: 40rpx;
	font-weight: 700;
	color: white;
	margin-bottom: var(--spacing-xs);
	text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.navbar-subtitle {
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.8);
	font-weight: 300;
}

/* 主体内容 */
.home-content {
	padding: var(--spacing-lg);
	padding-bottom: calc(var(--spacing-xl) + env(safe-area-inset-bottom));
}

/* 欢迎卡片 */
.welcome-card {
	margin-bottom: var(--spacing-xl);
	background: var(--primary-gradient);
	color: white;
	border: none;
}

.welcome-header {
	display: flex;
	align-items: center;
	margin-bottom: var(--spacing-lg);
}

.welcome-avatar {
	width: 100rpx;
	height: 100rpx;
	background: rgba(255, 255, 255, 0.2);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: var(--spacing-md);

	.fas {
		font-size: 48rpx;
		color: white;
	}
}

.welcome-info {
	flex: 1;
}

.welcome-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: var(--spacing-xs);
}

.welcome-desc {
	font-size: 24rpx;
	opacity: 0.8;
}

.welcome-stats {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: var(--spacing-md) 0;
	border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-item {
	flex: 1;
	text-align: center;
}

.stat-number {
	font-size: 48rpx;
	font-weight: 700;
	margin-bottom: var(--spacing-xs);
}

.stat-label {
	font-size: 24rpx;
	opacity: 0.8;
}

.stat-divider {
	width: 2rpx;
	height: 60rpx;
	background: rgba(255, 255, 255, 0.2);
	margin: 0 var(--spacing-lg);
}

/* 菜单区域 */
.menu-section {
	margin-bottom: var(--spacing-xl);
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	color: var(--text-primary);
	margin-bottom: var(--spacing-lg);
	padding-left: var(--spacing-sm);
}

/* 网格图标包装器 */
.grid-icon-wrapper {
	width: 120rpx;
	height: 120rpx;
	border-radius: var(--radius-lg);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: var(--spacing-md);
	box-shadow: var(--shadow-md);
}

.grid-icon-text {
	font-size: 60rpx;
	color: white;
}

/* 快捷操作 */
.quick-actions {
	margin-bottom: var(--spacing-xl);
}

.action-cards {
	display: flex;
	flex-direction: column;
	gap: var(--spacing-md);
}

.action-card {
	display: flex;
	align-items: center;
	padding: var(--spacing-lg);
	transition: all 0.3s ease;
	cursor: pointer;

	&:active {
		transform: scale(0.98);
	}
}

.action-icon {
	width: 80rpx;
	height: 80rpx;
	border-radius: var(--radius-md);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: var(--spacing-md);

	.fas {
		font-size: 36rpx;
	}
}

.action-content {
	flex: 1;
}

.action-title {
	font-size: 30rpx;
	font-weight: 500;
	color: var(--text-primary);
	margin-bottom: var(--spacing-xs);
}

.action-desc {
	font-size: 24rpx;
	color: var(--text-secondary);
}

.action-arrow {
	.fas {
		font-size: 28rpx;
		color: var(--text-tertiary);
	}
}

/* 用户信息卡片 */
.user-card {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.user-header {
	display: flex;
	align-items: center;
}

.user-avatar {
	width: 80rpx;
	height: 80rpx;
	background: var(--primary-gradient);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: var(--spacing-md);

	.fas {
		font-size: 36rpx;
		color: white;
	}
}

.user-name {
	font-size: 30rpx;
	font-weight: 500;
	color: var(--text-primary);
	margin-bottom: var(--spacing-xs);
}

.user-status {
	font-size: 24rpx;
	color: var(--success-color);
}

.logout-btn {
	display: flex;
	align-items: center;
	gap: var(--spacing-xs);
	padding: var(--spacing-sm) var(--spacing-lg);
	font-size: 26rpx;
	border: 1px solid var(--border-color);
	color: var(--error-color);
	background: rgba(239, 68, 68, 0.1);

	&:active {
		background: rgba(239, 68, 68, 0.2);
	}

	.fas {
		font-size: 24rpx;
	}
}

/* 响应式调整 */
@media screen and (max-width: 750rpx) {
	.home-content {
		padding: var(--spacing-md);
	}

	.modern-navbar {
		padding: var(--spacing-md) var(--spacing-md) var(--spacing-lg);
	}

	.navbar-title {
		font-size: 36rpx;
	}

	.welcome-avatar {
		width: 80rpx;
		height: 80rpx;

		.fas {
			font-size: 36rpx;
		}
	}

	.action-card {
		padding: var(--spacing-md);
	}
}
</style>