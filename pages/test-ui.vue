<template>
	<view class="test-container">
		<!-- 现代化导航栏 -->
		<view class="modern-navbar">
			<view class="navbar-content">
				<view class="navbar-title">UI测试页面</view>
				<view class="navbar-subtitle">验证新UI组件库</view>
			</view>
		</view>

		<!-- 主体内容 -->
		<view class="test-content">
			<!-- 测试结果 -->
			<view class="test-result modern-card fade-in-up">
				<view class="result-header">
					<view class="result-icon success">
						<text class="iconfont icon-check"></text>
					</view>
					<view class="result-info">
						<view class="result-title">UI重构完成</view>
						<view class="result-desc">已成功替换为现代化UI组件库</view>
					</view>
				</view>
			</view>

			<!-- 组件测试 -->
			<view class="component-tests modern-card fade-in-up">
				<view class="section-title">组件测试</view>

				<!-- 按钮测试 -->
				<view class="test-group">
					<view class="test-label">按钮组件</view>
					<view class="button-group">
						<button class="modern-btn modern-btn-primary">主要按钮</button>
						<button class="modern-btn modern-btn-secondary">次要按钮</button>
						<button class="modern-btn modern-btn-success">成功按钮</button>
						<button class="modern-btn modern-btn-warning">警告按钮</button>
						<button class="modern-btn modern-btn-error">危险按钮</button>
					</view>
				</view>

				<!-- 输入框测试 -->
				<view class="test-group">
					<view class="test-label">输入框组件</view>
					<view class="input-wrapper">
						<input class="modern-input" placeholder="请输入内容" v-model="testInput" />
					</view>
				</view>

				<!-- 卡片测试 -->
				<view class="test-group">
					<view class="test-label">卡片组件</view>
					<view class="card-grid">
						<view class="mini-card">
							<view class="mini-card-title">测试卡片1</view>
							<view class="mini-card-desc">这是一个测试卡片</view>
						</view>
						<view class="mini-card">
							<view class="mini-card-title">测试卡片2</view>
							<view class="mini-card-desc">另一个测试卡片</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 页面导航测试 -->
			<view class="navigation-tests modern-card fade-in-up">
				<view class="section-title">页面导航测试</view>
				<view class="nav-buttons">
					<button class="nav-btn" @click="goToPage('index')">
						<text class="iconfont icon-user"></text>
						<text>登录页面</text>
					</button>
					<button class="nav-btn" @click="goToPage('home')">
						<text class="iconfont icon-home"></text>
						<text>主页</text>
					</button>
					<button class="nav-btn" @click="goToPage('report')">
						<text class="iconfont icon-search"></text>
						<text>报告查询</text>
					</button>
					<button class="nav-btn" @click="goToPage('myreport')">
						<text class="iconfont icon-folder"></text>
						<text>我的报告</text>
					</button>
				</view>
			</view>

			<!-- API测试 -->
			<view class="api-tests modern-card fade-in-up">
				<view class="section-title">API连接测试</view>
				<view class="test-item">
					<view class="test-info">
						<view class="test-name">HTTP服务</view>
						<view class="test-status" :class="{ success: httpServiceOk, error: !httpServiceOk }">
							{{ httpServiceOk ? '正常' : '异常' }}
						</view>
					</view>
					<button class="test-action-btn" @click="testHttpService">测试连接</button>
				</view>

				<view class="test-item">
					<view class="test-info">
						<view class="test-name">会话管理</view>
						<view class="test-status" :class="{ success: sessionOk, error: !sessionOk }">
							{{ sessionOk ? '正常' : '异常' }}
						</view>
					</view>
					<button class="test-action-btn" @click="testSession">测试会话</button>
				</view>
			</view>

			<!-- 测试信息 -->
			<view class="test-info-card modern-card fade-in-up">
				<view class="section-title">重构信息</view>
				<view class="info-list">
					<view class="info-item">
						<text class="info-label">原UI库：</text>
						<text class="info-value">diy-uview-ui</text>
					</view>
					<view class="info-item">
						<text class="info-label">新UI库：</text>
						<text class="info-value">ColorUI + 自定义组件</text>
					</view>
					<view class="info-item">
						<text class="info-label">Vue版本：</text>
						<text class="info-value">Vue 3</text>
					</view>
					<view class="info-item">
						<text class="info-label">重构页面：</text>
						<text class="info-value">4个主要页面</text>
					</view>
					<view class="info-item">
						<text class="info-label">API状态：</text>
						<text class="info-value">保持原有功能</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				testInput: '',
				httpServiceOk: false,
				sessionOk: false
			};
		},
		onShow() {
			this.setCurrentPage(this);
			// 初始化测试
			this.initTests();
		},
		methods: {
			initTests() {
				// 检查HTTP服务
				this.httpServiceOk = !!this.$http;
				// 检查会话管理
				this.sessionOk = !!this.$session;
			},

			goToPage(page) {
				uni.navigateTo({
					url: `/pages/${page}`,
					fail: (err) => {
						uni.showToast({
							title: `无法跳转到${page}页面`,
							icon: 'none'
						});
					}
				});
			},

			async testHttpService() {
				try {
					// 简单的HTTP测试
					const result = await this.$http.get('/sys/health', {}, {}, 'json');
					this.httpServiceOk = true;
					uni.showToast({
						title: 'HTTP服务正常',
						icon: 'success'
					});
				} catch (error) {
					this.httpServiceOk = false;
					uni.showToast({
						title: 'HTTP服务异常',
						icon: 'none'
					});
				}
			},

			testSession() {
				try {
					// 测试会话功能
					const user = this.$session.getUser();
					this.sessionOk = true;
					uni.showModal({
						title: '会话状态',
						content: user ? `当前用户: ${JSON.stringify(user)}` : '未登录',
						showCancel: false
					});
				} catch (error) {
					this.sessionOk = false;
					uni.showToast({
						title: '会话服务异常',
						icon: 'none'
					});
				}
			}
		}
	};
