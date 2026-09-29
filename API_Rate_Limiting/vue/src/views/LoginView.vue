<template>
  <div class="login-page">
    <div class="login-box">
      <h2 class="title">系统登录</h2>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item lable="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>
        <br /><br />
        <el-button
          type="primary"
          class="login-button"
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </el-button>

        <div class="register-link">
          没有账号？
          <router-link to="/register"> 注册 </router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { useUserStore } from "@/stores/user";

interface LoginForm {
  username: string;
  password: string;
}

const router = useRouter();
const useStore = useUserStore();

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive<LoginForm>({
  username: "",
  password: "",
});

const rules: FormRules<LoginForm> = {
  username: [
    {
      required: true,
      message: "请输入用户名",
      trigger: "blur",
    },
  ],

  password: [
    {
      required: true,
      message: "请输入密码",
      trigger: "blur",
    },
  ],
};

const handleLogin = async () => {
  if (!formRef.value) {
    return;
  }

  const valid = await formRef.value.validate();

  if (!valid) {
    return;
  }

  try {
    loading.value = true;

    const message = await useStore.login(form);
    ElMessage.success(message);
    await router.replace("/");
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f5f7fa;
}

.login-box {
  width: 360px;
  padding: 32px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgb(0 0 0 / 10%);
}

.title {
  margin: 0 0 28px;
  text-align: center;
  color: #303133;
}

.login-button {
  width: 100%;
}

.register-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
}
</style>
