#!/usr/bin/env python3
"""Rewrite an arm64 iOS-device MH_OBJECT as an iOS-simulator object.

Replaces LC_VERSION_MIN_IPHONEOS (16 bytes) with LC_BUILD_VERSION
(platform=IOSSIMULATOR, 24 bytes), shifting file data and patching offsets.
"""
import struct
import sys

MH_MAGIC_64 = 0xFEEDFACF
LC_SEGMENT_64 = 0x19
LC_SYMTAB = 0x2
LC_DYSYMTAB = 0xB
LC_VERSION_MIN_IPHONEOS = 0x25
LC_BUILD_VERSION = 0x32
LINKEDIT_DATA = {0x26, 0x29, 0x2E, 0x1D, 0x1E, 0x2B, 0x33, 0x34}
NO_OFFSET = {0x2D}  # LC_LINKER_OPTION
PLATFORM_IOSSIMULATOR = 7
S_ZEROFILL = {0x1, 0xC, 0x12}


def ver(s):
    a, b = (s.split(".") + ["0"])[:2]
    return (int(a) << 16) | (int(b) << 8)


def convert(src, dst, minos="14.0", sdk="14.0"):
    data = bytearray(open(src, "rb").read())
    magic, cpu, sub, ftype, ncmds, sizeofcmds, flags, res = struct.unpack_from("<8I", data, 0)
    assert magic == MH_MAGIC_64 and ftype == 1, "not a 64-bit MH_OBJECT"
    lc_end = 32 + sizeofcmds

    # pass 1: gather cmds, max section alignment, first data offset
    cmds, off, max_align, first_data = [], 32, 3, len(data)
    for _ in range(ncmds):
        cmd, size = struct.unpack_from("<2I", data, off)
        cmds.append((cmd, off, size))
        if cmd == LC_SEGMENT_64:
            nsects = struct.unpack_from("<I", data, off + 64)[0]
            for i in range(nsects):
                s = off + 72 + i * 80
                offset, align = struct.unpack_from("<2I", data, s + 48)
                stype = struct.unpack_from("<I", data, s + 64)[0] & 0xFF
                max_align = max(max_align, align)
                if offset and stype not in S_ZEROFILL:
                    first_data = min(first_data, offset)
        off += size

    if any(c == LC_BUILD_VERSION for c, _, _ in cmds):
        raise SystemExit(f"{src}: already has LC_BUILD_VERSION")
    if not any(c == LC_VERSION_MIN_IPHONEOS for c, _, _ in cmds):
        raise SystemExit(f"{src}: no LC_VERSION_MIN_IPHONEOS")

    slack = first_data - lc_end
    shift = 0 if slack >= 8 else max(1 << max_align, 8)

    def fix(o):
        return o + shift if o >= lc_end and o != 0 else o

    # pass 2: build new load commands
    new_lcs = bytearray()
    for cmd, o, size in cmds:
        raw = bytearray(data[o:o + size])
        if cmd == LC_VERSION_MIN_IPHONEOS:
            raw = bytearray(struct.pack("<6I", LC_BUILD_VERSION, 24, PLATFORM_IOSSIMULATOR,
                                        ver(minos), ver(sdk), 0))
        elif cmd == LC_SEGMENT_64:
            fileoff = struct.unpack_from("<Q", raw, 40)[0]
            filesize = struct.unpack_from("<Q", raw, 48)[0]
            if filesize:
                struct.pack_into("<Q", raw, 40, fix(fileoff))
            nsects = struct.unpack_from("<I", raw, 64)[0]
            for i in range(nsects):
                s = 72 + i * 80
                offset, align, reloff, nreloc = struct.unpack_from("<4I", raw, s + 48)
                stype = struct.unpack_from("<I", raw, s + 64)[0] & 0xFF
                if offset and stype not in S_ZEROFILL:
                    struct.pack_into("<I", raw, s + 48, fix(offset))
                if nreloc:
                    struct.pack_into("<I", raw, s + 56, fix(reloff))
        elif cmd == LC_SYMTAB:
            symoff, nsyms, stroff, strsize = struct.unpack_from("<4I", raw, 8)
            struct.pack_into("<4I", raw, 8, fix(symoff), nsyms, fix(stroff), strsize)
        elif cmd == LC_DYSYMTAB:
            # (offset field index, count field index) pairs, as uint32 words after cmd/cmdsize
            vals = list(struct.unpack_from("<18I", raw, 8))
            for oi in (6, 8, 10, 12, 14, 16):
                if vals[oi]:
                    vals[oi] = fix(vals[oi])
            struct.pack_into("<18I", raw, 8, *vals)
        elif cmd in LINKEDIT_DATA:
            dataoff, dsize = struct.unpack_from("<2I", raw, 8)
            if dsize:
                struct.pack_into("<I", raw, 8, fix(dataoff))
        elif cmd in NO_OFFSET:
            pass
        else:
            raise SystemExit(f"{src}: unhandled load command 0x{cmd:x}")
        new_lcs += raw

    grow = len(new_lcs) - sizeofcmds  # 8
    header = bytearray(data[:32])
    struct.pack_into("<I", header, 20, len(new_lcs))
    body = data[lc_end:]
    if shift:
        pad = bytes(shift - grow)
        out = header + new_lcs + pad + body
    else:
        # fits in existing slack: overwrite padding after load commands
        out = header + new_lcs + body[grow:]
    open(dst, "wb").write(out)


if __name__ == "__main__":
    convert(sys.argv[1], sys.argv[2])