</script>

<style lang="scss" scoped>
.test-container {
	min-height: 100vh;
	background: var(--bg-secondary);
}

.modern-navbar {
	background: var(--primary-gradient);
	padding: calc(var(--spacing-lg) + var(--status-bar-height)) var(--spacing-lg) var(--spacing-xl);
	text-align: center;
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

.navbar-title {
	font-size: 36rpx;
	font-weight: 700;
	color: white;
	margin-bottom: var(--spacing-xs);
	text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.navbar-subtitle {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.8);
	font-weight: 300;
}

.test-content {
	padding: var(--spacing-lg);
	padding-bottom: calc(var(--spacing-xl) + env(safe-area-inset-bottom));
}

.test-result {
	margin-bottom: var(--spacing-xl);
	background: var(--success-color);
	color: white;
	border: none;
}

.result-header {
	display: flex;
	align-items: center;
}

.result-icon {
	width: 80rpx;
	height: 80rpx;
	background: rgba(255, 255, 255, 0.2);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: var(--spacing-md);

	.iconfont {
		font-size: 40rpx;
		color: white;
	}
}

.result-info {
	flex: 1;
}

.result-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: var(--spacing-xs);
}

.result-desc {
	font-size: 24rpx;
	opacity: 0.8;
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	color: var(--text-primary);
	margin-bottom: var(--spacing-lg);
}

.test-group {
	margin-bottom: var(--spacing-xl);
}

.test-label {
	font-size: 26rpx;
	color: var(--text-secondary);
	margin-bottom: var(--spacing-md);
	font-weight: 500;
}

.button-group {
	display: flex;
	flex-wrap: wrap;
	gap: var(--spacing-md);
}

.modern-btn {
	flex: 1;
	min-width: 120rpx;
}

.input-wrapper {
	margin-bottom: var(--spacing-md);
}

.card-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: var(--spacing-md);
}

.mini-card {
	background: var(--bg-tertiary);
	border-radius: var(--radius-lg);
	padding: var(--spacing-md);
	text-align: center;
}

.mini-card-title {
	font-size: 26rpx;
	font-weight: 600;
	color: var(--text-primary);
	margin-bottom: var(--spacing-xs);
}

.mini-card-desc {
	font-size: 22rpx;
	color: var(--text-secondary);
}

.nav-buttons {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: var(--spacing-md);
}

.nav-btn {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: var(--spacing-xs);
	padding: var(--spacing-lg);
	background: var(--bg-tertiary);
	border: 2px solid var(--border-color);
	border-radius: var(--radius-lg);
	transition: all 0.3s ease;

	&:active {
		transform: scale(0.95);
		border-color: var(--primary-color);
	}

	.iconfont {
		font-size: 40rpx;
		color: var(--primary-color);
	}

	text {
		font-size: 24rpx;
		color: var(--text-primary);
		font-weight: 500;
	}
}

.test-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: var(--spacing-md);
	background: var(--bg-tertiary);
	border-radius: var(--radius-md);
	margin-bottom: var(--spacing-md);

	&:last-child {
		margin-bottom: 0;
	}
}

.test-info {
	display: flex;
	align-items: center;
	flex: 1;
}

.test-name {
	font-size: 28rpx;
	color: var(--text-primary);
	margin-right: var(--spacing-md);
}

.test-status {
	padding: var(--spacing-xs) var(--spacing-md);
	border-radius: var(--radius-sm);
	font-size: 22rpx;
	font-weight: 500;

	&.success {
		background: rgba(16, 185, 129, 0.1);
		color: var(--success-color);
	}

	&.error {
		background: rgba(239, 68, 68, 0.1);
		color: var(--error-color);
	}
}

.test-action-btn {
	padding: var(--spacing-sm) var(--spacing-md);
	background: var(--primary-color);
	color: white;
	border: none;
	border-radius: var(--radius-sm);
	font-size: 24rpx;
}

.info-list {
	display: flex;
	flex-direction: column;
	gap: var(--spacing-md);
}

.info-item {
	display: flex;
	align-items: center;
}

.info-label {
	font-size: 26rpx;
	color: var(--text-secondary);
	min-width: 140rpx;
}

.info-value {
	font-size: 26rpx;
	color: var(--text-primary);
	font-weight: 500;
}
</style>