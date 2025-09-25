import { NativeEventEmitter, NativeModules, Platform } from "react-native";
import { ColumnAlignment } from "./constant";
import {
  IBLEPrinter,
  INetPrinter,
  IUSBPrinter,
  PrinterImageOptions,
  PrinterOptions,
} from "./type";
import * as EPToolkit from "./utils/EPToolkit";
import { connectToHost } from "./utils/net-connect";
import { processColumnText } from "./utils/print-column";
export * from "./constant";
export * from "./type";
export * from "./utils/printer-commands";

const RNUSBPrinter = NativeModules.RNUSBPrinter;
const RNBLEPrinter = NativeModules.RNBLEPrinter;
const RNNetPrinter = NativeModules.RNNetPrinter;

const textTo64Buffer = (text: string, opts: PrinterOptions) => {
  const defaultOptions = {
    beep: false,
    cut: false,
    tailingLine: false,
    encoding: "UTF8",
  };

  const options = {
    ...defaultOptions,
    ...opts,
  };

  const fixAndroid = "\n";
  const buffer = EPToolkit.exchange_text(text + fixAndroid, options);
  return buffer.toString("base64");
};

const billTo64Buffer = (text: string, opts: PrinterOptions) => {
  const defaultOptions = {
    beep: true,
    cut: true,
    encoding: "UTF8",
    tailingLine: true,
  };
  const options = {
    ...defaultOptions,
    ...opts,
  };
  const buffer = EPToolkit.exchange_text(text, options);
  return buffer.toString("base64");
};

const textPreprocessingIOS = (text: string, canCut = true, beep = true) => {
  let options = {
    beep: beep,
    cut: canCut,
  };
  return {
    text: text
      .replace(/<\/?CB>/g, "")
      .replace(/<\/?CM>/g, "")
      .replace(/<\/?CD>/g, "")
      .replace(/<\/?C>/g, "")
      .replace(/<\/?D>/g, "")
      .replace(/<\/?B>/g, "")
      .replace(/<\/?M>/g, ""),
    opts: options,
  };
};

// const imageToBuffer = async (imagePath: string, threshold: number = 60) => {
//   const buffer = await EPToolkit.exchange_image(imagePath, threshold);
//   return buffer.toString("base64");
// };

