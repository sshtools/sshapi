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

import java.nio.file.Path;

/**
 * @author Sergey Tselovalnikov
 * @since 03.06.15
 */
public interface Mountable {
    /**
     * Mount is not safe to invoke multiple times in default implementation
     *
     * Add option -h to see list of all available options
     */
    void mount(Path mountPoint, boolean blocking, boolean debug, String[] fuseOpts);

    void umount();

    default void mount(Path mountPoint, boolean blocking, boolean debug) {
        mount(mountPoint, blocking, debug, new String[]{});
    }

    default void mount(Path mountPoint, boolean blocking) {
        mount(mountPoint, blocking, false);
    }

    default void mount(Path mountPoint) {
        mount(mountPoint, false);
    }
}
