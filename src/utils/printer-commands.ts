/**
 * Comprehensive ESC/POS command set for thermal printers.
 * Provides English descriptions, hex values, and TypeScript IntelliSense support.
 * Useful for building raw printer commands (e.g., printing text, barcodes, cash drawer routing).
 */

/** Horizontal line templates for printing receipts */
export interface HorizontalLine {
  /** 58mm layout: 34 equal signs ('=') */
  readonly HR_58MM: string;
  /** 58mm layout: 34 asterisks ('*') */
  readonly HR2_58MM: string;
  /** 58mm layout: 34 hyphens ('-') */
  readonly HR3_58MM: string;
  /** 80mm layout: 48 equal signs ('=') */
  readonly HR_80MM: string;
  /** 80mm layout: 48 asterisks ('*') */
  readonly HR2_80MM: string;
  /** 80mm layout: 48 hyphens ('-') */
  readonly HR3_80MM: string;
}

/** Printer feed and cursor positioning control sequences */
export interface FeedControlSequences {
  /** Line Feed (LF): Prints the current buffer and advances paper by one line */
  readonly CTL_LF: string;
  /** Form Feed (FF): Prints the buffer and advances paper to the next top of form (depending on printer support) */
  readonly CTL_FF: string;
  /** Carriage Return (CR): Moves the print position to the beginning of the current print line */
  readonly CTL_CR: string;
  /** Horizontal Tab (HT): Moves the print position to the next horizontal tab position */
  readonly CTL_HT: string;
  /** Vertical Tab (VT): Moves the print position to the next vertical tab position */
  readonly CTL_VT: string;
}

/** Specific controls for adjusting vertical spacing between printed lines */
export interface LineSpacing {
  /** Reverts the line spacing to the printer's factory default (typically 1/6 inch or about 3.75mm) */
  readonly LS_DEFAULT: string;
  /**
   * Sets the line spacing using a specific dot count (ESC 3 n).
   * `n` defines the **total height of a single line** (character height + whitespace below) in dots.
   *
   * @param n Dot count for line height (Range: 0 - 255).
   * - Values **< 14** often have no visible effect if the standard font is ~12-14 dots high; the printer enforces a physical minimum limit.
   * - Default is usually around **14** depending on the specific printer firmware.
   * - Smaller values -> tighter line spacing, Larger values -> more white space between lines.
   * @example
   * LS_SET(14) // Standard line spacing
   * LS_SET(30) // Double/Spaced out line spacing
   * @returns The corresponding ESC/POS command string combined with the given padding length.
   */
  readonly LS_SET: (n: number) => string;
}

/** Printer hardware lifecycle controls */
export interface Hardware {
  /** Initialize Printer (ESC @): Clears all print buffers, resets settings to defaults, and prepares the printer */
  readonly HW_INIT: string;
  /** Select Peripheral (ESC = n): Frequently used to wake up the printer or address a specific printer in a multi-device setup */
  readonly HW_SELECT: string;
  /** Hardware Reset (ESC ? LF NUL): Performs a hard reset of the printer hardware */
  readonly HW_RESET: string;
}

/** Commands to open external cash drawers connected to the printer via RJ11/RJ12 */
export interface CashDrawer {
  /** Send kick pulse to Pin 2: Opens the drawer connected to connector pin 2 */
  readonly CD_KICK_2: string;
  /** Send kick pulse to Pin 5: Opens the drawer connected to connector pin 5 */
  readonly CD_KICK_5: string;
}

/** Configuration for printing alignment boundaries */
export interface Margins {
  /** Set Bottom Margin (ESC O n) – Behavior varies deeply by hardware vendor */
  readonly BOTTOM: string;
  /** Set Left Margin (ESC l n) – Sets the left print boundary */
  readonly LEFT: string;
  /** Set Right Margin (ESC Q n) – Sets the right print boundary */
  readonly RIGHT: string;
}

