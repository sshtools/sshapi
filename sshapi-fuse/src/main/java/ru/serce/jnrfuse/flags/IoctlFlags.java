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
package ru.serce.jnrfuse.flags;

/**
 * @author Sergey Tselovalnikov
 * @since 05.06.15
 */
public final class IoctlFlags {

    // flags
    public static final int _IOC_NONE = 0;
    public static final int _IOC_READ = 2;
    public static final int _IOC_WRITE = 1;


    // macros
    public static final int _IOC_SIZEBITS = 14;
    public static final int _IOC_TYPEBITS = 8;
    public static final int _IOC_NRBITS = 8;
    public static final int _IOC_NRSHIFT = 0;

    public static int _IOC_TYPESHIFT() {
        return (_IOC_NRSHIFT + _IOC_NRBITS);
    }

    public static int _IOC_SIZESHIFT() {
        return (_IOC_TYPESHIFT() + _IOC_TYPEBITS);
    }

    public static int _IOC_SIZEMASK() {
        return ((1 << _IOC_SIZEBITS) - 1);
    }

    public static int _IOC_SIZE(int nr) {
        return (((nr) >> _IOC_SIZESHIFT()) & _IOC_SIZEMASK());
    }

    private IoctlFlags() {
    }
}
