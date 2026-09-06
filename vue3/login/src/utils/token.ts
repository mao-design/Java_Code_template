import { reactive } from "vue";

// 保存 Token
// 获取 Token
// 删除 Token

// token_key名称（常量）
const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";

// 定义类型
interface TokenState {
  accessToken: string;
  refreshToken: string;
}

//
export const tokenState = reactive<TokenState>({
  accessToken: localStorage.getItem(ACCESS_TOKEN_KEY) || "",
  refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY) || "",
});

//
export function setTokens(accessToken: string, refreshToken: string) {
  tokenState.accessToken = accessToken;
  tokenState.refreshToken = refreshToken;

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