/** Auto-cutter sequences for models with physical cutter blades */
export interface Paper {
  /** Full Cut (GS V 0): Completely slices the paper off */
  readonly PAPER_FULL_CUT: string;
  /** Partial Cut (GS V 1): Slices the paper but leaves a small attach point in the center */
  readonly PAPER_PART_CUT: string;
  /** Cut Mode A (GS V 65): Feeds paper to the cutting position and executes a full cut */
  readonly PAPER_CUT_A: string;
  /** Cut Mode B (GS V 66): Feeds paper to the cutting position and executes a partial cut */
  readonly PAPER_CUT_B: string;
}

/** Multipliers mapping used for scaling text sizes from 1x to 8x */
export type EightSteps = {
  readonly 1: string;
  readonly 2: string;
  readonly 3: string;
  readonly 4: string;
  readonly 5: string;
  readonly 6: string;
  readonly 7: string;
  readonly 8: string;
};

/** Text styling, sizes, and fonts */
export interface TextFormat {
  // --- Basic Text Size Commands (ESC !) ---
  /** Normal size text (1x width, 1x height) */
  readonly TXT_NORMAL: string;
  /** Double height text (1x width, 2x height) */
  readonly TXT_2HEIGHT: string;
  /** Double width text (2x width, 1x height) */
  readonly TXT_2WIDTH: string;
  /** Quadruple size text (2x width, 2x height) */
  readonly TXT_4SQUARE: string;

  /**
   * Defines a custom text size using multipliers (GS ! n).
   *
   * @param width Width multiplier coefficient (1 to 8).
   * @param height Height multiplier coefficient (1 to 8).
   * @returns The dynamically calculated ESC/POS size command.
   */
  readonly TXT_CUSTOM_SIZE: (width: number, height: number) => string;

  // --- Granular Scaling Lookup Maps ---
  /** Maps integer multi-factors (1..8) to their specific GS ! height hex bits */
  readonly TXT_HEIGHT: EightSteps;
  /** Maps integer multi-factors (1..8) to their specific GS ! width hex bits */
  readonly TXT_WIDTH: EightSteps;

  // --- Text Decoration & Styling ---
  /** Underline mode: OFF */
  readonly TXT_UNDERL_OFF: string;
  /** Underline mode: 1-dot thickness */
  readonly TXT_UNDERL_ON: string;
  /** Underline mode: 2-dots thickness (Thick) */
  readonly TXT_UNDERL2_ON: string;
  /** Emphasized (Bold) mode: OFF */
  readonly TXT_BOLD_OFF: string;
  /** Emphasized (Bold) mode: ON */
  readonly TXT_BOLD_ON: string;
  /** Italic tracking mode: OFF (Note: Not all printers physically support italics) */
  readonly TXT_ITALIC_OFF: string;
  /** Italic tracking mode: ON */
  readonly TXT_ITALIC_ON: string;

  // --- Font Selection ---
  /** Select Font A: Usually standard 12x24 dots character matrix */
  readonly TXT_FONT_A: string;
  /** Select Font B: Usually compressed 9x17 dots character matrix */
  readonly TXT_FONT_B: string;
  /** Select Font C: Specialized or user-defined font (hardware dependent) */
  readonly TXT_FONT_C: string;

  // --- Text Alignment (ESC a n) ---
  /** Justify text to the Left boundary */
  readonly TXT_ALIGN_LT: string;
  /** Justify text to the Center */
  readonly TXT_ALIGN_CT: string;
  /** Justify text to the Right boundary */
  readonly TXT_ALIGN_RT: string;
}

/**
 * Interface representing the complete unified COMMANDS library.
 */
