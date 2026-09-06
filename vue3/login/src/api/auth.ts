import request from "@/utils/request";
import type { ApiResponse } from "@/types/api";

// 登录参数
export interface LoginParams {
  username: string;
  password: string;
}

// 注册属性
export interface RegisterParams {
  username: string;
  password: string;
  confirmPassword: string;
  nickName?: string;
  phone?: string;
  avatar?: string;
}

// RefreshToken参数
export interface RefreshParams {
  refreshToken: string;
}

// 登录返回双Token
export interface LoginToken {
  accessToken: string;
  refreshToken: string;
}

// 注册函数
export function registerApi(data: RefreshParams) {
  return request.post<ApiResponse<object>>("/user/auth/register", data);
}

// 登录
export function loginApi(data: LoginParams) {
  return request.post<ApiResponse<LoginToken>>("/user/auth/login", data);
}

// 刷新Token
export function refreshApi(data: RefreshParams) {
  return request.post<ApiResponse<LoginToken>>("/user/auth/refresh", data);
}

// 退出登录
export function LogoutApi(data: RefreshParams) {
  return request.post<ApiResponse<Object>>("/user/auth/logout", data);
}
