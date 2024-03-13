<script setup>

import {reactive} from "vue";
import axios from "axios";
import {isoDateStrToShortDateStr, isoDateTimeStrToShortDateStr} from "@/utils/convertors.js";

const state = reactive({
  isLoading: false,
  searchResults: null,
  searchForm: {
    params: {
      passportType: '1',
      // isFullName: false
    },
    lastName: '',
    firstName: '',
    patronymicName: '',
    birthday: '',
    // birthday: '1994-01-23',
    inn: '',
    localPassportSerial: '',
    localPassportNumber: '',
    idPassportNumber: '',
    intPassportSerial: '',
    intPassportNumber: '',
    // address: '',
    // phone: ''
  },
})


const convertDate = (dateStr) => {
  const parts = dateStr.split('.');
  if (parts.length !== 3 || !parts[0] || !parts[1] || !parts[2] || isNaN(Date.parse(`${parts[2]}-${parts[1]}-${parts[0]}`))) {
    return "";
  }
  const converted = `${parts[2]}-${parts[1]}-${parts[0]}`;
  return converted;
}


const clearForm = () => {
  state.searchForm = {
    params: {
      passportType: '1',
      isFullName: false
    },
    lastName: '',
    firstName: '',
    patronymicName: '',
    birthday: '',
    inn: '',
    localPassportSerial: '',
    localPassportNumber: '',
    idPassportRecordNumber: '',
    idPassportNumber: '',
    intPassportSerial: '',
    intPassportNumber: '',
    address: '',
    phone: ''
  }
}

