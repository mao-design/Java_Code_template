<template>
  <div class="home">
    <el-card>
      <h2>首页</h2>

      <p>当前已经登录</p>

      <el-button type="danger" @click="handleLogout"> 退出登录 </el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";

import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();

const handleLogout = async () => {
  try {
    const message = await userStore.logout();

    if (message) {
      ElMessage.success(message);
    }
  } finally {
    await router.replace("/login");
  }
};
</script>

<style scoped>
.home {
  padding: 20px;
}
</style>