export interface Commands {
  readonly BASIC: {
    /** ASCII Control: Line Feed (0A) */
    readonly LF: string;
    /** ASCII Control: Escape (1B) */
    readonly ESC: string;
    /** ASCII Control: File Separator (1C) */
    readonly FS: string;
    /** ASCII Control: Group Separator (1D) */
    readonly GS: string;
    /** ASCII Control: Unit Separator (1F) */
    readonly US: string;
    /** ASCII Control: Form Feed (0C) */
    readonly FF: string;
    /** ASCII Control: Data Link Escape (10) */
    readonly DLE: string;
    /** ASCII Control: Device Control 1 (11) */
    readonly DC1: string;
    /** ASCII Control: Device Control 4 (14) */
    readonly DC4: string;
    /** ASCII Control: End Of Transmission (04) */
    readonly EOT: string;
    /** ASCII Control: Null Character (00) */
    readonly NUL: string;
    /** Utility: Standard OS Newline (\n) */
    readonly EOL: string;
  };
  /** Commands for horizontal lines and borders */
  readonly HORIZONTAL_LINE: HorizontalLine;
  /** Sequences for line feed, carriage return, tabs, etc. */
  readonly FEED_CONTROL_SEQUENCES: FeedControlSequences;
  /** Commands to adjust the vertical space between lines */
  readonly LINE_SPACING: LineSpacing;
  /** System-level commands to initialize or reset hardware */
  readonly HARDWARE: Hardware;
  /** Commands to trigger cash drawer pulses */
  readonly CASH_DRAWER: CashDrawer;
  /** Commands to adjust print margins */
  readonly MARGINS: Margins;
  /** Cutter commands for full and partial slicing */
  readonly PAPER: Paper;
  /** Commands for styling, sizing, alignment, and fonts */
  readonly TEXT_FORMAT: TextFormat;
}

/**
 * Dictionary mapping containing all core ESC/POS constant hex strings.
 * Use IntelliSense under `COMMANDS.[PROPERTY]` to view specific English documentation and raw hex usages.
 * Note: Comments are heavily documented in the Types/Interfaces above to avoid duplication.
 */
