<template>
	<view class="diy-sortable">
		<template v-if="viewWidth">
			<movable-area class="area" :style="{ height: areaHeight }" @mouseenter="mouseenter"
				@mouseleave="mouseleave">
				<movable-view v-for="(item, index) in itemList" :key="item.id" class="view" direction="all" :y="item.y"
					:x="item.x" :damping="40" :disabled="item.disable" @change="onChange($event, item)"
					@touchstart="touchstart(item)" @mousedown="touchstart(item)" @touchend="touchend(item)"
					@mouseup="touchend(item)" :style="{
					  width: viewWidth + 'px', 
					  height: viewHeight + 'px', 
					  'z-index': item.zIndex, 
					  opacity: item.opacity 
					}">
					<view class="sort-item" :style="{
						width: childWidth, 
						height: childHeight, 
						background:itemBgColor,
						borderRadius: borderRadius + 'rpx',
						transform: 'scale(' + item.scale + ')' 
					  }">
						<slot class="sort-item-inner" :item="item.source" :index="index">
							{{item.source[labelName]}}
						</slot>
						<view v-if="isDel" class="del-item"  @click="delItems(item, index)" @touchstart.stop="delItemMp(item, index)"
							@touchend.stop="nothing()" @mousedown.stop="nothing()" @mouseup.stop="nothing()">
							<view class="del-wrap" :style="{background:delBgColor,color:delColor}">
								<text class="diy-icon-close"></text>
							</view>
						</view>
					</view>
				</movable-view>
				<view class="add" v-if="itemList.length < number&&isAdd"
					:style="{ top: add.y, left: add.x, width: viewWidth + 'px', height: viewHeight + 'px' }">
					<view class="add-wrap"
						:style="{background:this.addBgColor, width: childWidth, height: childHeight, borderRadius: borderRadius + 'rpx' }">
						<slot name="add">
							<text class="diy-icon-add" @tap="addItem"></text>
						</slot>
					</view>
				</view>
			</movable-area>
		</template>
	</view>
</template>

