import { expectStatus, extractJson, get } from './fetch';
import type { Configuration } from './types';

export function getConfiguration(): Promise<Configuration> {
  return get('api/configuration')
    .then(expectStatus(200))
    .then(extractJson<Configuration>);
}
