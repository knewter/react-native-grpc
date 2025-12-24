/**
 * Utility functions for TurboModule architecture detection.
 *
 * This module provides runtime detection of React Native's New Architecture
 * to enable backward-compatible module loading.
 */

declare global {
  var __turboModuleProxy: unknown;
}

/**
 * Detects if the app is running with TurboModules enabled (New Architecture).
 *
 * The `global.__turboModuleProxy` object is set by React Native when
 * TurboModules are enabled. This is the official way to detect the
 * New Architecture at runtime.
 *
 * @returns true if TurboModules are enabled, false otherwise
 */
export function isTurboModuleEnabled(): boolean {
  return global.__turboModuleProxy != null;
}

/**
 * Type guard to narrow the module type based on architecture.
 * Useful for conditional logic based on module capabilities.
 */
export function isTurboModule<T>(module: T | null): module is T {
  return module != null && isTurboModuleEnabled();
}
