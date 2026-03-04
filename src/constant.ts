export enum EDevicesPrinter {
  usb = 'usb',
  net = 'net',
  ble = 'ble',
}
export enum EPrintColumnType {
  COLUMN = 'COLUMN',
  RAW = 'RAW',
}
export enum IPaperWidth {
  '58mm' = 58,
  '80mm' = 80,
}
export interface IPrinterWidth {
  width: number;
  charPerLine: number;
}
export const PrinterWidth: Record<IPaperWidth, IPrinterWidth> = {
  [IPaperWidth['58mm']]: { width: 384, charPerLine: 32 },
  [IPaperWidth['80mm']]: { width: 576, charPerLine: 48 },
};

/**
 * Horizontal alignment for text and image output.
 * Use this for all alignment needs going forward.
 */
export type Alignment = 'left' | 'center' | 'right';

/**
 * @deprecated Use {@link Alignment} (string literal type) instead.
 * `ColumnAlignment` will be removed in a future major version.
 */
export enum ColumnAlignment {
  LEFT,
  CENTER,
  RIGHT,
}
export enum RN_THERMAL_PRINTER_EVENTS {
  EVENT_NET_PRINTER_SCANNED_SUCCESS = 'scannerResolved',
  EVENT_NET_PRINTER_SCANNING = 'scannerRunning',
  EVENT_NET_PRINTER_SCANNED_ERROR = 'registerError',
}
