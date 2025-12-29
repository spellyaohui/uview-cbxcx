<script>
import HeartbeatService from '@/common/HeartbeatService.js';
import KeepAliveManager from '@/common/KeepAliveManager.js';

export default {
	globalData: {
		userInfo: null,
		tabBar: [],
		homePage: '/pages/index',
		pages: ['/pages/index', '/pages/home', '/pages/report', '/pages/myreport'],
		userData: {},
		// 简化的主题状态
		currentTheme: 'light',
		isDarkMode: false,
		// 心跳服务实例
		heartbeatService: null,
		// 保活管理器实例
		keepAliveManager: null,
		// 应用版本号
		appVersion: '1.0.0'
	},
		async onLaunch() {
			// 初始化标准主题系统
			this.initStandardTheme();

			// 获取状态栏高度并设置全局变量
			this.setStatusBarHeight();

			// 增强型参数解析函数 - 处理URL Scheme和推送冷启动
						const parseIdFromArgs = (args) => {
							if (!args) return null;
							
							// 1. 处理URL Scheme参数 (spellcuibo?id=xxx)
							const urlParams = args.match(/id=([^&]*)/);
							if (urlParams) return decodeURIComponent(urlParams[1]);
							
							// 2. 处理推送冷启动参数 (JSON格式)
							try {
								const data = JSON.parse(args);
								
								// 根据您提供的推送数据结构进行解析
								const id = data?.payload?.payload?.id || // 深层嵌套ID
										  data?.payload?.id ||          // 中层ID
										  data?.id;                     // 最外层ID
								
								if (id) {
									console.log("从冷启动参数解析到ID:", id);
									return id;
								}
							} catch (e) {
								console.warn('参数非JSON格式，跳过解析');
							}
							
							return null;
						};
			
						// 统一的导航函数 - 确保跳转到报告页面
						const navigateToReport = (id) => {
							console.log("正在导航到报告页面，ID:", id);
							const targetUrl = `/pages/myreport?id=${encodeURIComponent(id)}`;
							
							// 关键修复：先导航到首页，再跳转到报告页
							uni.reLaunch({
								url: '/pages/home',
								success: () => {
									console.log("已重置导航栈到首页");
									// 延迟确保首页加载完成
									setTimeout(() => {
										uni.navigateTo({
											url: targetUrl,
											success: () => console.log(`导航成功: ${targetUrl}`),
											fail: (err) => {
												console.error('导航失败:', err);
												// 备选方案：使用重定向
												console.warn("尝试备选重定向方案...");
												uni.redirectTo({
													url: targetUrl,
													fail: (err) => console.error('备选导航也失败:', err)
												});
											}
										});
									}, 500);
								},
								fail: (err) => {
									console.error('重置首页失败:', err);
									// 直接尝试导航到报告页
									uni.navigateTo({
										url: targetUrl,
										fail: (err) => console.error('导航失败:', err)
									});
								}
							});
						};
			
						// 默认导航函数
						const navigateToDefault = () => {
							console.log("导航到默认页面");
							uni.switchTab({
								url: '/pages/index',
								fail: (err) => console.error('切换标签页失败:', err)
							});
						};
			
						// 导航到首页
						const navigateToHome = () => {
							console.log("导航到首页");
							uni.reLaunch({
								url: '/pages/home',
								fail: (err) => console.error('导航到首页失败:', err)
							});
						};
			
					// 检查更新功能
					const checkUpdate = async () => {
						console.log('检查应用更新...');
						// #ifdef APP-PLUS
						try {
							const { default: checkUpdateFn } = await import('@/uni_modules/uni-upgrade-center-app/utils/check-update');
							const result = await checkUpdateFn();
							// code=0 表示当前已是最新版本，不需要更新
							if (result?.code === 0) {
								console.log('当前已是最新版本，无需更新');
							} else if (result?.code > 0) {
								console.log('发现新版本，已弹出升级提示');
							}
						} catch (error) {
							// code=0 表示当前已是最新版本，不需要更新
							if (error?.code === 0) {
								console.log('当前已是最新版本');
							} else {
								console.log('检查更新失败或无更新:', error?.message || error);
							}
						}
						// #endif
					};
			
						// 处理登录状态跳转逻辑
						const handleLoginNavigation = async () => {
							// 无论是否登录，都先检查更新
							await checkUpdate();
							
							setTimeout(() => {
								if (this.$session?.getToken()) {
									console.log('用户已登录，导航到首页');
									navigateToHome();
								} else {
									console.log('用户未登录，停留在登录页');
									// 未登录时停留在登录页，不需要额外操作
								}
							}, 500);
						};
			
						// 获取启动参数并处理
						const launchArgs = plus.runtime.arguments;
						console.log("冷启动参数:", launchArgs);
						const launchId = parseIdFromArgs(launchArgs);
						
						// 关键修改：优先处理所有冷启动场景
						if (launchId) {
							console.log('冷启动参数发现ID:', launchId);
							navigateToReport(launchId);
						} else {
							// 没有ID参数，处理登录状态
							console.log('未发现ID参数，处理登录状态');
							handleLoginNavigation();
						}
			
						// 推送消息处理（热启动场景）
						uni.onPushMessage((res) => {
							console.log('收到推送消息:', JSON.stringify(res, null, 2));
							
							// 提取推送ID的通用方法 - 适配新的嵌套结构
							const extractPushId = (data) => {
								// 根据您提供的推送数据结构进行解析
								return data?.payload?.payload?.id ||  // 深层嵌套ID (res.data.payload.payload.id)
									   data?.payload?.id ||           // 中层ID (res.data.payload.id)
									   data?.id;                     // 直接ID (res.data.id)
							};
			
							switch (res.type) {
								case 'click': // 点击通知栏启动
									console.log("处理推送点击事件");
									const pushId = extractPushId(res.data);
									if (pushId) {
										console.log('推送点击携带ID:', pushId);
										navigateToReport(pushId);
									} else {
										console.warn('推送消息缺少ID参数', res);
										navigateToDefault();
									}
									break;
									
								case 'receive': // 收到推送（创建本地通知）
									console.log("处理推送接收事件");
									// 使用深层结构提取标题和内容
									const title = res.data?.payload?.payload?.title || 
												 res.data?.payload?.title || 
												 res.data?.title || 
												 '新消息';
												 
									const content = res.data?.payload?.payload?.content || 
												   res.data?.payload?.content || 
												   res.data?.content || 
												   '';
									
									uni.createPushMessage({
										title: title,
										content: content,
										payload: JSON.stringify(res.data) // 保存完整数据
									});
									break;
									
								default:
									console.warn('未知推送类型:', res.type);
							}
						});

			// 初始化心跳服务
			this.initHeartbeatService();
			
			// 初始化保活管理器
			this.initKeepAliveManager();

			// 登录成功后启动需要登录态的服务（心跳/保活）
			// 避免“启动时未登录 → 登录后不触发启动”的情况
			// #ifdef APP-PLUS
			try {
				uni.$off('userLogin');
				uni.$on('userLogin', () => {
					console.log('收到登录事件，尝试启动心跳/保活服务');
					this.startHeartbeatService();
					this.startKeepAliveService();
				});
			} catch (e) {
				console.warn('注册登录事件监听失败:', e);
			}
			// #endif
		},

		onShow() {
			// 解决 uniapp 设置自动登录跳转首页时加载登录页问题
			setTimeout(() => {
				// #ifdef APP-PLUS
				plus.navigator.closeSplashscreen();
				// #endif
			}, 2000);
		},

		// 官方主题变化回调
		onThemeChange(res) {
			console.log('系统主题变化:', res.theme);
			
			// 更新全局状态
			this.globalData.currentTheme = res.theme;
			this.globalData.isDarkMode = res.theme === 'dark';

			// 触发全局事件通知所有页面
			uni.$emit('themeChange', {
				theme: res.theme,
				isDark: res.theme === 'dark'
			});

			// 更新原生界面主题
			this.updateNativeTheme(res.theme);
		},

		methods: {
			// 初始化标准主题系统
			initStandardTheme() {
				console.log('初始化标准主题系统...');
				
				// 获取初始主题状态
				this.getCurrentTheme().then(theme => {
					console.log('应用启动时的主题:', theme);
				});

				// 初始化全局状态管理
				if (this.$store && this.$store.initTheme) {
					this.$store.initTheme();
				}
			},

			// 更新原生界面主题
			updateNativeTheme(theme) {
				const isDark = theme === 'dark';
				console.log('更新原生界面主题:', theme);

				// 设置导航栏主题
				// #ifdef APP-PLUS || H5
				uni.setNavigationBarColor({
					frontColor: isDark ? '#ffffff' : '#000000',
					backgroundColor: isDark ? '#1a1a1a' : '#07c160',
					animation: {
						duration: 300,
						timingFunc: 'easeIn'
					}
				});
				// #endif

				// 设置状态栏主题
				// #ifdef APP-PLUS
				if (typeof plus !== 'undefined' && plus.navigator) {
					plus.navigator.setStatusBarStyle(isDark ? 'light' : 'dark');
				}
				// #endif
			},

			// 获取当前主题
			getCurrentTheme() {
				return new Promise((resolve) => {
					uni.getSystemInfo({
						success: (res) => {
							const theme = res.theme || 'light';
							this.globalData.currentTheme = theme;
							this.globalData.isDarkMode = theme === 'dark';
							resolve(theme);
						},
						fail: () => {
							const defaultTheme = 'light';
							this.globalData.currentTheme = defaultTheme;
							this.globalData.isDarkMode = false;
							resolve(defaultTheme);
						}
					});
				});
			},

			// 设置状态栏高度
			setStatusBarHeight() {
				uni.getSystemInfo({
					success: (e) => {
						const statusBarHeight = e.statusBarHeight || 44;
						this.globalData.statusBarHeight = statusBarHeight;

						// 在页面加载后设置CSS变量
						this.$nextTick(() => {
							// 通过动态样式设置状态栏高度
							const style = document.createElement('style');
							style.textContent = `
								:root {
									--status-bar-height: ${statusBarHeight}px;
								}
							`;
							document.head.appendChild(style);
						});
					},
					fail: () => {
						// 获取失败时使用默认值
						const defaultHeight = 44;
						this.globalData.statusBarHeight = defaultHeight;

						this.$nextTick(() => {
							const style = document.createElement('style');
							style.textContent = `
								:root {
									--status-bar-height: ${defaultHeight}px;
								}
							`;
							document.head.appendChild(style);
						});
					}
				});
			},

			// 初始化心跳服务
			initHeartbeatService() {
				// 只在APP平台启用心跳服务
				// #ifdef APP-PLUS
				try {
					this.globalData.heartbeatService = new HeartbeatService();
					console.log('心跳服务已初始化');
					
					// 检查用户登录状态，如果已登录则启动心跳
					const token = this.$session?.getToken?.() || uni.getStorageSync('token');
					if (token) {
						// 延迟启动，确保应用完全初始化
						setTimeout(() => {
							this.startHeartbeatService();
						}, 5000); // 5秒后启动
					}
				} catch (error) {
					console.error('心跳服务初始化失败:', error);
				}
				// #endif
			},

			// 启动心跳服务
			startHeartbeatService() {
				// #ifdef APP-PLUS
				if (this.globalData.heartbeatService && !this.globalData.heartbeatService.isRunning) {
					this.globalData.heartbeatService.start();
					console.log('心跳服务已启动');
				}
				// #endif
			},

			// 停止心跳服务
			stopHeartbeatService() {
				// #ifdef APP-PLUS
				if (this.globalData.heartbeatService && this.globalData.heartbeatService.isRunning) {
					this.globalData.heartbeatService.stop();
					console.log('心跳服务已停止');
				}
				// #endif
			},

			// 获取心跳服务统计
			getHeartbeatStats() {
				// #ifdef APP-PLUS
				if (this.globalData.heartbeatService) {
					return this.globalData.heartbeatService.getHeartbeatStats();
				}
				// #endif
				return null;
			},
			
			// 初始化保活管理器
			async initKeepAliveManager() {
				// 只在Android平台启用保活功能
				// #ifdef APP-PLUS
				try {
					const systemInfo = uni.getSystemInfoSync();
					if (systemInfo.platform !== 'android') {
						console.log('保活功能仅支持Android平台');
						return;
					}
					
					console.log('初始化保活管理器...');
					this.globalData.keepAliveManager = new KeepAliveManager();
					
					// 初始化配置
					const config = {
						enabled: true,
						heartbeatInterval: 30000, // 30秒
						maxRetryCount: 3,
						notificationConfig: {
							title: '崔博小程序正在后台运行',
							content: '确保及时接收健康报告通知',
							icon: 'ic_notification',
							showProgress: false
						},
						adaptationConfig: {
							enableManufacturerOptimization: true,
							enableBatteryWhitelist: true,
							enableAutoStart: true
						}
					};
					
					const result = await this.globalData.keepAliveManager.init(config);
					if (result.success) {
						console.log('保活管理器初始化成功');
						// 无论是否已登录，都先启动保活服务（通知/前台服务才能拉起）
						setTimeout(() => {
							this.startKeepAliveService();
						}, 3000); // 3秒后启动
					} else {
						console.error('保活管理器初始化失败:', result.message);
					}
				} catch (error) {
					console.error('保活管理器初始化异常:', error);
				}
				// #endif
			},
			
			// 启动保活服务
			async startKeepAliveService() {
				// #ifdef APP-PLUS
				try {
					if (!this.globalData.keepAliveManager) {
						console.warn('保活管理器未初始化');
						return;
					}
					
					// 先检查权限（即便失败也继续尝试启动，避免挂死在未回调）
					try {
						const permissionResult = await this.globalData.keepAliveManager.checkPermissions();
						console.log('权限检查结果:', permissionResult);
					} catch (err) {
						console.warn('权限检查异常，继续尝试启动保活:', err?.message || err);
					}

					// 启动保活服务
					const result = await this.globalData.keepAliveManager.start();
					if (result.success) {
						console.log('保活服务启动成功');
						
						// 设置心跳事件监听
						this.setupHeartbeatListeners();
					} else {
						console.error('保活服务启动失败:', result.message);
					}
				} catch (error) {
					console.error('启动保活服务异常:', error);
				}
				// #endif
			},
			
			// 停止保活服务
			async stopKeepAliveService() {
				// #ifdef APP-PLUS
				try {
					if (this.globalData.keepAliveManager) {
						const result = await this.globalData.keepAliveManager.stop();
						if (result.success) {
							console.log('保活服务已停止');
						}
					}
				} catch (error) {
					console.error('停止保活服务异常:', error);
				}
				// #endif
			},
			
			// 设置心跳事件监听
			setupHeartbeatListeners() {
				// #ifdef APP-PLUS
				try {
					// 监听心跳事件
					uni.$on('heartbeat', (data) => {
						console.log('收到心跳数据:', data);
						
						// 可以在这里处理心跳数据，例如更新UI或发送到服务器
						// 如果需要发送到服务器，可以调用HeartbeatService
						if (this.globalData.heartbeatService && this.globalData.heartbeatService.isRunning) {
							// 心跳服务会自动处理
						}
					});
					
					// 监听保活状态变化
					uni.$on('keepAliveStatusChanged', (status) => {
						console.log('保活状态变化:', status);
					});
					
					// 监听保活错误
					uni.$on('keepAliveError', (error) => {
						console.error('保活服务错误:', error);
					});
					
					console.log('心跳事件监听器设置完成');
				} catch (error) {
					console.error('设置心跳事件监听器异常:', error);
				}
				// #endif
			},
			
			// 获取保活状态
			async getKeepAliveStatus() {
				// #ifdef APP-PLUS
				try {
					if (this.globalData.keepAliveManager) {
						return await this.globalData.keepAliveManager.getStatus();
					}
				} catch (error) {
					console.error('获取保活状态异常:', error);
				}
				// #endif
				return null;
			},
			
			// 获取本地心跳日志
			async getLocalHeartbeatLogs(count = 0) {
				// #ifdef APP-PLUS
				try {
					if (this.globalData.keepAliveManager) {
						const options = count > 0 ? { count } : {};
						return await this.globalData.keepAliveManager.getHeartbeatLogs(options);
					}
				} catch (error) {
					console.error('获取心跳日志异常:', error);
				}
				// #endif
				return null;
			},
			
			// 获取本地心跳统计
			async getLocalHeartbeatStats() {
				// #ifdef APP-PLUS
				try {
					if (this.globalData.keepAliveManager) {
						return await this.globalData.keepAliveManager.getHeartbeatStats();
					}
				} catch (error) {
					console.error('获取心跳统计异常:', error);
				}
				// #endif
				return null;
			}
		}
	};
