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

import jnr.ffi.NativeType;
import jnr.posix.util.Platform;
import net.sf.sshapi.fuse.fs.BaseStruct;

/**
 * @author Sergey Tselovalnikov
 * @since 31.05.15
 */
public class Flock extends BaseStruct {

    public static final int LOCK_SH = 1;    /* Shared lock.  */
    public static final int LOCK_EX = 2;    /* Exclusive lock.  */
    public static final int LOCK_UN = 8;    /* Unlock.  */

    // lock types
    public static final int F_RDLCK = 0;	/* Read lock.  */
    public static final int F_WRLCK = 1;	/* Write lock.	*/
    public static final int F_UNLCK = 2;	/* Remove lock.	 */

    protected Flock(jnr.ffi.Runtime runtime) {
        super(runtime);
        if (Platform.IS_MAC) {
//            struct flock {
//                off_t	l_start;	/* starting offset */
//                off_t	l_len;		/* len = 0 means until end of file */
//                pid_t	l_pid;		/* lock owner */
//                short	l_type;		/* lock type: read/write, etc. */
//                short	l_whence;	/* type of l_start */
//            };
            l_start = new __off64_t();
            l_len = new __off64_t();
            l_pid = new pid_t();
            l_type = new Signed16();
            l_whence = new Signed16();
            pad = null;
        } else if (Platform.IS_WINDOWS) {
            l_type = new Signed16();
            l_whence = new Signed16();
            l_start = new __off64_t();
            l_len = new __off64_t();
            l_pid = new pid_t();
            pad = null;
        } else {
            l_type = new Signed16();
            l_whence = new Signed16();
            l_start = new __off64_t();
            l_len = new __off64_t();
            l_pid = new pid_t();
            pad = Platform.IS_64_BIT ? new Padding(NativeType.UCHAR, 4) : null;
        }

    }

    public final Signed16 l_type;     /* Type of lock: F_RDLCK, F_WRLCK, or F_UNLCK.	*/
    public final Signed16 l_whence;   /* Where `l_start' is relative to (like `lseek').  */
    public final __off64_t l_start;  /* Offset where the lock begins.  */
    public final __off64_t l_len;    /* Size of the locked area; zero means until EOF.  */
    public final pid_t l_pid;            /* Process holding the lock.  */
    private final Padding pad;  // for alighnment to 32

    public static Flock of(jnr.ffi.Pointer pointer) {
        Flock flock = new Flock(jnr.ffi.Runtime.getSystemRuntime());
        flock.useMemory(pointer);
        return flock;
    }
}
