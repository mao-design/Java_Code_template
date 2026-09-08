import { createRouter, createWebHistory } from "vue-router";

import { useUserStore } from "@/stores/user";

const router = createRouter({
  history: createWebHistory(),
  // 路由表
  routes: [
    {
      path: "/login",
      component: () => import("@/views/LoginView.vue"),
    },

    {
      path: "/register",
      component: () => import("@/views/RegisterView.vue"),
    },

    {
      path: "/",
      component: () => import("@/views/HomeView.vue"),
    },
  ],
});

// 守卫路由，
router.beforeEach((to) => {
  const userStore = useUserStore();

  const publicPaths = ["/login", "/register"];

  // includes() 判断数组里面有没有这个值
  // 例如：
  // ["a","b"].includes("a")
  // 返回 true
  const isPublic = publicPaths.includes(to.path);

  // 没登录不能访问系统
  // 未登录和访问页不是公开的
  if (!userStore.isLoggedIn && !isPublic) {
    return "/login";
  }

  // 已登录不能再次进入登录/注册页面
  if (userStore.isLoggedIn && isPublic) {
    return "/";
  }
});

export default router;
