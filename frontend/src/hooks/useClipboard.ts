/**
 * useClipboard Hook
 * Provides copy-to-clipboard functionality with user feedback
 */

import { useState, useCallback } from "react";

export interface UseClipboardReturn {
  copied: boolean;
  copy: (text: string) => Promise<void>;
  reset: () => void;
}

/**
 * useClipboard Hook
 * Provides copy-to-clipboard functionality with feedback
 *
 * @param timeout - Reset 'copied' state after this many ms (default: 2000)
 * @returns Object with copied state, copy function, and reset function
 */
export function useClipboard(timeout = 2000): UseClipboardReturn {
  const [copied, setCopied] = useState(false);

  const copy = useCallback(
    async (text: string) => {
      try {
        if (!navigator.clipboard) {
          // Fallback for older browsers
          const textArea = document.createElement("textarea");
          textArea.value = text;
          textArea.style.position = "fixed";
          textArea.style.opacity = "0";
          document.body.appendChild(textArea);
          textArea.select();
          document.execCommand("copy");
          document.body.removeChild(textArea);
        } else {
          await navigator.clipboard.writeText(text);
        }

        setCopied(true);

        // Auto-reset after timeout
        setTimeout(() => {
          setCopied(false);
        }, timeout);
      } catch (error) {
        console.error("Failed to copy text:", error);
        setCopied(false);
      }
    },
    [timeout],
  );

  const reset = useCallback(() => {
    setCopied(false);
  }, []);

  return { copied, copy, reset };
}
