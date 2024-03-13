<script setup>

import {ref} from "vue";
import SearchPPComponent from "@/components/SearchPPComponent.vue";
import SearchLEComponent from "@/components/SearchLEComponent.vue";
import ProgressTableComponent from "@/components/ProgressTableComponent.vue";

const activeTab = ref(1)
</script>

<template>

  <div class="flex flex-col p-4">
    <div class="flex">
      <span class="text-3xl font-black uppercase  text-gray-300  mb-4">
        <font-awesome-icon :icon="['fas', 'magnifying-glass']" class="mr-2 fa-fw"/>
        Пошук</span>
    </div>
    <div class="flex flex-col">

      <div class="flex flex-row  justify-center">
        <div class="rounded-2xl overflow-hidden">
          <button
              @click="activeTab = 1"
              class="tab-btn"
              :class="{ 'tab-btn-active': activeTab === 1 }">
            Фізичні особи
          </button>
          <button
              @click="activeTab = 2"
              class="tab-btn"
              :class="{ 'tab-btn-active': activeTab === 2 }">
            Юридичні особи
          </button>
        </div>
      </div>
      <div class="flex flex-row " v-if="activeTab === 1">
        <search-p-p-component/>


      </div>
      <div class="flex flex-row " v-if="activeTab === 2">
        <search-l-e-component/>
      </div>


    </div>
  </div>




<!--  <div class="flex flex-col">-->
<!--    <span class="text-3xl font-black uppercase  text-gray-300  mb-4">  <font-awesome-icon-->
<!--        :icon="['fas', 'cloud-arrow-up']" class="mr-2 fa-fw"/> </span>-->

<!--    -->


<!--  </div>-->
</template>

<style scoped>

</style>