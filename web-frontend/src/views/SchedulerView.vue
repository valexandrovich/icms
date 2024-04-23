<script setup>


import {onMounted, reactive} from "vue";
import axios from "axios";
import ProgressTableComponent from "@/components/ProgressTableComponent.vue";

const state = reactive({
  storedJobs: [],
  tmpUrl: ''
})

onMounted(() => {
  // axios.get('/api/scheduler/stored-jobs')
  axios.get('http://23.88.42.241:9000/scheduler/stored-jobs')
  // axios.get('http://localhost:9000/scheduler/stored-jobs')
      .then(resp => {
        state.storedJobs = resp.data
      })
      .catch(err => {
        console.log(err)
      })
})

const initStoredJob = (storedJobId) => {
  console.log(typeof storedJobId)

  const storedJobRequest = {
    storedJobId: storedJobId,
    initiatorName: 'valex'
  }
  axios.post('/api/scheduler/init', storedJobRequest);
}

const tst = () => {
  axios.get(state.tmpUrl)
      .then(resp => {
        console.log(resp)
      })
      .catch(err => {
        console.log(err)
      })
}

</script>

<template>


  <div class="flex flex-col p-4">
    <div class="flex">
      <span class="text-3xl font-black uppercase  text-gray-300  mb-4">
        <font-awesome-icon :icon="['fas', 'calendar']" class="mr-2 fa-fw"/>
        РОЗКЛАД</span>
    </div>


    <input type="text" v-model="state.tmpUrl">
    <button @click="tst">Test</button>

    <div class="flex flex-col">
      <div class="flex flex-col">
        <div
            class="flex flex-row bg-gradient-to-b from-green-500 to-green-700  text-green-50 text-lg font-bold   rounded-t-xl py-1 ">

          <div class="flex flex-col w-10p text-center">ID</div>
          <div class="flex flex-col w-25p">Назва</div>
          <div class="flex flex-col w-55p">Опис</div>
          <div class="flex flex-col w-10p text-center">Дія</div>
        </div>
        <div class="flex flex-row  py-2  gap-2 text-sm  text-gray-600 font-bold last:rounded-b-xl"
             v-for="(job, index) in state.storedJobs" :key="job.id"
             :class="index % 2 == 0 ? 'bg-gray-50': 'bg-gray-100' ">

          <div class="flex flex-col w-10p text-center">{{ job.id }}</div>
          <div class="flex flex-col w-25p">{{ job.name }}</div>
          <div class="flex flex-col w-55p">{{ job.description }}</div>
          <div class="flex flex-col w-10p px-2  min-w-[120px]">
            <button @click="initStoredJob(job.id)"
                    class="whitespace-nowrap bg-green-700 text-white px-6 py-1 rounded-xl hover:bg-green-500 ">
              <font-awesome-icon :icon="['fas', 'play']" class="mr-2 fa-fw"/>
              Старт
            </button>
          </div>

          <!--        <span>{{job.id}} - {{job.name}} - {{job.description}}</span>-->
          <!--        <button @click="initStoredJob(job.id)">Start</button>-->
        </div>

      </div>
    </div>
  </div>



</template>

<style scoped>

</style>