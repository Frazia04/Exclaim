// Mirage's own database and ORM features massively lack TypeScript support, so we setup our own little database here.
// The idea is to mock the whole back-end data storage such that we can dynamically generate responses reflecting changed made in the current session.
// We do not need any normal form guarantees here, chose what is suitable to provide the expected API.
// Use Faker.js to generate some dummy data such that the UI is not that empty.

import type { Response } from 'miragejs';

import type { AccountData, Exercise, ExerciseWithDetails, Language } from '../../api';
import { type DBExercise, fetchExercise, fetchExercises, joinExercise, seed as seedExercises } from './exercises';
import { type DBUser, fetchAccountData, seed as seedUsers, setLanguage, toAccountData } from './users';

type DBStorage = {
  // Currently logged-in user id
  userId: number | null;

  // Storage for database "tables"
  users: Map<number, DBUser>;
  exercises: Map<string, DBExercise>;
};

// Accessor functions
type DBFunctions = {
  toAccountData(user: DBUser): AccountData;
  fetchAccountData(userId?: number | null): AccountData | null;
  setLanguage(language: Language): Response;

  fetchExercises(): Exercise[];
  fetchExercise(exerciseId: string): ExerciseWithDetails | null;

  joinExercise(exerciseId: string): ExerciseWithDetails;
};

export type DB = DBStorage & DBFunctions;

export function setupDatabase(): DB {
  const users = seedUsers();
  const db: DBStorage = {
    userId: null,
    users,
    exercises: seedExercises(users),
  };
  console.debug('Mirage JS database:\n', db);

  return {
    ...db,

    toAccountData(user: DBUser): AccountData {
      return toAccountData(this, user);
    },
    fetchAccountData(userId?: number | null): AccountData | null {
      return fetchAccountData(this, userId);
    },
    setLanguage(language: Language): Response {
      return setLanguage(this, language);
    },

    fetchExercises(): Exercise[] {
      return fetchExercises(this);
    },
    fetchExercise(exerciseId: string): ExerciseWithDetails | null {
      return fetchExercise(this, exerciseId);
    },

    joinExercise(exerciseId: string): ExerciseWithDetails {
      return joinExercise(this, exerciseId);
    },
  };
}
