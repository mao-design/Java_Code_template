// reactive() 用来创建“响应式对象”
import { reactive } from "vue";

// 保存 Token
// 获取 Token
// 删除 Token

// localStorage 里面保存数据时使用的 key
const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";

// 定义类型
interface TokenState {
  accessToken: string;
  refreshToken: string;
}

export const tokenState = reactive<TokenState>({
  // localStorage.getItem() 的作用是从浏览器本地存储里面读取数据
  accessToken: localStorage.getItem(ACCESS_TOKEN_KEY) || "",
  refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY) || "",
});

export function setTokens(accessToken: string, refreshToken: string) {
  // 刷新双token状态（更新 Vue 当前运行中的状态）
  tokenState.accessToken = accessToken;
  tokenState.refreshToken = refreshToken;
  // 存入双token
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
}

// 清除token
export function clearTokens() {
  tokenState.accessToken = "";
  tokenState.refreshToken = "";

  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}
