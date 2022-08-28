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

import jnr.posix.util.Platform;
import net.sf.sshapi.fuse.fs.BaseStruct;

/**
 * POSIX.1b structure for a time value.
 *
 * @author Sergey Tselovalnikov
 * @since 31.05.15
 */
public class Timespec extends BaseStruct {
    protected Timespec(jnr.ffi.Runtime runtime) {
        super(runtime);
        tv_sec = new time_t();
        if(!Platform.IS_WINDOWS) {
            tv_nsec = new SignedLong();
        } else {
            tv_nsec = new Signed64();
        }
    }

    public final time_t tv_sec;       /* seconds */
    public final NumberField tv_nsec; /* nanoseconds */

    public static Timespec of(jnr.ffi.Pointer pointer) {
        Timespec timespec = new Timespec(jnr.ffi.Runtime.getSystemRuntime());
        timespec.useMemory(pointer);
        return timespec;
    }
}
