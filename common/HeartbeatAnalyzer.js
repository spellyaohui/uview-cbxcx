/**
 * 心跳数据分析器
 * 提供心跳数据的统计分析和异常检测功能
 * 
 * 验证需求：4.4
 */
class HeartbeatAnalyzer {
  constructor() {
    this.analysisCache = null;
    this.cacheTime = 0;
    this.cacheDuration = 60000; // 缓存1分钟
  }
  
  /**
   * 分析心跳日志
   * @param {Array} logs 心跳日志数组
   * @returns {Object} 分析结果
   */
  analyze(logs) {
    if (!logs || logs.length === 0) {
      return this.getEmptyAnalysis();
    }
    
    // 检查缓存
    const now = Date.now();
    if (this.analysisCache && (now - this.cacheTime) < this.cacheDuration) {
      return this.analysisCache;
    }
    
    const analysis = {
      overview: this.analyzeOverview(logs),
      reliability: this.analyzeReliability(logs),
      performance: this.analyzePerformance(logs),
      network: this.analyzeNetwork(logs),
      battery: this.analyzeBattery(logs),
      anomalies: this.detectAnomalies(logs),
      trends: this.analyzeTrends(logs),
      recommendations: []
    };
    
    // 生成建议
    analysis.recommendations = this.generateRecommendations(analysis);
    
    // 缓存结果
    this.analysisCache = analysis;
    this.cacheTime = now;
    
    return analysis;
  }
  
  /**
   * 分析概览信息
   */
  analyzeOverview(logs) {
    const totalCount = logs.length;
    const successCount = logs.filter(log => log.status === 'success').length;
    const failureCount = logs.filter(log => log.status === 'error' || log.status === 'failure').length;
    
    const firstLog = logs[logs.length - 1];
    const lastLog = logs[0];
    
    const timeSpan = lastLog.timestamp - firstLog.timestamp;
    const hours = Math.floor(timeSpan / 3600000);
    const minutes = Math.floor((timeSpan % 3600000) / 60000);
    
    return {
      totalCount: totalCount,
      successCount: successCount,
      failureCount: failureCount,
      successRate: totalCount > 0 ? ((successCount / totalCount) * 100).toFixed(1) : 0,
      timeSpan: {
        milliseconds: timeSpan,
        hours: hours,
        minutes: minutes,
        formatted: `${hours}小时${minutes}分钟`
      },
      firstHeartbeat: firstLog.timestamp,
      lastHeartbeat: lastLog.timestamp
    };
  }
  
  /**
   * 分析可靠性
   */
  analyzeReliability(logs) {
    if (logs.length < 2) {
      return {
        score: 0,
        level: 'unknown',
        maxGap: 0,
        avgGap: 0,
        gapCount: 0,
        largeGaps: []
      };
    }
    
    const gaps = [];
    const largeGaps = [];
    const expectedInterval = 30000; // 30秒
    const gapThreshold = expectedInterval * 2; // 超过60秒视为间隙
    
    for (let i = 0; i < logs.length - 1; i++) {
      const gap = logs[i].timestamp - logs[i + 1].timestamp;
      gaps.push(gap);
      
      if (gap > gapThreshold) {
        largeGaps.push({
          startTime: logs[i + 1].timestamp,
          endTime: logs[i].timestamp,
          duration: gap,
          durationFormatted: this.formatDuration(gap)
        });
      }
    }
    
    const maxGap = Math.max(...gaps);
    const avgGap = gaps.reduce((sum, gap) => sum + gap, 0) / gaps.length;
    
    // 计算可靠性分数 (0-100)
    const gapPenalty = (largeGaps.length / logs.length) * 50;
    const failureRate = (logs.filter(log => log.status === 'error' || log.status === 'failure').length / logs.length) * 50;
    const score = Math.max(0, 100 - gapPenalty - failureRate);
    
    let level = 'excellent';
    if (score < 60) level = 'poor';
    else if (score < 75) level = 'fair';
    else if (score < 90) level = 'good';
    
    return {
      score: score.toFixed(1),
      level: level,
      maxGap: maxGap,
      maxGapFormatted: this.formatDuration(maxGap),
      avgGap: avgGap,
      avgGapFormatted: this.formatDuration(avgGap),
      gapCount: largeGaps.length,
      largeGaps: largeGaps.slice(0, 5) // 只返回前5个最大间隙
    };
  }
  
