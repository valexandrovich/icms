<script setup>

import {routes} from "@/router/routes.js";
</script>

<template>
  <aside class="w-64 h-screen flex flex-col bg-gradient-to-b  from-green-500 to-green-900 text-white">
    <div class="flex-1 overflow-y-auto     rounded ">
      <div class="text-center p-4">

        <span class="text-5xl font-montserrat font-semibold">
          <font-awesome-icon :icon="['fas', 'shield-halved']"/>
          ICMS
        </span>
      </div>
      <div class="text-center">
        <span class="text-sm ">інформаційна система моніторингу та перевірок</span>
      </div>

      <nav class="py-6">
        <ul class="flex flex-col gap-2">
          <router-link v-for="route in routes" :key="route.name" :to="route.path" v-show="route.isSideMenu === true"
                       class="ml-6 px-4 py-2 text-lg font-semibold rounded-l-xl hover:bg-green-100  hover:text-green-700 transition-all duration-100">
            <!--              <font-awesome-icon :icon="route.icon" class="mr-2 fa-fw"/>-->
            <font-awesome-icon :icon="route.icon" class="mr-2 fa-fw"/>
            {{ route.label }}
          </router-link>
        </ul>
      </nav>
    </div>


    <div class="mt-auto py-4 px-3  rounded dark:bg-gray-800">
      <div class="flex items-center justify-center text-gray-200">
        <div class="flex-shrink-0">
          <!-- Optional: Account icon or image -->
          <!--          <font-awesome-icon icon="fa-solid fa-house" />-->
          <font-awesome-icon :icon="['fas', 'user']"/>
        </div>
        <div class="ml-3">
          <div class="text-xl font-medium  ">BieloienkoV</div>
          <!-- Optional: Additional account info -->
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.router-link-active {
  @apply bg-gradient-to-r from-green-100 to-white text-green-700 shadow-xl;
}


.router-link-active:hover {
  @apply bg-white text-green-700 !important;
}
</style>