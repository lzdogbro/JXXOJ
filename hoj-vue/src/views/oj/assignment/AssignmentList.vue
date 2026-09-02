<template>
  <div>
    <el-card class="container">
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.NavBar_Assignment') }}</span>
      </div>
      <div class="content">
        <p>作业功能开发中，敬请期待。</p>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {};
</script>

<style scoped>
.container {
  margin-bottom: 20px;
}
.container .content {
  font-size: 16px;
  margin: 0 50px 20px 50px;
  text-align: center;
  padding: 60px 0;
  color: #909399;
}
@media screen and (max-width: 768px) {
  .container .content {
    font-size: 1rem;
    margin: 0 5px;
  }
}
</style>
