// Guard routes for authenticated users, redirecting to the login route.
// We remember the original route such that we can restore it after login.

import { type RouteLocationNormalized, type RouteLocationRaw, type RouteRecordName } from 'vue-router';

import { authenticated } from '../store/account';
import { homeRouteName, loginRouteName } from './names';

// Names of routes that can be accessed by unauthenticated users
const unprotectedRouteNames = new Set<RouteRecordName>([loginRouteName]);

// The route that was accessed when the user got redirected to login
let savedRoute: RouteLocationRaw | null = null;

/**
 * Redirect the user to login, if unauthenticated and accessing a protected route.
 * Redirect the user to the home route, if authenticated and accessing the login route.
 * This function is to be used in a `beforeEach` navigation guard.
 * @param route the route being navigated to
 * @returns navigation guard result
 */
export function redirectToOrFromLogin(route: RouteLocationNormalized): undefined | RouteLocationRaw {
  const currentRouteName = route.name;
  if (authenticated.value) {
    if (currentRouteName === loginRouteName) {
      return { name: homeRouteName };
    }
  } else if (!currentRouteName || !unprotectedRouteNames.has(currentRouteName)) {
    savedRoute = route;
    return { name: loginRouteName };
  }
}

/**
 * Get the route a user should be redirected to after successful login.
 * @returns the route to redirect to
 */
export function getSuccessfulLoginRoute(): RouteLocationRaw {
  const route = savedRoute;
  if (route) {
    savedRoute = null;
    return route;
  }

  return { name: homeRouteName };
}
