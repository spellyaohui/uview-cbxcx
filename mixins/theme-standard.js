// 标准主题混入 - 完全基于 uni-app 官方 DarkMode 标准
export default {
  data() {
    return {
      currentTheme: 'light',
      isDarkMode: false
    }
  },

  mounted() {
    // 获取初始主题状态
    this.initThemeState();

    // 监听主题变化
    uni.$on('themeChange', this.handleThemeChange);
  },

  beforeDestroy() {
    // 移除主题变化监听
    uni.$off('themeChange', this.handleThemeChange);
  },

  methods: {
    // 初始化主题状态
    initThemeState() {
      uni.getSystemInfo({
        success: (res) => {
          this.currentTheme = res.theme || 'light';
          this.isDarkMode = res.theme === 'dark';
          console.log('页面初始化主题状态:', this.currentTheme);
        },
        fail: () => {
          this.currentTheme = 'light';
          this.isDarkMode = false;
          console.log('获取系统主题失败，使用默认浅色主题');
        }
      });
    },

    // 处理主题变化
    handleThemeChange({ theme, isDark }) {
      console.log('页面接收到主题变化事件:', theme, isDark);
      this.currentTheme = theme;
      this.isDarkMode = isDark;
      
      // 子组件可重写此方法来处理特定的主题逻辑
      if (this.onThemeChange) {
        this.onThemeChange(theme, isDark);
      }

      // 强制更新组件以确保样式生效
      this.$forceUpdate && this.$forceUpdate();
    },

    // 获取当前主题
    getCurrentTheme() {
      return this.currentTheme;
    },

    // 判断是否为暗黑模式
    isCurrentDark() {
      return this.isDarkMode;
    }
  }
}