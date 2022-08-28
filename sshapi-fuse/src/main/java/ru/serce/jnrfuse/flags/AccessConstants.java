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

import jnr.constants.platform.Access;

/**
 * Values for the second argument to access. These may be OR'd together.
 * <p>
 *
 * @author Sergey Tselovalnikov
 * @since 05.06.15
 */
public final class AccessConstants {
    public static final int R_OK = Access.R_OK.intValue(); /* Test for read permission.  */
    public static final int W_OK = Access.W_OK.intValue(); /* Test for write permission.  */
    public static final int X_OK = Access.X_OK.intValue(); /* Test for execute permission.  */
    public static final int F_OK = Access.F_OK.intValue(); /* Test for existence.  */

    private AccessConstants() {}
}