export const COMMANDS: Commands = {
  BASIC: {
    /** ASCII Control: Line Feed (0A) */
    LF: "\x0a",
    /** ASCII Control: Escape (1B) */
    ESC: "\x1b",
    /** ASCII Control: File Separator (1C) */
    FS: "\x1c",
    /** ASCII Control: Group Separator (1D) */
    GS: "\x1d",
    /** ASCII Control: Unit Separator (1F) */
    US: "\x1f",
    /** ASCII Control: Form Feed (0C) */
    FF: "\x0c",
    /** ASCII Control: Data Link Escape (10) */
    DLE: "\x10",
    /** ASCII Control: Device Control 1 (11) */
    DC1: "\x11",
    /** ASCII Control: Device Control 4 (14) */
    DC4: "\x14",
    /** ASCII Control: End Of Transmission (04) */
    EOT: "\x04",
    /** ASCII Control: Null Character (00) */
    NUL: "\x00",
    /** Utility: Standard OS Newline (\n) */
    EOL: "\n",
  } as const,

  HORIZONTAL_LINE: {
    /** 58mm layout: 34 equal signs ('=') */
    HR_58MM: "==================================",
    /** 58mm layout: 34 asterisks ('*') */
    HR2_58MM: "**********************************",
    /** 58mm layout: 34 hyphens ('-') */
    HR3_58MM: "----------------------------------",
    /** 80mm layout: 48 equal signs ('=') */
    HR_80MM: "================================================",
    /** 80mm layout: 48 asterisks ('*') */
    HR2_80MM: "************************************************",
    /** 80mm layout: 48 hyphens ('-') */
    HR3_80MM: "------------------------------------------------",
  } as const,

  FEED_CONTROL_SEQUENCES: {
    /** Line Feed (LF): Prints the current buffer and advances paper by one line */
    CTL_LF: "\x0a",
    /** Form Feed (FF): Prints the buffer and advances paper to the next top of form (depending on printer support) */
    CTL_FF: "\x0c",
    /** Carriage Return (CR): Moves the print position to the beginning of the current print line */
    CTL_CR: "\x0d",
    /** Horizontal Tab (HT): Moves the print position to the next horizontal tab position */
    CTL_HT: "\x09",
    /** Vertical Tab (VT): Moves the print position to the next vertical tab position */
    CTL_VT: "\x0b",
  } as const,

  LINE_SPACING: {
    /** Reverts the line spacing to the printer's factory default (typically 1/6 inch or about 3.75mm) */
    LS_DEFAULT: "\x1b\x32",
    /**
     * Sets the line spacing using a specific dot count (ESC 3 n).
     * `n` defines the **total height of a single line** (character height + whitespace below) in dots.
     *
     * @param n Dot count for line height (Range: 0 - 255).
     * - Values **< 14** often have no visible effect if the standard font is ~12-14 dots high; the printer enforces a physical minimum limit.
     * - Default is usually around **14** depending on the specific printer firmware.
     * - Smaller values -> tighter line spacing, Larger values -> more white space between lines.
     * @example
     * LS_SET(14) // Standard line spacing
     * LS_SET(30) // Double/Spaced out line spacing
     * @returns The corresponding ESC/POS command string combined with the given padding length.
     */
    LS_SET: function (n: number) {
      return "\x1b\x33" + String.fromCharCode(n);
    },
  },

  HARDWARE: {
    /** Initialize Printer (ESC @): Clears all print buffers, resets settings to defaults, and prepares the printer */
    HW_INIT: "\x1b\x40",
    /** Select Peripheral (ESC = n): Frequently used to wake up the printer or address a specific printer in a multi-device setup */
    HW_SELECT: "\x1b\x3d\x01",
    /** Hardware Reset (ESC ? LF NUL): Performs a hard reset of the printer hardware */
    HW_RESET: "\x1b\x3f\x0a\x00",
  } as const,

  CASH_DRAWER: {
    /** Send kick pulse to Pin 2: Opens the drawer connected to connector pin 2 */
    CD_KICK_2: "\x1b\x70\x00",
    /** Send kick pulse to Pin 5: Opens the drawer connected to connector pin 5 */
    CD_KICK_5: "\x1b\x70\x01",
  } as const,

  MARGINS: {
    /** Set Bottom Margin (ESC O n) – Behavior varies deeply by hardware vendor */
    BOTTOM: "\x1b\x4f",
    /** Set Left Margin (ESC l n) – Sets the left print boundary */
    LEFT: "\x1b\x6c",
    /** Set Right Margin (ESC Q n) – Sets the right print boundary */
    RIGHT: "\x1b\x51",
  } as const,

  PAPER: {
    /** Full Cut (GS V 0): Completely slices the paper off */
    PAPER_FULL_CUT: "\x1d\x56\x00",
    /** Partial Cut (GS V 1): Slices the paper but leaves a small attach point in the center */
    PAPER_PART_CUT: "\x1d\x56\x01",
    /** Cut Mode A (GS V 65): Feeds paper to the cutting position and executes a full cut */
    PAPER_CUT_A: "\x1d\x56\x41",
    /** Cut Mode B (GS V 66): Feeds paper to the cutting position and executes a partial cut */
    PAPER_CUT_B: "\x1d\x56\x42",
  } as const,

  TEXT_FORMAT: {
    // ---- TEXT SIZE ----
    /** Normal size text (1x width, 1x height) */
    TXT_NORMAL: "\x1b\x21\x00",
    /** Double height text (1x width, 2x height) */
    TXT_2HEIGHT: "\x1b\x21\x10",
    /** Double width text (2x width, 1x height) */
    TXT_2WIDTH: "\x1b\x21\x20",
    /** Quadruple size text (2x width, 2x height) */
    TXT_4SQUARE: "\x1b\x21\x30",

    /**
     * Defines a custom text size using multipliers (GS ! n).
     *
     * @param width Width multiplier coefficient (1 to 8).
     * @param height Height multiplier coefficient (1 to 8).
     * @returns The dynamically calculated ESC/POS size command.
     */
    TXT_CUSTOM_SIZE: function (width: number, height: number) {
      const widthDec = (width - 1) * 16;
      const heightDec = height - 1;
      const sizeDec = widthDec + heightDec;
      return "\x1d\x21" + String.fromCharCode(sizeDec);
    },

    // ---- TEXT HEIGHT OPTIONS ----
    /** Maps integer multi-factors (1..8) to their specific GS ! height hex bits */
    TXT_HEIGHT: {
      1: "\x00",
      2: "\x01",
      3: "\x02",
      4: "\x03",
      5: "\x04",
      6: "\x05",
      7: "\x06",
      8: "\x07",
    } as const,

    // ---- TEXT WIDTH OPTIONS ----
    /** Maps integer multi-factors (1..8) to their specific GS ! width hex bits */
    TXT_WIDTH: {
      1: "\x00",
      2: "\x10",
      3: "\x20",
      4: "\x30",
      5: "\x40",
      6: "\x50",
      7: "\x60",
      8: "\x70",
    } as const,

    // ---- TEXT DECORATION ----
    /** Underline mode: OFF */
    TXT_UNDERL_OFF: "\x1b\x2d\x00",
    /** Underline mode: 1-dot thickness */
    TXT_UNDERL_ON: "\x1b\x2d\x01",
    /** Underline mode: 2-dots thickness (Thick) */
    TXT_UNDERL2_ON: "\x1b\x2d\x02",
    /** Emphasized (Bold) mode: OFF */
    TXT_BOLD_OFF: "\x1b\x45\x00",
    /** Emphasized (Bold) mode: ON */
    TXT_BOLD_ON: "\x1b\x45\x01",
    /** Italic tracking mode: OFF (Note: Not all printers physically support italics) */
    TXT_ITALIC_OFF: "\x1b\x35",
    /** Italic tracking mode: ON */
    TXT_ITALIC_ON: "\x1b\x34",

    // ---- FONT TYPES ----
    /** Select Font A: Usually standard 12x24 dots character matrix */
    TXT_FONT_A: "\x1b\x4d\x00",
    /** Select Font B: Usually compressed 9x17 dots character matrix */
    TXT_FONT_B: "\x1b\x4d\x01",
    /** Select Font C: Specialized or user-defined font (hardware dependent) */
    TXT_FONT_C: "\x1b\x4d\x02",

    // ---- TEXT ALIGNMENT ----
    /** Justify text to the Left boundary */
    TXT_ALIGN_LT: "\x1b\x61\x00",
    /** Justify text to the Center */
    TXT_ALIGN_CT: "\x1b\x61\x01",
    /** Justify text to the Right boundary */
    TXT_ALIGN_RT: "\x1b\x61\x02",
  },
};

