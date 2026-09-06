<template>
  <div class="register-page">
    <div class="register-box">
      <h2 class="title">用户注册</h2>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入密码"
          />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="头像地址">
          <el-input v-model="form.avatar" placeholder="请输入头像地址" />
        </el-form-item>

        <el-button
          type="primary"
          class="register-button"
          :loading="loading"
          @click="handleRegister"
        >
          注册
        </el-button>

        <div class="login-link">
          已有账号？
          <router-link to="/login"> 返回登录 </router-link>
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

interface RegisterForm {
  username: string;
  password: string;
  confirmPassword: string;
  nickname: string;
  phone: string;
  avatar: string;
}

const router = useRouter();
const userStore = useUserStore();

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive<RegisterForm>({
  username: "",
  password: "",
  confirmPassword: "",
  nickname: "",
  phone: "",
  avatar: "",
});

const rules: FormRules<RegisterForm> = {
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

  confirmPassword: [
    {
      required: true,
      message: "请再次输入密码",
      trigger: "blur",
    },
  ],
};

const handleRegister = async () => {
  if (!formRef.value) {
    return;
  }

  const valid = await formRef.value.validate();

  if (!valid) {
    return;
  }

  try {
    loading.value = true;

    const message = await userStore.register(form);

    ElMessage.success(message);

    await router.replace("/login");
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 30px 0;
  background: #f5f7fa;
}

.register-box {
  width: 400px;
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

.register-button {
  width: 100%;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
}
</style>
