import { reactive } from "vue";

export const tokenState = reactive({
  accessToken: localStorage.getItem("accessToken") || "",
  refreshToken: localStorage.getItem("refreshToken") || "",
});

export function setTokens(accessToken: string, refreshToken: string);
