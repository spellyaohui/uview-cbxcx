<template>
	<view class="container container339776">
		<view class="flex diygw-col-24">
			<view class="diygw-grid col-3">
				<view @tap="navigateTo" data-type="page" data-url="/pages/report" class="diygw-grid-item">
					<view class="diygw-grid-inner">
						<view class="diygw-grid-icon diygw-avatar">
							<image mode="aspectFit" class="diygw-avatar-img" src="/static/dzbg.png"></image>
						</view>
						<view class="diygw-grid-title"> 电子报告 </view>
					</view>
				</view>
			</view>
		</view>
		<view class="clearfix"></view>
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
	}
</style>
