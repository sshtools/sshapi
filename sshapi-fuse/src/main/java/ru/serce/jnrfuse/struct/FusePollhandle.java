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

/**
 * @see "fuse_lowlevel.c"
 *
 * <pre>
 * struct fuse_pollhandle {
 *   uint64_t kh;
 *   struct fuse_chan *ch;
 *   struct fuse_ll *f;
 * };
 * </pre>
 *
 * @author Sergey Tselovalnikov
 * @since 02.06.15
 */
public class FusePollhandle extends BaseStruct {
    protected FusePollhandle(jnr.ffi.Runtime runtime) {
        super(runtime);
    }

    public final Unsigned64 kh = new Unsigned64();
    // TODO struct fuse_chan *ch;
    public final Pointer ch = new Pointer();
    // TODO struct fuse_ll *f;
    public final Pointer f = new Pointer();

    public static FusePollhandle of(jnr.ffi.Pointer pointer) {
        FusePollhandle ph = new FusePollhandle(jnr.ffi.Runtime.getSystemRuntime());
        ph.useMemory(pointer);
        return ph;
    }
}
