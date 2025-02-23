// Configure vue-router

import { ref } from 'vue';
import { createRouter, createWebHistory, type RouteComponent, type Router, type RouteRecordRaw } from 'vue-router';

import { redirectToOrFromLogin } from './redirect-to-login';
import { buildRoutes } from './routes';

/**
 * Flag indicating that a lazy route is currently loading.
 */
export const loading = ref(false);

export default function setup(): Router {
  // Collect all routes
  const routes = buildRoutes();
  guardLazyRoutes(routes);

  // Create the router instance
  const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior: () => ({ left: 0, top: 0 }),
  });

  // Add guard for redirecting unauthenticated requests to login and authenticated requests to home
  router.beforeEach(redirectToOrFromLogin);

  // Unset the loading flag that was added by the guards injected with `guardLazyRoutes`
  router.afterEach(() => {
    loading.value = false;
  });

  return router;
}

/**
 * Guard lazy loading routes such that `loading.value` is set to `true` before asynchronously
 * loading the view component.
 * @param routes the routes to guard
 * @see https://router.vuejs.org/guide/advanced/lazy-loading.html
 */
function guardLazyRoutes(routes?: RouteRecordRaw[]): void {
  // Helper that takes an object and a key, such that object[key] is the component to guard.
  function guardComponent<K extends string>(object: Partial<Record<K, RouteRecordRaw['component']>>, key: K): void {
    const component = object[key];
    if (
      // Lazy routes are () => Promise<RouteComponent>
      typeof component === 'function' &&
      // distinguish from functional components
      !('displayName' in component)
    ) {
      // Even though we expect no function arguments, we still pass them on to the guarded function
      // in order to not break anything if that assumption changes.
      object[key] = (...args: []) => {
        loading.value = true;
        return (component as () => Promise<RouteComponent>)(...args);
      };
    }
  }

  routes?.forEach((route) => {
    // Guard route.component
    guardComponent(route, 'component');

    // Guard each entry in route.components
    const components = route.components;
    if (components) {
      Object.keys(components).forEach((key) => guardComponent(components, key));
    }

    // Recursively guard child routes
    guardLazyRoutes(route.children);
  });
}
