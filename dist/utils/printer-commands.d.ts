/**
 * Bộ lệnh ESC/POS cho máy in nhiệt với chú thích tiếng Việt cho từng trường.
 * Sử dụng cùng TypeScript để nhận gợi ý IntelliSense khi nhập lệnh.
 */
/** Đường kẻ ngang mẫu cho khổ giấy khác nhau */
export interface HorizontalLine {
    /** Đường kẻ khổ 58mm bằng dấu = */ HR_58MM: string;
    /** Đường kẻ khổ 58mm bằng dấu * */ HR2_58MM: string;
    /** Đường kẻ khổ 58mm bằng dấu - */ HR3_58MM: string;
    /** Đường kẻ khổ 80mm bằng dấu = */ HR_80MM: string;
    /** Đường kẻ khổ 80mm bằng dấu * */ HR2_80MM: string;
    /** Đường kẻ khổ 80mm bằng dấu - */ HR3_80MM: string;
}
/** Trình tự điều khiển nạp giấy/di chuyển con trỏ */
export interface FeedControlSequences {
    /** Xuống dòng (Line Feed) – in phần đệm và nhảy xuống dòng mới */ CTL_LF: string;
    /** Form Feed – nhảy đến trang/biểu mẫu tiếp theo (nếu hỗ trợ) */ CTL_FF: string;
    /** Carriage Return – đưa con trỏ về đầu dòng hiện tại */ CTL_CR: string;
    /** Tab ngang – nhảy đến vị trí tab tiếp theo */ CTL_HT: string;
    /** Tab dọc – nhảy đến vị trí tab dọc tiếp theo */ CTL_VT: string;
}
/** Khoảng cách dòng */
export interface LineSpacing {
    /** Đặt khoảng cách dòng về mặc định của máy */ LS_DEFAULT: string;
    /** Thiết lập khoảng cách dòng theo số chấm (ESC 3 n) – tùy máy */ LS_SET: string;
    /** Thiết lập khoảng cách dòng kiểu 1 (ESC 1) – khoảng cách lớn hơn */ LS_SET1: string;
}
/** Điều khiển phần cứng */
export interface Hardware {
    /** Khởi tạo máy in – xóa bộ đệm và trả về chế độ mặc định */ HW_INIT: string;
    /** Chọn máy in (nếu có nhiều) – thường dùng để bật thiết bị */ HW_SELECT: string;
    /** Reset phần cứng máy in */ HW_RESET: string;
}
/** Ngăn kéo đựng tiền (Cash Drawer) */
export interface CashDrawer {
    /** Phát xung tới chân 2 để bật ngăn kéo */ CD_KICK_2: string;
    /** Phát xung tới chân 5 để bật ngăn kéo */ CD_KICK_5: string;
}
/** Lề giấy */
export interface Margins {
    /** Thiết lập lề dưới (ESC O n) – tùy máy */ BOTTOM: string;
    /** Thiết lập lề trái (ESC l n) – tùy máy */ LEFT: string;
    /** Thiết lập lề phải (ESC Q n) – tùy máy */ RIGHT: string;
}
/** Cắt giấy */
export interface Paper {
    /** Cắt toàn phần */ PAPER_FULL_CUT: string;
    /** Cắt một phần */ PAPER_PART_CUT: string;
    /** Cắt kiểu A (GS V 65) – tùy máy */ PAPER_CUT_A: string;
    /** Cắt kiểu B (GS V 66) – tùy máy */ PAPER_CUT_B: string;
}
/** Tùy chọn hệ số 1..8 cho chiều cao/chiều rộng chữ */
export type EightSteps = {
    /** Hệ số 1x */ 1: string;
    /** Hệ số 2x */ 2: string;
    /** Hệ số 3x */ 3: string;
    /** Hệ số 4x */ 4: string;
    /** Hệ số 5x */ 5: string;
    /** Hệ số 6x */ 6: string;
    /** Hệ số 7x */ 7: string;
    /** Hệ số 8x */ 8: string;
};
/** Định dạng văn bản */
export interface TextFormat {
    /** Kích thước bình thường */ TXT_NORMAL: string;
    /** Chữ cao gấp đôi */ TXT_2HEIGHT: string;
    /** Chữ rộng gấp đôi */ TXT_2WIDTH: string;
    /** Chữ rộng và cao gấp đôi (4-square) */ TXT_4SQUARE: string;
    /**
     * Kích thước tuỳ chỉnh theo hệ số (1..8)
     * @param width Hệ số bề rộng (1..8)
     * @param height Hệ số chiều cao (1..8)
     * @returns Chuỗi lệnh ESC/POS tương ứng
     */
    TXT_CUSTOM_SIZE: (width: number, height: number) => string;
    /** Bảng hệ số chiều cao 1..8 */ TXT_HEIGHT: EightSteps;
    /** Bảng hệ số chiều rộng 1..8 */ TXT_WIDTH: EightSteps;
    /** Tắt gạch dưới */ TXT_UNDERL_OFF: string;
    /** Bật gạch dưới 1 chấm */ TXT_UNDERL_ON: string;
    /** Bật gạch dưới 2 chấm */ TXT_UNDERL2_ON: string;
    /** Tắt chữ đậm */ TXT_BOLD_OFF: string;
    /** Bật chữ đậm */ TXT_BOLD_ON: string;
    /** Tắt chữ nghiêng */ TXT_ITALIC_OFF: string;
    /** Bật chữ nghiêng */ TXT_ITALIC_ON: string;
    /** Phông A */ TXT_FONT_A: string;
    /** Phông B */ TXT_FONT_B: string;
    /** Phông C (nếu hỗ trợ) */ TXT_FONT_C: string;
    /** Căn trái */ TXT_ALIGN_LT: string;
    /** Căn giữa */ TXT_ALIGN_CT: string;
    /** Căn phải */ TXT_ALIGN_RT: string;
}
/**
 * Kiểu dữ liệu cho đối tượng COMMANDS
 */
export interface Commands {
    /** Xuống dòng (LF) */ LF: string;
    /** Escape (ESC) */ ESC: string;
    /** File Separator (FS) */ FS: string;
    /** Group Separator (GS) */ GS: string;
    /** Unit Separator (US) */ US: string;
    /** Form Feed (FF) */ FF: string;
    /** Data Link Escape (DLE) */ DLE: string;
    /** Device Control 1 (DC1) */ DC1: string;
    /** Device Control 4 (DC4) */ DC4: string;
    /** End Of Transmission (EOT) */ EOT: string;
    /** Null (NUL) */ NUL: string;
    /** Kết thúc dòng (\n) */ EOL: string;
    /** Đường kẻ ngang mẫu */ HORIZONTAL_LINE: HorizontalLine;
    /** Trình tự điều khiển nạp giấy/di chuyển */ FEED_CONTROL_SEQUENCES: FeedControlSequences;
    /** Khoảng cách dòng */ LINE_SPACING: LineSpacing;
    /** Điều khiển phần cứng */ HARDWARE: Hardware;
    /** Ngăn kéo đựng tiền */ CASH_DRAWER: CashDrawer;
    /** Lề giấy */ MARGINS: Margins;
    /** Cắt giấy */ PAPER: Paper;
    /** Định dạng văn bản */ TEXT_FORMAT: TextFormat;
}
/**
 * Đối tượng hằng chứa tất cả lệnh ESC/POS tiện dụng cho máy in nhiệt.
 * Hãy nhập COMMANDS. để xem gợi ý và mô tả từng trường bằng tiếng Việt.
 */
export declare const COMMANDS: Commands;
