<script setup lang="ts">
import ExpandedIcon from '@material-symbols/svg-400/outlined/expand_less.svg';
import CollapsedIcon from '@material-symbols/svg-400/outlined/expand_more.svg';
import { onMounted } from 'vue';
import { onBeforeRouteUpdate, RouterLink } from 'vue-router';

import { encodeRFC3986URIComponent } from '../../../api';
import { errorHandler } from '../../../errors';
import { t } from '../../../i18n';
import { joinCourseRouteName, manageCourseRouteName } from '../../../router/names';
import { accountData } from '../../../store/account';
import { exercises, refresh as refreshExercises } from '../../../store/exercise';
import { groupByTerm, termKey, termString } from '../../../utils/term';
import LoadingSpinner from '../../general/LoadingSpinner.vue';
import { useCollapsedTerms } from './collapseTerms';

// Expanded/collapsed state for terms
const { toggleExpandTerm, isExpanded, expandMostRecentTerm } = useCollapsedTerms();

// When entering the route: refresh exercises only if cache is expired.
onMounted(async () => {
  expandMostRecentTerm(exercises.value?.joined);
  await refreshExercises();
  expandMostRecentTerm(exercises.value?.joined);
});

// When updating the route (click on logo): force refresh.
// Do not return/await the promise to avoid showing the loading spinner.
onBeforeRouteUpdate(() => void refreshExercises(0).catch(errorHandler));
</script>

<template>
  <main class="home">
    <h1 v-text="t('home.my-courses')" />
    <template v-if="exercises">
      <p>
        <RouterLink
          v-if="!accountData?.isAdmin && !accountData?.isAssistant"
          :to="{ name: joinCourseRouteName }"
          class="button"
          >{{ t('home.join-course') }}</RouterLink
        >
        <RouterLink v-if="accountData?.isAdmin" :to="{ name: manageCourseRouteName }" class="button">{{
          t('home.manage-courses')
        }}</RouterLink>
      </p>
      <template v-if="exercises.joined.length">
        <template v-for="[term, exercisesForTerm] of groupByTerm(exercises.joined)" :key="termKey(term)">
          <h2 @click="toggleExpandTerm(term)">
            <ExpandedIcon v-if="isExpanded(term)" />
            <CollapsedIcon v-else />
            {{ termString(term) }}
          </h2>
          <ul v-show="isExpanded(term)">
            <li v-for="exercise of exercisesForTerm" :key="exercise.exerciseId">
              <b
                ><a :href="`exercise/${encodeRFC3986URIComponent(exercise.exerciseId)}`" v-text="exercise.exerciseId"
              /></b>
              {{ exercise.lecture }}
            </li>
          </ul>
        </template>
      </template>
      <template v-else>{{ t('home.no-joined-courses') }}</template>
    </template>
    <div v-else>
      <LoadingSpinner />
    </div>
  </main>
</template>
