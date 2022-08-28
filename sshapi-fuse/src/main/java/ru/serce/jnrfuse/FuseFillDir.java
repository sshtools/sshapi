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
package ru.serce.jnrfuse;

import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.types.off_t;
import ru.serce.jnrfuse.struct.FileStat;

import java.nio.ByteBuffer;

/**
 * Function to add an entry in a readdir() operation
 */
public interface FuseFillDir {
    @Delegate
    int apply(Pointer buf, ByteBuffer name, Pointer stbuf, @off_t long off);

    /**
     * Function to add an entry in a readdir() operation
     *
     * @param buf   the buffer passed to the readdir() operation
     * @param name  the file name of the directory entry
     * @param stbuf file attributes, can be NULL
     * @param off   offset of the next entry or zero
     *
     * @return 1 if buffer is full, zero otherwise
     */
    default int apply(Pointer buf, String name, FileStat stbuf, @off_t long off) {
        return apply(buf, ByteBuffer.wrap(name.getBytes()), stbuf == null ? null : Struct.getMemory(stbuf), off);
    }
}
