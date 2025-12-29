<template>
  <view class="analysis-container">
    <!-- 顶部操作栏 -->
    <view class="toolbar">
      <button 
        class="tool-btn refresh-btn"
        @click="refreshAnalysis"
        :disabled="loading"
      >
        刷新分析
      </button>
      
      <button 
        class="tool-btn export-btn"
        @click="exportReport"
        :disabled="loading || !hasData"
      >
        导出报告
      </button>
    </view>

    <!-- 总体评分卡片 -->
    <view class="score-card" v-if="hasData">
      <view class="score-circle" :class="conclusion.level">
        <text class="score-value">{{ conclusion.score }}</text>
        <text class="score-label">可靠性评分</text>
      </view>
      
      <view class="score-info">
        <text class="score-level">{{ getLevelText(conclusion.level) }}</text>
        <text class="score-desc">{{ conclusion.text }}</text>
      </view>
    </view>

    <!-- 概览统计 -->
    <view class="section" v-if="hasData">
      <text class="section-title">概览统计</text>
      
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-label">心跳总数</text>
          <text class="stat-value">{{ analysis.overview.totalCount }}</text>
        </view>
        
        <view class="stat-item">
          <text class="stat-label">成功率</text>
          <text class="stat-value success">{{ analysis.overview.successRate }}%</text>
        </view>
        
        <view class="stat-item">
          <text class="stat-label">失败次数</text>
          <text class="stat-value error">{{ analysis.overview.failureCount }}</text>
        </view>
        
        <view class="stat-item">
          <text class="stat-label">运行时长</text>
          <text class="stat-value">{{ analysis.overview.timeSpan.formatted }}</text>
        </view>
      </view>
    </view>

    <!-- 可靠性分析 -->
    <view class="section" v-if="hasData">
      <text class="section-title">可靠性分析</text>
      
      <view class="reliability-card">
        <view class="reliability-row">
          <text class="reliability-label">可靠性等级:</text>
          <text :class="['reliability-value', analysis.reliability.level]">
            {{ getLevelText(analysis.reliability.level) }}
          </text>
        </view>
        
        <view class="reliability-row">
          <text class="reliability-label">最大间隙:</text>
          <text class="reliability-value">{{ analysis.reliability.maxGapFormatted }}</text>
        </view>
        
        <view class="reliability-row">
          <text class="reliability-label">平均间隔:</text>
          <text class="reliability-value">{{ analysis.reliability.avgGapFormatted }}</text>
        </view>
        
        <view class="reliability-row">
          <text class="reliability-label">中断次数:</text>
          <text class="reliability-value">{{ analysis.reliability.gapCount }}</text>
        </view>
      </view>
      
      <!-- 大间隙列表 -->
      <view v-if="analysis.reliability.largeGaps.length > 0" class="gaps-list">
        <text class="subsection-title">心跳中断记录</text>
        
        <view 
          v-for="(gap, index) in analysis.reliability.largeGaps" 
          :key="index"
          class="gap-item"
        >
          <text class="gap-duration">{{ gap.durationFormatted }}</text>
          <text class="gap-time">{{ formatTime(gap.startTime) }} - {{ formatTime(gap.endTime) }}</text>
        </view>
      </view>
    </view>

    <!-- 性能分析 -->
    <view class="section" v-if="hasData">
      <text class="section-title">性能分析</text>
      
      <view class="performance-card">
        <view class="performance-row">
          <text class="performance-label">稳定性:</text>
          <text :class="['performance-value', analysis.performance.stabilityLevel]">
            {{ analysis.performance.stability }}分 ({{ getLevelText(analysis.performance.stabilityLevel) }})
          </text>
        </view>
        
        <view class="performance-row">
          <text class="performance-label">平均间隔:</text>
          <text class="performance-value">{{ analysis.performance.avgIntervalFormatted }}</text>
        </view>
        
        <view class="performance-row">
          <text class="performance-label">最小间隔:</text>
          <text class="performance-value">{{ analysis.performance.minIntervalFormatted }}</text>
        </view>
        
        <view class="performance-row">
          <text class="performance-label">最大间隔:</text>
          <text class="performance-value">{{ analysis.performance.maxIntervalFormatted }}</text>
        </view>
      </view>
    </view>

    <!-- 网络分析 -->
    <view class="section" v-if="hasData && analysis.network.distribution.length > 0">
      <text class="section-title">网络分析</text>
      
      <view class="network-list">
        <view 
          v-for="(item, index) in analysis.network.distribution" 
          :key="index"
          class="network-item"
        >
          <view class="network-header">
            <text class="network-type">{{ getNetworkTypeText(item.type) }}</text>
            <text class="network-percentage">{{ item.percentage }}%</text>
          </view>
          
          <view class="network-stats">
            <text class="network-stat">使用次数: {{ item.count }}</text>
            <text class="network-stat">失败次数: {{ item.failureCount }}</text>
            <text class="network-stat">失败率: {{ item.failureRate }}%</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 异常检测 -->
    <view class="section" v-if="hasData && analysis.anomalies.length > 0">
      <text class="section-title">异常检测</text>
      
      <view class="anomalies-list">
        <view 
          v-for="(anomaly, index) in analysis.anomalies" 
          :key="index"
          :class="['anomaly-item', anomaly.severity]"
        >
          <view class="anomaly-header">
            <text class="anomaly-severity">{{ getSeverityText(anomaly.severity) }}</text>
            <text class="anomaly-type">{{ getAnomalyTypeText(anomaly.type) }}</text>
          </view>
          
          <text class="anomaly-message">{{ anomaly.message }}</text>
        </view>
      </view>
    </view>

    <!-- 建议 -->
    <view class="section" v-if="hasData && analysis.recommendations.length > 0">
      <text class="section-title">优化建议</text>
      
      <view class="recommendations-list">
        <view 
          v-for="(rec, index) in analysis.recommendations" 
          :key="index"
          :class="['recommendation-item', rec.priority]"
        >
          <view class="recommendation-header">
            <text class="recommendation-priority">{{ getPriorityText(rec.priority) }}</text>
            <text class="recommendation-category">{{ getCategoryText(rec.category) }}</text>
          </view>
          
          <text class="recommendation-title">{{ rec.title }}</text>
          <text class="recommendation-message">{{ rec.message }}</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="!hasData && !loading" class="empty-state">
      <text class="empty-text">暂无分析数据</text>
      <text class="empty-hint">请先启动保活服务并等待心跳数据收集</text>
    </view>

    <!-- 加载提示 -->
    <view v-if="loading" class="loading-overlay">
      <view class="loading-spinner"></view>
      <text class="loading-text">分析中...</text>
    </view>
  </view>
