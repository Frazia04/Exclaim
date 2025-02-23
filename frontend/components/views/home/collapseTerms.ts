import { reactive } from 'vue';

import type { Exercise, Term } from '../../../api';
import { termKey } from '../../../utils/term';

export type CollapsedTerms = {
  toggleExpandTerm: (term: Term) => void;
  isExpanded: (term: Term) => boolean;
  expandMostRecentTerm: (exercises: Exercise[] | undefined) => void;
};

export function useCollapsedTerms(): CollapsedTerms {
  const expandedTerms = reactive(new Set<string>());

  function toggleExpandTerm(term: Term): void {
    const key = termKey(term);
    expandedTerms.delete(key) || expandedTerms.add(key);
  }

  function isExpanded(term: Term): boolean {
    return expandedTerms.has(termKey(term));
  }

  function expandMostRecentTerm(exercises: Exercise[] | undefined): void {
    if (exercises?.length) {
      expandedTerms.add(termKey(exercises[0].term));
    }
  }

  return {
    toggleExpandTerm,
    isExpanded,
    expandMostRecentTerm,
  };
}
