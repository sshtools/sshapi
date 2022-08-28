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
package net.sf.sshapi.fuse.fs;

import jnr.ffi.NativeType;
import jnr.ffi.Struct;
import jnr.posix.util.Platform;

public abstract class BaseStruct extends Struct {
    protected BaseStruct(jnr.ffi.Runtime runtime) {
        super(runtime);
    }

    public class Func<T> extends AbstractMember {
        private final Class<? extends T> closureClass;
        private T instance;

        public Func(Class<? extends T> closureClass) {
            super(NativeType.ADDRESS);
            this.closureClass = closureClass;
        }

        public final void set(T value) {
            getMemory().putPointer(offset(), getRuntime().getClosureManager().getClosurePointer(closureClass, instance = value));
        }
    }

    protected final <T> Func<T> func(Class<T> closureClass) {
        return new Func<>(closureClass);
    }

    public class fsblkcnt64_t extends NumberField {
        public fsblkcnt64_t() {
            super(Platform.IS_MAC ? NativeType.UINT : NativeType.SLONGLONG);
        }

        public fsblkcnt64_t(Offset offset) {
            super(Platform.IS_MAC ? NativeType.UINT : NativeType.SLONGLONG, offset);
        }

        public long get() {
            return Platform.IS_MAC ? getMemory().getInt(offset()) : getMemory().getLongLong(offset());
        }

        public void set(java.lang.Number value) {
            if (Platform.IS_MAC) {
                getMemory().putInt(offset(), value.intValue());
            } else {
                getMemory().putLongLong(offset(), value.longValue());
            }
        }

        @Override
        public int intValue() {
            return (int) get();
        }

        @Override
        public long longValue() {
            return get();
        }

        @Override
        public java.lang.String toString() {
            return java.lang.Long.toString(get());
        }
    }

    public class fsfilcnt64_t extends NumberField {
        public fsfilcnt64_t() {
            super(Platform.IS_MAC ? NativeType.UINT : NativeType.SLONGLONG);
        }

        public fsfilcnt64_t(Offset offset) {
            super(Platform.IS_MAC ? NativeType.UINT : NativeType.SLONGLONG, offset);
        }

        public long get() {
            return Platform.IS_MAC ? getMemory().getInt(offset()) : getMemory().getLongLong(offset());
        }

        public void set(java.lang.Number value) {
            if (Platform.IS_MAC) {
                getMemory().putInt(offset(), value.intValue());
            } else {
                getMemory().putLongLong(offset(), value.longValue());
            }
        }

        @Override
        public int intValue() {
            return (int) get();
        }


        @Override
        public long longValue() {
            return get();
        }

        @Override
        public java.lang.String toString() {
            return java.lang.Long.toString(get());
        }
    }

    public class __time_t extends SignedLong {
    }

    public class __off64_t extends Signed64 {
    }


}
