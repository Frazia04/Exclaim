<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue';

import type { Assistant } from '../../../../api';
import { addTutor, getTutors, removeTutor } from '../../../../api/exercise';
import { t } from '../../../../i18n';
import { exerciseGroupManagmentRouteName } from '../../../../router/names';

const renderComponentTutorTable = ref(true);

const tutorGroups: Assistant[] = [];
var exerciseId: string;
var groupId: string;

onMounted(async () => {
  exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  groupId = document.getElementById('groupIdElement')!.innerHTML;

  await getTutors(exerciseId, groupId).then((resultTutors) => {
    if (resultTutors != null) {
      for (const element of resultTutors) {
        tutorGroups.push(element);
      }
    }
  });

  renderComponentTutorTable.value = false;
  await nextTick();
  renderComponentTutorTable.value = true;
});

async function addTutorwithUsername(): Promise<void> {
  var username = (<HTMLInputElement>document.getElementById('tutorUsername')!).value;

  const resultAddTutor = await addTutor(exerciseId, groupId, username);
  if (resultAddTutor != null && resultAddTutor.status == 'SUCCESS') {
    location.reload();
  } else {
    alert(t('exercise.failProcess'));
  }
}

async function removeTutorWithId(userId: number): Promise<void> {
  const resultDeleteTutor = await removeTutor(exerciseId, groupId, userId);

  if (resultDeleteTutor != null && resultDeleteTutor.status == 'SUCCESS') {
    location.reload();
  } else {
    alert(t('exercise.failProcess'));
  }
}
</script>

<template>
  <main>
    <div id="exerciseIdElement" style="visibility: hidden">{{ $route.params.exerciseId }}</div>
    <div id="groupIdElement" style="visibility: hidden">{{ $route.params.groupId }}</div>
    <h2 v-text="t('exercise.exercise') + ' ' + $route.params.exerciseId" />
    <h2 v-text="t('exercise.groups') + ' ' + $route.params.groupId" />

    <div>
      <RouterLink :to="{ name: exerciseGroupManagmentRouteName }">{{ t('exercise.backToGroupManagement') }}</RouterLink>
    </div>

    <br />
    <h3>Tutors</h3>

    <div class="vue-nice-table table-responsive">
      <table id="tutorsTable" class="styled-table">
        <thead>
          <tr>
            <th v-text="'User ID'"></th>
            <th v-text="'Username'"></th>
            <th v-text="'Fisrt Name'"></th>
            <th v-text="'Last Name'"></th>
            <th v-text="t('courses.actions')"></th>
          </tr>
        </thead>
        <tbody v-if="renderComponentTutorTable">
          <tr v-for="tutor in tutorGroups" :key="tutor?.userId">
            <td>{{ tutor.userId }}</td>
            <td>{{ tutor.username }}</td>
            <td>{{ tutor.firstname }}</td>
            <td>{{ tutor.lastname }}</td>
            <td>
              <button class="button btn-beauty1" style="color: red" @click="removeTutorWithId(tutor.userId)">
                {{ t('exercise.groupDelete') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <form @submit.prevent="addTutorwithUsername">
      <div class="form-fields">
        <label for="tutorUsername" class="label-beauty">{{ 'Tutor' }}</label>
        <input id="tutorUsername" style="width: 30%" type="text" required class="input-beauty" />
      </div>
      <input
        type="submit"
        style="width: 12%"
        class="button btn-beauty-submit"
        :value="t('exercise.saveExercisesheet')"
      />
    </form>
  </main>
</template>

<style scoped>
@import '../../../../styles/new/btn-beauty1.css';
@import '../../../../styles/new/btn-beauty-submit.css';
@import '../../../../styles/new/inputLable-beauty.css';
@import '../../../../styles/new/table-exercise.css';
</style>