export const BASIC = COMMANDS.BASIC;
export const HORIZONTAL_LINE = COMMANDS.HORIZONTAL_LINE;
export const FEED_CONTROL_SEQUENCES = COMMANDS.FEED_CONTROL_SEQUENCES;
export const LINE_SPACING = COMMANDS.LINE_SPACING;
export const HARDWARE = COMMANDS.HARDWARE;
export const CASH_DRAWER = COMMANDS.CASH_DRAWER;
export const MARGINS = COMMANDS.MARGINS;
export const PAPER = COMMANDS.PAPER;
export const TEXT_FORMAT = COMMANDS.TEXT_FORMAT;

// ==================== EXPORT ALL ====================
export const BREAK_LINE = BASIC.EOL;
export const UNDERLINE_OFF = TEXT_FORMAT.TXT_UNDERL_OFF;
export const UNDERLINE_ON = TEXT_FORMAT.TXT_UNDERL_ON;
export const UNDERLINE_2DOT_ON = TEXT_FORMAT.TXT_UNDERL2_ON;
export const BOLD_OFF = TEXT_FORMAT.TXT_BOLD_OFF;
export const BOLD_ON = TEXT_FORMAT.TXT_BOLD_ON;
export const ITALIC_OFF = TEXT_FORMAT.TXT_ITALIC_OFF;
export const ITALIC_ON = TEXT_FORMAT.TXT_ITALIC_ON;

// Font types
export const FONT_A = TEXT_FORMAT.TXT_FONT_A;
export const FONT_B = TEXT_FORMAT.TXT_FONT_B;
export const FONT_C = TEXT_FORMAT.TXT_FONT_C;

// Text alignment
export const ALIGN_LEFT = TEXT_FORMAT.TXT_ALIGN_LT;
export const ALIGN_CENTER = TEXT_FORMAT.TXT_ALIGN_CT;
export const ALIGN_RIGHT = TEXT_FORMAT.TXT_ALIGN_RT;