</script>
<style lang="scss">
	// 引入现代化UI样式
	@import 'styles/modern-ui.scss';

	// 引入Font Awesome图标样式（本地图标字体解决方案）
	@import 'styles/font-awesome.scss';

	// 保留必要的自定义样式（移除diy相关引用）
	// @import 'common/diygw-ui/animate.css';

	// 基础样式重置
	* {
		box-sizing: border-box;
	}

	body {
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
		background-color: var(--bg-secondary);
		color: var(--text-primary);
		transition: background-color 0.3s ease, color 0.3s ease; // 添加平滑过渡效果
	}

	// 现代化容器
	.container {
		padding: var(--spacing-md);
		min-height: 100vh;
		background: var(--primary-gradient);
		transition: background 0.3s ease; // 添加平滑过渡效果
	}
	
	// 强制应用主题到根元素
	:root {
		--bg-primary: #ffffff;        /* 默认浅色模式 */
		--bg-secondary: #f8fafc;      /* 默认浅色模式 */
		--text-primary: #1f2937;      /* 默认浅色模式 */
		--primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	}
	
	/* 深色模式的强制样式 */
	.theme-dark {
		--bg-primary: #1e293b;
		--bg-secondary: #0f172a;
		--text-primary: #f1f5f9;
		--primary-gradient: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
		
		background-color: var(--bg-secondary) !important;
		color: var(--text-primary) !important;
	}
</style>