  /**
   * 分析性能
   */
  analyzePerformance(logs) {
    const intervals = [];
    
    for (let i = 0; i < logs.length - 1; i++) {
      const interval = logs[i].timestamp - logs[i + 1].timestamp;
      intervals.push(interval);
    }
    
    if (intervals.length === 0) {
      return {
        avgInterval: 0,
        minInterval: 0,
        maxInterval: 0,
        stability: 0,
        stabilityLevel: 'unknown'
      };
    }
    
    const avgInterval = intervals.reduce((sum, val) => sum + val, 0) / intervals.length;
    const minInterval = Math.min(...intervals);
    const maxInterval = Math.max(...intervals);
    
    // 计算标准差来评估稳定性
    const variance = intervals.reduce((sum, val) => sum + Math.pow(val - avgInterval, 2), 0) / intervals.length;
    const stdDev = Math.sqrt(variance);
    
    // 稳定性分数：标准差越小越稳定
    const stability = Math.max(0, 100 - (stdDev / avgInterval) * 100);
    
    let stabilityLevel = 'excellent';
    if (stability < 60) stabilityLevel = 'poor';
    else if (stability < 75) stabilityLevel = 'fair';
    else if (stability < 90) stabilityLevel = 'good';
    
    return {
      avgInterval: avgInterval,
      avgIntervalFormatted: this.formatDuration(avgInterval),
      minInterval: minInterval,
      minIntervalFormatted: this.formatDuration(minInterval),
      maxInterval: maxInterval,
      maxIntervalFormatted: this.formatDuration(maxInterval),
      stability: stability.toFixed(1),
      stabilityLevel: stabilityLevel,
      stdDev: stdDev
    };
  }
  
  /**
   * 分析网络状况
   */
  analyzeNetwork(logs) {
    const networkTypes = {};
    const networkFailures = {};
    
    logs.forEach(log => {
      const networkType = log.networkType || 'unknown';
      networkTypes[networkType] = (networkTypes[networkType] || 0) + 1;
      
      if (log.status === 'error' || log.status === 'failure') {
        networkFailures[networkType] = (networkFailures[networkType] || 0) + 1;
      }
    });
    
    const distribution = Object.entries(networkTypes).map(([type, count]) => ({
      type: type,
      count: count,
      percentage: ((count / logs.length) * 100).toFixed(1),
      failureCount: networkFailures[type] || 0,
      failureRate: networkFailures[type] ? ((networkFailures[type] / count) * 100).toFixed(1) : 0
    })).sort((a, b) => b.count - a.count);
    
    return {
      distribution: distribution,
      mostCommon: distribution[0]?.type || 'unknown',
      totalTypes: Object.keys(networkTypes).length
    };
  }
  
  /**
   * 分析电池状况
   */
  analyzeBattery(logs) {
    const batteryLevels = logs
      .filter(log => log.batteryLevel !== undefined && log.batteryLevel >= 0)
      .map(log => log.batteryLevel);
    
    if (batteryLevels.length === 0) {
      return {
        avgLevel: 0,
        minLevel: 0,
        maxLevel: 0,
        trend: 'unknown',
        lowBatteryCount: 0
      };
    }
    
    const avgLevel = batteryLevels.reduce((sum, val) => sum + val, 0) / batteryLevels.length;
    const minLevel = Math.min(...batteryLevels);
    const maxLevel = Math.max(...batteryLevels);
    
    // 分析电池趋势
    const firstBattery = batteryLevels[batteryLevels.length - 1];
    const lastBattery = batteryLevels[0];
    const batteryChange = lastBattery - firstBattery;
    
    let trend = 'stable';
    if (batteryChange < -10) trend = 'decreasing';
    else if (batteryChange > 10) trend = 'increasing';
    
    // 统计低电量次数（<20%）
    const lowBatteryCount = batteryLevels.filter(level => level < 20).length;
    
    return {
      avgLevel: avgLevel.toFixed(1),
      minLevel: minLevel,
      maxLevel: maxLevel,
      trend: trend,
      batteryChange: batteryChange,
      lowBatteryCount: lowBatteryCount,
      lowBatteryPercentage: ((lowBatteryCount / batteryLevels.length) * 100).toFixed(1)
    };
  }
  
