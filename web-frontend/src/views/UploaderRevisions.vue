<script setup>


import {onMounted, reactive} from "vue";
import axios from "axios";

const state = reactive({
  revisionsPp: []
})

onMounted(()=>{
  axios.get('/api/upload/pp/revisions')
      .then(resp => state.revisionsPp = resp.data)
      .catch(err => console.log(err))
})

const initRevision = (revisionId) => {
  axios.post('/api/upload/pp/init/' + revisionId)
      .then(resp => console.log(resp))
      .catch(err => console.log(err))
}

</script>

<template>
  <div class="flex flex-col">
    <h1>Завантаження по фізичним особам </h1>

    <table>
      <thead>
      <th>ID</th>
      <th>Дата</th>
      <th>Автор</th>
      <th>Назва</th>
      <th>Дії</th>
      </thead>
      <tbody>
      <tr v-for="revision in state.revisionsPp" :key="revision.id">
        <td>{{revision.id}}</td>
        <td>{{revision.createDate}}</td>
        <td>{{revision.initiatorName}}</td>
        <td>{{revision.revisionName}}</td>
        <td><button class="bg-green-700 py-2 px-6 text-white rounded-xl hover:bg-green-500" @click="initRevision(revision.id)">Завантажити</button></td>

      </tr>
      </tbody>
    </table>



  </div>




</template>

<style scoped>


</style>