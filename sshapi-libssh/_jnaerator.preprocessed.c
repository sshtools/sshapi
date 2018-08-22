

#line 1 "/usr/include/libssh/libssh.h" 1
/*
 * This file is part of the SSH Library
 *
 * Copyright (c) 2003-2009 by Aris Adamantiadis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */





  
    
  
    
      
        
      
        
      
    
      
        
      
        
      
    
  

  
    
  
    
  



  
  
  
  
  
  
  

  
#line 1 "/usr/include/unistd.h" 1
/* Copyright (C) 1991-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */

/*
 *	POSIX Standard: 2.10 Symbolic Constants		<unistd.h>
 */





#line 1 "/usr/include/features.h" 1
/* Copyright (C) 1991-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */




/* These are defined by the user (or the compiler)
   to specify the desired environment:

   __STRICT_ANSI__	ISO Standard C.
   _ISOC99_SOURCE	Extensions to ISO C89 from ISO C99.
   _ISOC11_SOURCE	Extensions to ISO C99 from ISO C11.
   __STDC_WANT_LIB_EXT2__
			Extensions to ISO C99 from TR 27431-2:2010.
   __STDC_WANT_IEC_60559_BFP_EXT__
			Extensions to ISO C11 from TS 18661-1:2014.
   __STDC_WANT_IEC_60559_FUNCS_EXT__
			Extensions to ISO C11 from TS 18661-4:2015.
   __STDC_WANT_IEC_60559_TYPES_EXT__
			Extensions to ISO C11 from TS 18661-3:2015.

   _POSIX_SOURCE	IEEE Std 1003.1.
   _POSIX_C_SOURCE	If ==1, like _POSIX_SOURCE; if >=2 add IEEE Std 1003.2;
			if >=199309L, add IEEE Std 1003.1b-1993;
			if >=199506L, add IEEE Std 1003.1c-1995;
			if >=200112L, all of IEEE 1003.1-2004
			if >=200809L, all of IEEE 1003.1-2008
   _XOPEN_SOURCE	Includes POSIX and XPG things.  Set to 500 if
			Single Unix conformance is wanted, to 600 for the
			sixth revision, to 700 for the seventh revision.
   _XOPEN_SOURCE_EXTENDED XPG things and X/Open Unix extensions.
   _LARGEFILE_SOURCE	Some more functions for correct standard I/O.
   _LARGEFILE64_SOURCE	Additional functionality from LFS for large files.
   _FILE_OFFSET_BITS=N	Select default filesystem interface.
   _ATFILE_SOURCE	Additional *at interfaces.
   _GNU_SOURCE		All of the above, plus GNU extensions.
   _DEFAULT_SOURCE	The default set of features (taking precedence over
			__STRICT_ANSI__).

   _FORTIFY_SOURCE	Add security hardening to many library functions.
			Set to 1 or 2; 2 performs stricter checks than 1.

   _REENTRANT, _THREAD_SAFE
			Obsolete; equivalent to _POSIX_C_SOURCE=199506L.

   The `-ansi' switch to the GNU C compiler, and standards conformance
   options such as `-std=c99', define __STRICT_ANSI__.  If none of
   these are defined, or if _DEFAULT_SOURCE is defined, the default is
   to have _POSIX_SOURCE set to one and _POSIX_C_SOURCE set to
   200809L, as well as enabling miscellaneous functions from BSD and
   SVID.  If more than one of these are defined, they accumulate.  For
   example __STRICT_ANSI__, _POSIX_SOURCE and _POSIX_C_SOURCE together
   give you ISO C, 1003.1, and 1003.2, but nothing else.

   These are defined by this file and are used by the
   header files to decide what to declare or define:

   __GLIBC_USE (F)	Define things from feature set F.  This is defined
			to 1 or 0; the subsequent macros are either defined
			or undefined, and those tests should be moved to
			__GLIBC_USE.
   __USE_ISOC11		Define ISO C11 things.
   __USE_ISOC99		Define ISO C99 things.
   __USE_ISOC95		Define ISO C90 AMD1 (C95) things.
   __USE_ISOCXX11	Define ISO C++11 things.
   __USE_POSIX		Define IEEE Std 1003.1 things.
   __USE_POSIX2		Define IEEE Std 1003.2 things.
   __USE_POSIX199309	Define IEEE Std 1003.1, and .1b things.
   __USE_POSIX199506	Define IEEE Std 1003.1, .1b, .1c and .1i things.
   __USE_XOPEN		Define XPG things.
   __USE_XOPEN_EXTENDED	Define X/Open Unix things.
   __USE_UNIX98		Define Single Unix V2 things.
   __USE_XOPEN2K        Define XPG6 things.
   __USE_XOPEN2KXSI     Define XPG6 XSI things.
   __USE_XOPEN2K8       Define XPG7 things.
   __USE_XOPEN2K8XSI    Define XPG7 XSI things.
   __USE_LARGEFILE	Define correct standard I/O things.
   __USE_LARGEFILE64	Define LFS things with separate names.
   __USE_FILE_OFFSET64	Define 64bit interface as default.
   __USE_MISC		Define things from 4.3BSD or System V Unix.
   __USE_ATFILE		Define *at interfaces and AT_* constants for them.
   __USE_GNU		Define GNU extensions.
   __USE_FORTIFY_LEVEL	Additional security measures used, according to level.

   The macros `__GNU_LIBRARY__', `__GLIBC__', and `__GLIBC_MINOR__' are
   defined by this file unconditionally.  `__GNU_LIBRARY__' is provided
   only for compatibility.  All new code should use the other symbols
   to test for features.

   All macros listed above as possibly being defined by this file are
   explicitly undefined if they are not explicitly defined.
   Feature-test macros that are not defined by the user or compiler
   but are implied by the other feature-test macros defined (or by the
   lack of any definitions) are defined by the file.

   ISO C feature test macros depend on the definition of the macro
   when an affected header is included, not when the first system
   header is included, and so they are handled in
   <bits/libc-header-start.h>, which does not have a multiple include
   guard.  Feature test macros that can be handled from the first
   system header included are handled here.  */


/* Undefine everything, so we get a clean slate.  */

























/* Suppress kernel-name space pollution unless user expressedly asks
   for it.  */




/* Convenience macro to test the version of gcc.
   Use like this:
   #if __GNUC_PREREQ (2,8)
   ... code requiring gcc 2.8 or later ...
   #endif
   Note: only works for GCC 2.0 and later, because __GNUC_MINOR__ was
   added in 2.0.  */







/* Similarly for clang.  Features added to GCC after version 4.2 may
   or may not also be available in clang, and clang's definitions of
   __GNUC(_MINOR)__ are fixed at 4 and 2 respectively.  Not all such
   features can be queried via __has_extension/__has_feature.  */







/* Whether to use feature set F.  */


/* _BSD_SOURCE and _SVID_SOURCE are deprecated aliases for
   _DEFAULT_SOURCE.  If _DEFAULT_SOURCE is present we do not
   issue a warning; the expectation is that the source is being
   transitioned to use the new macro.  */







/* If _GNU_SOURCE was defined by the user, turn on all the other features.  */























/* If nothing (other than _GNU_SOURCE and _DEFAULT_SOURCE) is defined,
   define _DEFAULT_SOURCE.  */









/* This is to enable the ISO C11 extension.  */





/* This is to enable the ISO C99 extension.  */





/* This is to enable the ISO C90 Amendment 1:1995 extension.  */






/* This is to enable compatibility for ISO C++17.  */



/* This is to enable compatibility for ISO C++11.
   Check the temporary macro for now, too.  */






/* If none of the ANSI/POSIX macros are defined, or if _DEFAULT_SOURCE
   is defined, use POSIX.1-2008 (or another version depending on
   _XOPEN_SOURCE).  */


























/* Some C libraries once required _REENTRANT and/or _THREAD_SAFE to be
   defined in all multithreaded code.  GNU libc has not required this
   for many years.  We now treat them as compatibility synonyms for
   _POSIX_C_SOURCE=199506L, which is the earliest level of POSIX with
   comprehensive support for multithreaded code.  Using them never
   lowers the selected level of POSIX conformance, only raises it.  */




































































































/* The function 'gets' existed in C89, but is impossible to use
   safely.  It has been removed from ISO C11 and ISO C++14.  Note: for
   compatibility with various implementations of <cstdio>, this test
   must consider only the value of __cplusplus when compiling C++.  */






/* Get definitions of __STDC_* predefined macros, if the compiler has
   not preincluded this header automatically.  */

#line 1 "/usr/include/stdc-predef.h" 1
/* Copyright (C) 1991-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */




/* This header is separate from features.h so that the compiler can
   include it implicitly at the start of every compilation.  It must
   not itself include <features.h> or any other header that includes
   <features.h> because the implicit include comes before any feature
   test macros that may be defined in a source file before it first
   explicitly includes a system header.  GCC knows the name of this
   header in order to preinclude it.  */

/* glibc's intent is to support the IEC 559 math functionality, real
   and complex.  If the GCC (4.9 and later) predefined macros
   specifying compiler intent are available, use them to determine
   whether the overall intent is to support these features; otherwise,
   presume an older compiler has intent to support these features and
   define these macros by default.  */

















/* wchar_t uses Unicode 10.0.0.  Version 10.0 of the Unicode Standard is
   synchronized with ISO/IEC 10646:2017, fifth edition, plus
   the following additions from Amendment 1 to the fifth edition:
   - 56 emoji characters
   - 285 hentaigana
   - 3 additional Zanabazar Square characters */


/* We do not support C11 <threads.h>.  */




#line 212 "/usr/include/features.h" 2

/* This macro indicates that the installed library is the GNU C Library.
   For historic reasons the value now is 6 and this will stay from now
   on.  The use of this variable is deprecated.  Use __GLIBC__ and
   __GLIBC_MINOR__ now (see below) when you want to test for a specific
   GNU C library version and use the values in <gnu/lib-names.h> to get
   the sonames of the shared libraries.  */



/* Major and minor version number of the GNU C library package.  Use
   these macros to test for features in specific releases.  */






/* This is here only because every header file already includes this one.  */



#line 1 "/usr/include/sys/cdefs.h" 1
/* Copyright (C) 1992-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */




/* We are almost always included from features.h. */




/* The GNU libc does not support any K&R compilers or the traditional mode
   of ISO C compilers anymore.  Check for some of the combinations not
   anymore supported.  */




/* Some user header file might have defined this before.  */





/* All functions, except those with callbacks or those that
   synchronize memory, are leaf functions.  */








/* GCC can always grok prototypes.  For C++ programs we add throw()
   to help it optimize the function calls.  But this works only with
   gcc 2.8.x and egcs.  For gcc 3.2 and up we even mark C functions
   as non-throwing using a function attribute since programs can use
   the -fexceptions options for C code as well.  */





























/* Compilers that are not clang may object to
       #if defined __clang__ && __has_extension(...)
   even though they do not need to evaluate the right-hand side of the &&.  */






/* These two macros are not used in glibc anymore.  They are kept here
   only because some other projects expect the macros to be defined.  */



/* For these things, GCC behaves the ANSI way normally,
   and the non-ANSI way under -traditional.  */




/* This is not a typedef so `const __ptr_t' does the right thing.  */



/* C++ needs to know that types and declarations are C, not C++.  */









/* Fortify support.  */















/* Support for flexible arrays.
   Headers that should use flexible arrays only if they're "real"
   (e.g. only if they won't affect sizeof()) should test
   #if __glibc_c99_flexarr_available.  */









/* Pre-2.97 GCC did not support C99 flexible arrays but did have
   an equivalent extension with slightly different notation.  */









/* __asm__ ("xyz") is used throughout the headers to rename functions
   at the assembly language level.  This is wrapped by the __REDIRECT
   macro, in order to support compilers that can do this some other
   way.  When compilers don't support asm-names at all, we have to do
   preprocessor tricks instead (which don't have exactly the right
   semantics, but it's the best we can do).

   Example:
   int __REDIRECT(setpgrp, (__pid_t pid, __pid_t pgrp), setpgid); */


























/* GCC has various useful declarations that can be made with the
   `__attribute__' syntax.  All of the ways we use this do fine if
   they are omitted for compilers that don't understand it. */




/* At some point during the gcc 2.96 development the `malloc' attribute
   for functions was introduced.  We don't want to use it unconditionally
   (although this would be possible) since it generates warnings.  */






/* Tell the compiler which arguments to an allocation function
   indicate the size of the allocation.  */







/* At some point during the gcc 2.96 development the `pure' attribute
   for functions was introduced.  We don't want to use it unconditionally
   (although this would be possible) since it generates warnings.  */






/* This declaration tells the compiler that the value is constant.  */






/* At some point during the gcc 3.1 development the `used' attribute
   for functions was introduced.  We don't want to use it unconditionally
   (although this would be possible) since it generates warnings.  */








/* Since version 3.2, gcc allows marking deprecated functions.  */






/* Since version 4.5, gcc also allows one to specify the message printed
   when a deprecated function is used.  clang claims to be gcc 4.2, but
   may also support this feature.  */








/* At some point during the gcc 2.8 development the `format_arg' attribute
   for functions was introduced.  We don't want to use it unconditionally
   (although this would be possible) since it generates warnings.
   If several `format_arg' attributes are given for the same function, in
   gcc-3.0 and older, all but the last one are ignored.  In newer gccs,
   all designated arguments are considered.  */






/* At some point during the gcc 2.97 development the `strfmon' format
   attribute for functions was introduced.  We don't want to use it
   unconditionally (although this would be possible) since it
   generates warnings.  */







/* The nonull function attribute allows to mark pointer parameters which
   must not be NULL.  */






/* If fortification mode, we warn about unused results of certain
   function calls which can lead to problems.  */













/* Forces a function to be always inlined.  */











/* Associate error messages with the source location of the call site rather
   than with the source location inside the function.  */






/* GCC 4.3 and above with -std=c99 or -std=gnu99 implements ISO C99
   inline semantics, unless -fgnu89-inline is used.  Using __GNUC_STDC_INLINE__
   or __GNUC_GNU_INLINE is not a good enough check for gcc because gcc versions
   older than 4.3 may define these macros and still not guarantee GNU inlining
   semantics.

   clang++ identifies itself as gcc-4.2, but has support for GNU inlining
   semantics, that can be checked fot by using the __GNUC_STDC_INLINE_ and
   __GNUC_GNU_INLINE__ macro definitions.  */

















/* GCC 4.3 and above allow passing all anonymous arguments of an
   __extern_always_inline function to some other vararg function.  */





/* It is possible to compile containing GCC extensions even if GCC is
   run in pedantic mode if the uses are carefully marked using the
   `__extension__' keyword.  But this is not generally available before
   version 2.8.  */




/* __restrict is known in EGCS 1.2 and above. */




/* ISO C99 also allows to declare arrays as non-overlapping.  The syntax is
     array_name[restrict]
   GCC 3.1 supports this.  */



















































#line 1 "/usr/include/bits/wordsize.h" 1
/* Determine the wordsize from the preprocessor defines.  */











/* Both x86-64 and x32 use the 64-bit system call interface.  */





#line 294 "/usr/include/sys/cdefs.h" 2

#line 1 "/usr/include/bits/long-double.h" 1
/* Properties of long double type.  ldbl-96 version.
   Copyright (C) 2016-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License  published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */

/* long double is distinct from double, so there is nothing to
   define here.  */

#line 295 "/usr/include/sys/cdefs.h" 2

































/* __glibc_macro_warning (MESSAGE) issues warning MESSAGE.  This is
   intended for use in preprocessor macros.

   Note: MESSAGE must be a _single_ string; concatenation of string
   literals is not supported.  */








/* Generic selection (ISO C11) is a C-only feature, available in GCC
   since version 4.9.  Previous versions do not provide generic
   selection, even though they might set __STDC_VERSION__ to 201112L,
   when in -std=c11 mode.  Thus, we must check for !defined __GNUC__
   when testing __STDC_VERSION__ for generic selection support.
   On the other hand, Clang also defines __GNUC__, so a clang-specific
   check is required to enable the use of generic selection.  */












#line 228 "/usr/include/features.h" 2


/* If we don't have __REDIRECT, prototypes will be missing if
   __USE_FILE_OFFSET64 but not __USE_LARGEFILE[64]. */







/* Decide whether we can define 'extern inline' functions in headers.  */







/* This is here only because every header file already includes this one.
   Get the definitions of all the appropriate `__stub_FUNCTION' symbols.
   <gnu/stubs.h> contains `#define __stub_FUNCTION' when FUNCTION is a stub
   that will always return failure (and set errno to ENOSYS).  */

#line 1 "/usr/include/gnu/stubs.h" 1
/* This file is automatically generated.
   This file selects the right generated file of `__stub_FUNCTION' macros
   based on the architecture being compiled for.  */












#line 244 "/usr/include/features.h" 2




#line 25 "/usr/include/unistd.h" 2

