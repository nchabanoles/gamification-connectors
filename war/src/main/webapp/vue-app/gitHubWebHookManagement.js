import gitHubWebHookManagementApp from './components/gitHubWebHookManagementApp.vue';
import './../css/main.less';
Vue.use(Vuetify);

$(document).ready(() => {
    new Vue({
      render: (h) => h(gitHubWebHookManagementApp),
      vuetify,
    }).$mount('#gitHubWebHookManagementApp');

});
