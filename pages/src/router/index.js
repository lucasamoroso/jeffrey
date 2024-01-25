import { createRouter, createWebHashHistory } from 'vue-router';
import AppLayout from '@/layout/AppLayout.vue';

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            component: AppLayout,
            children: [
                {
                    path: '/',
                    name: 'recordings',
                    component: () => import('@/views/Recordings.vue')
                },
                {
                    path: '/profiles',
                    name: 'profiles',
                    component: () => import('@/views/Profiles.vue')
                },
                {
                    path: '/flamegraph/general',
                    component: () => import('@/views/flamegraph/General.vue'),
                    children: [
                        {
                            path: '/flamegraph/general',
                            component: () => import('@/views/flamegraph/general/EventTypes.vue')
                        },
                        {
                            path: '/flamegraph/general/sql',
                            component: () => import('@/views/flamegraph/general/Recordings.vue')
                        }
                    ]
                },
                {
                    path: '/flamegraph/show',
                    name: 'flamegraph-show',
                    component: () => import('@/views/flamegraph/Show.vue')
                },
                {
                    path: '/flamegraph/selectable',
                    name: 'flamegraph-selectable',
                    component: () => import('@/views/flamegraph/Selectable.vue')
                },
                {
                    path: '/flamegraph/differential',
                    name: 'flamegraph-differential',
                    component: () => import('@/views/flamegraph/Differential.vue')
                }
            ]
        }
    ]
});

export default router;