extern "C" {

/* These may be used to determine what facilities are present at compile time.
   Their values can be obtained at run time from `sysconf'.  */


/* POSIX Standard approved as ISO/IEC 9945-1 as of September 2008.  */















/* These are not #ifdef __USE_POSIX2 because they are
   in the theoretically application-owned namespace.  */



/* The utilities on GNU systems also correspond to this version.  */











/* The utilities on GNU systems also correspond to this version.  */


/* This symbol was required until the 2001 edition of POSIX.  */


/* If defined, the implementation supports the
   C Language Bindings Option.  */


/* If defined, the implementation supports the
   C Language Development Utilities Option.  */


/* If defined, the implementation supports the
   Software Development Utilities Option.  */


/* If defined, the implementation supports the
   creation of locales with the localedef utility.  */


/* X/Open version number to which the library conforms.  It is selectable.  */










/* Commands and utilities from XPG4 are available.  */


/* We are compatible with the old published standards as well.  */




/* The X/Open Unix extensions are available.  */


/* Encryption is present.  */


/* The enhanced internationalization capabilities according to XPG4.2
   are present.  */


/* The legacy interfaces are also available.  */



/* Get values of POSIX options:

   If these symbols are defined, the corresponding features are
   always available.  If not, they may be available sometimes.
   The current values can be obtained with `sysconf'.

   _POSIX_JOB_CONTROL		Job control is supported.
   _POSIX_SAVED_IDS		Processes have a saved set-user-ID
				and a saved set-group-ID.
   _POSIX_REALTIME_SIGNALS	Real-time, queued signals are supported.
   _POSIX_PRIORITY_SCHEDULING	Priority scheduling is supported.
   _POSIX_TIMERS		POSIX.4 clocks and timers are supported.
   _POSIX_ASYNCHRONOUS_IO	Asynchronous I/O is supported.
   _POSIX_PRIORITIZED_IO	Prioritized asynchronous I/O is supported.
   _POSIX_SYNCHRONIZED_IO	Synchronizing file data is supported.
   _POSIX_FSYNC			The fsync function is present.
   _POSIX_MAPPED_FILES		Mapping of files to memory is supported.
   _POSIX_MEMLOCK		Locking of all memory is supported.
   _POSIX_MEMLOCK_RANGE		Locking of ranges of memory is supported.
   _POSIX_MEMORY_PROTECTION	Setting of memory protections is supported.
   _POSIX_MESSAGE_PASSING	POSIX.4 message queues are supported.
   _POSIX_SEMAPHORES		POSIX.4 counting semaphores are supported.
   _POSIX_SHARED_MEMORY_OBJECTS	POSIX.4 shared memory objects are supported.
   _POSIX_THREADS		POSIX.1c pthreads are supported.
   _POSIX_THREAD_ATTR_STACKADDR	Thread stack address attribute option supported.
   _POSIX_THREAD_ATTR_STACKSIZE	Thread stack size attribute option supported.
   _POSIX_THREAD_SAFE_FUNCTIONS	Thread-safe functions are supported.
   _POSIX_THREAD_PRIORITY_SCHEDULING
				POSIX.1c thread execution scheduling supported.
   _POSIX_THREAD_PRIO_INHERIT	Thread priority inheritance option supported.
   _POSIX_THREAD_PRIO_PROTECT	Thread priority protection option supported.
   _POSIX_THREAD_PROCESS_SHARED	Process-shared synchronization supported.
   _POSIX_PII			Protocol-independent interfaces are supported.
   _POSIX_PII_XTI		XTI protocol-indep. interfaces are supported.
   _POSIX_PII_SOCKET		Socket protocol-indep. interfaces are supported.
   _POSIX_PII_INTERNET		Internet family of protocols supported.
   _POSIX_PII_INTERNET_STREAM	Connection-mode Internet protocol supported.
   _POSIX_PII_INTERNET_DGRAM	Connectionless Internet protocol supported.
   _POSIX_PII_OSI		ISO/OSI family of protocols supported.
   _POSIX_PII_OSI_COTS		Connection-mode ISO/OSI service supported.
   _POSIX_PII_OSI_CLTS		Connectionless ISO/OSI service supported.
   _POSIX_POLL			Implementation supports `poll' function.
   _POSIX_SELECT		Implementation supports `select' and `pselect'.

   _XOPEN_REALTIME		X/Open realtime support is available.
   _XOPEN_REALTIME_THREADS	X/Open realtime thread support is available.
   _XOPEN_SHM			Shared memory interface according to XPG4.2.

   _XBS5_ILP32_OFF32		Implementation provides environment with 32-bit
				int, long, pointer, and off_t types.
   _XBS5_ILP32_OFFBIG		Implementation provides environment with 32-bit
				int, long, and pointer and off_t with at least
				64 bits.
   _XBS5_LP64_OFF64		Implementation provides environment with 32-bit
				int, and 64-bit long, pointer, and off_t types.
   _XBS5_LPBIG_OFFBIG		Implementation provides environment with at
				least 32 bits int and long, pointer, and off_t
				with at least 64 bits.

   If any of these symbols is defined as -1, the corresponding option is not
   true for any file.  If any is defined as other than -1, the corresponding
   option is true for all files.  If a symbol is not defined at all, the value
   for a specific file can be obtained from `pathconf' and `fpathconf'.

   _POSIX_CHOWN_RESTRICTED	Only the super user can use `chown' to change
				the owner of a file.  `chown' can only be used
				to change the group ID of a file to a group of
				which the calling process is a member.
   _POSIX_NO_TRUNC		Pathname components longer than
				NAME_MAX generate an error.
   _POSIX_VDISABLE		If defined, if the value of an element of the
				`c_cc' member of `struct termios' is
				_POSIX_VDISABLE, no character will have the
				effect associated with that element.
   _POSIX_SYNC_IO		Synchronous I/O may be performed.
   _POSIX_ASYNC_IO		Asynchronous I/O may be performed.
   _POSIX_PRIO_IO		Prioritized Asynchronous I/O may be performed.

   Support for the Large File Support interface is not generally available.
   If it is available the following constants are defined to one.
   _LFS64_LARGEFILE		Low-level I/O supports large files.
   _LFS64_STDIO			Standard I/O supports large files.
   */


#line 1 "/usr/include/bits/posix_opt.h" 1
/* Define POSIX options for Linux.
   Copyright (C) 1996-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License as
   published by the Free Software Foundation; either version 2.1 of the
   License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; see the file COPYING.LIB.  If
   not, see <http://www.gnu.org/licenses/>.  */




/* Job control is supported.  */


/* Processes have a saved set-user-ID and a saved set-group-ID.  */


/* Priority scheduling is supported.  */


/* Synchronizing file data is supported.  */


/* The fsync function is present.  */


/* Mapping of files to memory is supported.  */


/* Locking of all memory is supported.  */


/* Locking of ranges of memory is supported.  */


/* Setting of memory protections is supported.  */


/* Some filesystems allow all users to change file ownership.  */


/* `c_cc' member of 'struct termios' structure can be disabled by
   using the value _POSIX_VDISABLE.  */


/* Filenames are not silently truncated.  */


/* X/Open realtime support is available.  */


/* X/Open thread realtime support is available.  */


/* XPG4.2 shared memory is supported.  */


/* Tell we have POSIX threads.  */


/* We have the reentrant functions described in POSIX.  */



/* We provide priority scheduling for threads.  */


/* We support user-defined stack sizes.  */


/* We support user-defined stacks.  */


/* We support priority inheritence.  */


/* We support priority protection, though only for non-robust
   mutexes.  */



/* We support priority inheritence for robust mutexes.  */


/* We do not support priority protection for robust mutexes.  */



/* We support POSIX.1b semaphores.  */


/* Real-time signals are supported.  */


/* We support asynchronous I/O.  */


/* Alternative name for Unix98.  */

/* Support for prioritization is also available.  */


/* The LFS support in asynchronous I/O is also available.  */


/* The rest of the LFS is also available.  */




/* POSIX shared memory objects are implemented.  */


/* CPU-time clocks support needs to be checked at runtime.  */


/* Clock support in threads must be also checked at runtime.  */


/* GNU libc provides regular expression handling.  */


/* Reader/Writer locks are available.  */


/* We have a POSIX shell.  */


/* We support the Timeouts option.  */


/* We support spinlocks.  */


/* The `spawn' function family is supported.  */


/* We have POSIX timers.  */


/* The barrier functions are available.  */


/* POSIX message queues are available.  */


/* Thread process-shared synchronization is supported.  */


/* The monotonic clock might be available.  */


/* The clock selection interfaces are available.  */


/* Advisory information interfaces are available.  */


/* IPv6 support is available.  */


/* Raw socket support is available.  */


/* We have at least one terminal.  */


/* Neither process nor thread sporadic server interfaces is available.  */



/* trace.h is not available.  */





/* Typed memory objects are not available.  */




#line 161 "/usr/include/unistd.h" 2

/* Get the environment definitions from Unix98.  */


#line 1 "/usr/include/bits/environments.h" 1
/* Copyright (C) 1999-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */






#line 1 "/usr/include/bits/environments.h" 1

/* This header should define the following symbols under the described
   situations.  A value `1' means that the model is always supported,
   `-1' means it is never supported.  Undefined means it cannot be
   statically decided.

   _POSIX_V7_ILP32_OFF32   32bit int, long, pointers, and off_t type
   _POSIX_V7_ILP32_OFFBIG  32bit int, long, and pointers and larger off_t type

   _POSIX_V7_LP64_OFF32	   64bit long and pointers and 32bit off_t type
   _POSIX_V7_LPBIG_OFFBIG  64bit long and pointers and large off_t type

   The macros _POSIX_V6_ILP32_OFF32, _POSIX_V6_ILP32_OFFBIG,
   _POSIX_V6_LP64_OFF32, _POSIX_V6_LPBIG_OFFBIG, _XBS5_ILP32_OFF32,
   _XBS5_ILP32_OFFBIG, _XBS5_LP64_OFF32, and _XBS5_LPBIG_OFFBIG were
   used in previous versions of the Unix standard and are available
   only for compatibility.
*/



/* Environments with 32-bit wide pointers are optionally provided.
   Therefore following macros aren't defined:
   # undef _POSIX_V7_ILP32_OFF32
   # undef _POSIX_V7_ILP32_OFFBIG
   # undef _POSIX_V6_ILP32_OFF32
   # undef _POSIX_V6_ILP32_OFFBIG
   # undef _XBS5_ILP32_OFF32
   # undef _XBS5_ILP32_OFFBIG
   and users need to check at runtime.  */

/* We also have no use (for now) for an environment with bigger pointers
   and offsets.  */




/* By default we have 64-bit wide `long int', pointers and `off_t'.  */














































#line 164 "/usr/include/unistd.h" 2


/* Standard file descriptors.  */





/* All functions that are not declared anywhere else.  */


#line 1 "/usr/include/bits/types.h" 1
/* bits/types.h -- definitions of __*_t types underlying *_t types.
   Copyright (C) 2002-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */

/*
 * Never include this file directly; use <sys/types.h> instead.
 */





#line 1 "/usr/include/bits/types.h" 1

#line 1 "/usr/include/bits/types.h" 1

/* Convenience types.  */
typedef unsigned char __u_char;
typedef unsigned short int __u_short;
typedef unsigned int __u_int;
typedef unsigned long int __u_long;

/* Fixed-size types, underlying types depend on word size and compiler.  */
typedef signed char __int8_t;
typedef unsigned char __uint8_t;
typedef signed short int __int16_t;
typedef unsigned short int __uint16_t;
typedef signed int __int32_t;
typedef unsigned int __uint32_t;

typedef signed long int __int64_t;
typedef unsigned long int __uint64_t;





/* quad_t is also 64 bits.  */

typedef long int __quad_t;
typedef unsigned long int __u_quad_t;





/* Largest integral types.  */

typedef long int __intmax_t;
typedef unsigned long int __uintmax_t;






/* The machine-dependent file <bits/typesizes.h> defines __*_T_TYPE
   macros for each of the OS types we define below.  The definitions
   of those macros must use the following macros for underlying types.
   We define __S<SIZE>_TYPE and __U<SIZE>_TYPE for the signed and unsigned
   variants of each of the following integer types on this machine.

	16		-- "natural" 16-bit type (always short)
	32		-- "natural" 32-bit type (always int)
	64		-- "natural" 64-bit type (long or long long)
	LONG32		-- 32-bit type, traditionally long
	QUAD		-- 64-bit type, always long long
	WORD		-- natural type of __WORDSIZE bits (int or long)
	LONGWORD	-- type of __WORDSIZE bits, traditionally long

   We distinguish WORD/LONGWORD, 32/LONG32, and 64/QUAD so that the
   conventional uses of `long' or `long long' type modifiers match the
   types we define, even when a less-adorned type would be the same size.
   This matters for (somewhat) portably writing printf/scanf formats for
   these types, where using the appropriate l or ll format modifiers can
   make the typedefs and the formats match up across all GNU platforms.  If
   we used `long' when it's 64 bits where `long long' is expected, then the
   compiler would warn about the formats not matching the argument types,
   and the programmer changing them to shut up the compiler would break the
   program's portability.

   Here we assume what is presently the case in all the GCC configurations
   we support: long long is always 64 bits, long is always word/address size,
   and int is always 32 bits.  */




























/* No need to mark the typedef with __extension__.   */





#line 1 "/usr/include/bits/typesizes.h" 1
/* bits/typesizes.h -- underlying types for *_t.  Linux/x86-64 version.
   Copyright (C) 2012-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */








/* See <bits/types.h> for the meaning of these macros.  This file exists so
   that <bits/types.h> need not vary across different GNU platforms.  */

/* X32 kernel interface is 64-bit.  */















































/* Tell the libc code that off_t and off64_t are actually the same type
   for all ABI purposes, even if possibly expressed as different base types
   for C type-checking purposes.  */


/* Same for ino_t and ino64_t.  */


/* And for __rlim_t and __rlim64_t.  */





/* Number of descriptors that can fit in an `fd_set'.  */





#line 92 "/usr/include/bits/types.h" 2


typedef unsigned long int __dev_t;	/* Type of device numbers.  */
typedef unsigned int __uid_t;	/* Type of user identifications.  */
typedef unsigned int __gid_t;	/* Type of group identifications.  */
typedef unsigned long int __ino_t;	/* Type of file serial numbers.  */
typedef unsigned long int __ino64_t;	/* Type of file serial numbers (LFS).*/
typedef unsigned int __mode_t;	/* Type of file attribute bitmasks.  */
typedef unsigned long int __nlink_t;	/* Type of file link counts.  */
typedef long int __off_t;	/* Type of file sizes and offsets.  */
typedef long int __off64_t;	/* Type of file sizes and offsets (LFS).  */
typedef int __pid_t;	/* Type of process identifications.  */
typedef struct { int __val[2]; } __fsid_t;	/* Type of file system IDs.  */
typedef long int __clock_t;	/* Type of CPU usage counts.  */
typedef unsigned long int __rlim_t;	/* Type for resource measurement.  */
typedef unsigned long int __rlim64_t;	/* Type for resource measurement (LFS).  */
typedef unsigned int __id_t;		/* General type for IDs.  */
typedef long int __time_t;	/* Seconds since the Epoch.  */
typedef unsigned int __useconds_t; /* Count of microseconds.  */
typedef long int __suseconds_t; /* Signed count of microseconds.  */

typedef int __daddr_t;	/* The type of a disk address.  */
typedef int __key_t;	/* Type of an IPC key.  */

/* Clock ID used in clock and timer functions.  */
typedef int __clockid_t;

/* Timer ID returned by `timer_create'.  */
typedef void * __timer_t;

/* Type to represent block size.  */
typedef long int __blksize_t;

/* Types from the Large File Support interface.  */

/* Type to count number of disk blocks.  */
typedef long int __blkcnt_t;
typedef long int __blkcnt64_t;

/* Type to count file system blocks.  */
typedef unsigned long int __fsblkcnt_t;
typedef unsigned long int __fsblkcnt64_t;

/* Type to count file system nodes.  */
typedef unsigned long int __fsfilcnt_t;
typedef unsigned long int __fsfilcnt64_t;

/* Type of miscellaneous file system fields.  */
typedef long int __fsword_t;

typedef long int __ssize_t; /* Type of a byte count, or error.  */

/* Signed long type used in system calls.  */
typedef long int __syscall_slong_t;
/* Unsigned long type used in system calls.  */
typedef unsigned long int __syscall_ulong_t;

/* These few don't really vary by system, they always correspond
   to one of the other defined types.  */
typedef __off64_t __loff_t;	/* Type of file sizes and offsets (LFS).  */
typedef char *__caddr_t;

/* Duplicates info from stdint.h but this is used in unistd.h.  */
typedef long int __intptr_t;

/* Duplicate info from sys/socket.h.  */
typedef unsigned int __socklen_t;

/* C99: An integer type that can be accessed as an atomic entity,
   even in the presence of asynchronous interrupts.
   It is not currently necessary for this to be machine-specific.  */
typedef int __sig_atomic_t;





#line 174 "/usr/include/unistd.h" 2


typedef __ssize_t ssize_t;






#line 1 "/usr/include/unistd.h" 1


/* The Single Unix specification says that some more types are
   available here.  */

typedef __gid_t gid_t;




typedef __uid_t uid_t;





typedef __off_t off_t;











typedef __useconds_t useconds_t;




typedef __pid_t pid_t;






typedef __intptr_t intptr_t;






typedef __socklen_t socklen_t;




/* Values for the second argument to access.
   These may be OR'd together.  */





/* Test for access to NAME using the real UID and real GID.  */
extern int access (const char *__name, int __type)  ;





     



     



/* Test for access to FILE relative to the directory FD is open on.
   If AT_EACCESS is set in FLAG, then use effective IDs like `eaccess',
   otherwise use real IDs like `access'.  */
extern int faccessat (int __fd, const char *__file, int __type, int __flag)
       ;



/* Values for the WHENCE argument to lseek.  */











/* Old BSD names for the same constants; just for compatibility.  */






/* Move FD's file position to OFFSET bytes from the
   beginning of the file (if WHENCE is SEEK_SET),
   the current position (if WHENCE is SEEK_CUR),
   or the end of the file (if WHENCE is SEEK_END).
   Return the new file position.  */

extern __off_t lseek (int __fd, __off_t __offset, int __whence) ;



				 
				 






     


/* Close the file descriptor FD.

   This function is a cancellation point and therefore not marked with
   __THROW.  */
extern int close (int __fd);

/* Read NBYTES into BUF from FD.  Return the
   number read, -1 for errors or 0 for EOF.

   This function is a cancellation point and therefore not marked with
   __THROW.  */
extern ssize_t read (int __fd, void *__buf, size_t __nbytes) ;

/* Write N bytes of BUF to FD.  Return the number written, or -1.

   This function is a cancellation point and therefore not marked with
   __THROW.  */
extern ssize_t write (int __fd, const void *__buf, size_t __n) ;



/* Read NBYTES into BUF from FD at the given position OFFSET without
   changing the file pointer.  Return the number read, -1 for errors
   or 0 for EOF.

   This function is a cancellation point and therefore not marked with
   __THROW.  */
extern ssize_t pread (int __fd, void *__buf, size_t __nbytes,
		      __off_t __offset) ;

/* Write N bytes of BUF to FD at the given position OFFSET without
   changing the file pointer.  Return the number written, or -1.

   This function is a cancellation point and therefore not marked with
   __THROW.  */
extern ssize_t pwrite (int __fd, const void *__buf, size_t __n,
		       __off_t __offset) ;



				   
			   

				    
			   











			



			 



/* Create a one-way communication channel (pipe).
   If successful, two file descriptors are stored in PIPEDES;
   bytes written on PIPEDES[1] can be read from PIPEDES[0].
   Returns 0 if successful, -1 if not.  */
extern int pipe (int __pipedes[2])  ;







/* Schedule an alarm.  In SECONDS seconds, the process will get a SIGALRM.
   If SECONDS is zero, any currently scheduled alarm will be cancelled.
   The function returns the number of seconds remaining until the last
   alarm scheduled would have signaled, or zero if there wasn't one.
   There is no return value to indicate an error, but you can set `errno'
   to 0 and check its value after calling `alarm', and this might tell you.
   The signal may come late due to processor scheduling.  */
extern unsigned int alarm (unsigned int __seconds) ;

/* Make the process sleep for SECONDS seconds, or until a signal arrives
   and is not ignored.  The function returns the number of seconds less
   than SECONDS which it actually slept (thus zero if it slept the full time).
   If a signal handler does a `longjmp' or modifies the handling of the
   SIGALRM signal while inside `sleep' call, the handling of the SIGALRM
   signal afterwards is undefined.  There is no return value to indicate
   error, but if `sleep' returns SECONDS, it probably didn't work.

   This function is a cancellation point and therefore not marked with
   __THROW.  */
extern unsigned int sleep (unsigned int __seconds);



/* Set an alarm to go off (generating a SIGALRM signal) in VALUE
   microseconds.  If INTERVAL is nonzero, when the alarm goes off, the
   timer is reset to go off every INTERVAL microseconds thereafter.
   Returns the number of microseconds remaining before the alarm.  */
extern __useconds_t ualarm (__useconds_t __value, __useconds_t __interval)
     ;

/* Sleep USECONDS microseconds, or until a signal arrives that is not blocked
   or ignored.

   This function is a cancellation point and therefore not marked with
   __THROW.  */
extern int usleep (__useconds_t __useconds);



/* Suspend the process until a signal arrives.
   This always returns -1 and sets `errno' to EINTR.

   This function is a cancellation point and therefore not marked with
   __THROW.  */
extern int pause (void);


/* Change the owner and group of FILE.  */
extern int chown (const char *__file, __uid_t __owner, __gid_t __group)
       ;


/* Change the owner and group of the file that FD is open on.  */
extern int fchown (int __fd, __uid_t __owner, __gid_t __group)  ;


/* Change owner and group of FILE, if it is a symbolic
   link the ownership of the symbolic link is changed.  */
extern int lchown (const char *__file, __uid_t __owner, __gid_t __group)
       ;




/* Change the owner and group of FILE relative to the directory FD is open
   on.  */
extern int fchownat (int __fd, const char *__file, __uid_t __owner,
		     __gid_t __group, int __flag)
       ;


/* Change the process's working directory to PATH.  */
extern int chdir (const char *__path)   ;


/* Change the process's working directory to the one FD is open on.  */
extern int fchdir (int __fd)  ;


/* Get the pathname of the current working directory,
   and put it in SIZE bytes of BUF.  Returns NULL if the
   directory couldn't be determined or SIZE was too small.
   If successful, returns BUF.  In GNU, if BUF is NULL,
   an array is allocated with `malloc'; the array is SIZE
   bytes long, unless SIZE == 0, in which case it is as
   big as necessary.  */
extern char *getcwd (char *__buf, size_t __size)  ;










/* Put the absolute pathname of the current working directory in BUF.
   If successful, return BUF.  If not, put an error message in
   BUF and return NULL.  BUF should be at least PATH_MAX bytes long.  */
extern char *getwd (char *__buf)
        ;



/* Duplicate FD, returning a new file descriptor on the same file.  */
extern int dup (int __fd)  ;

/* Duplicate FD to FD2, closing FD2 and making it open on the same file.  */
extern int dup2 (int __fd, int __fd2) ;







/* NULL-terminated array of "NAME=VALUE" environment variables.  */
extern char **__environ;





/* Replace the current process, executing PATH with arguments ARGV and
   environment ENVP.  ARGV and ENVP are terminated by NULL pointers.  */
extern int execve (const char *__path, char *const __argv[],
		   char *const __envp[])  ;


/* Execute the file FD refers to, overlaying the running program image.
   ARGV and ENVP are passed to the new program, as for `execve'.  */
extern int fexecve (int __fd, char *const __argv[], char *const __envp[])
      ;



/* Execute PATH with arguments ARGV and environment from `environ'.  */
extern int execv (const char *__path, char *const __argv[])
      ;

/* Execute PATH with all arguments after PATH until a NULL pointer,
   and the argument after that for environment.  */
extern int execle (const char *__path, const char *__arg, ...)
      ;

/* Execute PATH with all arguments after PATH until
   a NULL pointer and environment from `environ'.  */
extern int execl (const char *__path, const char *__arg, ...)
      ;

/* Execute FILE, searching in the `PATH' environment variable if it contains
   no slashes, with arguments ARGV and environment from `environ'.  */
extern int execvp (const char *__file, char *const __argv[])
      ;

/* Execute FILE, searching in the `PATH' environment variable if
   it contains no slashes, with all arguments after FILE until a
   NULL pointer and environment from `environ'.  */
extern int execlp (const char *__file, const char *__arg, ...)
      ;





		    
     




/* Add INC to priority of the current process.  */
extern int nice (int __inc)  ;



/* Terminate program execution with the low-order 8 bits of STATUS.  */
extern void _exit (int __status) ;


/* Get the `_PC_*' symbols for the NAME argument to `pathconf' and `fpathconf';
   the `_SC_*' symbols for the NAME argument to `sysconf';
   and the `_CS_*' symbols for the NAME argument to `confstr'.  */

#line 1 "/usr/include/bits/confname.h" 1
/* `sysconf', `pathconf', and `confstr' NAME values.  Generic version.
   Copyright (C) 1993-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */





/* Values for the NAME argument to `pathconf' and `fpathconf'.  */
enum
  {
    _PC_LINK_MAX,

    _PC_MAX_CANON,

    _PC_MAX_INPUT,

    _PC_NAME_MAX,

    _PC_PATH_MAX,

    _PC_PIPE_BUF,

    _PC_CHOWN_RESTRICTED,

    _PC_NO_TRUNC,

    _PC_VDISABLE,

    _PC_SYNC_IO,

    _PC_ASYNC_IO,

    _PC_PRIO_IO,

    _PC_SOCK_MAXBUF,

    _PC_FILESIZEBITS,

    _PC_REC_INCR_XFER_SIZE,

    _PC_REC_MAX_XFER_SIZE,

    _PC_REC_MIN_XFER_SIZE,

    _PC_REC_XFER_ALIGN,

    _PC_ALLOC_SIZE_MIN,

    _PC_SYMLINK_MAX,

    _PC_2_SYMLINKS

  };

/* Values for the argument to `sysconf'.  */
enum
  {
    _SC_ARG_MAX,

    _SC_CHILD_MAX,

    _SC_CLK_TCK,

    _SC_NGROUPS_MAX,

    _SC_OPEN_MAX,

    _SC_STREAM_MAX,

    _SC_TZNAME_MAX,

    _SC_JOB_CONTROL,

    _SC_SAVED_IDS,

    _SC_REALTIME_SIGNALS,

    _SC_PRIORITY_SCHEDULING,

    _SC_TIMERS,

    _SC_ASYNCHRONOUS_IO,

    _SC_PRIORITIZED_IO,

    _SC_SYNCHRONIZED_IO,

    _SC_FSYNC,

    _SC_MAPPED_FILES,

    _SC_MEMLOCK,

    _SC_MEMLOCK_RANGE,

    _SC_MEMORY_PROTECTION,

    _SC_MESSAGE_PASSING,

    _SC_SEMAPHORES,

    _SC_SHARED_MEMORY_OBJECTS,

    _SC_AIO_LISTIO_MAX,

    _SC_AIO_MAX,

    _SC_AIO_PRIO_DELTA_MAX,

    _SC_DELAYTIMER_MAX,

    _SC_MQ_OPEN_MAX,

    _SC_MQ_PRIO_MAX,

    _SC_VERSION,

    _SC_PAGESIZE,


    _SC_RTSIG_MAX,

    _SC_SEM_NSEMS_MAX,

    _SC_SEM_VALUE_MAX,

    _SC_SIGQUEUE_MAX,

    _SC_TIMER_MAX,


    /* Values for the argument to `sysconf'
       corresponding to _POSIX2_* symbols.  */
    _SC_BC_BASE_MAX,

    _SC_BC_DIM_MAX,

    _SC_BC_SCALE_MAX,

    _SC_BC_STRING_MAX,

    _SC_COLL_WEIGHTS_MAX,

    _SC_EQUIV_CLASS_MAX,

    _SC_EXPR_NEST_MAX,

    _SC_LINE_MAX,

    _SC_RE_DUP_MAX,

    _SC_CHARCLASS_NAME_MAX,


    _SC_2_VERSION,

    _SC_2_C_BIND,

    _SC_2_C_DEV,

    _SC_2_FORT_DEV,

    _SC_2_FORT_RUN,

    _SC_2_SW_DEV,

    _SC_2_LOCALEDEF,


    _SC_PII,

    _SC_PII_XTI,

    _SC_PII_SOCKET,

    _SC_PII_INTERNET,

    _SC_PII_OSI,

    _SC_POLL,

    _SC_SELECT,

    _SC_UIO_MAXIOV,

    _SC_IOV_MAX = _SC_UIO_MAXIOV,

    _SC_PII_INTERNET_STREAM,

    _SC_PII_INTERNET_DGRAM,

    _SC_PII_OSI_COTS,

    _SC_PII_OSI_CLTS,

    _SC_PII_OSI_M,

    _SC_T_IOV_MAX,


    /* Values according to POSIX 1003.1c (POSIX threads).  */
    _SC_THREADS,

    _SC_THREAD_SAFE_FUNCTIONS,

    _SC_GETGR_R_SIZE_MAX,

    _SC_GETPW_R_SIZE_MAX,

    _SC_LOGIN_NAME_MAX,

    _SC_TTY_NAME_MAX,

    _SC_THREAD_DESTRUCTOR_ITERATIONS,

    _SC_THREAD_KEYS_MAX,

    _SC_THREAD_STACK_MIN,

    _SC_THREAD_THREADS_MAX,

    _SC_THREAD_ATTR_STACKADDR,

    _SC_THREAD_ATTR_STACKSIZE,

    _SC_THREAD_PRIORITY_SCHEDULING,

    _SC_THREAD_PRIO_INHERIT,

    _SC_THREAD_PRIO_PROTECT,

    _SC_THREAD_PROCESS_SHARED,


    _SC_NPROCESSORS_CONF,

    _SC_NPROCESSORS_ONLN,

    _SC_PHYS_PAGES,

    _SC_AVPHYS_PAGES,

    _SC_ATEXIT_MAX,

    _SC_PASS_MAX,


    _SC_XOPEN_VERSION,

    _SC_XOPEN_XCU_VERSION,

    _SC_XOPEN_UNIX,

    _SC_XOPEN_CRYPT,

    _SC_XOPEN_ENH_I18N,

    _SC_XOPEN_SHM,


    _SC_2_CHAR_TERM,

    _SC_2_C_VERSION,

    _SC_2_UPE,


    _SC_XOPEN_XPG2,

    _SC_XOPEN_XPG3,

    _SC_XOPEN_XPG4,


    _SC_CHAR_BIT,

    _SC_CHAR_MAX,

    _SC_CHAR_MIN,

    _SC_INT_MAX,

    _SC_INT_MIN,

    _SC_LONG_BIT,

    _SC_WORD_BIT,

    _SC_MB_LEN_MAX,

    _SC_NZERO,

    _SC_SSIZE_MAX,

    _SC_SCHAR_MAX,

    _SC_SCHAR_MIN,

    _SC_SHRT_MAX,

    _SC_SHRT_MIN,

    _SC_UCHAR_MAX,

    _SC_UINT_MAX,

    _SC_ULONG_MAX,

    _SC_USHRT_MAX,


    _SC_NL_ARGMAX,

    _SC_NL_LANGMAX,

    _SC_NL_MSGMAX,

    _SC_NL_NMAX,

    _SC_NL_SETMAX,

    _SC_NL_TEXTMAX,


    _SC_XBS5_ILP32_OFF32,

    _SC_XBS5_ILP32_OFFBIG,

    _SC_XBS5_LP64_OFF64,

    _SC_XBS5_LPBIG_OFFBIG,


    _SC_XOPEN_LEGACY,

    _SC_XOPEN_REALTIME,

    _SC_XOPEN_REALTIME_THREADS,


    _SC_ADVISORY_INFO,

    _SC_BARRIERS,

    _SC_BASE,

    _SC_C_LANG_SUPPORT,

    _SC_C_LANG_SUPPORT_R,

    _SC_CLOCK_SELECTION,

    _SC_CPUTIME,

    _SC_THREAD_CPUTIME,

    _SC_DEVICE_IO,

    _SC_DEVICE_SPECIFIC,

    _SC_DEVICE_SPECIFIC_R,

    _SC_FD_MGMT,

    _SC_FIFO,

    _SC_PIPE,

    _SC_FILE_ATTRIBUTES,

    _SC_FILE_LOCKING,

    _SC_FILE_SYSTEM,

    _SC_MONOTONIC_CLOCK,

    _SC_MULTI_PROCESS,

    _SC_SINGLE_PROCESS,

    _SC_NETWORKING,

    _SC_READER_WRITER_LOCKS,

    _SC_SPIN_LOCKS,

    _SC_REGEXP,

    _SC_REGEX_VERSION,

    _SC_SHELL,

    _SC_SIGNALS,

    _SC_SPAWN,

    _SC_SPORADIC_SERVER,

    _SC_THREAD_SPORADIC_SERVER,

    _SC_SYSTEM_DATABASE,

    _SC_SYSTEM_DATABASE_R,

    _SC_TIMEOUTS,

    _SC_TYPED_MEMORY_OBJECTS,

    _SC_USER_GROUPS,

    _SC_USER_GROUPS_R,

    _SC_2_PBS,

    _SC_2_PBS_ACCOUNTING,

    _SC_2_PBS_LOCATE,

    _SC_2_PBS_MESSAGE,

    _SC_2_PBS_TRACK,

    _SC_SYMLOOP_MAX,

    _SC_STREAMS,

    _SC_2_PBS_CHECKPOINT,


    _SC_V6_ILP32_OFF32,

    _SC_V6_ILP32_OFFBIG,

    _SC_V6_LP64_OFF64,

    _SC_V6_LPBIG_OFFBIG,


    _SC_HOST_NAME_MAX,

    _SC_TRACE,

    _SC_TRACE_EVENT_FILTER,

    _SC_TRACE_INHERIT,

    _SC_TRACE_LOG,


    _SC_LEVEL1_ICACHE_SIZE,

    _SC_LEVEL1_ICACHE_ASSOC,

    _SC_LEVEL1_ICACHE_LINESIZE,

    _SC_LEVEL1_DCACHE_SIZE,

    _SC_LEVEL1_DCACHE_ASSOC,

    _SC_LEVEL1_DCACHE_LINESIZE,

    _SC_LEVEL2_CACHE_SIZE,

    _SC_LEVEL2_CACHE_ASSOC,

    _SC_LEVEL2_CACHE_LINESIZE,

    _SC_LEVEL3_CACHE_SIZE,

    _SC_LEVEL3_CACHE_ASSOC,

    _SC_LEVEL3_CACHE_LINESIZE,

    _SC_LEVEL4_CACHE_SIZE,

    _SC_LEVEL4_CACHE_ASSOC,

    _SC_LEVEL4_CACHE_LINESIZE,

    /* Leave room here, maybe we need a few more cache levels some day.  */

    _SC_IPV6 = _SC_LEVEL1_ICACHE_SIZE + 50,

    _SC_RAW_SOCKETS,


    _SC_V7_ILP32_OFF32,

    _SC_V7_ILP32_OFFBIG,

    _SC_V7_LP64_OFF64,

    _SC_V7_LPBIG_OFFBIG,


    _SC_SS_REPL_MAX,


    _SC_TRACE_EVENT_NAME_MAX,

    _SC_TRACE_NAME_MAX,

    _SC_TRACE_SYS_MAX,

    _SC_TRACE_USER_EVENT_MAX,


    _SC_XOPEN_STREAMS,


    _SC_THREAD_ROBUST_PRIO_INHERIT,

    _SC_THREAD_ROBUST_PRIO_PROTECT

  };

/* Values for the NAME argument to `confstr'.  */
enum
  {
    _CS_PATH,			/* The default search path.  */


    _CS_V6_WIDTH_RESTRICTED_ENVS,



    _CS_GNU_LIBC_VERSION,

    _CS_GNU_LIBPTHREAD_VERSION,


    _CS_V5_WIDTH_RESTRICTED_ENVS,



    _CS_V7_WIDTH_RESTRICTED_ENVS,



    _CS_LFS_CFLAGS = 1000,

    _CS_LFS_LDFLAGS,

    _CS_LFS_LIBS,

    _CS_LFS_LINTFLAGS,

    _CS_LFS64_CFLAGS,

    _CS_LFS64_LDFLAGS,

    _CS_LFS64_LIBS,

    _CS_LFS64_LINTFLAGS,


    _CS_XBS5_ILP32_OFF32_CFLAGS = 1100,

    _CS_XBS5_ILP32_OFF32_LDFLAGS,

    _CS_XBS5_ILP32_OFF32_LIBS,

    _CS_XBS5_ILP32_OFF32_LINTFLAGS,

    _CS_XBS5_ILP32_OFFBIG_CFLAGS,

    _CS_XBS5_ILP32_OFFBIG_LDFLAGS,

    _CS_XBS5_ILP32_OFFBIG_LIBS,

    _CS_XBS5_ILP32_OFFBIG_LINTFLAGS,

    _CS_XBS5_LP64_OFF64_CFLAGS,

    _CS_XBS5_LP64_OFF64_LDFLAGS,

    _CS_XBS5_LP64_OFF64_LIBS,

    _CS_XBS5_LP64_OFF64_LINTFLAGS,

    _CS_XBS5_LPBIG_OFFBIG_CFLAGS,

    _CS_XBS5_LPBIG_OFFBIG_LDFLAGS,

    _CS_XBS5_LPBIG_OFFBIG_LIBS,

    _CS_XBS5_LPBIG_OFFBIG_LINTFLAGS,


    _CS_POSIX_V6_ILP32_OFF32_CFLAGS,

    _CS_POSIX_V6_ILP32_OFF32_LDFLAGS,

    _CS_POSIX_V6_ILP32_OFF32_LIBS,

    _CS_POSIX_V6_ILP32_OFF32_LINTFLAGS,

    _CS_POSIX_V6_ILP32_OFFBIG_CFLAGS,

    _CS_POSIX_V6_ILP32_OFFBIG_LDFLAGS,

    _CS_POSIX_V6_ILP32_OFFBIG_LIBS,

    _CS_POSIX_V6_ILP32_OFFBIG_LINTFLAGS,

    _CS_POSIX_V6_LP64_OFF64_CFLAGS,

    _CS_POSIX_V6_LP64_OFF64_LDFLAGS,

    _CS_POSIX_V6_LP64_OFF64_LIBS,

    _CS_POSIX_V6_LP64_OFF64_LINTFLAGS,

    _CS_POSIX_V6_LPBIG_OFFBIG_CFLAGS,

    _CS_POSIX_V6_LPBIG_OFFBIG_LDFLAGS,

    _CS_POSIX_V6_LPBIG_OFFBIG_LIBS,

    _CS_POSIX_V6_LPBIG_OFFBIG_LINTFLAGS,


    _CS_POSIX_V7_ILP32_OFF32_CFLAGS,

    _CS_POSIX_V7_ILP32_OFF32_LDFLAGS,

    _CS_POSIX_V7_ILP32_OFF32_LIBS,

    _CS_POSIX_V7_ILP32_OFF32_LINTFLAGS,

    _CS_POSIX_V7_ILP32_OFFBIG_CFLAGS,

    _CS_POSIX_V7_ILP32_OFFBIG_LDFLAGS,

    _CS_POSIX_V7_ILP32_OFFBIG_LIBS,

    _CS_POSIX_V7_ILP32_OFFBIG_LINTFLAGS,

    _CS_POSIX_V7_LP64_OFF64_CFLAGS,

    _CS_POSIX_V7_LP64_OFF64_LDFLAGS,

    _CS_POSIX_V7_LP64_OFF64_LIBS,

    _CS_POSIX_V7_LP64_OFF64_LINTFLAGS,

    _CS_POSIX_V7_LPBIG_OFFBIG_CFLAGS,

    _CS_POSIX_V7_LPBIG_OFFBIG_LDFLAGS,

    _CS_POSIX_V7_LPBIG_OFFBIG_LIBS,

    _CS_POSIX_V7_LPBIG_OFFBIG_LINTFLAGS,


    _CS_V6_ENV,

    _CS_V7_ENV

  };

#line 475 "/usr/include/unistd.h" 2

/* Get file-specific configuration information about PATH.  */
extern long int pathconf (const char *__path, int __name)
      ;

/* Get file-specific configuration about descriptor FD.  */
extern long int fpathconf (int __fd, int __name) ;

/* Get the value of the system variable NAME.  */
extern long int sysconf (int __name) ;


/* Get the value of the string-valued system variable NAME.  */
extern size_t confstr (int __name, char *__buf, size_t __len) ;



/* Get the process ID of the calling process.  */
extern __pid_t getpid (void) ;

/* Get the process ID of the calling process's parent.  */
extern __pid_t getppid (void) ;

/* Get the process group ID of the calling process.  */
extern __pid_t getpgrp (void) ;

/* Get the process group ID of process PID.  */
extern __pid_t __getpgid (__pid_t __pid) ;

extern __pid_t getpgid (__pid_t __pid) ;



/* Set the process group ID of the process matching PID to PGID.
   If PID is zero, the current process's process group ID is set.
   If PGID is zero, the process ID of the process is used.  */
extern int setpgid (__pid_t __pid, __pid_t __pgid) ;


/* Both System V and BSD have `setpgrp' functions, but with different
   calling conventions.  The BSD function is the same as POSIX.1 `setpgid'
   (above).  The System V function takes no arguments and puts the calling
   process in its on group like `setpgid (0, 0)'.

   New programs should always use `setpgid' instead.

   GNU provides the POSIX.1 function.  */

/* Set the process group ID of the calling process to its own PID.
   This is exactly the same as `setpgid (0, 0)'.  */
extern int setpgrp (void) ;



/* Create a new session with the calling process as its leader.
   The process group IDs of the session and the calling process
   are set to the process ID of the calling process, which is returned.  */
extern __pid_t setsid (void) ;


/* Return the session ID of the given process.  */
extern __pid_t getsid (__pid_t __pid) ;


/* Get the real user ID of the calling process.  */
extern __uid_t getuid (void) ;

/* Get the effective user ID of the calling process.  */
extern __uid_t geteuid (void) ;

/* Get the real group ID of the calling process.  */
extern __gid_t getgid (void) ;

/* Get the effective group ID of the calling process.  */
extern __gid_t getegid (void) ;

/* If SIZE is zero, return the number of supplementary groups
   the calling process is in.  Otherwise, fill in the group IDs
   of its supplementary groups in LIST and return the number written.  */
extern int getgroups (int __size, __gid_t __list[])  ;






/* Set the user ID of the calling process to UID.
   If the calling process is the super-user, set the real
   and effective user IDs, and the saved set-user-ID to UID;
   if not, the effective user ID is set to UID.  */
extern int setuid (__uid_t __uid)  ;


/* Set the real user ID of the calling process to RUID,
   and the effective user ID of the calling process to EUID.  */
extern int setreuid (__uid_t __ruid, __uid_t __euid)  ;



/* Set the effective user ID of the calling process to UID.  */
extern int seteuid (__uid_t __uid)  ;


/* Set the group ID of the calling process to GID.
   If the calling process is the super-user, set the real
   and effective group IDs, and the saved set-group-ID to GID;
   if not, the effective group ID is set to GID.  */
extern int setgid (__gid_t __gid)  ;


/* Set the real group ID of the calling process to RGID,
   and the effective group ID of the calling process to EGID.  */
extern int setregid (__gid_t __rgid, __gid_t __egid)  ;



/* Set the effective group ID of the calling process to GID.  */
extern int setegid (__gid_t __gid)  ;






     




     




     




     



/* Clone the calling process, creating an exact copy.
   Return -1 for errors, 0 to the new process,
   and the process ID of the new process to the old process.  */
extern __pid_t fork (void) ;



/* Clone the calling process, but without copying the whole address space.
   The calling process is suspended until the new process exits or is
   replaced by a call to `execve'.  Return -1 for errors, 0 to the new process,
   and the process ID of the new process to the old process.  */
extern __pid_t vfork (void) ;



/* Return the pathname of the terminal FD is open on, or NULL on errors.
   The returned storage is good only until the next call to this function.  */
extern char *ttyname (int __fd) ;

/* Store at most BUFLEN characters of the pathname of the terminal FD is
   open on in BUF.  Return 0 on success, otherwise an error number.  */
extern int ttyname_r (int __fd, char *__buf, size_t __buflen)
       ;

/* Return 1 if FD is a valid descriptor associated
   with a terminal, zero if not.  */
extern int isatty (int __fd) ;


/* Return the index into the active-logins file (utmp) for
   the controlling terminal.  */
extern int ttyslot (void) ;



/* Make a link to FROM named TO.  */
extern int link (const char *__from, const char *__to)
       ;


/* Like link but relative paths in TO and FROM are interpreted relative
   to FROMFD and TOFD respectively.  */
extern int linkat (int __fromfd, const char *__from, int __tofd,
		   const char *__to, int __flags)
       ;



/* Make a symbolic link to FROM named TO.  */
extern int symlink (const char *__from, const char *__to)
       ;

/* Read the contents of the symbolic link PATH into no more than
   LEN bytes of BUF.  The contents are not null-terminated.
   Returns the number of characters read, or -1 for errors.  */
extern ssize_t readlink (const char * __path,
			 char * __buf, size_t __len)
       ;



/* Like symlink but a relative path in TO is interpreted relative to TOFD.  */
extern int symlinkat (const char *__from, int __tofd,
		      const char *__to)   ;

/* Like readlink but a relative PATH is interpreted relative to FD.  */
extern ssize_t readlinkat (int __fd, const char * __path,
			   char * __buf, size_t __len)
       ;


/* Remove the link NAME.  */
extern int unlink (const char *__name)  ;


/* Remove the link NAME relative to FD.  */
extern int unlinkat (int __fd, const char *__name, int __flag)
      ;


/* Remove the directory PATH.  */
extern int rmdir (const char *__path)  ;


/* Return the foreground process group ID of FD.  */
extern __pid_t tcgetpgrp (int __fd) ;

/* Set the foreground process group ID of FD set PGRP_ID.  */
extern int tcsetpgrp (int __fd, __pid_t __pgrp_id) ;


/* Return the login name of the user.

   This function is a possible cancellation point and therefore not
   marked with __THROW.  */
extern char *getlogin (void);

/* Return at most NAME_LEN characters of the login name of the user in NAME.
   If it cannot be determined or some other error occurred, return the error
   code.  Otherwise return 0.

   This function is a possible cancellation point and therefore not
   marked with __THROW.  */
extern int getlogin_r (char *__name, size_t __name_len) ;



/* Set the login name returned by `getlogin'.  */
extern int setlogin (const char *__name)  ;




/* Get definitions and prototypes for functions to process the
   arguments in ARGV (ARGC of them, minus the program name) for
   options given in OPTS.  */

#line 1 "/usr/include/bits/getopt_posix.h" 1
/* Declarations for getopt (POSIX compatibility shim).
   Copyright (C) 1989-2018 Free Software Foundation, Inc.
   Unlike the bulk of the getopt implementation, this file is NOT part
   of gnulib.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */









#line 1 "/usr/include/bits/getopt_core.h" 1
/* Declarations for getopt (basic, portable features only).
   Copyright (C) 1989-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library and is also part of gnulib.
   Patches to this file should be submitted to both projects.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */




/* This header should not be used directly; include getopt.h or
   unistd.h instead.  Unlike most bits headers, it does not have
   a protective #error, because the guard macro for getopt.h in
   gnulib is not fixed.  */

extern "C" {

/* For communication from 'getopt' to the caller.
   When 'getopt' finds an option that takes an argument,
   the argument value is returned here.
   Also, when 'ordering' is RETURN_IN_ORDER,
   each non-option ARGV-element is returned here.  */

extern char *optarg;

/* Index in ARGV of the next element to be scanned.
   This is used for communication to and from the caller
   and for communication between successive calls to 'getopt'.

   On entry to 'getopt', zero means this is the first call; initialize.

   When 'getopt' returns -1, this is the index of the first of the
   non-option elements that the caller should itself scan.

   Otherwise, 'optind' communicates from one call to the next
   how much of ARGV has been scanned so far.  */

extern int optind;

/* Callers store zero here to inhibit the error message 'getopt' prints
   for unrecognized options.  */

extern int opterr;

/* Set to an option character which was unrecognized.  */

extern int optopt;

/* Get definitions and prototypes for functions to process the
   arguments in ARGV (ARGC of them, minus the program name) for
   options given in OPTS.

   Return the option character from OPTS just read.  Return -1 when
   there are no more options.  For unrecognized options, or options
   missing arguments, 'optopt' is set to the option letter, and '?' is
   returned.

   The OPTS string is a list of characters which are recognized option
   letters, optionally followed by colons, specifying that that letter
   takes an argument, to be placed in 'optarg'.

   If a letter in OPTS is followed by two colons, its argument is
   optional.  This behavior is specific to the GNU 'getopt'.

   The argument '--' causes premature termination of argument
   scanning, explicitly telling 'getopt' that there are no more
   options.

   If OPTS begins with '-', then non-option arguments are treated as
   arguments to the option '\1'.  This behavior is specific to the GNU
   'getopt'.  If OPTS begins with '+', or POSIXLY_CORRECT is set in
   the environment, then do not permute arguments.

   For standards compliance, the 'argv' argument has the type
   char *const *, but this is inaccurate; if argument permutation is
   enabled, the argv array (not the strings it points to) must be
   writable.  */

extern int getopt (int ___argc, char *const *___argv, const char *__shortopts)
        ;

}



#line 25 "/usr/include/bits/getopt_posix.h" 2

extern "C" {









				    
			   


			   
  




}



#line 703 "/usr/include/unistd.h" 2




/* Put the name of the current host in no more than LEN bytes of NAME.
   The result is null-terminated if LEN is large enough for the full
   name and the terminator.  */
extern int gethostname (char *__name, size_t __len)  ;




/* Set the name of the current host to NAME, which is LEN bytes long.
   This call is restricted to the super-user.  */
extern int sethostname (const char *__name, size_t __len)
       ;

/* Set the current machine's Internet number to ID.
   This call is restricted to the super-user.  */
extern int sethostid (long int __id)  ;


/* Get and set the NIS (aka YP) domain name, if any.
   Called just like `gethostname' and `sethostname'.
   The NIS domain name is usually the empty string when not using NIS.  */
extern int getdomainname (char *__name, size_t __len)
       ;
extern int setdomainname (const char *__name, size_t __len)
       ;


/* Revoke access permissions to all processes currently communicating
   with the control terminal, and then send a SIGHUP signal to the process
   group of the control terminal.  */
extern int vhangup (void) ;

/* Revoke the access of all descriptors currently open on FILE.  */
extern int revoke (const char *__file)   ;


/* Enable statistical profiling, writing samples of the PC into at most
   SIZE bytes of SAMPLE_BUFFER; every processor clock tick while profiling
   is enabled, the system examines the user PC and increments
   SAMPLE_BUFFER[((PC - OFFSET) / 2) * SCALE / 65536].  If SCALE is zero,
   disable profiling.  Returns zero on success, -1 on error.  */
extern int profil (unsigned short int *__sample_buffer, size_t __size,
		   size_t __offset, unsigned int __scale)
      ;


/* Turn accounting on if NAME is an existing file.  The system will then write
   a record for each process as it terminates, to this file.  If NAME is NULL,
   turn accounting off.  This call is restricted to the super-user.  */
extern int acct (const char *__name) ;


/* Successive calls return the shells listed in `/etc/shells'.  */
extern char *getusershell (void) ;
extern void endusershell (void) ; /* Discard cached info.  */
extern void setusershell (void) ; /* Rewind and re-read the file.  */


/* Put the program in the background, and dissociate from the controlling
   terminal.  If NOCHDIR is zero, do `chdir ("/")'.  If NOCLOSE is zero,
   redirects stdin, stdout, and stderr to /dev/null.  */
extern int daemon (int __nochdir, int __noclose)  ;




/* Make PATH be the root directory (the starting point for absolute paths).
   This call is restricted to the super-user.  */
extern int chroot (const char *__path)   ;

/* Prompt with PROMPT and read a string from the terminal without echoing.
   Uses /dev/tty if possible; otherwise stderr and stdin.  */
extern char *getpass (const char *__prompt) ;



/* Make all changes done to FD actually appear on disk.

   This function is a cancellation point and therefore not marked with
   __THROW.  */
extern int fsync (int __fd);











/* Return identifier for the current host.  */
extern long int gethostid (void);

/* Make all changes done to all files actually appear on disk.  */
extern void sync (void) ;



/* Return the number of bytes in a page.  This is the system's page size,
   which is not necessarily the same as the hardware page size.  */
extern int getpagesize (void)   ;


/* Return the maximum number of file descriptors
   the current process could possibly have.  */
extern int getdtablesize (void) ;







/* Truncate FILE to LENGTH bytes.  */

extern int truncate (const char *__file, __off_t __length)
       ;



			   
			   






     







/* Truncate the file FD is open on to LENGTH bytes.  */

extern int ftruncate (int __fd, __off_t __length)  ;



			   














/* Set the end of accessible data space (aka "the break") to ADDR.
   Returns zero on success and -1 for errors (with errno set).  */
extern int brk (void *__addr)  ;

/* Increase or decrease the end of accessible data space by DELTA bytes.
   If successful, returns the address the previous end of data space
   (i.e. the beginning of the new space, if DELTA > 0);
   returns (void *) -1 for errors (with errno set).  */
extern void *sbrk (intptr_t __delta) ;




/* Invoke `system call' number SYSNO, passing it the remaining arguments.
   This is completely system-dependent, and not often useful.

   In Unix, `syscall' sets `errno' for all errors and most calls return -1
   for errors; in many systems you cannot pass arguments or get return
   values for all system calls (`pipe', `fork', and `getppid' typically
   among them).

   In Mach, all system calls take normal arguments and always return an
   error code (zero for success).  */
extern long int syscall (long int __sysno, ...) ;





/* NOTE: These declarations also appear in <fcntl.h>; be sure to keep both
   files consistent.  Some systems have them there and some here, and some
   software depends on the macros being defined without including both.  */

/* `lockf' is a simpler interface to the locking facilities of `fcntl'.
   LEN is always relative to the current file position.
   The CMD argument is one of the following.

   This function is a cancellation point and therefore not marked with
   __THROW.  */







extern int lockf (int __fd, int __cmd, __off_t __len) ;



		       
























			 
			 



/* Synchronize at least the data part of a file with the underlying
   media.  */
extern int fdatasync (int __fildes);



/* XPG4.2 specifies that prototypes for the encryption functions must
   be defined here.  */



     




     







		  



/* Prior to Issue 6, the Single Unix Specification required these
   prototypes to appear in this header.  They are also found in
   <stdio.h>.  */









/* Unix98 requires this function to be declared here.  In other
   standards it is in <pthread.h>.  */


			   
			   



/* Write LENGTH bytes of randomness starting at BUFFER.  Return 0 on
   success or -1 on error.  */
int getentropy (void *__buffer, size_t __length) ;


/* Define some macros helping to catch buffer overflows.  */




}



