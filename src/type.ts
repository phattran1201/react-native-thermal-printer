import { BLEPrinter, NetPrinter, USBPrinter } from ".";
import { Alignment, ColumnAlignment, EDevicesPrinter, EPrintColumnType, IPaperWidth } from "./constant";

/**
 * A flexible discriminated union representing a selected printer device.
 * Allows selecting any printer type (USB, BLE, Net) with the appropriate connection fields.
 * Use `printerType` to disambiguate which device fields are expected.
 */
export type IDevicesSelectPrinter =
  | ({ printerType: keyof typeof EDevicesPrinter } & Partial<IUSBPrinter & IBLEPrinter & INetPrinter>)
  | ({ printerType: EDevicesPrinter.usb } & IUSBPrinter)
  | ({ printerType: EDevicesPrinter.ble } & IBLEPrinter)
  | ({ printerType: EDevicesPrinter.net } & INetPrinter);

/**
 * A union type representing one of the three available printer module instances:
 * - `USBPrinter` – physical USB-connected printers
 * - `BLEPrinter` – Bluetooth Low Energy printers
 * - `NetPrinter` – network/TCP printers
 */
export type IDevicesPrinter =
  | Partial<typeof USBPrinter & typeof BLEPrinter & typeof NetPrinter>
  | typeof USBPrinter
  | typeof BLEPrinter
  | typeof NetPrinter;

/**
 * Parameters used for printing an expanded (formatted) column row.
 * Supports both simple value/amount pairs and configurable tab widths.
 */
export interface IPrintExpandColumnParams {
  /** The left-side label or main text to print */
  value: string;
  /** An optional right-side value or amount (e.g., price, quantity) */
  amount?: string | number;
  /** Paper width setting, controls column layout widths. Defaults to 58mm if omitted */
  paperWidth?: IPaperWidth;
  /** Custom tab width for spacing between columns (in characters) */
  tabWidth?: number;
  /** Custom tab value (e.g., separator or spacer between columns) */
  valueTab?: string | number;
}

/**
 * Represents a multi-column print row structure.
 * Each column has its own text, width, and alignment settings.
 */
export interface IPrintExpandColumn {
  /** Array of text strings, one per column */
  texts: string[];
  /** Array of widths (in characters) for each column */
  columnWidth: number[];
  /** Array of alignments (LEFT, CENTER, RIGHT) for each column */
  columnAlignment: (Alignment | ColumnAlignment)[];
  /** Optional array of ESC/POS style commands to apply to each column */
  columnStyle?: string[];
}

/**
 * Configuration options for standard text printing operations.
 */
export interface PrinterOptions {
  /** Whether to trigger a beep sound after printing (if printer supports it) */
  beep?: boolean;
  /** Whether to auto-cut the paper after printing */
  cut?: boolean;
  /** Whether to append tailing blank lines before cutting */
  tailingLine?: boolean;
  /** Character encoding to use for the print data (e.g., `'UTF-8'`, `'PC852'`) */
  encoding?: string;
  /** Callback invoked when a printing error occurs */
  onError?: (error: Error) => void;
}

/**
 * Configuration options for image printing operations.
 * Extends `PrinterOptions` with image-specific sizing and layout parameters.
 */
export interface PrinterImageOptions {
  /** Whether to trigger a beep sound after printing (if printer supports it) */
  beep?: boolean;
  /** Whether to auto-cut the paper after printing */
  cut?: boolean;
  /** Whether to append tailing blank lines before cutting */
  tailingLine?: boolean;
  /** Character encoding for any accompanying text */
  encoding?: string;
  /** Desired output width of the image in pixels */
  imageWidth?: number;
  /** Paper width setting used to calculate proper scaling */
  printerWidthType?: IPaperWidth;
  /**
   * Horizontal alignment of the image on the paper.
   * @default 'center'
   */
  align?: Alignment;
  /** iOS only: Horizontal padding offset (in pixels) for image positioning */
  paddingX?: number;
  /** Callback invoked when a printing error occurs */
  onError?: (error: Error) => void;
}

/**
 * Represents a USB-connected thermal printer device.
 * Fields typically obtained from Android USB device enumeration.
 */
export interface IUSBPrinter {
  /** Raw device descriptor string */
  device: string;
  /** Human-readable manufacturer name (e.g., `'Epson'`) */
  manufacturer_name: string;
  /** Human-readable product name (e.g., `'TM-T20'`) */
  product_name: string;
  /** System-level device name or path (e.g., `/dev/bus/usb/001/003`) */
  device_name: string;
  /** USB Vendor ID (decimal or hex string) */
  vendor_id: string;
  /** USB Product ID (decimal or hex string) */
  product_id: string;
}

/**
 * Represents a Bluetooth Low Energy (BLE) printer device.
 */
export interface IBLEPrinter {
  /** Raw device descriptor string */
  device: string;
  /** Friendly display name of the BLE device */
  device_name: string;
  /** MAC address of the BLE device (used for connection) */
  inner_mac_address: string;
}

/**
 * Represents a network (TCP/IP) printer device.
 */
export interface INetPrinter {
  /** Raw device descriptor string */
  device: string;
  /** Optional friendly display name of the network printer */
  device_name?: string;
  /** IP address or hostname of the printer */
  host: string;
  /** TCP port the printer is listening on (typically `9100`) */
  port: number;
}

/**
 * Discriminated union representing a single printable data item.
 * - `RAW` kind: a plain string key/value pair for direct printing.
 * - `COLUMN` kind: a structured multi-column row for tabular printing.
 */
export type IDataPrinter =
  | {
      /** Unique identifier for this print entry */
      key: string;
      /** Raw string content to send directly to the printer */
      value: string;
      /** Indicates this is a raw text item */
      kind: EPrintColumnType.RAW;
    }
  | {
      /** Unique identifier for this print entry */
      key: string;
      /** Array of column row definitions for structured printing */
      value: IPrintExpandColumn[];
      /** Indicates this is a multi-column layout item */
      kind: EPrintColumnType.COLUMN;
    };
