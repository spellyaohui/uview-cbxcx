// 简化的全局状态管理 - 基于官方 DarkMode 标准
const globalState = {
  currentTheme: 'light',
  isDarkMode: false
}

// 获取当前主题状态
function getThemeState() {
  return {
    theme: globalState.currentTheme,
    isDarkMode: globalState.isDarkMode
  }
}

// 更新主题状态（由系统主题变化触发）
function updateThemeState(theme) {
  globalState.currentTheme = theme;
  globalState.isDarkMode = theme === 'dark';
  
  console.log('全局状态更新主题:', theme, globalState.isDarkMode ? '暗黑模式' : '浅色模式');
  
  // 触发全局事件通知所有页面
  if (typeof uni !== 'undefined') {
    uni.$emit('globalThemeChange', getThemeState());
  }
}

// 初始化主题状态
function initTheme() {
  console.log('初始化全局主题状态...');
  
  // 获取系统主题
  uni.getSystemInfo({
    success: (res) => {
      const theme = res.theme || 'light';
      console.log('系统主题:', theme);
      updateThemeState(theme);
    },
    fail: () => {
      console.log('获取系统主题失败，使用默认浅色主题');
      updateThemeState('light');
    }
  });

  // 监听系统主题变化
  uni.onThemeChange((res) => {
    console.log('系统主题变化:', res.theme);
    updateThemeState(res.theme);
  });
}

export default {
  state: globalState,
  getThemeState,
  updateThemeState,
  initTheme
}