#line 39 "/usr/include/libssh/libssh.h" 2
  
#line 1 "/usr/include/inttypes.h" 1
/* Copyright (C) 1997-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */

/*
 *	ISO C99: 7.8 Format conversion of integer types	<inttypes.h>
 */





#line 1 "/usr/include/inttypes.h" 1
/* Get the type definitions.  */

#line 1 "/usr/include/stdint.h" 1
/* Copyright (C) 1997-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */

/*
 *	ISO C99: 7.18 Integer types <stdint.h>
 */






#line 1 "/usr/include/bits/libc-header-start.h" 1
/* Handle feature test macros at the start of a header.
   Copyright (C) 2016-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */

/* This header is internal to glibc and should not be included outside
   of glibc headers.  Headers including it must define
   __GLIBC_INTERNAL_STARTING_HEADER_IMPLEMENTATION first.  This header
   cannot have multiple include guards because ISO C feature test
   macros depend on the definition of the macro when an affected
   header is included, not when the first system header is
   included.  */








#line 1 "/usr/include/bits/libc-header-start.h" 1

/* ISO/IEC TR 24731-2:2010 defines the __STDC_WANT_LIB_EXT2__
   macro.  */








/* ISO/IEC TS 18661-1:2014 defines the __STDC_WANT_IEC_60559_BFP_EXT__
   macro.  */







/* ISO/IEC TS 18661-4:2015 defines the
   __STDC_WANT_IEC_60559_FUNCS_EXT__ macro.  */







/* ISO/IEC TS 18661-3:2015 defines the
   __STDC_WANT_IEC_60559_TYPES_EXT__ macro.  */







#line 25 "/usr/include/stdint.h" 2

#line 1 "/usr/include/stdint.h" 1

#line 1 "/usr/include/bits/wchar.h" 1
/* wchar_t type related definitions.
   Copyright (C) 2000-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */




/* The fallback definitions, for when __WCHAR_MAX__ or __WCHAR_MIN__
   are not defined, give the right value and type as long as both int
   and wchar_t are 32-bit types.  Adding L'\0' to a constant value
   ensures that the type is correct; it is necessary to use (L'\0' +
   0) rather than just L'\0' so that the type in C++ is the promoted
   version of wchar_t rather than the distinct wchar_t type itself.
   Because wchar_t in preprocessor #if expressions is treated as
   intmax_t or uintmax_t, the expression (L'\0' - 1) would have the
   wrong value for WCHAR_MAX in such expressions and so cannot be used
   to define __WCHAR_MAX in the unsigned case.  */



















#line 27 "/usr/include/stdint.h" 2

#line 1 "/usr/include/stdint.h" 1

/* Exact integral types.  */

/* Signed.  */

#line 1 "/usr/include/bits/stdint-intn.h" 1
/* Define intN_t types.
   Copyright (C) 2017-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */





#line 1 "/usr/include/bits/stdint-intn.h" 1

typedef __int8_t int8_t;
typedef __int16_t int16_t;
typedef __int32_t int32_t;
typedef __int64_t int64_t;



#line 33 "/usr/include/stdint.h" 2

/* Unsigned.  */

#line 1 "/usr/include/bits/stdint-uintn.h" 1
/* Define uintN_t types.
   Copyright (C) 2017-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */





#line 1 "/usr/include/bits/stdint-uintn.h" 1

typedef __uint8_t uint8_t;
typedef __uint16_t uint16_t;
typedef __uint32_t uint32_t;
typedef __uint64_t uint64_t;



#line 36 "/usr/include/stdint.h" 2


/* Small types.  */

/* Signed.  */
typedef signed char		int_least8_t;
typedef short int		int_least16_t;
typedef int			int_least32_t;

typedef long int		int_least64_t;





/* Unsigned.  */
typedef unsigned char		uint_least8_t;
typedef unsigned short int	uint_least16_t;
typedef unsigned int		uint_least32_t;

typedef unsigned long int	uint_least64_t;






/* Fast types.  */

/* Signed.  */
typedef signed char		int_fast8_t;

typedef long int		int_fast16_t;
typedef long int		int_fast32_t;
typedef long int		int_fast64_t;







/* Unsigned.  */
typedef unsigned char		uint_fast8_t;

typedef unsigned long int	uint_fast16_t;
typedef unsigned long int	uint_fast32_t;
typedef unsigned long int	uint_fast64_t;








/* Types for `void *' pointers.  */





typedef unsigned long int	uintptr_t;









/* Largest integral types.  */
typedef __intmax_t		intmax_t;
typedef __uintmax_t		uintmax_t;










/* Limits of integral types.  */

/* Minimum of signed integral types.  */




/* Maximum of signed integral types.  */





/* Maximum of unsigned integral types.  */






/* Minimum of signed integral types having a minimum size.  */




/* Maximum of signed integral types having a minimum size.  */





/* Maximum of unsigned integral types having a minimum size.  */






/* Minimum of fast signed integral types having a minimum size.  */









