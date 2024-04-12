<script setup>

import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {computed, onMounted, reactive, ref} from "vue";
import * as XLSX from "xlsx";
import {dateToShortDateStr, excelDateToDate} from "@/utils/convertors.js";
import {randomUUID} from "@/utils/common.js";
import {
  isValidCountryCode,
  isValidDate,
  isValidEnName, isValidIdPassNumber, isValidIdPassRecordNumber, isValidInn, isValidIntPassSerial, isValidIssuerCode,
  isValidLocalIntPassNumber,
  isValidLocalPassSerial, isValidPhone,
  isValidRuName, isValidSex,
  isValidUaName, isValidVarchar255
} from "@/utils/validators.js";
import axios from "axios";
// import {toast} from "vue3-toastify";

const fileInput = ref(null);

const state = reactive({

  data: [],
  tagTypes: [],
  searchQuery: '',
  revisionName: '',
  showOnlyErrors: false,
  isLoading: false,
  pagination: {
    currentPage: 1,
    rowsPerPage: 20,
  },
  manualForm: {
    uuid: null,
    last_name_ua:'БЄЛОЄНКО',
    first_name_ua:'ВАЛЕРІЙ',
    patronymic_name_ua:'ОЛЕКСАНДРОВИЧ',
    last_name_ru:'БЕЛОЕНКО',
    first_name_ru:'ВАЛЕРИЙ',
    patronymic_name_ru:'АЛЕКСАНДРОВИЧ',

    last_name_en:'BIELOIENKO',
    first_name_en:'VALERII',
    patronymic_name_en:'OLEKSANDROVICH',

    birthday:'23.01.1994',
    inn:'3435603818',
    local_pass_serial:'ВК',
    local_pass_number:'702215',


  }

})


const handleFileUpload = (event) => {
  state.isLoading = true
  const file = event.target.files[0];
  const reader = new FileReader();
  reader.onload = (e) => {
    const data = e.target.result;
    const workbook = XLSX.read(data, {type: 'binary'});
    const sheetName = workbook.SheetNames[0];
    const worksheet = workbook.Sheets[sheetName];
    let json = XLSX.utils.sheet_to_json(worksheet, {range: 3});

    json = json.map(row => {
      if (row.birthday && !isNaN(row.birthday)) {
        const jsDate = excelDateToDate(row.birthday);
        row.birthday = dateToShortDateStr(jsDate)
        row.birthday = dateToShortDateStr(jsDate);
      }

      if (row.local_pass_issue_date && !isNaN(row.local_pass_issue_date)) {
        const jsDate = excelDateToDate(row.local_pass_issue_date);
        row.local_pass_issue_date = dateToShortDateStr(jsDate);
      }


      if (row.id_pass_issue_date && !isNaN(row.id_pass_issue_date)) {
        const jsDate = excelDateToDate(row.id_pass_issue_date);
        row.id_pass_issue_date = dateToShortDateStr(jsDate);
      }

      if (row.int_pass_issue_date && !isNaN(row.int_pass_issue_date)) {
        const jsDate = excelDateToDate(row.int_pass_issue_date);
        row.int_pass_issue_date = dateToShortDateStr(jsDate);
      }


      if (row.mk_event_date && !isNaN(row.mk_event_date)) {
        const jsDate = excelDateToDate(row.mk_event_date);
        row.mk_event_date = dateToShortDateStr(jsDate);
      }

      if (row.mk_start_date && !isNaN(row.mk_start_date)) {
        const jsDate = excelDateToDate(row.mk_start_date);
        row.mk_start_date = dateToShortDateStr(jsDate);
      }

      if (row.mk_end_date && !isNaN(row.mk_end_date)) {
        const jsDate = excelDateToDate(row.mk_end_date);
        row.mk_end_date = dateToShortDateStr(jsDate);
      }
      row.uuid = randomUUID();
      validateRow(row)
      return row;
    });
    state.data = json;
    state.isLoading = false
  };
  reader.readAsBinaryString(file);

  // console.log(event)
}

const fetchTagTypes = () => {
  // axios.get('/api/tags/all')
  axios.get('http://localhost:9000/tags/all')
      .then(
          resp => {
            state.tagTypes = resp.data
            state.tagTypes.push({
              "id": "1",
              "code": undefined ,
              "description": "БЕЗ ТЕГУ -"
            })
          }
      )
      .catch(err => console.log(err))
}

onMounted(() => {
  fetchTagTypes();

  state.manualForm.uuid = randomUUID();


})


