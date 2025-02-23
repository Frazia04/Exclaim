<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import type { ExerciseSheetAssignments } from '../../../../api';
import {
  deleteExerciseSheetAssignemnt,
  editExerciseSheet,
  getExerciseSheetDetailsWithAssignments,
} from '../../../../api/exercise';
import { t } from '../../../../i18n';
import {
  exerciseRouteName,
  exercisesheetAssignmentsCreateRouteName,
  exercisesheetAssignmentsEditRouteName,
} from '../../../../router/names';

const router = useRouter();

const renderComponentTasks = ref(true);
const taskDetails: ExerciseSheetAssignments[] = [];

onMounted(() => {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  var sheetId = document.getElementById('sheetIdElement')!.innerHTML;

  return loadFirstDataOfSheet(exerciseId, sheetId);
});

async function loadFirstDataOfSheet(exerciseId: string, sheetId: string): Promise<void> {
  await getExerciseSheetDetailsWithAssignments(exerciseId, sheetId)
    .catch(() => console.log('fail'))
    .then((sheetResult) => {
      if (sheetResult != null) {
        (<HTMLInputElement>document.getElementById('exerciseSheetId')!).value = sheetResult.sheetId;
        (<HTMLInputElement>document.getElementById('exerciseSheetLable')!).value = sheetResult.label;

        if (!(sheetResult.assignments == null || sheetResult.assignments == undefined)) {
          for (const assignment of sheetResult.assignments) {
            taskDetails.push(assignment);
          }
        }
      }
    });

  renderComponentTasks.value = false;
  await nextTick();
  renderComponentTasks.value = true;
}

async function editEXerciseSheet(): Promise<void> {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  var sheetId = (<HTMLInputElement>document.getElementById('exerciseSheetId')!).value;
  var sheetLable = (<HTMLInputElement>document.getElementById('exerciseSheetLable')!).value;

  if (
    exerciseId == null ||
    exerciseId == undefined ||
    exerciseId == '' ||
    sheetId == null ||
    sheetId == undefined ||
    sheetId == '' ||
    sheetLable == null ||
    sheetLable == undefined ||
    sheetLable == ''
  ) {
    alert(t('exercise.enterCorrectInfo'));
  } else {
    await editExerciseSheet(exerciseId, sheetId, sheetLable)
      .catch(() => console.log('fail'))
      .then((resultCreateSheet) => {
        if (resultCreateSheet != null && resultCreateSheet.status == 'SUCCESS') {
          return router.push({ name: exerciseRouteName, params: { exerciseId: exerciseId } });
        } else {
          alert(t('exercise.failProcess'));
        }
      });
  }
}

async function deleteAssignemntById(assignmentId: string): Promise<void> {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  var sheetId = (<HTMLInputElement>document.getElementById('exerciseSheetId')!).value;

  await deleteExerciseSheetAssignemnt(exerciseId, sheetId, assignmentId)
    .catch(() => console.log('fail'))
    .then((resultDeleteAssignment) => {
      if (resultDeleteAssignment != null && resultDeleteAssignment.status == 'SUCCESS') {
        location.reload();
      } else {
        alert(t('exercise.failProcess'));
      }
    });
}
</script>

<template>
  <main class="home">
    <div id="exerciseIdElement" style="visibility: hidden">{{ $route.params.exerciseId }}</div>
    <div id="sheetIdElement" style="visibility: hidden">{{ $route.params.sheetId }}</div>
    <h2 v-text="t('exercise.lecture') + ' ' + $route.params.exerciseId" />

    <div>
      <RouterLink :to="{ name: exerciseRouteName }">{{ t('exercise.backToExercise') }}</RouterLink>
    </div>

    <br />
    <h2 v-text="t('exercise.exerciseSheet') + ' ' + $route.params.sheetId" />

    <form @submit.prevent="editEXerciseSheet">
      <div class="form-fields">
        <div class="row">
          <label for="exerciseSheetId" class="label-beauty">ID</label>
          <input id="exerciseSheetId" class="input-beauty" :disabled="true" />

          <label for="exerciseSheetLable" class="label-beauty">Lable</label>
          <input id="exerciseSheetLable" class="input-beauty" />

          <label for="editExerciseSheetBtnId" class="label-beauty"></label>
          <input
            id="editExerciseSheetBtnId"
            type="submit"
            class="button btn-beauty-submit"
            style="width: 12%"
            :value="t('exercise.saveExercisesheet')"
          />
        </div>
      </div>
    </form>

    <br />
    <h2 v-text="t('exercise.exerciseSheetTask')" />

    <div class="vue-nice-table table-responsive">
      <table id="exerciseSheetsTable" class="styled-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>{{ t('exercise.exerciseSheetTaskDesignation') }}</th>
            <th>{{ t('exercise.exerciseSheetTaskMaximumScore') }}</th>
            <th>{{ t('exercise.exerciseSheetTaskShowStatistics') }}</th>
          </tr>
        </thead>
        <tbody v-if="renderComponentTasks">
          <tr v-for="(item, index) in taskDetails" :key="index">
            <td>{{ item.assignmentId }}</td>
            <td>{{ item.assignmentLable }}</td>
            <td>{{ item.maxPoint }}</td>
            <td>{{ item.showStatics }}</td>
            <td>
              <RouterLink
                :to="{
                  name: exercisesheetAssignmentsEditRouteName,
                  params: {
                    exerciseId: $route.params.exerciseId,
                    sheetId: $route.params.sheetId,
                    assignmentId: item.assignmentId,
                  },
                }"
                class="button btn-beauty1"
                style="color: orange"
              >
                {{ t('exercise.edit') }}
              </RouterLink>

              <button class="button btn-beauty1" style="color: red" @click="deleteAssignemntById(item.assignmentId)">
                {{ t('exercise.delete') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div>
      <RouterLink
        :to="{
          name: exercisesheetAssignmentsCreateRouteName,
          params: { exerciseId: $route.params.exerciseId, sheetId: $route.params.sheetId },
        }"
        class="button btn-beauty1"
      >
        {{ t('exercise.exerciseSheetTaskCreate') }}
      </RouterLink>
    </div>
  </main>
</template>

<style scoped>
@import '../../../../styles/new/btn-beauty-submit.css';
@import '../../../../styles/new/inputLable-beauty.css';
@import '../../../../styles/new/table-exercise.css';
@import '../../../../styles/new/btn-beauty1.css';
</style>
