import axios from "axios";
import type { AxiosError, InternalAxiosRequestConfig } from "axios";
import { ElMessage } from "element-plus";
import type { ApiResponse } from "@/types/api";
import { tokenState, setTokens, clearTokens } from "@/utils/token";

interface LoginToken {
  accessToken: string;
  refreshToken: string;
}

interface RetryConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const request = axios.create({
  baseURL: "/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器
request.interceptors.request.use((config) => {
  if (tokenState.accessToken) {
    config.headers.Authorizaton = `Bearer ${tokenState.accessToken}`;
  }
  return config;
});

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const result = response.data as ApiResponse<unknown>;

    if (result.code !== "00000") {
      ElMessage.error(result.message);

      return Promise.reject(new Error(result.message));
    }

    return response;
  },

  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config as RetryConfig;

    const status = error.response?.status;

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
    if (
      status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      tokenState.refreshToken &&
      !isAuthRequest(originalRequest.url)
    ) {
      originalRequest._retry = true;

      try {
        const response = await axios.post<ApiResponse<LoginToken>>(
          "/api/user/auth/refresh",
          {
            refreshToken: tokenState.refreshToken,
          },
        );

        const result = response.data;

        if (result.code !== "00000") {
          throw new Error(result.message);
        }

        setTokens(result.data.accessToken, result.data.refreshToken);

        originalRequest.headers.Authorization = `Bearer ${result.data.accessToken}`;

        // 重新发送刚才失败的请求
        return request(originalRequest);
      } catch (refreshError) {
        clearTokens();

        let refreshMessage = "";

        if (axios.isAxiosError(refreshError)) {
          refreshMessage = refreshError.request?.data?.message || "";
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
