import { expectStatus, postURLSearchParams } from './fetch';
import type { AccountData, LoginSuccess } from './types';

export type LoginRequest = {
  readonly username: string;
  readonly password: string;
};

export async function login(request: LoginRequest): Promise<AccountData | null> {
  const response = await postURLSearchParams('api/login', request).then(expectStatus([200, 401]));

  if (response.status === 200) {
    const { csrf, accountData } = (await response.json()) as LoginSuccess;
    window.csrfToken = csrf;
    return accountData;
  }

  return null;
}
