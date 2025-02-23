import { faker } from '@faker-js/faker';
import { Response } from 'miragejs';

import type { AccountData, Language } from '../../api';
import { languages } from '../../i18n';
import type { DB } from '.';

export type DBUser = Omit<AccountData, 'isAssistant' | 'isTutor'>;

export function seed(): Map<number, DBUser> {
  faker.seed(0);
  const users = new Map<number, DBUser>();

  // Keep track of usernames and student ids we have already used
  const usernames = new Set<string>();
  const studentIds = new Set<string>();

  for (let userId = 1; userId <= 500; userId++) {
    // Generate names
    const firstname = faker.person.firstName();
    const lastname = faker.person.lastName();
    let username: string | null = faker.internet.userName({ firstName: firstname, lastName: lastname });

    // Check whether username is unique, otherwise account has no username
    if (usernames.has(username)) {
      username = null;
    } else {
      usernames.add(username);
    }

    // Generate a student id for 80% of the accounts, but ensure that it is unique
    let studentId: string | null = null;
    if (faker.number.float() < 0.8) {
      studentId = String(faker.number.int({ min: 100000, max: 999999 }));
      if (studentIds.has(studentId)) {
        studentId = null;
      } else {
        studentIds.add(studentId);
      }
    }

    users.set(userId, {
      userId,
      username,
      firstname,
      lastname,
      studentId,
      email: faker.internet.email({ firstName: firstname, lastName: lastname }),
      language: faker.helpers.arrayElement([...languages, null]),

      // Every tenths account is an admin
      isAdmin: userId % 10 === 1,
    });
  }

  return users;
}

export function toAccountData(db: DB, user: DBUser): AccountData {
  return {
    ...user,
    isTutor: [...db.exercises.values()].some(({ groups }) =>
      [...groups.values()].some(({ tutors }) => tutors.has(user.userId)),
    ),
    isAssistant: [...db.exercises.values()].some(({ assistants }) => assistants.has(user.userId)),
  };
}

export function fetchAccountData(db: DB, userId?: number | null): AccountData | null {
  const id = userId ?? db.userId;
  if (id !== null) {
    const user = db.users.get(id);
    if (user) {
      return toAccountData(db, user);
    }
  }
  return null;
}

export function setLanguage(db: DB, language: Language): Response {
  if (db.userId !== null) {
    const user = db.users.get(db.userId);
    if (user) {
      user.language = language;
      return new Response(200);
    }
  }
  return new Response(401);
}
