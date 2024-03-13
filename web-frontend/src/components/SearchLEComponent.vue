<script setup>

import {reactive} from "vue";
import {isoDateStrToShortDateStr, isoDateTimeStrToShortDateStr} from "@/utils/convertors.js";
import axios from "axios";

const state = reactive({
  isLoading: false,
  searchForm: {
    name: '',
    edrpou: '',
    // address: '',

  },
  searchResults: null
})



const clearForm = () => {
  state.searchForm =   {
    name: '',
    edrpou: '',

  }
}

const search = () => {
  state.isLoading = true;
  state.searchResults = null

  axios.post('/api/search-red/le', state.searchForm)
      .then(resp => {
        console.log(resp)
        state.isLoading = false;
        state.searchResults = resp.data
      })
      .catch(err => {
        state.isLoading = false;
        console.log(err)
      })
}

</script>

<template>


  <div class="flex flex-col gap-2">
    <div class="flex flex-row gap-4 mt-4 flex-wrap">
      <input type="text" class="bg-white p-2 rounded-xl outline w-[550px] outline-gray-400" placeholder="Назва"
             v-model="state.searchForm.name">
      <input type="text" class="bg-white p-2 rounded-xl outline w-[250px] outline-gray-400" placeholder="Код ЄДРПОУ"
             v-model="state.searchForm.edrpou">

<!--      <input type="text" class="bg-white p-2 rounded-xl outline w-[650px] outline-gray-400" placeholder="Адреса"-->
<!--             v-model="state.searchForm.address">-->
    </div>


    <div class="flex flex-row my-5 justify-center gap-8">
      <button @click="clearForm"
              class="bg-red-400 py-2 w-[200px] text-white rounded-2xl hover:cursor-pointer hover:bg-red-500">Очистити
      </button>
      <button
          @click="search"
              class="bg-green-600 py-2 w-[200px] text-white rounded-2xl hover:cursor-pointer hover:bg-green-700">Пошук
      </button>
    </div>


    <div class="relative">

      <div class=" absolute top-0 left-0 w-full text-center" v-if="state.isLoading">
        <span class="text-2xl font-semibold text-gray-500">Завантаження...</span>
      </div>
    <div v-if="state.searchResults">
      <div v-if="state.searchResults.govua01List.length > 0">
        <span class=" text-2xl text-gray-400 font-black">Банкрутство:</span>
        <table>
          <thead>
          <th>Дата події</th>
          <th>Дата створення</th>
          <th>Дата закінчення</th>
          <th>Код</th>
          <th>Назва</th>
          <th>Тип</th>
          <th>Номер справи</th>
          <th>Назва суду</th>
          </thead>
          <tbody>
          <tr v-for="g1 in state.searchResults.govua01List" :key="g1.hash">
            <td>{{ isoDateStrToShortDateStr(g1.date) }}</td>
            <td> {{ isoDateTimeStrToShortDateStr(g1.createDate) }}</td>
            <td>{{ isoDateTimeStrToShortDateStr(g1.disableDate) }}</td>
            <td>{{ g1.firmEdrpou }}</td>
            <td>{{ g1.firmName }}</td>
            <td>{{ g1.type }}</td>
            <td>{{ g1.caseNumber }}</td>
            <td>{{ g1.courtName }}</td>
          </tr>
          </tbody>
        </table>
      </div>


      <div v-if="state.searchResults.govua07List.length > 0">
        <span class=" text-2xl text-gray-400 font-black">Реєстр боржників:</span>
        <table>
          <thead>
          <th>Дата створення</th>
          <th>Дата актуальності</th>
          <th>Дата закінчення</th>
          <th>Код</th>
          <th>Назва</th>
          <th>Дата народження</th>

          <th>Автор</th>
          <th>Категорія</th>

          <th>Номер справи</th>
          </thead>
          <tbody>
          <tr v-for="g7 in state.searchResults.govua07List" :key="g7.hash">
            <td> {{ isoDateTimeStrToShortDateStr(g7.createDate) }}</td>
            <td>{{ isoDateTimeStrToShortDateStr(g7.updateDate) }}</td>
            <td>{{ isoDateTimeStrToShortDateStr(g7.disableDate) }}</td>
            <td>{{ g7.debtorCode }}</td>
            <td>{{ g7.debtorName }}</td>
            <td>{{ g7.debtorBirthdate }}</td>
            <td>{{ g7.publisher }}</td>
            <td>{{ g7.vdCat }}</td>
            <td>{{ g7.vpOrdernum }}</td>
          </tr>
          </tbody>
        </table>
      </div>

    </div>

    </div>

  </div>




  <!--<div>-->

  <!--  <input type="text" class="bg-green-200" placeholder="lastname" v-model="state.searchForm.name">-->
  <!--  <input type="text" class="bg-green-200" placeholder="first" v-model="state.searchForm.edrpou">-->
  <!--  <button @click="search">Search</button>-->

  <!--&lt;!&ndash;  <div class="flex flex-row gap-2 flex-wrap justify-between">&ndash;&gt;-->
  <!--&lt;!&ndash;  <person-card-component :person="person" v-for="person in state.searchResults" :key="person.id"/>&ndash;&gt;-->
  <!--&lt;!&ndash;  </div>&ndash;&gt;-->

  <!--  <div class="flex flex-row gap-2 flex-wrap justify-between" v-if="useLegalEntityStore().legalEntities">-->


  <!--    <legal-entity-card-component :legal-entity="legalEntity" v-for="legalEntity in useLegalEntityStore().legalEntities.slice(0, 10)" :key="legalEntity.id"/>-->
  <!--&lt;!&ndash;    <person-card-component :person="person" v-for="person in usePrivatePersonStore().privatePersons" :key="person.id"/>&ndash;&gt;-->
  <!--  </div>-->

  <!--&lt;!&ndash;  {{usePrivatePersonStore().privatePersons}}&ndash;&gt;-->

  <!--&lt;!&ndash;  <div v-for="person in state.searchResults" :key="person.id">&ndash;&gt;-->
  <!--&lt;!&ndash;    {{person}}&ndash;&gt;-->
  <!--&lt;!&ndash;  </div>&ndash;&gt;-->



  <!--</div>-->
</template>

<style scoped>
table {
  border-collapse: collapse; /* Ensures that the border is collapsed into a single border */
  width: 100%; /* Optional: Sets the table width */
}

table, th, td {
  border: 1px solid black; /* Sets the border style for the table and table cells */
}

th, td {
  padding: 8px; /* Adds some padding inside table cells for better readability */
  text-align: left; /* Aligns text to the left inside table cells */
}
</style>