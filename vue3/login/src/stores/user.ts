import { computed } from "vue";
import { defineStore } from "pinia";

import {
  loginApi,
  LogoutApi,
  registerApi,
  type LoginParams,
  type RefreshParams,
} from "@/api/auth";

import { tokenState, setTokens, clearTokens } from "@/utils/token";

export const useUserStore = defineStore("user", () => {
  const isLoggedIn = computed(() => {
    return !!tokenState.accessToken;
  });

  // 注册
  const register = async (params: RefreshParams) => {
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

      const response = await LogoutApi({
        refreshToken,
      });

      return response.data.message;
    } finally {
      clearTokens();
    }
  };

  return {
    isLoggedIn,
    register,
    login,
    logout,
  };
});
