<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { getExercise } from '../../../api/exercise';
import { createLecture, updateLecture } from '../../../api/lecture';
import type { Lecture, Term_SummerWinter } from '../../../api/types';
import { t } from '../../../i18n';
import { manageCourseRouteName } from '../../../router/names';

const route = useRoute();
const router = useRouter();
const isEditMode = ref(false);
const lectureId = ref<string | null>(null);

const lecture = ref<Lecture>({
  lectureId: '',
  lectureName: '',
  term: {
    term: 'summer' as Term_SummerWinter,
    year: new Date().getFullYear(),
    comment: '',
  },
  year: new Date().getFullYear(),
});

watch(
  () => route.params.lectureId,
  async (newLectureId) => {
    if (newLectureId) {
      isEditMode.value = true;
      lectureId.value = newLectureId as string;
      const fetchedLecture = await getExercise(lectureId.value);
      if (fetchedLecture) {
        lecture.value = {
          lectureId: fetchedLecture.exerciseId,
          lectureName: fetchedLecture.lecture,
          term: {
            term: fetchedLecture.term.term as Term_SummerWinter,
            year: fetchedLecture.term.year,
            comment: fetchedLecture.term.comment,
          },
          year: fetchedLecture.term.year,
        };
      }
    } else {
      isEditMode.value = false;
      lectureId.value = null;
      resetLecture();
    }
  },
  { immediate: true },
);

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
function resetLecture() {
  lecture.value = {
    lectureId: '',
    lectureName: '',
    term: {
      term: 'summer' as Term_SummerWinter,
      year: new Date().getFullYear(),
      comment: '',
    },
    year: new Date().getFullYear(),
  };
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
async function submitLecture() {
  try {
    if (isEditMode.value) {
      await updateLecture(lectureId.value!, lecture.value);
      alert('lecture updated successfully');
    } else {
      await createLecture(lecture.value);
      alert('lecture created successfully');
    }
    await router.push({ name: manageCourseRouteName });
  } catch (error) {
    alert('lecture save failed');
  }
}
</script>

<template>
  <main>
    <div v-if="isEditMode">
      <h1 v-text="t('courses.update-lecture')"></h1>
    </div>
    <div v-else>
      <h1 v-text="t('courses.create-new-lecture')"></h1>
    </div>
    <form @submit.prevent="submitLecture">
      <div>
        <label for="lectureId">{{ t('courses.id') }}</label>
        <input id="lectureId" v-model="lecture.lectureId" type="text" :readonly="isEditMode" required />
      </div>
      <div>
        <label for="lectureName">{{ t('courses.name') }}</label>
        <input id="lectureName" v-model="lecture.lectureName" type="text" required />
      </div>
      <div>
        <label for="term">{{ t('courses.term') }}</label>
        <select id="term" v-model="lecture.term.term" required>
          <option value="summer">{{ 'Summer' }}</option>
          <option value="winter">{{ 'Winter' }}</option>
        </select>
      </div>
      <div>
        <label for="year">{{ t('courses.year') }}</label>
        <input id="year" v-model.number="lecture.year" type="number" required />
      </div>
      <div v-if="isEditMode">
        <button class="submit-button" type="submit">{{ t('courses.update') }}</button>
      </div>
      <div v-else>
        <button class="submit-button" type="submit">{{ t('courses.create') }}</button>
      </div>
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
</style>
