// 全局状态管理 - 主题状态
const globalState = {
  isDark: false, // 默认是浅色模式
  theme: 'light' // 当前主题
}

// 更新主题状态
function updateTheme(isDark, theme) {
  globalState.isDark = isDark
  globalState.theme = theme || (isDark ? 'dark' : 'light')
  
  // 触发全局事件
  if (typeof uni !== 'undefined') {
    uni.$emit && uni.$emit('globalThemeChange', { isDark, theme: globalState.theme })
  }
  
  // 更新body类
  updateBodyClass(isDark);
}

// 更新body类以应用主题
function updateBodyClass(isDark) {
  // #ifdef H5
  if (typeof document !== 'undefined' && document.body) {
    if (isDark) {
      document.body.classList.add('theme-dark');
      document.body.classList.remove('theme-light');
      document.body.setAttribute('data-theme', 'dark');
    } else {
      document.body.classList.remove('theme-dark');
      document.body.classList.add('theme-light');
      document.body.setAttribute('data-theme', 'light');
    }
  }
  // #endif
  
  // #ifdef APP-PLUS
  if (typeof plus !== 'undefined') {
    plus.navigator.setStatusBarStyle(isDark ? 'light' : 'dark');
  }
  // #endif
}

// 获取当前主题状态
function getThemeState() {
  return {
    isDark: globalState.isDark,
    theme: globalState.theme
  }
}

// 初始化主题状态
function initTheme() {
  // 获取系统主题
  if (typeof uni !== 'undefined') {
    uni.getSystemInfo({
      success: (res) => {
        const isDark = res.theme === 'dark';
        updateTheme(isDark, res.theme);
      }
    });
    
    // 监听主题变化
    uni.onThemeChange((res) => {
      updateTheme(res.theme === 'dark', res.theme);
    });
  }
}

export default {
  state: globalState,
  updateTheme,
  getThemeState,
  initTheme
}