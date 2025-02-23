import { expectStatus, extractJson, get, postJson } from './fetch';
import type { AccountData, Language, SetLanguageRequest } from './types';

export function getAccountData(): Promise<AccountData | null> {
  return get('api/account')
    .then(expectStatus(200))
    .then(extractJson<AccountData | null>);
}

export async function setLanguage(language: Language): Promise<void> {
  await postJson('api/account/language', { language } satisfies SetLanguageRequest).then(expectStatus(200));
}

// export async function changePassword(oldPassword:string,password: string): Promise<void> {
//   const data: Password = { oldPassword, password,};
//   await postJson('api/account/password', data).then(expectStatus(200));
// }

export async function changePassword(oldPassword: string, password: string): Promise<string> {
  const data: any = { oldPassword: oldPassword, password: password };
  try {
    const response = await postJson('api/account/password', data);
    expectStatus(200)(response); // This will throw an error if the status is not 200
    return 'Password Changed Successfully!'; // Return a success message
  } catch (error) {
    if (error instanceof Response && error.status === 400) {
      throw new Error('Incorrect old password.'); // Throw a specific error message
    } else if (error instanceof Error) {
      throw new Error(error.message); // Throw a generic error message
    } else {
      throw new Error('An error occurred while changing the password.');
    }
  }
}
