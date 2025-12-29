<template>
	<view class="login-container">
		<!-- 状态栏占位区域 -->
		<view class="status-bar-placeholder"></view>

		<!-- 顶部装饰元素 -->
		<view class="login-header">
			<view class="header-decoration"></view>
		</view>

		<!-- 主体内容区域 -->
		<view class="login-content">
			<!-- Logo区域 -->
			<view class="logo-section fade-in-up">
				<view class="logo-container">
					<image src="/static/cxtb.png" class="logo-image" mode="aspectFit"></image>
				</view>
				<view class="app-title">崔博小程序</view>
				<view class="app-subtitle">专业健康管理平台</view>
			</view>

			<!-- 登录表单 -->
			<view class="login-form slide-in-right">
				<view class="form-title">欢迎登录</view>

				<!-- 用户名输入 -->
				<view class="form-item">
					<view class="input-icon">
						<text class="fas fa-user"></text>
					</view>
					<input
						class="modern-input"
						placeholder="请输入登录账户"
						v-model="username"
						placeholder-class="input-placeholder"
						@confirm="handleLoginOnEnter"
					/>
				</view>

				<!-- 密码输入 -->
				<view class="form-item">
					<view class="input-icon">
						<text class="fas fa-lock"></text>
					</view>
					<input
						class="modern-input"
						placeholder="请输入密码"
						v-model="password"
						password="true"
						placeholder-class="input-placeholder"
						@confirm="handleLoginOnEnter"
					/>
				</view>

				<!-- 登录按钮 -->
				<view class="login-button-container">
					<button
						class="modern-btn modern-btn-primary login-button"
						@click="handleLogin"
						:disabled="!username || !password"
					>
						<text v-if="!loading">立即登录</text>
						<text v-else class="loading-text">登录中...</text>
					</button>
				</view>

				<!-- 装饰性分隔线 -->
				<view class="divider">
					<view class="divider-line"></view>
					<text class="divider-text">其他登录方式</text>
					<view class="divider-line"></view>
				</view>

				<!-- 底部提示 -->
				<view class="login-tips">
					<text class="tips-text">首次登录请联系管理员获取账户</text>
				</view>
			</view>
		</view>

		<!-- 底部装饰 -->
		<view class="login-footer">
			<view class="footer-decoration"></view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				//用户全局信息
				userInfo: {},
				//页面传参
				globalOption: {},
				//自定义全局变量
				globalData: { cid: 'null' },
				logindata: {
					code: 0,
					msg: ''
				},
				username: '',
				password: '',
				loading: false
			};
		},
		onShow() {
			this.setCurrentPage(this);

			// 确保状态栏样式正确
			// #ifdef APP-PLUS
			setTimeout(() => {
				plus.navigator.setStatusBarBackground('transparent');
				plus.navigator.setStatusBarStyle('light');
			}, 50);
			// #endif
		},
		onLoad(option) {
			if (option) {
				this.setData({
					globalOption: this.getOption(option)
				});
			}
			this.setCurrentPage(this);
			this.init();

			// 强制设置状态栏样式
			// #ifdef APP-PLUS
			setTimeout(() => {
				plus.navigator.setStatusBarBackground('transparent');
				plus.navigator.setStatusBarStyle('light');
			}, 100);
			// #endif
		},
		methods: {
			async init() {
				await this.getcidFunction();
			},

			// 处理登录
			async handleLogin() {
				if (!this.username || !this.password) {
					uni.showToast({
						title: '请输入用户名和密码',
						icon: 'none',
						duration: 2000
					});
					return;
				}

				// 设置加载状态
				this.loading = true;

				try {
					await this.logindataApi({
						lxdh: this.username,
						password: this.password,
						cid: this.globalData.cid
					});
				} catch (error) {
					console.error('登录失败:', error);
					uni.showToast({
						title: '登录失败，请重试',
						icon: 'none',
						duration: 2000
					});
				} finally {
					this.loading = false;
				}
			},

			// 回车键登录处理
			handleLoginOnEnter() {
				if (this.username && this.password) {
					this.handleLogin();
				}
			},

			// 用户登录 API请求方法
			async logindataApi(param) {
				let thiz = this;
				param = param || {};

				//请求地址及请求数据，可以在加载前执行上面增加自己的代码逻辑
				let http_url = '/sys/login';
				let http_data = {
					lxdh: param.lxdh || this.username,
					password: param.password || this.password,
					cid: param.cid || this.globalData.cid
				};
				let http_header = {
					'Content-Type': 'application/json'
				};

				let logindata = await this.$http.post(http_url, http_data, http_header, 'json');

				this.logindata = logindata;
				if (logindata.code == 404) {
					this.showModal(logindata.msg, '提示', false);
					return;
				}
				if (logindata.code == 200) {
					// 处理用户数据，确保name字段被正确保存
					const userData = logindata.data;
					console.log('登录返回的用户数据:', userData);

					// 确保数据结构包含必要字段
					const processedUserData = {
						...userData,
						// 如果没有name字段但有username，则复制一份作为name
						name: userData.name || userData.username || '用户',
						username: userData.username || userData.lxdh || ''
					};

					console.log('处理后的用户数据:', processedUserData);

					this.$session.setUser(processedUserData);
					// 通知 App：登录已完成（用于启动保活/心跳等依赖登录态的服务）
					uni.$emit('userLogin', processedUserData);
					uni.showToast({
						title: '登录成功',
						icon: 'success',
						duration: 1500
					});

					// 延迟跳转，让用户看到成功提示
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/home'
						});
					}, 1500);
				}
			},

			// 获取cid 自定义方法
			async getcidFunction(param) {
				let thiz = this;
				uni.getPushClientId({
					success: (res) => {
						console.log('获取客户端ID成功:', res.cid);
						this.globalData.cid = res.cid;
					},
					fail: (err) => {
						console.error('获取客户端ID失败:', err);
					}
				});
			}
		}
	};
