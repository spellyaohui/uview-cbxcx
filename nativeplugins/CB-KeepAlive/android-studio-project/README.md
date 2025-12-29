# CB-KeepAlive Android Studio 项目

## 快速开始

### 1. 准备工作

1. 下载 HBuilderX 离线 SDK：
   - 访问：https://nativesupport.dcloud.net.cn/AppDocs/download/android
   - 下载最新版本的 Android 离线 SDK

2. 复制 uni-app SDK：
   - 解压下载的 SDK
   - 找到 `SDK/libs/uniapp-v8-release.aar`
   - 复制到 `keepalive/libs/` 目录

### 2. 复制源代码

将插件源代码复制到项目中：

```bash
# 在项目根目录执行
cp -r ../android/src/io keepalive/src/main/java/
cp ../android/AndroidManifest.xml keepalive/src/main/
```

### 3. 构建 AAR

使用 Android Studio：
1. 打开此项目
2. 选择 `Build` → `Make Project`
3. 或选择 `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`

使用命令行：
```bash
./gradlew :keepalive:assembleRelease
```

### 4. 获取 AAR 文件

构建完成后，AAR 文件位于：
- `keepalive/build/outputs/aar/keepalive-release.aar`

构建脚本会自动将 AAR 复制到：
- `../android/libs/CB-KeepAlive.aar`

## 目录结构

```
android-studio-project/
├── build.gradle              # 根项目配置
├── settings.gradle           # 项目设置
├── gradle.properties         # Gradle 属性
├── README.md                 # 本文件
└── keepalive/
    ├── build.gradle          # 模块配置
    ├── libs/                 # 依赖库（放置 uniapp-v8-release.aar）
    └── src/
        └── main/
            ├── java/         # Java 源代码
            └── AndroidManifest.xml
```

## 注意事项

1. **uni-app SDK 必须**：必须将 `uniapp-v8-release.aar` 放入 `keepalive/libs/` 目录
2. **编译版本**：使用 Java 8 编译
3. **依赖方式**：使用 `compileOnly` 避免依赖冲突
