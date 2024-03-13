<script setup>

import ProgressTableComponent from "@/components/ProgressTableComponent.vue";
</script>

<template>
  <div class="flex flex-col p-4">
    <div class="flex">
      <span class="text-3xl font-black uppercase  text-gray-300  mb-4">
        <font-awesome-icon :icon="['fas', 'bars-progress']" class="mr-2 fa-fw"/>
        Прогресс</span>
    </div>
    <div class="flex flex-col">
      <progress-table-component/>
    </div>
  </div>

</template>

<style scoped>

</style>