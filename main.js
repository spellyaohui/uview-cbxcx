// #ifndef VUE3
import Vue from 'vue'
import App from './App'
import {
	setCurrentPage,
	Validate,
	setData,
	navigateTo,
	showModal,
	showToast,
	getPickerChildren,
	uploadImage,
	getOption,
	setAuthorize
} from '@/common/Page.js'

import Tools from '@/common/Tools.js'
import HttpService from '@/common/HttpService.js'
import Session from '@/common/Session.js'
// 移除uView，将使用ColorUI + 自定义组件
Vue.config.productionTip = false
Vue.prototype.$tools = new Tools()
Vue.prototype.$http = new HttpService()
Vue.prototype.$session = Session
Vue.prototype.Validate = Validate
Vue.prototype.setData = setData
Vue.prototype.navigateTo = navigateTo
Vue.prototype.showModal = showModal
Vue.prototype.setAuthorize = setAuthorize
Vue.prototype.showToast = showToast
Vue.prototype.getPickerChildren = getPickerChildren
Vue.prototype.uploadImage = uploadImage
Vue.prototype.setCurrentPage = setCurrentPage
Vue.prototype.getOption = getOption

App.mpType = 'app'

const app = new Vue({
	...App
})

uni.getSystemInfo({
	success: function(e) {
		// #ifndef MP
		Vue.prototype.StatusBar = e.statusBarHeight;
		if (e.platform == 'android') {
			Vue.prototype.CustomBar = e.statusBarHeight + 50;
		} else {
			Vue.prototype.CustomBar = e.statusBarHeight + 45;
		};
		// #endif

		// #ifdef MP-WEIXIN
		Vue.prototype.StatusBar = e.statusBarHeight;
		let custom = wx.getMenuButtonBoundingClientRect();
		Vue.prototype.Custom = custom;
		Vue.prototype.CustomBar = custom.bottom + custom.top - e.statusBarHeight;
		// #endif

		// #ifdef MP-ALIPAY
		Vue.prototype.StatusBar = e.statusBarHeight;
		Vue.prototype.CustomBar = e.statusBarHeight + e.titleBarHeight;
		// #endif
	}
})
app.$mount()

// #endif

// #ifdef VUE3
import App from './App'
import {
	setCurrentPage,
	Validate,
	setData,
	navigateTo,
	showModal,
	showToast,
	getPickerChildren,
	uploadImage,
	getOption,
	setAuthorize
} from '@/common/Page.js'

import Tools from '@/common/Tools.js'
import HttpService from '@/common/HttpService.js'
import Session from '@/common/Session.js'
// 引入uView Plus
import uviewPlus from 'uview-plus'
import Auth from './common/Auth'
import store from './store/index'

import {
	createSSRApp
} from 'vue'


export function createApp() {
	const app = createSSRApp(App)

	// 使用uView Plus
	app.use(uviewPlus)

	app.config.globalProperties.$tools = new Tools()
	app.config.globalProperties.$http = new HttpService()
	app.config.globalProperties.$session = Session
	app.config.globalProperties.$auth = Auth
	app.config.globalProperties.$store = store

	// 初始化主题
	store.initTheme();

	// 确保App端支持深色模式
	// #ifdef APP-PLUS
	plus.nativeUI.setUIStyle('auto');
	// #endif

	uni.getSystemInfo({
		success: function(e) {
			// 在App.vue中设置状态栏高度
			app.config.globalProperties.statusBarHeight = e.statusBarHeight || 44;

			// #ifndef MP
			app.config.globalProperties.StatusBar = e.statusBarHeight;
			if (e.platform == 'android') {
				app.config.globalProperties.CustomBar = e.statusBarHeight + 50;
			} else {
				app.config.globalProperties.CustomBar = e.statusBarHeight + 45;
			};
			// #endif

			// #ifdef MP-WEIXIN
			app.config.globalProperties.StatusBar = e.statusBarHeight;
			let custom = wx.getMenuButtonBoundingClientRect();
			app.config.globalProperties.Custom = custom;
			app.config.globalProperties.CustomBar = custom.bottom + custom.top - e.statusBarHeight;
			// #endif

			// #ifdef MP-ALIPAY
			app.config.globalProperties.StatusBar = e.statusBarHeight;
			app.config.globalProperties.CustomBar = e.statusBarHeight + e.titleBarHeight;
			// #endif
		}
	})

	app.mixin({
		methods: {
			setCurrentPage,
			Validate,
			setData,
			navigateTo,
			showModal,
			showToast,
			getPickerChildren,
			uploadImage,
			getOption,
			setAuthorize
		}
	})
	// 移除uView，将使用ColorUI + 自定义组件
	return {
		app
	}
}
// #endif