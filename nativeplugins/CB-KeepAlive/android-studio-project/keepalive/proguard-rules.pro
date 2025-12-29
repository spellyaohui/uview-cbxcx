# CB-KeepAlive 混淆规则
# 保留uni-app模块类
-keep class io.dcloud.feature.keepalive.** { *; }
-keep class io.dcloud.feature.uniapp.** { *; }

# 保留FastJSON
-keep class com.alibaba.fastjson.** { *; }
-dontwarn com.alibaba.fastjson.**

# 保留AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**
