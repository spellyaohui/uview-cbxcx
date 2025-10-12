<script>
// 我将提供修改后的方法代码，您可以手动复制替换
async deleteReport() {
    if (!this.selectedReport || this.deleting) return;
    this.deleting = true;

    uni.showLoading({
        title: '删除中...'
    });

    try {
        await this.deletereportApi({
            reportid: this.selectedReport.cxid
        });

        uni.hideLoading();
        uni.showToast({
            title: '删除成功',
            icon: 'success'
        });

        this.closeDeleteModal();
        // 刷新列表
        await this.hqbglsApi();
        this.calculateTodayReports();
    } catch (error) {
        uni.hideLoading();
        uni.showToast({
            title: '删除失败',
            icon: 'none'
        });
    } finally {
        this.deleting = false;
    }
}
</script>