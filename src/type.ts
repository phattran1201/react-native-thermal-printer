import { BLEPrinter, NetPrinter, USBPrinter } from ".";
import { ColumnAlignment, EDevicesPrinter, IPaperWidth } from "./constant";

export type IDevicesSelectPrinter =
  | ({ printerType: keyof typeof EDevicesPrinter } & Partial<
      IUSBPrinter & IBLEPrinter & INetPrinter
    >)
  | ({ printerType: EDevicesPrinter.usb } & IUSBPrinter)
  | ({ printerType: EDevicesPrinter.ble } & IBLEPrinter)
  | ({ printerType: EDevicesPrinter.net } & INetPrinter);

export type IDevicesPrinter =
  | Partial<typeof USBPrinter & typeof BLEPrinter & typeof NetPrinter>
  | typeof USBPrinter
  | typeof BLEPrinter
  | typeof NetPrinter;

export interface IPrintExpandColumnParams {
  value: string;
  amount?: string | number;
  paperWidth?: IPaperWidth;
  tabWidth?: number;
  valueTab?: string | number;
}
export interface IPrintExpandColumn {
  texts: string[];
  columnWidth: number[];
  columnAlignment: ColumnAlignment[];
  columnStyle?: string[];
}

export interface PrinterOptions {
  beep?: boolean;
  cut?: boolean;
  tailingLine?: boolean;
  encoding?: string;
  onError?: (error: Error) => void;
}

export interface PrinterImageOptions {
  beep?: boolean;
  cut?: boolean;
  tailingLine?: boolean;
  encoding?: string;
  imageWidth?: number;
  imageHeight?: number;
  printerWidthType?: IPaperWidth;
  // only ios
  paddingX?: number;
  onError?: (error: Error) => void;
}

export interface IUSBPrinter {
  device: string;
  manufacturer_name: string;
  product_name: string;
  device_name: string;
  vendor_id: string;
  product_id: string;
}

export interface IBLEPrinter {
  device: string;
  device_name: string;
  inner_mac_address: string;
}

export interface INetPrinter {
  device: string;
  device_name?: string;
  host: string;
  port: number;
}
