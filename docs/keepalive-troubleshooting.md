# 保活插件排障笔记（uview-cbxcx）

## 遇到的问题
- 自定义基座运行时报 “基座不包含原生插件 [CB-KeepAlive]”，实际 APK 中 dex 找不到 keepalive 类。
- HBuilderX 校验 `integrateType`，设置为 `code` 会报“插件不合法，必须为 aar/jar”。
- 本地插件目录缺少 `android/build.gradle`，导致 AAR 未被合入基座。
- `manifest.json` 里带 `__plugin_info__`（bought=-1），疑似被当作市场插件跳过；去掉后仍需确保 AAR 合入。
- Heartbeat 接口异常时，前端出现 `Cannot read property 'code' of undefined`。
- 登录后心跳/保活未启动：App 用 `uni.getStorageSync('token')` 判断，而登录写在 `$session`。
- 批量心跳接口 `/api/heartbeat/batch` 实际返回 404。
- 通知不显示：前台服务通知渠道优先级过低。

## 解决方案概览
1) **插件配置与打包**
   - `manifest.json` 的 `nativePlugins.CB-KeepAlive` 仅保留 `package/class`（纯本地插件写法）。
   - `nativeplugins/CB-KeepAlive/package.json`：`integrateType: "aar"`，版本升到 `1.0.1` 触发重新集成。
   - 补齐 `nativeplugins/CB-KeepAlive/android/build.gradle`，显式 `implementation(name: 'CB-KeepAlive', ext: 'aar')`，并在 `repositories` 同时包含 `libs` 与 `.`。
   - 将 AAR 放在 `android/libs/CB-KeepAlive.aar`，并复制一份到 `android/CB-KeepAlive.aar` 兼容路径搜索。
   - 补充 `android/proguard-rules.pro`，keep 全部 `io.dcloud.feature.keepalive.**`。
   - 自定义基座制作后，用脚本验包：
     ```powershell
     pwsh -NoProfile -File scripts/check-custom-base.ps1
     ```
     脚本会输出 APK hash 并检查 dex 是否包含 KeepAliveModule。

2) **前端 JS 侧**
   - `App.vue` 登录判断统一用 `$session.getToken()`，登录事件 `uni.$emit('userLogin')` 触发心跳/保活启动。
   - `startKeepAliveService` 权限检查即便异常也继续尝试启动。
   - `KeepAliveManager.callNativeMethod`：
     - 空参数方法仅传 callback，避免签名不匹配。
     - 加 5s 超时兜底，防止原生无回调卡死。

3) **Heartbeat（心跳）**
   - 加入 `normalizeApiResponse` / `getResponseCode`，接口异常不再抛 TypeError。
   - 移除批量上传逻辑 `/api/heartbeat/batch`（后端 404），仅缓存并在正常心跳流程逐步发送。

4) **通知 / 前台服务**
   - Android 前台通知渠道从 `IMPORTANCE_LOW` 提升到 `IMPORTANCE_DEFAULT`，通知优先级设为 `PRIORITY_HIGH`（`KeepAliveService.java`）。
   - 确认系统权限：通知、自启动、后台运行、电池优化白名单需手动放行（尤其 Android 13+ 和国产 ROM）。

## 关键改动文件
- `manifest.json`（nativePlugins 配置精简）
- `nativeplugins/CB-KeepAlive/package.json`（integrateType=aar，版本 1.0.1）
- `nativeplugins/CB-KeepAlive/android/build.gradle`（显式合入 AAR）
- `nativeplugins/CB-KeepAlive/android/proguard-rules.pro`
- `common/KeepAliveManager.js`（调用签名修正、超时兜底）
- `common/HeartbeatService.js`（响应兜底，移除批量上传）
- `App.vue`（登录事件触发，token 判断统一）
- `nativeplugins/CB-KeepAlive/android-studio-project/keepalive/KeepAliveService.java`（通知渠道/优先级提升）

## 复现/验证步骤
1. 清理/制作自定义调试基座（Android），安装到设备。
2. 运行脚本验证基座：
   ```powershell
   pwsh -NoProfile -File scripts/check-custom-base.ps1
   ```
   确认输出 `OK` 且 dex 包含 KeepAliveModule。
3. 启动 App，登录后查看控制台应出现：
   - `调用原生方法: start`
   - `保活服务启动成功`
   - 通知栏显示“崔博小程序正在后台运行”。
4. 如果无通知，手动检查系统通知权限、自启动、电池优化白名单。

## 经验教训
- 本地插件务必提供 `android/build.gradle`，并显式声明 AAR 依赖，否则 HBuilderX 可能只登记不合入。
- `manifest.json` 写法要符合本地插件规范，避免误当市场插件。
- 前端调用原生方法时，注意方法签名（无参时只传 callback），并加超时兜底。
- 心跳接口要对返回做容错，避免后端异常导致前端 TypeError。
- 批量接口需要确认后端真实存在，若无则移除，避免无意义重试。


