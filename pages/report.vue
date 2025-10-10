<template>
	<view class="report-container">
		<!-- 现代化导航栏 -->
		<view class="modern-navbar">
			<view class="navbar-back" @click="goBack">
				<text class="fas fa-arrow-left"></text>
			</view>
			<view class="navbar-content">
				<view class="navbar-title">报告查询</view>
				<view class="navbar-subtitle">电子健康报告系统</view>
			</view>
		</view>

		<!-- 主体内容 -->
		<view class="report-content">
			<!-- 搜索区域 -->
			<view class="search-section modern-card fade-in-up">
				<view class="section-title">
					<text class="fas fa-search"></text>
					<text>查询条件</text>
				</view>

				<!-- 文本输入搜索 -->
				<view class="search-input-group">
					<view class="input-label">查询信息</view>
					<view class="search-input-wrapper">
						<view class="input-icon">
							<text class="fas fa-user"></text>
						</view>
						<input
							class="modern-input search-input"
							placeholder="身份证/姓名/联系电话/ID"
							v-model="text_ycxsj"
							placeholder-class="input-placeholder"
						/>
					</view>
				</view>

				<!-- 扫码输入 -->
				<view class="scan-input-group">
					<view class="input-label">扫描条码</view>
					<view class="scan-input-wrapper">
						<view class="input-icon">
							<text class="fas fa-qrcode"></text>
						</view>
						<input
							class="modern-input scan-input"
							placeholder="请扫描条形码"
							v-model="scaninput_smcx"
							:focus="scaninput_smcxFocus"
							placeholder-class="input-placeholder"
						/>
						<button class="scan-button" @click="showScaninput_smcx">
							<text class="fas fa-qrcode"></text>
							<text>扫码</text>
						</button>
					</view>
				</view>

				<!-- 查询按钮 -->
				<view class="search-button-container">
					<button
						class="modern-btn modern-btn-primary search-button"
						@click="handleSearch"
						:disabled="!text_ycxsj && !scaninput_smcx"
					>
						<text v-if="!searching">开始查询</text>
						<text v-else class="loading-text">查询中...</text>
					</button>
				</view>
			</view>

			<!-- 查询结果统计 -->
			<view v-if="cxgrxx.code == 200 && cxgrxx.data.length > 0" class="stats-section modern-card fade-in-up">
				<view class="stats-content">
					<view class="stat-item">
						<view class="stat-number">{{ cxgrxx.data.length }}</view>
						<view class="stat-label">查询结果</view>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item">
						<view class="stat-number">{{ totalCount }}</view>
						<view class="stat-label">总记录数</view>
					</view>
				</view>
			</view>

			<!-- 查询结果列表 -->
			<view v-if="cxgrxx.code == 200" class="results-section">
				<view v-if="cxgrxx.data.length > 0" class="section-title">
					<text class="fas fa-list"></text>
					<text>查询结果</text>
				</view>

				<view
					v-for="(item, index) in cxgrxx.data"
					:key="index"
					class="report-card modern-card slide-in-right"
					:style="`animation-delay: ${index * 0.1}s`"
					@click="handleReportClick(item)"
				>
					<!-- 报告头部 -->
					<view class="report-header">
						<view class="report-avatar">
							<text class="fas fa-user-circle"></text>
						</view>
						<view class="report-basic-info">
							<view class="report-name">{{ item.姓名 || '未知' }}</view>
							<view class="report-id">ID: {{ item.ID }}</view>
						</view>
						<view class="report-status" :class="getStatusClass(item.汇总状态)">
							{{ item.汇总状态 || '未知' }}
						</view>
					</view>

					<!-- 报告详情网格 -->
					<view class="report-details">
						<view class="detail-row">
							<view class="detail-item">
								<view class="detail-label">性别</view>
								<view class="detail-value">{{ item.性别 || '未知' }}</view>
							</view>
							<view class="detail-item">
								<view class="detail-label">年龄</view>
								<view class="detail-value">{{ item.年龄 || '未知' }}</view>
							</view>
						</view>

						<view class="detail-row">
							<view class="detail-item full-width">
								<view class="detail-label">身份证号</view>
								<view class="detail-value">{{ formatIdCard(item.身份证号) }}</view>
							</view>
						</view>

						<view class="detail-row">
							<view class="detail-item full-width">
								<view class="detail-label">联系电话</view>
								<view class="detail-value">{{ item.联系电话 || '未知' }}</view>
							</view>
						</view>

						<view class="detail-row">
							<view class="detail-item full-width">
								<view class="detail-label">单位名称</view>
								<view class="detail-value text-ellipsis">{{ item.单位名称 || '未知' }}</view>
							</view>
						</view>

						<view class="detail-row">
							<view class="detail-item">
								<view class="detail-label">体检区域</view>
								<view class="detail-value">{{ item.体检区域 || '未知' }}</view>
							</view>
							<view class="detail-item">
								<view class="detail-label">体检日期</view>
								<view class="detail-value">{{ formatDate(item.体检日期) }}</view>
							</view>
						</view>

						<!-- 状态信息 -->
						<view class="status-section">
							<view class="status-item" v-if="item.未上传项目">
								<text class="status-label">未上传项目:</text>
								<text class="status-value">{{ item.未上传项目 }}</text>
							</view>
							<view class="status-item">
								<text class="status-label">微信状态:</text>
								<text class="status-value">{{ item.微信上传状态 || '未知' }}</text>
							</view>
						</view>
					</view>

					<!-- 操作按钮 -->
					<view class="report-actions">
						<button class="action-btn primary-btn" @click.stop="handleReportAction(item)">
							<text class="fas fa-cogs"></text>
							<text>处理报告</text>
						</button>
					</view>
				</view>

				<!-- 空状态 -->
				<view v-if="cxgrxx.data.length === 0 && hasSearched" class="empty-state">
					<view class="empty-icon">
						<text class="fas fa-inbox"></text>
					</view>
					<view class="empty-title">未找到相关报告</view>
					<view class="empty-desc">请检查查询条件或尝试其他关键词</view>
				</view>
			</view>
		</view>

		<!-- 报告处理模态框 -->
		<view v-if="modal_bgcl" class="modal-overlay" @click="closeModal_bgcl">
			<view class="modern-modal" @click.stop>
				<view class="modal-header">
					<view class="modal-title">报告处理</view>
					<button class="modal-close" @click="closeModal_bgcl">
						<text class="fas fa-times"></text>
					</button>
				</view>

				<view class="modal-content">
					<view v-if="globalData.ymbl == 1" class="partial-report">
						<view class="form-item">
							<view class="form-label">发送人</view>
							<input
								class="modern-input"
								placeholder="请输入报告发送人"
								v-model="input_bgfsr"
							/>
						</view>

						<view class="modal-actions">
							<button
								class="modern-btn modern-btn-error"
								@click="() => navigateTo({ type: 'tjbgjcApi', reportid: globalData.m_reportid, reportpeople: input_bgfsr })"
							>
								部分报告监测
							</button>
							<button
								class="modern-btn modern-btn-success"
								@click="() => navigateTo({ type: 'tjbgzzApi', reportid: globalData.m_reportid })"
							>
								报告制作
							</button>
						</view>
					</view>

					<view v-else-if="globalData.ymbl == 2" class="full-report">
						<button
							class="modern-btn modern-btn-primary full-width"
							@click="() => navigateTo({ type: 'tjbgzzApi', reportid: globalData.m_reportid })"
						>
							报告制作
						</button>
					</view>
				</view>

				<view class="modal-footer">
					<button class="modern-btn modern-btn-secondary" @click="closeModal_bgcl">
						关闭
					</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				// 用户全局信息
				userInfo: {},
				// 页面传参
				globalOption: {},
				// 自定义全局变量
				globalData: { ymbl: '0', m_reportid: 'null' },
				cxgrxxNum: 1,
				cxgrxx: {
					code: 0,
					data: []
				},
				tjbgjc: ``,
				tjbgzz: ``,
				navbarHeight: 0,
				text_ycxsj: '',
				scaninput_smcx: '',
				scaninput_smcxShow: false,
				scaninput_smcxFocus: false,
				modal_bgcl: '',
				input_bgfsr: '',
				searching: false,
				hasSearched: false,
				totalCount: 0
			};
		},
		onShow() {
			this.setCurrentPage(this);
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
				// 页面初始化
			},

			// 返回上一页
			goBack() {
				uni.navigateBack();
			},

			// 处理搜索
			async handleSearch() {
				if (!this.text_ycxsj && !this.scaninput_smcx) {
					uni.showToast({
						title: '请输入查询条件',
						icon: 'none'
					});
					return;
				}

				this.searching = true;
				this.hasSearched = true;

				try {
					await this.cxgrxxApi({
						userid_tysj: this.text_ycxsj,
						userid_scanid: this.scaninput_smcx,
						refresh: 1
					});
				} catch (error) {
					console.error('查询失败:', error);
					uni.showToast({
						title: '查询失败，请重试',
						icon: 'none'
					});
				} finally {
					this.searching = false;
				}
			},

			// 处理报告点击
			handleReportClick(item) {
				this.handleReportAction(item);
			},

			// 处理报告操作
			handleReportAction(item) {
				this.zbbgclFunction({
					reportid: item.ID,
					reportzt: item.汇总状态
				});
			},

			// 格式化身份证号
			formatIdCard(idCard) {
				if (!idCard) return '未知';
				if (idCard.length > 8) {
					return idCard.substring(0, 4) + '****' + idCard.substring(idCard.length - 4);
				}
				return idCard;
			},

			// 格式化日期
			formatDate(dateStr) {
				if (!dateStr) return '未知';
				// 简单的日期格式化，可以根据需要调整
				return dateStr;
			},

			// 获取状态样式类
			getStatusClass(status) {
				switch (status) {
					case '全':
						return 'status-complete';
					case '部分':
						return 'status-partial';
					default:
						return 'status-pending';
				}
			},

			// 电子报告数据查询 API请求方法
			async cxgrxxApi(param) {
				let thiz = this;
				param = param || {};

				// 如果请求要重置页面，请配置点击附加参数refresh=1
				if (param.refresh || typeof param != 'object') {
					this.cxgrxxNum = 1;
					this.totalCount = 0;
				}

				// 请求地址及请求数据
				let http_url = '/dzbg/sjcx';
				let http_data = {
					pagenum: this.cxgrxxNum,
					pagesize: 10,
					userid_tysj: param.userid_tysj || this.text_ycxsj,
					userid_scanid: param.userid_scanid || this.scaninput_smcx
				};
				let http_header = {
					'Content-Type': 'application/json'
				};

				try {
					let cxgrxx = await this.$http.post(http_url, http_data, http_header, 'json');

					let datarows = cxgrxx.data;
					if (http_data.pagenum == 1) {
						this.cxgrxx = cxgrxx;
						this.totalCount = datarows ? datarows.length : 0;
					} else if (datarows) {
						let rows = this.cxgrxx.data.concat(datarows);
						cxgrxx.data = rows;
						this.cxgrxx = Object.assign(this.cxgrxx, cxgrxx);
						this.totalCount += datarows.length;
					}

					if (datarows && datarows.length > 0) {
						this.cxgrxxNum = this.cxgrxxNum + 1;
					}

					if (cxgrxx.code == 301) {
						this.$session.clearUser();
						this.showToast('请先登录');
						uni.reLaunch({
							url: '/pages/index'
						});
						return;
					}

					if (cxgrxx.code != 200) {
						uni.showToast({
							title: cxgrxx.msg || '查询失败',
							icon: 'none'
						});
					}
				} catch (error) {
					console.error('API请求失败:', error);
					uni.showToast({
						title: '网络错误，请重试',
						icon: 'none'
					});
				}
			},

			// 提交报告监测 API请求方法
			async tjbgjcApi(param) {
				let thiz = this;
				param = param || {};

				// 请求地址及请求数据
				let http_url = '/dzbg/tjbgjc';
				let http_data = {
					reportid: param.reportid || this.globalData.m_reportid,
					reportpeople: param.reportpeople || this.input_bgfsr
				};
				let http_header = {
					'Content-Type': 'application/json'
				};

				try {
					let tjbgjc = await this.$http.post(http_url, http_data, http_header, 'json');
					this.tjbgjc = tjbgjc;

					if (tjbgjc.code == 301) {
						this.$session.clearUser();
						this.showToast('请先登录');
						uni.reLaunch({
							url: '/pages/index'
						});
						return;
					}

					if (tjbgjc.code == 200) {
						uni.showToast({
							title: '监测成功!',
							icon: 'success'
						});
						this.navigateTo({
							type: 'closemodal',
							id: 'modal_bgcl'
						});
						// 刷新查询结果
						this.handleSearch();
						return;
					}

					if (tjbgjc.code == 400) {
						uni.showModal({
							title: '提示',
							content: tjbgjc.msg,
							showCancel: false
						});
						return;
					}
				} catch (error) {
					console.error('提交报告监测失败:', error);
					uni.showToast({
						title: '操作失败，请重试',
						icon: 'none'
					});
				}
			},

			// 提交报告制作 API请求方法
			async tjbgzzApi(param) {
				let thiz = this;
				param = param || {};

				// 请求地址及请求数据
				let http_url = '/dzbg/tjbgzz';
				let http_data = {
					reportid: param.reportid || this.globalData.m_reportid
				};
				let http_header = {
					'Content-Type': 'application/json'
				};

				try {
					let tjbgzz = await this.$http.post(http_url, http_data, http_header, 'json');
					this.tjbgzz = tjbgzz;

					if (tjbgzz.code == 301) {
						this.$session.clearUser();
						this.showToast('请先登录');
						uni.reLaunch({
							url: '/pages/index'
						});
						return;
					}

					if (tjbgzz.code == 200) {
						uni.showToast({
							title: '提交成功!',
							icon: 'success'
						});
						this.navigateTo({
							type: 'closemodal',
							id: 'modal_bgcl'
						});
						// 刷新查询结果
						this.handleSearch();
						return;
					}

					if (tjbgzz.code == 400) {
						uni.showModal({
							title: '提示',
							content: tjbgzz.msg,
							showCancel: false
						});
						return;
					}
				} catch (error) {
					console.error('提交报告制作失败:', error);
					uni.showToast({
						title: '操作失败，请重试',
						icon: 'none'
					});
				}
			},

			// 准备报告处理 自定义方法
			async zbbgclFunction(param) {
				let thiz = this;
				let reportid = param && (param.reportid || param.reportid == 0) ? param.reportid : '';
				let reportzt = param && (param.reportzt || param.reportzt == 0) ? param.reportzt : '';

				// 根据报告状态设置ymbl值
				if (reportzt == '部分') {
					this.globalData.ymbl = 1;
				} else if (reportzt == '全') {
					this.globalData.ymbl = 2;
				}

				// 设置报告ID
				this.globalData.m_reportid = reportid;

				// 打开模态框
				this.modal_bgcl = 'show';
			},

			changeNavbarHeight(val) {
				this.navbarHeight = val;
			},

			// 显示扫码
			showScaninput_smcx(evt) {
				// #ifdef H5
				this.scaninput_smcxShow = true;
				// #endif
				// #ifndef H5
				let thiz = this;
				uni.scanCode({
					success: function (res) {
						thiz.scaninput_smcx = res.result;
						thiz.handleSearch();
					},
					fail: function (err) {
						console.error('扫码失败:', err);
						thiz.showToast('扫码失败，请重试', 'none');
					}
				});
				// #endif
			},

			// H5扫码成功回调方法
			changeScaninput_smcx(evt) {
				this.scaninput_smcx = evt.text;
				this.handleSearch();
			},

			// 关闭模态框
			closeModal_bgcl() {
				this.modal_bgcl = '';
				this.input_bgfsr = '';
			}
		},

		// 触底加载更多
		onReachBottom() {
			if (this.cxgrxx.code == 200 && this.cxgrxx.data.length > 0) {
				this.cxgrxxApi();
			}
		}
	};
