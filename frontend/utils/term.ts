import type { Exercise, Term } from '../api';
import { t } from '../i18n';

/**
 * Compute the i18n-aware representation of the given term.
 * @param term the term to represent
 * @returns the i18n-aware representation of the given term
 */
export function termString({ year, term, comment }: Term): string {
  let result = '';
  if (term) {
    result += t(`common.term-${term}`) + ' ' + year;
    if (term === 'winter') {
      const nextYearShort = (year + 1) % 100;
      result += '/' + (nextYearShort === 0 ? year + 1 : (nextYearShort < 10 ? '0' : '') + nextYearShort);
    }
  } else if (year) {
    result += year;
  }
  if (comment) {
    result += result ? ` (${comment})` : comment;
  }
  return result;
}

/**
 * Compute an i18n-independent string representation of the given term, to be used e.g. as key in a map.
 * @param term the term to represent
 * @returns an i18n-independent string representation of the given term
 */
export function termKey({ year, term, comment }: Term): string {
  return `${year}_${term ?? ''}_${comment}`;
}

/**
 * Check whether the two given terms are equal.
 * @param t1 first term
 * @param t2 second term
 * @returns whether the two given terms are equal
 */
export function termEquals(t1: Term, t2: Term): boolean {
  return t1.year === t2.year && t1.term === t2.term && t1.comment === t2.comment;
}

/**
 * Compare the two given terms by time.
 * @param t1 first term
 * @param t2 second term
 * @returns `< 0` / `0` / `> 0` if `t1` is earlier / equal to / later than `t2`
 */
export function compareTerm(t1: Term, t2: Term): number {
  return (
    // compare year: numerically
    t1.year - t2.year ||
    // compare term: summer, winter, null
    (t1.term ?? 'z').charCodeAt(0) - (t2.term ?? 'z').charCodeAt(0) ||
    // compare comment: lexicographically
    t1.comment.localeCompare(t2.comment)
  );
}

/**
 * Compare the two given exercises by time: order more recent terms first, order equal terms by exercise id.
 * @param e1 first exercise
 * @param e2 second exercise
 * @returns `< 0` / `0` / `> 0` if `e1` is more recent / equal / less recent than `e2`
 */
export function compareExerciseByTerm(e1: Exercise, e2: Exercise): number {
  return (
    // compare term/year: more recent terms first
    compareTerm(e2.term, e1.term) ||
    // if same term: compare id lexicographically
    e1.exerciseId.localeCompare(e2.exerciseId)
  );
}

/**
 * Group exercises by term. **Provided exercises must already be sorted by term!**
 * @param exercises Exercises (already sorted by term)
 * @returns pairs of term and exercises list
 */
export function groupByTerm(exercises: Exercise[]): [Term, Exercise[]][] {
  const result: [Term, Exercise[]][] = [];
  let currentTerm: Term | null = null;
  let currentList: Exercise[] = [];
  for (const exercise of exercises) {
    if (currentTerm && termEquals(currentTerm, exercise.term)) {
      currentList.push(exercise);
    } else {
      currentTerm = exercise.term;
      currentList = [exercise];
      result.push([currentTerm, currentList]);
    }
  }
  return result;
}
