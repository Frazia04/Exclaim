// Mirage JS is a library to mock the backend api for frontend development.
// It intercepts api requests and provides responses with fake data specified here.

// We include this library only in development builds and only when the developer
// does not provide the URL to a real backend that Vite can proxy api requests to.

import { createServer } from 'miragejs';

import type {
  AccountData,
  Configuration,
  Exercise,
  ExerciseWithDetails,
  LoginRequest,
  SetLanguageRequest,
} from '../api';
import { setupDatabase } from './db';
import { ok, parseParams } from './helpers';
import { login } from './login';

// Password that works for all user accounts
const expectedPassword = 'asdf';

const sampleConfiguration: Configuration = {
  samlRegistrationIds: ['SAML', 'RHRK', 'WhatEver'],
};

export function setupMirage(): void {
  const db = setupDatabase();

  createServer({
    routes() {
      this.get('/api/configuration', () => ok<Configuration>(sampleConfiguration));

      this.get('/api/account', () => ok<AccountData | null>(db.fetchAccountData()));

      this.post('/api/account/language', (schema, request) =>
        db.setLanguage(parseParams<SetLanguageRequest>(request.requestBody).language),
      );

      this.post('/api/login', (schema, request) => {
        const { username, password } = parseParams<LoginRequest>(request.requestBody);
        return login(username, password, db, expectedPassword);
      });

      this.get('/api/exercise', () => ok<Exercise[]>(db.fetchExercises()));

      this.post('/api/exercise/:exerciseId/join', (schema, request) =>
        ok<ExerciseWithDetails>(db.joinExercise(request.params.exerciseId)),
      );
    },
  });
}
