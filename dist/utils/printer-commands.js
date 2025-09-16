/**
 * Bộ lệnh ESC/POS cho máy in nhiệt với chú thích tiếng Việt cho từng trường.
 * Sử dụng cùng TypeScript để nhận gợi ý IntelliSense khi nhập lệnh.
 */
/**
 * Đối tượng hằng chứa tất cả lệnh ESC/POS tiện dụng cho máy in nhiệt.
 * Hãy nhập COMMANDS. để xem gợi ý và mô tả từng trường bằng tiếng Việt.
 */
export var COMMANDS = {
    // ==================== BASIC CONTROL CHARACTERS ====================
    /** Line Feed (LF) – xuống dòng in phần đệm và xuống dòng mới */
    LF: "\x0a",
    /** Escape – ký tự bắt đầu cho nhiều lệnh ESC/POS */
    ESC: "\x1b",
    /** File Separator (FS) – ký tự điều khiển */
    FS: "\x1c",
    /** Group Separator (GS) – mở đầu nhiều lệnh GS (0x1D) */
    GS: "\x1d",
    /** Unit Separator (US) – ký tự điều khiển */
    US: "\x1f",
    /** Form Feed (FF) – chuyển sang trang/biểu mẫu tiếp theo (nếu hỗ trợ) */
    FF: "\x0c",
    /** Data Link Escape (DLE) – ký tự điều khiển liên kết dữ liệu */
    DLE: "\x10",
    /** Device Control 1 (DC1) – ký tự điều khiển thiết bị 1 */
    DC1: "\x11",
    /** Device Control 4 (DC4) – ký tự điều khiển thiết bị 4 */
    DC4: "\x14",
    /** End Of Transmission (EOT) – kết thúc truyền */
    EOT: "\x04",
    /** Null (NUL) – ký tự rỗng */
    NUL: "\x00",
    /** End Of Line (EOL) – xuống dòng kiểu \n */
    EOL: "\n",
    // ==================== HORIZONTAL LINES ====================
    HORIZONTAL_LINE: {
        /** Đường kẻ khổ 58mm bằng dấu = */
        HR_58MM: "==================================",
        /** Đường kẻ khổ 58mm bằng dấu * */
        HR2_58MM: "**********************************",
        /** Đường kẻ khổ 58mm bằng dấu - */
        HR3_58MM: "----------------------------------",
        /** Đường kẻ khổ 80mm bằng dấu = */
        HR_80MM: "================================================",
        /** Đường kẻ khổ 80mm bằng dấu * */
        HR2_80MM: "************************************************",
        /** Đường kẻ khổ 80mm bằng dấu - */
        HR3_80MM: "------------------------------------------------",
    },
    // ==================== FEED CONTROL SEQUENCES ====================
    FEED_CONTROL_SEQUENCES: {
        /** Line Feed – xuống dòng in phần đệm và nhảy xuống dòng mới */
        CTL_LF: "\x0a",
        /** Form Feed – nhảy đến trang/biểu mẫu tiếp theo (nếu hỗ trợ) */
        CTL_FF: "\x0c",
        /** Carriage Return – đưa con trỏ về đầu dòng hiện tại */
        CTL_CR: "\x0d",
        /** Tab ngang – nhảy đến vị trí tab tiếp theo */
        CTL_HT: "\x09",
        /** Tab dọc – nhảy đến vị trí tab dọc tiếp theo */
        CTL_VT: "\x0b",
    },
    // ==================== LINE SPACING ====================
    LINE_SPACING: {
        /** Đặt khoảng cách dòng về mặc định của máy */
        LS_DEFAULT: "\x1b\x32",
        /** Thiết lập khoảng cách dòng theo số chấm (ESC 3 n) – tùy máy */
        LS_SET: "\x1b\x33",
        /** Thiết lập khoảng cách dòng kiểu 1 (ESC 1) – khoảng cách lớn hơn */
        LS_SET1: "\x1b\x31",
    },
    // ==================== HARDWARE CONTROL ====================
    HARDWARE: {
        /** Khởi tạo máy in – xóa bộ đệm và trả về chế độ mặc định */
        HW_INIT: "\x1b\x40",
        /** Chọn máy in (nếu có nhiều) – thường dùng để bật thiết bị */
        HW_SELECT: "\x1b\x3d\x01",
        /** Reset phần cứng máy in */
        HW_RESET: "\x1b\x3f\x0a\x00",
    },
    // ==================== CASH DRAWER ====================
    CASH_DRAWER: {
        /** Phát xung tới chân 2 để bật ngăn kéo */
        CD_KICK_2: "\x1b\x70\x00",
        /** Phát xung tới chân 5 để bật ngăn kéo */
        CD_KICK_5: "\x1b\x70\x01",
    },
    // ==================== MARGINS ====================
    MARGINS: {
        /** Thiết lập lề dưới (ESC O n) – tùy máy */
        BOTTOM: "\x1b\x4f",
        /** Thiết lập lề trái (ESC l n) – tùy máy */
        LEFT: "\x1b\x6c",
        /** Thiết lập lề phải (ESC Q n) – tùy máy */
        RIGHT: "\x1b\x51",
    },
    // ==================== PAPER CUTTING ====================
    PAPER: {
        /** Cắt toàn phần */
        PAPER_FULL_CUT: "\x1d\x56\x00",
        /** Cắt một phần */
        PAPER_PART_CUT: "\x1d\x56\x01",
        /** Cắt kiểu A (GS V 65) – tùy máy */
        PAPER_CUT_A: "\x1d\x56\x41",
        /** Cắt kiểu B (GS V 66) – tùy máy */
        PAPER_CUT_B: "\x1d\x56\x42",
    },
    // ==================== TEXT FORMATTING ====================
    TEXT_FORMAT: {
        // ---- TEXT SIZE ----
        /** Chữ kích thước bình thường */
        TXT_NORMAL: "\x1b\x21\x00",
        /** Chữ cao gấp đôi */
        TXT_2HEIGHT: "\x1b\x21\x10",
        /** Chữ rộng gấp đôi */
        TXT_2WIDTH: "\x1b\x21\x20",
        /** Chữ rộng và cao gấp đôi (4-square) */
        TXT_4SQUARE: "\x1b\x21\x30",
        /**
         * Kích thước tuỳ chỉnh theo hệ số (1..8)
         * @param width Hệ số bề rộng (1..8)
         * @param height Hệ số chiều cao (1..8)
         * @returns Chuỗi lệnh ESC/POS tương ứng
         */
        TXT_CUSTOM_SIZE: function (width, height) {
            var widthDec = (width - 1) * 16;
            var heightDec = height - 1;
            var sizeDec = widthDec + heightDec;
            return "\x1d\x21" + String.fromCharCode(sizeDec);
        },
        // ---- TEXT HEIGHT OPTIONS ----
        /** Bảng hệ số chiều cao 1..8 */
        TXT_HEIGHT: {
            1: "\x00",
            2: "\x01",
            3: "\x02",
            4: "\x03",
            5: "\x04",
            6: "\x05",
            7: "\x06",
            8: "\x07",
        },
        // ---- TEXT WIDTH OPTIONS ----
        /** Bảng hệ số chiều rộng 1..8 */
        TXT_WIDTH: {
            1: "\x00",
            2: "\x10",
            3: "\x20",
            4: "\x30",
            5: "\x40",
            6: "\x50",
            7: "\x60",
            8: "\x70",
        },
        // ---- TEXT DECORATION ----
        /** Tắt gạch dưới */
        TXT_UNDERL_OFF: "\x1b\x2d\x00",
        /** Bật gạch dưới 1 chấm */
        TXT_UNDERL_ON: "\x1b\x2d\x01",
        /** Bật gạch dưới 2 chấm */
        TXT_UNDERL2_ON: "\x1b\x2d\x02",
        /** Tắt chữ đậm */
        TXT_BOLD_OFF: "\x1b\x45\x00",
        /** Bật chữ đậm */
        TXT_BOLD_ON: "\x1b\x45\x01",
        /** Tắt chữ nghiêng */
        TXT_ITALIC_OFF: "\x1b\x35",
        /** Bật chữ nghiêng */
        TXT_ITALIC_ON: "\x1b\x34",
        // ---- FONT TYPES ----
        /** Phông A */
        TXT_FONT_A: "\x1b\x4d\x00",
        /** Phông B */
        TXT_FONT_B: "\x1b\x4d\x01",
        /** Phông C (nếu hỗ trợ) */
        TXT_FONT_C: "\x1b\x4d\x02",
        // ---- TEXT ALIGNMENT ----
        /** Căn trái */
        TXT_ALIGN_LT: "\x1b\x61\x00",
        /** Căn giữa */
        TXT_ALIGN_CT: "\x1b\x61\x01",
        /** Căn phải */
        TXT_ALIGN_RT: "\x1b\x61\x02",
    },
};
