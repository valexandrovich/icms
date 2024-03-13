<script setup>




import SideMenu from "@/components/SideMenu.vue";
</script>

<template>
  <div class="flex h-screen">
    <SideMenu/>
    <main class="flex-1 overflow-y-auto">
      <RouterView/>
    </main>
  </div>
</template>

<style scoped>


</style>
