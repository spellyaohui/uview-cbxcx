<template>
	<view class="myreport-container">
		<!-- 现代化导航栏 -->
		<view class="modern-navbar">
			<view class="navbar-back" @click="goBack">
				<text class="fas fa-arrow-left"></text>
			</view>
			<view class="navbar-content">
				<view class="navbar-title">我的报告</view>
				<view class="navbar-subtitle">个人健康报告管理</view>
			</view>
			<view class="navbar-action" @click="refreshReports">
				<text class="fas fa-sync-alt"></text>
			</view>
		</view>

		<!-- 主体内容 -->
		<view class="myreport-content">
			<!-- 统计卡片 -->
			<view v-if="hqbgls.code == 200 && hqbgls.data.length > 0" class="stats-card modern-card fade-in-up">
				<view class="stats-header">
					<view class="stats-icon">
						<text class="fas fa-folder"></text>
					</view>
					<view class="stats-info">
						<view class="stats-title">报告统计</view>
						<view class="stats-desc">您的健康报告总览</view>
					</view>
				</view>
				<view class="stats-content">
					<view class="stat-item">
						<view class="stat-number">{{ hqbgls.data.length }}</view>
						<view class="stat-label">总报告数</view>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item">
						<view class="stat-number">{{ todayReports }}</view>
						<view class="stat-label">今日新增</view>
					</view>
				</view>
			</view>

			<!-- 筛选和排序 -->
			<view v-if="hqbgls.code == 200 && hqbgls.data.length > 0" class="filter-section modern-card fade-in-up">
				<view class="filter-header">
					<text class="filter-title">筛选排序</text>
				</view>
				<view class="filter-options">
					<view class="filter-group">
						<view class="filter-label">排序方式</view>
						<view class="filter-buttons">
							<button
								class="filter-btn"
								:class="{ active: sortBy === 'date' }"
								@click="sortBy = 'date'"
							>
								按时间
							</button>
							<button
								class="filter-btn"
								:class="{ active: sortBy === 'name' }"
								@click="sortBy = 'name'"
							>
								按名称
							</button>
						</view>
					</view>
				</view>
			</view>

			<!-- 报告列表 -->
			<view v-if="hqbgls.code == 200" class="reports-section">
				<view v-if="hqbgls.data.length > 0" class="section-title">
					<text class="fas fa-list"></text>
					<text>报告列表</text>
					<view class="section-count">{{ hqbgls.data.length }}个报告</view>
				</view>

				<view
					v-for="(item, index) in sortedReports"
					:key="item.cxid"
					class="report-card modern-card slide-in-right"
					:style="`animation-delay: ${index * 0.1}s`"
				>
					<!-- 报告头部 -->
					<view class="report-header">
						<view class="report-avatar">
							<text class="fas fa-file-alt"></text>
						</view>
						<view class="report-basic-info">
							<view class="report-name">{{ item.PATHNAME || '未命名报告' }}</view>
							<view class="report-id">ID: {{ item.cxid }}</view>
						</view>
						<view class="report-menu" @click="showReportMenu(item)">
							<text class="fas fa-ellipsis-v"></text>
						</view>
					</view>

					<!-- 报告详情 -->
					<view class="report-details">
						<view class="detail-item">
							<view class="detail-label">接收人</view>
							<view class="detail-value">{{ item.note || '未知' }}</view>
						</view>
						<view class="detail-item">
							<view class="detail-label">提交时间</view>
							<view class="detail-value">{{ formatDateTime(item.date) }}</view>
						</view>
					</view>

					<!-- 操作按钮 -->
					<view class="report-actions">
						<button class="action-btn download-btn" @click="downloadReport(item)">
							<text class="fas fa-download"></text>
							<text>下载</text>
						</button>
						<button class="action-btn delete-btn" @click="confirmDelete(item)">
							<text class="fas fa-trash"></text>
							<text>删除</text>
						</button>
					</view>
				</view>

				<!-- 空状态 -->
				<view v-if="hqbgls.data.length === 0" class="empty-state">
					<view class="empty-icon">
						<text class="fas fa-inbox"></text>
					</view>
					<view class="empty-title">暂无报告</view>
					<view class="empty-desc">您还没有任何健康报告</view>
					<button class="modern-btn modern-btn-primary" @click="goToReportQuery">
						去查询报告
					</button>
				</view>
			</view>

			<!-- 下载进度模态框 -->
			<view v-if="showDownloadModal" class="modal-overlay">
				<view class="download-modal modern-modal" @click.stop>
					<view class="modal-header">
						<view class="modal-title">下载报告</view>
					</view>

					<view class="modal-content">
						<view class="download-info">
							<view class="download-icon">
								<view class="icon-circle" :class="downloadStatus">
									<text class="fas" :class="getDownloadIcon()"></text>
								</view>
							</view>
							<view class="download-details">
								<view class="download-filename" :title="downloadingReport?.PATHNAME || '未知报告'">
									{{ downloadingReport?.PATHNAME || '未知报告' }}
								</view>
								<view class="download-status-text">
									{{ getDownloadStatusText() }}
								</view>
							</view>
						</view>

						<!-- 进度条 -->
						<view class="progress-container">
							<view class="progress-bar">
								<view class="progress-fill" :style="{ width: downloadProgress + '%' }"></view>
							</view>
							<view class="progress-text">{{ downloadProgress }}%</view>
						</view>

						<!-- 状态描述 -->
						<view class="status-description">
							<text v-if="downloadStatus === 'preparing'">正在准备下载链接...</text>
							<text v-else-if="downloadStatus === 'downloading'">正在下载报告文件...</text>
							<text v-else-if="downloadStatus === 'completed'">下载完成，即将打开文件</text>
							<text v-else-if="downloadStatus === 'error'">下载失败，请重试</text>
						</view>
					</view>

					<view class="modal-footer">
						<button v-if="downloadStatus === 'error'" class="modern-btn modern-btn-primary" @click="showDownloadModal = false">
							关闭
						</button>
					</view>
				</view>
			</view>

			<!-- 删除确认模态框 -->
			<view v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
				<view class="modern-modal delete-modal" @click.stop>
					<view class="modal-header">
						<view class="modal-title">确认删除</view>
						<button class="modal-close" @click="closeDeleteModal">
							<text class="fas fa-times"></text>
						</button>
					</view>

					<view class="modal-content">
						<view class="delete-warning">
							<view class="warning-icon">
								<text class="fas fa-exclamation-triangle"></text>
							</view>
							<view class="warning-text">
								<view class="warning-title">确定要删除这个报告吗？</view>
								<view class="warning-desc">删除后无法恢复，请谨慎操作</view>
							</view>
						</view>

						<view v-if="selectedReport" class="delete-info">
							<view class="info-item">
								<text class="info-label">报告名称：</text>
								<text class="info-value">{{ selectedReport.PATHNAME }}</text>
							</view>
							<view class="info-item">
								<text class="info-label">报告ID：</text>
								<text class="info-value">{{ selectedReport.cxid }}</text>
							</view>
						</view>
					</view>

					<view class="modal-footer">
						<button class="modern-btn modern-btn-secondary" @click="closeDeleteModal">
							取消
						</button>
						<button class="modern-btn modern-btn-error" @click="deleteReport">
							确认删除
						</button>
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
				// 用户全局状态
				userInfo: {},
				// 页面传参
				globalOption: {},
				// 自定义全局变量
				globalData: { PATHNAME: '', url: '' },
				hqbglsNum: 1,
				hqbgls: {
					code: 0,
					data: []
				},
				xzbg: {
					code: 0,
					msg: '',
					PATHNAME: '',
					url: ''
				},
				deletereport: ``,
				navbarHeight: 0,
				loading: false,
				sortBy: 'date',
				showDeleteModal: false,
				selectedReport: null,
				todayReports: 0,
				// 防重复提交
				deleting: false,
				// 下载状态
				downloading: false,
				// 下载进度相关
				showDownloadModal: false,
				downloadingReport: null,
				downloadProgress: 0,
				downloadStatus: 'preparing', // preparing, downloading, completed, error
			};
		},
		computed: {
			// 排序后的报告列表
			sortedReports() {
				if (!this.hqbgls.data || this.hqbgls.data.length === 0) {
					return [];
				}

				const reports = [...this.hqbgls.data];

				return reports.sort((a, b) => {
					if (this.sortBy === 'date') {
						// 按时间排序
						return new Date(b.date) - new Date(a.date);
					} else if (this.sortBy === 'name') {
						// 按名称排序
						return (a.PATHNAME || '').localeCompare(b.PATHNAME || '');
					}
					return 0;
				});
			}
		},
		onShow() {
			this.setCurrentPage(this);
			// 重新加载数据，确保从其他页面返回时数据是最新的
			this.init();
			// 页面显示时获取最新的主题设置
			this.updateThemeFromAPI();
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
				await this.hqbglsApi();
				this.calculateTodayReports();
			},

			// 计算今天报告数量
			calculateTodayReports() {
				if (!this.hqbgls.data) return;

				const today = new Date().toDateString();
				this.todayReports = this.hqbgls.data.filter(report => {
					const reportDate = new Date(report.date).toDateString();
					return reportDate === today;
				}).length;
			},

			// 返回上一页
			goBack() {
				uni.navigateBack();
			},

			// 刷新报告列表
			async refreshReports() {
				this.loading = true;
				try {
					await this.hqbglsApi({ refresh: true });
					this.calculateTodayReports();
					uni.showToast({
						title: '刷新成功',
						icon: 'success'
					});
				} catch (error) {
					uni.showToast({
						title: '刷新失败',
						icon: 'none'
					});
				} finally {
					this.loading = false;
				}
			},

			// 跳转到报告查询页面
			goToReportQuery() {
				uni.navigateTo({
					url: '/pages/report'
				});
			},

			// 格式化日期时间
			formatDateTime(dateStr) {
				if (!dateStr) return '未知时间';

				try {
					const date = new Date(dateStr);
					const now = new Date();
					const diffTime = now - date;
					const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

					if (diffDays === 0) {
						return '今天';
					} else if (diffDays === 1) {
						return '昨天';
					} else if (diffDays < 7) {
						return `${diffDays}天前`;
					} else {
						return date.toLocaleDateString('zh-CN');
					}
				} catch (error) {
					return dateStr;
				}
			},

			// 显示报告菜单
			showReportMenu(item) {
				uni.showActionSheet({
					itemList: ['下载报告', '删除报告', '查看详情'],
					success: (res) => {
						switch (res.tapIndex) {
							case 0:
								this.downloadReport(item);
								break;
							case 1:
								this.confirmDelete(item);
								break;
							case 2:
								this.viewReportDetails(item);
								break;
						}
					}
				});
			},

			// 查看报告详情
			viewReportDetails(item) {
				uni.showModal({
					title: '报告详情',
					content: `报告名称：${item.PATHNAME}\\n接收人：${item.note}\\n提交时间：${item.date}\\n报告ID：${item.cxid}`,
					showCancel: false
				});
			},

			// 下载报告
			async downloadReport(item) {
				if (this.downloading) return;
				this.downloading = true;
				this.downloadingReport = item;
				this.downloadProgress = 0;
				this.showDownloadModal = true;

				try {
					await this.xzbgApi({
						reportid: item.cxid
					});
				} catch (error) {
					this.showDownloadModal = false;
					this.downloadingReport = null;
					uni.showToast({
						title: '下载失败', 
						icon: 'none'
					});
				} finally {
					this.downloading = false;
				}
			},

			// 删除报告
			async deleteReport() {
				if (!this.selectedReport || this.deleting) return;
				this.deleting = true;
				
				uni.showLoading({
					title: '删除中...'
				});

				try {
					await this.deletereportApi({
						reportid: this.selectedReport.cxid
					});

					uni.hideLoading();
					uni.showToast({
						title: '删除成功',
						icon: 'success'
					});

					this.closeDeleteModal();
					// 刷新列表
					await this.hqbglsApi();
					this.calculateTodayReports();
				} catch (error) {
					uni.hideLoading();
					uni.showToast({
						title: '删除失败',
						icon: 'none'
					});
				} finally {
					this.deleting = false;
				}
			},

			// 获取报告历史 API请求方法
			async hqbglsApi(param) {
				let thiz = this;
				param = param || {};

				// 如果是刷新，重置页码
				if (param.refresh) {
					this.hqbglsNum = 1;
				}

				// 请求地址及请求数据
				let http_url = '/dzbg/hqbgls';
				let http_data = {
					pagenum: this.hqbglsNum,
					pagesize: 10,
					reportid: param.reportid || this.globalOption.id
				};
				let http_header = {
					'Content-Type': 'application/json'
				};

				try {
					let hqbgls = await this.$http.post(http_url, http_data, http_header, 'json');
					this.hqbgls = hqbgls;

					if (hqbgls.code == 301) {
						this.$session.clearUser();
						this.showToast('请先登录');
						uni.reLaunch({
							url: '/pages/index'
						});
						return;
					}

					if (hqbgls.code == 404) {
						// 无数据是正常情况，不显示错误提示
						return;
					}

					if (hqbgls.code != 200) {
						uni.showToast({
							title: hqbgls.msg || '获取报告列表失败',
							icon: 'none'
						});
					}
				} catch (error) {
					console.error('获取报告列表失败:', error);
					uni.showToast({
						title: '网络错误，请重试',
						icon: 'none'
					});
				}
			},

			// 下载报告 API请求方法
			async xzbgApi(param) {
				let thiz = this;
				param = param || {};

				// 请求地址及请求数据
				let http_url = '/dzbg/xzbg';
				let http_data = {
					reportid: param.reportid
				};
				let http_header = {
					'Content-Type': 'application/json'
				};

				try {
					// 模拟准备阶段进度
					this.downloadStatus = 'preparing';
					let progress = 0;
					const prepareInterval = setInterval(() => {
						progress += 10;
						this.downloadProgress = Math.min(progress, 30);
						if (progress >= 30) {
							clearInterval(prepareInterval);
						}
					}, 100);

					let xzbg = await this.$http.post(http_url, http_data, http_header, 'json');
					this.xzbg = xzbg;

					if (xzbg.code == 200) {
						var url = xzbg.url;
						this.downloadStatus = 'downloading';
						this.downloadProgress = 30;

						// #ifdef APP-PLUS
						let dtask = plus.downloader.createDownload(
							url,
							{
								filename: 'file://storage/emulated/0/pdf/' + xzbg.PATHNAME
							},
							function (d, status) {
								if (status == 200) {
									thiz.downloadStatus = 'completed';
									thiz.downloadProgress = 100;

									setTimeout(() => {
										thiz.showDownloadModal = false;
										thiz.downloadingReport = null;

										let fileSaveUrl = plus.io.convertLocalFileSystemURL(d.filename);
										plus.runtime.openFile(d.filename);

										uni.showToast({
											title: '下载成功',
											icon: 'success'
										});
									}, 1000);
								} else {
									thiz.downloadStatus = 'error';
									plus.downloader.clear();

									setTimeout(() => {
										thiz.showDownloadModal = false;
										thiz.downloadingReport = null;
										uni.showToast({
											title: '下载失败',
											icon: 'none'
										});
									}, 1000);
								}
							}
						);

						// 监听下载进度
						dtask.addEventListener("statechanged", function (download, status) {
							if (download.state == 3 && download.downloadedSize > 0 && download.totalSize > 0) {
								let progress = Math.round((download.downloadedSize / download.totalSize) * 70);
								thiz.downloadProgress = 30 + progress;
							}
						}, false);

						dtask.start();
						// #endif

						// #ifdef H5
						// H5端直接打开链接
						this.downloadStatus = 'completed';
						this.downloadProgress = 100;

						setTimeout(() => {
							this.showDownloadModal = false;
							this.downloadingReport = null;
							window.open(url, '_blank');
						}, 1000);
						// #endif

						// #ifdef MP
						// 小程序端提示下载
						this.downloadStatus = 'error';
						setTimeout(() => {
							this.showDownloadModal = false;
							this.downloadingReport = null;
							uni.showModal({
								title: '下载提示',
								content: '请在APP中使用下载功能',
								showCancel: false
							});
						}, 1000);
						// #endif
					} else {
						this.downloadStatus = 'error';
						setTimeout(() => {
							this.showDownloadModal = false;
							this.downloadingReport = null;
							uni.showToast({
								title: xzbg.msg || '获取下载链接失败',
								icon: 'none'
							});
						}, 1000);
					}
				} catch (error) {
					this.downloadStatus = 'error';
					setTimeout(() => {
						this.showDownloadModal = false;
						this.downloadingReport = null;
						console.error('下载报告失败:', error);
						uni.showToast({
							title: '下载失败，请重试',
							icon: 'none'
						});
					}, 1000);
				}
			},

			// 报告删除 API请求方法
			async deletereportApi(param) {
				let thiz = this;
				param = param || {};

				// 请求地址及请求数据
				let http_url = '/dzbg/deletereport';
				let http_data = {
					reportid: param.reportid
				};
				let http_header = {
					'Content-Type': 'application/json'
				};

				try {
					let deletereport = await this.$http.post(http_url, http_data, http_header, 'json');
					this.deletereport = deletereport;

					if (deletereport.code == 200) {
						return true; // 删除成功
					} else {
						throw new Error(deletereport.msg || '删除失败');
					}
				} catch (error) {
					console.error('删除报告失败:', error);
					throw error;
				}
			},

			// 获取下载图标
			getDownloadIcon() {
				switch (this.downloadStatus) {
					case 'preparing':
						return 'fa-clock';
					case 'downloading':
						return 'fa-download';
					case 'completed':
						return 'fa-check';
					case 'error':
						return 'fa-exclamation-triangle';
					default:
						return 'fa-download';
				}
			},

			// 获取下载状态文本
			getDownloadStatusText() {
				switch (this.downloadStatus) {
					case 'preparing':
						return '准备中';
					case 'downloading':
						return '下载中';
					case 'completed':
						return '已完成';
					case 'error':
						return '下载失败';
					default:
						return '准备中';
				}
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
			},

			changeNavbarHeight(val) {
				this.navbarHeight = val;
			}
		},

		// 触底加载更多
		onReachBottom() {
			if (this.hqbgls.code == 200 && this.hqbgls.data.length > 0 && !this.loading) {
				this.hqbglsNum++;
				this.hqbglsApi();
			}
		}
	};
