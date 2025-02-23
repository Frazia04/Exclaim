<script setup lang="ts">
import { useRouter } from 'vue-router';

import { saveExerciseSheetAssignemnt } from '../../../../api/exercise';
import { t } from '../../../../i18n';
import { exerciseSheetEditRouteName } from '../../../../router/names';

const router = useRouter();

async function submitNewEXerciseSheetAssignemnt(): Promise<void> {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  var sheetId = document.getElementById('sheetIdElement')!.innerHTML;

  var assignmentId = (<HTMLInputElement>document.getElementById('assignmentId')!).value;
  var assignmentDesignation = (<HTMLInputElement>document.getElementById('assignmentDesignationId')!).value;
  var assignmentMaximumScore = (<HTMLInputElement>document.getElementById('assignmentMaximumScoreId')!).value;
  var assignmentShowStatistics = (<HTMLInputElement>document.getElementById('assignmentShowStatisticsId')!).checked;

  if (
    exerciseId == null ||
    exerciseId == undefined ||
    exerciseId == '' ||
    sheetId == null ||
    sheetId == undefined ||
    sheetId == '' ||
    assignmentId == null ||
    assignmentId == undefined ||
    assignmentId == '' ||
    assignmentDesignation == null ||
    assignmentDesignation == undefined ||
    assignmentDesignation == '' ||
    assignmentMaximumScore == null ||
    assignmentMaximumScore == undefined ||
    assignmentMaximumScore == ''
  ) {
    alert(t('exercise.enterCorrectInfo'));
  } else {
    if (assignmentShowStatistics == null || assignmentShowStatistics == undefined) {
      assignmentShowStatistics = false;
    }

    await saveExerciseSheetAssignemnt(
      exerciseId,
      sheetId,
      assignmentId,
      assignmentDesignation,
      assignmentMaximumScore,
      assignmentShowStatistics,
    )
      .catch(() => console.log('fail'))
      .then((resultCreateSheetAssignment) => {
        if (resultCreateSheetAssignment != null && resultCreateSheetAssignment.status == 'SUCCESS') {
          return router.push({
            name: exerciseSheetEditRouteName,
            params: { exerciseId: exerciseId, sheetId: sheetId },
          });
        } else {
          alert(t('exercise.failProcess'));
        }
      });
  }
}
</script>

<template>
  <main class="home">
    <div id="exerciseIdElement" style="visibility: hidden">{{ $route.params.exerciseId }}</div>
    <div id="sheetIdElement" style="visibility: hidden">{{ $route.params.sheetId }}</div>
    <h2 v-text="t('exercise.lecture') + ' ' + $route.params.exerciseId" />
    <br />
    <h2 v-text="t('exercise.exerciseSheet') + ' ' + $route.params.sheetId" />
    <div>
      <RouterLink :to="{ name: exerciseSheetEditRouteName }">{{ t('exercise.backToExerciseSheet') }}</RouterLink>
    </div>
    <br />

    <h2 v-text="t('exercise.createNewTask')" />

    <form @submit.prevent="submitNewEXerciseSheetAssignemnt">
      <div class="form-fields">
        <label for="assignmentId" class="label-beauty">ID</label>
        <input id="assignmentId" class="input-beauty" />

        <label for="assignmentDesignationId" class="label-beauty">{{
          t('exercise.exerciseSheetTaskDesignation')
        }}</label>
        <input id="assignmentDesignationId" class="input-beauty" />

        <label for="assignmentMaximumScoreId" class="label-beauty">{{
          t('exercise.exerciseSheetTaskMaximumScore')
        }}</label>
        <input id="assignmentMaximumScoreId" class="input-beauty" />

        <label for="assignmentShowStatisticsId" class="label-beauty">{{
          t('exercise.exerciseSheetTaskShowStatistics')
        }}</label>
        <input id="assignmentShowStatisticsId" type="checkbox" class="check-beauty" />

        <input
          id="saveAssignemntBtnId"
          type="submit"
          class="button btn-beauty-submit"
          style="width: 12%"
          :value="t('exercise.saveExercisesheet')"
        />
      </div>
    </form>

    <br />
    <br />
  </main>
</template>

<style scoped>
@import '../../../../styles/new/btn-beauty-submit.css';
@import '../../../../styles/new/inputLable-beauty.css';
</style>
