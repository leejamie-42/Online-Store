/**
 * Simulate API delay for realistic testing
 */
export const delay = (ms: number = 800): Promise<void> =>
  new Promise((resolve) => setTimeout(resolve, ms));
