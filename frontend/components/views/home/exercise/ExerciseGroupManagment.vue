<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue';

import type { ExerciseGroupWithDetails } from '../../../../api';
import {
  deleteExerciseGroup,
  editGroupRegistration,
  getExercise,
  getExerciseGroupDetailData,
} from '../../../../api/exercise';
import { t } from '../../../../i18n';
import {
  exerciseGroupCreateRouteName,
  exerciseGroupEditRouteName,
  exerciseGroupTutorRouteName,
  exerciseRouteName,
} from '../../../../router/names';

const renderComponentGroupTable = ref(true);
const groupDetails: ExerciseGroupWithDetails[] = [];

onMounted(() => {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  return loadGroupData(exerciseId);
});

async function loadGroupData(exerciseId: string): Promise<void> {
  await getExerciseGroupDetailData(exerciseId)
    .catch(() => console.log('fail'))
    .then((groupsResult) => {
      if (groupsResult != null) {
        for (const groupdata of groupsResult) {
          groupDetails.push(groupdata);
        }
      }
    });
  renderComponentGroupTable.value = false;
  await nextTick();
  renderComponentGroupTable.value = true;

  var registrationOpen;
  var groupJoin;
  await getExercise(exerciseId)
    .catch(() => console.log('fail'))
    .then((exercisBaseData) => {
      if (exercisBaseData != null) {
        registrationOpen = exercisBaseData.registrationOpen;
        groupJoin = exercisBaseData.groupJoin.toUpperCase();
      }
    });

  if (registrationOpen == null || registrationOpen == undefined || registrationOpen == false) {
    (<HTMLInputElement>document.getElementById('registrationOpen')!).value = String(0);
  } else {
    (<HTMLInputElement>document.getElementById('registrationOpen')!).value = String(1);
  }

  if (groupJoin == null || groupJoin == undefined) {
    (<HTMLInputElement>document.getElementById('groupJoin')!).value = 'NONE';
  } else {
    (<HTMLInputElement>document.getElementById('groupJoin')!).value = groupJoin;
  }
}

async function deleteGroupById(groupId: string): Promise<void> {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;

  await deleteExerciseGroup(exerciseId, groupId)
    .catch(() => console.log('fail'))
    .then((resultDeleteGroup) => {
      if (resultDeleteGroup != null && resultDeleteGroup.status == 'SUCCESS') {
        location.reload();
      } else {
        alert(t('exercise.failProcess'));
      }
    });
}

async function updateGroupRegisterationData(): Promise<void> {
  var exerciseId = document.getElementById('exerciseIdElement')!.innerHTML;
  var registrationOpen = (<HTMLInputElement>document.getElementById('registrationOpen')!).value;
  var groupJoin = (<HTMLInputElement>document.getElementById('groupJoin')!).value;

  await editGroupRegistration(exerciseId, Boolean(registrationOpen), groupJoin)
    .catch(() => console.log('fail'))
    .then((resultDeleteGroup) => {
      if (resultDeleteGroup != null && resultDeleteGroup.status == 'SUCCESS') {
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
    <h2 v-text="t('exercise.groupTiltle') + ' ' + $route.params.exerciseId" />

    <div>
      <RouterLink :to="{ name: exerciseRouteName }">{{ t('exercise.backToExercise') }}</RouterLink>
    </div>

    <br />

    <table class="buttons">
      <tr>
        <td>
          <a
            id="linkGroupImportStudent"
            :href="'exercise/' + $route.params.exerciseId + '/admin/import'"
            class="button btn-beauty1"
            >{{ t('exercise.groupImportStudent') }}
          </a>
        </td>
        <td>
          <a
            id="linkGroupRegisteration"
            :href="'exercise/' + $route.params.exerciseId + '/admin/registrations'"
            class="button btn-beauty1"
            >{{ t('exercise.groupRegisteration') }}
          </a>
        </td>
        <td>
          <a
            id="linkGroupAllocation"
            :href="'exercise/' + $route.params.exerciseId + '/admin/optimus'"
            class="button btn-beauty1"
            >{{ t('exercise.groupAllocation') }}
          </a>
        </td>
      </tr>
    </table>

    <div class="custom-select">
      <form @submit.prevent="updateGroupRegisterationData">
        <fieldset>
          <div class="form-fields">
            <label for="registrationOpen" class="col-sm-3 control-label">{{
              t('exercise.groupRegisterationEvent')
            }}</label>
            <select id="registrationOpen" class="form-control" name="registrationOpen">
              <option value="0">{{ t('exercise.groupClosed') }}</option>
              <option value="1">{{ t('exercise.groupOpen') }}</option>
            </select>
            <label for="groupJoin" class="col-sm-3 control-label">{{ t('exercise.groupRegisterationIn') }}</label>
            <select id="groupJoin" class="form-control" name="groupJoin">
              <option value="NONE">{{ t('exercise.groupClosed') }}</option>
              <option value="GROUP">{{ t('exercise.groupFreeSelection') }}</option>
              <option value="PREFERENCES">{{ t('exercise.groupSpecifyPreferences') }}</option>
            </select>

            <input
              id="saveExerciseGroupBtnId"
              type="submit"
              class="button btn-beauty-submit"
              style="width: 15%"
              :value="t('exercise.groupSubmitNew')"
            />
          </div>
        </fieldset>
      </form>
    </div>

    <div class="vue-nice-table table-responsive">
      <table id="exerciseSheetsTable" class="styled-table">
        <thead>
          <tr>
            <th>{{ t('exercise.groupId') }}</th>
            <th>{{ t('exercise.groupDay') }}</th>
            <th>{{ t('exercise.groupTime') }}</th>
            <th>{{ t('exercise.groupLocation') }}</th>
            <th>{{ t('exercise.groupMaxSize') }}</th>
            <th>{{ t('exercise.groupCurrentSize') }}</th>
            <th>{{ t('exercise.groupTutors') }}</th>
          </tr>
        </thead>
        <tbody v-if="renderComponentGroupTable">
          <tr v-for="(item, index) in groupDetails" :key="index">
            <td>{{ item.groupId }}</td>
            <td>{{ item.day }}</td>
            <td>{{ item.time }}</td>
            <td>{{ item.location }}</td>
            <td>{{ item.maxSize }}</td>
            <td>{{ item.currentSize }}</td>
            <td>{{ item.tutorNames }}</td>
            <td>
              <RouterLink
                :to="{ name: exerciseGroupTutorRouteName, params: { groupId: item.groupId } }"
                class="button btn-beauty1"
                >{{ t('exercise.groupTutors') }}
              </RouterLink>
              <RouterLink
                :to="{ name: exerciseGroupEditRouteName, params: { groupId: item.groupId } }"
                class="button btn-beauty1"
                style="color: orange"
              >
                {{ t('exercise.groupEdit') }}
              </RouterLink>

              <button class="button btn-beauty1" style="color: red" @click="deleteGroupById(item.groupId)">
                {{ t('exercise.groupDelete') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div>
      <RouterLink :to="{ name: exerciseGroupCreateRouteName }" class="button btn-beauty1">
        {{ t('exercise.newGroup') }}
      </RouterLink>
    </div>
  </main>
</template>

<style scoped>
@import '../../../../styles/new/btn-beauty1.css';
@import '../../../../styles/new/btn-beauty2.css';
@import '../../../../styles/new/table-exercise.css';
@import '../../../../styles/new/btn-beauty-submit.css';
@import '../../../../styles/new/select-beauty.css';
</style>
