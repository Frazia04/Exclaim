<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue';

import { deleteExerciseSheet, getExercise, getExerciseSheet } from '../../../../api/exercise';
import { t } from '../../../../i18n';
import {
  createExerciseSheetRouteName,
  exerciseGroupManagmentRouteName,
  exerciseGroupsRouteName,
  exerciseResultRouteName,
  exerciseSheetEditRouteName,
  exerciseSheetOverviewRouteName,
} from '../../../../router/names';

var assistant: boolean = false;
var tutor: boolean = false;

const renderComponentSheetTable = ref(true);
const items: any[] = [],
  columns = ['ID', 'Lable', 'Points'];

onMounted(() => {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  return loadFirstDataOfExercise(exerciseId);
});

async function loadFirstDataOfExercise(exerciseId: string): Promise<void> {
  await getExercise(exerciseId)
    .catch(() => console.log('fail'))
    .then((exerciseResult) => {
      if (exerciseResult?.roles != null && exerciseResult?.roles != undefined) {
        if (exerciseResult?.roles.assistant != null && exerciseResult?.roles.assistant != undefined) {
          assistant = exerciseResult?.roles.assistant;
        }

        // if (exerciseResult?.roles.student!=null && exerciseResult?.roles.student!=undefined){
        //   student = true;
        // }

        if (
          exerciseResult?.roles.tutorGroups != null &&
          exerciseResult?.roles.tutorGroups != undefined &&
          exerciseResult?.roles.tutorGroups.length != 0
        ) {
          tutor = true;
        }
      }
    });
  await getExerciseSheet(exerciseId)
    .catch(() => console.log('fail'))
    .then((sheetsresult) => {
      if (!(sheetsresult == null || sheetsresult == undefined)) {
        for (const sheetElement of sheetsresult) {
          items.push({
            ID: sheetElement.sheetId,
            Lable: sheetElement.label,
            Points: sheetElement.achievedPoints + '/' + sheetElement.maxPointsTotal,
          });
        }
      }
    });
  renderComponentSheetTable.value = false;
  await nextTick();
  renderComponentSheetTable.value = true;
}

async function deleteExerciseSheetById(sheetId: string): Promise<void> {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  await deleteExerciseSheet(exerciseId, sheetId)
    .catch(() => console.log('fail'))
    .then((resultDeleteSheet) => {
      console.log(resultDeleteSheet);
      if (resultDeleteSheet != null && resultDeleteSheet.status == 'SUCCESS') {
        location.reload();
      } else {
        alert(t('exercise.failProcess'));
      }
    });
}

// const isModalDeleteSheetVisible = ref(false);
// function ShowDeleteSheetPopup() {
//     isModalDeleteSheetVisible.value = true;
// };
//  function closeDeleteSheetPopup() {
//   isModalDeleteSheetVisible.value = false;
// };
</script>

<template>
  <main class="home">
    <div id="exerciseIdElement" style="visibility: hidden">{{ $route.params.exerciseId }}</div>

    <h2 v-text="t('exercise.lecture') + ' ' + $route.params.exerciseId" />
    <h2 v-text="t('exercise.exercise')" />

    <table class="buttons">
      <tr>
        <td>
          <RouterLink :to="{ name: exerciseGroupsRouteName }" class="button btn-beauty1"
            >{{ t('exercise.groups') }}
          </RouterLink>
        </td>
        <td>
          <RouterLink v-if="assistant || tutor" :to="{ name: exerciseResultRouteName }" class="button btn-beauty1"
            >{{ t('exercise.grade-overview') }}
          </RouterLink>
        </td>
        <td>
          <RouterLink v-if="assistant" :to="{ name: exerciseGroupManagmentRouteName }" class="button btn-beauty1"
            >{{ t('exercise.group-management') }}
          </RouterLink>
        </td>
        <!-- <td>
            <a v-if="assistant" :href="'exercise/' + $route.params.exerciseId + '/exams'" class="button btn-beauty1">{{
                t('exercise.exam')
              }}
            </a>
          </td> -->
      </tr>
    </table>

    <div class="vue-nice-table table-responsive">
      <table id="exerciseSheetsTable" class="styled-table">
        <thead>
          <tr>
            <th v-for="(column, index) in columns" :key="index">{{ column }}</th>
          </tr>
        </thead>
        <tbody v-if="renderComponentSheetTable">
          <tr v-for="(item, index) in items" :key="index">
            <td v-for="(column, indexColumn) in columns" :key="indexColumn">{{ item[column] }}</td>

            <td>
              <RouterLink
                :to="{ name: exerciseSheetOverviewRouteName, params: { sheetId: item.ID } }"
                class="button btn-beauty1"
                >{{ t('exercise.submission') }}
              </RouterLink>
              <a
                v-if="assistant || tutor"
                :href="'exercise/' + $route.params.exerciseId + '/sheet/' + item.ID + '/assessment'"
                class="button btn-beauty1"
                >{{ t('exercise.enter-points') }}
              </a>
              <a
                v-if="assistant || tutor"
                :href="'exercise/' + $route.params.exerciseId + '/sheet/' + item.ID + '/attendance'"
                class="button btn-beauty1"
                >{{ t('exercise.attendance') }}
              </a>
              <RouterLink
                v-if="assistant"
                :to="{ name: exerciseSheetEditRouteName, params: { sheetId: item.ID } }"
                class="button btn-beauty1"
                style="color: orange"
              >
                {{ t('exercise.edit') }}
              </RouterLink>

              <button
                v-if="assistant"
                class="button btn-beauty1"
                style="color: red"
                @click="deleteExerciseSheetById(item.ID)"
              >
                {{ t('exercise.delete') }}
              </button>
              <!-- <modal v-show="isModalDeleteSheetVisible" @close="closeDeleteSheetPopup"/> -->
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <br />
    <div>
      <RouterLink v-if="assistant" :to="{ name: createExerciseSheetRouteName }" class="button btn-beauty1">
        {{ t('exercise.newExerciseSheet') }}
      </RouterLink>
    </div>
  </main>
</template>

<style scoped>
@import '../../../../styles/new/btn-beauty1.css';
@import '../../../../styles/new/table-exercise.css';
</style>
