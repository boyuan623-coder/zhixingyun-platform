<template>
  <div class="about">
    <h1>关于智行云</h1>
    <a-descriptions bordered :column="1">
      <a-descriptions-item label="平台名称">智行云</a-descriptions-item>
      <a-descriptions-item label="作者">tuboyuan</a-descriptions-item>
      <a-descriptions-item label="项目简介">火车票售票系统，包含用户端、管理端与微服务后端。</a-descriptions-item>
      <a-descriptions-item label="技术栈">Spring Boot 3、Spring Cloud Gateway、MyBatis、Quartz、Vue 3</a-descriptions-item>
      <a-descriptions-item label="后端服务">gateway:8000 / member:8001 / business:8002 / batch:8003</a-descriptions-item>
      <a-descriptions-item label="前端应用">web 用户端:9000 / admin 管理端:9001</a-descriptions-item>
      <a-descriptions-item label="编译要求">JDK 17+，推荐使用项目内 mvnw.cmd 编译</a-descriptions-item>
      <a-descriptions-item label="数据库">需本地或远程 MySQL，执行 sql 目录下脚本初始化</a-descriptions-item>
    </a-descriptions>
  </div>
</template>
<script>
import { defineComponent } from 'vue';

export default defineComponent({
  setup() {
    return {};
  },
});
</script>
<style>
.about {
  max-width: 900px;
}
</style>
