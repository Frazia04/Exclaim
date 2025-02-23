<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

import { editExerciseSheetAssignemnt, getAssignmentWithDetails } from '../../../../api/exercise';
import { t } from '../../../../i18n';
import { exerciseSheetEditRouteName } from '../../../../router/names';

const router = useRouter();

onMounted(() => {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  var sheetId = document.getElementById('sheetIdElement')!.innerHTML;
  var assignmentId = document.getElementById('assignmentIdElement')!.innerHTML;

  return loadFirstDataOfAssignemnt(exerciseId, sheetId, assignmentId);
});

async function loadFirstDataOfAssignemnt(exerciseId: string, sheetId: string, assignmentId: string): Promise<void> {
  await getAssignmentWithDetails(exerciseId, sheetId, assignmentId)
    .catch(() => console.log('fail'))
    .then((assignmentResult) => {
      if (assignmentResult != null) {
        (<HTMLInputElement>document.getElementById('assignmentDesignationId')!).value =
          assignmentResult.assignmentLable;
        (<HTMLInputElement>document.getElementById('assignmentMaximumScoreId')!).value = assignmentResult.maxPoint;
        var assignmentShowStatistics = (<HTMLInputElement>document.getElementById('assignmentShowStatisticsId')!)
          .checked;

        if (
          !(
            assignmentShowStatistics == null ||
            assignmentShowStatistics == undefined ||
            assignmentShowStatistics == false
          )
        ) {
          (<HTMLInputElement>document.getElementById('assignmentShowStatisticsId')!).checked = true;
        }
      }
    });
}

async function editEXerciseSheetAssignemnt(): Promise<void> {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  var sheetId = document.getElementById('sheetIdElement')!.innerHTML;
  var assignmentId = document.getElementById('assignmentIdElement')!.innerHTML;

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

    await editExerciseSheetAssignemnt(
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
    <div id="assignmentIdElement" style="visibility: hidden">{{ $route.params.assignmentId }}</div>

    <h2 v-text="t('exercise.lecture') + ' ' + $route.params.exerciseId" />
    <h2 v-text="t('exercise.exerciseSheet') + ' ' + $route.params.sheetId" />
    <div>
      <RouterLink :to="{ name: exerciseSheetEditRouteName }">{{ t('exercise.backToExerciseSheet') }}</RouterLink>
    </div>
    <br />

    <h2 v-text="t('exercise.task') + ' ' + $route.params.assignmentId" />

    <form @submit.prevent="editEXerciseSheetAssignemnt">
      <div class="form-fields">
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
          id="editAssignemntBtnId"
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
