import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { getAdminAuth } from '../auth/adminAuth'
import { getUserAuth } from '../auth/userAuth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView, meta: { title: '首页' } },
    { path: '/playlists', name: 'playlists', component: () => import('../views/PlaylistsView.vue'), meta: { title: '歌单' } },
    { path: '/playlists/:id', name: 'playlist-detail', component: () => import('../views/PlaylistDetailView.vue'), meta: { title: '歌单详情' } },
    { path: '/artists', name: 'artists', component: () => import('../views/ArtistsView.vue'), meta: { title: '歌手' } },
    { path: '/artists/:id', name: 'artist-detail', component: () => import('../views/ArtistDetailView.vue'), meta: { title: '歌手详情' } },
    { path: '/search', name: 'search', component: () => import('../views/SearchView.vue'), meta: { title: '搜索' } },
    { path: '/login', name: 'login', component: () => import('../views/AuthView.vue'), meta: { title: '登录与注册' } },
    { path: '/profile', name: 'profile', component: () => import('../views/ProfileView.vue'), meta: { title: '个人资料', user: true } },
    { path: '/me', name: 'my', component: () => import('../views/MyView.vue'), meta: { title: '我的', user: true } },
    { path: '/favorites', name: 'favorites', component: () => import('../views/FavoritesView.vue'), meta: { title: '我的收藏', user: true } },
    { path: '/admin/login', name: 'admin-login', component: () => import('../views/AdminLoginView.vue'), meta: { title: '管理员登录' } },
    { path: '/admin', name: 'admin', component: () => import('../views/PlaceholderView.vue'), meta: { title: '后台概览', admin: true } },
    { path: '/admin/users', name: 'admin-users', component: () => import('../views/AdminUsersView.vue'), meta: { title: '用户管理', admin: true } },
    { path: '/admin/artists', name: 'admin-artists', component: () => import('../views/AdminArtistsView.vue'), meta: { title: '歌手管理', admin: true } },
    { path: '/admin/songs', name: 'admin-songs', component: () => import('../views/AdminSongsView.vue'), meta: { title: '歌曲管理', admin: true } },
    { path: '/admin/playlists', name: 'admin-playlists', component: () => import('../views/AdminPlaylistsView.vue'), meta: { title: '歌单管理', admin: true } },
    { path: '/admin/banners', name: 'admin-banners', component: () => import('../views/AdminBannersView.vue'), meta: { title: '轮播图管理', admin: true } },
    { path: '/admin/analytics', name: 'admin-analytics', component: () => import('../views/AdminAnalyticsView.vue'), meta: { title: '数据统计', admin: true } },
  ],
})

router.beforeEach((to) => {
  if (to.meta.admin && !getAdminAuth()) return { name: 'admin-login', query: { redirect: to.fullPath } }
  if (to.meta.user && !getUserAuth()) return { name: 'login', query: { redirect: to.fullPath } }
  if (to.name === 'login' && getUserAuth()) return { name: 'home' }
  return true
})

router.afterEach((to) => {
  document.title = `回声唱片 · ${String(to.meta.title ?? '音乐')}`
})

export default router
