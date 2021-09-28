import Vue from "vue";
import VueRouter from 'vue-router';

// 捕获VueRouter路由重复触发导致的报错
const originalPush = VueRouter.prototype.push;
VueRouter.prototype.push = function push(location, onResolve, onReject) {
    if (onResolve || onReject) {
        return originalPush.call(this, location, onResolve, onReject);
    }
    return originalPush.call(this, location).catch(err => err);
};

Vue.use(VueRouter)

const routes = [
    {
        path: '/',
        name: 'Index',
        component: resolve => require(['./pages/Index'], resolve),
        meta: {
            // 页面标题title
            title: '首页',
        }
    },
    {
        path: '/login',
        name: 'Login',
        component: resolve => require(['./pages/Login'], resolve),
        meta: {
            title: '登录'
        }
    },
    {
        path: '/register',
        name: 'Register',
        component: resolve => require(['./pages/Register'], resolve),
        meta: {
            title: '注册'
        }
    },
    {
        path: '/forgetPsw',
        name: 'ForgetPassword',
        component: resolve => require(['./pages/ForgetPassword'], resolve),
        meta: {
            title: '忘记密码'
        }
    },
    {
        path: '/info',
        name: 'Info',
        component: resolve => require(['./pages/Info'], resolve),
        meta: {
            title: '个人信息',
        }
    },
    {
        path: '/indent',
        name: 'Indent',
        component: resolve => require(['./pages/Indent'], resolve),
        meta: {
            title: '我的订单'
        }
    },
    {
        path: '/search',
        name: 'Search',
        component: resolve => require(['./pages/Search'], resolve),
        meta: {
            title: '出行火车票查询'
        }
    },
    {
        path: '/tickets',
        name: 'Tickets',
        component: resolve => require(['./pages/Tickets'], resolve),
        meta: {
            title: '余票查询'
        }
    },
    {
        path: '/transferTickets',
        name: 'TransferTickets',
        component: resolve => require(['./pages/TransferTickets'], resolve),
        meta: {
            title: '转乘余票查询'
        }
    },
    {
        path: '/admin',
        name: 'AdminIndex',
        component: resolve => require(['./pages/Admin/Index'], resolve),
        redirect: "/admin/route",
        meta: {
            title: '后台管理界面',
        },
        children: [
            {
                path: 'route',
                name: 'TrainRoutes',
                component: resolve => require(['./components/Admin/Trains.vue'], resolve),
            },
            {
                path: 'user',
                name: 'AdminUsers',
                component: resolve => require(['./components/Admin/Users.vue'], resolve),
            },
            {
                path: 'route/:trainId',
                name: 'TrainRoute',
                component: resolve => require(['./pages/Admin/Trains'], resolve),
                meta: {
                    title: '列车管理'
                },
            },
        ]
    },
    {
        path: '/pay/success',
        name: 'PaySuccess',
        component: resolve => require(['./pages/Pay/Success'], resolve),
        meta: {
            title: '付款成功'
        },
    },
    {
        path: '/error/mobile',
        name: 'IsMobile',
        component: resolve => require(['./pages/Error/IsMobile'], resolve),
        meta: {
            title: '移动端'
        },
    },
]

const router = new VueRouter({
    routes
})

export default router