const USBPrinter = {
  init: (): Promise<void> =>
    new Promise((resolve, reject) =>
      RNUSBPrinter.init(
        () => resolve(),
        (error: Error) => reject(error),
      ),
    ),

  getDeviceList: (): Promise<IUSBPrinter[]> =>
    new Promise((resolve, reject) =>
      RNUSBPrinter.getDeviceList(
        (printers: IUSBPrinter[]) =>
          resolve(
            printers.map((printer) => ({
              ...printer,
              device: JSON.parse(printer.device || "{}"),
            })),
          ),
        (error: Error) => reject(error),
      ),
    ),

  connectPrinter: (vendorId: string, productId: string): Promise<IUSBPrinter> =>
    new Promise((resolve, reject) =>
      RNUSBPrinter.connectPrinter(
        vendorId,
        productId,
        (printer: IUSBPrinter) =>
          resolve({ ...printer, device: JSON.parse(printer.device || "{}") }),
        (error: Error) => reject(error),
      ),
    ),

  closeConn: (): Promise<void> =>
    new Promise((resolve) => {
      RNUSBPrinter.closeConn();
      resolve();
    }),

  printText: (text: string, opts: PrinterOptions = {}): void =>
    RNUSBPrinter.printRawData(textTo64Buffer(text, opts), (error: Error) => {
      console.warn(error);
      opts?.onError?.(error);
    }),

  printBill: (text: string, opts: PrinterOptions = {}): void =>
    RNUSBPrinter.printRawData(billTo64Buffer(text, opts), (error: Error) => {
      console.warn(error);
      opts?.onError?.(error);
    }),
  /**
   * image url
   * @param imgUrl
   * @param opts
   */
  printImage: function (imgUrl: string, opts: PrinterImageOptions = {}) {
    if (Platform.OS === "ios") {
      RNUSBPrinter.printImageData(imgUrl, opts, (error: Error) => {
        console.warn(error);
        opts?.onError?.(error);
      });
    } else {
      RNUSBPrinter.printImageData(
        imgUrl,
        opts?.imageWidth ?? 0,
        opts?.imageHeight ?? 0,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    }
  },
  /**
   * base 64 string
   * @param Base64
   * @param opts
   */
  printImageBase64: function (Base64: string, opts: PrinterImageOptions = {}) {
    if (Platform.OS === "ios") {
      RNUSBPrinter.printImageBase64(Base64, opts, (error: Error) => {
        console.warn(error);
        opts?.onError?.(error);
      });
    } else {
      RNUSBPrinter.printImageBase64(
        Base64,
        opts?.imageWidth ?? 0,
        opts?.imageHeight ?? 0,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    }
  },
  /**
   * android print with encoder
   * @param text
   */
  printRaw: (text: string): void => {
    if (Platform.OS === "ios") {
    } else {
      RNUSBPrinter.printRawData(text, (error: Error) => console.warn(error));
    }
  },
  /**
   * `columnWidth`
   * 80mm => 46 character
   * 58mm => 30 character
   */
  printColumnsText: (
    texts: string[],
    columnWidth: number[],
    columnAlignment: ColumnAlignment[],
    columnStyle: string[],
    opts: PrinterOptions = {},
  ): void => {
    const result = processColumnText(
      texts,
      columnWidth,
      columnAlignment,
      columnStyle,
    );
    RNUSBPrinter.printRawData(textTo64Buffer(result, opts), (error: Error) => {
      console.warn(error);
      opts?.onError?.(error);
    });
  },
};

const BLEPrinter = {
  init: (): Promise<void> =>
    new Promise((resolve, reject) =>
      RNBLEPrinter.init(
        () => resolve(),
        (error: Error) => reject(error),
      ),
    ),

  getDeviceList: (): Promise<IBLEPrinter[]> =>
    new Promise((resolve, reject) =>
      RNBLEPrinter.getDeviceList(
        (printers: IBLEPrinter[]) =>
          resolve(
            printers.map((printer) => ({
              ...printer,
              device: JSON.parse(printer?.device || "{}"),
            })),
          ),
        (error: Error) => reject(error),
      ),
    ),

  connectPrinter: (inner_mac_address: string): Promise<IBLEPrinter> =>
    new Promise((resolve, reject) =>
      RNBLEPrinter.connectPrinter(
        inner_mac_address,
        (printer: IBLEPrinter) =>
          resolve({ ...printer, device: JSON.parse(printer?.device || "{}") }),
        (error: Error) => reject(error),
      ),
    ),

  closeConn: (): Promise<void> =>
    new Promise((resolve) => {
      RNBLEPrinter.closeConn();
      resolve();
    }),

  printText: (text: string, opts: PrinterOptions = {}): void => {
    if (Platform.OS === "ios") {
      const processedText = textPreprocessingIOS(text, false, false);
      RNBLEPrinter.printRawData(
        processedText.text,
        processedText.opts,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    } else {
      RNBLEPrinter.printRawData(textTo64Buffer(text, opts), (error: Error) => {
        console.warn(error);
        opts?.onError?.(error);
      });
    }
  },

  printBill: (text: string, opts: PrinterOptions = {}): void => {
    if (Platform.OS === "ios") {
      const processedText = textPreprocessingIOS(
        text,
        opts?.cut ?? true,
        opts.beep ?? true,
      );
      RNBLEPrinter.printRawData(
        processedText.text,
        processedText.opts,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    } else {
      RNBLEPrinter.printRawData(billTo64Buffer(text, opts), (error: Error) => {
        console.warn(error);
        opts?.onError?.(error);
      });
    }
  },
  /**
   * image url
   * @param imgUrl
   * @param opts
   */
  printImage: function (imgUrl: string, opts: PrinterImageOptions = {}) {
    if (Platform.OS === "ios") {
      /**
       * just development
       */
      RNBLEPrinter.printImageData(imgUrl, opts, (error: Error) => {
        console.warn(error);
        opts?.onError?.(error);
      });
    } else {
      RNBLEPrinter.printImageData(
        imgUrl,
        opts?.imageWidth ?? 0,
        opts?.imageHeight ?? 0,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    }
  },
  /**
   * base 64 string
   * @param Base64
   * @param opts
   */
  printImageBase64: function (Base64: string, opts: PrinterImageOptions = {}) {
    if (Platform.OS === "ios") {
      /**
       * just development
       */
      RNBLEPrinter.printImageBase64(Base64, opts, (error: Error) => {
        console.warn(error);
        opts?.onError?.(error);
      });
    } else {
      /**
       * just development
       */
      RNBLEPrinter.printImageBase64(
        Base64,
        opts?.imageWidth ?? 0,
        opts?.imageHeight ?? 0,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    }
  },
  /**
   * android print with encoder
   * @param text
   */
  printRaw: (text: string): void => {
    if (Platform.OS === "ios") {
      var processedText = textPreprocessingIOS(text, false, false);

      RNBLEPrinter.printRawData(
        processedText.text,
        processedText.opts,
        function (error) {
          return console.warn(error);
        },
      );
    } else {
      RNBLEPrinter.printRawData(text, (error: Error) => console.warn(error));
    }
  },
  /**
   * `columnWidth`
   * 80mm => 46 character
   * 58mm => 30 character
   */
  printColumnsText: (
    texts: string[],
    columnWidth: number[],
    columnAlignment: ColumnAlignment[],
    columnStyle: string[],
    opts: PrinterOptions = {},
  ): void => {
    const result = processColumnText(
      texts,
      columnWidth,
      columnAlignment,
      columnStyle,
    );
    if (Platform.OS === "ios") {
      const processedText = textPreprocessingIOS(result, false, false);
      RNBLEPrinter.printRawData(
        processedText.text,
        processedText.opts,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    } else {
      RNBLEPrinter.printRawData(
        textTo64Buffer(result, opts),
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    }
  },
};

const NetPrinter = {
  init: (): Promise<void> =>
    new Promise((resolve, reject) =>
      RNNetPrinter.init(
        () => resolve(),
        (error: Error) => reject(error),
      ),
    ),

  getDeviceList: (): Promise<INetPrinter[]> =>
    new Promise((resolve, reject) =>
      RNNetPrinter.getDeviceList(
        (printers: INetPrinter[]) =>
          resolve(
            printers.map((printer) => ({
              ...printer,
              device: JSON.parse(printer?.device || "{}"),
            })),
          ),
        (error: Error) => reject(error),
      ),
    ),

  connectPrinter: (
    host: string,
    port: number,
    timeout?: number,
  ): Promise<INetPrinter> =>
    new Promise(async (resolve, reject) => {
      try {
        await connectToHost(host, timeout);
        RNNetPrinter.connectPrinter(
          host,
          port,
          (printer: INetPrinter) =>
            resolve({
              ...printer,
              device: JSON.parse(printer?.device || "{}"),
            }),
          (error: Error) => reject(error),
        );
      } catch (error: any) {
        reject(error?.message || `Connect to ${host} fail`);
      }
    }),

  closeConn: (): Promise<void> =>
    new Promise((resolve) => {
      RNNetPrinter.closeConn();
      resolve();
    }),

  printText: (text: string, opts: PrinterOptions = {}): void => {
    if (Platform.OS === "ios") {
      const processedText = textPreprocessingIOS(text, false, false);
      RNNetPrinter.printRawData(
        processedText.text,
        processedText.opts,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    } else {
      RNNetPrinter.printRawData(textTo64Buffer(text, opts), (error: Error) => {
        console.warn(error);
        opts?.onError?.(error);
      });
    }
  },

  printBill: (text: string, opts: PrinterOptions = {}): void => {
    if (Platform.OS === "ios") {
      const processedText = textPreprocessingIOS(
        text,
        opts?.cut ?? true,
        opts.beep ?? true,
      );
      RNNetPrinter.printRawData(
        processedText.text,
        processedText.opts,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    } else {
      RNNetPrinter.printRawData(billTo64Buffer(text, opts), (error: Error) => {
        console.warn(error);
        opts?.onError?.(error);
      });
    }
  },
  /**
   * image url
   * @param imgUrl
   * @param opts
   */
  printImage: function (imgUrl: string, opts: PrinterImageOptions = {}) {
    if (Platform.OS === "ios") {
      RNNetPrinter.printImageData(imgUrl, opts, (error: Error) => {
        console.warn(error);
        opts?.onError?.(error);
      });
    } else {
      RNNetPrinter.printImageData(
        imgUrl,
        opts?.imageWidth ?? 0,
        opts?.imageHeight ?? 0,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    }
  },
  /**
   * base 64 string
   * @param Base64
   * @param opts
   */
  printImageBase64: function (Base64: string, opts: PrinterImageOptions = {}) {
    if (Platform.OS === "ios") {
      RNNetPrinter.printImageBase64(Base64, opts, (error: Error) => {
        console.warn(error);
        opts?.onError?.(error);
      });
    } else {
      RNNetPrinter.printImageBase64(
        Base64,
        opts?.imageWidth ?? 0,
        opts?.imageHeight ?? 0,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    }
  },

  /**
   * Android print with encoder
   * @param text
   */
  printRaw: (text: string): void => {
    if (Platform.OS === "ios") {
    } else {
      RNNetPrinter.printRawData(text, (error: Error) => console.warn(error));
    }
  },

  /**
   * `columnWidth`
   * 80mm => 46 character
   * 58mm => 30 character
   */
  printColumnsText: (
    texts: string[],
    columnWidth: number[],
    columnAlignment: ColumnAlignment[],
    columnStyle: string[] = [],
    opts: PrinterOptions = {},
  ): void => {
    const result = processColumnText(
      texts,
      columnWidth,
      columnAlignment,
      columnStyle,
    );
    if (Platform.OS === "ios") {
      const processedText = textPreprocessingIOS(result, false, false);
      RNNetPrinter.printRawData(
        processedText.text,
        processedText.opts,
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    } else {
      RNNetPrinter.printRawData(
        textTo64Buffer(result, opts),
        (error: Error) => {
          console.warn(error);
          opts?.onError?.(error);
        },
      );
    }
  },
};

const NetPrinterEventEmitter =
  Platform.OS === "ios"
    ? new NativeEventEmitter(RNNetPrinter)
    : new NativeEventEmitter();

export { BLEPrinter, NetPrinter, NetPrinterEventEmitter, USBPrinter };
export const DEVICE_PRINTER: Record<
  string,
  typeof USBPrinter | typeof BLEPrinter | typeof NetPrinter
> = {
  usb: USBPrinter,
  ble: BLEPrinter,
  net: NetPrinter,
};
