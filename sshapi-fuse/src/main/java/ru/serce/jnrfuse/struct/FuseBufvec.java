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


import jnr.ffi.*;
import jnr.ffi.Runtime;
import net.sf.sshapi.fuse.fs.BaseStruct;

/**
 * Data buffer vector
 * <p>
 * An array of data buffers, each containing a memory pointer or a
 * file descriptor.
 * <p>
 * Allocate dynamically to add more than one buffer.
 *
 * @author Sergey Tselovalnikov
 * @since 02.06.15
 */
public class FuseBufvec extends BaseStruct {
    public FuseBufvec(jnr.ffi.Runtime runtime) {
        super(runtime);
    }

    /**
     * Number of buffers in the array
     */
    public final size_t count = new size_t();

    /**
     * Index of current buffer within the array
     */
    public final size_t idx = new size_t();

    /**
     * Current offset within the current buffer
     */
    public final size_t off = new size_t();

    /**
     * Array of buffers
     */
    public final FuseBuf buf = inner(new FuseBuf(getRuntime()));

    public static FuseBufvec of(jnr.ffi.Pointer pointer) {
        FuseBufvec buf = new FuseBufvec(jnr.ffi.Runtime.getSystemRuntime());
        buf.useMemory(pointer);
        return buf;
    }

    /**
     * Similar to FUSE_BUFVEC_INIT macros
     */
    public static void init(FuseBufvec buf, long size) {
        buf.count.set(1);
        buf.idx.set(0);
        buf.off.set(0);
        buf.buf.size.set(size);
        buf.buf.flags.set(0);
        buf.buf.mem.set(0);
        buf.buf.fd.set(-1);
        buf.buf.pos.set(0);
    }
}
