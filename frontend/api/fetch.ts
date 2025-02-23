import type { CsrfCheckFailed } from './types';

/**
 * Issue a GET request to the given path.
 * @param path the path, relative to the application context path
 * @returns the response
 */
export async function get(path: string): Promise<Response> {
  return await fetch(new URL(path, document.baseURI), { redirect: 'error' });
}

/**
 * Issue a POST request to the given path.
 * @param path the path, relative to the application context path
 * @param body the data to send in the request
 * @param retryOnCsrfError whether the request should be retried with updated csrf token if an csrf error occurs
 * @returns the response
 */
async function postBody(
  path: string,
  contentType: string | null,
  body: BodyInit | null,
  retryOnCsrfError: boolean = true,
): Promise<Response> {
  const response = await fetch(new URL(path, document.baseURI), {
    method: 'POST',
    redirect: 'error',
    headers: {
      ...(contentType ? { 'Content-Type': contentType } : {}),
      ...(window.csrfToken ? { 'X-CSRF-TOKEN': window.csrfToken } : {}),
    },
    body,
  });

  // Detect and handle CSRF errors
  if (retryOnCsrfError && response.status === 403) {
    let error: unknown;
    let csrf: string | null;
    try {
      ({ error, csrf } = (await response.json()) as CsrfCheckFailed);
    } catch (ignored) {
      return response; // if json() throws then it is not a valid CSRF error response
    }
    if (error === ('csrf' satisfies CsrfCheckFailed['error'])) {
      window.csrfToken = csrf;
      return postBody(path, contentType, body, false);
    }
  }

  return response;
}

/**
 * Issue a POST request to the given path. No data is sent in the request.
 * @param path the path, relative to the application context path
 * @param retryOnCsrfError whether the request should be retried with updated csrf token if an csrf error occurs
 * @returns the response
 */
export function post(path: string, retryOnCsrfError: boolean = true): Promise<Response> {
  return postBody(path, null, null, retryOnCsrfError);
}

/**
 * Issue a POST request to the given path. Encode the data as URLSearchParams.
 * @param path the path, relative to the application context path
 * @param data the data to send in the request
 * @param retryOnCsrfError whether the request should be retried with updated csrf token if an csrf error occurs
 * @returns the response
 */
export function postURLSearchParams<T extends Record<string, string>>(
  path: string,
  data: T,
  retryOnCsrfError: boolean = true,
): Promise<Response> {
  return postBody(path, 'application/x-www-form-urlencoded', new URLSearchParams(data).toString(), retryOnCsrfError);
}

/**
 * Issue a POST request to the given path. Encode the data as JSON.
 * @param path the path, relative to the application context path
 * @param data the data to send in the request
 * @param retryOnCsrfError whether the request should be retried with updated csrf token if an csrf error occurs
 * @returns the response
 */
export function postJson<T>(path: string, data: T, retryOnCsrfError: boolean = true): Promise<Response> {
  return postBody(path, 'application/json', JSON.stringify(data), retryOnCsrfError);
}

/**
 * Check that a response has a specific status code.
 * @param expectedStatus the expected status code(s) or a predicate for expected status codes
 * @param reloadOnUnauthenticatedError whether to reload the page if a 401 status code occurs.
 * 401 is "Unauthorized", but actually means that the user in not authenticated, i.e. the session has expired.
 * **Warning: This looses all state the user has entered in forms!**
 * @returns the response, if it has an expected status code
 * @throws `UnexpectedHttpStatusError` if the response has an unexpected status code
 */
export function expectStatus(
  expectedStatus: number | number[] | ((status: number) => boolean),
  reloadOnUnauthenticatedError: boolean = false,
): (response: Response) => Response {
  return (response) => {
    const actualStatus = response.status;
    let passed: boolean;
    switch (typeof expectedStatus) {
      case 'number':
        passed = actualStatus === expectedStatus;
        break;
      case 'function':
        passed = expectedStatus(actualStatus);
        break;
      default:
        passed = expectedStatus.includes(actualStatus);
    }
    if (!passed) {
      if (reloadOnUnauthenticatedError && actualStatus === 401) {
        window.location.reload();
        throw new UnexpectedHttpStatusError(actualStatus, true);
      }
      throw new UnexpectedHttpStatusError(actualStatus, false);
    }
    return response;
  };
}

export class UnexpectedHttpStatusError extends Error {
  public readonly status: number;
  public readonly reloading: boolean;

  constructor(status: number, reloading: boolean) {
    super(`Unexpected HTTP status ${status}`);
    this.name = 'UnexpectedHttpStatusError';
    this.status = status;
    this.reloading = reloading;
  }
}

/**
 * Parse the body of the given response to JSON.
 * @param response the response to read the body from
 * @returns parsed JSON data
 */
export function extractJson<T>(response: Response): Promise<T> {
  return response.json();
}
