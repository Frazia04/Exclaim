<script setup lang="ts">
import ExpandedIcon from '@material-symbols/svg-400/outlined/expand_less.svg';
import CollapsedIcon from '@material-symbols/svg-400/outlined/expand_more.svg';
import { computed, onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';

import { encodeRFC3986URIComponent, type Exercise } from '../../../api';
import { t } from '../../../i18n';
import { navigateLegacy } from '../../../router/legacy';
import { homeRouteName } from '../../../router/names';
import { exercises, joinExercise, refresh as refreshExercises } from '../../../store/exercise';
import { groupByTerm, termKey, termString } from '../../../utils/term';
import LoadingSpinner from '../../general/LoadingSpinner.vue';
import { useCollapsedTerms } from './collapseTerms';

// Expanded/collapsed state for terms
const { toggleExpandTerm, isExpanded, expandMostRecentTerm } = useCollapsedTerms();

// Whether joining a course is in progress
const joining = ref(false);

// Split translation to insert a link
const joinedCoursesTextTranslation = computed(() => t('home.joined-courses-text').split('#', 3));

// Refresh exercises only if cache is expired
onMounted(async () => {
  expandMostRecentTerm(exercises.value?.joinable);
  await refreshExercises();
  expandMostRecentTerm(exercises.value?.joinable);
});

// Join an exercise
async function join(exercise: Exercise): Promise<void> {
  if (
    !joining.value &&
    confirm(t('home.confirm-join', exercise.exerciseId, exercise.lecture + ' - ' + termString(exercise.term)))
  ) {
    joining.value = true;

    // Send the api request to join the exercise and update our local data with the response
    // eslint-disable-next-line no-param-reassign
    exercise = await joinExercise(exercise.exerciseId);

    // Navigate to exercise
    let path = `exercise/${encodeRFC3986URIComponent(exercise.exerciseId)}`;
    switch (exercise.groupJoin) {
      case 'group':
        path += '/groups';
        break;
      case 'preferences':
        path += '/groups/preferences';
        break;
    }
    navigateLegacy(path);
  }
}
</script>

<template>
  <main class="home">
    <h1 v-text="t('home.join-course')" />
    <template v-if="exercises && !joining">
      <template v-if="exercises.joinable.length">
        <p>
          {{ t('home.join-courses-text') }} {{ joinedCoursesTextTranslation[0]
          }}<RouterLink :to="{ name: homeRouteName }">{{ joinedCoursesTextTranslation[1] }}</RouterLink
          >{{ joinedCoursesTextTranslation[2] }}
        </p>
        <template v-for="[term, exercisesForTerm] of groupByTerm(exercises.joinable)" :key="termKey(term)">
          <h2 @click="toggleExpandTerm(term)">
            <ExpandedIcon v-if="isExpanded(term)" />
            <CollapsedIcon v-else />
            {{ termString(term) }}
          </h2>
          <ul v-show="isExpanded(term)">
            <li v-for="exercise of exercisesForTerm" :key="exercise.exerciseId">
              <b v-text="exercise.exerciseId" /> {{ exercise.lecture }}
              <button @click="join(exercise)" v-text="t('home.join')" />
            </li>
          </ul>
        </template>
      </template>
      <template v-else>
        {{ t('home.no-joinable-courses') }} {{ joinedCoursesTextTranslation[0]
        }}<RouterLink :to="{ name: homeRouteName }">{{ joinedCoursesTextTranslation[1] }}</RouterLink
        >{{ joinedCoursesTextTranslation[2] }}
      </template>
    </template>
    <div v-else>
      <LoadingSpinner />
    </div>
  </main>
</template>
