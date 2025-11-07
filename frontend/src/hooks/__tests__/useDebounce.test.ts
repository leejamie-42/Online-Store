/**
 * useDebounce Hook Tests
 */

import { describe, expect, it } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { useDebounce } from "../useDebounce";

describe("useDebounce", () => {
  it("returns initial value immediately", () => {
    const { result } = renderHook(() => useDebounce("test", 100));

    expect(result.current).toBe("test");
  });

  it("debounces value changes", async () => {
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      {
        initialProps: { value: "initial", delay: 100 },
      }
    );

    expect(result.current).toBe("initial");

    // Update value
    rerender({ value: "updated", delay: 100 });

    // Value should still be 'initial' immediately after update
    expect(result.current).toBe("initial");

    // Wait for debounce delay to complete
    await waitFor(
      () => {
        expect(result.current).toBe("updated");
      },
      { timeout: 200 }
    );
  });

  it("cancels previous timeout on rapid changes", async () => {
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      {
        initialProps: { value: "first", delay: 100 },
      }
    );

    // Rapid updates
    rerender({ value: "second", delay: 100 });
    rerender({ value: "third", delay: 100 });

    // Should still be 'first' immediately
    expect(result.current).toBe("first");

    // Wait for debounce - should skip to 'third'
    await waitFor(
      () => {
        expect(result.current).toBe("third");
      },
      { timeout: 200 }
    );
  });

  it("uses custom delay", async () => {
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      {
        initialProps: { value: "initial", delay: 150 },
      }
    );

    rerender({ value: "updated", delay: 150 });

    // Should still be initial immediately
    expect(result.current).toBe("initial");

    // Wait for custom delay
    await waitFor(
      () => {
        expect(result.current).toBe("updated");
      },
      { timeout: 250 }
    );
  });

  it("works with different data types", async () => {
    const { result, rerender } = renderHook(
      ({ value }) => useDebounce(value, 100),
      {
        initialProps: { value: 123 },
      }
    );

    expect(result.current).toBe(123);

    rerender({ value: 456 });

    await waitFor(
      () => {
        expect(result.current).toBe(456);
      },
      { timeout: 200 }
    );
  });
});