</script>

<style lang="scss" scoped>
.myreport-container {
	min-height: 100vh;
	background: var(--bg-secondary);
}

/* 现代化导航栏 */
.modern-navbar {
	background: var(--primary-gradient);
	padding: calc(var(--spacing-lg) + var(--status-bar-height)) var(--spacing-lg) var(--spacing-xl);
	display: flex;
	align-items: center;
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

.navbar-back,
.navbar-action {
	width: 80rpx;
	height: 80rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: rgba(255, 255, 255, 0.1);
	border-radius: 50%;

	.fas {
		font-size: 36rpx;
		color: white;
	}
}

.navbar-content {
	flex: 1;
	text-align: center;
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

/* 主体内容 */
.myreport-content {
	padding: var(--spacing-lg);
	padding-bottom: calc(var(--spacing-xl) + env(safe-area-inset-bottom));
}

/* 统计卡片 */
.stats-card {
	margin-bottom: var(--spacing-xl);
	background: var(--primary-gradient);
	color: white;
	border: none;
}

.stats-header {
	display: flex;
	align-items: center;
	margin-bottom: var(--spacing-lg);
}

.stats-icon {
	width: 80rpx;
	height: 80rpx;
	background: rgba(255, 255, 255, 0.2);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: var(--spacing-md);

	.fas {
		font-size: 40rpx;
		color: white;
	}
}

.stats-info {
	flex: 1;
}

.stats-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: var(--spacing-xs);
}

.stats-desc {
	font-size: 24rpx;
	opacity: 0.8;
}

.stats-content {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: var(--spacing-lg) 0;
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
	margin: 0 var(--spacing-xl);
}

/* 筛选区 */
.filter-section {
	margin-bottom: var(--spacing-xl);
}

.filter-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: var(--spacing-lg);
}

