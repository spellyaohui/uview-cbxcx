@echo off
chcp 65001 >nul
echo ========================================
echo CB-KeepAlive AAR 构建脚本
echo ========================================
echo.

REM 检查Java环境
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [错误] 未找到Java环境，请先安装JDK 11+
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)

echo [1/5] 检查环境...
echo Java版本:
java -version 2>&1 | findstr /i "version"
echo.

REM 检查源代码
if not exist "keepalive\src\main\java\io\dcloud\feature\keepalive\KeepAliveModule.java" (
    echo [2/5] 复制源代码...
    if not exist "keepalive\src\main\java" mkdir keepalive\src\main\java
    xcopy /E /I /Y "..\android\src\io" "keepalive\src\main\java\io" >nul
    echo 源代码复制完成
) else (
    echo [2/5] 源代码已存在
)
echo.

REM 检查uni-app SDK
if not exist "keepalive\libs\uniapp-v8-release.aar" (
    echo [3/5] 警告: 缺少 uni-app SDK
    echo.
    echo ============================================
    echo 请手动完成以下步骤:
    echo.
    echo 1. 下载 HBuilderX 离线 SDK:
    echo    https://nativesupport.dcloud.net.cn/AppDocs/download/android
    echo.
    echo 2. 解压后找到: SDK/libs/uniapp-v8-release.aar
    echo.
    echo 3. 复制到: keepalive/libs/ 目录
    echo.
    echo 4. 重新运行此脚本
    echo ============================================
    echo.
    pause
    exit /b 1
) else (
    echo [3/5] uni-app SDK 已就绪
)
echo.

echo [4/5] 开始构建AAR...
echo.

REM 使用gradlew构建
if exist "gradlew.bat" (
    call gradlew.bat :keepalive:assembleRelease --no-daemon
) else (
    echo 正在下载Gradle Wrapper...
    REM 如果没有gradlew，尝试使用系统gradle
    where gradle >nul 2>nul
    if %ERRORLEVEL% neq 0 (
        echo [错误] 未找到Gradle，请安装Android Studio或Gradle
        pause
        exit /b 1
    )
    gradle :keepalive:assembleRelease
)

if %ERRORLEVEL% neq 0 (
    echo.
    echo [错误] 构建失败，请检查错误信息
    pause
    exit /b 1
)

echo.
echo [5/5] 复制AAR文件...

if exist "keepalive\build\outputs\aar\keepalive-release.aar" (
    if not exist "..\android\libs" mkdir "..\android\libs"
    copy /Y "keepalive\build\outputs\aar\keepalive-release.aar" "..\android\libs\CB-KeepAlive.aar" >nul
    echo.
    echo ========================================
    echo 构建成功!
    echo ========================================
    echo.
    echo AAR文件已复制到:
    echo   nativeplugins/CB-KeepAlive/android/libs/CB-KeepAlive.aar
    echo.
    echo 下一步:
    echo   1. 在HBuilderX中选择 发行 - 原生App-云打包
    echo   2. 勾选 自定义基座
    echo   3. 点击打包，等待完成
    echo   4. 使用自定义基座运行测试
    echo ========================================
) else (
    echo [错误] 未找到构建输出文件
    echo 请检查 keepalive/build/outputs/aar/ 目录
)

echo.
pause