<script>
	export default {
		emits: ['change', 'update:modelValue'],
		props: {
			// 排序内容
			modelValue: {
				type: Array,
				default: function() {
					return []
				}
			},
			isAdd: {
				type: Boolean,
				default: true
			},
			labelName:{
				type: String,
				default: 'label'
			},
			// 选择内容数量限制
			number: {
				type: Number,
				default: 9
			},
			// 内容列数
			cols: {
				type: Number,
				default: 3
			},
			// 内容圆角，单位 rpx
			borderRadius: {
				type: Number,
				default: 0
			},
			// 内容周围空白填充，单位 rpx
			padding: {
				type: Number,
				default: 10
			},
			//拖拉正方形,如果为true,拖拉高度itemHeight无效
			isSquare:{
				type: Boolean,
				default: false
			},
			// 拖拉每个高度
			itemHeight:{
				type: Number,
				default: 50
			},
			// 拖动内容时放大倍数 [0, ∞)
			scale: {
				type: Number,
				default: 1.1
			},
			// 拖动内容时不透明度
			opacity: {
				type: Number,
				default: 0.7
			},
			isDel:{
				type: Boolean,
				default: true
			},
			//删除按钮颜色
			delBgColor:{
				type:String,
				default:'#888'
			},
			//删除颜色
			delColor:{
				type:String,
				default:'#000'
			},
			itemBgColor:{
				type:String,
				default:'#eee'
			},
			addBgColor:{
				type:String,
				default:'#eee'
			}
		},
		data() {
			return {
				itemList: [],
				width: 0,
				add: {
					x: 0,
					y: 0
				},
				colsValue: 0,
				viewWidth: 0,
				viewHeight: 0,
				tempItem: null,
				timer: null,
				changeStatus: true,
				preStatus: true,
				first: true,
			}
		},
		computed: {
			areaHeight() {
				let height = ''
				if (this.itemList.length < this.number && this.isAdd) {
					height = (Math.ceil((this.itemList.length + 1) / this.colsValue) * this.viewHeight).toFixed() + 'px'
				} else {
					height = (Math.ceil(this.itemList.length / this.colsValue) * this.viewHeight).toFixed() + 'px'
				}
				return height
			},
			childWidth() {
				return this.viewWidth - this.rpx2px(this.padding) * 2 + 'px'
			},
			childHeight() {
				return this.viewHeight - this.rpx2px(this.padding) * 2 + 'px'
			},
		},
		watch: {
			modelValue: {
				handler(n) {
					if (!this.first && this.changeStatus) {
						let flag = false
						for (let i = 0; i < n.length; i++) {
							if (flag) {
								this.addProperties(n[i])
								continue
							}
							if (this.itemList.length === i || this.itemList[i].source !== n[i]) {
								flag = true
								this.itemList.splice(i)
								this.addProperties(n[i])
							}
						}
					}
				},
				deep: true
			},
		},
		created() {
			this.width = uni.getSystemInfoSync().windowWidth
		},
		mounted() {
			const query = uni.createSelectorQuery().in(this)
			query.select('.diy-sortable').boundingClientRect(data => {
				this.colsValue = this.cols
				this.viewWidth = data.width / this.cols
				if(this.isSquare){
					this.viewHeight = this.viewWidth
				}else{
					this.viewHeight = this.itemHeight?this.itemHeight:this.viewWidth / 2
				}
				let list = this.modelValue
				for (let item of list) {
					this.addProperties(item)
				}
				this.first = false

			})
			query.exec()
		},
		methods: {
			addItem(){
				uni.showToast({
					title:'请自定义增加模板',
					icon:'none'
				})
			},
			onChange(e, item) {
				if (!item) return
				item.oldX = e.detail.x
				item.oldY = e.detail.y
				if (e.detail.source === 'touch') {
					if (item.moveEnd) {
						item.offset = Math.sqrt(Math.pow(item.oldX - item.absX * this.viewWidth, 2) + Math.pow(item.oldY -
							item
							.absY * this.viewWidth, 2))
					}
					let x = Math.floor((e.detail.x + this.viewWidth / 2) / this.viewWidth)
					if (x >= this.colsValue) return
					let y = Math.floor((e.detail.y + this.viewHeight / 2) / this.viewHeight)
					let index = this.colsValue * y + x
					if (item.index != index && index < this.itemList.length) {
						this.changeStatus = false
						for (let obj of this.itemList) {
							if (item.index > index && obj.index >= index && obj.index < item.index) {
								this.change(obj, 1)
							} else if (item.index < index && obj.index <= index && obj.index > item.index) {
								this.change(obj, -1)
							} else if (obj.id != item.id) {
								obj.offset = 0
								obj.x = obj.oldX
								obj.y = obj.oldY
								setTimeout(() => {
									this.$nextTick(() => {
										obj.x = obj.absX * this.viewWidth
										obj.y = obj.absY * this.viewHeight
									})
								}, 0)
							}
						}
						item.index = index
						item.absX = x
						item.absY = y
						if (!item.moveEnd) {
							setTimeout(() => {
								this.$nextTick(() => {
									item.x = item.absX * this.viewWidth
									item.y = item.absY * this.viewHeight
								})
							}, 0)
						}
						this.sortList()
					}
				}
			},
			change(obj, i) {
				obj.index += i
				obj.offset = 0
				obj.x = obj.oldX
				obj.y = obj.oldY
				obj.absX = obj.index % this.colsValue
				obj.absY = Math.floor(obj.index / this.colsValue)
				setTimeout(() => {
					this.$nextTick(() => {
						obj.x = obj.absX * this.viewWidth
						obj.y = obj.absY * this.viewHeight
					})
				}, 0)
			},
			touchstart(item) {
				this.itemList.forEach(v => {
					v.zIndex = v.index + 9
				})
				item.zIndex = 99
				item.moveEnd = true
				this.tempItem = item
				this.timer = setTimeout(() => {
					item.scale = this.scale
					item.opacity = this.opacity
					clearTimeout(this.timer)
					this.timer = null
				}, 200)
			},
			touchend(item) {
				item.scale = 1
				item.opacity = 1
				item.x = item.oldX
				item.y = item.oldY
				item.offset = 0
				item.moveEnd = false
				setTimeout(() => {
					this.$nextTick(() => {
						item.x = item.absX * this.viewWidth
						item.y = item.absY * this.viewHeight
						this.tempItem = null
						this.changeStatus = true
						this.$emit("change", this.modelValue);
					})
				}, 0)
			},
			mouseenter() {
				//#ifdef H5
				this.itemList.forEach(v => {
					v.disable = false
				})
				//#endif
			},
			mouseleave() {
				//#ifdef H5
				if (this.tempItem) {
					this.itemList.forEach(v => {
						v.disable = true
						v.zIndex = v.index + 9
						v.offset = 0
						v.moveEnd = false
						if (v.id == this.tempItem.id) {
							if (this.timer) {
								clearTimeout(this.timer)
								this.timer = null
							}
							v.scale = 1
							v.opacity = 1
							v.x = v.oldX
							v.y = v.oldY
							this.$nextTick(() => {
								v.x = v.absX * this.viewWidth
								v.y = v.absY * this.viewHeight
								this.tempItem = null
							})
						}
					})
					this.changeStatus = true
				}
				//#endif
			},
			delItems(item, index) {
				this.delItemHandle(item, index)
			},
			delItemHandle(item, index) {
				this.itemList.splice(index, 1)
				for (let obj of this.itemList) {
					if (obj.index > item.index) {
						obj.index -= 1
						obj.x = obj.oldX
						obj.y = obj.oldY
						obj.absX = obj.index % this.colsValue
						obj.absY = Math.floor(obj.index / this.colsValue)
						this.$nextTick(() => {
							obj.x = obj.absX * this.viewWidth
							obj.y = obj.absY * this.viewHeight
						})
					}
				}
				this.add.x = (this.itemList.length % this.colsValue) * this.viewWidth + 'px'
				this.add.y = Math.floor(this.itemList.length / this.colsValue) * this.viewHeight + 'px'
				let result = this.sortList()
				this.$emit("change", result);
			},
			delItemMp(item, index) {
				//#ifdef MP
				this.delItems(item, index)
				//#endif
			},
			sortList() {
				const result = []
				let source = this.modelValue
				let list = this.itemList.slice()
				list.sort((a, b) => {
					return a.index - b.index
				})
				for (let s of list) {
					let item = source.find(d => d == s.source)
					if (item) {
						result.push(item)
					} else {
						result.push(s.source)
					}
				}
				this.$emit("update:modelValue", result);
				return result
			},
			addProperties(item) {
				let absX = this.itemList.length % this.colsValue
				let absY = Math.floor(this.itemList.length / this.colsValue)
				let x = absX * this.viewWidth
				let y = absY * this.viewHeight
				this.itemList.push({
					source: item,
					x,
					y,
					oldX: x,
					oldY: y,
					absX,
					absY,
					scale: 1,
					zIndex: 9,
					opacity: 1,
					index: this.itemList.length,
					id: this.guid(16),
					disable: false,
					offset: 0,
					moveEnd: false
				})
				this.add.x = (this.itemList.length % this.colsValue) * this.viewWidth + 'px'
				this.add.y = Math.floor(this.itemList.length / this.colsValue) * this.viewHeight + 'px'
			},
			nothing() {},
			rpx2px(v) {
				return this.width * v / 750
			},
			guid(len = 32) {
				const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('')
				const uuid = []
				const radix = chars.length
				for (let i = 0; i < len; i++) uuid[i] = chars[0 | Math.random() * radix]
				uuid.shift()
				return `u${uuid.join('')}`
			}
		}
	}
</script>

<style lang="scss" scoped>
	.diy-sortable {
	    width: 100%;
	    
		.area {
			width: 100%;

			.view {
				display: flex;
				justify-content: center;
				align-items: center;

				.sort-item {
					position: relative;
					overflow: hidden;
					display: flex;	
					align-items: center;
					justify-content: center;
						
					.sort-item-inner {
						width: 100%;
						height: 100%;
					}

					.del-item {
						position: absolute;
						top: 0rpx;
						right: 0rpx;
						padding: 0 0 20rpx 20rpx;

						.del-wrap {
							width: 36rpx;
							height: 36rpx; 
							border-radius: 0 0 0 10rpx;
							display: flex;
							justify-content: center;
							align-items: center;

						}
					}
				}
			}

			.add {
				position: absolute;
				display: flex;
				justify-content: center;
				align-items: center;

				.add-wrap {
					display: flex;
					justify-content: center;
					align-items: center;
					background-color: #eeeeee;
				}
			}
		}
	}
</style>