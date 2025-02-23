import { computed, reactive, toRaw } from 'vue';

import {
  type Exercise,
  type ExerciseWithDetails,
  getExercise,
  getExercises,
  joinExercise as apiJoinExercise,
} from '../api';
import { compareExerciseByTerm } from '../utils/term';

type ExerciseWithOptionalData = Exercise & { timestamp?: number };

type ExerciseStore = {
  timestamp: number;
  exercises: Map<string, ExerciseWithOptionalData>;
};

const store = reactive<ExerciseStore>({
  timestamp: 0,
  exercises: new Map<string, ExerciseWithOptionalData>(),
});

function storeExercise(exercise: ExerciseWithDetails): void {
  const id = exercise.exerciseId;
  store.exercises.set(id, {
    // the current map entry might have additional data that we want to keep
    ...toRaw(store).exercises.get(id),
    // update data from API result (including details)
    ...exercise,
    timestamp: Date.now(),
  });
}

export const exercises = computed<{ joined: Exercise[]; joinable: Exercise[] } | null>(() => {
  if (store.timestamp) {
    const joined: Exercise[] = [];
    const joinable: Exercise[] = [];
    for (const exercise of store.exercises.values()) {
      const {
        roles: { student, tutorGroups, assistant },
      } = exercise;
      if (student || tutorGroups.length || assistant) {
        joined.push(exercise);
      }
      if (!student && exercise.registrationOpen) {
        joinable.push(exercise);
      }
    }
    return {
      joined: joined.sort(compareExerciseByTerm),
      joinable: joinable.sort(compareExerciseByTerm),
    };
  }
  return null;
});

export async function refresh(cacheValidSeconds: number = 180, exerciseId?: string): Promise<void> {
  // First check whether the cache is still valid or has expired
  let cacheExpired: boolean;
  if (!(cacheExpired = cacheValidSeconds === 0)) {
    const timestamp = exerciseId ? toRaw(store).exercises.get(exerciseId)?.timestamp : toRaw(store).timestamp;
    cacheExpired = !timestamp || timestamp < Date.now() - 1000 * cacheValidSeconds;
  }

  // Refresh only if cache has expired
  if (cacheExpired) {
    if (exerciseId) {
      // Update a single exercise (including details data)
      const exercise = await getExercise(exerciseId);
      if (exercise) {
        // Exercise exists -> update it
        storeExercise(exercise);
      } else {
        // Exercise does not exist -> delete it
        store.exercises.delete(exerciseId);
      }
    } else {
      // Update all exercises (load without additional data, but keep what we already have)
      // Get the old map as source for existing additional data that we want to preserve
      const oldExercises = toRaw(store).exercises;
      // Fetch exercises from API and build a fresh map (to get rid of deleted exercises)
      const newExercises = new Map<string, ExerciseWithOptionalData>();
      for (const exercise of await getExercises()) {
        const id = exercise.exerciseId;
        newExercises.set(id, {
          // the old map entry might have additional data that we want to keep
          ...oldExercises.get(id),
          // update data from API result
          ...exercise,
        });
      }
      store.exercises = newExercises;
      store.timestamp = Date.now();
    }
  }
}

export async function joinExercise(exerciseId: string): Promise<ExerciseWithDetails> {
  const exercise = await apiJoinExercise(exerciseId);
  storeExercise(exercise);
  return exercise;
}