.filter-title {
	font-size: 28rpx;
	font-weight: 600;
	color: var(--text-primary);
}

.filter-group {
	margin-bottom: var(--spacing-md);

	&:last-child {
		margin-bottom: 0;
	}
}

.filter-label {
	font-size: 26rpx;
	color: var(--text-secondary);
	margin-bottom: var(--spacing-sm);
	font-weight: 500;
}

.filter-buttons {
	display: flex;
	gap: var(--spacing-sm);
}

.filter-btn {
	flex: 1;
	padding: var(--spacing-sm) var(--spacing-md);
	background: var(--bg-tertiary);
	border: 1px solid var(--border-color);
	border-radius: var(--radius-md);
	font-size: 26rpx;
	color: var(--text-secondary);
	transition: all 0.3s ease;

	&.active {
		background: var(--primary-color);
		color: white;
		border-color: var(--primary-color);
	}

	&:not(.active):active {
		background: var(--bg-secondary);
	}
}

/* 报告区域 */
.reports-section {
	margin-bottom: var(--spacing-xl);
}

.section-title {
	display: flex;
	align-items: center;
	justify-content: space-between;
	font-size: 32rpx;
	font-weight: 600;
	color: var(--text-primary);
	margin-bottom: var(--spacing-lg);

	.fas {
		font-size: 32rpx;
		margin-right: var(--spacing-sm);
		color: var(--primary-color);
	}
}

