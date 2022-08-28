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

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.CompositeTypeMapper;
import jnr.ffi.mapper.DefaultSignatureType;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.provider.ClosureManager;
import jnr.ffi.provider.jffi.ClosureFromNativeConverter;
import jnr.ffi.provider.jffi.SimpleNativeContext;
import ru.serce.jnrfuse.FuseException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClosureHelper {

    private final Object asmClassLoader;
    private final CompositeTypeMapper ctm;
    private final SimpleNativeContext ctx;
    private final Class<?> asmClassLoaderClass;
    private final ConcurrentHashMap<Class<?>, FromNativeConverter<?, Pointer>> cache = new ConcurrentHashMap<>();

    public static ClosureHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> FromNativeConverter<T, Pointer> getNativeConveter(Class<T> closureClass) {
        FromNativeConverter<T, Pointer> result = (FromNativeConverter<T, Pointer>) cache.get(closureClass);
        if (result != null) {
            return result;
        }
        DefaultSignatureType sig = DefaultSignatureType.create(closureClass, (FromNativeContext) ctx);
        
        try {
			result = (FromNativeConverter<T, Pointer>) ClosureFromNativeConverter.class.getMethod("getInstance", jnr.ffi.Runtime.class, SignatureType.class, asmClassLoaderClass, SignatureTypeMapper.class).invoke(null, Runtime.getSystemRuntime(), sig, asmClassLoader, ctm);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException("Failed to get native converter.", e);
		}
        cache.putIfAbsent(closureClass, result);
        return result;
    }

    public FromNativeContext getFromNativeContext() {
        return ctx;
    }


    private static class SingletonHolder {
        private static final ClosureHelper INSTANCE = new ClosureHelper();
    }

    @SuppressWarnings("unchecked")
	private ClosureHelper() {
        try {
            ClosureManager closureManager = jnr.ffi.Runtime.getSystemRuntime().getClosureManager();
			asmClassLoaderClass = getClass().getClassLoader().loadClass("jnr.ffi.provider.jffi.NativeClosureManager");
            Field asmClassLoadersField = asmClassLoaderClass.getDeclaredField("asmClassLoaders");
            asmClassLoadersField.setAccessible(true);

            Map<ClassLoader, Object> asmClassLoaders = (Map<ClassLoader, Object>) asmClassLoadersField.get(closureManager);
            asmClassLoader = asmClassLoaders.get(ClosureHelper.class.getClassLoader());

            Field typeMapperField = asmClassLoaderClass.getDeclaredField("typeMapper");
            typeMapperField.setAccessible(true);
            ctm = (CompositeTypeMapper) typeMapperField.get(closureManager);
            Constructor<SimpleNativeContext> constructor = SimpleNativeContext.class.getConstructor(Runtime.class, Collections.class);
			constructor.setAccessible(true);
			ctx = constructor.newInstance(Runtime.getSystemRuntime(), Collections.emptyList());
        } catch (Exception e) {
            throw new FuseException("Unable to create helper", e);
        }
    }
}
