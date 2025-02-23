import { expectStatus, extractJson, post } from './fetch';
import type { ExerciseWithDetails } from './types';
import { encodeRFC3986URIComponent } from './utils';

export function joinExercise(exerciseId: string): Promise<ExerciseWithDetails> {
  return post(`api/exercise/${encodeRFC3986URIComponent(exerciseId)}/join`)
    .then(expectStatus(200, true))
    .then(extractJson<ExerciseWithDetails>);
}