/* Maximum of fast signed integral types having a minimum size.  */










/* Maximum of fast unsigned integral types having a minimum size.  */











/* Values to test for integral types holding `void *' pointer.  */











/* Minimum for largest signed integral type.  */

/* Maximum for largest signed integral type.  */


/* Maximum for largest unsigned integral type.  */



/* Limits of other integer types.  */

/* Limits of `ptrdiff_t' type.  */













/* Limits of `sig_atomic_t'.  */



/* Limit of `size_t' type.  */










/* Limits of `wchar_t'.  */

/* These constants might also be defined in <wchar.h>.  */




/* Limits of `wint_t'.  */



/* Signed.  */









/* Unsigned.  */









/* Maximal type.  */





















































#line 27 "/usr/include/inttypes.h" 2

/* Get a definition for wchar_t.  But we must not define wchar_t itself.  */





















/* Macros for printing format specifiers.  */

/* Decimal notation.  */































/* Octal notation.  */















/* Unsigned integers.  */















/* lowercase hexadecimal notation.  */















/* UPPERCASE hexadecimal notation.  */
















/* Macros for printing `intmax_t' and `uintmax_t'.  */








/* Macros for printing `intptr_t' and `uintptr_t'.  */








/* Macros for scanning format specifiers.  */

/* Signed decimal notation.  */















/* Signed decimal notation.  */















/* Unsigned decimal notation.  */















/* Octal notation.  */















/* Hexadecimal notation.  */
















/* Macros for scanning `intmax_t' and `uintmax_t'.  */






/* Macros for scaning `intptr_t' and `uintptr_t'.  */







extern "C" {



/* We have to define the `uintmax_t' type using `ldiv_t'.  */
typedef struct
  {
    long int quot;		/* Quotient.  */
    long int rem;		/* Remainder.  */
  } imaxdiv_t;





  
    
    
  




/* Compute absolute value of N.  */
extern intmax_t imaxabs (intmax_t __n)  ;

/* Return the `imaxdiv_t' representation of the value of NUMER over DENOM. */
extern imaxdiv_t imaxdiv (intmax_t __numer, intmax_t __denom)
       ;

/* Like `strtol' but convert to `intmax_t'.  */
extern intmax_t strtoimax (const char * __nptr,
			   char ** __endptr, int __base) ;

/* Like `strtoul' but convert to `uintmax_t'.  */
extern uintmax_t strtoumax (const char * __nptr,
			    char **  __endptr, int __base) ;

/* Like `wcstol' but convert to `intmax_t'.  */
extern intmax_t wcstoimax (const wchar_t * __nptr,
			   wchar_t ** __endptr, int __base)
     ;

/* Like `wcstoul' but convert to `uintmax_t'.  */
extern uintmax_t wcstoumax (const wchar_t * __nptr,
			    wchar_t **  __endptr, int __base)
     ;






				   
				   
  



		  

  



					     
					     
  



		  

  



				   
				   
  



		  

  



					     
					     
					     
					     
  



		  

  






					 
					 
  



		  

  




						   
						   
						   
						   
						   
  



		  

  




					 
					 
  



		  

  





						   
						   
						   
						   
						   
  



		  

  





}



#line 40 "/usr/include/libssh/libssh.h" 2
  
#line 1 "/usr/include/sys/types.h" 1
/* Copyright (C) 1991-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */

/*
 *	POSIX Standard: 2.6 Primitive System Data Types	<sys/types.h>
 */





#line 1 "/usr/include/sys/types.h" 1

extern "C" {


#line 1 "/usr/include/sys/types.h" 1



typedef __u_char u_char;
typedef __u_short u_short;
typedef __u_int u_int;
typedef __u_long u_long;
typedef __quad_t quad_t;
typedef __u_quad_t u_quad_t;
typedef __fsid_t fsid_t;




typedef __loff_t loff_t;



typedef __ino_t ino_t;











typedef __dev_t dev_t;









typedef __mode_t mode_t;




typedef __nlink_t nlink_t;




























typedef __id_t id_t;










typedef __daddr_t daddr_t;
typedef __caddr_t caddr_t;





typedef __key_t key_t;





#line 1 "/usr/include/bits/types/clock_t.h" 1




#line 1 "/usr/include/bits/types/clock_t.h" 1

/* Returned by `clock'.  */
typedef __clock_t clock_t;



#line 70 "/usr/include/sys/types.h" 2


#line 1 "/usr/include/bits/types/clockid_t.h" 1




#line 1 "/usr/include/bits/types/clockid_t.h" 1

/* Clock ID used in clock and timer functions.  */
typedef __clockid_t clockid_t;



#line 71 "/usr/include/sys/types.h" 2

#line 1 "/usr/include/bits/types/time_t.h" 1




#line 1 "/usr/include/bits/types/time_t.h" 1

/* Returned by `time'.  */
typedef __time_t time_t;



#line 72 "/usr/include/sys/types.h" 2

#line 1 "/usr/include/bits/types/timer_t.h" 1




#line 1 "/usr/include/bits/types/timer_t.h" 1

/* Timer ID returned by `timer_create'.  */
typedef __timer_t timer_t;



#line 73 "/usr/include/sys/types.h" 2














#line 1 "/usr/include/sys/types.h" 1


/* Old compatibility names for C types.  */
typedef unsigned long int ulong;
typedef unsigned short int ushort;
typedef unsigned int uint;


/* These size-specific names are used by some of the inet code.  */


#line 1 "/usr/include/sys/types.h" 1



/* These were defined by ISO C without the first `_'.  */
typedef	unsigned char u_int8_t;
typedef	unsigned short int u_int16_t;
typedef	unsigned int u_int32_t;

typedef unsigned long int u_int64_t;




typedef int register_t;






















/* In BSD <sys/types.h> is expected to define BYTE_ORDER.  */

#line 1 "/usr/include/endian.h" 1
/* Copyright (C) 1992-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */





#line 1 "/usr/include/endian.h" 1

/* Definitions for byte order, according to significance of bytes,
   from low addresses to high addresses.  The value is what you get by
   putting '4' in the most significant byte, '3' in the second most
   significant byte, '2' in the second least significant byte, and '1'
   in the least significant byte, and then writing down one digit for
   each byte, starting with the byte at the lowest address at the left,
   and proceeding to the byte with the highest address at the right.  */





/* This file defines `__BYTE_ORDER' for the particular machine.  */

#line 1 "/usr/include/bits/endian.h" 1
/* i386/x86_64 are little-endian.  */







#line 33 "/usr/include/endian.h" 2

/* Some machines may need to use a different endianness for floating point
   values.  */



















/* Conversion interfaces.  */

#line 1 "/usr/include/bits/byteswap.h" 1
/* Macros to swap the order of bytes in integer values.
   Copyright (C) 1997-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */









#line 1 "/usr/include/bits/byteswap.h" 1

#line 1 "/usr/include/bits/byteswap.h" 1

#line 1 "/usr/include/bits/byteswap.h" 1

/* Swap bytes in 16 bit value.  */



/* Get __bswap_16.  */

#line 1 "/usr/include/bits/byteswap-16.h" 1
/* Macros to swap the order of bytes in 16-bit integer values.
   Copyright (C) 2012-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */



















/* This is better than nothing.  */









  



#line 33 "/usr/include/bits/byteswap.h" 2

/* Swap bytes in 32 bit value.  */









  










































  




















  




































static __inline __uint64_t
__bswap_64 (__uint64_t __bsx)
{
  return ((((__bsx) & 0xff00000000000000ull) >> 56) | (((__bsx) & 0x00ff000000000000ull) >> 40) | (((__bsx) & 0x0000ff0000000000ull) >> 24) | (((__bsx) & 0x000000ff00000000ull) >> 8) | (((__bsx) & 0x00000000ff000000ull) << 8) | (((__bsx) & 0x0000000000ff0000ull) << 24) | (((__bsx) & 0x000000000000ff00ull) << 40) | (((__bsx) & 0x00000000000000ffull) << 56));
}




#line 42 "/usr/include/endian.h" 2

#line 1 "/usr/include/bits/uintn-identity.h" 1
/* Inline functions to return unsigned integer values unchanged.
   Copyright (C) 2017-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */









#line 1 "/usr/include/bits/uintn-identity.h" 1

/* These inline functions are to ensure the appropriate type
   conversions and associated diagnostics from macros that convert to
   a given endianness.  */

static __inline __uint16_t
__uint16_identity (__uint16_t __x)
{
  return __x;
}

static __inline __uint32_t
__uint32_identity (__uint32_t __x)
{
  return __x;
}

static __inline __uint64_t
__uint64_identity (__uint64_t __x)
{
  return __x;
}



#line 43 "/usr/include/endian.h" 2





































#line 118 "/usr/include/sys/types.h" 2

/* It also defines `fd_set' and the FD_* macros for `select'.  */

#line 1 "/usr/include/sys/select.h" 1
/* `fd_set' type and related macros, and `select'/`pselect' declarations.
   Copyright (C) 1996-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */

/*	POSIX 1003.1g: 6.2 Select from File Descriptor Sets <sys/select.h>  */





#line 1 "/usr/include/sys/select.h" 1

/* Get definition of needed basic types.  */

#line 1 "/usr/include/sys/select.h" 1

/* Get __FD_* definitions.  */

#line 1 "/usr/include/bits/select.h" 1
/* Copyright (C) 1997-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */






#line 1 "/usr/include/bits/select.h" 1























/* We don't use `memset' because this would require a prototype and
   the array isn't too big.  */

















#line 30 "/usr/include/sys/select.h" 2

/* Get sigset_t.  */

#line 1 "/usr/include/bits/types/sigset_t.h" 1




#line 1 "/usr/include/bits/types/__sigset_t.h" 1




typedef struct
{
  unsigned long int __val[(1024 / (8 * sizeof (unsigned long int)))];
} __sigset_t;



#line 4 "/usr/include/bits/types/sigset_t.h" 2

/* A set of signals to be blocked, unblocked, or waited for.  */
typedef __sigset_t sigset_t;



#line 33 "/usr/include/sys/select.h" 2

/* Get definition of timer specification structures.  */

#line 1 "/usr/include/sys/select.h" 1

#line 1 "/usr/include/bits/types/struct_timeval.h" 1




#line 1 "/usr/include/bits/types/struct_timeval.h" 1

/* A time value that is accurate to the nearest
   microsecond but also has a range of years.  */
struct timeval
{
  __time_t tv_sec;		/* Seconds.  */
  __suseconds_t tv_usec;	/* Microseconds.  */
};


#line 37 "/usr/include/sys/select.h" 2


#line 1 "/usr/include/bits/types/struct_timespec.h" 1




#line 1 "/usr/include/bits/types/struct_timespec.h" 1

/* POSIX.1b structure for a time value.  This is like a `struct timeval' but
   has nanoseconds instead of microseconds.  */
struct timespec
{
  __time_t tv_sec;		/* Seconds.  */
  __syscall_slong_t tv_nsec;	/* Nanoseconds.  */
};



#line 38 "/usr/include/sys/select.h" 2



typedef __suseconds_t suseconds_t;




/* The fd_set member is required to be an array of longs.  */
typedef long int __fd_mask;

/* Some versions of <linux/posix_types.h> define this macros.  */

/* It's easier to assume 8-bit bytes than to get CHAR_BIT.  */




/* fd_set for select and pselect.  */
typedef struct
  {
    /* XPG4.2 requires this member name.  Otherwise avoid the name
       from the global namespace.  */

    


    __fd_mask __fds_bits[1024 / (8 * (int) sizeof (__fd_mask))];


  } fd_set;

/* Maximum number of file descriptors in `fd_set'.  */



/* Sometimes the fd_set member is assumed to have this type.  */
typedef __fd_mask fd_mask;

/* Number of bits per word of `fd_set' (some code assumes this is 32).  */




/* Access macros for `fd_set'.  */






extern "C" {

/* Check the first NFDS descriptors each in READFDS (if not NULL) for read
   readiness, in WRITEFDS (if not NULL) for write readiness, and in EXCEPTFDS
   (if not NULL) for exceptional conditions.  If TIMEOUT is not NULL, time out
   after waiting the interval specified therein.  Returns the number of ready
   descriptors, or -1 for errors.

   This function is a cancellation point and therefore not marked with
   __THROW.  */
extern int select (int __nfds, fd_set * __readfds,
		   fd_set * __writefds,
		   fd_set * __exceptfds,
		   struct timeval * __timeout);


/* Same as above only that the TIMEOUT value is given with higher
   resolution and a sigmask which is been set temporarily.  This version
   should be used.

   This function is a cancellation point and therefore not marked with
   __THROW.  */
extern int pselect (int __nfds, fd_set * __readfds,
		    fd_set * __writefds,
		    fd_set * __exceptfds,
		    const struct timespec * __timeout,
		    const __sigset_t * __sigmask);



/* Define some inlines helping to catch common problems.  */




}



#line 121 "/usr/include/sys/types.h" 2

/* BSD defines `major', `minor', and `makedev' in this header.
   However, these symbols are likely to collide with user code, so we are
   going to stop defining them here in an upcoming release.  Code that needs
   these macros should include <sys/sysmacros.h> directly.  Code that does
   not need these macros should #undef them after including this header.  */


#line 1 "/usr/include/sys/sysmacros.h" 1
/* Definitions of macros to access `dev_t' values.
   Copyright (C) 1996-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */







/* If <sys/sysmacros.h> is included after <sys/types.h>, these macros
   will already be defined, and we need to redefine them without the
   deprecation warnings.  (If they are included in the opposite order,
   the outer #ifndef will suppress this entire file and the macros
   will be usable without warnings.)  */




/* This is the macro that must be defined to satisfy the misuse check
   in bits/sysmacros.h. */




#line 1 "/usr/include/sys/sysmacros.h" 1

#line 1 "/usr/include/sys/sysmacros.h" 1

#line 1 "/usr/include/bits/sysmacros.h" 1
/* Definitions of macros to access `dev_t' values.
   Copyright (C) 1996-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */








/* dev_t in glibc is a 64-bit quantity, with 32-bit major and minor numbers.
   Our default encoding is MMMM Mmmm mmmM MMmm, where M is a hex digit of
   the major number and m is a hex digit of the minor number.  This is
   downward compatible with legacy systems where dev_t is 16 bits wide,
   encoded as MMmm.  It is also downward compatible with the Linux kernel,
   which (as of 2016) uses 32-bit dev_t, encoded as mmmM MMmm.

   Systems that use an incompatible encoding for dev_t should override this
   file in the appropriate sysdeps subdirectory.  */









































#line 34 "/usr/include/sys/sysmacros.h" 2

/* Caution: The text of this deprecation message is unquoted, so that
   #symbol can be substituted.  (It is converted to a string by
   __SYSMACROS_DM1.)  This means the message must be a sequence of
   complete pp-tokens; in particular, English contractions (it's,
   can't) cannot be used.

   The message has been manually word-wrapped to fit in 80 columns
   when output by GCC 5 and 6.  The first line is shorter to leave
   some room for the "foo.c:23: warning:" annotation.  */








/* This macro is variadic because the deprecation message above
   contains commas.  */









extern "C" {

extern unsigned int gnu_dev_major (__dev_t __dev)  ;
extern unsigned int gnu_dev_minor (__dev_t __dev)  ;
extern __dev_t gnu_dev_makedev (unsigned int __major, unsigned int __minor)  ;









}


























#line 128 "/usr/include/sys/types.h" 2






typedef __blksize_t blksize_t;



/* Types from the Large File Support interface.  */


typedef __blkcnt_t blkcnt_t;	 /* Type to count number of disk blocks.  */



typedef __fsblkcnt_t fsblkcnt_t; /* Type to count file system blocks.  */



typedef __fsfilcnt_t fsfilcnt_t; /* Type to count file system inodes.  */
























/* Now add the thread types.  */


#line 1 "/usr/include/bits/pthreadtypes.h" 1
/* Declaration of common pthread types for all architectures.
   Copyright (C) 2017-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */




/* For internal mutex and condition variable definitions.  */

#line 1 "/usr/include/bits/thread-shared-types.h" 1
/* Common threading primitives definitions for both POSIX and C11.
   Copyright (C) 2017-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */




/* Arch-specific definitions.  Each architecture must define the following
   macros to define the expected sizes of pthread data types:

   __SIZEOF_PTHREAD_ATTR_T        - size of pthread_attr_t.
   __SIZEOF_PTHREAD_MUTEX_T       - size of pthread_mutex_t.
   __SIZEOF_PTHREAD_MUTEXATTR_T   - size of pthread_mutexattr_t.
   __SIZEOF_PTHREAD_COND_T        - size of pthread_cond_t.
   __SIZEOF_PTHREAD_CONDATTR_T    - size of pthread_condattr_t.
   __SIZEOF_PTHREAD_RWLOCK_T      - size of pthread_rwlock_t.
   __SIZEOF_PTHREAD_RWLOCKATTR_T  - size of pthread_rwlockattr_t.
   __SIZEOF_PTHREAD_BARRIER_T     - size of pthread_barrier_t.
   __SIZEOF_PTHREAD_BARRIERATTR_T - size of pthread_barrierattr_t.

   Also, the following macros must be define for internal pthread_mutex_t
   struct definitions (struct __pthread_mutex_s):

   __PTHREAD_COMPAT_PADDING_MID   - any additional members after 'kind'
				    and before '__spin' (for 64 bits) or
				    '__nusers' (for 32 bits).
   __PTHREAD_COMPAT_PADDING_END   - any additional members at the end of
				    the internal structure.
   __PTHREAD_MUTEX_LOCK_ELISION   - 1 if the architecture supports lock
				    elision or 0 otherwise.
   __PTHREAD_MUTEX_NUSERS_AFTER_KIND - control where to put __nusers.  The
				       preferred value for new architectures
				       is 0.
   __PTHREAD_MUTEX_USE_UNION      - control whether internal __spins and
				    __list will be place inside a union for
				    linuxthreads compatibility.
				    The preferred value for new architectures
				    is 0.

   For a new port the preferred values for the required defines are:

   #define __PTHREAD_COMPAT_PADDING_MID
   #define __PTHREAD_COMPAT_PADDING_END
   #define __PTHREAD_MUTEX_LOCK_ELISION         0
   #define __PTHREAD_MUTEX_NUSERS_AFTER_KIND    0
   #define __PTHREAD_MUTEX_USE_UNION            0

   __PTHREAD_MUTEX_LOCK_ELISION can be set to 1 if the hardware plans to
   eventually support lock elision using transactional memory.

   The additional macro defines any constraint for the lock alignment
   inside the thread structures:

   __LOCK_ALIGNMENT - for internal lock/futex usage.

   Same idea but for the once locking primitive:

   __ONCE_ALIGNMENT - for pthread_once_t/once_flag definition.

   And finally the internal pthread_rwlock_t (struct __pthread_rwlock_arch_t)
   must be defined.
 */

#line 1 "/usr/include/bits/pthreadtypes-arch.h" 1
/* Copyright (C) 2002-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */





#line 1 "/usr/include/bits/pthreadtypes-arch.h" 1




























/* Definitions for internal mutex struct.  */














struct __pthread_rwlock_arch_t
{
  unsigned int __readers;
  unsigned int __writers;
  unsigned int __wrphase_futex;
  unsigned int __writers_futex;
  unsigned int __pad3;
  unsigned int __pad4;

  int __cur_writer;
  int __shared;
  signed char __rwelision;

  


  unsigned char __pad1[7];


  unsigned long int __pad2;
  /* FLAGS must stay at this position in the structure to maintain
     binary compatibility.  */
  unsigned int __flags;


  

  
  
  

  
  

};








#line 77 "/usr/include/bits/thread-shared-types.h" 2

/* Common definition of pthread_mutex_t. */


typedef struct __pthread_internal_list
{
  struct __pthread_internal_list *__prev;
  struct __pthread_internal_list *__next;
} __pthread_list_t;



  



/* Lock elision support.  */























struct __pthread_mutex_s
{
  int __lock ;
  unsigned int __count;
  int __owner;

  unsigned int __nusers;

  /* KIND must stay at this position in the structure to maintain
     binary compatibility with static initializers.  */
  int __kind;
  

  


  short __spins; short __elision;
  __pthread_list_t __list;


  
  
    
    
  


  
};


/* Common definition of pthread_cond_t. */

struct __pthread_cond_s
{
   union
  {
     unsigned long long int __wseq;
    struct
    {
      unsigned int __low;
      unsigned int __high;
    } __wseq32;
  };
   union
  {
     unsigned long long int __g1_start;
    struct
    {
      unsigned int __low;
      unsigned int __high;
    } __g1_start32;
  };
  unsigned int __g_refs[2] ;
  unsigned int __g_size[2];
  unsigned int __g1_orig_size;
  unsigned int __wrefs;
  unsigned int __g_signals[2];
};



#line 23 "/usr/include/bits/pthreadtypes.h" 2

/* Thread identifiers.  The structure of the attribute type is not
   exposed on purpose.  */
typedef unsigned long int pthread_t;


/* Data structures for mutex handling.  The structure of the attribute
   type is not exposed on purpose.  */
typedef union
{
  char __size[4];
  int __align;
} pthread_mutexattr_t;


/* Data structure for condition variable handling.  The structure of
   the attribute type is not exposed on purpose.  */
typedef union
{
  char __size[4];
  int __align;
} pthread_condattr_t;


/* Keys for thread-specific data */
typedef unsigned int pthread_key_t;


/* Once-only execution */
typedef int  pthread_once_t;


union pthread_attr_t
{
  char __size[56];
  long int __align;
};

typedef union pthread_attr_t pthread_attr_t;




typedef union
{
  struct __pthread_mutex_s __data;
  char __size[40];
  long int __align;
} pthread_mutex_t;


typedef union
{
  struct __pthread_cond_s __data;
  char __size[48];
   long long int __align;
} pthread_cond_t;



/* Data structure for reader-writer lock variable handling.  The
   structure of the attribute type is deliberately not exposed.  */
typedef union
{
  struct __pthread_rwlock_arch_t __data;
  char __size[56];
  long int __align;
} pthread_rwlock_t;

typedef union
{
  char __size[8];
  long int __align;
} pthread_rwlockattr_t;




/* POSIX spinlock data type.  */
typedef volatile int pthread_spinlock_t;


/* POSIX barriers data type.  The structure of the type is
   deliberately not exposed.  */
typedef union
{
  char __size[32];
  long int __align;
} pthread_barrier_t;

typedef union
{
  char __size[4];
  int __align;
} pthread_barrierattr_t;




#line 149 "/usr/include/sys/types.h" 2


}



#line 41 "/usr/include/libssh/libssh.h" 2



  
#line 1 "/usr/include/libssh/libssh.h" 1

 
 





/* libssh version macros */




/* libssh version */











/* GCC have printf type attribute check.  */













