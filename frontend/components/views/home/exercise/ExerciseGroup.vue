<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue';

import { getExerciseGroupDetailData, groupJoin } from '../../../../api/exercise';
import { t } from '../../../../i18n';
import { exerciseRouteName } from '../../../../router/names';
import { accountData } from '../../../../store/account';

interface GroupDetail {
  groupId: string;
  day: string;
  time: string;
  location: string;
  tutorNames: string;
  currentSize: number;
  maxSize: number;
}

const renderComponentGroup = ref(true);
const groupDetails = ref<GroupDetail[]>([]);

onMounted(async () => {
  const exerciseId = document.getElementById('exerciseIdElement')?.innerHTML;
  if (exerciseId) {
    await loadGroupData(exerciseId);
  }
});

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
async function loadGroupData(exerciseId: string) {
  try {
    const groupsResult = await getExerciseGroupDetailData(exerciseId);
    if (groupsResult !== null) {
      groupDetails.value = groupsResult;
    }
  } catch (error) {
    console.error('Error loading group data:', error);
  } finally {
    renderComponentGroup.value = false;
    await nextTick();
    renderComponentGroup.value = true;
  }
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
async function joinGroup(groupId: string) {
  const exerciseId = document.getElementById('exerciseIdElement')?.innerHTML;
  if (exerciseId) {
    try {
      const response: any = await groupJoin(exerciseId, groupId);

      if (response.status === 200) {
        // Update local state directly
        const group = groupDetails.value.find((g) => g.groupId === groupId);
        if (group) {
          group.currentSize += 1;
        }
      } else {
        console.error('Failed to join group:', response);
      }
    } catch (error) {
      console.error('Error joining group:', error);
    }
  }
}
</script>

<template>
  <main class="home">
    <div id="exerciseIdElement" style="visibility: hidden">{{ $route.params.exerciseId }}</div>
    <h2 v-text="t('exercise.groupTiltle') + ' ' + $route.params.exerciseId" />

    <div>
      <RouterLink :to="{ name: exerciseRouteName }">{{ t('exercise.backToExercise') }}</RouterLink>
    </div>

    <div v-if="renderComponentGroup">
      <table>
        <th:block v-for="(group, indexColumn) in groupDetails" :key="indexColumn">
          <td>
            <div v-if="accountData?.isAssistant || accountData?.isAdmin" class="col-sm-3">
              <table style="border: 1px solid; margin: 5px">
                <tr>
                  <td>{{ t('exercise.groupId') }}</td>
                  <td>{{ group.groupId }}</td>
                </tr>
                <tr>
                  <td>{{ t('exercise.groupDay') }}</td>
                  <td>{{ group.day }}</td>
                </tr>
                <tr>
                  <td>{{ t('exercise.groupTime') }}</td>
                  <td>{{ group.time }}</td>
                </tr>
                <tr>
                  <td>{{ t('exercise.groupLocation') }}</td>
                  <td>{{ group.location }}</td>
                </tr>
                <tr>
                  <td>{{ t('exercise.groupTutors') }}</td>
                  <td>{{ group.tutorNames }}</td>
                </tr>
                <tr>
                  <td>{{ t('exercise.groupMemebrs') }}</td>
                  <td>{{ group.currentSize }}/{{ group.maxSize }}</td>
                </tr>
                <tr></tr>
              </table>
            </div>
            <div v-else class="col-sm-3">
              <table style="border: 1px solid; margin: 5px">
                <tr>
                  <td>{{ t('exercise.groupId') }}</td>
                  <td>{{ group.groupId }}</td>
                </tr>
                <tr>
                  <td>{{ t('exercise.groupDay') }}</td>
                  <td>{{ group.day }}</td>
                </tr>
                <tr>
                  <td>{{ t('exercise.groupTime') }}</td>
                  <td>{{ group.time }}</td>
                </tr>
                <tr>
                  <td>{{ t('exercise.groupLocation') }}</td>
                  <td>{{ group.location }}</td>
                </tr>
                <tr>
                  <td>{{ t('exercise.groupTutors') }}</td>
                  <td>{{ group.tutorNames }}</td>
                </tr>
                <tr>
                  <td>{{ t('exercise.groupMemebrs') }}</td>
                  <td>{{ group.currentSize }}/{{ group.maxSize }}</td>
                </tr>
                <tr>
                  <td v-if="group.currentSize < group.maxSize" colspan="2">
                    <button @click="joinGroup(group.groupId)">
                      {{ t('exercise.joinGroup') }}
                    </button>
                  </td>
                </tr>
              </table>
            </div>
          </td>
        </th:block>
      </table>
    </div>
  </main>
</template>
