<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';

import { deleteLecture, type Exercise, getExercises } from '../../../api';
import { t } from '../../../i18n';
import { addAssistantsRouteName, createNewLectureRouteName, editLectureRouteName } from '../../../router/names';
import LoadingSpinner from '../../general/LoadingSpinner.vue';

const lectures = ref<Exercise[]>([]);
const router = useRouter();

onMounted(async () => {
  await fetchLectures();
});

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
async function fetchLectures() {
  lectures.value = await getExercises();
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
function editLecture(lectureId: string) {
  // eslint-disable-next-line @typescript-eslint/no-floating-promises
  router.push({ name: editLectureRouteName, params: { lectureId } });
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
async function deleteLecturewithId(lectureId: string) {
  await deleteLecture(lectureId);
  await fetchLectures();
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
function addAssistants(lectureId: string) {
  // eslint-disable-next-line @typescript-eslint/no-floating-promises
  router.push({ name: addAssistantsRouteName, params: { lectureId } });
}
</script>

<template>
  <main>
    <h1 v-text="t('courses.manage-courses')" />
    <RouterLink :to="{ name: createNewLectureRouteName }" class="button">{{
      t('courses.create-new-lecture')
    }}</RouterLink>
    <template v-if="lectures.length">
      <div>
        <table class="table">
          <thead>
            <tr>
              <th v-text="t('courses.id')"></th>
              <th v-text="t('courses.lecture-title')"></th>
              <th v-text="t('courses.term')"></th>
              <th v-text="t('courses.actions')"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="lecture in lectures" :key="lecture.exerciseId">
              <td>{{ lecture.exerciseId }}</td>
              <td>{{ lecture.lecture }}</td>
              <td>{{ lecture.term.term + ' ' + lecture.term.year }}</td>
              <td>
                <button v-tooltip="'Edit'" class="btn btn-primary" @click="editLecture(lecture.exerciseId)">
                  <i class="fas fa-edit"></i>
                </button>
                <button v-tooltip="'Assistants'" class="btn btn-primary" @click="addAssistants(lecture.exerciseId)">
                  <i class="fas fa-users"></i>
                </button>
                <button v-tooltip="'Delete'" class="btn btn-primary" @click="deleteLecturewithId(lecture.exerciseId)">
                  <i class="fas fa-trash"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
    <template v-else>
      <div>
        <LoadingSpinner />
      </div>
    </template>
  </main>
</template>

<style>
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
</style>

<style>
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
</style>
