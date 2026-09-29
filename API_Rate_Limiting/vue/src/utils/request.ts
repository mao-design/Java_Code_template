import axios from "axios";
import type { AxiosError, InternalAxiosRequestConfig } from "axios";
import { ElMessage } from "element-plus";
import type { ApiResponse } from "@/types/api";
import { tokenState, setTokens, clearTokens } from "@/utils/token";

interface LoginToken {
  accessToken: string;
  refreshToken: string;
}

// 有token就表示已经有请求刷新token了，所以就没必要重复刷新了
let refreshPromise: Promise<LoginToken> | null = null;

interface RetryConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const request = axios.create({
  baseURL: "/api",
  // 超过10s
  timeout: 10000,
  headers: {
    // 告诉后端，前端发送的是json
    "Content-Type": "application/json",
  },
});

function refreshTokens() {
  // 没有refreshToken就报错
  if (!tokenState.refreshToken) {
    return Promise.reject(new Error("Refresh Token 不存在"));
  }

  if (!refreshPromise) {
    // 给 refreshPromise 添加刷新好的 双token
    // 后续接口直接调用刷新好的 双token
    refreshPromise = axios
      .post<ApiResponse<LoginToken>>("/api/user/auth/refresh", {
        refreshToken: tokenState.refreshToken,
      })

      .then((response) => {
        const result = response.data;

        if (result.code !== "00000") {
          throw new Error(result.message);
        }

        // 存入token
        setTokens(result.data.accessToken, result.data.refreshToken);

        return result.data;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  // 有双token直接返回
  return refreshPromise;
}

// 请求拦截器
request.interceptors.request.use((config) => {
  // 判断token
  if (tokenState.accessToken) {
    config.headers.Authorization = `Bearer ${tokenState.accessToken}`;
  }
  return config;
});

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 将后端发送过来的json强制转换为定义好的api格式模版，类型未知，如果要使用，则需要进行类型判断
    const result = response.data as ApiResponse<unknown>;

    if (result.code !== "00000") {
      ElMessage.error(result.message);
      // 告诉调用方：这次请求失败。
      return Promise.reject(new Error(result.message));
    }
    // 如果成功，则返回页面
    return response;
  },
  // 失败响应
  async (error: AxiosError<ApiResponse<unknown>>) => {
    // 响应错误，则为false
    const originalRequest = error.config as RetryConfig;
    // http状态码
    const status = error.response?.status;
    // 错误提示
    const message = error.response?.data?.message;

    /*
     * Access Token 失效
     *
     * 401
     * ↓
     * 使用 Refresh Token 获取新 Token
     * ↓
     * 重新发送原来的请求
     */
    /**
     * originalRequest：确保原来的请求存在
     * !originalRequest._retry：确保没有刷新过，防止死循环
     * tokenState.refreshToken：要有 refreshToken
     * !isAuthRequest(originalRequest.url)：注册接口不刷新
     */
    if (
      status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      tokenState.refreshToken &&
      !isAuthRequest(originalRequest.url)
    ) {
      originalRequest._retry = true;

      try {
        const tokens = await refreshTokens();

        originalRequest.headers.Authorization = `Bearer ${tokens.accessToken}`;

        return request(originalRequest);
      } catch (refreshError) {
        // 删除token
        clearTokens();

        let refreshMessage = "";

        if (axios.isAxiosError(refreshError)) {
          refreshMessage = refreshError.response?.data?.message || "";
        } else if (refreshError instanceof Error) {
          refreshMessage = refreshError.message;
        }

        if (refreshMessage) {
          ElMessage.error(refreshMessage);
        }

        if (window.location.pathname !== "/login") {
          window.location.href = "/login";
        }

        return Promise.reject(refreshError);
      }
    }

    /*
     * 400 / 401 / 409 / 500
     *
     * 后端有 message 就直接显示后端 message
     */
    if (message) {
      ElMessage.error(message);
    } else {
      ElMessage.error("网络请求失败");
    }

    return Promise.reject(error);
  },
);

function isAuthRequest(url?: string) {
  if (!url) {
    return false;
  }

  return (
    url.includes("/user/auth/login") ||
    url.includes("/user/auth/register") ||
    url.includes("/user/auth/refresh") ||
    url.includes("/user/auth/logout")
  );
}

export default request;
