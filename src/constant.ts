export enum EDevicesPrinter {
  usb = "usb",
  net = "net",
  ble = "ble",
}
export enum EPrintColumnType {
  COLUMN = "COLUMN",
  RAW = "RAW",
}
export enum IPaperWidth {
  "58mm" = 58,
  "80mm" = 80,
}
export interface IPrinterWidth {
  width: number;
  charPerLine: number;
}
export const PrinterWidth: Record<IPaperWidth, IPrinterWidth> = {
  [IPaperWidth["58mm"]]: { width: 384, charPerLine: 32 },
  [IPaperWidth["80mm"]]: { width: 576, charPerLine: 48 },
};

export enum ColumnAlignment {
  LEFT,
  CENTER,
  RIGHT,
}
export enum RN_THERMAL_PRINTER_EVENTS {
  EVENT_NET_PRINTER_SCANNED_SUCCESS = "scannerResolved",
  EVENT_NET_PRINTER_SCANNING = "scannerRunning",
  EVENT_NET_PRINTER_SCANNED_ERROR = "registerError",
}
