<script setup lang="ts">
import { useRouter } from 'vue-router';

import { submitExerciseSheet } from '../../../../api/exercise';
import { t } from '../../../../i18n';
import { exerciseRouteName } from '../../../../router/names';

const router = useRouter();

async function submitNewEXerciseSheet(): Promise<void> {
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
    await submitExerciseSheet(exerciseId, sheetId, sheetLable)
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
</script>

<template>
  <main class="home">
    <div id="exerciseIdElement" style="visibility: hidden">{{ $route.params.exerciseId }}</div>
    <h2 v-text="t('exercise.lecture') + ' ' + $route.params.exerciseId" />

    <div>
      <RouterLink :to="{ name: exerciseRouteName }">{{ t('exercise.backToExercise') }}</RouterLink>
    </div>
    <br />
    <h2 v-text="t('exercise.newExerciseSheet')" />

    <br />
    <form @submit.prevent="submitNewEXerciseSheet">
      <div class="form-fields">
        <label for="exerciseSheetId" class="label-beauty">ID</label>
        <input id="exerciseSheetId" class="input-beauty" />
        <label for="exerciseSheetLable" class="label-beauty">Lable</label>
        <input id="exerciseSheetLable" class="input-beauty" />

        <input
          id="saveExerciseSheetBtnId"
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
