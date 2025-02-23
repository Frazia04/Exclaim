import { Response } from 'miragejs';

import type { LoginSuccess } from '../api';
import type { DB } from './db';

export function login(username: string | null, password: string | null, db: DB, expectedPassword: string): Response {
  if (username && password === expectedPassword) {
    // Find user by provided real username
    let user = [...db.users.values()].find((u) => u.username === username);

    // Otherwise find user by userId
    if (!user) {
      const matches = username.match(/^u(\d+)$/);
      if (matches) {
        user = db.users.get(parseInt(matches[1]));
      }
    }

    if (user) {
      // Perform the login
      db.userId = user.userId;

      // Send success response
      return new Response(200, {}, {
        csrf: 'foobar',
        accountData: db.toAccountData(user),
      } satisfies LoginSuccess);
    }
  }

  // Failed login. Send help message to console.
  console.warn(
    `Login failed! The password for all accounts is '${expectedPassword}' (without quotes).\n`,
    'Valid usernames derived from user ids:\n',
    [...db.users.keys()].sort((a, b) => a - b).map((userId) => `u${userId}`),
    '\nValid real usernames:\n',
    [...db.users.values()].map(({ username }) => username).filter((n) => n !== null),
  );

  // Send error response
  return new Response(401, { 'WWW-Authenticate': 'Form' });
}