</script>

<style lang="scss" scoped>
.report-container {
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

.navbar-back {
	width: 80rpx;
	height: 80rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: rgba(255, 255, 255, 0.1);
	border-radius: 50%;
	margin-right: var(--spacing-md);

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
.report-content {
	padding: var(--spacing-lg);
	padding-bottom: calc(var(--spacing-xl) + env(safe-area-inset-bottom));
}

/* 搜索区域 */
.search-section {
	margin-bottom: var(--spacing-xl);
}

.section-title {
	display: flex;
	align-items: center;
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

.search-input-group,
.scan-input-group {
	margin-bottom: var(--spacing-lg);
}

.input-label {
	font-size: 26rpx;
	color: var(--text-secondary);
	margin-bottom: var(--spacing-sm);
	font-weight: 500;
}

.search-input-wrapper,
.scan-input-wrapper {
	display: flex;
	align-items: center;
	background: var(--bg-primary);
	border: 2px solid var(--border-color);
	border-radius: var(--radius-md);
	transition: all 0.3s ease;

	&:focus-within {
		border-color: var(--primary-color);
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

.search-input,
.scan-input {
	flex: 1;
	padding: var(--spacing-md);
	font-size: 30rpx;
	color: var(--text-primary);
	background: transparent;
	border: none;
	outline: none;
}

.scan-button {
	display: flex;
	align-items: center;
	gap: var(--spacing-xs);
	padding: var(--spacing-md) var(--spacing-lg);
	background: var(--success-color);
	color: white;
	border: none;
	border-radius: 0 var(--radius-md) var(--radius-md) 0;
	font-size: 26rpx;

	.fas {
		font-size: 24rpx;
	}
}

.search-button-container {
	margin-top: var(--spacing-lg);
}

.search-button {
	width: 100%;
	height: 96rpx;
	font-size: 32rpx;
	font-weight: 600;
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

/* 统计区域 */
.stats-section {
	margin-bottom: var(--spacing-xl);
	background: var(--primary-gradient);
	color: white;
	border: none;
}

.stats-content {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: var(--spacing-lg) 0;
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

/* 结果区域 */
.results-section {
	margin-bottom: var(--spacing-xl);
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
}

.report-id {
	font-size: 24rpx;
	color: var(--text-tertiary);
}

.report-status {
	padding: var(--spacing-xs) var(--spacing-md);
	border-radius: var(--radius-sm);
	font-size: 24rpx;
	font-weight: 500;

	&.status-complete {
		background: rgba(16, 185, 129, 0.1);
		color: var(--success-color);
	}

	&.status-partial {
		background: rgba(245, 158, 11, 0.1);
		color: var(--warning-color);
	}

	&.status-pending {
		background: rgba(107, 114, 128, 0.1);
		color: var(--text-secondary);
	}
}

.report-details {
	padding: var(--spacing-lg);
}

.detail-row {
	display: flex;
	margin-bottom: var(--spacing-md);

	&:last-child {
		margin-bottom: 0;
	}
}

.detail-item {
	flex: 1;
	margin-right: var(--spacing-md);

	&.full-width {
		flex: 1;
		margin-right: 0;
	}

	&:last-child {
		margin-right: 0;
	}
}

.detail-label {
	font-size: 24rpx;
	color: var(--text-secondary);
	margin-bottom: var(--spacing-xs);
}

.detail-value {
	font-size: 28rpx;
	color: var(--text-primary);
	font-weight: 500;
}

.text-ellipsis {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.status-section {
	margin-top: var(--spacing-lg);
	padding-top: var(--spacing-lg);
	border-top: 1px solid var(--border-color);
}

.status-item {
	display: flex;
	align-items: center;
	margin-bottom: var(--spacing-sm);

	&:last-child {
		margin-bottom: 0;
	}
}

.status-label {
	font-size: 24rpx;
	color: var(--text-secondary);
	margin-right: var(--spacing-sm);
	min-width: 140rpx;
}

.status-value {
	font-size: 26rpx;
	color: var(--text-primary);
	flex: 1;
}

.report-actions {
	padding: var(--spacing-lg);
	background: var(--bg-tertiary);
	border-top: 1px solid var(--border-color);
}

.action-btn {
	display: flex;
	align-items: center;
	gap: var(--spacing-xs);
	width: 100%;
	height: 80rpx;
	border-radius: var(--radius-md);
	font-size: 28rpx;
	font-weight: 500;

	&.primary-btn {
		background: var(--primary-color);
		color: white;
		border: none;

		&:active {
			background: var(--primary-dark);
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
}

/* 模态框 */
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

.modern-modal {
	background: var(--bg-primary);
	border-radius: var(--radius-xl);
	width: 100%;
	max-width: 600rpx;
	max-height: 80vh;
	overflow: hidden;
	box-shadow: var(--shadow-xl);
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
	max-height: 60vh;
	overflow-y: auto;
}

.form-item {
	margin-bottom: var(--spacing-lg);
}

.form-label {
	font-size: 26rpx;
	color: var(--text-secondary);
	margin-bottom: var(--spacing-sm);
	font-weight: 500;
}

.modal-actions {
	display: flex;
	gap: var(--spacing-md);
	margin-bottom: var(--spacing-lg);
}

.full-width {
	width: 100%;
}

.modal-footer {
	padding: var(--spacing-lg);
	border-top: 1px solid var(--border-color);
}

/* 动画 */
@keyframes spin {
	to {
		transform: rotate(360deg);
	}
}

/* 响应式调整 */
@media screen and (max-width: 750rpx) {
	.report-content {
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