const dataToXlsx = () => {
  const sheetName = 'Sheet1'
  const workbook = XLSX.utils.book_new();
  const worksheet = XLSX.utils.json_to_sheet(state.data);
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName || "Sheet1");

  // Generate XLSX file and write to a binary string
  const wbout = XLSX.write(workbook, {bookType:'xlsx', type: 'binary'});

  // Convert binary string to Blob
  const blob = new Blob([s2ab(wbout)], {type: 'application/octet-stream'});

  // Return the Blob for uploading
  return blob;
}

const s2ab = (s) => {
  const buffer = new ArrayBuffer(s.length);
  const view = new Uint8Array(buffer);
  for (let i = 0; i < s.length; i++) {
    view[i] = s.charCodeAt(i) & 0xFF;
  }
  return buffer;
}


const validateRow = (row) => {

  row = normalizeRow(row)

  row.errors = {}

  const namesUaFields = ['last_name_ua', 'first_name_ua', 'patronymic_name_ua']
  const namesRuFields = ['last_name_ru', 'first_name_ru', 'patronymic_name_ru']
  const namesEnFields = ['last_name_en', 'first_name_en', 'patronymic_name_en']

  const varchar255Fields = ['local_pass_issuer_name', 'birthplace', 'address_simple',
    'address_country', 'address_region', 'address_county', 'address_city_type', 'address_city',
    'address_street_type', 'address_street', 'address_building_number', 'address_building_letter',
    'address_building_part', 'address_apartment']

  const dateFields = ['birthday', 'local_pass_issue_date', 'id_pass_issue_date', 'int_pass_issue_date', 'mk_event_date', 'mk_start_date', 'mk_end_date']

  namesUaFields.forEach(fieldName => {
    if (!isValidUaName(row[fieldName])) {
      row.errors[fieldName] = 'Поле повинно містити лише великі українські літери, апостроф або дефіс. Максимум 64 символи.';
    }
  });

  namesRuFields.forEach(fieldName => {
    if (!isValidRuName(row[fieldName])) {
      row.errors[fieldName] = 'Поле повинно містити лише великі російські літери, апостроф або дефіс. Максимум 64 символи.';
    }
  });

  namesEnFields.forEach(fieldName => {
    if (!isValidEnName(row[fieldName])) {
      row.errors[fieldName] = 'Поле повинно містити лише великі англійські літери, апостроф або дефіс. Максимум 64 символи.';
    }
  });

  dateFields.forEach(fieldName => {
    if (!isValidDate(row[fieldName])) {
      row.errors[fieldName] = 'Дата повинна бути у форматі дд.мм.рррр';
    }
  });

  varchar255Fields.forEach(fieldName => {
    if (!isValidVarchar255(row[fieldName])) {
      row.errors[fieldName] = 'Поле повинно містити меньше 255 символів';
    }
  });

  if (!isValidLocalPassSerial(row['local_pass_serial'])) {
    row.errors['local_pass_serial'] = 'Серія паспорта повинна містити 2 великі кирилистичні літери'
  }

  if (!isValidLocalIntPassNumber(row['local_pass_number'])) {
    row.errors['local_pass_number'] = 'Номер паспорту повинен містити 6 цифр'
  }

  if (!isValidLocalIntPassNumber(row['int_pass_number'])) {
    row.errors['int_pass_number'] = 'Номер паспорту повинен містити 6 цифр'
  }

  if (!isValidInn(row['inn'])) {
    row.errors['inn'] = 'РНОКПП (ІПН) повинен містити 10 цифр'
  }

  if (!isValidCountryCode(row['citizenship'])) {
    row.errors['citizenship'] = 'Має містити двозначний код країни згідно з ISO 3166-2'
  }

  if (!isValidIdPassNumber(row['id_pass_number'])) {
    row.errors['id_pass_number'] = 'Має містити 9 цифр'
  }


  if (!isValidIdPassRecordNumber(row['id_pass_record'])) {
    row.errors['id_pass_record'] = 'Має містити 8 та 5 цифр з дефісом поміж ними ХХХХХХХХ-YYYYY'
  }

  if (!isValidIssuerCode(row['id_pass_issuer_code'])) {
    row.errors['id_pass_issuer_code'] = 'Має містити 4 цифри'
  }

  if (!isValidIssuerCode(row['int_pass_issuer_code'])) {
    row.errors['int_pass_issuer_code'] = 'Має містити 4 цифри'
  }

  if (!isValidIntPassSerial(row['int_pass_serial'])) {
    row.errors['int_pass_serial'] = 'Має містити 2 великі латинські літери'
  }

  if (!isValidSex(row['sex'])) {
    row.errors['sex'] = 'Має бути F - для жіночої статі та M - для чоловічої'
  }

  if (!isValidPhone(row['phone'])) {
    row.errors['phone'] = 'Повинен бути у міжнародному форматі'
  }

  if (!isValidMkCode(row['mk_code'])) {
    row.errors['mk_code'] = 'Немає тегу з кодом "' + row['mk_code'] + '". Виберіть з випадаючого списка!'
  }


}

