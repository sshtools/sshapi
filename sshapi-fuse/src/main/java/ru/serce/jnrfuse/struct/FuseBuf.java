/**
 * Copyright (c) 2020 The JavaSSH Project
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package ru.serce.jnrfuse.struct;


import net.sf.sshapi.fuse.fs.BaseStruct;
import ru.serce.jnrfuse.flags.FuseBufFlags;

/**
 * Single data buffer
 * <p>
 * Generic data buffer for I/O, extended attributes, etc...  Data may
 * be supplied as a memory pointer or as a file descriptor
 */
public class FuseBuf extends BaseStruct {
    protected FuseBuf(jnr.ffi.Runtime runtime) {
        super(runtime);
    }

    /**
     * Size of data in bytes
     */
    public final size_t size = new size_t();

    /**
     * Buffer flags
     */
    public final Enum<FuseBufFlags> flags = new Enum<>(FuseBufFlags.class);

    /**
     * Memory pointer
     * <p>
     * Used unless FUSE_BUF_IS_FD flag is set.
     */
    public final Pointer mem = new Pointer();

    /**
     * File descriptor
     * <p>
     * Used if FUSE_BUF_IS_FD flag is set.
     */
    public final Signed32 fd = new Signed32();

    /**
     * File position
     * <p>
     * Used if FUSE_BUF_FD_SEEK flag is set.
     */
    public final off_t pos = new off_t();

    public static FuseBuf of(jnr.ffi.Pointer pointer) {
        FuseBuf buf = new FuseBuf(jnr.ffi.Runtime.getSystemRuntime());
        buf.useMemory(pointer);
        return buf;
    }
}
