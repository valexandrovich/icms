<script setup>
import * as XLSX from 'xlsx';
import {computed, onMounted, reactive, ref, watch} from "vue";
import UploadPPFileComponent from "@/components/UploadPPFileComponent.vue";
import UploadLEFileComponent from "@/components/UploadLEFileComponent.vue";
import {excelDateToDate} from "@/utils/convertors.js";

const state = reactive({
  fileData: [],
  searchQuery: '',
  showOnlyErrors: false
})

const pagination = reactive({
  currentPage: 1,
  rowsPerPage: 40,
});

const totalPages = computed(() => Math.ceil(filteredData.value.length / pagination.rowsPerPage));

const isValidName = (value) => {
  // Return true if value is null or an empty string
  if (value === null || value === '') {
    return true;
  }

  // Continue with regex validation for non-null and non-empty strings
  return /^[a-zA-Zа-яА-ЯіІїЇєЄ'-]+$/.test(value);
};
const isValidDate = (value) => value === null || /^(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[012])\.\d{4}$/.test(value);

// watch(() => state.jsonData, validateData, { deep: true, immediate: true });


const validateRow = (row) => {

  // console.log('Validating row ')
  // console.log(row)

  row.errors = {}


  const namesFields = ['last_name_ua', 'first_name_ua', 'patronymic_name_ua', 'last_name_ru']

  namesFields.forEach(fieldName => {
    if (!isValidName(row[fieldName])) {
      row.errors[fieldName] = 'Invalid characters';
    }
  });

}


const filteredData = computed(() => {

  let data = state.fileData.filter((row) => {
    if (state.searchQuery.trim() === '') {
      return true; // If no search query, don't exclude any rows here
    }
    // Return true if any value in the row includes the search query
    return Object.values(row).some((value) =>
        String(value).toLowerCase().includes(state.searchQuery.toLowerCase())
    );
  });

  // Then, if showErrorsOnly is true, filter out rows without errors
  // console.log(state.showOnlyErrors)
  if (state.showOnlyErrors) {

    // console.log("FIND ONLY ERRORS")

    data = data.filter(row => row.errors && Object.keys(row.errors).length > 0);
  }

  return data;


  // if (state.searchQuery.trim() === '') {
  //   return state.fileData;
  // }
  // return state.fileData.filter((row) => {
  //   return Object.values(row).some((value) =>
  //       String(value).toLowerCase().includes(state.searchQuery.toLowerCase())
  //   );
  // });
});

const paginatedData = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.rowsPerPage;
  const end = start + pagination.rowsPerPage;
  return filteredData.value.slice(start, end);
  // return state.jsonData.slice(start, end);
});

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    pagination.currentPage = page;
  }
};


const generateUUID = () => {
  let dt = new Date().getTime();
  const uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (dt + Math.random() * 16) % 16 | 0;
    dt = Math.floor(dt / 16);
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
  return uuid;
};

const handleFileUpload = (event) => {

  const startTime = performance.now();

  const file = event.target.files[0];
  const reader = new FileReader();
  reader.onload = (e) => {
    const data = e.target.result;
    const workbook = XLSX.read(data, {type: 'binary'});
    const sheetName = workbook.SheetNames[0];
    const worksheet = workbook.Sheets[sheetName];
    let json = XLSX.utils.sheet_to_json(worksheet);

    json = json.map(row => {
      if (row.birthday && !isNaN(row.birthday)) {
        const jsDate = excelDateToDate(row.birthday);
        row.birthday = formatDate(jsDate);
      }

      if (row.local_pass_issue_date && !isNaN(row.local_pass_issue_date)) {
        const jsDate = excelDateToDate(row.local_pass_issue_date);
        row.local_pass_issue_date = formatDate(jsDate);
      }


      if (row.id_pass_issue_date && !isNaN(row.id_pass_issue_date)) {
        const jsDate = excelDateToDate(row.id_pass_issue_date);
        row.id_pass_issue_date = formatDate(jsDate);
      }

      if (row.int_pass_issue_date && !isNaN(row.int_pass_issue_date)) {
        const jsDate = excelDateToDate(row.int_pass_issue_date);
        row.int_pass_issue_date = formatDate(jsDate);
      }


      if (row.mk_event_date && !isNaN(row.mk_event_date)) {
        const jsDate = excelDateToDate(row.mk_event_date);
        row.mk_event_date = formatDate(jsDate);
      }

      if (row.mk_start_date && !isNaN(row.mk_start_date)) {
        const jsDate = excelDateToDate(row.mk_start_date);
        row.mk_start_date = formatDate(jsDate);
      }

      if (row.mk_end_date && !isNaN(row.mk_end_date)) {
        const jsDate = excelDateToDate(row.mk_end_date);
        row.mk_end_date = formatDate(jsDate);
      }

      row.uuid = generateUUID();

      validateRow(row)

      return row;
    });


    state.fileData = json;

    const endTime = performance.now();

    // Calculate elapsed time in milliseconds
    const elapsedTimeMs = endTime - startTime;

    // Convert milliseconds to mm:ss:ms
    const minutes = Math.floor(elapsedTimeMs / 60000);
    const seconds = Math.floor((elapsedTimeMs % 60000) / 1000);
    const milliseconds = elapsedTimeMs % 1000;

    // Format mm:ss:ms
    const formattedTime = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}:${milliseconds.toString().padStart(3, '0')}`;

    // Log formatted elapsed time
    console.log(`Elapsed time: ${formattedTime}`);

  };
  reader.readAsBinaryString(file);
}


const formatDate = (date) => {
  let day = date.getDate();
  let month = date.getMonth() + 1; // Months start at 0
  const year = date.getFullYear();

  day = day < 10 ? '0' + day : day;
  month = month < 10 ? '0' + month : month;

  return `${day}.${month}.${year}`;
}


const fileInput = ref(null);

const activeTab = ref(1)

const testData = [
  {id: 1, name: "Valex"},
  {id: 2, name: "Petya"},
  {id: 3, name: "Sasha"}
]

</script>

<template>


  <div class="flex flex-col">
    <span class="text-3xl font-black uppercase  text-gray-300  mb-4">  <font-awesome-icon
        :icon="['fas', 'cloud-arrow-up']" class="mr-2 fa-fw"/> Завантаження</span>

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
      <upload-p-p-file-component/>


    </div>
    <div class="flex flex-row " v-if="activeTab === 2">
      <upload-l-e-file-component/>
    </div>


  </div>


</template>

<style scoped>


.dropzone {
  @apply bg-white cursor-pointer w-full hover:bg-gray-200 border-4 border-dashed rounded-2xl p-24 text-center text-gray-400 font-black text-2xl ;
}


</style>