const isValidMkCode = (value) => {
  if (value === null || value === '' || value === undefined) {
    return true;
  }
  return state.tagTypes.some(tag => tag.code === value);
}

const normalizeRow = (row) => {
  Object.keys(row).forEach(key => {
    if (typeof row[key] === 'number') {
      row[key] = row[key]
          .toString()
          .toUpperCase()
          .trim()
          .replace(/\s+/g, ' ');
    }
    if (typeof row[key] === 'string') {
      row[key] = row[key]
          .toUpperCase()
          .trim()
          .replace(/\s+/g, ' ');
    } else if (typeof row[key] === 'object' && row[key] !== null && !Array.isArray(row[key])) {
      normalizeRow(row[key]);
    }
  });
  return row;
}

const filteredData = computed(() => {

  let data = state.data.filter((row) => {
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

const totalPages = computed(() => Math.ceil(filteredData.value.length / state.pagination.rowsPerPage));

const totalErrors = computed(() => {
  return state.data.reduce((acc, obj) => {
    const errors = obj.errors ? Object.keys(obj.errors).length : 0;
    return acc + errors;
  }, 0);
});

const paginatedData = computed(() => {
  const start = (state.pagination.currentPage - 1) * state.pagination.rowsPerPage;
  const end = start + state.pagination.rowsPerPage;
  return filteredData.value.slice(start, end);
  // return state.jsonData.slice(start, end);
});

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    state.pagination.currentPage = page;
  }
};


const  generateCsv = () => {
  if (state.data.length === 0) return '';

  // const columns = Object.keys(state.data[0]);
  // const csvRows = state.data.map(row =>
  //     columns.map(fieldName => JSON.stringify(row[fieldName], replacer)).join('|')
  // );


  const columns = Object.keys(state.data[0]).filter(key => key !== 'errors' && key !== 'uuid');

  const csvRows = state.data.map(row =>
      columns.map(fieldName => JSON.stringify(row[fieldName], replacer)).join('|')
  );

  csvRows.unshift(columns.join('|')); // Add header row
  return csvRows.join('\n');

  function replacer(key, value) {
    return value === null ? '' : value;
  }
}

const sendData = () => {
  state.isLoading = true

  // const csvData = generateCsv();
  // const blob = new Blob([csvData], { type: 'text/csv' });
  // // const sheetName = 'file'
  // // const blob = dataToXlsx();
  // const formData = new FormData();
  // formData.append("file", blob,  "test.csv");
  // formData.append("rowsCount", state.data.length);
  // formData.append("initiatorName", 'valex');
  //
  // axios.post('/api/upload/pp', formData)
  //     .then(resp => {
  //       console.log(resp)
  //       state.isLoading = false
  //     })
  //     .catch(err => {
  //       console.log(err)
  //       state.isLoading = false
  //     })

  // state.isLoading = true
  //
  const data = {
    initiatorName: 'valex',
    revisionName: state.revisionName,
    data: state.data,

  }

  console.log(data)

  axios.post('/api/upload/pp', data)
      .then((resp) => {
        console.log(resp)
        state.isLoading = false
      })
      .catch(err => {
        console.log(err)
        state.isLoading = false
      })
}


const sendManualData = () => {
  state.isLoading = true

  const data = JSON.parse(JSON.stringify(state.manualForm));

  // const data = {
  //   initiatorName: 'valex',
  //   revisionName: state.manualForm.uuid,
  //   data: [ state.manualForm],
  //
  // }
  // console.log(data)

  console.log(data)
  axios.post('/api/upload/manual-pp', data)
      .then((resp) => {
        console.log(resp)
        state.isLoading = false
      })
      .catch(err => {
        console.log(err)
        state.isLoading = false
      })
}

const cleanManualForm = () => {
  for (const key in state.manualForm) {
    if (state.manualForm.hasOwnProperty(key)) {
      if (key === 'uuid') {
        state.manualForm[key] = randomUUID();
      } else {
        state.manualForm[key] = '';
      }
    }
  }
}


</script>

<template>


  <!--  <div v-show="state.isLoading" class="  bg-amber-500  z-10 justify-center flex flex-col text-center">-->
  <!--    <div class="flex flex-row justify-center align-bottom ">-->
  <!--    <svg class="animate-spin -ml-1 mr-3 h-14 w-14 text-green-500" xmlns="http://www.w3.org/2000/svg" fill="none"-->
  <!--         viewBox="0 0 24 24">-->
  <!--      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>-->
  <!--      <path class="opacity-75" fill="currentColor"-->
  <!--            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>-->
  <!--    </svg>-->
  <!--    <span class="text-4xl font-black text-gray-400 ">Завантаження</span>-->
  <!--    </div>-->
  <!--  </div>-->


  <!--  <div v-if="state.isLoading" class="flex flex-col   w-full ">-->
  <!--    <div class="flex flex-row  justify-center h-screen items-center">-->
  <!--      <svg class="animate-spin -ml-1 mr-3 h-14 w-14 text-green-500" xmlns="http://www.w3.org/2000/svg" fill="none"-->
  <!--           viewBox="0 0 24 24">-->
  <!--        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>-->
  <!--        <path class="opacity-75" fill="currentColor"-->
  <!--              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>-->
  <!--      </svg>-->
  <!--      <span class="text-gray-300 font-black text-4xl uppercase">завантаження</span>-->
  <!--    </div>-->
  <!--  </div>-->
  <div  class="flex flex-col  w-full ">

    <div v-show="state.isLoading" class="absolute w-screen h-screen top-0 left-0   bg-white bg-opacity-75  z-10 justify-center flex flex-col text-center">
      <div class="flex flex-row justify-center  ">

        <div class="flex flex-col justify-center items-center gap-4">
          <svg class="animate-spin -ml-1 mr-3 h-14 w-14 text-green-700" xmlns="http://www.w3.org/2000/svg" fill="none"
               viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span class="text-4xl font-black text-gray-400 ">Обробка</span>
        </div>
      </div>
    </div>

    <div v-if="state.data.length === 0" class="flex flex-row justify-end py-2">
      <a href="/files/Дані про фізичних осіб.xlsx" download
         class="text-green-600 font-semibold text-lg  hover:text-green-500">
        <font-awesome-icon icon="fa-file-excel" class="mr-2 fa-fw"/>
        Завантажити шаблон файлу</a>
    </div>


    <div v-if="state.data.length === 0" class="flex flex-row ">
      <div id="dropzone" class="dropzone" @click="fileInput.click()">
                        <span>
                         <font-awesome-icon :icon="['fas', 'cloud-arrow-up']" class="mr-2 fa-fw"/>
                         Натисніть для вибору файла
                        </span>

      </div>
      <input type="file" ref="fileInput" @change="handleFileUpload" multiple style="display: none;" accept=".xlsx"/>
    </div>


    <!--    <div v-show="state.data.length===0">-->
    <!--        або додайте вручну-->


    <!--      <div class="flex flex-col outline outline-1">-->

    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="uuid" disabled v-model="state.manualForm.uuid">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="lastNameUa" v-model="state.manualForm.last_name_ua">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="firstNameUa" v-model="state.manualForm.first_name_ua">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="patronymicNameUa" v-model="state.manualForm.patronymic_name_ua">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="lastNameRu" v-model="state.manualForm.last_name_ru">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="firstNameRu" v-model="state.manualForm.first_name_ru">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="patronymicNameRu" v-model="state.manualForm.patronymic_name_ru">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="lastNameEn" v-model="state.manualForm.last_name_en">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="firstNameEn" v-model="state.manualForm.first_name_en">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="patronymicNameEn" v-model="state.manualForm.patronymic_name_en">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="birthday" v-model="state.manualForm.birthday">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="inn" v-model="state.manualForm.inn">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="localPassSerial" v-model="state.manualForm.local_pass_serial">-->
    <!--        <input type="text" class="m-2 outline-1 outline w-64" placeholder="localPassNumber" v-model="state.manualForm.local_pass_number">-->


    <!--        <button class="bg-green-500" @click="sendManualData">Send</button>-->

    <!--        <button class="bg-red-400" @click="cleanManualForm"> Clean</button>-->

    <!--      </div>-->




    <!--    </div>-->


    <div v-show="state.data.length>0" class="flex flex-row justify-between py-4  ">

      <div class="flex items-center ">
        <button class="bg-red-500 text-white py-2 px-6 rounded-xl" @click="state.data = []">Скинути дані</button>
      </div>

      <div class=" flex items-center   text-lg" :class="totalErrors>0 ? 'text-red-400' : 'text-green-600'">
        <span>Всього помилок: {{ totalErrors }}</span>
      </div>

      <div class=" flex items-center  text-gray-500 text-lg" >
        <label for="isOnlyErrorsCheckbox" class="mr-2">
          <span> Показувати лише записи з помилками </span>
        </label>
        <input id="isOnlyErrorsCheckbox" class="scale-125" style=" accent-color: green;" type="checkbox" :disabled="totalErrors===0"
               v-model="state.showOnlyErrors">
      </div>
      <div class=" flex items-center  text-gray-500 text-lg" >
        <input
            type="text"
            v-model="state.revisionName"

            class="mb-4 p-2 border border-2 min-w-[300px] border-gray-300 rounded-xl"
            placeholder="Назва файлу"/>
      </div>

      <!--        <input type="file" @change="handleFileUpload"/>-->
      <div>
        <!--        <span class="mr-2">Пошук в даних</span>-->
        <input

            type="text"
            v-model="state.searchQuery"
            placeholder="Пошук в даних"
            class="mb-4 p-2 border border-2 min-w-[300px] border-gray-300 rounded-xl"
        />
      </div>
      <div>
        <button
            class="bg-green-600 text-white px-12 py-2 rounded-xl disabled:bg-gray-300 disabled:cursor-not-allowed hover:bg-green-500"
            :disabled="totalErrors > 0" @click="sendData()">Завантажити
        </button>
      </div>


    </div>


    <div v-if="totalPages>1" class="flex flex-row justify-between my-2  items-center text-gray-500 font-semibold">
      <div class="pagination-buttons-group">
        <button @click="changePage(1)">Початок</button>
        <button @click="changePage(state.pagination.currentPage - 1)"><</button>
        <span class="px-4">сторінка {{ state.pagination.currentPage }} з {{ totalPages }}</span>
        <button @click="changePage(state.pagination.currentPage + 1)">></button>
        <button @click="changePage(totalPages)">Кінець</button>
      </div>
      <div class="flex flex-row">
        <span class="mr-4">Записів на сторінку</span>
        <select name="" id="" class="scale-125 px-4" v-model="state.pagination.rowsPerPage">
          <option value=10>10</option>
          <option value=20>20</option>
          <option value=30>30</option>
          <option value=40>40</option>
        </select>
      </div>
    </div>


    <div v-if="state.data.length>0" class="flex flex-row w-full pb-4 overflow-x-scroll">


      <table>
        <thead class="text-left">
        <tr class="bg-gray-300">
          <!--      <th rowspan="2">Номер</th>-->
          <th colspan="3" class="text-center">Iм'я Українською</th>
          <th colspan="3" class="text-center">Iм'я Російською</th>
          <th colspan="3" class="text-center">Iм'я Англійською</th>
          <th rowspan="2" class="text-center"><span class="text-nowrap">Дата народження</span></th>
          <th rowspan="2" class="text-center"><span class="text-nowrap">РНОКПП (ІПН)</span></th>
          <th colspan="4" class="text-center ">Паспорт громадянина України (книжка)</th>
          <th colspan="4" class="text-center">Паспорт громадянина України (ID картка)</th>
          <th colspan="4" class="text-center">Паспорт громадянина України (Для виїзду за кордон)</th>
          <th rowspan="2">Громадянство</th>
          <th rowspan="2"><span class="text-nowrap">Місце народження</span></th>
          <th rowspan="2">Стать</th>
          <th rowspan="2">Телефон</th>
          <th rowspan="2"><span class="text-nowrap">E-mail</span></th>
          <th rowspan="2">Адреса</th>
          <th colspan="11" class="text-center">Детальна адреса</th>
          <th colspan="7" class="text-center">Тег</th>
          <th rowspan="2"><span class="text-nowrap">Назва джерела</span></th>
        </tr>
        <tr class="bg-gray-200">
          <th class=" min-w-[150px]">Прізвищє</th>
          <th class=" min-w-[150px]">Ім'я</th>
          <th class=" min-w-[150px]"><span class="text-nowrap">По-батькові</span></th>

          <th class=" min-w-[150px]">Прізвищє</th>
          <th class=" min-w-[150px]">Ім'я</th>
          <th class=" min-w-[150px]"><span class="text-nowrap">По-батькові</span></th>

          <th class=" min-w-[150px]">Прізвищє</th>
          <th class=" min-w-[150px]">Ім'я</th>
          <th class=" min-w-[150px]"><span class="text-nowrap">По-батькові</span></th>

          <th class=" min-w-[150px] text-center">Серія</th>
          <th class=" min-w-[150px] text-center">Номер</th>
          <th class=" min-w-[150px] text-center"><span class="text-nowrap">Дата видачі</span></th>
          <th class=" min-w-[150px]"><span class="text-nowrap">Орган видачі</span></th>

          <th class=" min-w-[150px]">Номер</th>
          <th class=" min-w-[150px]"><span class="text-nowrap">Номер запису в реєстрі</span></th>
          <th class=" min-w-[150px]"><span class="text-nowrap">Дата видачі</span></th>
          <th class=" min-w-[150px]"><span class="text-nowrap">Код органа видачі</span></th>

          <th class=" min-w-[150px]">Серія</th>
          <th class=" min-w-[150px]">Номер</th>
          <th class=" min-w-[150px]"><span class="text-nowrap">Дата видачі</span></th>
          <th class=" min-w-[150px]"><span class="text-nowrap">Код органа видачі</span></th>

          <th class=" min-w-[150px]">Країна</th>
          <th class=" min-w-[150px]"><span class="text-nowrap">Регіон \ Область</span></th>
          <th class=" min-w-[150px]">Район</th>
          <th class=" min-w-[150px]"><span class="text-nowrap">Тип НП</span></th>
          <th class=" min-w-[150px]"><span class="text-nowrap">Назва НП</span></th>
          <th class=" min-w-[150px]"><span class="text-nowrap">Тип вулиці</span></th>
          <th><span class="text-nowrap">Назва вулиці</span></th>
          <th><span class="text-nowrap">Номер будівлі</span></th>
          <th><span class="text-nowrap">Номер будівлі 2</span></th>
          <th><span class="text-nowrap">Літера будівлі</span></th>
          <th><span class="text-nowrap">Номер квартири</span></th>
          <th>Код</th>
          <th><span class="text-nowrap">Дата події</span></th>
          <th><span class="text-nowrap">Дата початку дії</span></th>
          <th><span class="text-nowrap">Дата закінчення дії</span></th>
          <th><span class="text-nowrap">Текстове значення</span></th>
          <th><span class="text-nowrap">Числове значення</span></th>
          <th>Коментар</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(row) in paginatedData" :key="row.uuid" class="align-baseline">
          <!--      <td> {{index + 1}}</td>-->
          <td>
            <input type="text" :class="{'non-validate-cell' : row.errors.last_name_ua }"
                   @blur="validateRow(row)" v-model="row.last_name_ua"/>
            <span v-if="row.errors['last_name_ua']" class="text-red-600 text-sm pb-12">{{
                row.errors.last_name_ua
              }}</span>
          </td>


          <td>
            <input type="text" :class="{'non-validate-cell' : row.errors.first_name_ua }"
                   @blur="validateRow(row)" v-model="row.first_name_ua"/>
            <span v-if="row.errors['first_name_ua']" class="text-red-600 text-sm pb-12">{{
                row.errors.first_name_ua
              }}</span>
          </td>

          <td><input type="text" :class="{'non-validate-cell' : row.errors.patronymic_name_ua }"
                     @blur="validateRow(row)" v-model="row.patronymic_name_ua"/>
            <span v-if="row.errors['patronymic_name_ua']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.patronymic_name_ua }}</span>
          </td>

          <td><input type="text" :class="{'non-validate-cell' : row.errors.last_name_ru }"
                     @blur="validateRow(row)" v-model="row.last_name_ru"/>
            <span v-if="row.errors['last_name_ru']" class="text-red-600 text-sm pb-12">{{
                row.errors.last_name_ru
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.first_name_ru }"
                     @blur="validateRow(row)" v-model="row.first_name_ru"/>
            <span v-if="row.errors['first_name_ru']" class="text-red-600 text-sm pb-12">{{
                row.errors.first_name_ru
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.patronymic_name_ru }"
                     @blur="validateRow(row)" v-model="row.patronymic_name_ru"/>
            <span v-if="row.errors['patronymic_name_ru']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.patronymic_name_ru }}</span>
          </td>

          <td><input type="text" :class="{'non-validate-cell' : row.errors.last_name_en }"
                     @blur="validateRow(row)" v-model="row.last_name_en"/>
            <span v-if="row.errors['last_name_en']" class="text-red-600 text-sm pb-12">{{
                row.errors.last_name_en
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.first_name_en }"
                     @blur="validateRow(row)" v-model="row.first_name_en"/>
            <span v-if="row.errors['first_name_en']" class="text-red-600 text-sm pb-12">{{
                row.errors.first_name_en
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.patronymic_name_en }"
                     @blur="validateRow(row)" v-model="row.patronymic_name_en"/>
            <span v-if="row.errors['patronymic_name_en']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.patronymic_name_en }}</span>
          </td>


          <td><input type="text" class="text-center" :class="{'non-validate-cell' : row.errors.birthday }"
                     @blur="validateRow(row)" v-model="row.birthday"/>
            <span v-if="row.errors['birthday']" class="text-red-600 text-sm pb-12">{{ row.errors.birthday }}</span>
          </td>
          <td><input type="text" class="text-center" :class="{'non-validate-cell' : row.errors.inn }"
                     @blur="validateRow(row)" v-model="row.inn"/>
            <span v-if="row.errors['inn']" class="text-red-600 text-sm pb-12">{{ row.errors.inn }}</span>
          </td>


          <td><input type="text" class="text-center"
                     :class="{'non-validate-cell' : row.errors.local_pass_serial }"
                     @blur="validateRow(row)" v-model="row.local_pass_serial"/>
            <span v-if="row.errors['local_pass_serial']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.local_pass_serial }}</span>
          </td>


          <td><input type="text" class="text-center" :class="{'non-validate-cell' : row.errors.local_pass_number }"
                     @blur="validateRow(row)" v-model="row.local_pass_number"/>
            <span v-if="row.errors['local_pass_number']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.local_pass_number }}</span>
          </td>
          <td><input type="text" class="text-center"
                     :class="{'non-validate-cell' : row.errors.local_pass_issue_date }"
                     @blur="validateRow(row)" v-model="row.local_pass_issue_date"/>
            <span v-if="row.errors['local_pass_issue_date']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.local_pass_issue_date }}</span>
          </td>
          <td><input type="text" class=" min-w-[500px]"
                     :class="{'non-validate-cell' : row.errors.local_pass_issuer_name }"
                     @blur="validateRow(row)" v-model="row.local_pass_issuer_name"/>
            <span v-if="row.errors['local_pass_issuer_name']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.local_pass_issuer_name }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.id_pass_number }"
                     @blur="validateRow(row)" v-model="row.id_pass_number"/>
            <span v-if="row.errors['id_pass_number']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.id_pass_number }}</span>

          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.id_pass_record }"
                     @blur="validateRow(row)" v-model="row.id_pass_record"/>
            <span v-if="row.errors['id_pass_record']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.id_pass_record }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.id_pass_issue_date }"
                     @blur="validateRow(row)" v-model="row.id_pass_issue_date"/>
            <span v-if="row.errors['id_pass_issue_date']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.id_pass_issue_date }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.id_pass_issuer_code }"
                     @blur="validateRow(row)" v-model="row.id_pass_issuer_code"/>

            <span v-if="row.errors['id_pass_issuer_code']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.id_pass_issuer_code }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.int_pass_serial }"
                     @blur="validateRow(row)" v-model="row.int_pass_serial"/>
            <span v-if="row.errors['int_pass_serial']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.int_pass_serial }}</span>
          </td>


          <td><input type="text" :class="{'non-validate-cell' : row.errors.int_pass_number }"
                     @blur="validateRow(row)" v-model="row.int_pass_number"/>
            <span v-if="row.errors['int_pass_number']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.int_pass_number }}</span>
          </td>

          <td><input type="text" :class="{'non-validate-cell' : row.errors.int_pass_issue_date }"
                     @blur="validateRow(row)" v-model="row.int_pass_issue_date"/>
            <span v-if="row.errors['int_pass_issue_date']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.int_pass_issue_date }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.int_pass_issuer_code }"
                     @blur="validateRow(row)" v-model="row.int_pass_issuer_code"/>
            <span v-if="row.errors['int_pass_issuer_code']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.int_pass_issuer_code }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.citizenship }"
                     @blur="validateRow(row)" v-model="row.citizenship"/>
            <span v-if="row.errors['citizenship']" class="text-red-600 text-sm pb-12">{{
                row.errors.citizenship
              }}</span>
          </td>

          <td><input type="text" :class="{'non-validate-cell' : row.errors.birthplace }"
                     @blur="validateRow(row)" v-model="row.birthplace"/>
            <span v-if="row.errors['birthplace']" class="text-red-600 text-sm pb-12">{{
                row.errors.birthplace
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.sex }"
                     @blur="validateRow(row)" v-model="row.sex"/>
            <span v-if="row.errors['sex']" class="text-red-600 text-sm pb-12">{{
                row.errors.sex
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.phone }"
                     @blur="validateRow(row)" v-model="row.phone"/>
            <span v-if="row.errors['phone']" class="text-red-600 text-sm pb-12">{{
                row.errors.phone
              }}</span>
          </td>
          <td><input type="text" v-model="row.email"/></td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_simple }"
                     @blur="validateRow(row)" v-model="row.address_simple"/>
            <span v-if="row.errors['address_simple']" class="text-red-600 text-sm pb-12">{{
                row.errors.address_simple
              }}</span>
          </td>


          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_country }"
                     @blur="validateRow(row)" v-model="row.address_country"/>
            <span v-if="row.errors['address_country']" class="text-red-600 text-sm pb-12">{{
                row.errors.address_country
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_region }"
                     @blur="validateRow(row)" v-model="row.address_region"/>
            <span v-if="row.errors['address_region']" class="text-red-600 text-sm pb-12">{{
                row.errors.address_region
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_county }"
                     @blur="validateRow(row)" v-model="row.address_county"/>
            <span v-if="row.errors['address_county']" class="text-red-600 text-sm pb-12">{{
                row.errors.address_county
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_city_type }"
                     @blur="validateRow(row)" v-model="row.address_city_type"/>
            <span v-if="row.errors['address_city_type']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.address_city_type }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_city }"
                     @blur="validateRow(row)" v-model="row.address_city"/>
            <span v-if="row.errors['address_city']" class="text-red-600 text-sm pb-12">{{
                row.errors.address_city
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_street_type }"
                     @blur="validateRow(row)" v-model="row.address_street_type"/>
            <span v-if="row.errors['address_street_type']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.address_street_type }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_street }"
                     @blur="validateRow(row)" v-model="row.address_street"/>
            <span v-if="row.errors['address_street']" class="text-red-600 text-sm pb-12">{{
                row.errors.address_street
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_building_number }"
                     @blur="validateRow(row)" v-model="row.address_building_number"/>
            <span v-if="row.errors['address_building_number']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.address_building_number }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_building_letter }"
                     @blur="validateRow(row)" v-model="row.address_building_letter"/>
            <span v-if="row.errors['address_building_letter']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.address_building_letter }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_building_part }"
                     @blur="validateRow(row)" v-model="row.address_building_part"/>
            <span v-if="row.errors['address_building_part']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.address_building_part }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.address_apartment }"
                     @blur="validateRow(row)" v-model="row.address_apartment"/>
            <span v-if="row.errors['address_apartment']"
                  class="text-red-600 text-sm pb-12">{{ row.errors.address_apartment }}</span>
          </td>


          <td>
            <select name="" id="" v-model="row.mk_code"  @change="validateRow(row)">
              <option :value="tag.code" v-for="tag in state.tagTypes" :key="tag.code"> {{ tag.code }} -
                {{ tag.description }}
              </option>
            </select>

            <!--            <input type="text" :class="{'non-validate-cell' : row.errors.mk_code }"-->
            <!--                     @blur="validateRow(row)"  v-model="row.mk_code"/>-->
            <span v-if="row.errors['mk_code']" class="text-red-600 text-sm pb-12">{{ row.errors.mk_code }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.mk_event_date }"
                     @blur="validateRow(row)" v-model="row.mk_event_date"/>
            <span v-if="row.errors['mk_event_date']" class="text-red-600 text-sm pb-12">{{
                row.errors.mk_event_date
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.mk_start_date }"
                     @blur="validateRow(row)" v-model="row.mk_start_date"/>
            <span v-if="row.errors['mk_start_date']" class="text-red-600 text-sm pb-12">{{
                row.errors.mk_start_date
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.mk_end_date }"
                     @blur="validateRow(row)" v-model="row.mk_end_date"/>
            <span v-if="row.errors['mk_end_date']" class="text-red-600 text-sm pb-12">{{
                row.errors.mk_end_date
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.mk_text_value }"
                     @blur="validateRow(row)" v-model="row.mk_text_value"/>
            <span v-if="row.errors['mk_text_value']" class="text-red-600 text-sm pb-12">{{
                row.errors.mk_text_value
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.mk_number_value }"
                     @blur="validateRow(row)" v-model="row.mk_number_value"/>
            <span v-if="row.errors['mk_number_value']" class="text-red-600 text-sm pb-12">{{
                row.errors.mk_number_value
              }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.mk_comment }"
                     @blur="validateRow(row)" v-model="row.mk_comment"/>
            <span v-if="row.errors['mk_comment']" class="text-red-600 text-sm pb-12">{{ row.errors.mk_comment }}</span>
          </td>
          <td><input type="text" :class="{'non-validate-cell' : row.errors.source }"
                     @blur="validateRow(row)" v-model="row.source"/>
            <span v-if="row.errors['source']" class="text-red-600 text-sm pb-12">{{ row.errors.source }}</span>
          </td>


        </tr>
        </tbody>

      </table>


    </div>

  </div>
</template>

<style scoped>

table {
  border-collapse: collapse;
}

th, td {
  @apply px-2  border border-gray-600;
}


.non-validate-cell {
  @apply bg-red-300 ;
}

.pagination-buttons-group > button {
  @apply bg-green-600 py-1 px-6 text-white font-semibold m-1 rounded-xl;
}

.dropzone {
  @apply bg-white cursor-pointer w-full hover:bg-gray-200 border-4 border-dashed rounded-2xl p-24 text-center text-gray-400 font-black text-2xl ;
}
</style>