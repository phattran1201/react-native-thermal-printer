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

<div align="center">

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

</div>

<p style="color:#FFA500;"><b>⚠️ Lưu ý:</b> <i>Chức năng <b>In hình & QR qua Bluetooth trên iOS</b> đã được triển khai nhưng <b>chưa kiểm thử thực tế</b>.</i></p>

## 🖨️ Printer Support

<div align="center">

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

</div>

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

## 🛠️ API Reference

---

| Method                                                                                                                                          | Description                                                      |
| :---------------------------------------------------------------------------------------------------------------------------------------------- | :--------------------------------------------------------------- |
| `init()`                                                                                                                                        | Initialize the printer module. Call before any other method.     |
| `getDeviceList()`                                                                                                                               | Get a list of available printers (USB, BLE, NET).                |
| `connectPrinter(host: string, port: number, timeout?: number)` 🔌                                                                                | Connect to a network printer by host and port. Optional timeout. |
| `closeConn()`                                                                                                                                   | Close the current printer connection.                            |
| `printText(text: string, opts?: {})`                                                                                                            | Print plain text. Optional formatting options.                   |
| `printBill(text: string, opts?: PrinterOptions)                                                                                                 | Print a formatted bill or receipt.                               |
| `printImage(imgUrl: string, opts?: PrinterImageOptions)`                                                                                        | Print an image from a URL.                                       |
| `printImageBase64(base64: string, opts?: PrinterImageOptions)`                                                                                  | Print an image from a Base64 string.                             |
| `printRaw(text: string)` ⚡ *(Android only)*                                                                                                     | Print raw ESC/POS commands (Android only).                       |
| `printColumnsText(texts: string[], columnWidth: number[], columnAlignment: ColumnAlignment[], columnStyle?: string[], opts?: PrinterOptions)` 📑 | Print text in columns with custom width, alignment, and style.   |

---

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