const search = () => {
state.isLoading = true;
state.searchResults = null
  state.searchForm.birthday = convertDate(state.searchForm.birthday)

  axios.post('/api/search-red/pp', state.searchForm)
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
      <input type="text" class="bg-white p-2 rounded-xl outline w-[250px] outline-gray-400" placeholder="Прізвищє"
             v-model="state.searchForm.lastName">
      <input type="text" class="bg-white p-2 rounded-xl outline w-[250px] outline-gray-400" placeholder="Ім'я"
             v-model="state.searchForm.firstName">
      <input type="text" class="bg-white p-2 rounded-xl outline w-[250px] outline-gray-400" placeholder="По-батькові"
             v-model="state.searchForm.patronymicName">
      <input type="text" class="bg-white p-2 rounded-xl outline w-[200px] outline-gray-400"
             placeholder="Дата народження" v-model="state.searchForm.birthday">
      <input type="text" class="bg-white p-2 rounded-xl outline w-[150px] outline-gray-400" placeholder="РНОКПП (ІПН)"
             v-model="state.searchForm.inn">

      <select name="" id="" class="bg-white p-2 rounded-xl outline w-[290px] outline-gray-400"
              v-model="state.searchForm.params.passportType">
        <option value='1'>Паспорт книжка</option>
        <option value='2'>Паспорт ID картка</option>
        <option value='3'>Паспорт для виїзду за кородон</option>
      </select>


      <div v-if="state.searchForm.params.passportType === '1'" class=" flex flex-row gap-2">
        <input type="text" class="bg-white p-2 rounded-xl outline w-[80px] outline-gray-400" placeholder="Серія"
               v-model="state.searchForm.localPassportSerial">
        <input type="text" class="bg-white p-2 rounded-xl outline w-[150px] outline-gray-400" placeholder="Номер"
               v-model="state.searchForm.localPassportNumber">
      </div>


      <div v-if="state.searchForm.params.passportType === '2'" class=" flex flex-row gap-2">
        <input type="text" class="bg-white p-2 rounded-xl outline w-[120px] outline-gray-400" placeholder="Номер"
               v-model="state.searchForm.idPassportNumber">
        <input type="text" class="bg-white p-2 rounded-xl outline w-[200px] outline-gray-400"
               placeholder="Номер запису у реєстрі" v-model="state.searchForm.idPassportRecordNumber">
      </div>

      <div v-if="state.searchForm.params.passportType === '3'" class=" flex flex-row gap-2">
        <input type="text" class="bg-white p-2 rounded-xl outline w-[80px] outline-gray-400" placeholder="Серія"
               v-model="state.searchForm.intPassportSerial">
        <input type="text" class="bg-white p-2 rounded-xl outline w-[150px] outline-gray-400" placeholder="Номер"
               v-model="state.searchForm.intPassportNumber">
      </div>

      <!--      <input type="text" class="bg-white p-2 rounded-xl outline w-[300px] outline-gray-400" placeholder="Адреса"-->
      <!--             v-model="state.searchForm.address">-->
      <!--      <input type="text" class="bg-white p-2 rounded-xl outline w-[250px] outline-gray-400" placeholder="Телефон"-->
      <!--             v-model="state.searchForm.phone">-->


    </div>
    <div class="flex flex-row my-5 justify-center gap-8 ">
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


        <div v-if="state.searchResults.govua08List.length > 0">
          <span class=" text-2xl text-gray-400 font-black">Реєстр безвістно зниклих:</span>
          <table>
            <thead>
            <th>Дата створення</th>
            <th>Дата актуальності</th>
            <th>Дата закінчення</th>
            <th>Прізвищє (UA)</th>
            <th>Ім'я (UA)</th>
            <th>По-батькові (UA)</th>

            <th>Прізвищє (RU)</th>
            <th>Ім'я (RU)</th>
            <th>По-батькові (RU)</th>

            <th>Прізвищє (EN)</th>
            <th>Ім'я (EN)</th>
            <th>По-батькові (EN)</th>
            <th>Дата народження</th>
            <th>Дата події</th>
            <th>Місце події</th>
            <th>Опис справи</th>
            </thead>
            <tbody>
            <tr v-for="g8 in state.searchResults.govua08List" :key="g8.hash">
              <td> {{ isoDateTimeStrToShortDateStr(g8.createDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.updateDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.disableDate) }}</td>
              <td>{{ g8.lastNameUa }}</td>
              <td>{{ g8.firstNameUa }}</td>
              <td>{{ g8.patronymicNameUa }}</td>

              <td>{{ g8.lastNameRu }}</td>
              <td>{{ g8.firstNameRu }}</td>
              <td>{{ g8.patronymicNameRu }}</td>

              <td>{{ g8.lastNameEn }}</td>
              <td>{{ g8.firstNameEn }}</td>
              <td>{{ g8.patronymicNameEn }}</td>

              <td>{{ isoDateStrToShortDateStr(g8.birthday) }}</td>
              <td>{{ isoDateStrToShortDateStr(g8.lostDate) }}</td>
              <td>{{ g8.lostPlace }}</td>
              <td>{{ g8.articleCrim }}</td>
            </tr>
            </tbody>
          </table>
        </div>


        <div v-if="state.searchResults.govua09List.length > 0">
          <span class=" text-2xl text-gray-400 font-black">Реєстр розшуку:</span>
          <table>
            <thead>
            <th>Дата створення</th>
            <th>Дата актуальності</th>
            <th>Дата закінчення</th>
            <th>Прізвищє (UA)</th>
            <th>Ім'я (UA)</th>
            <th>По-батькові (UA)</th>

            <th>Прізвищє (RU)</th>
            <th>Ім'я (RU)</th>
            <th>По-батькові (RU)</th>

            <th>Прізвищє (EN)</th>
            <th>Ім'я (EN)</th>
            <th>По-батькові (EN)</th>
            <th>Дата народження</th>
            <th>Дата події</th>
            <th>Місце події</th>
            <th>Опис справи</th>
            </thead>
            <tbody>
            <tr v-for="g8 in state.searchResults.govua09List" :key="g8.hash">
              <td> {{ isoDateTimeStrToShortDateStr(g8.createDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.updateDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.disableDate) }}</td>
              <td>{{ g8.lastNameUa }}</td>
              <td>{{ g8.firstNameUa }}</td>
              <td>{{ g8.patronymicNameUa }}</td>

              <td>{{ g8.lastNameRu }}</td>
              <td>{{ g8.firstNameRu }}</td>
              <td>{{ g8.patronymicNameRu }}</td>

              <td>{{ g8.lastNameEn }}</td>
              <td>{{ g8.firstNameEn }}</td>
              <td>{{ g8.patronymicNameEn }}</td>

              <td>{{ isoDateStrToShortDateStr(g8.birthday) }}</td>
              <td>{{ isoDateStrToShortDateStr(g8.lostDate) }}</td>
              <td>{{ g8.lostPlace }}</td>
              <td>{{ g8.articleCrim }}</td>
            </tr>
            </tbody>
          </table>
        </div>


        <div v-if="state.searchResults.govua10List.length > 0">
          <span class=" text-2xl text-gray-400 font-black">Реєстр недійсних паспортів Громадянина України (Міграційна служба):</span>
          <table>
            <thead>
            <th>Дата створення</th>
            <th>Дата актуальності</th>
            <th>Дата закінчення</th>
            <th>Серія</th>
            <th>Номер</th>
            <th>Статус</th>
            </thead>
            <tbody>
            <tr v-for="g8 in state.searchResults.govua10List" :key="g8.hash">
              <td> {{ isoDateTimeStrToShortDateStr(g8.createDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.updateDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.disableDate) }}</td>
              <td>{{ g8.series }}</td>
              <td>{{ g8.number }}</td>
              <td>{{ g8.status }}</td>
            </tr>
            </tbody>
          </table>
        </div>


        <div v-if="state.searchResults.govua11List.length > 0">
          <span class=" text-2xl text-gray-400 font-black">Реєстр недійсних паспортів Громадянина України для виїзду за кордон (Міграційна служба):</span>
          <table>
            <thead>
            <th>Дата створення</th>
            <th>Дата актуальності</th>
            <th>Дата закінчення</th>
            <th>Серія</th>
            <th>Номер</th>
            <th>Статус</th>
            </thead>
            <tbody>
            <tr v-for="g8 in state.searchResults.govua11List" :key="g8.hash">
              <td> {{ isoDateTimeStrToShortDateStr(g8.createDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.updateDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.disableDate) }}</td>
              <td>{{ g8.series }}</td>
              <td>{{ g8.number }}</td>
              <td>{{ g8.status }}</td>
            </tr>
            </tbody>
          </table>
        </div>


        <div v-if="state.searchResults.govua12List.length > 0">
          <span class=" text-2xl text-gray-400 font-black">Реєстр недійсних паспортів Громадянина України (МВC):</span>
          <table>
            <thead>
            <th>Дата створення</th>
            <th>Дата актуальності</th>
            <th>Дата закінчення</th>
            <th>Серія</th>
            <th>Номер</th>
            <th>Статус</th>
            </thead>
            <tbody>
            <tr v-for="g8 in state.searchResults.govua12List" :key="g8.hash">
              <td> {{ isoDateTimeStrToShortDateStr(g8.createDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.updateDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.disableDate) }}</td>
              <td>{{ g8.series }}</td>
              <td>{{ g8.number }}</td>
              <td>{{ g8.status }}</td>
            </tr>
            </tbody>
          </table>
        </div>

        <div v-if="state.searchResults.govua13List.length > 0">
          <span class=" text-2xl text-gray-400 font-black">Реєстр недійсних паспортів Громадянина України для виїзду за кордон (МВC):</span>
          <table>
            <thead>
            <th>Дата створення</th>
            <th>Дата актуальності</th>
            <th>Дата закінчення</th>
            <th>Серія</th>
            <th>Номер</th>
            <th>Статус</th>
            </thead>
            <tbody>
            <tr v-for="g8 in state.searchResults.govua13List" :key="g8.hash">
              <td> {{ isoDateTimeStrToShortDateStr(g8.createDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.updateDate) }}</td>
              <td>{{ isoDateTimeStrToShortDateStr(g8.disableDate) }}</td>
              <td>{{ g8.series }}</td>
              <td>{{ g8.number }}</td>
              <td>{{ g8.status }}</td>
            </tr>
            </tbody>
          </table>
        </div>


      </div>

    </div>

  </div>


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