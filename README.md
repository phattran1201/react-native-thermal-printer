# React Native Thermal Printer

> A React Native module for printing and images to thermal printers via USB, Bluetooth (BLE), and Network (LAN/WiFi). Supports both Android and iOS.

**Maintained and enhanced by [Harold - @phattran1201](https://github.com/phattran1201) 👨‍💻**

---

<div align="center">

[![npm][npm]][npm-URL] [![React-Native][React-Native]][React-Native-URL] [![Android][Android]][Android-URL][![iOS][iOS]][iOS-URL]

</div>

---

## 📦 Installation

```sh
yarn add @haroldtran/react-native-thermal-printer
```

## ✨ Features

<table>
  <thead>
    <tr>
      <th style="text-align:left;">Feature</th>
      <th style="text-align:center;">Android</th>
      <th style="text-align:center;">iOS</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>🖼️ <b>Image & QR (URL & Base64)</b></td>
      <td align="center">✅</td>
      <td align="center">✅</td>
    </tr>
    <tr>
      <td>✂️ <b>Fix cut</b></td>
      <td align="center">✅</td>
      <td align="center">✅</td>
    </tr>
    <tr>
      <td>📑 <b>Print With Column</b></td>
      <td align="center">✅</td>
      <td align="center">✅</td>
    </tr>
    <tr>
      <td>⏱️ <b>NET Connect Timeout</b></td>
      <td align="center">✅</td>
      <td align="center">✅</td>
    </tr>
  </tbody>
</table>

<p style="color:#FFA500;"><b>⚠️ Lưu ý:</b> <i>Chức năng <b>In hình & QR qua Bluetooth trên iOS</b> đã được triển khai nhưng <b>chưa kiểm thử thực tế</b>.</i></p>

## 🖨️ Printer Support

<table>
  <thead>
    <tr>
      <th style="text-align:left;">Printer</th>
      <th style="text-align:center;">Android</th>
      <th style="text-align:center;">iOS</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>🔌 <b>USBPrinter</b></td>
      <td align="center">✅</td>
      <td align="center">❌</td>
    </tr>
    <tr>
      <td>🔵 <b>BLEPrinter</b></td>
      <td align="center">✅</td>
      <td align="center">✅</td>
    </tr>
    <tr>
      <td>🌐 <b>NetPrinter</b></td>
      <td align="center">✅</td>
      <td align="center">✅</td>
    </tr>
  </tbody>
</table>

---

### 🧾 Print Columns Text

```tsx
import Printer, { COMMANDS, ColumnAlignment } from '@haroldtran/react-native-thermal-printer';

const handlePrint = async () => {
  try {
    const Printer = DEVICE_PRINTER[selectedValue];
    const BOLD_ON = COMMANDS.TEXT_FORMAT.TXT_BOLD_ON;
    const BOLD_OFF = COMMANDS.TEXT_FORMAT.TXT_BOLD_OFF;
    const CENTER = COMMANDS.TEXT_FORMAT.TXT_ALIGN_CT;
    let orderList = [
      ['1. Skirt Palas Labuh Muslimah Fashion', 'x2', '500$'],
      ['2. BLOUSE ROPOL VIRAL MUSLIMAH FASHION ', 'x4222', '12.333.500$'],
      ['3. Women Crew Neck Button Down Ruffle Collar Loose Blouse', 'x1', '30000000000000$'],
    ];
    let columnAlignment = [ColumnAlignment.LEFT, ColumnAlignment.CENTER, ColumnAlignment.RIGHT];
    let columnWidth = [46 - (7 + 12), 7, 12];
    const header = ['Product list', 'Qty', 'Price'];
    Printer.printImage('https://i.ibb.co/21dsjpLx/image-23-2.png', { imageWidth: 400 });
    Printer.printColumnsText(header, columnWidth, columnAlignment, [`${BOLD_ON}`, '', '']);
    for (let i in orderList) {
      Printer.printColumnsText(orderList[i], columnWidth, columnAlignment, [`${BOLD_OFF}`, '', '']);
    }
    Printer.printBill(`${CENTER}Thank you\n`);
  } catch (err) {
    console.warn('Print bill error' + err);
  }
};
```

### 🖼️ Print Image

```tsx
Printer.printImage('https://i.ibb.co/21dsjpLx/image-23-2.png');
Printer.printImageBase64('iVBORw0KGgoAAAANSUhEUgAAAWwAAACIAQMAAAD580PaAAAABGdBTUEAALGPC/xhBQAAAAFzUkdCAK7OHOkAAAAJcEhZcwAAFiUAABYlAUlSJPAAAAAGUExURf///////1V89WwAAAACdFJOU/kEtIBT1QAABPpJREFUWMPd2L2OGzcQB/BdbEEXhnnpA7NP5TLF4bZwkTJpUqfIaxjHPahQqSYPkFpNHsErXJEHyAOESro04RUBNgDDyQw5q/0iV6vCjQVDnz/9jyKHXNIFALiiv2lYv9liwqvbeFHfxuVtvEwY/3uWFxE0xceBF4XK8ppf059pQ09ZemHoKzbGjTk1vuPvRd5QB+e46LlgHj6sc7zqecncFmvpZc8L5maVFxeuI28WHMadpnteRx4zMrylHolcBe5XuaW3O+5S4u4Kl1PerfJu4CJwG7sry0W8a5hjP5bdiE/n0YUb5i110SnPq8gtPok81HSKuwJlGXnHvAlVlGz7mDvmPMG28zrHO+JF5B6/h9zzr7pwN267HvHqOl+kO/7zea7H6Y7XhnzbJ7xb4bHtM17Bakdix406csntrO09F5GLLI/TtOYiCNxu5HbO+5n/z/d0m3MTZ5PlNdlM1tBRXzJvtnJF3PNKsC39ss6YTemXVcxsToeb0qssnyypfbrYyDld3ZYuP01j6vFPtVt5eRsvZjxzbYol1vGoLut9PkyaS+wGLoFLbGM61nt9bSWIHHie3sRNnB5ry9LA5UZu+/Wd+WJJnXLTcxuntpsv2POLfDXhPnH18MV0Q0NtH7haLQIxT5erXMJ4jQTeuW7lDYl/81zFto+4hKc8r6dX7Xa545hwPb0Mmyscptwutz/LjaHcyiuYXIZTe7HlHnjgfp2rGYd1rrntF96u8fKyIcNPNuyBF7xb5fWcw4aekbSz1pGbgudi7nQwO9iMZpxNDdP2c9N87l7l+jZef1Kubua44wOPJa8MeEFP8E0Dh3G/f3U8HoeVoAaHRanOyB1zvR9zDTzegavA5Ql8deGHBTe8in0IHOQzc/z5pt4DPNsJ75g/4jrzHkDskHfM1WHBHXMd+X4PvuwE82W6n/LnA/FD4Ge5TAdesImf8VOcg1/YPfNlOq2IVWy7RX5W4O9emO8PWY49Q9wgf0ccx+G8y6djP79YHPIa+Zn5cyK9ZY4fM//6T+bnfZ5jQ36mn4z8R277OZ1OF0pJA6wjf8/c5tN1PJbjo7v/pueJdBPT8cXpu5h+L5jj6K7w566O6Rcu1/jeRf4gmTuxmeM4nP2VdDXjq+nCM1fMIZvu8IVkXsOvgb/k+YaF47PhXeyd4fZHfPgtzVtB/3bQ1d19UbxVUCgcJdxHv0tyrGJ83OF0Iv6lwsOrrwB36Xcp7sOGnNJ1TMe9EHGbTnelcFWjh3T7pvQlCPMqzXFVkqYOba+d8sroxhfIdbrEcJmprcJ0TdypFtrA0x2JEwmboYb0FsxjoUULuXSjnQxtD+lPYB9f1ct0O+Wdjun4xYcU55+KWwa81AzpxF8rYepkuiuZ922voHt4LYWRSe5xuzNJJ/5GClslOZZIi5fIoe3E3wosgjRv5TJdimWJMbfYxfO2yx2eve4WPLxRzXsmcpPmUE57hrg6iXiqSvAnA+7CY7/LE2270/zEo8rpVATSCN7FDbzjZyesxVBilmvGPEqb509ckcrqviJVhtN2aYcQpwdugHSod9162Ym/XYLjVHqoOtHqpviA/R9mkyqJn+3dkoczEx0Nm+IB0yjd4tSWTrTzUXXMJW3fcReM95ROZ3WV5bSeNxUdtxsR0j1+X3phYonB5X+u/E/H4y9YkXiiUfDXke7/0/ADNHT3ravs/2olfabxpSRKAAAAAElFTkSuQmCC'.replace(/(\r\n|\n|\r)/gm, ''), { imageWidth: 575 });
```

---

## Printer Commands

The `COMMANDS` object provides a comprehensive set of ESC/POS commands for advanced text formatting, hardware control, and paper management. These commands allow you to customize the appearance and behavior of printed output.

### Text Formatting Commands

Use these commands to style your text:

```tsx
import { COMMANDS } from '@haroldtran/react-native-thermal-printer';

// Example: Bold text
const boldText = COMMANDS.TEXT_FORMAT.TXT_BOLD_ON + 'Bold Text' + COMMANDS.TEXT_FORMAT.TXT_BOLD_OFF;

// Example: Underlined text
const underlinedText = COMMANDS.TEXT_FORMAT.TXT_UNDERL_ON + 'Underlined' + COMMANDS.TEXT_FORMAT.TXT_UNDERL_OFF;

// Example: Centered text
const centeredText = COMMANDS.TEXT_FORMAT.TXT_ALIGN_CT + 'Centered Text';

// Example: Double height and width
const largeText = COMMANDS.TEXT_FORMAT.TXT_4SQUARE + 'Large Text' + COMMANDS.TEXT_FORMAT.TXT_NORMAL;

// Example: Custom size (width 2, height 3)
const customSizeText = COMMANDS.TEXT_FORMAT.TXT_CUSTOM_SIZE(2, 3) + 'Custom Size' + COMMANDS.TEXT_FORMAT.TXT_NORMAL;
```

**Visual Effects:**

- **TXT_BOLD_ON/OFF**: Makes text appear **bold** (darker/thicker).
- **TXT_UNDERL_ON/OFF**: Adds a line under the text.
- **TXT_2HEIGHT**: Doubles the height of text (taller characters).
- **TXT_2WIDTH**: Doubles the width of text (wider characters).
- **TXT_4SQUARE**: Combines double height and width for larger text.
- **TXT_ALIGN_CT**: Centers the text on the line.
- **TXT_CUSTOM_SIZE(w, h)**: Allows custom width (1-8) and height (1-8) scaling.

### Hardware and Control Commands

```tsx
// Initialize printer
const initCommand = COMMANDS.HARDWARE.HW_INIT;

// Cut paper
const cutCommand = COMMANDS.PAPER.PAPER_FULL_CUT;

// Feed paper
const feedCommand = COMMANDS.FEED_CONTROL_SEQUENCES.CTL_LF;
```

**Visual/Functional Effects:**

- **HW_INIT**: Resets the printer to default settings.
- **PAPER_FULL_CUT**: Cuts the paper completely.
- **CTL_LF**: Advances the paper by one line.

### Horizontal Lines

Pre-defined horizontal lines for 58mm and 80mm printers:

```tsx
// 58mm printer lines
const line58 = COMMANDS.HORIZONTAL_LINE.HR_58MM; // ==================================

// 80mm printer lines
const line80 = COMMANDS.HORIZONTAL_LINE.HR_80MM; // =================================================
```

These create solid lines across the paper width for visual separation.

### Usage in Print Functions

Combine commands with your print text:

```tsx
import Printer, { COMMANDS } from '@haroldtran/react-native-thermal-printer';

const receiptText =
  COMMANDS.TEXT_FORMAT.TXT_ALIGN_CT + 'RECEIPT\n' +
  COMMANDS.TEXT_FORMAT.TXT_BOLD_ON + 'Item: Coffee\n' +
  COMMANDS.TEXT_FORMAT.TXT_BOLD_OFF + 'Price: $5.00\n' +
  COMMANDS.HORIZONTAL_LINE.HR_58MM + '\n' +
  'Thank you!';

Printer.printText(receiptText);
```

This will produce a centered, formatted receipt with bold text and a horizontal line.

---

## 🛠️ API Reference

The library exports several modules and utilities for interacting with thermal printers:

### Exported Modules

- `USBPrinter`: For USB-connected printers (Android only).
- `BLEPrinter`: For Bluetooth Low Energy printers.
- `NetPrinter`: For network (LAN/WiFi) printers.
- `COMMANDS`: A collection of ESC/POS commands for advanced formatting.
- `NetPrinterEventEmitter`: Event emitter for network printer events.
- `RN_THERMAL_PRINTER_EVENTS`: Enum for event types.

### Common Methods Across Printers

Each printer module (`USBPrinter`, `BLEPrinter`, `NetPrinter`) supports the following methods:

| Method                                                                                                                                        | Description                                                  |
| --------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------ |
| `init()`                                                                                                                                      | Initialize the printer module. Call before any other method. |
| `getDeviceList()`                                                                                                                             | Get a list of available printers.                            |
| `connectPrinter(...)`                                                                                                                         | Connect to the printer (parameters vary by type).            |
| `closeConn()`                                                                                                                                 | Close the current printer connection.                        |
| `printText(text: string, opts?: PrinterOptions)`                                                                                              | Print plain text with optional formatting.                   |
| `printBill(text: string, opts?: PrinterOptions)`                                                                                              | Print a formatted bill or receipt.                           |
| `printImage(imgUrl: string, opts?: PrinterImageOptions)`                                                                                      | Print an image from a URL.                                   |
| `printImageBase64(base64: string, opts?: PrinterImageOptions)`                                                                                | Print an image from a Base64 string.                         |
| `printRaw(text: string)`                                                                                                                      | Print raw ESC/POS commands (Android only).                   |
| `printColumnsText(texts: string[], columnWidth: number[], columnAlignment: ColumnAlignment[], columnStyle?: string[], opts?: PrinterOptions)` | Print text in columns.                                       |

### Specific Connection Methods

- **USBPrinter**: `connectPrinter(vendorId: string, productId: string)`
- **BLEPrinter**: `connectPrinter(inner_mac_address: string)`
- **NetPrinter**: `connectPrinter(host: string, port: number, timeout?: number)`

### COMMANDS Object Reference

The `COMMANDS` object provides a comprehensive set of ESC/POS commands organized into logical categories:

#### Basic Control Characters

| Name | Description         | Code   |
| ---- | ------------------- | ------ |
| LF   | Line Feed           | `\x0a` |
| ESC  | Escape              | `\x1b` |
| FS   | Field Separator     | `\x1c` |
| GS   | Group Separator     | `\x1d` |
| US   | Unit Separator      | `\x1f` |
| FF   | Form Feed           | `\x0c` |
| DLE  | Data Link Escape    | `\x10` |
| DC1  | Device Control 1    | `\x11` |
| DC4  | Device Control 4    | `\x14` |
| EOT  | End of Transmission | `\x04` |
| NUL  | Null                | `\x00` |
| EOL  | End of Line         | `\n`   |

#### Horizontal Line Commands

| Command  | Description        | Example                                            |
| -------- | ------------------ | -------------------------------------------------- |
| HR_58MM  | Solid line (58mm)  | `==================================`               |
| HR2_58MM | Dashed line (58mm) | `**********************************`               |
| HR3_58MM | Dotted line (58mm) | `----------------------------------`               |
| HR_80MM  | Solid line (80mm)  | `================================================` |
| HR2_80MM | Dashed line (80mm) | `************************************************` |
| HR3_80MM | Dotted line (80mm) | `------------------------------------------------` |

#### Feed Control Sequences

| Command | Description         |
| ------- | ------------------- |
| CTL_LF  | Print and line feed |
| CTL_FF  | Form feed           |
| CTL_CR  | Carriage return     |
| CTL_HT  | Horizontal tab      |
| CTL_VT  | Vertical tab        |

#### Line Spacing

| Command    | Description              |
| ---------- | ------------------------ |
| LS_DEFAULT | Default line spacing     |
| LS_SET     | Set line spacing         |
| LS_SET1    | Alternative line spacing |

#### Hardware Control

| Command   | Description                        |
| --------- | ---------------------------------- |
| HW_INIT   | Initialize printer and reset modes |
| HW_SELECT | Select printer                     |
| HW_RESET  | Reset printer hardware             |

#### Cash Drawer

| Command   | Description         |
| --------- | ------------------- |
| CD_KICK_2 | Send pulse to pin 2 |
| CD_KICK_5 | Send pulse to pin 5 |

#### Margins

| Command | Description       |
| ------- | ----------------- |
| BOTTOM  | Set bottom margin |
| LEFT    | Set left margin   |
| RIGHT   | Set right margin  |

#### Paper Cutting

| Command        | Description               |
| -------------- | ------------------------- |
| PAPER_FULL_CUT | Full paper cut            |
| PAPER_PART_CUT | Partial paper cut         |
| PAPER_CUT_A    | Alternative partial cut A |
| PAPER_CUT_B    | Alternative partial cut B |

#### Text Formatting

**Text Size & Scaling:**

| Command                        | Description             |
| ------------------------------ | ----------------------- |
| TXT_NORMAL                     | Normal text size        |
| TXT_2HEIGHT                    | Double height           |
| TXT_2WIDTH                     | Double width            |
| TXT_4SQUARE                    | Double height and width |
| TXT_CUSTOM_SIZE(width, height) | Custom size function    |

**Text Height/Width (1-8):**

| Command           | Description               |
| ----------------- | ------------------------- |
| TXT_WIDTH[<1-8>]  | Size 1 to 8 TXT_WIDTH[6]  |
| TXT_HEIGHT[<1-8>] | Size 1 to 8 TXT_HEIGHT[6] |

**Text Decoration:**

| Command        | Description          |
| -------------- | -------------------- |
| TXT_UNDERL_OFF | Underline off        |
| TXT_UNDERL_ON  | Underline on (1-dot) |
| TXT_UNDERL2_ON | Underline on (2-dot) |
| TXT_BOLD_OFF   | Bold off             |
| TXT_BOLD_ON    | Bold on              |
| TXT_ITALIC_OFF | Italic off           |
| TXT_ITALIC_ON  | Italic on            |

**Font Types:**

| Command    | Description |
| ---------- | ----------- |
| TXT_FONT_A | Font type A |
| TXT_FONT_B | Font type B |
| TXT_FONT_C | Font type C |

**Text Alignment:**

| Command      | Description      |
| ------------ | ---------------- |
| TXT_ALIGN_LT | Left alignment   |
| TXT_ALIGN_CT | Center alignment |
| TXT_ALIGN_RT | Right alignment  |

---

## 🛠️ Troubleshooting

- For iOS, if you see `duplicate symbols for architecture x86_64`, comment out Flipper in your Podfile and AppDelegate.m.
- Make sure to run `pod install` after installing dependencies for iOS.
- Ensure your printer supports ESC/POS commands for best compatibility.

---

## 👥 Contributors

<table>
    <tbody>
        <tr>
            <td align="center">
                <a href="https://github.com/phattran1201">
                    <img src="https://avatars.githubusercontent.com/u/36856455" width="100;" alt="phattran1201"/>
                    <br />
                    <sub><b>Harold Tran</b></sub>
                </a>
            </td>
        </tr>
    </tbody>
</table>

<!-- Badge for README -->
[npm]: https://img.shields.io/npm/v/%40haroldtran%2Freact-native-thermal-printer?&style=for-the-badge&logo=npm&logoColor=red
[npm-URL]: https://www.npmjs.com/package/@haroldtran/react-native-thermal-printer
[Android]: https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white
[Android-URL]: https://www.android.com/
[iOS]: https://img.shields.io/badge/iOS-000000?style=for-the-badge&logo=ios&logoColor=white
[iOS-URL]: https://www.apple.com/ios
[React-Native]: https://img.shields.io/badge/React_Native-20232A?style=for-the-badge&logo=react&logoColor=61DAFB
[React-Native-URL]: https://reactnative.dev/
