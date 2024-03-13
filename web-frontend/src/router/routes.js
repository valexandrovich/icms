import SearchView from "@/views/SearchView.vue";
import SchedulerView from "@/views/SchedulerView.vue";
import ProgresView from "@/views/ProgresView.vue";
import UploadView from "@/views/UploadView.vue";

export const routes = [
    {
        path: '/',
        name: 'search',
        component: SearchView,
        label: 'Пошук',
        icon: ['fas', 'magnifying-glass'],
        isSideMenu: true
    },
    {
        path: '/scheduler',
        name: 'scheduler',
        component: SchedulerView,
        label: 'Розклад',
        icon: ['fas', 'calendar'],
        isSideMenu: true
    },
    {
        path: '/upload',
        name: 'upload',
        component: UploadView,
        label: 'Завантаження',
        icon: ['fas', 'cloud-arrow-up'],
        isSideMenu: true
    },
    {
        path: '/progress',
        name: 'progress',
        component: ProgresView,
        label: 'Прогрес',
        icon: ['fas', 'bars-progress'],
        isSideMenu: true
    },

]