</template>

<script>
import HeartbeatAnalyzer from '@/common/HeartbeatAnalyzer.js';
import KeepAliveManager from '@/common/KeepAliveManager.js';

export default {
  data() {
    return {
      analyzer: null,
      keepAliveManager: null,
      loading: false,
      hasData: false,
      analysis: null,
      conclusion: {
        level: 'unknown',
        text: '',
        score: 0
      }
    };
  },
  
  onLoad() {
    this.initPage();
  },
  
  onShow() {
    this.refreshAnalysis();
  },
  
  methods: {
    /**
     * 初始化页面
     */
    async initPage() {
      try {
        this.analyzer = new HeartbeatAnalyzer();
        
        // 获取全局保活管理器实例
        const app = getApp();
        if (app.globalData && app.globalData.keepAliveManager) {
          this.keepAliveManager = app.globalData.keepAliveManager;
        } else {
          this.keepAliveManager = new KeepAliveManager();
        }
        
        await this.refreshAnalysis();
        
      } catch (error) {
        console.error('初始化页面失败:', error);
        uni.showToast({
          title: '初始化失败',
          icon: 'none'
        });
      }
    },
    
    /**
     * 刷新分析
     */
    async refreshAnalysis() {
      try {
        this.loading = true;
        
        if (!this.keepAliveManager || !this.keepAliveManager.isReady()) {
          uni.showToast({
            title: '保活服务未初始化',
            icon: 'none'
          });
          this.hasData = false;
          return;
        }
        
        // 获取心跳日志
        const result = await this.keepAliveManager.getHeartbeatLogs({
          count: 100
        });
        
        if (!result.success || !result.logs || result.logs.length === 0) {
          this.hasData = false;
          uni.showToast({
            title: '暂无心跳数据',
            icon: 'none'
          });
          return;
        }
        
        // 分析数据
        this.analysis = this.analyzer.analyze(result.logs);
        this.conclusion = this.analyzer.generateConclusion(this.analysis);
        this.hasData = true;
        
        uni.showToast({
          title: '分析完成',
          icon: 'success'
        });
        
      } catch (error) {
        console.error('刷新分析失败:', error);
        uni.showToast({
          title: '分析失败',
          icon: 'none'
        });
        this.hasData = false;
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 导出报告
     */
    async exportReport() {
      try {
        if (!this.hasData) {
          uni.showToast({
            title: '暂无数据可导出',
            icon: 'none'
          });
          return;
        }
        
        // 获取心跳日志
        const result = await this.keepAliveManager.getHeartbeatLogs({
          count: 100
        });
        
        if (!result.success || !result.logs) {
          uni.showToast({
            title: '获取数据失败',
            icon: 'none'
          });
          return;
        }
        
        // 生成报告
        const report = this.analyzer.generateReport(result.logs);
        
        // 格式化报告为文本
        const reportText = this.formatReportAsText(report);
        
        // 复制到剪贴板
        uni.setClipboardData({
          data: reportText,
          success: () => {
            uni.showToast({
              title: '报告已复制到剪贴板',
              icon: 'success'
            });
          },
          fail: () => {
            uni.showToast({
              title: '导出失败',
              icon: 'none'
            });
          }
        });
        
      } catch (error) {
        console.error('导出报告失败:', error);
        uni.showToast({
          title: '导出失败',
          icon: 'none'
        });
      }
    },
    
    /**
     * 格式化报告为文本
     */
    formatReportAsText(report) {
      let text = '=== 保活效果分析报告 ===\n\n';
      
      text += `生成时间: ${this.formatTime(report.generatedAt)}\n\n`;
      
      text += '【总体评估】\n';
      text += `可靠性评分: ${report.summary.reliabilityScore}分 (${this.getLevelText(report.summary.reliabilityLevel)})\n`;
      text += `心跳总数: ${report.summary.totalHeartbeats}\n`;
      text += `成功率: ${report.summary.successRate}%\n`;
      text += `运行时长: ${report.summary.timeSpan}\n`;
      text += `结论: ${report.conclusion.text}\n\n`;
      
      if (report.recommendations.length > 0) {
        text += '【优化建议】\n';
        report.recommendations.forEach((rec, index) => {
          text += `${index + 1}. [${this.getPriorityText(rec.priority)}] ${rec.title}\n`;
          text += `   ${rec.message}\n`;
        });
        text += '\n';
      }
      
      if (report.details.anomalies.length > 0) {
        text += '【异常情况】\n';
        report.details.anomalies.forEach((anomaly, index) => {
          text += `${index + 1}. [${this.getSeverityText(anomaly.severity)}] ${anomaly.message}\n`;
        });
        text += '\n';
      }
      
      text += '=== 报告结束 ===';
      
      return text;
    },
    
    /**
     * 格式化时间
     */
    formatTime(timestamp) {
      const date = new Date(timestamp);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
    
    /**
     * 获取等级文本
     */
    getLevelText(level) {
      const levelMap = {
        'excellent': '优秀',
        'good': '良好',
        'fair': '一般',
        'poor': '较差',
        'unknown': '未知'
      };
      return levelMap[level] || '未知';
    },
    
    /**
     * 获取网络类型文本
     */
    getNetworkTypeText(type) {
      const typeMap = {
        'wifi': 'WiFi',
        '4g': '4G',
        '5g': '5G',
        '3g': '3G',
        '2g': '2G',
        'none': '无网络',
        'unknown': '未知'
      };
      return typeMap[type] || type;
    },
    
    /**
     * 获取严重程度文本
     */
    getSeverityText(severity) {
      const severityMap = {
        'high': '严重',
        'medium': '中等',
        'low': '轻微'
      };
      return severityMap[severity] || severity;
    },
    
    /**
     * 获取异常类型文本
     */
    getAnomalyTypeText(type) {
      const typeMap = {
        'consecutive_failures': '连续失败',
        'long_gap': '长时间中断',
        'frequent_network_switch': '频繁网络切换'
      };
      return typeMap[type] || type;
    },
    
    /**
     * 获取优先级文本
     */
    getPriorityText(priority) {
      const priorityMap = {
        'high': '高',
        'medium': '中',
        'low': '低'
      };
      return priorityMap[priority] || priority;
    },
    
    /**
     * 获取分类文本
     */
    getCategoryText(category) {
      const categoryMap = {
        'reliability': '可靠性',
        'performance': '性能',
        'network': '网络',
        'battery': '电池',
        'anomaly': '异常'
      };
      return categoryMap[category] || category;
    }
  }
};
</script>

<style lang="scss" scoped>
.analysis-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 40rpx;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
  gap: 10rpx;
}

.tool-btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 10rpx;
  font-size: 26rpx;
  font-weight: bold;
  border: none;
}

.refresh-btn {
  background-color: #2196F3;
  color: white;
}

.export-btn {
  background-color: #4CAF50;
  color: white;
}

.tool-btn:disabled {
  background-color: #ccc !important;
  color: #999 !important;
}

.score-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

.score-circle {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.2);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin-right: 30rpx;
}