  /**
   * 检测异常
   */
  detectAnomalies(logs) {
    const anomalies = [];
    
    // 检测连续失败
    let consecutiveFailures = 0;
    let maxConsecutiveFailures = 0;
    
    logs.forEach((log, index) => {
      if (log.status === 'error' || log.status === 'failure') {
        consecutiveFailures++;
        maxConsecutiveFailures = Math.max(maxConsecutiveFailures, consecutiveFailures);
      } else {
        consecutiveFailures = 0;
      }
    });
    
    if (maxConsecutiveFailures >= 3) {
      anomalies.push({
        type: 'consecutive_failures',
        severity: 'high',
        count: maxConsecutiveFailures,
        message: `检测到连续${maxConsecutiveFailures}次心跳失败`
      });
    }
    
    // 检测长时间无心跳
    const expectedInterval = 30000;
    const longGapThreshold = expectedInterval * 5; // 超过2.5分钟
    
    for (let i = 0; i < logs.length - 1; i++) {
      const gap = logs[i].timestamp - logs[i + 1].timestamp;
      if (gap > longGapThreshold) {
        anomalies.push({
          type: 'long_gap',
          severity: 'medium',
          duration: gap,
          durationFormatted: this.formatDuration(gap),
          startTime: logs[i + 1].timestamp,
          endTime: logs[i].timestamp,
          message: `检测到${this.formatDuration(gap)}的心跳中断`
        });
      }
    }
    
    // 检测网络频繁切换
    let networkSwitches = 0;
    for (let i = 0; i < logs.length - 1; i++) {
      if (logs[i].networkType !== logs[i + 1].networkType) {
        networkSwitches++;
      }
    }
    
    if (networkSwitches > logs.length * 0.3) {
      anomalies.push({
        type: 'frequent_network_switch',
        severity: 'low',
        count: networkSwitches,
        message: `检测到频繁的网络切换（${networkSwitches}次）`
      });
    }
    
    return anomalies;
  }
  
  /**
   * 分析趋势
   */
  analyzeTrends(logs) {
    if (logs.length < 10) {
      return {
        available: false,
        message: '数据量不足，无法分析趋势'
      };
    }
    
    // 按小时分组统计
    const hourlyStats = {};
    
    logs.forEach(log => {
      const hour = new Date(log.timestamp).getHours();
      if (!hourlyStats[hour]) {
        hourlyStats[hour] = {
          total: 0,
          success: 0,
          failure: 0
        };
      }
      
      hourlyStats[hour].total++;
      if (log.status === 'success') {
        hourlyStats[hour].success++;
      } else if (log.status === 'error' || log.status === 'failure') {
        hourlyStats[hour].failure++;
      }
    });
    
    // 找出最活跃和最不活跃的时段
    const hourlyData = Object.entries(hourlyStats).map(([hour, stats]) => ({
      hour: parseInt(hour),
      ...stats,
      successRate: ((stats.success / stats.total) * 100).toFixed(1)
    })).sort((a, b) => b.total - a.total);
    
    return {
      available: true,
      hourlyData: hourlyData,
      mostActiveHour: hourlyData[0],
      leastActiveHour: hourlyData[hourlyData.length - 1],
      peakHours: hourlyData.slice(0, 3).map(d => d.hour),
      lowHours: hourlyData.slice(-3).map(d => d.hour)
    };
  }
  
  /**
   * 生成建议
   */
  generateRecommendations(analysis) {
    const recommendations = [];
    
    // 基于可靠性的建议
    if (analysis.reliability.score < 75) {
      recommendations.push({
        priority: 'high',
        category: 'reliability',
        title: '保活可靠性较低',
        message: '建议检查设备电池优化设置，确保应用已添加到白名单',
        action: 'check_battery_optimization'
      });
    }
    
    if (analysis.reliability.gapCount > 5) {
      recommendations.push({
        priority: 'medium',
        category: 'reliability',
        title: '检测到多次心跳中断',
        message: `发现${analysis.reliability.gapCount}次较长的心跳间隙，可能影响保活效果`,
        action: 'review_gaps'
      });
    }
    
    // 基于性能的建议
    if (analysis.performance.stability < 70) {
      recommendations.push({
        priority: 'medium',
        category: 'performance',
        title: '心跳间隔不稳定',
        message: '心跳间隔波动较大，建议检查系统资源占用情况',
        action: 'check_system_resources'
      });
    }
    
    // 基于网络的建议
    const wifiUsage = analysis.network.distribution.find(d => d.type === 'wifi');
    if (wifiUsage && parseFloat(wifiUsage.failureRate) > 20) {
      recommendations.push({
        priority: 'low',
        category: 'network',
        title: 'WiFi网络下失败率较高',
        message: `WiFi环境下心跳失败率为${wifiUsage.failureRate}%，建议检查网络稳定性`,
        action: 'check_wifi_stability'
      });
    }
    
    // 基于电池的建议
    if (analysis.battery.lowBatteryCount > 0) {
      recommendations.push({
        priority: 'low',
        category: 'battery',
        title: '低电量运行记录',
        message: `检测到${analysis.battery.lowBatteryCount}次低电量运行，建议优化电池使用策略`,
        action: 'optimize_battery_usage'
      });
    }
    
    // 基于异常的建议
    analysis.anomalies.forEach(anomaly => {
      if (anomaly.severity === 'high') {
        recommendations.push({
          priority: 'high',
          category: 'anomaly',
          title: '检测到严重异常',
          message: anomaly.message,
          action: 'investigate_anomaly'
        });
      }
    });
    
    return recommendations;
  }
  
