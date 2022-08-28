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

import jnr.ffi.Runtime;
import net.sf.sshapi.fuse.fs.BaseStruct;

/**
 * Extra context that may be needed by some filesystems
 *
 * The uid, gid and pid fields are not filled in case of a writepage
 * operation.
 */
public class FuseContext extends BaseStruct {
    /** Pointer to the fuse object */
    public final Pointer fuse = new Pointer();
    /** User ID of the calling process */
    public final uid_t uid = new uid_t();
    /** Group ID of the calling process */
    public final gid_t gid = new gid_t();
    /** Thread ID of the calling process */
    public final pid_t pid = new pid_t();
    /** Private filesystem data */
    public final Pointer private_data = new Pointer();
    /** Umask of the calling process (introduced in version 2.8) */
    public final mode_t umask = new mode_t();

    public FuseContext(Runtime runtime) {
        super(runtime);
    }

    public static FuseContext of(jnr.ffi.Pointer pointer) {
        FuseContext fc = new FuseContext(jnr.ffi.Runtime.getSystemRuntime());
        fc.useMemory(pointer);
        return fc;
    }
}