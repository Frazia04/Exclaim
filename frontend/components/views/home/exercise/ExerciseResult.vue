<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue';
import * as XLSX from 'xlsx';

import type { ExerciseResultDetails } from '../../../../api';
import { getExerciseResult } from '../../../../api/exercise';
import { t } from '../../../../i18n';
import { exerciseRouteName } from '../../../../router/names';

const renderComponentResultsTable = ref(true);
const resultDetails: ExerciseResultDetails[] = [];
onMounted(() => {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  return loadFirstDataOfExercise(exerciseId);
});

async function loadFirstDataOfExercise(exerciseId: string): Promise<void> {
  await getExerciseResult(exerciseId)
    .catch(() => console.log('fail'))
    .then((resultList) => {
      console.log(resultList);
      if (resultList != null) {
        for (const resultElement of resultList) {
          resultDetails.push(resultElement);
        }
      }
    });
  renderComponentResultsTable.value = false;
  await nextTick();
  renderComponentResultsTable.value = true;
}

const exportToExcel = (): any => {
  const worksheet = XLSX.utils.json_to_sheet(
    resultDetails.map((item) => ({
      Gruppe: item.groupId,
      Team: item.teamId,
      BenutzerID: item.userId,
      Martikelnummer: item.studentId,
      Vorname: item.firstName,
      Nachname: item.lastName,
      Punkt: `${item.grade} / ${item.maxPointsTotal}`,
      Fehlzeiten: item.attendance,
    })),
  );
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Results');
  XLSX.writeFile(workbook, 'exercise_results.xlsx');
};

const copyData = async (): Promise<any> => {
  const copyText = resultDetails
    .map(
      (item) =>
        `${item.groupId}\t${item.teamId}\t${item.userId}\t${item.studentId}\t${item.firstName}\t${item.lastName}\t${item.grade} / ${item.maxPointsTotal}\t${item.attendance}`,
    )
    .join('\n');

  try {
    await navigator.clipboard.writeText(copyText);
    alert('Table data copied to clipboard');
  } catch (err) {
    alert('Failed to copy data:');
  }
};
</script>

<template>
  <main class="home">
    <div id="exerciseIdElement" style="visibility: hidden">{{ $route.params.exerciseId }}</div>
    <h2 v-text="t('exercise.lecture') + ' ' + $route.params.exerciseId" />

    <div>
      <RouterLink :to="{ name: exerciseRouteName }">{{ t('exercise.backToExercise') }}</RouterLink>
    </div>

    <table class="buttons">
      <tr>
        <td>
          <a class="button btn-beauty2" href="mailto:admin@example.com">{{ t('exercise.EmailToAll') }}</a>
        </td>
      </tr>
    </table>

    <table>
      <tr>
        <td>
          <button class="button btn-beauty2" @click="copyData">Copy</button>
        </td>
        <td>
          <button class="button btn-beauty2" @click="exportToExcel">CSV</button>
        </td>
      </tr>
    </table>

    <div class="vue-nice-table table-responsive">
      <table id="exerciseSheetsTable" class="styled-table">
        <thead>
          <tr>
            <th>
              {{ t('exercise.groupIdd') }}
            </th>
            <th>
              {{ t('exercise.Team') }}
            </th>
            <th>
              {{ t('exercise.userId') }}
            </th>
            <th>
              {{ t('exercise.studentId') }}
            </th>
            <th>
              {{ t('exercise.firstName') }}
            </th>
            <th>
              {{ t('exercise.lastName') }}
            </th>
            <th>
              {{ t('exercise.maximum-score') }}
            </th>
          </tr>
        </thead>
        <tbody v-if="renderComponentResultsTable">
          <tr v-for="(item, index) in resultDetails" :key="index">
            <td>{{ item.groupId }}</td>
            <td>{{ item.teamId }}</td>
            <td>{{ item.userId }}</td>
            <td>{{ item.studentId }}</td>
            <td>{{ item.firstName }}</td>
            <td>{{ item.lastName }}</td>
            <td>{{ item.grade }} / {{ item.maxPointsTotal }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </main>
</template>

<style scoped>
@import '../../../../styles/new/btn-beauty2.css';
@import '../../../../styles/new/table-exercise.css';

.styled-table th {
  cursor: pointer;
}

.sort-icon {
  display: inline-block;
  margin-left: 5px;
  font-size: 0.8em;
}
</style>
