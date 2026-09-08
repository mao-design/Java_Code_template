import { computed } from "vue";
import { defineStore } from "pinia";

// user.ts 文件就是 Vue3 + Pinia 项目中专门管理用户认证状态的核心文件。
// 页面不直接调用接口，而是调用这里的方法；这里再调用 Axios 接口。这样项目结构更清晰。

import {
  loginApi,
  LogoutApi,
  registerApi,
  type LoginParams,
  type RegisterParams,
} from "@/api/auth";

import { tokenState, setTokens, clearTokens } from "@/utils/token";
// import { ElMessage } from "element-plus";

export const useUserStore = defineStore("user", () => {
  // 判断是否登录成功
  const isLoggedIn = computed(() => {
    return !!tokenState.accessToken;
  });

  // 注册
  const register = async (params: RegisterParams) => {
    const response = await registerApi(params);

    return response.data.message;
  };

  // 登录
  const login = async (params: LoginParams) => {
    const response = await loginApi(params);
    const result = response.data;
    setTokens(result.data.accessToken, result.data.refreshToken);
    return result.message;
  };

  // 退出登录
  const logout = async () => {
    const refreshToken = tokenState.refreshToken;

    try {
      if (!refreshToken) {
        return "";
      }
      // 等待后端响应
      const response = await LogoutApi({
        refreshToken,
      });

      // if (response.data.code === "A0205") {
      //   ElMention.error("RefreshToken错误");
      //   return;
      // }
      // ElMessage.success(response.data.message);
      return response.data.message;
    } finally {
      // 不管结果怎么样，只要用户执行了退出操作
      // 就清除本地token，大不了用户重新登录
      clearTokens();
    }
  };

  // 将对应的接口返回给 useUserStore
  return {
    isLoggedIn,
    register,
    login,
    logout,
  };
  // 外部系统调用
  // const userStore=useUserStore()
  // userStore.login()
});