  /**
   * 生成保活效果报告
   */
  generateReport(logs) {
    const analysis = this.analyze(logs);
    
    const report = {
      generatedAt: Date.now(),
      summary: {
        totalHeartbeats: analysis.overview.totalCount,
        successRate: analysis.overview.successRate,
        reliabilityScore: analysis.reliability.score,
        reliabilityLevel: analysis.reliability.level,
        timeSpan: analysis.overview.timeSpan.formatted
      },
      details: analysis,
      conclusion: this.generateConclusion(analysis),
      recommendations: analysis.recommendations
    };
    
    return report;
  }
  
  /**
   * 生成结论
   */
  generateConclusion(analysis) {
    const score = parseFloat(analysis.reliability.score);
    
    let conclusion = '';
    let level = '';
    
    if (score >= 90) {
      level = 'excellent';
      conclusion = '保活效果优秀，系统运行稳定可靠。';
    } else if (score >= 75) {
      level = 'good';
      conclusion = '保活效果良好，偶有小问题但整体稳定。';
    } else if (score >= 60) {
      level = 'fair';
      conclusion = '保活效果一般，存在一些稳定性问题，建议优化。';
    } else {
      level = 'poor';
      conclusion = '保活效果较差，存在严重的稳定性问题，需要立即处理。';
    }
    
    // 添加具体问题描述
    const issues = [];
    
    if (analysis.reliability.gapCount > 0) {
      issues.push(`检测到${analysis.reliability.gapCount}次心跳中断`);
    }
    
    if (analysis.anomalies.length > 0) {
      issues.push(`发现${analysis.anomalies.length}个异常情况`);
    }
    
    if (parseFloat(analysis.overview.successRate) < 90) {
      issues.push(`心跳成功率为${analysis.overview.successRate}%`);
    }
    
    if (issues.length > 0) {
      conclusion += ' ' + issues.join('，') + '。';
    }
    
    return {
      level: level,
      text: conclusion,
      score: score
    };
  }
  
  /**
   * 获取空分析结果
   */
  getEmptyAnalysis() {
    return {
      overview: {
        totalCount: 0,
        successCount: 0,
        failureCount: 0,
        successRate: 0,
        timeSpan: { milliseconds: 0, hours: 0, minutes: 0, formatted: '0小时0分钟' },
        firstHeartbeat: 0,
        lastHeartbeat: 0
      },
      reliability: {
        score: 0,
        level: 'unknown',
        maxGap: 0,
        avgGap: 0,
        gapCount: 0,
        largeGaps: []
      },
      performance: {
        avgInterval: 0,
        minInterval: 0,
        maxInterval: 0,
        stability: 0,
        stabilityLevel: 'unknown'
      },
      network: {
        distribution: [],
        mostCommon: 'unknown',
        totalTypes: 0
      },
      battery: {
        avgLevel: 0,
        minLevel: 0,
        maxLevel: 0,
        trend: 'unknown',
        lowBatteryCount: 0
      },
      anomalies: [],
      trends: {
        available: false,
        message: '暂无数据'
      },
      recommendations: []
    };
  }
  
  /**
   * 格式化时长
   */
  formatDuration(milliseconds) {
    const seconds = Math.floor(milliseconds / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    
    if (hours > 0) {
      return `${hours}小时${minutes % 60}分钟`;
    } else if (minutes > 0) {
      return `${minutes}分钟${seconds % 60}秒`;
    } else {
      return `${seconds}秒`;
    }
  }
  
  /**
   * 清除缓存
   */
  clearCache() {
    this.analysisCache = null;
    this.cacheTime = 0;
  }
}

export default HeartbeatAnalyzer;
