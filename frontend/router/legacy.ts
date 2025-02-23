/**
 * Navigate to a legacy page that is not part of our Vue application.
 * @param path the path to navigate to
 */
export function navigateLegacy(path: string): void {
  window.location.href = path;
}