extern "C" {


struct ssh_counter_struct {
    uint64_t in_bytes;
    uint64_t out_bytes;
    uint64_t in_packets;
    uint64_t out_packets;
};
typedef struct ssh_counter_struct *ssh_counter;

typedef struct ssh_agent_struct* ssh_agent;
typedef struct ssh_buffer_struct* ssh_buffer;
typedef struct ssh_channel_struct* ssh_channel;
typedef struct ssh_message_struct* ssh_message;
typedef struct ssh_pcap_file_struct* ssh_pcap_file;
typedef struct ssh_key_struct* ssh_key;
typedef struct ssh_scp_struct* ssh_scp;
typedef struct ssh_session_struct* ssh_session;
typedef struct ssh_string_struct* ssh_string;
typedef struct ssh_event_struct* ssh_event;
typedef struct ssh_connector_struct * ssh_connector;
typedef void* ssh_gssapi_creds;

/* Socket type */


typedef SOCKET socket_t;









/* the offsets of methods */
enum ssh_kex_types_e {
	SSH_KEX=0,
	SSH_HOSTKEYS,
	SSH_CRYPT_C_S,
	SSH_CRYPT_S_C,
	SSH_MAC_C_S,
	SSH_MAC_S_C,
	SSH_COMP_C_S,
	SSH_COMP_S_C,
	SSH_LANG_C_S,
	SSH_LANG_S_C
};






enum ssh_auth_e {
	SSH_AUTH_SUCCESS=0,
	SSH_AUTH_DENIED,
	SSH_AUTH_PARTIAL,
	SSH_AUTH_INFO,
	SSH_AUTH_AGAIN,
	SSH_AUTH_ERROR=-1
};

/* auth flags */








/* messages */
enum ssh_requests_e {
	SSH_REQUEST_AUTH=1,
	SSH_REQUEST_CHANNEL_OPEN,
	SSH_REQUEST_CHANNEL,
	SSH_REQUEST_SERVICE,
	SSH_REQUEST_GLOBAL
};

enum ssh_channel_type_e {
	SSH_CHANNEL_UNKNOWN=0,
	SSH_CHANNEL_SESSION,
	SSH_CHANNEL_DIRECT_TCPIP,
	SSH_CHANNEL_FORWARDED_TCPIP,
	SSH_CHANNEL_X11,
	SSH_CHANNEL_AUTH_AGENT
};

enum ssh_channel_requests_e {
	SSH_CHANNEL_REQUEST_UNKNOWN=0,
	SSH_CHANNEL_REQUEST_PTY,
	SSH_CHANNEL_REQUEST_EXEC,
	SSH_CHANNEL_REQUEST_SHELL,
	SSH_CHANNEL_REQUEST_ENV,
	SSH_CHANNEL_REQUEST_SUBSYSTEM,
	SSH_CHANNEL_REQUEST_WINDOW_CHANGE,
	SSH_CHANNEL_REQUEST_X11
};

enum ssh_global_requests_e {
	SSH_GLOBAL_REQUEST_UNKNOWN=0,
	SSH_GLOBAL_REQUEST_TCPIP_FORWARD,
	SSH_GLOBAL_REQUEST_CANCEL_TCPIP_FORWARD,
	SSH_GLOBAL_REQUEST_KEEPALIVE
};

enum ssh_publickey_state_e {
	SSH_PUBLICKEY_STATE_ERROR=-1,
	SSH_PUBLICKEY_STATE_NONE=0,
	SSH_PUBLICKEY_STATE_VALID=1,
	SSH_PUBLICKEY_STATE_WRONG=2
};

/* Status flags */
/** Socket is closed */

/** Reading to socket won't block */

/** Session was closed due to an error */

/** Output buffer not empty */


enum ssh_server_known_e {
	SSH_SERVER_ERROR=-1,
	SSH_SERVER_NOT_KNOWN=0,
	SSH_SERVER_KNOWN_OK,
	SSH_SERVER_KNOWN_CHANGED,
	SSH_SERVER_FOUND_OTHER,
	SSH_SERVER_FILE_NOT_FOUND
};


    

/* errors */

enum ssh_error_types_e {
	SSH_NO_ERROR=0,
	SSH_REQUEST_DENIED,
	SSH_FATAL,
	SSH_EINTR
};

/* some types for keys */
enum ssh_keytypes_e{
  SSH_KEYTYPE_UNKNOWN=0,
  SSH_KEYTYPE_DSS=1,
  SSH_KEYTYPE_RSA,
  SSH_KEYTYPE_RSA1,
  SSH_KEYTYPE_ECDSA,
  SSH_KEYTYPE_ED25519,
  SSH_KEYTYPE_DSS_CERT01,
  SSH_KEYTYPE_RSA_CERT01
};

enum ssh_keycmp_e {
  SSH_KEY_CMP_PUBLIC = 0,
  SSH_KEY_CMP_PRIVATE
};

/* Error return codes */





/**
 * @addtogroup libssh_log
 *
 * @{
 */

enum {
	/** No logging at all
	 */
	SSH_LOG_NOLOG=0,
	/** Only warnings
	 */
	SSH_LOG_WARNING,
	/** High level protocol information
	 */
	SSH_LOG_PROTOCOL,
	/** Lower level protocol infomations, packet level
	 */
	SSH_LOG_PACKET,
	/** Every function path
	 */
	SSH_LOG_FUNCTIONS
};
/** @} */


/**
 * @name Logging levels
 *
 * @brief Debug levels for logging.
 * @{
 */

/** No logging at all */

/** Show only warnings */

/** Get some information what's going on */

/** Get detailed debuging information **/

/** Get trace output, packet information, ... */


/** @} */

enum ssh_options_e {
  SSH_OPTIONS_HOST,
  SSH_OPTIONS_PORT,
  SSH_OPTIONS_PORT_STR,
  SSH_OPTIONS_FD,
  SSH_OPTIONS_USER,
  SSH_OPTIONS_SSH_DIR,
  SSH_OPTIONS_IDENTITY,
  SSH_OPTIONS_ADD_IDENTITY,
  SSH_OPTIONS_KNOWNHOSTS,
  SSH_OPTIONS_TIMEOUT,
  SSH_OPTIONS_TIMEOUT_USEC,
  SSH_OPTIONS_SSH1,
  SSH_OPTIONS_SSH2,
  SSH_OPTIONS_LOG_VERBOSITY,
  SSH_OPTIONS_LOG_VERBOSITY_STR,
  SSH_OPTIONS_CIPHERS_C_S,
  SSH_OPTIONS_CIPHERS_S_C,
  SSH_OPTIONS_COMPRESSION_C_S,
  SSH_OPTIONS_COMPRESSION_S_C,
  SSH_OPTIONS_PROXYCOMMAND,
  SSH_OPTIONS_BINDADDR,
  SSH_OPTIONS_STRICTHOSTKEYCHECK,
  SSH_OPTIONS_COMPRESSION,
  SSH_OPTIONS_COMPRESSION_LEVEL,
  SSH_OPTIONS_KEY_EXCHANGE,
  SSH_OPTIONS_HOSTKEYS,
  SSH_OPTIONS_GSSAPI_SERVER_IDENTITY,
  SSH_OPTIONS_GSSAPI_CLIENT_IDENTITY,
  SSH_OPTIONS_GSSAPI_DELEGATE_CREDENTIALS,
  SSH_OPTIONS_HMAC_C_S,
  SSH_OPTIONS_HMAC_S_C,
};

enum {
  /** Code is going to write/create remote files */
  SSH_SCP_WRITE,
  /** Code is going to read remote files */
  SSH_SCP_READ,
  SSH_SCP_RECURSIVE=0x10
};

enum ssh_scp_request_types {
  /** A new directory is going to be pulled */
  SSH_SCP_REQUEST_NEWDIR=1,
  /** A new file is going to be pulled */
  SSH_SCP_REQUEST_NEWFILE,
  /** End of requests */
  SSH_SCP_REQUEST_EOF,
  /** End of directory */
  SSH_SCP_REQUEST_ENDDIR,
  /** Warning received */
  SSH_SCP_REQUEST_WARNING
};

enum ssh_connector_flags_e {
    /** Only the standard stream of the channel */
    SSH_CONNECTOR_STDOUT = 1,
    /** Only the exception stream of the channel */
    SSH_CONNECTOR_STDERR = 2,
    /** Merge both standard and exception streams */
    SSH_CONNECTOR_BOTH = 3
};

 int ssh_blocking_flush(ssh_session session, int timeout);
 ssh_channel ssh_channel_accept_x11(ssh_channel channel, int timeout_ms);
 int ssh_channel_change_pty_size(ssh_channel channel,int cols,int rows);
 int ssh_channel_close(ssh_channel channel);
 void ssh_channel_free(ssh_channel channel);
 int ssh_channel_get_exit_status(ssh_channel channel);
 ssh_session ssh_channel_get_session(ssh_channel channel);
 int ssh_channel_is_closed(ssh_channel channel);
 int ssh_channel_is_eof(ssh_channel channel);
 int ssh_channel_is_open(ssh_channel channel);
 ssh_channel ssh_channel_new(ssh_session session);
 int ssh_channel_open_auth_agent(ssh_channel channel);
 int ssh_channel_open_forward(ssh_channel channel, const char *remotehost,
    int remoteport, const char *sourcehost, int localport);
 int ssh_channel_open_session(ssh_channel channel);
 int ssh_channel_open_x11(ssh_channel channel, const char *orig_addr, int orig_port);
 int ssh_channel_poll(ssh_channel channel, int is_stderr);
 int ssh_channel_poll_timeout(ssh_channel channel, int timeout, int is_stderr);
 int ssh_channel_read(ssh_channel channel, void *dest, uint32_t count, int is_stderr);
 int ssh_channel_read_timeout(ssh_channel channel, void *dest, uint32_t count, int is_stderr, int timeout_ms);
 int ssh_channel_read_nonblocking(ssh_channel channel, void *dest, uint32_t count,
    int is_stderr);
 int ssh_channel_request_env(ssh_channel channel, const char *name, const char *value);
 int ssh_channel_request_exec(ssh_channel channel, const char *cmd);
 int ssh_channel_request_pty(ssh_channel channel);
 int ssh_channel_request_pty_size(ssh_channel channel, const char *term,
    int cols, int rows);
 int ssh_channel_request_shell(ssh_channel channel);
 int ssh_channel_request_send_signal(ssh_channel channel, const char *signum);
 int ssh_channel_request_sftp(ssh_channel channel);
 int ssh_channel_request_subsystem(ssh_channel channel, const char *subsystem);
 int ssh_channel_request_x11(ssh_channel channel, int single_connection, const char *protocol,
    const char *cookie, int screen_number);
 int ssh_channel_request_auth_agent(ssh_channel channel);
 int ssh_channel_send_eof(ssh_channel channel);
 int ssh_channel_select(ssh_channel *readchans, ssh_channel *writechans, ssh_channel *exceptchans, struct
        timeval * timeout);
 void ssh_channel_set_blocking(ssh_channel channel, int blocking);
 void ssh_channel_set_counter(ssh_channel channel,
                                        ssh_counter counter);
 int ssh_channel_write(ssh_channel channel, const void *data, uint32_t len);
 int ssh_channel_write_stderr(ssh_channel channel,
                                        const void *data,
                                        uint32_t len);
 uint32_t ssh_channel_window_size(ssh_channel channel);

 char *ssh_basename (const char *path);
 void ssh_clean_pubkey_hash(unsigned char **hash);
 int ssh_connect(ssh_session session);

 ssh_connector ssh_connector_new(ssh_session session);
 void ssh_connector_free(ssh_connector connector);
 int ssh_connector_set_in_channel(ssh_connector connector,
                                            ssh_channel channel,
                                            enum ssh_connector_flags_e flags);
 int ssh_connector_set_out_channel(ssh_connector connector,
                                             ssh_channel channel,
                                             enum ssh_connector_flags_e flags);
 void ssh_connector_set_in_fd(ssh_connector connector, socket_t fd);
 void ssh_connector_set_out_fd(ssh_connector connector, socket_t fd);

 const char *ssh_copyright(void);
 void ssh_disconnect(ssh_session session);
 char *ssh_dirname (const char *path);
 int ssh_finalize(void);

/* REVERSE PORT FORWARDING */
 ssh_channel ssh_channel_accept_forward(ssh_session session,
                                                  int timeout_ms,
                                                  int *destination_port);
 int ssh_channel_cancel_forward(ssh_session session,
                                          const char *address,
                                          int port);
 int ssh_channel_listen_forward(ssh_session session,
                                          const char *address,
                                          int port,
                                          int *bound_port);

 void ssh_free(ssh_session session);
 const char *ssh_get_disconnect_message(ssh_session session);
 const char *ssh_get_error(void *error);
 int ssh_get_error_code(void *error);
 socket_t ssh_get_fd(ssh_session session);
 char *ssh_get_hexa(const unsigned char *what, size_t len);
 char *ssh_get_issue_banner(ssh_session session);
 int ssh_get_openssh_version(ssh_session session);

 int ssh_get_server_publickey(ssh_session session, ssh_key *key);

enum ssh_publickey_hash_type {
    SSH_PUBLICKEY_HASH_SHA1,
    SSH_PUBLICKEY_HASH_MD5
};
 int ssh_get_publickey_hash(const ssh_key key,
                                      enum ssh_publickey_hash_type type,
                                      unsigned char **hash,
                                      size_t *hlen);

/* DEPRECATED FUNCTIONS */
  int ssh_get_pubkey_hash(ssh_session session, unsigned char **hash);
  ssh_channel ssh_forward_accept(ssh_session session, int timeout_ms);
  int ssh_forward_cancel(ssh_session session, const char *address, int port);
  int ssh_forward_listen(ssh_session session, const char *address, int port, int *bound_port);
  int ssh_get_publickey(ssh_session session, ssh_key *key);


 int ssh_get_random(void *where,int len,int strong);
 int ssh_get_version(ssh_session session);
 int ssh_get_status(ssh_session session);
 int ssh_get_poll_flags(ssh_session session);
 int ssh_init(void);
 int ssh_is_blocking(ssh_session session);
 int ssh_is_connected(ssh_session session);
 int ssh_is_server_known(ssh_session session);

/* LOGGING */
 int ssh_set_log_level(int level);
 int ssh_get_log_level(void);
 void *ssh_get_log_userdata(void);
 int ssh_set_log_userdata(void *data);
 void _ssh_log(int verbosity,
                         const char *function,
                         const char *format, ...) ;

/* legacy */
  void ssh_log(ssh_session session,
                                       int prioriry,
                                       const char *format, ...) ;

 ssh_channel ssh_message_channel_request_open_reply_accept(ssh_message msg);
 int ssh_message_channel_request_reply_success(ssh_message msg);
 void ssh_message_free(ssh_message msg);
 ssh_message ssh_message_get(ssh_session session);
 int ssh_message_subtype(ssh_message msg);
 int ssh_message_type(ssh_message msg);
 int ssh_mkdir (const char *pathname, mode_t mode);
 ssh_session ssh_new(void);

 int ssh_options_copy(ssh_session src, ssh_session *dest);
 int ssh_options_getopt(ssh_session session, int *argcptr, char **argv);
 int ssh_options_parse_config(ssh_session session, const char *filename);
 int ssh_options_set(ssh_session session, enum ssh_options_e type,
    const void *value);
 int ssh_options_get(ssh_session session, enum ssh_options_e type,
    char **value);
 int ssh_options_get_port(ssh_session session, unsigned int * port_target);
 int ssh_pcap_file_close(ssh_pcap_file pcap);
 void ssh_pcap_file_free(ssh_pcap_file pcap);
 ssh_pcap_file ssh_pcap_file_new(void);
 int ssh_pcap_file_open(ssh_pcap_file pcap, const char *filename);

/**
 * @brief SSH authentication callback.
 *
 * @param prompt        Prompt to be displayed.
 * @param buf           Buffer to save the password. You should null-terminate it.
 * @param len           Length of the buffer.
 * @param echo          Enable or disable the echo of what you type.
 * @param verify        Should the password be verified?
 * @param userdata      Userdata to be passed to the callback function. Useful
 *                      for GUI applications.
 *
 * @return              0 on success, < 0 on error.
 */
typedef int (*ssh_auth_callback) (const char *prompt, char *buf, size_t len,
    int echo, int verify, void *userdata);

 ssh_key ssh_key_new(void);
 void ssh_key_free (ssh_key key);
 enum ssh_keytypes_e ssh_key_type(const ssh_key key);
 const char *ssh_key_type_to_char(enum ssh_keytypes_e type);
 enum ssh_keytypes_e ssh_key_type_from_name(const char *name);
 int ssh_key_is_public(const ssh_key k);
 int ssh_key_is_private(const ssh_key k);
 int ssh_key_cmp(const ssh_key k1,
                           const ssh_key k2,
                           enum ssh_keycmp_e what);

 int ssh_pki_generate(enum ssh_keytypes_e type, int parameter,
        ssh_key *pkey);
 int ssh_pki_import_privkey_base64(const char *b64_key,
                                             const char *passphrase,
                                             ssh_auth_callback auth_fn,
                                             void *auth_data,
                                             ssh_key *pkey);
 int ssh_pki_import_privkey_file(const char *filename,
                                           const char *passphrase,
                                           ssh_auth_callback auth_fn,
                                           void *auth_data,
                                           ssh_key *pkey);
 int ssh_pki_export_privkey_file(const ssh_key privkey,
                                           const char *passphrase,
                                           ssh_auth_callback auth_fn,
                                           void *auth_data,
                                           const char *filename);

 int ssh_pki_copy_cert_to_privkey(const ssh_key cert_key,
                                            ssh_key privkey);

 int ssh_pki_import_pubkey_base64(const char *b64_key,
                                            enum ssh_keytypes_e type,
                                            ssh_key *pkey);
 int ssh_pki_import_pubkey_file(const char *filename,
                                          ssh_key *pkey);

 int ssh_pki_import_cert_base64(const char *b64_cert,
                                          enum ssh_keytypes_e type,
                                          ssh_key *pkey);
 int ssh_pki_import_cert_file(const char *filename,
                                        ssh_key *pkey);

 int ssh_pki_export_privkey_to_pubkey(const ssh_key privkey,
                                                ssh_key *pkey);
 int ssh_pki_export_pubkey_base64(const ssh_key key,
                                            char **b64_key);
 int ssh_pki_export_pubkey_file(const ssh_key key,
                                          const char *filename);

 const char *ssh_pki_key_ecdsa_name(const ssh_key key);

 void ssh_print_hexa(const char *descr, const unsigned char *what, size_t len);
 int ssh_send_ignore (ssh_session session, const char *data);
 int ssh_send_debug (ssh_session session, const char *message, int always_display);
 void ssh_gssapi_set_creds(ssh_session session, const ssh_gssapi_creds creds);
 int ssh_scp_accept_request(ssh_scp scp);
 int ssh_scp_close(ssh_scp scp);
 int ssh_scp_deny_request(ssh_scp scp, const char *reason);
 void ssh_scp_free(ssh_scp scp);
 int ssh_scp_init(ssh_scp scp);
 int ssh_scp_leave_directory(ssh_scp scp);
 ssh_scp ssh_scp_new(ssh_session session, int mode, const char *location);
 int ssh_scp_pull_request(ssh_scp scp);
 int ssh_scp_push_directory(ssh_scp scp, const char *dirname, int mode);
 int ssh_scp_push_file(ssh_scp scp, const char *filename, size_t size, int perms);
 int ssh_scp_push_file64(ssh_scp scp, const char *filename, uint64_t size, int perms);
 int ssh_scp_read(ssh_scp scp, void *buffer, size_t size);
 const char *ssh_scp_request_get_filename(ssh_scp scp);
 int ssh_scp_request_get_permissions(ssh_scp scp);
 size_t ssh_scp_request_get_size(ssh_scp scp);
 uint64_t ssh_scp_request_get_size64(ssh_scp scp);
 const char *ssh_scp_request_get_warning(ssh_scp scp);
 int ssh_scp_write(ssh_scp scp, const void *buffer, size_t len);
 int ssh_select(ssh_channel *channels, ssh_channel *outchannels, socket_t maxfd,
    fd_set *readfds, struct timeval *timeout);
 int ssh_service_request(ssh_session session, const char *service);
 int ssh_set_agent_channel(ssh_session session, ssh_channel channel);
 int ssh_set_agent_socket(ssh_session session, socket_t fd);
 void ssh_set_blocking(ssh_session session, int blocking);
 void ssh_set_counters(ssh_session session, ssh_counter scounter,
                                 ssh_counter rcounter);
 void ssh_set_fd_except(ssh_session session);
 void ssh_set_fd_toread(ssh_session session);
 void ssh_set_fd_towrite(ssh_session session);
 void ssh_silent_disconnect(ssh_session session);
 int ssh_set_pcap_file(ssh_session session, ssh_pcap_file pcapfile);

/* USERAUTH */
 int ssh_userauth_none(ssh_session session, const char *username);
 int ssh_userauth_list(ssh_session session, const char *username);
 int ssh_userauth_try_publickey(ssh_session session,
                                          const char *username,
                                          const ssh_key pubkey);
 int ssh_userauth_publickey(ssh_session session,
                                      const char *username,
                                      const ssh_key privkey);


                                  

 int ssh_userauth_publickey_auto(ssh_session session,
                                           const char *username,
                                           const char *passphrase);
 int ssh_userauth_password(ssh_session session,
                                     const char *username,
                                     const char *password);

 int ssh_userauth_kbdint(ssh_session session, const char *user, const char *submethods);
 const char *ssh_userauth_kbdint_getinstruction(ssh_session session);
 const char *ssh_userauth_kbdint_getname(ssh_session session);
 int ssh_userauth_kbdint_getnprompts(ssh_session session);
 const char *ssh_userauth_kbdint_getprompt(ssh_session session, unsigned int i, char *echo);
 int ssh_userauth_kbdint_getnanswers(ssh_session session);
 const char *ssh_userauth_kbdint_getanswer(ssh_session session, unsigned int i);
 int ssh_userauth_kbdint_setanswer(ssh_session session, unsigned int i,
    const char *answer);
 int ssh_userauth_gssapi(ssh_session session);
 const char *ssh_version(int req_version);
 int ssh_write_knownhost(ssh_session session);
 char *ssh_dump_knownhost(ssh_session session);

 void ssh_string_burn(ssh_string str);
 ssh_string ssh_string_copy(ssh_string str);
 void *ssh_string_data(ssh_string str);
 int ssh_string_fill(ssh_string str, const void *data, size_t len);
 void ssh_string_free(ssh_string str);
 ssh_string ssh_string_from_char(const char *what);
 size_t ssh_string_len(ssh_string str);
 ssh_string ssh_string_new(size_t size);
 const char *ssh_string_get_char(ssh_string str);
 char *ssh_string_to_char(ssh_string str);
 void ssh_string_free_char(char *s);

 int ssh_getpass(const char *prompt, char *buf, size_t len, int echo,
    int verify);


typedef int (*ssh_event_callback)(socket_t fd, int revents, void *userdata);

 ssh_event ssh_event_new(void);
 int ssh_event_add_fd(ssh_event event, socket_t fd, short events,
                                    ssh_event_callback cb, void *userdata);
 int ssh_event_add_session(ssh_event event, ssh_session session);
 int ssh_event_add_connector(ssh_event event, ssh_connector connector);
 int ssh_event_dopoll(ssh_event event, int timeout);
 int ssh_event_remove_fd(ssh_event event, socket_t fd);
 int ssh_event_remove_session(ssh_event event, ssh_session session);
 int ssh_event_remove_connector(ssh_event event, ssh_connector connector);
 void ssh_event_free(ssh_event event);
 const char* ssh_get_clientbanner(ssh_session session);
 const char* ssh_get_serverbanner(ssh_session session);
 const char* ssh_get_kex_algo(ssh_session session);
 const char* ssh_get_cipher_in(ssh_session session);
 const char* ssh_get_cipher_out(ssh_session session);
 const char* ssh_get_hmac_in(ssh_session session);
 const char* ssh_get_hmac_out(ssh_session session);

 ssh_buffer ssh_buffer_new(void);
 void ssh_buffer_free(ssh_buffer buffer);
 int ssh_buffer_reinit(ssh_buffer buffer);
 int ssh_buffer_add_data(ssh_buffer buffer, const void *data, uint32_t len);
 uint32_t ssh_buffer_get_data(ssh_buffer buffer, void *data, uint32_t requestedlen);
 void *ssh_buffer_get(ssh_buffer buffer);
 uint32_t ssh_buffer_get_len(ssh_buffer buffer);



#line 1 "/usr/include/libssh/legacy.h" 1
/*
 * This file is part of the SSH Library
 *
 * Copyright (c) 2010 by Aris Adamantiadis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

/* Since libssh.h includes legacy.h, it's important that libssh.h is included
 * first. we don't define LEGACY_H now because we want it to be defined when
 * included from libssh.h
 * All function calls declared in this header are deprecated and meant to be
 * removed in future.
 */




typedef struct ssh_private_key_struct* ssh_private_key;
typedef struct ssh_public_key_struct* ssh_public_key;

 int ssh_auth_list(ssh_session session);
 int ssh_userauth_offer_pubkey(ssh_session session, const char *username, int type, ssh_string publickey);
 int ssh_userauth_pubkey(ssh_session session, const char *username, ssh_string publickey, ssh_private_key privatekey);


    

 int ssh_userauth_autopubkey(ssh_session session, const char *passphrase);
 int ssh_userauth_privatekey_file(ssh_session session, const char *username,
    const char *filename, const char *passphrase);

  void buffer_free(ssh_buffer buffer);
  void *buffer_get(ssh_buffer buffer);
  uint32_t buffer_get_len(ssh_buffer buffer);
  ssh_buffer buffer_new(void);

  ssh_channel channel_accept_x11(ssh_channel channel, int timeout_ms);
  int channel_change_pty_size(ssh_channel channel,int cols,int rows);
  ssh_channel channel_forward_accept(ssh_session session, int timeout_ms);
  int channel_close(ssh_channel channel);
  int channel_forward_cancel(ssh_session session, const char *address, int port);
  int channel_forward_listen(ssh_session session, const char *address, int port, int *bound_port);
  void channel_free(ssh_channel channel);
  int channel_get_exit_status(ssh_channel channel);
  ssh_session channel_get_session(ssh_channel channel);
  int channel_is_closed(ssh_channel channel);
  int channel_is_eof(ssh_channel channel);
  int channel_is_open(ssh_channel channel);
  ssh_channel channel_new(ssh_session session);
  int channel_open_forward(ssh_channel channel, const char *remotehost,
    int remoteport, const char *sourcehost, int localport);
  int channel_open_session(ssh_channel channel);
  int channel_poll(ssh_channel channel, int is_stderr);
  int channel_read(ssh_channel channel, void *dest, uint32_t count, int is_stderr);

  int channel_read_buffer(ssh_channel channel, ssh_buffer buffer, uint32_t count,
    int is_stderr);

  int channel_read_nonblocking(ssh_channel channel, void *dest, uint32_t count,
    int is_stderr);
  int channel_request_env(ssh_channel channel, const char *name, const char *value);
  int channel_request_exec(ssh_channel channel, const char *cmd);
  int channel_request_pty(ssh_channel channel);
  int channel_request_pty_size(ssh_channel channel, const char *term,
    int cols, int rows);
  int channel_request_shell(ssh_channel channel);
  int channel_request_send_signal(ssh_channel channel, const char *signum);
  int channel_request_sftp(ssh_channel channel);
  int channel_request_subsystem(ssh_channel channel, const char *subsystem);
  int channel_request_x11(ssh_channel channel, int single_connection, const char *protocol,
    const char *cookie, int screen_number);
  int channel_send_eof(ssh_channel channel);
  int channel_select(ssh_channel *readchans, ssh_channel *writechans, ssh_channel *exceptchans, struct
        timeval * timeout);
  void channel_set_blocking(ssh_channel channel, int blocking);
  int channel_write(ssh_channel channel, const void *data, uint32_t len);

 void privatekey_free(ssh_private_key prv);
 ssh_private_key privatekey_from_file(ssh_session session, const char *filename,
    int type, const char *passphrase);
 void publickey_free(ssh_public_key key);
 int ssh_publickey_to_file(ssh_session session, const char *file,
    ssh_string pubkey, int type);
 ssh_string publickey_from_file(ssh_session session, const char *filename,
    int *type);
 ssh_public_key publickey_from_privatekey(ssh_private_key prv);
 ssh_string publickey_to_string(ssh_public_key key);
 int ssh_try_publickey_from_file(ssh_session session, const char *keyfile,
    ssh_string *publickey, int *type);
 enum ssh_keytypes_e ssh_privatekey_type(ssh_private_key privatekey);

 ssh_string ssh_get_pubkey(ssh_session session);

 ssh_message ssh_message_retrieve(ssh_session session, uint32_t packettype);
 ssh_public_key ssh_message_auth_publickey(ssh_message msg);

  void string_burn(ssh_string str);
  ssh_string string_copy(ssh_string str);
  void *string_data(ssh_string str);
  int string_fill(ssh_string str, const void *data, size_t len);
  void string_free(ssh_string str);
  ssh_string string_from_char(const char *what);
  size_t string_len(ssh_string str);
  ssh_string string_new(size_t size);
  char *string_to_char(ssh_string str);



#line 634 "/usr/include/libssh/libssh.h" 2



}