</script>

<style lang="scss" scoped>
.login-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	position: relative;
	overflow: hidden;
	/* 确保背景覆盖整个屏幕包括状态栏 */
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
}

/* 状态栏占位区域 */
.status-bar-placeholder {
	height: var(--status-bar-height);
	width: 100%;
	background: transparent;
	position: fixed;
	top: 0;
	left: 0;
	z-index: 1;
}

/* 顶部装饰 */
.login-header {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	height: 300rpx;
	z-index: 1;
}

.header-decoration {
	position: absolute;
	top: -100rpx;
	right: -100rpx;
	width: 400rpx;
	height: 400rpx;
	background: rgba(255, 255, 255, 0.1);
	border-radius: 50%;
	filter: blur(80rpx);
}

/* 底部装饰 */
.login-footer {
	position: absolute;
	bottom: 0;
	left: 0;
	right: 0;
	height: 200rpx;
	z-index: 1;
}

.footer-decoration {
	position: absolute;
	bottom: -50rpx;
	left: -50rpx;
	width: 300rpx;
	height: 300rpx;
	background: rgba(255, 255, 255, 0.05);
	border-radius: 50%;
	filter: blur(60rpx);
}

/* 主体内容 */
.login-content {
	position: relative;
	z-index: 2;
	padding: var(--spacing-2xl) var(--spacing-lg);
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	justify-content: center;
	/* 使用状态栏高度变量，确保内容不被遮挡 */
	padding-top: calc(var(--spacing-2xl) + var(--status-bar-height));
}

/* Logo区域 */
.logo-section {
	text-align: center;
	margin-bottom: var(--spacing-2xl);
}

.logo-container {
	margin-bottom: var(--spacing-lg);
}

.logo-image {
	width: 180rpx;
	height: 180rpx;
	border-radius: 24rpx;
	box-shadow: var(--shadow-xl);
}

.app-title {
	font-size: 48rpx;
	font-weight: 700;
	color: white;
	margin-bottom: var(--spacing-xs);
	text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.app-subtitle {
	font-size: 28rpx;
	color: rgba(255, 255, 255, 0.8);
	font-weight: 300;
}

/* 登录表单 */
.login-form {
	background: rgba(255, 255, 255, 0.95);
	backdrop-filter: blur(20rpx);
	border-radius: var(--radius-2xl);
	padding: var(--spacing-2xl);
	box-shadow: var(--shadow-xl);
	border: 1px solid rgba(255, 255, 255, 0.2);
}

.form-title {
	font-size: 36rpx;
	font-weight: 600;
	color: var(--text-primary);
	text-align: center;
	margin-bottom: var(--spacing-xl);
}

/* 表单项 */
.form-item {
	position: relative;
	margin-bottom: var(--spacing-lg);
	display: flex;
	align-items: center;
	background: var(--bg-secondary);
	border-radius: var(--radius-md);
	border: 2px solid var(--border-color);
	transition: all 0.3s ease;

	&:focus-within {
		border-color: var(--primary-color);
		background: var(--bg-primary);
		box-shadow: 0 0 0 6rpx rgba(37, 99, 235, 0.1);
	}
}

.input-icon {
	padding: 0 var(--spacing-md);
	display: flex;
	align-items: center;
	justify-content: center;

	.fas {
		font-size: 32rpx;
		color: var(--text-secondary);
	}
}

.modern-input {
	flex: 1;
	padding: var(--spacing-md) var(--spacing-md) var(--spacing-md) 0;
	font-size: 30rpx;
	color: var(--text-primary);
	background: transparent;
	border: none;
	outline: none;
	min-height: 88rpx;
}

.input-placeholder {
	color: var(--text-tertiary);
	font-size: 30rpx;
}

/* 登录按钮 */
.login-button-container {
	margin-top: var(--spacing-xl);
}

.login-button {
	width: 100%;
	height: 96rpx;
	font-size: 32rpx;
	font-weight: 600;
	border-radius: var(--radius-lg);
	position: relative;
	overflow: hidden;

	&:disabled {
		opacity: 0.6;
		transform: none;
	}

	&:not(:disabled):active {
		transform: scale(0.98);
	}
}

.loading-text {
	position: relative;

	&::after {
		content: '';
		display: inline-block;
		width: 20rpx;
		height: 20rpx;
		border: 3rpx solid rgba(255, 255, 255, 0.3);
		border-top-color: white;
		border-radius: 50%;
		animation: spin 1s linear infinite;
		margin-left: var(--spacing-sm);
	}
}

/* 分隔线 */
.divider {
	display: flex;
	align-items: center;
	margin: var(--spacing-xl) 0 var(--spacing-lg);
}

.divider-line {
	flex: 1;
	height: 2rpx;
	background: var(--border-color);
}

.divider-text {
	padding: 0 var(--spacing-lg);
	font-size: 24rpx;
	color: var(--text-tertiary);
}

/* 底部提示 */
.login-tips {
	text-align: center;
}

.tips-text {
	font-size: 24rpx;
	color: var(--text-tertiary);
	line-height: 1.5;
}

/* 动画 */
@keyframes spin {
	to {
		transform: rotate(360deg);
	}
}

/* 响应式调整 */
@media screen and (max-width: 750rpx) {
	.login-content {
		padding: var(--spacing-xl) var(--spacing-md);
	}

	.login-form {
		padding: var(--spacing-xl);
	}

	.logo-image {
		width: 140rpx;
		height: 140rpx;
	}

	.app-title {
		font-size: 40rpx;
	}
}
</style>