.section-count {
	font-size: 24rpx;
	color: var(--text-secondary);
	font-weight: normal;
}

/* 报告卡片 */
.report-card {
	margin-bottom: var(--spacing-lg);
	padding: 0;
	overflow: hidden;
	cursor: pointer;

	&:active {
		transform: scale(0.98);
	}
}

.report-header {
	display: flex;
	align-items: center;
	padding: var(--spacing-lg);
	background: var(--bg-primary);
	border-bottom: 1px solid var(--border-color);
}

.report-avatar {
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

.report-basic-info {
	flex: 1;
}

.report-name {
	font-size: 32rpx;
	font-weight: 600;
	color: var(--text-primary);
	margin-bottom: var(--spacing-xs);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.report-id {
	font-size: 24rpx;
	color: var(--text-tertiary);
}

.report-menu {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: var(--bg-tertiary);
	border-radius: 50%;

	.fas {
		font-size: 28rpx;
		color: var(--text-secondary);
	}
}

.report-details {
	padding: var(--spacing-lg);
}

.detail-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: var(--spacing-md);

	&:last-child {
		margin-bottom: 0;
	}
}

.detail-label {
	font-size: 26rpx;
	color: var(--text-secondary);
}

.detail-value {
	font-size: 28rpx;
	color: var(--text-primary);
	font-weight: 500;
	text-align: right;
	flex: 1;
	margin-left: var(--spacing-md);
}

.report-actions {
	display: flex;
	gap: var(--spacing-md);
	padding: var(--spacing-lg);
	background: var(--bg-tertiary);
	border-top: 1px solid var(--border-color);
}

.action-btn {
	display: flex;
	align-items: center;
	gap: var(--spacing-xs);
	flex: 1;
	height: 80rpx;
	border-radius: var(--radius-md);
	font-size: 28rpx;
	font-weight: 500;

	&.download-btn {
		background: var(--success-color);
		color: white;
		border: none;

		&:active {
			background: #059669;
		}
	}

	&.delete-btn {
		background: var(--error-color);
		color: white;
		border: none;

		&:active {
			background: #dc2626;
		}
	}

	.fas {
		font-size: 28rpx;
	}
}

/* 空状态 */
.empty-state {
	text-align: center;
	padding: var(--spacing-2xl) var(--spacing-lg);
}

.empty-icon {
	margin-bottom: var(--spacing-lg);

	.fas {
		font-size: 120rpx;
		color: var(--text-tertiary);
	}
}

.empty-title {
	font-size: 32rpx;
	font-weight: 600;
	color: var(--text-primary);
	margin-bottom: var(--spacing-sm);
}

.empty-desc {
	font-size: 26rpx;
	color: var(--text-secondary);
	line-height: 1.5;
	margin-bottom: var(--spacing-xl);
}


/* 下载进度模态框 */
.download-modal {
	max-width: 500rpx;
	width: 90%;
}

.download-info {
	display: flex;
	align-items: center;
	margin-bottom: var(--spacing-xl);
	gap: var(--spacing-md);
}

.download-icon {
	flex-shrink: 0; /* 防止图标被压缩 */
}

.icon-circle {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	position: relative;

	&.preparing {
		background: rgba(245, 158, 11, 0.1);

		.fas {
			color: var(--warning-color);
			font-size: 40rpx;
			animation: pulse 2s infinite;
		}
	}

	&.downloading {
		background: rgba(37, 99, 235, 0.1);

		.fas {
			color: var(--primary-color);
			font-size: 40rpx;
			animation: bounce 1s infinite;
		}
	}

	&.completed {
		background: rgba(16, 185, 129, 0.1);

		.fas {
			color: var(--success-color);
			font-size: 40rpx;
		}
	}

	&.error {
		background: rgba(239, 68, 68, 0.1);

		.fas {
			color: var(--error-color);
			font-size: 40rpx;
		}
	}
}

.download-details {
	flex: 1;
	min-width: 0; /* 关键：允许flex子项收缩 */
}

.download-filename {
	font-size: 30rpx;
	font-weight: 600;
	color: var(--text-primary);
	margin-bottom: var(--spacing-xs);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	width: 100%;
	display: block;
	word-break: break-all; /* 防止长单词溢出 */
}

.download-status-text {
	font-size: 24rpx;
	color: var(--text-secondary);
}

.progress-container {
	margin-bottom: var(--spacing-lg);
}

.progress-bar {
	width: 100%;
	height: 8rpx;
	background: var(--bg-tertiary);
	border-radius: 4rpx;
	overflow: hidden;
	margin-bottom: var(--spacing-sm);
}

.progress-fill {
	height: 100%;
	background: linear-gradient(90deg, var(--primary-color), var(--success-color));
	border-radius: 4rpx;
	transition: width 0.3s ease;
	position: relative;

	&::after {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
		animation: shimmer 2s infinite;
	}
}

.progress-text {
	text-align: center;
	font-size: 24rpx;
	color: var(--text-secondary);
	font-weight: 500;
}

.status-description {
	text-align: center;

	text {
		font-size: 26rpx;
		color: var(--text-secondary);
		line-height: 1.5;
	}
}

/* 删除模态框 */
.modal-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
	padding: var(--spacing-lg);
}

