@echo off
chcp 65001 >nul
echo ========================================
echo CB-KeepAlive 项目初始化脚本
echo ========================================
echo.

REM 获取脚本所在目录并切换到该目录
cd /d "%~dp0"
echo 当前目录: %CD%
echo.

REM 创建必要的目录
if not exist "keepalive\src\main\java" mkdir keepalive\src\main\java
if not exist "keepalive\libs" mkdir keepalive\libs
if not exist "gradle\wrapper" mkdir gradle\wrapper

REM 检查源代码是否存在
if not exist "..\android\src\io\dcloud\feature\keepalive\KeepAliveModule.java" (
    echo [错误] 找不到源代码目录!
    echo 请确保以下路径存在:
    echo   %CD%\..\android\src\io\dcloud\feature\keepalive\
    echo.
    pause
    exit /b 1
)

REM 检查源代码目录
if not exist "keepalive\src\main\java\io\dcloud\feature\keepalive\KeepAliveModule.java" (
    echo [1/3] 复制源代码...
    xcopy /E /I /Y "..\android\src\io" "keepalive\src\main\java\io"
    if %ERRORLEVEL% equ 0 (
        echo 源代码复制完成！
    ) else (
        echo [错误] 源代码复制失败，错误码: %ERRORLEVEL%
        pause
        exit /b 1
    )
) else (
    echo [1/3] 源代码已存在，跳过复制
)

echo.

REM 检查AndroidManifest.xml
if not exist "keepalive\src\main\AndroidManifest.xml" (
    echo [2/3] 复制AndroidManifest.xml...
    copy /Y "..\android\AndroidManifest.xml" "keepalive\src\main\AndroidManifest.xml"
    echo AndroidManifest.xml复制完成！
) else (
    echo [2/3] AndroidManifest.xml已存在
)

echo.

REM 检查uni-app SDK
if not exist "keepalive\libs\uniapp-v8-release.aar" (
    echo [3/3] 警告：缺少 uni-app SDK
    echo.
    echo ============================================
    echo 请手动完成以下步骤：
    echo.
    echo 1. 下载 HBuilderX 离线 SDK:
    echo    https://nativesupport.dcloud.net.cn/AppDocs/download/android
    echo.
    echo 2. 解压后找到: SDK/libs/uniapp-v8-release.aar
    echo.
    echo 3. 复制到以下目录:
    echo    %CD%\keepalive\libs\
    echo ============================================
) else (
    echo [3/3] uni-app SDK 已存在
)

echo.
echo ========================================
echo 初始化完成！
echo.
echo 下一步操作：
echo.
echo 方式A - 使用Android Studio:
echo   1. 用 Android Studio 打开此目录: %CD%
echo   2. 等待 Gradle 同步完成
echo   3. 选择 Build - Make Project 构建
echo.
echo 方式B - 使用命令行:
echo   1. 确保已安装JDK 11+
echo   2. 运行 build-aar.bat
echo ========================================
pause
