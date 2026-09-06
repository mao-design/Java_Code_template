import { createRouter, createWebHistory } from "vue-router";

import { useUserStore } from "@/stores/user";

const router = createRouter({
  history: createWebHistory(),

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

router.beforeEach((to) => {
  const userStore = useUserStore();

  const publicPaths = ["/login", "/register"];

  const isPublic = publicPaths.includes(to.path);

  // 没登录不能访问系统
  if (!userStore.isLoggedIn && !isPublic) {
    return "/login";
  }

  // 已登录不能再次进入登录/注册页面
  if (userStore.isLoggedIn && isPublic) {
    return "/";
  }
});

export default router;