/* vim: set ts=2 sw=2 et cindent: */

#line 1 "/usr/include/libssh/sftp.h" 1
/*
 * This file is part of the SSH Library
 *
 * Copyright (c) 2003-2008 by Aris Adamantiadis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

/**
 * @defgroup libssh_sftp The libssh SFTP API
 *
 * @brief SFTP handling functions
 *
 * SFTP commands are channeled by the ssh sftp subsystem. Every packet is
 * sent/read using a sftp_packet type structure. Related to these packets,
 * most of the server answers are messages having an ID and a message
 * specific part. It is described by sftp_message when reading a message,
 * the sftp system puts it into the queue, so the process having asked for
 * it can fetch it, while continuing to read for other messages (it is
 * unspecified in which order messages may be sent back to the client
 *
 * @{
 */





#line 1 "/usr/include/libssh/sftp.h" 1


#line 1 "/usr/include/libssh/libssh.h" 1
/*
 * This file is part of the SSH Library
 *
 * Copyright (c) 2003-2009 by Aris Adamantiadis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */





  
    
  
    
      
        
      
        
      
    
      
        
      
        
      
    
  

  
    
  
    
  



  
  
  
  
  
  
  

  
  
  



  

 
 








































    
    
    
    































	
	
	
	
	
	
	
	
	
	








	
	
	
	
	
	













	
	
	
	
	



	
	
	
	
	
	



	
	
	
	
	
	
	
	



	
	
	
	



	
	
	
	













	
	
	
	
	
	



    




	
	
	
	




  
  
  
  
  
  
  
  



  
  















	

	
	

	
	

	
	

	
	

	

























  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  



  
  
  
  
  



  
  
  
  
  
  
  
  
  
  



    
    
    
    
    
    















    







    




    





    



        


                                        


                                        
                                        









                                            
                                            

                                             
                                             










                                                  
                                                  

                                          
                                          

                                          
                                          
                                          













    
    


                                      
                                      
                                      
























                         
                         



                                       
                                       














    

    




















    









                           
                           


        

                                             
                                             
                                             
                                             

                                           
                                           
                                           
                                           

                                           
                                           
                                           
                                           


                                            


                                            
                                            

                                          


                                          
                                          

                                        


                                                

                                            

                                          


























    





                                 










                                          
                                          

                                      
                                      


                                  


                                           
                                           

                                     
                                     









    


















    






                                    































/* vim: set ts=2 sw=2 et cindent: */

#line 42 "/usr/include/libssh/sftp.h" 2


extern "C" {




  typedef uint32_t uid_t;


  typedef uint32_t gid_t;



  






typedef struct sftp_attributes_struct* sftp_attributes;
typedef struct sftp_client_message_struct* sftp_client_message;
typedef struct sftp_dir_struct* sftp_dir;
typedef struct sftp_ext_struct *sftp_ext;
typedef struct sftp_file_struct* sftp_file;
typedef struct sftp_message_struct* sftp_message;
typedef struct sftp_packet_struct* sftp_packet;
typedef struct sftp_request_queue_struct* sftp_request_queue;
typedef struct sftp_session_struct* sftp_session;
typedef struct sftp_status_message_struct* sftp_status_message;
typedef struct sftp_statvfs_struct* sftp_statvfs_t;

struct sftp_session_struct {
    ssh_session session;
    ssh_channel channel;
    int server_version;
    int client_version;
    int version;
    sftp_request_queue queue;
    uint32_t id_counter;
    int errnum;
    void **handles;
    sftp_ext ext;
};

struct sftp_packet_struct {
    sftp_session sftp;
    uint8_t type;
    ssh_buffer payload;
};

/* file handler */
struct sftp_file_struct {
    sftp_session sftp;
    char *name;
    uint64_t offset;
    ssh_string handle;
    int eof;
    int nonblocking;
};

struct sftp_dir_struct {
    sftp_session sftp;
    char *name;
    ssh_string handle; /* handle to directory */
    ssh_buffer buffer; /* contains raw attributes from server which haven't been parsed */
    uint32_t count; /* counts the number of following attributes structures into buffer */
    int eof; /* end of directory listing */
};

struct sftp_message_struct {
    sftp_session sftp;
    uint8_t packet_type;
    ssh_buffer payload;
    uint32_t id;
};

/* this is a bunch of all data that could be into a message */
struct sftp_client_message_struct {
    sftp_session sftp;
    uint8_t type;
    uint32_t id;
    char *filename; /* can be "path" */
    uint32_t flags;
    sftp_attributes attr;
    ssh_string handle;
    uint64_t offset;
    uint32_t len;
    int attr_num;
    ssh_buffer attrbuf; /* used by sftp_reply_attrs */
    ssh_string data; /* can be newpath of rename() */
    ssh_buffer complete_message; /* complete message in case of retransmission*/
    char *str_data; /* cstring version of data */
};

struct sftp_request_queue_struct {
    sftp_request_queue next;
    sftp_message message;
};

/* SSH_FXP_MESSAGE described into .7 page 26 */
struct sftp_status_message_struct {
    uint32_t id;
	uint32_t status;
    ssh_string error_unused; /* not used anymore */
    ssh_string lang_unused;  /* not used anymore */
    char *errormsg;
    char *langmsg;
};

struct sftp_attributes_struct {
    char *name;
    char *longname; /* ls -l output on openssh, not reliable else */
    uint32_t flags;
    uint8_t type;
    uint64_t size;
    uint32_t uid;
    uint32_t gid;
    char *owner; /* set if openssh and version 4 */
    char *group; /* set if openssh and version 4 */
    uint32_t permissions;
    uint64_t atime64;
    uint32_t atime;
    uint32_t atime_nseconds;
    uint64_t createtime;
    uint32_t createtime_nseconds;
    uint64_t mtime64;
    uint32_t mtime;
    uint32_t mtime_nseconds;
    ssh_string acl;
    uint32_t extended_count;
    ssh_string extended_type;
    ssh_string extended_data;
};

/**
 * @brief SFTP statvfs structure.
 */
struct sftp_statvfs_struct {
  uint64_t f_bsize;   /** file system block size */
  uint64_t f_frsize;  /** fundamental fs block size */
  uint64_t f_blocks;  /** number of blocks (unit f_frsize) */
  uint64_t f_bfree;   /** free blocks in file system */
  uint64_t f_bavail;  /** free blocks for non-root */
  uint64_t f_files;   /** total file inodes */
  uint64_t f_ffree;   /** free file inodes */
  uint64_t f_favail;  /** free file inodes for to non-root */
  uint64_t f_fsid;    /** file system id */
  uint64_t f_flag;    /** bit mask of f_flag values */
  uint64_t f_namemax; /** maximum filename length */
};

/**
 * @brief Start a new sftp session.
 *
 * @param session       The ssh session to use.
 *
 * @return              A new sftp session or NULL on error.
 *
 * @see sftp_free()
 */
 sftp_session sftp_new(ssh_session session);

/**
 * @brief Start a new sftp session with an existing channel.
 *
 * @param session       The ssh session to use.
 * @param channel		An open session channel with subsystem already allocated
 *
 * @return              A new sftp session or NULL on error.
 *
 * @see sftp_free()
 */
 sftp_session sftp_new_channel(ssh_session session, ssh_channel channel);


/**
 * @brief Close and deallocate a sftp session.
 *
 * @param sftp          The sftp session handle to free.
 */
 void sftp_free(sftp_session sftp);

/**
 * @brief Initialize the sftp session with the server.
 *
 * @param sftp          The sftp session to initialize.
 *
 * @return              0 on success, < 0 on error with ssh error set.
 *
 * @see sftp_new()
 */
 int sftp_init(sftp_session sftp);

/**
 * @brief Get the last sftp error.
 *
 * Use this function to get the latest error set by a posix like sftp function.
 *
 * @param sftp          The sftp session where the error is saved.
 *
 * @return              The saved error (see server responses), < 0 if an error
 *                      in the function occured.
 *
 * @see Server responses
 */
 int sftp_get_error(sftp_session sftp);

/**
 * @brief Get the count of extensions provided by the server.
 *
 * @param  sftp         The sftp session to use.
 *
 * @return The count of extensions provided by the server, 0 on error or
 *         not available.
 */
 unsigned int sftp_extensions_get_count(sftp_session sftp);

/**
 * @brief Get the name of the extension provided by the server.
 *
 * @param  sftp         The sftp session to use.
 *
 * @param  indexn        The index number of the extension name you want.
 *
 * @return              The name of the extension.
 */
 const char *sftp_extensions_get_name(sftp_session sftp, unsigned int indexn);

/**
 * @brief Get the data of the extension provided by the server.
 *
 * This is normally the version number of the extension.
 *
 * @param  sftp         The sftp session to use.
 *
 * @param  indexn        The index number of the extension data you want.
 *
 * @return              The data of the extension.
 */
 const char *sftp_extensions_get_data(sftp_session sftp, unsigned int indexn);

/**
 * @brief Check if the given extension is supported.
 *
 * @param  sftp         The sftp session to use.
 *
 * @param  name         The name of the extension.
 *
 * @param  data         The data of the extension.
 *
 * @return 1 if supported, 0 if not.
 *
 * Example:
 *
 * @code
 * sftp_extension_supported(sftp, "statvfs@openssh.com", "2");
 * @endcode
 */
 int sftp_extension_supported(sftp_session sftp, const char *name,
    const char *data);

/**
 * @brief Open a directory used to obtain directory entries.
 *
 * @param session       The sftp session handle to open the directory.
 * @param path          The path of the directory to open.
 *
 * @return              A sftp directory handle or NULL on error with ssh and
 *                      sftp error set.
 *
 * @see                 sftp_readdir
 * @see                 sftp_closedir
 */
 sftp_dir sftp_opendir(sftp_session session, const char *path);

/**
 * @brief Get a single file attributes structure of a directory.
 *
 * @param session      The sftp session handle to read the directory entry.
 * @param dir          The opened sftp directory handle to read from.
 *
 * @return             A file attribute structure or NULL at the end of the
 *                     directory.
 *
 * @see                sftp_opendir()
 * @see                sftp_attribute_free()
 * @see                sftp_closedir()
 */
 sftp_attributes sftp_readdir(sftp_session session, sftp_dir dir);

/**
 * @brief Tell if the directory has reached EOF (End Of File).
 *
 * @param dir           The sftp directory handle.
 *
 * @return              1 if the directory is EOF, 0 if not.
 *
 * @see                 sftp_readdir()
 */
 int sftp_dir_eof(sftp_dir dir);

/**
 * @brief Get information about a file or directory.
 *
 * @param session       The sftp session handle.
 * @param path          The path to the file or directory to obtain the
 *                      information.
 *
 * @return              The sftp attributes structure of the file or directory,
 *                      NULL on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 sftp_attributes sftp_stat(sftp_session session, const char *path);

/**
 * @brief Get information about a file or directory.
 *
 * Identical to sftp_stat, but if the file or directory is a symbolic link,
 * then the link itself is stated, not the file that it refers to.
 *
 * @param session       The sftp session handle.
 * @param path          The path to the file or directory to obtain the
 *                      information.
 *
 * @return              The sftp attributes structure of the file or directory,
 *                      NULL on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 sftp_attributes sftp_lstat(sftp_session session, const char *path);

/**
 * @brief Get information about a file or directory from a file handle.
 *
 * @param file          The sftp file handle to get the stat information.
 *
 * @return              The sftp attributes structure of the file or directory,
 *                      NULL on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 sftp_attributes sftp_fstat(sftp_file file);

/**
 * @brief Free a sftp attribute structure.
 *
 * @param file          The sftp attribute structure to free.
 */
 void sftp_attributes_free(sftp_attributes file);

/**
 * @brief Close a directory handle opened by sftp_opendir().
 *
 * @param dir           The sftp directory handle to close.
 *
 * @return              Returns SSH_NO_ERROR or SSH_ERROR if an error occured.
 */
 int sftp_closedir(sftp_dir dir);

/**
 * @brief Close an open file handle.
 *
 * @param file          The open sftp file handle to close.
 *
 * @return              Returns SSH_NO_ERROR or SSH_ERROR if an error occured.
 *
 * @see                 sftp_open()
 */
 int sftp_close(sftp_file file);

/**
 * @brief Open a file on the server.
 *
 * @param session       The sftp session handle.
 *
 * @param file          The file to be opened.
 *
 * @param accesstype    Is one of O_RDONLY, O_WRONLY or O_RDWR which request
 *                      opening  the  file  read-only,write-only or read/write.
 *                      Acesss may also be bitwise-or'd with one or  more of
 *                      the following:
 *                      O_CREAT - If the file does not exist it will be
 *                      created.
 *                      O_EXCL - When  used with O_CREAT, if the file already
 *                      exists it is an error and the open will fail.
 *                      O_TRUNC - If the file already exists it will be
 *                      truncated.
 *
 * @param mode          Mode specifies the permissions to use if a new file is
 *                      created.  It  is  modified  by  the process's umask in
 *                      the usual way: The permissions of the created file are
 *                      (mode & ~umask)
 *
 * @return              A sftp file handle, NULL on error with ssh and sftp
 *                      error set.
 *
 * @see sftp_get_error()
 */
 sftp_file sftp_open(sftp_session session, const char *file, int accesstype,
    mode_t mode);

/**
 * @brief Make the sftp communication for this file handle non blocking.
 *
 * @param[in]  handle   The file handle to set non blocking.
 */
 void sftp_file_set_nonblocking(sftp_file handle);

/**
 * @brief Make the sftp communication for this file handle blocking.
 *
 * @param[in]  handle   The file handle to set blocking.
 */
 void sftp_file_set_blocking(sftp_file handle);

/**
 * @brief Read from a file using an opened sftp file handle.
 *
 * @param file          The opened sftp file handle to be read from.
 *
 * @param buf           Pointer to buffer to recieve read data.
 *
 * @param count         Size of the buffer in bytes.
 *
 * @return              Number of bytes written, < 0 on error with ssh and sftp
 *                      error set.
 *
 * @see sftp_get_error()
 */
 ssize_t sftp_read(sftp_file file, void *buf, size_t count);

/**
 * @brief Start an asynchronous read from a file using an opened sftp file handle.
 *
 * Its goal is to avoid the slowdowns related to the request/response pattern
 * of a synchronous read. To do so, you must call 2 functions:
 *
 * sftp_async_read_begin() and sftp_async_read().
 *
 * The first step is to call sftp_async_read_begin(). This function returns a
 * request identifier. The second step is to call sftp_async_read() using the
 * returned identifier.
 *
 * @param file          The opened sftp file handle to be read from.
 *
 * @param len           Size to read in bytes.
 *
 * @return              An identifier corresponding to the sent request, < 0 on
 *                      error.
 *
 * @warning             When calling this function, the internal offset is
 *                      updated corresponding to the len parameter.
 *
 * @warning             A call to sftp_async_read_begin() sends a request to
 *                      the server. When the server answers, libssh allocates
 *                      memory to store it until sftp_async_read() is called.
 *                      Not calling sftp_async_read() will lead to memory
 *                      leaks.
 *
 * @see                 sftp_async_read()
 * @see                 sftp_open()
 */
 int sftp_async_read_begin(sftp_file file, uint32_t len);

/**
 * @brief Wait for an asynchronous read to complete and save the data.
 *
 * @param file          The opened sftp file handle to be read from.
 *
 * @param data          Pointer to buffer to recieve read data.
 *
 * @param len           Size of the buffer in bytes. It should be bigger or
 *                      equal to the length parameter of the
 *                      sftp_async_read_begin() call.
 *
 * @param id            The identifier returned by the sftp_async_read_begin()
 *                      function.
 *
 * @return              Number of bytes read, 0 on EOF, SSH_ERROR if an error
 *                      occured, SSH_AGAIN if the file is opened in nonblocking
 *                      mode and the request hasn't been executed yet.
 *
 * @warning             A call to this function with an invalid identifier
 *                      will never return.
 *
 * @see sftp_async_read_begin()
 */
 int sftp_async_read(sftp_file file, void *data, uint32_t len, uint32_t id);

/**
 * @brief Write to a file using an opened sftp file handle.
 *
 * @param file          Open sftp file handle to write to.
 *
 * @param buf           Pointer to buffer to write data.
 *
 * @param count         Size of buffer in bytes.
 *
 * @return              Number of bytes written, < 0 on error with ssh and sftp
 *                      error set.
 *
 * @see                 sftp_open()
 * @see                 sftp_read()
 * @see                 sftp_close()
 */
 ssize_t sftp_write(sftp_file file, const void *buf, size_t count);

/**
 * @brief Seek to a specific location in a file.
 *
 * @param file         Open sftp file handle to seek in.
 *
 * @param new_offset   Offset in bytes to seek.
 *
 * @return             0 on success, < 0 on error.
 */
 int sftp_seek(sftp_file file, uint32_t new_offset);

/**
 * @brief Seek to a specific location in a file. This is the
 * 64bit version.
 *
 * @param file         Open sftp file handle to seek in.
 *
 * @param new_offset   Offset in bytes to seek.
 *
 * @return             0 on success, < 0 on error.
 */
 int sftp_seek64(sftp_file file, uint64_t new_offset);

/**
 * @brief Report current byte position in file.
 *
 * @param file          Open sftp file handle.
 *
 * @return              The offset of the current byte relative to the beginning
 *                      of the file associated with the file descriptor. < 0 on
 *                      error.
 */
 unsigned long sftp_tell(sftp_file file);

/**
 * @brief Report current byte position in file.
 *
 * @param file          Open sftp file handle.
 *
 * @return              The offset of the current byte relative to the beginning
 *                      of the file associated with the file descriptor. < 0 on
 *                      error.
 */
 uint64_t sftp_tell64(sftp_file file);

/**
 * @brief Rewinds the position of the file pointer to the beginning of the
 * file.
 *
 * @param file          Open sftp file handle.
 */
 void sftp_rewind(sftp_file file);

/**
 * @brief Unlink (delete) a file.
 *
 * @param sftp          The sftp session handle.
 *
 * @param file          The file to unlink/delete.
 *
 * @return              0 on success, < 0 on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 int sftp_unlink(sftp_session sftp, const char *file);

/**
 * @brief Remove a directoy.
 *
 * @param sftp          The sftp session handle.
 *
 * @param directory     The directory to remove.
 *
 * @return              0 on success, < 0 on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 int sftp_rmdir(sftp_session sftp, const char *directory);

/**
 * @brief Create a directory.
 *
 * @param sftp          The sftp session handle.
 *
 * @param directory     The directory to create.
 *
 * @param mode          Specifies the permissions to use. It is modified by the
 *                      process's umask in the usual way:
 *                      The permissions of the created file are (mode & ~umask)
 *
 * @return              0 on success, < 0 on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 int sftp_mkdir(sftp_session sftp, const char *directory, mode_t mode);

/**
 * @brief Rename or move a file or directory.
 *
 * @param sftp          The sftp session handle.
 *
 * @param original      The original url (source url) of file or directory to
 *                      be moved.
 *
 * @param newname       The new url (destination url) of the file or directory
 *                      after the move.
 *
 * @return              0 on success, < 0 on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 int sftp_rename(sftp_session sftp, const char *original, const  char *newname);

/**
 * @brief Set file attributes on a file, directory or symbolic link.
 *
 * @param sftp          The sftp session handle.
 *
 * @param file          The file which attributes should be changed.
 *
 * @param attr          The file attributes structure with the attributes set
 *                      which should be changed.
 *
 * @return              0 on success, < 0 on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 int sftp_setstat(sftp_session sftp, const char *file, sftp_attributes attr);

/**
 * @brief Change the file owner and group
 *
 * @param sftp          The sftp session handle.
 *
 * @param file          The file which owner and group should be changed.
 *
 * @param owner         The new owner which should be set.
 *
 * @param group         The new group which should be set.
 *
 * @return              0 on success, < 0 on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 int sftp_chown(sftp_session sftp, const char *file, uid_t owner, gid_t group);

/**
 * @brief Change permissions of a file
 *
 * @param sftp          The sftp session handle.
 *
 * @param file          The file which owner and group should be changed.
 *
 * @param mode          Specifies the permissions to use. It is modified by the
 *                      process's umask in the usual way:
 *                      The permissions of the created file are (mode & ~umask)
 *
 * @return              0 on success, < 0 on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 int sftp_chmod(sftp_session sftp, const char *file, mode_t mode);

/**
 * @brief Change the last modification and access time of a file.
 *
 * @param sftp          The sftp session handle.
 *
 * @param file          The file which owner and group should be changed.
 *
 * @param times         A timeval structure which contains the desired access
 *                      and modification time.
 *
 * @return              0 on success, < 0 on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 int sftp_utimes(sftp_session sftp, const char *file, const struct timeval *times);

/**
 * @brief Create a symbolic link.
 *
 * @param  sftp         The sftp session handle.
 *
 * @param  target       Specifies the target of the symlink.
 *
 * @param  dest         Specifies the path name of the symlink to be created.
 *
 * @return              0 on success, < 0 on error with ssh and sftp error set.
 *
 * @see sftp_get_error()
 */
 int sftp_symlink(sftp_session sftp, const char *target, const char *dest);

/**
 * @brief Read the value of a symbolic link.
 *
 * @param  sftp         The sftp session handle.
 *
 * @param  path         Specifies the path name of the symlink to be read.
 *
 * @return              The target of the link, NULL on error.
 *
 * @see sftp_get_error()
 */
 char *sftp_readlink(sftp_session sftp, const char *path);

/**
 * @brief Get information about a mounted file system.
 *
 * @param  sftp         The sftp session handle.
 *
 * @param  path         The pathname of any file within the mounted file system.
 *
 * @return A statvfs structure or NULL on error.
 *
 * @see sftp_get_error()
 */
 sftp_statvfs_t sftp_statvfs(sftp_session sftp, const char *path);

/**
 * @brief Get information about a mounted file system.
 *
 * @param  file         An opened file.
 *
 * @return A statvfs structure or NULL on error.
 *
 * @see sftp_get_error()
 */
 sftp_statvfs_t sftp_fstatvfs(sftp_file file);

/**
 * @brief Free the memory of an allocated statvfs.
 *
 * @param  statvfs_o      The statvfs to free.
 */
 void sftp_statvfs_free(sftp_statvfs_t statvfs_o);

/**
 * @brief Synchronize a file's in-core state with storage device
 *
 * This calls the "fsync@openssh.com" extention. You should check if the
 * extensions is supported using:
 *
 * @code
 * int supported = sftp_extension_supported(sftp, "fsync@openssh.com", "1");
 * @endcode
 *
 * @param file          The opened sftp file handle to sync
 *
 * @return              0 on success, < 0 on error with ssh and sftp error set.
 */
 int sftp_fsync(sftp_file file);

/**
 * @brief Canonicalize a sftp path.
 *
 * @param sftp          The sftp session handle.
 *
 * @param path          The path to be canonicalized.
 *
 * @return              The canonicalize path, NULL on error.
 */
 char *sftp_canonicalize_path(sftp_session sftp, const char *path);

/**
 * @brief Get the version of the SFTP protocol supported by the server
 *
 * @param sftp          The sftp session handle.
 *
 * @return              The server version.
 */
 int sftp_server_version(sftp_session sftp);























/* this is not a public interface */

sftp_packet sftp_packet_read(sftp_session sftp);
int sftp_packet_write(sftp_session sftp,uint8_t type, ssh_buffer payload);
void sftp_packet_free(sftp_packet packet);
int buffer_add_attributes(ssh_buffer buffer, sftp_attributes attr);
sftp_attributes sftp_parse_attr(sftp_session session, ssh_buffer buf,int expectname);
/* sftpserver.c */

 sftp_client_message sftp_get_client_message(sftp_session sftp);
 void sftp_client_message_free(sftp_client_message msg);
 uint8_t sftp_client_message_get_type(sftp_client_message msg);
 const char *sftp_client_message_get_filename(sftp_client_message msg);
 void sftp_client_message_set_filename(sftp_client_message msg, const char *newname);
 const char *sftp_client_message_get_data(sftp_client_message msg);
 uint32_t sftp_client_message_get_flags(sftp_client_message msg);
 int sftp_send_client_message(sftp_session sftp, sftp_client_message msg);
int sftp_reply_name(sftp_client_message msg, const char *name,
    sftp_attributes attr);
int sftp_reply_handle(sftp_client_message msg, ssh_string handle);
ssh_string sftp_handle_alloc(sftp_session sftp, void *info);
int sftp_reply_attr(sftp_client_message msg, sftp_attributes attr);
void *sftp_handle(sftp_session sftp, ssh_string handle);
int sftp_reply_status(sftp_client_message msg, uint32_t status, const char *message);
int sftp_reply_names_add(sftp_client_message msg, const char *file,
    const char *longname, sftp_attributes attr);
int sftp_reply_names(sftp_client_message msg);
int sftp_reply_data(sftp_client_message msg, const void *data, int len);
void sftp_handle_remove(sftp_session sftp, void *handle);

/* SFTP commands and constants */






























/* attributes */
/* sftp draft is completely braindead : version 3 and 4 have different flags for same constants */
/* and even worst, version 4 has same flag for 2 different constants */
/* follow up : i won't develop any sftp4 compliant library before having a clarification */













/* types */






/**
 * @name Server responses
 *
 * @brief Responses returned by the sftp server.
 * @{
 */

/** No error */

/** End-of-file encountered */

/** File doesn't exist */

/** Permission denied */

/** Generic failure */

/** Garbage received from server */

/** No connection has been set up */

/** There was a connection, but we lost it */

/** Operation not supported by the server */

/** Invalid file handle */

/** No such file or directory path exists */

/** An attempt to create an already existing file or directory has been made */

/** We are trying to write on a write-protected filesystem */

/** No media in remote drive */


/** @} */

/* file flags */








/* file type flags */









/* rename flags */























/* openssh flags */




} ;




