import { Buffer } from "buffer";

export default class BufferHelper {
  buffers: Buffer[];
  size: number;

  constructor() {
    this.buffers = [];
    this.size = 0;
  }

  get length() {
    return this.size;
  }

  concat = (buffer: Buffer): BufferHelper => {
    this.buffers.push(buffer);
    this.size += buffer.length;
    return this;
  };

  empty = (): BufferHelper => {
    this.buffers = [];
    this.size = 0;
    return this;
  };

  toBuffer = (): Buffer => Buffer.concat(this.buffers, this.size);

  toString = (encoding: BufferEncoding): string =>
    this.toBuffer().toString(encoding);

  load = (
    stream: {
      on: (arg0: string, arg1: { (trunk: Buffer): void }) => void;
      once: (arg0: string, arg1: unknown) => void;
    },
    callback: (arg0: null, arg1: Buffer) => void,
  ) => {
    stream.on("data", (trunk: Buffer) => {
      this.concat(trunk);
    });
    stream.on("end", () => {
      callback(null, this.toBuffer());
    });
    stream.once("error", callback);
  };
}
