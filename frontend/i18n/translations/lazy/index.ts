// Lazy translations loading. This file just provides a type and utility function.

// Lazy loading happens in the files next to this one. The file names should be the same as in
// src/router/lazy/*.ts, where the lazy translations required for a lazy route component must be
// in a file with the same name, such that they and up in the same chunk.

import type { Translations } from '..';

export type Lazy<T> = () => Promise<T>;

export function lazy<T extends Translations>(x: Lazy<{ default: T }>): Lazy<T> {
  return async () => (await x()).default;
}