/** @} */
/* vim: set ts=2 sw=2 et cindent: */

#line 1 "/usr/include/libssh/callbacks.h" 1
/*
 * This file is part of the SSH Library
 *
 * Copyright (c) 2009 Aris Adamantiadis <aris@0xbadc0de.be>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

/* callback.h
 * This file includes the public declarations for the libssh callback mechanism
 */





#line 1 "/usr/include/libssh/callbacks.h" 1

#line 1 "/usr/include/string.h" 1
/* Copyright (C) 1991-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */

/*
 *	ISO C99 Standard: 7.21 String handling	<string.h>
 */






#line 1 "/usr/include/string.h" 1

extern "C" {

/* Get size_t and NULL from <stddef.h>.  */



#line 1 "/usr/include/string.h" 1

/* Tell the caller that we provide correct C++ prototypes.  */





/* Copy N bytes of SRC to DEST.  */
extern void *memcpy (void * __dest, const void * __src,
		     size_t __n)  ;
/* Copy N bytes of SRC to DEST, guaranteeing
   correct behavior for overlapping strings.  */
extern void *memmove (void *__dest, const void *__src, size_t __n)
      ;

/* Copy no more than N bytes of SRC to DEST, stopping when C is found.
   Return the position in DEST one byte past where C was copied,
   or NULL if C was not found in the first N bytes of SRC.  */

extern void *memccpy (void * __dest, const void * __src,
		      int __c, size_t __n)
      ;



/* Set N bytes of S to C.  */
extern void *memset (void *__s, int __c, size_t __n)  ;

/* Compare N bytes of S1 and S2.  */
extern int memcmp (const void *__s1, const void *__s2, size_t __n)
       ;

/* Search N bytes of S for C.  */




      

      





  





  




extern void *memchr (const void *__s, int __c, size_t __n)
        ;







     

     


     





      

      


      




/* Copy SRC to DEST.  */
extern char *strcpy (char * __dest, const char * __src)
      ;
/* Copy no more than N characters of SRC to DEST.  */
extern char *strncpy (char * __dest,
		      const char * __src, size_t __n)
      ;

/* Append SRC onto DEST.  */
extern char *strcat (char * __dest, const char * __src)
      ;
/* Append no more than N characters from SRC onto DEST.  */
extern char *strncat (char * __dest, const char * __src,
		      size_t __n)  ;

/* Compare S1 and S2.  */
extern int strcmp (const char *__s1, const char *__s2)
       ;
/* Compare N characters of S1 and S2.  */
extern int strncmp (const char *__s1, const char *__s2, size_t __n)
       ;

/* Compare the collated forms of S1 and S2.  */
extern int strcoll (const char *__s1, const char *__s2)
       ;
/* Put a transformation of SRC into no more than N bytes of DEST.  */
extern size_t strxfrm (char * __dest,
		       const char * __src, size_t __n)
      ;


/* POSIX.1-2008 extended locale interface (see locale.h).  */

#line 1 "/usr/include/bits/types/locale_t.h" 1
/* Definition of locale_t.
   Copyright (C) 2017-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */





#line 1 "/usr/include/bits/types/__locale_t.h" 1
/* Definition of struct __locale_struct and __locale_t.
   Copyright (C) 1997-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.
   Contributed by Ulrich Drepper <drepper@cygnus.com>, 1997.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */




/* POSIX.1-2008: the locale_t type, representing a locale context
   (implementation-namespace version).  This type should be treated
   as opaque by applications; some details are exposed for the sake of
   efficiency in e.g. ctype functions.  */

struct __locale_struct
{
  /* Note: LC_ALL is not a valid index into this array.  */
  struct __locale_data *__locales[13]; /* 13 = __LC_LAST. */

  /* To increase the speed of this solution we add some special members.  */
  const unsigned short int *__ctype_b;
  const int *__ctype_tolower;
  const int *__ctype_toupper;

  /* Note: LC_ALL is not a valid index into this array.  */
  const char *__names[13];
};

typedef struct __locale_struct *__locale_t;



#line 22 "/usr/include/bits/types/locale_t.h" 2

typedef __locale_t locale_t;



#line 127 "/usr/include/string.h" 2

/* Compare the collated forms of S1 and S2, using sorting rules from L.  */
extern int strcoll_l (const char *__s1, const char *__s2, locale_t __l)
       ;
/* Put a transformation of SRC into no more than N bytes of DEST,
   using sorting rules from L.  */
extern size_t strxfrm_l (char *__dest, const char *__src, size_t __n,
			 locale_t __l)  ;




/* Duplicate S, returning an identical malloc'd string.  */
extern char *strdup (const char *__s)
       ;


/* Return a malloc'd copy of at most N bytes of STRING.  The
   resultant string is terminated even if no null terminator
   appears before STRING[N].  */

extern char *strndup (const char *__string, size_t __n)
       ;

























/* Find the first occurrence of C in S.  */




     

     





  





  




extern char *strchr (const char *__s, int __c)
       ;

/* Find the last occurrence of C in S.  */




     

     





  





  




extern char *strrchr (const char *__s, int __c)
       ;







     

     


     



/* Return the length of the initial segment of S which
   consists entirely of characters not in REJECT.  */
extern size_t strcspn (const char *__s, const char *__reject)
       ;
/* Return the length of the initial segment of S which
   consists entirely of characters in ACCEPT.  */
extern size_t strspn (const char *__s, const char *__accept)
       ;
/* Find the first occurrence in S of any character in ACCEPT.  */




     

     





  





  




extern char *strpbrk (const char *__s, const char *__accept)
       ;

/* Find the first occurrence of NEEDLE in HAYSTACK.  */




     

     





  





  




extern char *strstr (const char *__haystack, const char *__needle)
       ;



/* Divide S into tokens separated by characters in DELIM.  */
extern char *strtok (char * __s, const char * __delim)
      ;

/* Divide S into tokens separated by characters in DELIM.  Information
   passed between calls are stored in SAVE_PTR.  */
extern char *__strtok_r (char * __s,
			 const char * __delim,
			 char ** __save_ptr)
      ;

extern char *strtok_r (char * __s, const char * __delim,
		       char ** __save_ptr)
      ;






     

				     
     


     








		     
     




			
     

		      
     



/* Return the length of S.  */
extern size_t strlen (const char *__s)
       ;


/* Find the length of STRING, but scan at most MAXLEN characters.
   If no '\0' terminator is found in that many characters, return MAXLEN.  */
extern size_t strnlen (const char *__string, size_t __maxlen)
       ;



/* Return a string describing the meaning of the `errno' code in ERRNUM.  */
extern char *strerror (int __errnum) ;

/* Reentrant version of `strerror'.
   There are 2 flavors of `strerror_r', GNU which returns the string
   and may or may not use the supplied temporary buffer and POSIX one
   which fills the string into the buffer.
   To use the POSIX version, -D_XOPEN_SOURCE=600 or -D_POSIX_C_SOURCE=200112L
   without -D_GNU_SOURCE is needed, otherwise the GNU version is
   preferred.  */

/* Fill BUF with a string describing the meaning of the `errno' code in
   ERRNUM.  */


			   
			   

extern int __xpg_strerror_r (int __errnum, char *__buf, size_t __buflen)
      ;






     




/* Translate error number to string according to the locale L.  */
extern char *strerror_l (int __errnum, locale_t __l) ;




#line 1 "/usr/include/strings.h" 1
/* Copyright (C) 1991-2018 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */





#line 1 "/usr/include/strings.h" 1


#line 1 "/usr/include/strings.h" 1

/* Tell the caller that we provide correct C++ prototypes.  */




extern "C" {


/* Compare N bytes of S1 and S2 (same as memcmp).  */
extern int bcmp (const void *__s1, const void *__s2, size_t __n)
       ;

/* Copy N bytes of SRC to DEST (like memmove, but args reversed).  */
extern void bcopy (const void *__src, void *__dest, size_t __n)
   ;

/* Set N bytes of S to 0.  */
extern void bzero (void *__s, size_t __n)  ;

/* Find the first occurrence of C in S (same as strchr).  */




     

     





  





  




extern char *index (const char *__s, int __c)
       ;


/* Find the last occurrence of C in S (same as strrchr).  */




     

     





  





  




extern char *rindex (const char *__s, int __c)
       ;




/* Return the position of the first bit set in I, or 0 if none are set.
   The least-significant bit is position 1, the most-significant 32.  */
extern int ffs (int __i)  ;


/* The following two functions are non-standard but necessary for non-32 bit
   platforms.  */

extern int ffsl (long int __l)  ;
 extern int ffsll (long long int __ll)
      ;


/* Compare S1 and S2, ignoring case.  */
extern int strcasecmp (const char *__s1, const char *__s2)
       ;

/* Compare no more than N chars of S1 and S2, ignoring case.  */
extern int strncasecmp (const char *__s1, const char *__s2, size_t __n)
       ;


/* POSIX.1-2008 extended locale interface (see locale.h).  */

#line 1 "/usr/include/strings.h" 1

/* Compare S1 and S2, ignoring case, using collation rules from LOC.  */
extern int strcasecmp_l (const char *__s1, const char *__s2, locale_t __loc)
       ;

/* Compare no more than N chars of S1 and S2, ignoring case, using
   collation rules from LOC.  */
extern int strncasecmp_l (const char *__s1, const char *__s2,
			  size_t __n, locale_t __loc)
       ;


}











#line 337 "/usr/include/string.h" 2

/* Set N bytes of S to 0.  The compiler will not delete a call to this
   function, even if S is dead after the call.  */
extern void explicit_bzero (void *__s, size_t __n)  ;

/* Return the next DELIM-delimited token from *STRINGP,
   terminating it with a '\0', and update *STRINGP to point past it.  */
extern char *strsep (char ** __stringp,
		     const char * __delim)
      ;



/* Return a string describing the meaning of the signal number in SIG.  */
extern char *strsignal (int __sig) ;

/* Copy SRC to DEST, returning the address of the terminating '\0' in DEST.  */
extern char *__stpcpy (char * __dest, const char * __src)
      ;
extern char *stpcpy (char * __dest, const char * __src)
      ;

/* Copy no more than N characters of SRC to DEST, returning the address of
   the last character written into DEST.  */
extern char *__stpncpy (char * __dest,
			const char * __src, size_t __n)
      ;
extern char *stpncpy (char * __dest,
		      const char * __src, size_t __n)
      ;





     














     

     













}



#line 29 "/usr/include/libssh/callbacks.h" 2


extern "C" {


/**
 * @defgroup libssh_callbacks The libssh callbacks
 * @ingroup libssh
 *
 * Callback which can be replaced in libssh.
 *
 * @{
 */

/** @internal
 * @brief callback to process simple codes
 * @param code value to transmit
 * @param user Userdata to pass in callback
 */
typedef void (*ssh_callback_int) (int code, void *user);

/** @internal
 * @brief callback for data received messages.
 * @param data data retrieved from the socket or stream
 * @param len number of bytes available from this stream
 * @param user user-supplied pointer sent along with all callback messages
 * @returns number of bytes processed by the callee. The remaining bytes will
 * be sent in the next callback message, when more data is available.
 */
typedef int (*ssh_callback_data) (const void *data, size_t len, void *user);

typedef void (*ssh_callback_int_int) (int code, int errno_code, void *user);

typedef int (*ssh_message_callback) (ssh_session, ssh_message message, void *user);
typedef int (*ssh_channel_callback_int) (ssh_channel channel, int code, void *user);
typedef int (*ssh_channel_callback_data) (ssh_channel channel, int code, void *data, size_t len, void *user);

/**
 * @brief SSH log callback. All logging messages will go through this callback
 * @param session Current session handler
 * @param priority Priority of the log, the smaller being the more important
 * @param message the actual message
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_log_callback) (ssh_session session, int priority,
    const char *message, void *userdata);

/**
 * @brief SSH log callback.
 *
 * All logging messages will go through this callback.
 *
 * @param priority  Priority of the log, the smaller being the more important.
 *
 * @param function  The function name calling the the logging fucntions.
 *
 * @param message   The actual message
 *
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_logging_callback) (int priority,
                                      const char *function,
                                      const char *buffer,
                                      void *userdata);

/**
 * @brief SSH Connection status callback.
 * @param session Current session handler
 * @param status Percentage of connection status, going from 0.0 to 1.0
 * once connection is done.
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_status_callback) (ssh_session session, float status,
		void *userdata);

/**
 * @brief SSH global request callback. All global request will go through this
 * callback.
 * @param session Current session handler
 * @param message the actual message
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_global_request_callback) (ssh_session session,
                                        ssh_message message, void *userdata);

/**
 * @brief Handles an SSH new channel open X11 request. This happens when the server
 * sends back an X11 connection attempt. This is a client-side API
 * @param session current session handler
 * @param userdata Userdata to be passed to the callback function.
 * @returns a valid ssh_channel handle if the request is to be allowed
 * @returns NULL if the request should not be allowed
 * @warning The channel pointer returned by this callback must be closed by the application.
 */
typedef ssh_channel (*ssh_channel_open_request_x11_callback) (ssh_session session,
      const char * originator_address, int originator_port, void *userdata);

/**
 * @brief Handles an SSH new channel open "auth-agent" request. This happens when the server
 * sends back an "auth-agent" connection attempt. This is a client-side API
 * @param session current session handler
 * @param userdata Userdata to be passed to the callback function.
 * @returns a valid ssh_channel handle if the request is to be allowed
 * @returns NULL if the request should not be allowed
 * @warning The channel pointer returned by this callback must be closed by the application.
 */
typedef ssh_channel (*ssh_channel_open_request_auth_agent_callback) (ssh_session session,
      void *userdata);

/**
 * The structure to replace libssh functions with appropriate callbacks.
 */
struct ssh_callbacks_struct {
  /** DON'T SET THIS use ssh_callbacks_init() instead. */
  size_t size;
  /**
   * User-provided data. User is free to set anything he wants here
   */
  void *userdata;
  /**
   * This functions will be called if e.g. a keyphrase is needed.
   */
  ssh_auth_callback auth_function;
  /**
   * This function will be called each time a loggable event happens.
   */
  ssh_log_callback log_function;
  /**
   * This function gets called during connection time to indicate the
   * percentage of connection steps completed.
   */
  void (*connect_status_function)(void *userdata, float status);
  /**
   * This function will be called each time a global request is received.
   */
  ssh_global_request_callback global_request_function;
  /** This function will be called when an incoming X11 request is received.
   */
  ssh_channel_open_request_x11_callback channel_open_request_x11_function;
  /** This function will be called when an incoming "auth-agent" request is received.
   */
  ssh_channel_open_request_auth_agent_callback channel_open_request_auth_agent_function;
};
typedef struct ssh_callbacks_struct *ssh_callbacks;

/** These are callbacks used specifically in SSH servers.
 */

/**
 * @brief SSH authentication callback.
 * @param session Current session handler
 * @param user User that wants to authenticate
 * @param password Password used for authentication
 * @param userdata Userdata to be passed to the callback function.
 * @returns SSH_AUTH_SUCCESS Authentication is accepted.
 * @returns SSH_AUTH_PARTIAL Partial authentication, more authentication means are needed.
 * @returns SSH_AUTH_DENIED Authentication failed.
 */
typedef int (*ssh_auth_password_callback) (ssh_session session, const char *user, const char *password,
		void *userdata);

/**
 * @brief SSH authentication callback. Tries to authenticates user with the "none" method
 * which is anonymous or passwordless.
 * @param session Current session handler
 * @param user User that wants to authenticate
 * @param userdata Userdata to be passed to the callback function.
 * @returns SSH_AUTH_SUCCESS Authentication is accepted.
 * @returns SSH_AUTH_PARTIAL Partial authentication, more authentication means are needed.
 * @returns SSH_AUTH_DENIED Authentication failed.
 */
typedef int (*ssh_auth_none_callback) (ssh_session session, const char *user, void *userdata);

/**
 * @brief SSH authentication callback. Tries to authenticates user with the "gssapi-with-mic" method
 * @param session Current session handler
 * @param user Username of the user (can be spoofed)
 * @param principal Authenticated principal of the user, including realm.
 * @param userdata Userdata to be passed to the callback function.
 * @returns SSH_AUTH_SUCCESS Authentication is accepted.
 * @returns SSH_AUTH_PARTIAL Partial authentication, more authentication means are needed.
 * @returns SSH_AUTH_DENIED Authentication failed.
 * @warning Implementations should verify that parameter user matches in some way the principal.
 * user and principal can be different. Only the latter is guaranteed to be safe.
 */
typedef int (*ssh_auth_gssapi_mic_callback) (ssh_session session, const char *user, const char *principal,
		void *userdata);

/**
 * @brief SSH authentication callback.
 * @param session Current session handler
 * @param user User that wants to authenticate
 * @param pubkey public key used for authentication
 * @param signature_state SSH_PUBLICKEY_STATE_NONE if the key is not signed (simple public key probe),
 * 							SSH_PUBLICKEY_STATE_VALID if the signature is valid. Others values should be
 * 							replied with a SSH_AUTH_DENIED.
 * @param userdata Userdata to be passed to the callback function.
 * @returns SSH_AUTH_SUCCESS Authentication is accepted.
 * @returns SSH_AUTH_PARTIAL Partial authentication, more authentication means are needed.
 * @returns SSH_AUTH_DENIED Authentication failed.
 */
typedef int (*ssh_auth_pubkey_callback) (ssh_session session, const char *user, struct ssh_key_struct *pubkey,
		char signature_state, void *userdata);


/**
 * @brief Handles an SSH service request
 * @param session current session handler
 * @param service name of the service (e.g. "ssh-userauth") requested
 * @param userdata Userdata to be passed to the callback function.
 * @returns 0 if the request is to be allowed
 * @returns -1 if the request should not be allowed
 */

typedef int (*ssh_service_request_callback) (ssh_session session, const char *service, void *userdata);

/**
 * @brief Handles an SSH new channel open session request
 * @param session current session handler
 * @param userdata Userdata to be passed to the callback function.
 * @returns a valid ssh_channel handle if the request is to be allowed
 * @returns NULL if the request should not be allowed
 * @warning The channel pointer returned by this callback must be closed by the application.
 */
typedef ssh_channel (*ssh_channel_open_request_session_callback) (ssh_session session, void *userdata);

/*
 * @brief handle the beginning of a GSSAPI authentication, server side.
 * @param session current session handler
 * @param user the username of the client
 * @param n_oid number of available oids
 * @param oids OIDs provided by the client
 * @returns an ssh_string containing the chosen OID, that's supported by both
 * client and server.
 * @warning It is not necessary to fill this callback in if libssh is linked
 * with libgssapi.
 */
typedef ssh_string (*ssh_gssapi_select_oid_callback) (ssh_session session, const char *user,
		int n_oid, ssh_string *oids, void *userdata);

/*
 * @brief handle the negociation of a security context, server side.
 * @param session current session handler
 * @param[in] input_token input token provided by client
 * @param[out] output_token output of the gssapi accept_sec_context method,
 * 				NULL after completion.
 * @returns SSH_OK if the token was generated correctly or accept_sec_context
 * returned GSS_S_COMPLETE
 * @returns SSH_ERROR in case of error
 * @warning It is not necessary to fill this callback in if libssh is linked
 * with libgssapi.
 */
typedef int (*ssh_gssapi_accept_sec_ctx_callback) (ssh_session session,
		ssh_string input_token, ssh_string *output_token, void *userdata);

/*
 * @brief Verify and authenticates a MIC, server side.
 * @param session current session handler
 * @param[in] mic input mic to be verified provided by client
 * @param[in] mic_buffer buffer of data to be signed.
 * @param[in] mic_buffer_size size of mic_buffer
 * @returns SSH_OK if the MIC was authenticated correctly
 * @returns SSH_ERROR in case of error
 * @warning It is not necessary to fill this callback in if libssh is linked
 * with libgssapi.
 */
typedef int (*ssh_gssapi_verify_mic_callback) (ssh_session session,
		ssh_string mic, void *mic_buffer, size_t mic_buffer_size, void *userdata);


/**
 * This structure can be used to implement a libssh server, with appropriate callbacks.
 */

struct ssh_server_callbacks_struct {
  /** DON'T SET THIS use ssh_callbacks_init() instead. */
  size_t size;
  /**
   * User-provided data. User is free to set anything he wants here
   */
  void *userdata;
  /** This function gets called when a client tries to authenticate through
   * password method.
   */
  ssh_auth_password_callback auth_password_function;

  /** This function gets called when a client tries to authenticate through
   * none method.
   */
  ssh_auth_none_callback auth_none_function;

  /** This function gets called when a client tries to authenticate through
   * gssapi-mic method.
   */
  ssh_auth_gssapi_mic_callback auth_gssapi_mic_function;

  /** this function gets called when a client tries to authenticate or offer
   * a public key.
   */
  ssh_auth_pubkey_callback auth_pubkey_function;

  /** This functions gets called when a service request is issued by the
   * client
   */
  ssh_service_request_callback service_request_function;
  /** This functions gets called when a new channel request is issued by
   * the client
   */
  ssh_channel_open_request_session_callback channel_open_request_session_function;
  /** This function will be called when a new gssapi authentication is attempted.
   */
  ssh_gssapi_select_oid_callback gssapi_select_oid_function;
  /** This function will be called when a gssapi token comes in.
   */
  ssh_gssapi_accept_sec_ctx_callback gssapi_accept_sec_ctx_function;
  /* This function will be called when a MIC needs to be verified.
   */
  ssh_gssapi_verify_mic_callback gssapi_verify_mic_function;
};
typedef struct ssh_server_callbacks_struct *ssh_server_callbacks;

/**
 * @brief Set the session server callback functions.
 *
 * This functions sets the callback structure to use your own callback
 * functions for user authentication, new channels and requests.
 *
 * @code
 * struct ssh_server_callbacks_struct cb = {
 *   .userdata = data,
 *   .auth_password_function = my_auth_function
 * };
 * ssh_callbacks_init(&cb);
 * ssh_set_server_callbacks(session, &cb);
 * @endcode
 *
 * @param  session      The session to set the callback structure.
 *
 * @param  cb           The callback structure itself.
 *
 * @return SSH_OK on success, SSH_ERROR on error.
 */
 int ssh_set_server_callbacks(ssh_session session, ssh_server_callbacks cb);

/**
 * These are the callbacks exported by the socket structure
 * They are called by the socket module when a socket event appears
 */
struct ssh_socket_callbacks_struct {
  /**
   * User-provided data. User is free to set anything he wants here
   */
  void *userdata;
	/**
	 * This function will be called each time data appears on socket. The data
	 * not consumed will appear on the next data event.
	 */
  ssh_callback_data data;
  /** This function will be called each time a controlflow state changes, i.e.
   * the socket is available for reading or writing.
   */
  ssh_callback_int controlflow;
  /** This function will be called each time an exception appears on socket. An
   * exception can be a socket problem (timeout, ...) or an end-of-file.
   */
  ssh_callback_int_int exception;
  /** This function is called when the ssh_socket_connect was used on the socket
   * on nonblocking state, and the connection successed.
   */
  ssh_callback_int_int connected;
};
typedef struct ssh_socket_callbacks_struct *ssh_socket_callbacks;











/**
 * @brief Initializes an ssh_callbacks_struct
 * A call to this macro is mandatory when you have set a new
 * ssh_callback_struct structure. Its goal is to maintain the binary
 * compatibility with future versions of libssh as the structure
 * evolves with time.
 */




/**
 * @internal
 * @brief tests if a callback can be called without crash
 *  verifies that the struct size if big enough
 *  verifies that the callback pointer exists
 * @param p callback pointer
 * @param c callback name
 * @returns nonzero if callback can be called
 */





/**
 * @internal
 *
 * @brief Iterate through a list of callback structures
 *
 * This tests for their validity and executes them. The userdata argument is
 * automatically passed through.
 *
 * @param list     list of callbacks
 *
 * @param cbtype   type of the callback
 *
 * @param c        callback name
 *
 * @param va_args parameters to be passed
 */












/**
 * @internal
 *
 * @brief iterate through a list of callback structures.
 *
 * This tests for their validity and give control back to the calling code to
 * execute them. Caller can decide to break the loop or continue executing the
 * callbacks with different parameters
 *
 * @code
 * ssh_callbacks_iterate(channel->callbacks, ssh_channel_callbacks,
 *                     channel_eof_function){
 *     rc = ssh_callbacks_iterate_exec(session, channel);
 *     if (rc != SSH_OK){
 *         break;
 *     }
 * }
 * ssh_callbacks_iterate_end();
 * @endcode
 */















/** @brief Prototype for a packet callback, to be called when a new packet arrives
 * @param session The current session of the packet
 * @param type packet type (see ssh2.h)
 * @param packet buffer containing the packet, excluding size, type and padding fields
 * @param user user argument to the callback
 * and are called each time a packet shows up
 * @returns SSH_PACKET_USED Packet was parsed and used
 * @returns SSH_PACKET_NOT_USED Packet was not used or understood, processing must continue
 */
typedef int (*ssh_packet_callback) (ssh_session session, uint8_t type, ssh_buffer packet, void *user);

/** return values for a ssh_packet_callback */
/** Packet was used and should not be parsed by another callback */

/** Packet was not used and should be passed to any other callback
 * available */



/** @brief This macro declares a packet callback handler
 * @code
 * SSH_PACKET_CALLBACK(mycallback){
 * ...
 * }
 * @endcode
 */



struct ssh_packet_callbacks_struct {
	/** Index of the first packet type being handled */
	uint8_t start;
	/** Number of packets being handled by this callback struct */
	uint8_t n_callbacks;
	/** A pointer to n_callbacks packet callbacks */
	ssh_packet_callback *callbacks;
  /**
   * User-provided data. User is free to set anything he wants here
   */
	void *user;
};

typedef struct ssh_packet_callbacks_struct *ssh_packet_callbacks;

/**
 * @brief Set the session callback functions.
 *
 * This functions sets the callback structure to use your own callback
 * functions for auth, logging and status.
 *
 * @code
 * struct ssh_callbacks_struct cb = {
 *   .userdata = data,
 *   .auth_function = my_auth_function
 * };
 * ssh_callbacks_init(&cb);
 * ssh_set_callbacks(session, &cb);
 * @endcode
 *
 * @param  session      The session to set the callback structure.
 *
 * @param  cb           The callback structure itself.
 *
 * @return SSH_OK on success, SSH_ERROR on error.
 */
 int ssh_set_callbacks(ssh_session session, ssh_callbacks cb);

/**
 * @brief SSH channel data callback. Called when data is available on a channel
 * @param session Current session handler
 * @param channel the actual channel
 * @param data the data that has been read on the channel
 * @param len the length of the data
 * @param is_stderr is 0 for stdout or 1 for stderr
 * @param userdata Userdata to be passed to the callback function.
 * @returns number of bytes processed by the callee. The remaining bytes will
 * be sent in the next callback message, when more data is available.
 */
typedef int (*ssh_channel_data_callback) (ssh_session session,
                                           ssh_channel channel,
                                           void *data,
                                           uint32_t len,
                                           int is_stderr,
                                           void *userdata);

/**
 * @brief SSH channel eof callback. Called when a channel receives EOF
 * @param session Current session handler
 * @param channel the actual channel
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_channel_eof_callback) (ssh_session session,
                                           ssh_channel channel,
                                           void *userdata);

/**
 * @brief SSH channel close callback. Called when a channel is closed by remote peer
 * @param session Current session handler
 * @param channel the actual channel
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_channel_close_callback) (ssh_session session,
                                            ssh_channel channel,
                                            void *userdata);

/**
 * @brief SSH channel signal callback. Called when a channel has received a signal
 * @param session Current session handler
 * @param channel the actual channel
 * @param signal the signal name (without the SIG prefix)
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_channel_signal_callback) (ssh_session session,
                                            ssh_channel channel,
                                            const char *signal,
                                            void *userdata);

/**
 * @brief SSH channel exit status callback. Called when a channel has received an exit status
 * @param session Current session handler
 * @param channel the actual channel
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_channel_exit_status_callback) (ssh_session session,
                                            ssh_channel channel,
                                            int exit_status,
                                            void *userdata);

/**
 * @brief SSH channel exit signal callback. Called when a channel has received an exit signal
 * @param session Current session handler
 * @param channel the actual channel
 * @param signal the signal name (without the SIG prefix)
 * @param core a boolean telling wether a core has been dumped or not
 * @param errmsg the description of the exception
 * @param lang the language of the description (format: RFC 3066)
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_channel_exit_signal_callback) (ssh_session session,
                                            ssh_channel channel,
                                            const char *signal,
                                            int core,
                                            const char *errmsg,
                                            const char *lang,
                                            void *userdata);

/**
 * @brief SSH channel PTY request from a client.
 * @param channel the channel
 * @param term The type of terminal emulation
 * @param width width of the terminal, in characters
 * @param height height of the terminal, in characters
 * @param pxwidth width of the terminal, in pixels
 * @param pxheight height of the terminal, in pixels
 * @param userdata Userdata to be passed to the callback function.
 * @returns 0 if the pty request is accepted
 * @returns -1 if the request is denied
 */
typedef int (*ssh_channel_pty_request_callback) (ssh_session session,
                                            ssh_channel channel,
                                            const char *term,
                                            int width, int height,
                                            int pxwidth, int pwheight,
                                            void *userdata);

/**
 * @brief SSH channel Shell request from a client.
 * @param channel the channel
 * @param userdata Userdata to be passed to the callback function.
 * @returns 0 if the shell request is accepted
 * @returns 1 if the request is denied
 */
typedef int (*ssh_channel_shell_request_callback) (ssh_session session,
                                            ssh_channel channel,
                                            void *userdata);
/**
 * @brief SSH auth-agent-request from the client. This request is
 * sent by a client when agent forwarding is available.
 * Server is free to ignore this callback, no answer is expected.
 * @param channel the channel
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_channel_auth_agent_req_callback) (ssh_session session,
                                            ssh_channel channel,
                                            void *userdata);

/**
 * @brief SSH X11 request from the client. This request is
 * sent by a client when X11 forwarding is requested(and available).
 * Server is free to ignore this callback, no answer is expected.
 * @param channel the channel
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_channel_x11_req_callback) (ssh_session session,
                                            ssh_channel channel,
                                            int single_connection,
                                            const char *auth_protocol,
                                            const char *auth_cookie,
                                            uint32_t screen_number,
                                            void *userdata);
/**
 * @brief SSH channel PTY windows change (terminal size) from a client.
 * @param channel the channel
 * @param width width of the terminal, in characters
 * @param height height of the terminal, in characters
 * @param pxwidth width of the terminal, in pixels
 * @param pxheight height of the terminal, in pixels
 * @param userdata Userdata to be passed to the callback function.
 * @returns 0 if the pty request is accepted
 * @returns -1 if the request is denied
 */
typedef int (*ssh_channel_pty_window_change_callback) (ssh_session session,
                                            ssh_channel channel,
                                            int width, int height,
                                            int pxwidth, int pwheight,
                                            void *userdata);

/**
 * @brief SSH channel Exec request from a client.
 * @param channel the channel
 * @param command the shell command to be executed
 * @param userdata Userdata to be passed to the callback function.
 * @returns 0 if the exec request is accepted
 * @returns 1 if the request is denied
 */
typedef int (*ssh_channel_exec_request_callback) (ssh_session session,
                                            ssh_channel channel,
                                            const char *command,
                                            void *userdata);

/**
 * @brief SSH channel environment request from a client.
 * @param channel the channel
 * @param env_name name of the environment value to be set
 * @param env_value value of the environment value to be set
 * @param userdata Userdata to be passed to the callback function.
 * @returns 0 if the env request is accepted
 * @returns 1 if the request is denied
 * @warning some environment variables can be dangerous if changed (e.g.
 * 			LD_PRELOAD) and should not be fulfilled.
 */
typedef int (*ssh_channel_env_request_callback) (ssh_session session,
                                            ssh_channel channel,
                                            const char *env_name,
                                            const char *env_value,
                                            void *userdata);
/**
 * @brief SSH channel subsystem request from a client.
 * @param channel the channel
 * @param subsystem the subsystem required
 * @param userdata Userdata to be passed to the callback function.
 * @returns 0 if the subsystem request is accepted
 * @returns 1 if the request is denied
 */
typedef int (*ssh_channel_subsystem_request_callback) (ssh_session session,
                                            ssh_channel channel,
                                            const char *subsystem,
                                            void *userdata);

/**
 * @brief SSH channel write will not block (flow control).
 *
 * @param channel the channel
 *
 * @param[in] bytes size of the remote window in bytes. Writing as much data
 *            will not block.
 *
 * @param[in] userdata Userdata to be passed to the callback function.
 *
 * @returns 0 default return value (other return codes may be added in future).
 */
typedef int (*ssh_channel_write_wontblock_callback) (ssh_session session,
                                                     ssh_channel channel,
                                                     size_t bytes,
                                                     void *userdata);

struct ssh_channel_callbacks_struct {
  /** DON'T SET THIS use ssh_callbacks_init() instead. */
  size_t size;
  /**
   * User-provided data. User is free to set anything he wants here
   */
  void *userdata;
  /**
   * This functions will be called when there is data available.
   */
  ssh_channel_data_callback channel_data_function;
  /**
   * This functions will be called when the channel has received an EOF.
   */
  ssh_channel_eof_callback channel_eof_function;
  /**
   * This functions will be called when the channel has been closed by remote
   */
  ssh_channel_close_callback channel_close_function;
  /**
   * This functions will be called when a signal has been received
   */
  ssh_channel_signal_callback channel_signal_function;
  /**
   * This functions will be called when an exit status has been received
   */
  ssh_channel_exit_status_callback channel_exit_status_function;
  /**
   * This functions will be called when an exit signal has been received
   */
  ssh_channel_exit_signal_callback channel_exit_signal_function;
  /**
   * This function will be called when a client requests a PTY
   */
  ssh_channel_pty_request_callback channel_pty_request_function;
  /**
   * This function will be called when a client requests a shell
   */
  ssh_channel_shell_request_callback channel_shell_request_function;
  /** This function will be called when a client requests agent
   * authentication forwarding.
   */
  ssh_channel_auth_agent_req_callback channel_auth_agent_req_function;
  /** This function will be called when a client requests X11
   * forwarding.
   */
  ssh_channel_x11_req_callback channel_x11_req_function;
  /** This function will be called when a client requests a
   * window change.
   */
  ssh_channel_pty_window_change_callback channel_pty_window_change_function;
  /** This function will be called when a client requests a
   * command execution.
   */
  ssh_channel_exec_request_callback channel_exec_request_function;
  /** This function will be called when a client requests an environment
   * variable to be set.
   */
  ssh_channel_env_request_callback channel_env_request_function;
  /** This function will be called when a client requests a subsystem
   * (like sftp).
   */
  ssh_channel_subsystem_request_callback channel_subsystem_request_function;
  /** This function will be called when the channel write is guaranteed
   * not to block.
   */
  ssh_channel_write_wontblock_callback channel_write_wontblock_function;
};

typedef struct ssh_channel_callbacks_struct *ssh_channel_callbacks;

/**
 * @brief Set the channel callback functions.
 *
 * This functions sets the callback structure to use your own callback
 * functions for channel data and exceptions
 *
 * @code
 * struct ssh_channel_callbacks_struct cb = {
 *   .userdata = data,
 *   .channel_data = my_channel_data_function
 * };
 * ssh_callbacks_init(&cb);
 * ssh_set_channel_callbacks(channel, &cb);
 * @endcode
 *
 * @param  channel      The channel to set the callback structure.
 *
 * @param  cb           The callback structure itself.
 *
 * @return SSH_OK on success, SSH_ERROR on error.
 * @warning this function will not replace existing callbacks but set the
 *          new one atop of them.
 */
 int ssh_set_channel_callbacks(ssh_channel channel,
                                         ssh_channel_callbacks cb);

/**
 * @brief Add channel callback functions
 *
 * This function will add channel callback functions to the channel callback
 * list.
 * Callbacks missing from a callback structure will be probed in the next
 * on the list.
 *
 * @param  channel      The channel to set the callback structure.
 *
 * @param  cb           The callback structure itself.
 *
 * @return SSH_OK on success, SSH_ERROR on error.
 *
 * @see ssh_set_channel_callbacks
 */
 int ssh_add_channel_callbacks(ssh_channel channel,
                                         ssh_channel_callbacks cb);

/**
 * @brief Remove a channel callback.
 *
 * The channel has been added with ssh_add_channel_callbacks or
 * ssh_set_channel_callbacks in this case.
 *
 * @param channel  The channel to remove the callback structure from.
 *
 * @param cb       The callback structure to remove
 *
 * @returns SSH_OK on success, SSH_ERROR on error.
 */
 int ssh_remove_channel_callbacks(ssh_channel channel,
                                            ssh_channel_callbacks cb);

/** @} */

/** @group libssh_threads
 * @{
 */

typedef int (*ssh_thread_callback) (void **lock);

typedef unsigned long (*ssh_thread_id_callback) (void);
struct ssh_threads_callbacks_struct {
	const char *type;
  ssh_thread_callback mutex_init;
  ssh_thread_callback mutex_destroy;
  ssh_thread_callback mutex_lock;
  ssh_thread_callback mutex_unlock;
  ssh_thread_id_callback thread_id;
};

/**
 * @brief Set the thread callbacks structure.
 *
 * This is necessary if your program is using libssh in a multithreaded fashion.
 * This function must be called first, outside of any threading context (in your
 * main() function for instance), before you call ssh_init().
 *
 * @param[in] cb   A pointer to a ssh_threads_callbacks_struct structure, which
 *                 contains the different callbacks to be set.
 *
 * @returns        Always returns SSH_OK.
 *
 * @see ssh_threads_callbacks_struct
 * @see SSH_THREADS_PTHREAD
 * @bug libgcrypt 1.6 and bigger backend does not support custom callback.
 *      Using anything else than pthreads here will fail.
 */
 int ssh_threads_set_callbacks(struct ssh_threads_callbacks_struct
    *cb);

/**
 * @brief returns a pointer on the pthread threads callbacks, to be used with
 * ssh_threads_set_callbacks.
 * @warning you have to link with the library ssh_threads.
 * @see ssh_threads_set_callbacks
 */
 struct ssh_threads_callbacks_struct *ssh_threads_get_pthread(void);

/**
 * @brief Get the noop threads callbacks structure
 *
 * This can be used with ssh_threads_set_callbacks. These callbacks do nothing
 * and are being used by default.
 *
 * @return Always returns a valid pointer to the noop callbacks structure.
 *
 * @see ssh_threads_set_callbacks
 */
 struct ssh_threads_callbacks_struct *ssh_threads_get_noop(void);

/**
 * @brief Set the logging callback function.
 *
 * @param[in]  cb  The callback to set.
 *
 * @return         0 on success, < 0 on errror.
 */
 int ssh_set_log_callback(ssh_logging_callback cb);

/**
 * @brief Get the pointer to the logging callback function.
 *
 * @return The pointer the the callback or NULL if none set.
 */
 ssh_logging_callback ssh_get_log_callback(void);

/** @} */

}