.score-value {
  font-size: 56rpx;
  font-weight: bold;
  color: white;
}

.score-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 8rpx;
}

.score-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.score-level {
  font-size: 36rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 12rpx;
}

.score-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

.section {
  background-color: white;
  border-radius: 15rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.subsection-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #666;
  margin: 20rpx 0 15rpx 0;
  display: block;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.stat-item {
  text-align: center;
  padding: 20rpx;
  background-color: #f8f9fa;
  border-radius: 10rpx;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.stat-value {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.stat-value.success {
  color: #4CAF50;
}

.stat-value.error {
  color: #F44336;
}

.reliability-card,
.performance-card {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.reliability-row,
.performance-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.reliability-row:last-child,
.performance-row:last-child {
  border-bottom: none;
}

.reliability-label,
.performance-label {
  font-size: 28rpx;
  color: #666;
}

.reliability-value,
.performance-value {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.reliability-value.excellent,
.performance-value.excellent {
  color: #4CAF50;
}

.reliability-value.good,
.performance-value.good {
  color: #8BC34A;
}

.reliability-value.fair,
.performance-value.fair {
  color: #FF9800;
}

.reliability-value.poor,
.performance-value.poor {
  color: #F44336;
}

.gaps-list {
  margin-top: 20rpx;
}

.gap-item {
  background-color: #fff3e0;
  border-left: 4rpx solid #FF9800;
  padding: 15rpx;
  margin-bottom: 10rpx;
  border-radius: 8rpx;
}

.gap-duration {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #FF9800;
  margin-bottom: 8rpx;
}

.gap-time {
  display: block;
  font-size: 24rpx;
  color: #666;
}

.network-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.network-item {
  background-color: #f8f9fa;
  border-radius: 10rpx;
  padding: 20rpx;
}

.network-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.network-type {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.network-percentage {
  font-size: 32rpx;
  font-weight: bold;
  color: #2196F3;
}

.network-stats {
  display: flex;
  justify-content: space-between;
  gap: 10rpx;
}

.network-stat {
  font-size: 24rpx;
  color: #666;
}

.anomalies-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.anomaly-item {
  border-radius: 10rpx;
  padding: 20rpx;
  border-left: 4rpx solid;
}

.anomaly-item.high {
  background-color: #ffebee;
  border-left-color: #F44336;
}

.anomaly-item.medium {
  background-color: #fff3e0;
  border-left-color: #FF9800;
}

.anomaly-item.low {
  background-color: #e3f2fd;
  border-left-color: #2196F3;
}

.anomaly-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.anomaly-severity {
  font-size: 24rpx;
  font-weight: bold;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background-color: rgba(0, 0, 0, 0.05);
}

.anomaly-type {
  font-size: 24rpx;
  color: #666;
}

.anomaly-message {
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
}

.recommendations-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.recommendation-item {
  border-radius: 10rpx;
  padding: 20rpx;
  border-left: 4rpx solid;
}

.recommendation-item.high {
  background-color: #ffebee;
  border-left-color: #F44336;
}

.recommendation-item.medium {
  background-color: #fff3e0;
  border-left-color: #FF9800;
}

.recommendation-item.low {
  background-color: #e8f5e9;
  border-left-color: #4CAF50;
}

.recommendation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.recommendation-priority {
  font-size: 24rpx;
  font-weight: bold;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background-color: rgba(0, 0, 0, 0.05);
}

.recommendation-category {
  font-size: 24rpx;
  color: #666;
}

.recommendation-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.recommendation-message {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 150rpx 0;
}

.empty-text {
  font-size: 32rpx;
  color: #999;
  margin-bottom: 20rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #ccc;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 20rpx;
  font-size: 28rpx;
  color: white;
}
</style>
