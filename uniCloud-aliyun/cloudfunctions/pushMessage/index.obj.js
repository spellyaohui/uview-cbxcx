// 云对象教程: https://uniapp.dcloud.net.cn/uniCloud/cloud-obj
// jsdoc语法提示教程：https://ask.dcloud.net.cn/docs/#//ask.dcloud.net.cn/article/129
const uniPush = uniCloud.getPushManager({
	appId: "__UNI__595054E"
})
module.exports = {
	_before: function() { // 通用预处理器
		//this.prams=this.getHttpInfo()
		this.prams = JSON.parse(this.getHttpInfo().body)
	},

	/* 	async mySendMessage(data) {
			return this.prams
			return await uniPush.sendMessage({
				"push_clientid": data.push_clientid,
				"title": data.title,
				"content": data.content,
				"payload":{"id":data.id,"title":data.title,"content":data.content}
			});
		} */
	async mySendMessage() {
		let data = this.prams;
		return await uniPush.sendMessage({
			"push_clientid": data.push_clientid,
			"title": data.title,
			"content": data.content,
			"payload": {
				"id": data.id,
				"title": data.title,
				"content": data.content
			}
		});
	}
}