/* @} */

#line 1 "/usr/include/libssh/legacy.h" 1
/*
 * This file is part of the SSH Library
 *
 * Copyright (c) 2010 by Aris Adamantiadis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

/* Since libssh.h includes legacy.h, it's important that libssh.h is included
 * first. we don't define LEGACY_H now because we want it to be defined when
 * included from libssh.h
 * All function calls declared in this header are deprecated and meant to be
 * removed in future.
 */












    



    




















    





    


    




    





    


        





    


    

    



    



















#line 1 "/usr/include/libssh/ssh2.h" 1

















































































#line 1 "/usr/include/libssh/server.h" 1
/* Public include file for server support */
/*
 * This file is part of the SSH Library
 *
 * Copyright (c) 2003-2008 by Aris Adamantiadis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

/**
 * @defgroup libssh_server The libssh server API
 *
 * @{
 */





#line 1 "/usr/include/libssh/server.h" 1



extern "C" {


enum ssh_bind_options_e {
  SSH_BIND_OPTIONS_BINDADDR,
  SSH_BIND_OPTIONS_BINDPORT,
  SSH_BIND_OPTIONS_BINDPORT_STR,
  SSH_BIND_OPTIONS_HOSTKEY,
  SSH_BIND_OPTIONS_DSAKEY,
  SSH_BIND_OPTIONS_RSAKEY,
  SSH_BIND_OPTIONS_BANNER,
  SSH_BIND_OPTIONS_LOG_VERBOSITY,
  SSH_BIND_OPTIONS_LOG_VERBOSITY_STR,
  SSH_BIND_OPTIONS_ECDSAKEY,
  SSH_BIND_OPTIONS_IMPORT_KEY
};

typedef struct ssh_bind_struct* ssh_bind;

/* Callback functions */

/**
 * @brief Incoming connection callback. This callback is called when a ssh_bind
 *        has a new incoming connection.
 * @param sshbind Current sshbind session handler
 * @param userdata Userdata to be passed to the callback function.
 */
typedef void (*ssh_bind_incoming_connection_callback) (ssh_bind sshbind,
    void *userdata);

/**
 * @brief These are the callbacks exported by the ssh_bind structure.
 *
 * They are called by the server module when events appear on the network.
 */
struct ssh_bind_callbacks_struct {
  /** DON'T SET THIS use ssh_callbacks_init() instead. */
  size_t size;
  /** A new connection is available. */
  ssh_bind_incoming_connection_callback incoming_connection;
};
typedef struct ssh_bind_callbacks_struct *ssh_bind_callbacks;

/**
 * @brief Creates a new SSH server bind.
 *
 * @return A newly allocated ssh_bind session pointer.
 */
 ssh_bind ssh_bind_new(void);

 int ssh_bind_options_set(ssh_bind sshbind,
    enum ssh_bind_options_e type, const void *value);

/**
 * @brief Start listening to the socket.
 *
 * @param  ssh_bind_o     The ssh server bind to use.
 *
 * @return 0 on success, < 0 on error.
 */
 int ssh_bind_listen(ssh_bind ssh_bind_o);

/**
 * @brief Set the callback for this bind.
 *
 * @param[in] sshbind   The bind to set the callback on.
 *
 * @param[in] callbacks An already set up ssh_bind_callbacks instance.
 *
 * @param[in] userdata  A pointer to private data to pass to the callbacks.
 *
 * @return              SSH_OK on success, SSH_ERROR if an error occured.
 *
 * @code
 *     struct ssh_callbacks_struct cb = {
 *         .userdata = data,
 *         .auth_function = my_auth_function
 *     };
 *     ssh_callbacks_init(&cb);
 *     ssh_bind_set_callbacks(session, &cb);
 * @endcode
 */
 int ssh_bind_set_callbacks(ssh_bind sshbind, ssh_bind_callbacks callbacks,
    void *userdata);

/**
 * @brief  Set the session to blocking/nonblocking mode.
 *
 * @param  ssh_bind_o     The ssh server bind to use.
 *
 * @param  blocking     Zero for nonblocking mode.
 */
 void ssh_bind_set_blocking(ssh_bind ssh_bind_o, int blocking);

/**
 * @brief Recover the file descriptor from the session.
 *
 * @param  ssh_bind_o     The ssh server bind to get the fd from.
 *
 * @return The file descriptor.
 */
 socket_t ssh_bind_get_fd(ssh_bind ssh_bind_o);

/**
 * @brief Set the file descriptor for a session.
 *
 * @param  ssh_bind_o     The ssh server bind to set the fd.
 *
 * @param  fd           The file descriptssh_bind B
 */
 void ssh_bind_set_fd(ssh_bind ssh_bind_o, socket_t fd);

/**
 * @brief Allow the file descriptor to accept new sessions.
 *
 * @param  ssh_bind_o     The ssh server bind to use.
 */
 void ssh_bind_fd_toaccept(ssh_bind ssh_bind_o);

/**
 * @brief Accept an incoming ssh connection and initialize the session.
 *
 * @param  ssh_bind_o     The ssh server bind to accept a connection.
 * @param  session			A preallocated ssh session
 * @see ssh_new
 * @return SSH_OK when a connection is established
 */
 int ssh_bind_accept(ssh_bind ssh_bind_o, ssh_session session);

/**
 * @brief Accept an incoming ssh connection on the given file descriptor
 *        and initialize the session.
 *
 * @param  ssh_bind_o     The ssh server bind to accept a connection.
 * @param  session        A preallocated ssh session
 * @param  fd             A file descriptor of an already established TCP
 *                          inbound connection
 * @see ssh_new
 * @see ssh_bind_accept
 * @return SSH_OK when a connection is established
 */
 int ssh_bind_accept_fd(ssh_bind ssh_bind_o, ssh_session session,
        socket_t fd);

 ssh_gssapi_creds ssh_gssapi_get_creds(ssh_session session);

/**
 * @brief Handles the key exchange and set up encryption
 *
 * @param  session			A connected ssh session
 * @see ssh_bind_accept
 * @return SSH_OK if the key exchange was successful
 */
 int ssh_handle_key_exchange(ssh_session session);

/**
 * @brief Free a ssh servers bind.
 *
 * @param  ssh_bind_o     The ssh server bind to free.
 */
 void ssh_bind_free(ssh_bind ssh_bind_o);

/**
 * @brief Set the acceptable authentication methods to be sent to the client.
 *
 *
 * @param[in]  session  The server session
 *
 * @param[in]  auth_methods The authentication methods we will support, which
 *                          can be bitwise-or'd.
 *
 *                          Supported methods are:
 *
 *                          SSH_AUTH_METHOD_PASSWORD
 *                          SSH_AUTH_METHOD_PUBLICKEY
 *                          SSH_AUTH_METHOD_HOSTBASED
 *                          SSH_AUTH_METHOD_INTERACTIVE
 *                          SSH_AUTH_METHOD_GSSAPI_MIC
 */
 void ssh_set_auth_methods(ssh_session session, int auth_methods);

/**********************************************************
 * SERVER MESSAGING
 **********************************************************/

/**
 * @brief Reply with a standard reject message.
 *
 * Use this function if you don't know what to respond or if you want to reject
 * a request.
 *
 * @param[in] msg       The message to use for the reply.
 *
 * @return              0 on success, -1 on error.
 *
 * @see ssh_message_get()
 */
 int ssh_message_reply_default(ssh_message msg);

/**
 * @brief Get the name of the authenticated user.
 *
 * @param[in] msg       The message to get the username from.
 *
 * @return              The username or NULL if an error occured.
 *
 * @see ssh_message_get()
 * @see ssh_message_type()
 */
 const char *ssh_message_auth_user(ssh_message msg);

/**
 * @brief Get the password of the authenticated user.
 *
 * @param[in] msg       The message to get the password from.
 *
 * @return              The username or NULL if an error occured.
 *
 * @see ssh_message_get()
 * @see ssh_message_type()
 */
 const char *ssh_message_auth_password(ssh_message msg);

/**
 * @brief Get the publickey of the authenticated user.
 *
 * If you need the key for later user you should duplicate it.
 *
 * @param[in] msg       The message to get the public key from.
 *
 * @return              The public key or NULL.
 *
 * @see ssh_key_dup()
 * @see ssh_key_cmp()
 * @see ssh_message_get()
 * @see ssh_message_type()
 */
 ssh_key ssh_message_auth_pubkey(ssh_message msg);

 int ssh_message_auth_kbdint_is_response(ssh_message msg);
 enum ssh_publickey_state_e ssh_message_auth_publickey_state(ssh_message msg);
 int ssh_message_auth_reply_success(ssh_message msg,int partial);
 int ssh_message_auth_reply_pk_ok(ssh_message msg, ssh_string algo, ssh_string pubkey);
 int ssh_message_auth_reply_pk_ok_simple(ssh_message msg);

 int ssh_message_auth_set_methods(ssh_message msg, int methods);

 int ssh_message_auth_interactive_request(ssh_message msg,
                    const char *name, const char *instruction,
                    unsigned int num_prompts, const char **prompts, char *echo);

 int ssh_message_service_reply_success(ssh_message msg);
 const char *ssh_message_service_service(ssh_message msg);

 int ssh_message_global_request_reply_success(ssh_message msg,
                                                        uint16_t bound_port);

 void ssh_set_message_callback(ssh_session session,
    int(*ssh_bind_message_callback)(ssh_session session, ssh_message msg, void *data),
    void *data);
 int ssh_execute_message_callbacks(ssh_session session);

 const char *ssh_message_channel_request_open_originator(ssh_message msg);
 int ssh_message_channel_request_open_originator_port(ssh_message msg);
 const char *ssh_message_channel_request_open_destination(ssh_message msg);
 int ssh_message_channel_request_open_destination_port(ssh_message msg);

 ssh_channel ssh_message_channel_request_channel(ssh_message msg);

 const char *ssh_message_channel_request_pty_term(ssh_message msg);
 int ssh_message_channel_request_pty_width(ssh_message msg);
 int ssh_message_channel_request_pty_height(ssh_message msg);
 int ssh_message_channel_request_pty_pxwidth(ssh_message msg);
 int ssh_message_channel_request_pty_pxheight(ssh_message msg);

 const char *ssh_message_channel_request_env_name(ssh_message msg);
 const char *ssh_message_channel_request_env_value(ssh_message msg);

 const char *ssh_message_channel_request_command(ssh_message msg);

 const char *ssh_message_channel_request_subsystem(ssh_message msg);

 int ssh_message_channel_request_x11_single_connection(ssh_message msg);
 const char *ssh_message_channel_request_x11_auth_protocol(ssh_message msg);
 const char *ssh_message_channel_request_x11_auth_cookie(ssh_message msg);
 int ssh_message_channel_request_x11_screen_number(ssh_message msg);

 const char *ssh_message_global_request_address(ssh_message msg);
 int ssh_message_global_request_port(ssh_message msg);

 int ssh_channel_open_reverse_forward(ssh_channel channel, const char *remotehost,
    int remoteport, const char *sourcehost, int localport);
 int ssh_channel_open_x11(ssh_channel channel, 
                                        const char *orig_addr, int orig_port);

 int ssh_channel_request_send_exit_status(ssh_channel channel,
                                                int exit_status);
 int ssh_channel_request_send_exit_signal(ssh_channel channel,
                                                const char *signum,
                                                int core,
                                                const char *errmsg,
                                                const char *lang);

 int ssh_send_keepalive(ssh_session session);

/* deprecated functions */
  int ssh_accept(ssh_session session);
  int channel_write_stderr(ssh_channel channel,
        const void *data, uint32_t len);


}




/** @} */
