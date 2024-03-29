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
import jnr.ffi.types.size_t;
import jnr.ffi.types.ssize_t;
import ru.serce.jnrfuse.struct.FuseBufvec;
import ru.serce.jnrfuse.struct.FuseContext;
import ru.serce.jnrfuse.struct.FuseOperations;
import ru.serce.jnrfuse.struct.FusePollhandle;

/**
 * Library interface of FUSE
 *
 * @author Sergey Tselovalnikov
 * @since 03.06.15
 */
public interface LibFuse {

    @size_t
    long fuse_buf_size(FuseBufvec bufv);

    @ssize_t
    long fuse_buf_copy(FuseBufvec dstv, FuseBufvec srcv, int flags);

    // poll
    void fuse_pollhandle_destroy(FusePollhandle ph);

    int fuse_notify_poll(FusePollhandle ph);

    FuseContext fuse_get_context();

    /**
     * Flag session as terminated
     *
     * This function will cause any running event loops to exit on
     * the next opportunity.
     *
     * NOTE(win): On WinFsp this function does unmount the filesystem
     *
     * @param fuse the FUSE handle
     */
    void fuse_exit(Pointer fuse);

    /**
     * Main function of FUSE.
     * <p>
     * This function does the following:
     * - parses command line options (-d -s and -h)
     * - passes relevant mount options to the fuse_mount()
     * - installs signal handlers for INT, HUP, TERM and PIPE
     * - registers an exit handler to unmount the filesystem on program exit
     * - creates a fuse handle
     * - registers the operations
     * - calls either the single-threaded or the multi-threaded event loop
     *
     * @param argc      the argument counter passed to the main() function
     * @param argv      the argument vector passed to the main() function
     * @param op        the file system operation
     * @param user_data user data supplied in the context during the init() method
     * @return 0 on success, nonzero on failure
     */
    int fuse_main_real(int argc, String[] argv, FuseOperations op, int op_size, Pointer user_data);
}
