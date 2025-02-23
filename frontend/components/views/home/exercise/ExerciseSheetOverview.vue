<script setup lang="ts">
import { type ComponentPublicInstance, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';

import { getExerciseSheetDetailsWithAssignments } from '../../../../api';
import { t } from '../../../../i18n';
import { exerciseRouteName } from '../../../../router/names';
import { accountData } from '../../../../store/account';

const taskDetails = ref<any[]>([]);
const route = useRoute();
const fileInputs = reactive<Record<number, HTMLInputElement | null>>({});
const selectedFiles = reactive<Record<number, File | null>>({});
const exerciseId = ref<string>('');
const sheetId = ref<string>('');

onMounted(async () => {
  exerciseId.value = route.params.exerciseId as string;
  sheetId.value = route.params.sheetId as string;
  await getTasks();
});

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
function setFileInputRef(el: Element | ComponentPublicInstance | null, index: number) {
  // Ensure el is an HTMLInputElement before assigning it
  if (el instanceof HTMLInputElement) {
    fileInputs[index] = el;
  } else {
    fileInputs[index] = null;
  }
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
async function getTasks() {
  console.log();
  await getExerciseSheetDetailsWithAssignments(exerciseId.value, sheetId.value).then((sheetResult: any) => {
    if (!(sheetResult.assignments == null || sheetResult.assignments == undefined)) {
      for (const assignment of sheetResult.assignments) {
        taskDetails.value.push(assignment);
      }
    }
  });
  console.log(taskDetails.value, 'taskdetails');
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
function selectFile(index: number) {
  const fileInput = fileInputs[index];
  if (fileInput) {
    fileInput.click();
  } else {
    console.error('fileInput is null for index', index);
  }
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
function handleFileChange(event: Event, index: number) {
  const target = event.target as HTMLInputElement;
  const file = target.files ? target.files[0] : null;
  if (file) {
    selectedFiles[index] = file;
  }
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
function deleteFile(index: number) {
  selectedFiles[index] = null;
  if (fileInputs[index]) {
    fileInputs[index]!.value = '';
  }
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
function handleSubmissions() {
  alert('Submitted Successfully!');
}
</script>

<template>
  <main class="home">
    <div id="exerciseIdElement" style="visibility: hidden">{{ $route.params.exerciseId }}</div>
    <div id="sheetIdIdElement" style="visibility: hidden">{{ $route.params.sheetId }}</div>
    <h2>
      {{ t('exercise.lecture') }} {{ $route.params.exerciseId }}, {{ t('exercise.sheet') }} {{ $route.params.sheetId }}
    </h2>

    <div>
      <RouterLink :to="{ name: exerciseRouteName }">{{ t('exercise.backToExercise') }}</RouterLink>
    </div>

    <div v-if="accountData?.isAdmin || accountData?.isAssistant">
      <input
        id="sheetFilesDownloadBtnId"
        class="button btn-beauty1"
        style="width: 15%"
        :value="t('exercise.sheetFilesDownload')"
      />
    </div>
    <div v-if="!accountData?.isAdmin && !accountData?.isAssistant && taskDetails">
      <table id="taskTable" class="table">
        <thead>
          <tr>
            <th>{{ t('exercise.task-id') }}</th>
            <th>{{ t('exercise.files') }}</th>
            <th>{{ t('exercise.maximum-score') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(task, index) in taskDetails" :key="task.taskName">
            <td>{{ task.assignmentId }}</td>
            <td>
              <div v-if="!selectedFiles[index]">
                <div class="upload-box" @click="() => selectFile(index)">
                  <p>Upload File</p>
                  <input
                    :ref="(el) => setFileInputRef(el, index)"
                    type="file"
                    style="display: none"
                    @change="(event) => handleFileChange(event, index)"
                  />
                </div>
              </div>
              <div v-else>
                <p>Selected File: {{ selectedFiles[index]?.name }}</p>
                <button @click="deleteFile(index)">Delete File</button>
              </div>
            </td>
            <td>{{ task.maxPoint }}</td>
          </tr>
        </tbody>
      </table>
      <div>
        <button class="button btn-beauty1" @click="handleSubmissions">{{ t('exercise.groupSubmitNew') }}</button>
      </div>
    </div>
  </main>
</template>

<style scoped>
@import '../../../../styles/new/btn-beauty1.css';
@import '../../../../styles/new/btn-beauty2.css';

.table {
  width: 100%;
  border-collapse: collapse;
  margin: 10px;
}
.table th,
.table td {
  padding: 10px;
  border: 1px solid #ddd;
  text-align: center;
}
.btn {
  margin: 5px;
  border-radius: 100%;
  padding-left: 15px;
  padding-right: 15px;
  padding-top: 10px;
  padding-bottom: 10px;
}

.upload-box {
  border: 2px dashed #3013ee;
  padding: 20px;
  text-align: center;
  border-radius: 5px;
  cursor: pointer;
  transition:
    background-color 0.3s,
    opacity 0.3s;
}

.upload-box:hover {
  background-color: #f0f0f0;
  opacity: 0.7;
}

.upload-box p {
  margin: 0;
  color: #aaa;
}
</style>