.delete-modal {
	max-width: 500rpx;
}

.modal-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: var(--spacing-lg);
	border-bottom: 1px solid var(--border-color);
}

.modal-title {
	font-size: 32rpx;
	font-weight: 600;
	color: var(--text-primary);
}

.modal-close {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: var(--bg-tertiary);
	border-radius: 50%;
	border: none;

	.fas {
		font-size: 28rpx;
		color: var(--text-secondary);
	}
}

.modal-content {
	padding: var(--spacing-lg);
}

.delete-warning {
	display: flex;
	align-items: flex-start;
	margin-bottom: var(--spacing-lg);
}

.warning-icon {
	width: 80rpx;
	height: 80rpx;
	background: rgba(239, 68, 68, 0.1);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: var(--spacing-md);

	.fas {
		font-size: 40rpx;
		color: var(--error-color);
	}
}

.warning-text {
	flex: 1;
}

.warning-title {
	font-size: 28rpx;
	font-weight: 600;
	color: var(--text-primary);
	margin-bottom: var(--spacing-xs);
}

.warning-desc {
	font-size: 24rpx;
	color: var(--text-secondary);
	line-height: 1.5;
}

.delete-info {
	background: var(--bg-tertiary);
	border-radius: var(--radius-md);
	padding: var(--spacing-md);
}

