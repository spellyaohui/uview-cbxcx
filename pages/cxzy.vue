<template>
	<view class="container container339776">
		<view class="u-grid u-grid-cols-3">
			<view class="u-grid-item" @tap="navigateTo" data-type="page" data-url="/pages/report">
				<view class="u-grid-item-content">
					<view class="u-grid-item-icon">
						<image mode="aspectFit" class="u-grid-item-icon-img" src="/static/dzbg.png"></image>
					</view>
					<view class="u-grid-item-text">电子报告</view>
				</view>
			</view>
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
				globalData: {}
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
			async init() {}
		}
	};
</script>

<style lang="scss" scoped>
.container339776 {
	padding: var(--spacing-lg);
	min-height: 100vh;
	background: var(--bg-secondary);
	display: flex;
	align-items: center;
	justify-content: center;
}

.u-grid {
	width: 100%;
	max-width: 400rpx;
}

.u-grid-item {
	padding: var(--spacing-lg);
	text-align: center;
}

.u-grid-item-content {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: var(--spacing-md);
	padding: var(--spacing-xl);
	background: var(--bg-primary);
	border-radius: var(--radius-xl);
	box-shadow: var(--shadow-md);
	transition: all 0.3s ease;

	&:active {
		transform: scale(0.95);
		box-shadow: var(--shadow-lg);
	}
}

.u-grid-item-icon {
	width: 120rpx;
	height: 120rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: var(--primary-gradient);
	border-radius: var(--radius-lg);
	box-shadow: var(--shadow-md);
}

.u-grid-item-icon-img {
	width: 80rpx;
	height: 80rpx;
}

.u-grid-item-text {
	font-size: 28rpx;
	font-weight: 600;
	color: var(--text-primary);
}
</style>
