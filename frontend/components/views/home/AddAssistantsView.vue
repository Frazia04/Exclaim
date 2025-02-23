<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { type Assistant, getAssistants } from '../../../api';
import { addAssisstant, removeAssistant } from '../../../api/lecture';
import { t } from '../../../i18n';

const route = useRoute();
const assistantUsername = ref<string>('');
const assistants = ref<Assistant[]>([]);
const lectureId = ref(route.params.lectureId);

onMounted(async () => {
  await fetchAssistants();
});

watch(
  () => route.params.lectureId,
  (newLectureId) => {
    lectureId.value = newLectureId;
  },
);
// eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/explicit-function-return-type
async function addAssistantwithUsername(event: Event): Promise<void> {
  event.preventDefault();
  await addAssisstant(lectureId.value as string, { username: assistantUsername.value });
  assistantUsername.value = '';
  await fetchAssistants();
}
// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
async function fetchAssistants() {
  assistants.value = await getAssistants(lectureId.value as string);
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
async function removeAssistantWithId(userId: number) {
  await removeAssistant(lectureId.value as string, userId);
  await fetchAssistants();
  console.log('removed successfully!');
}
</script>

<template>
  <main>
    <h1 v-text="t('courses.assistants')" />
    <div v-if="assistants.length > 0">
      <table class="table">
        <thead>
          <tr>
            <th v-text="t('courses.user-id')"></th>
            <th v-text="t('common.username')"></th>
            <th v-text="t('common.first-name')"></th>
            <th v-text="t('common.last-name')"></th>
            <th v-text="t('courses.actions')"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="assistant in assistants" :key="assistant?.userId">
            <td>{{ assistant.userId }}</td>
            <td>{{ assistant.username }}</td>
            <td>{{ assistant.firstname }}</td>
            <td>{{ assistant.lastname }}</td>
            <td>
              <button v-tooltip="'Delete'" class="btn btn-primary" @click="removeAssistantWithId(assistant.userId)">
                <i class="fas fa-trash"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-else>
      <p v-text="t('courses.no-assistants-added')"></p>
    </div>
    <form @submit="addAssistantwithUsername">
      <div>
        <label for="assistant">{{ t('courses.add-assistant') }}</label>
        <input id="assistant" v-model="assistantUsername" type="text" required />
      </div>
      <button type="submit" class="submit-button">
        {{ t('courses.add-assistant') }}
      </button>
    </form>
  </main>
</template>

<style scoped>
.container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f8f9fa;
}

h1 {
  text-align: center;
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
}

.form {
  width: 100%;
  max-width: 100;
  padding: 20px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
}

input,
select,
textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  transition: border-color 0.3s;
}

input:focus,
select:focus,
textarea:focus {
  border-color: #007bff;
  outline: none;
}

textarea {
  resize: vertical;
}

.submit-button {
  padding: 10px 15px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 10px;
}

.submit-button:hover {
  background-color: #0056b3;
}

.error,
.success {
  margin-top: 20px;
  text-align: center;
}

.error {
  color: red;
}

.success {
  color: green;
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
