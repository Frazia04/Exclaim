// Reactively access DOM attributes

import { customRef, type Ref } from 'vue';

/**
 * Create a reactive reference for a DOM attribute. The created reference allows to get and set the
 * attribute. Only updates through the returned ref trigger reactive effects.
 * @param domElement the DOM element where the attribute is located
 * @param domAttribute name of the attribute
 * @param initialValue the initial value to set the DOM attribute to
 */
export function createDomBackedRef<T extends string>(
  domElement: HTMLElement,
  domAttribute: string,
  initialValue: T,
): Ref<T> {
  // Read attribute from DOM
  function getDOMAttribute(): T {
    return domElement.getAttribute(domAttribute) as T;
  }

  // Write attribute to DOM
  function setDOMAttribute(newValue: T): void {
    domElement.setAttribute(domAttribute, newValue);
  }

  // Set the initial value
  setDOMAttribute(initialValue);

  return customRef<T>((track, trigger) => ({
    get() {
      track();
      return getDOMAttribute();
    },
    set(newValue) {
      if (newValue !== getDOMAttribute()) {
        setDOMAttribute(newValue);
        trigger();
      }
    },
  }));
}
