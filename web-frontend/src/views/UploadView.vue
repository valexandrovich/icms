<script setup>

import {ref} from "vue";
import axios from "axios";

const url = ref("");

const send = () => {
  axios.get(url)
      .then(resp => {
        console.log(resp)
      })
      .catch(err => {
        console.log(err)
      })
}

</script>

<template>
<div>
  <input type="text" v-model="url" class="outline-gray-500 outline m-6 p-2 rounded-xl" >
  <br>
  <span class="text-gray-500">{{url}}</span>
  <br>
  <button @click="send" class="bg-green-500 text-white px-6 py-2 rounded-xl">Send</button>
</div>
</template>

<style scoped>

</style>