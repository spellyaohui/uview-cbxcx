/* 字体加载检测和错误处理 */

(function() {
  // 检测Font Awesome字体是否加载成功
  function checkFontLoaded() {
    // 创建测试元素
    const testElement = document.createElement('span');
    testElement.style.position = 'absolute';
    testElement.style.visibility = 'hidden';
    testElement.style.fontSize = '100px';
    testElement.style.fontFamily = 'Font Awesome 5 Free';
    testElement.style.fontWeight = '900';
    testElement.innerHTML = '&#xf060;'; // 测试fa-arrow-left图标
    document.body.appendChild(testElement);

    // 获取字符宽度
    const width = testElement.offsetWidth;
    document.body.removeChild(testElement);

    // 如果宽度为0或很小，说明字体未加载
    return width > 50; // Font Awesome图标应该有明显的宽度
  }

  // 字体加载失败处理
  function handleFontLoadError() {
    console.warn('Font Awesome字体加载失败，切换到Unicode备用图标');
    document.documentElement.classList.add('font-loading-error');

    // 可选：显示提示信息
    if (typeof uni !== 'undefined' && uni.showToast) {
      uni.showToast({
        title: '图标字体加载失败',
        icon: 'none',
        duration: 2000
      });
    }
  }

  // 延迟检测字体加载状态
  setTimeout(function() {
    if (!checkFontLoaded()) {
      handleFontLoadError();
    } else {
      console.log('Font Awesome字体加载成功');
    }
  }, 3000);

  // DOM加载完成后再次检测
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
      setTimeout(function() {
        if (!checkFontLoaded()) {
          handleFontLoadError();
        }
      }, 1000);
    });
  }
})();