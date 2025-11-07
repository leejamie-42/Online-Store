import { describe, it, expect, beforeEach, vi } from "vitest";
import { storage, tokenStorage } from "../storage";

describe("Storage Utilities", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  describe("storage.get", () => {
    it("should retrieve and parse value", () => {
      const data = { id: 1, name: "Test" };
      localStorage.setItem("test", JSON.stringify(data));

      expect(storage.get("test")).toEqual(data);
    });

    it("should return null for non-existent key", () => {
      expect(storage.get("nonexistent")).toBeNull();
    });

    it("should return null for invalid JSON", () => {
      // Suppress expected console error
      const consoleError = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      localStorage.setItem("invalid", "invalid-json-{");
      expect(storage.get("invalid")).toBeNull();

      consoleError.mockRestore();
    });
  });

  describe("tokenStorage", () => {
    it("should store and retrieve access token", () => {
      const token = "eyJhbGc...";
      tokenStorage.setToken(token);
      expect(tokenStorage.getToken()).toBe(token);
    });

    it("should store and retrieve refresh token", () => {
      const token = "eyJhbGc...";
      tokenStorage.setRefreshToken(token);
      expect(tokenStorage.getRefreshToken()).toBe(token);
    });

    it("should clear all tokens", () => {
      tokenStorage.setToken("access");
      tokenStorage.setRefreshToken("refresh");

      tokenStorage.clearAll();

      expect(tokenStorage.getToken()).toBeNull();
      expect(tokenStorage.getRefreshToken()).toBeNull();
    });
  });
});