.info-item {
	display: flex;
	margin-bottom: var(--spacing-sm);

	&:last-child {
		margin-bottom: 0;
	}
}

.info-label {
	font-size: 24rpx;
	color: var(--text-secondary);
	min-width: 140rpx;
}

.info-value {
	font-size: 26rpx;
	color: var(--text-primary);
	flex: 1;
}

.modal-footer {
	display: flex;
	gap: var(--spacing-md);
	padding: var(--spacing-lg);
	border-top: 1px solid var(--border-color);
}

/* 动画 */
@keyframes spin {
	to {
		transform: rotate(360deg);
	}
}

@keyframes pulse {
	0%, 100% {
		opacity: 1;
		transform: scale(1);
	}
	50% {
		opacity: 0.8;
		transform: scale(1.05);
	}
}

@keyframes bounce {
	0%, 100% {
		transform: translateY(0);
	}
	50% {
		transform: translateY(-10rpx);
	}
}

@keyframes shimmer {
	0% {
		transform: translateX(-100%);
	}
	100% {
		transform: translateX(100%);
	}
}

/* 响应式调整 */
@media screen and (max-width: 750rpx) {
	.myreport-content {
		padding: var(--spacing-md);
	}

	.modern-navbar {
		padding: var(--spacing-md) var(--spacing-md) var(--spacing-lg);
	}

	.navbar-title {
		font-size: 32rpx;
	}

	.report-header {
		padding: var(--spacing-md);
	}

	.report-details {
		padding: var(--spacing-md);
	}

	.report-actions {
		padding: var(--spacing-md);
	}
}
</style>