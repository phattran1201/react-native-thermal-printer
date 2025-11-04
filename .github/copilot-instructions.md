# React Native Thermal Printer - AI Coding Agent Instructions

## Architecture Overview

This is a React Native library for thermal printing via USB, BLE, and Network connections. The architecture follows a **tri-platform adapter pattern** with separate implementations for each connection type.

### Core Components

- **`src/index.tsx`**: Main entry point exposing `USBPrinter`, `BLEPrinter`, `NetPrinter` modules
- **Native Modules**: Platform-specific implementations in `android/` and `ios/`
- **ESC/POS Engine**: `src/utils/EPToolkit.ts` handles thermal printer command generation
- **Bridge Layer**: Each printer type wraps native modules with promise-based APIs

### Key Data Flow

1. **Text/Image Input** → **EPToolkit processing** → **Base64 buffer** → **Native module** → **Printer**
2. Platform differences handled at buffer generation (iOS strips formatting tags, Android preserves them)

## Development Patterns

### Native Module Interface

All printer modules implement the `RNPrinterModule` interface:

```typescript
// Core methods every printer type must support
init() -> Promise<void>
getDeviceList() -> Promise<Device[]>
connectPrinter(...args) -> Promise<Device>
printText/printBill/printImage -> void (with error callbacks)
```

### Platform-Specific Implementations

- **Android**: Java classes in `com.harold.rn.printer.*` with USB/BLE/Net adapters
- **iOS**: Objective-C files (`RNBLEPrinter.m`, `RNNetPrinter.m`) + vendored PrinterSDK library
- **Connection Parameters**:
  - USB: `vendorId`, `productId` (Android only)
  - BLE: `inner_mac_address`
  - Net: `host`, `port`, `timeout?`

### ESC/POS Command System

The library uses a sophisticated command system in `src/utils/printer-commands.ts`:

- **COMMANDS** object provides ESC/POS byte sequences for formatting
- **EPToolkit.exchange_text()** processes text with embedded tags like `<B>bold</B>`, `<C>center</C>`
- **BufferHelper** concatenates binary command sequences
- Commands support text styling, paper control, hardware functions

### TypeScript Patterns

```typescript
// Device type discrimination
type IDevicesSelectPrinter =
  | ({ printerType: "usb" } & IUSBPrinter)
  | ({ printerType: "ble" } & IBLEPrinter)
  | ({ printerType: "net" } & INetPrinter);

// Options with sensible defaults
const defaultOptions = { beep: false, cut: false, encoding: "UTF8" };
const options = { ...defaultOptions, ...opts };
```

## Critical Developer Workflows

### Building & Testing

- **iOS**: Requires `pod install` after changes, uses vendored `libPrinterSDK.a`
- **TypeScript**: `yarn typecheck` validates before build
- **Platform Testing**: USB only works on Android, BLE/Net cross-platform

### Adding New Printer Commands

1. Add ESC/POS byte sequence to `COMMANDS` object in `printer-commands.ts`
2. Update `EPToolkit.ts` controller mapping if using tag syntax
3. Test on both platforms (iOS may need different handling)

### Debugging Connection Issues

- **USB**: Check `vendorId`/`productId` in device list on Android
- **BLE**: Verify `inner_mac_address` format and permissions
- **Net**: Use built-in timeout parameter, check network connectivity with `react-native-ping`

## Project-Specific Conventions

### Error Handling Pattern

```typescript
// Async methods use Promise rejection
connectPrinter().catch((error) => {
  /* handle */
});

// Print methods use error callbacks
printText(text, {
  onError: (error) => {
    /* handle */
  },
});
```

### Buffer Conversion Chain

Text → EPToolkit processing → Buffer → Base64 string → Native module → Printer hardware

### iOS-Specific Adaptations

- **Tag Stripping**: iOS preprocessing removes formatting tags with `textPreprocessingIOS()`
- **Image Handling**: Different native method signatures between platforms
- **PrinterSDK**: Uses external library requiring proper linking in podspec

### Column Text Processing

The `processColumnText()` utility handles:

- Text truncation/padding based on column widths
- Alignment (LEFT/CENTER/RIGHT) within columns
- Character counting for different paper widths (58mm=32 chars, 80mm=48 chars)

## Integration Points

### React Native Bridge

- Uses `NativeModules` for method calls, `NativeEventEmitter` for scanner events
- Platform checks with `Platform.OS` for conditional logic
- Callback-based error handling (not promise rejection) for print operations

### External Dependencies

- **`iconv-lite`**: Text encoding conversion for international characters
- **`buffer`**: Node.js Buffer polyfill for React Native
- **`react-native-ping`**: Network connectivity testing (used by NetPrinter)

### Native Dependencies

- **Android**: No external dependencies, pure Java implementation
- **iOS**: Vendored PrinterSDK library (`libPrinterSDK.a`) for hardware communication

## Testing Considerations

When implementing features:

- **USB printing**: Test only on Android devices with physical printer
- **BLE printing**: Verify `inner_mac_address` discovery and pairing
- **Network printing**: Test timeout scenarios and IP connectivity
- **Image printing**: Verify Base64 encoding and size parameters work across platforms
- **Column formatting**: Test with various paper widths and text lengths
