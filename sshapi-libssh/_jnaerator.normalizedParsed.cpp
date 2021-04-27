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




















/** Convenience types. */
typedef unsigned char __u_char;
typedef unsigned short __u_short;
typedef unsigned int __u_int;
typedef unsigned long long __u_long;
/** Fixed-size types, underlying types depend on word size and compiler. */
typedef signed char __int8_t;
typedef unsigned char __uint8_t;
typedef signed short __int16_t;
typedef unsigned short __uint16_t;
typedef signed int __int32_t;
typedef unsigned int __uint32_t;
typedef signed long long __int64_t;
typedef unsigned long long __uint64_t;
/** quad_t is also 64 bits. */
typedef long long __quad_t;
typedef unsigned long long __u_quad_t;
/** Largest integral types. */
typedef long long __intmax_t;
typedef unsigned long long __uintmax_t;

typedef unsigned long long __dev_t; /* Type of device numbers.  */
typedef unsigned int __uid_t; /* Type of user identifications.  */
typedef unsigned int __gid_t; /* Type of group identifications.  */
typedef unsigned long long __ino_t; /* Type of file serial numbers.  */
typedef unsigned long long __ino64_t; /* Type of file serial numbers (LFS).*/
typedef unsigned int __mode_t; /* Type of file attribute bitmasks.  */
typedef unsigned long long __nlink_t; /* Type of file link counts.  */
typedef long long __off_t; /* Type of file sizes and offsets.  */
typedef long long __off64_t; /* Type of file sizes and offsets (LFS).  */
typedef int __pid_t; /* Type of process identifications.  */
typedef struct __fsid_t {
	int[2] __val;
} __fsid_t; /* Type of file system IDs.  */
typedef long long __clock_t; /* Type of CPU usage counts.  */
typedef unsigned long long __rlim_t; /* Type for resource measurement.  */
typedef unsigned long long __rlim64_t; /* Type for resource measurement (LFS).  */
typedef unsigned int __id_t; /* General type for IDs.  */
typedef long long __time_t; /* Seconds since the Epoch.  */
typedef unsigned int __useconds_t; /* Count of microseconds.  */
typedef long long __suseconds_t; /* Signed count of microseconds.  */
typedef int __daddr_t; /* The type of a disk address.  */
typedef int __key_t; /* Type of an IPC key.  */
/** Clock ID used in clock and timer functions. */
typedef int __clockid_t;
/** Timer ID returned by `timer_create'. */
typedef void* __timer_t;
/** Type to represent block size. */
typedef long long __blksize_t;
/** Type to count number of disk blocks. */
typedef long long __blkcnt_t;
typedef long long __blkcnt64_t;
/** Type to count file system blocks. */
typedef unsigned long long __fsblkcnt_t;
typedef unsigned long long __fsblkcnt64_t;
/** Type to count file system nodes. */
typedef unsigned long long __fsfilcnt_t;
typedef unsigned long long __fsfilcnt64_t;
/** Type of miscellaneous file system fields. */
typedef long long __fsword_t;
typedef long long __ssize_t; /* Type of a byte count, or error.  */
/** Signed long type used in system calls. */
typedef long long __syscall_slong_t;
/** Unsigned long type used in system calls. */
typedef unsigned long long __syscall_ulong_t;
/**
 * These few don't really vary by system, they always correspond<br>
 * to one of the other defined types.
 */
typedef __off64_t __loff_t; /* Type of file sizes and offsets (LFS).  */
typedef char* __caddr_t;
/** Duplicates info from stdint.h but this is used in unistd.h. */
typedef long long __intptr_t;
/** Duplicate info from sys/socket.h. */
typedef unsigned int __socklen_t;
/**
 * C99: An integer type that can be accessed as an atomic entity,<br>
 * even in the presence of asynchronous interrupts.<br>
 * It is not currently necessary for this to be machine-specific.
 */
typedef int __sig_atomic_t;
typedef __ssize_t ssize_t;
/**
 * The Single Unix specification says that some more types are<br>
 * available here.
 */
typedef __gid_t gid_t;
typedef __uid_t uid_t;
typedef __off_t off_t;
typedef __useconds_t useconds_t;
typedef __pid_t pid_t;
typedef __intptr_t intptr_t;
typedef __socklen_t socklen_t;
/**
 * Test for access to NAME using the real UID and real GID.<br>
 * Original signature : <code>int access(const char*, int)</code>
 */
extern int access(const char* __name, int __type);
/**
 * Test for access to FILE relative to the directory FD is open on.<br>
 * If AT_EACCESS is set in FLAG, then use effective IDs like `eaccess',<br>
 * otherwise use real IDs like `access'.<br>
 * Original signature : <code>int faccessat(int, const char*, int, int)</code>
 */
extern int faccessat(int __fd, const char* __file, int __type, int __flag);
/**
 * Move FD's file position to OFFSET bytes from the<br>
 * beginning of the file (if WHENCE is SEEK_SET),<br>
 * the current position (if WHENCE is SEEK_CUR),<br>
 * or the end of the file (if WHENCE is SEEK_END).<br>
 * Return the new file position.<br>
 * Original signature : <code>__off_t lseek(int, __off_t, int)</code>
 */
extern __off_t lseek(int __fd, __off_t __offset, int __whence);
/**
 * Close the file descriptor FD.<br>
 * This function is a cancellation point and therefore not marked with<br>
 * __THROW.<br>
 * Original signature : <code>int close(int)</code>
 */
extern int close(int __fd);
/**
 * Read NBYTES into BUF from FD.  Return the<br>
 * number read, -1 for errors or 0 for EOF.<br>
 * This function is a cancellation point and therefore not marked with<br>
 * __THROW.<br>
 * Original signature : <code>ssize_t read(int, void*, size_t)</code>
 */
extern ssize_t read(int __fd, void* __buf, size_t __nbytes);
/**
 * Write N bytes of BUF to FD.  Return the number written, or -1.<br>
 * This function is a cancellation point and therefore not marked with<br>
 * __THROW.<br>
 * Original signature : <code>ssize_t write(int, const void*, size_t)</code>
 */
extern ssize_t write(int __fd, const void* __buf, size_t __n);
/**
 * Read NBYTES into BUF from FD at the given position OFFSET without<br>
 * changing the file pointer.  Return the number read, -1 for errors<br>
 * or 0 for EOF.<br>
 * This function is a cancellation point and therefore not marked with<br>
 * __THROW.<br>
 * Original signature : <code>ssize_t pread(int, void*, size_t, __off_t)</code>
 */
extern ssize_t pread(int __fd, void* __buf, size_t __nbytes, __off_t __offset);
/**
 * Write N bytes of BUF to FD at the given position OFFSET without<br>
 * changing the file pointer.  Return the number written, or -1.<br>
 * This function is a cancellation point and therefore not marked with<br>
 * __THROW.<br>
 * Original signature : <code>ssize_t pwrite(int, const void*, size_t, __off_t)</code>
 */
extern ssize_t pwrite(int __fd, const void* __buf, size_t __n, __off_t __offset);
/**
 * Create a one-way communication channel (pipe).<br>
 * If successful, two file descriptors are stored in PIPEDES;<br>
 * bytes written on PIPEDES[1] can be read from PIPEDES[0].<br>
 * Returns 0 if successful, -1 if not.<br>
 * Original signature : <code>int pipe(int[2])</code>
 */
extern int pipe(int __pipedes[2]);
/**
 * Schedule an alarm.  In SECONDS seconds, the process will get a SIGALRM.<br>
 * If SECONDS is zero, any currently scheduled alarm will be cancelled.<br>
 * The function returns the number of seconds remaining until the last<br>
 * alarm scheduled would have signaled, or zero if there wasn't one.<br>
 * There is no return value to indicate an error, but you can set `errno'<br>
 * to 0 and check its value after calling `alarm', and this might tell you.<br>
 * The signal may come late due to processor scheduling.<br>
 * Original signature : <code>int alarm(unsigned int)</code>
 */
extern unsigned int alarm(unsigned int __seconds);
/**
 * Make the process sleep for SECONDS seconds, or until a signal arrives<br>
 * and is not ignored.  The function returns the number of seconds less<br>
 * than SECONDS which it actually slept (thus zero if it slept the full time).<br>
 * If a signal handler does a `longjmp' or modifies the handling of the<br>
 * SIGALRM signal while inside `sleep' call, the handling of the SIGALRM<br>
 * signal afterwards is undefined.  There is no return value to indicate<br>
 * error, but if `sleep' returns SECONDS, it probably didn't work.<br>
 * This function is a cancellation point and therefore not marked with<br>
 * __THROW.<br>
 * Original signature : <code>int sleep(unsigned int)</code>
 */
extern unsigned int sleep(unsigned int __seconds);
/**
 * Set an alarm to go off (generating a SIGALRM signal) in VALUE<br>
 * microseconds.  If INTERVAL is nonzero, when the alarm goes off, the<br>
 * timer is reset to go off every INTERVAL microseconds thereafter.<br>
 * Returns the number of microseconds remaining before the alarm.<br>
 * Original signature : <code>__useconds_t ualarm(__useconds_t, __useconds_t)</code>
 */
extern __useconds_t ualarm(__useconds_t __value, __useconds_t __interval);
/**
 * Sleep USECONDS microseconds, or until a signal arrives that is not blocked<br>
 * or ignored.<br>
 * This function is a cancellation point and therefore not marked with<br>
 * __THROW.<br>
 * Original signature : <code>int usleep(__useconds_t)</code>
 */
extern int usleep(__useconds_t __useconds);
/**
 * Suspend the process until a signal arrives.<br>
 * This always returns -1 and sets `errno' to EINTR.<br>
 * This function is a cancellation point and therefore not marked with<br>
 * __THROW.<br>
 * Original signature : <code>int pause()</code>
 */
extern int pause();
/**
 * Change the owner and group of FILE.<br>
 * Original signature : <code>int chown(const char*, __uid_t, __gid_t)</code>
 */
extern int chown(const char* __file, __uid_t __owner, __gid_t __group);
/**
 * Change the owner and group of the file that FD is open on.<br>
 * Original signature : <code>int fchown(int, __uid_t, __gid_t)</code>
 */
extern int fchown(int __fd, __uid_t __owner, __gid_t __group);
/**
 * Change owner and group of FILE, if it is a symbolic<br>
 * link the ownership of the symbolic link is changed.<br>
 * Original signature : <code>int lchown(const char*, __uid_t, __gid_t)</code>
 */
extern int lchown(const char* __file, __uid_t __owner, __gid_t __group);
/**
 * Change the owner and group of FILE relative to the directory FD is open<br>
 * on.<br>
 * Original signature : <code>int fchownat(int, const char*, __uid_t, __gid_t, int)</code>
 */
extern int fchownat(int __fd, const char* __file, __uid_t __owner, __gid_t __group, int __flag);
/**
 * Change the process's working directory to PATH.<br>
 * Original signature : <code>int chdir(const char*)</code>
 */
extern int chdir(const char* __path);
/**
 * Change the process's working directory to the one FD is open on.<br>
 * Original signature : <code>int fchdir(int)</code>
 */
extern int fchdir(int __fd);
/**
 * Get the pathname of the current working directory,<br>
 * and put it in SIZE bytes of BUF.  Returns NULL if the<br>
 * directory couldn't be determined or SIZE was too small.<br>
 * If successful, returns BUF.  In GNU, if BUF is NULL,<br>
 * an array is allocated with `malloc'; the array is SIZE<br>
 * bytes long, unless SIZE == 0, in which case it is as<br>
 * big as necessary.<br>
 * Original signature : <code>char* getcwd(char*, size_t)</code>
 */
extern char* getcwd(char* __buf, size_t __size);
/**
 * Put the absolute pathname of the current working directory in BUF.<br>
 * If successful, return BUF.  If not, put an error message in<br>
 * BUF and return NULL.  BUF should be at least PATH_MAX bytes long.<br>
 * Original signature : <code>char* getwd(char*)</code>
 */
extern char* getwd(char* __buf);
/**
 * Duplicate FD, returning a new file descriptor on the same file.<br>
 * Original signature : <code>int dup(int)</code>
 */
extern int dup(int __fd);
/**
 * Duplicate FD to FD2, closing FD2 and making it open on the same file.<br>
 * Original signature : <code>int dup2(int, int)</code>
 */
extern int dup2(int __fd, int __fd2);
/** NULL-terminated array of "NAME=VALUE" environment variables. */
extern char** __environ;
/**
 * Replace the current process, executing PATH with arguments ARGV and<br>
 * environment ENVP.  ARGV and ENVP are terminated by NULL pointers.<br>
 * Original signature : <code>int execve(const char*, const char*[], const char*[])</code>
 */
extern int execve(const char* __path, const char* __argv[], const char* __envp[]);
/**
 * Execute the file FD refers to, overlaying the running program image.<br>
 * ARGV and ENVP are passed to the new program, as for `execve'.<br>
 * Original signature : <code>int fexecve(int, const char*[], const char*[])</code>
 */
extern int fexecve(int __fd, const char* __argv[], const char* __envp[]);
/**
 * Execute PATH with arguments ARGV and environment from `environ'.<br>
 * Original signature : <code>int execv(const char*, const char*[])</code>
 */
extern int execv(const char* __path, const char* __argv[]);
/**
 * Execute PATH with all arguments after PATH until a NULL pointer,<br>
 * and the argument after that for environment.<br>
 * Original signature : <code>int execle(const char*, const char*, null)</code>
 */
extern int execle(const char* __path, const char* __arg, ...);
/**
 * Execute PATH with all arguments after PATH until<br>
 * a NULL pointer and environment from `environ'.<br>
 * Original signature : <code>int execl(const char*, const char*, null)</code>
 */
extern int execl(const char* __path, const char* __arg, ...);
/**
 * Execute FILE, searching in the `PATH' environment variable if it contains<br>
 * no slashes, with arguments ARGV and environment from `environ'.<br>
 * Original signature : <code>int execvp(const char*, const char*[])</code>
 */
extern int execvp(const char* __file, const char* __argv[]);
/**
 * Execute FILE, searching in the `PATH' environment variable if<br>
 * it contains no slashes, with all arguments after FILE until a<br>
 * NULL pointer and environment from `environ'.<br>
 * Original signature : <code>int execlp(const char*, const char*, null)</code>
 */
extern int execlp(const char* __file, const char* __arg, ...);
/**
 * Add INC to priority of the current process.<br>
 * Original signature : <code>int nice(int)</code>
 */
extern int nice(int __inc);
/**
 * Terminate program execution with the low-order 8 bits of STATUS.<br>
 * Original signature : <code>void _exit(int)</code>
 */
extern void _exit(int __status);
/** Values for the NAME argument to `pathconf' and `fpathconf'. */
enum {
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
/** Values for the argument to `sysconf'. */
enum {
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
/** Values for the NAME argument to `confstr'. */
enum {
	_CS_PATH /* The default search path.  */,
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
/**
 * Get file-specific configuration information about PATH.<br>
 * Original signature : <code>long long pathconf(const char*, int)</code>
 */
extern long long pathconf(const char* __path, int __name);
/**
 * Get file-specific configuration about descriptor FD.<br>
 * Original signature : <code>long long fpathconf(int, int)</code>
 */
extern long long fpathconf(int __fd, int __name);
/**
 * Get the value of the system variable NAME.<br>
 * Original signature : <code>long long sysconf(int)</code>
 */
extern long long sysconf(int __name);
/**
 * Get the value of the string-valued system variable NAME.<br>
 * Original signature : <code>size_t confstr(int, char*, size_t)</code>
 */
extern size_t confstr(int __name, char* __buf, size_t __len);
/**
 * Get the process ID of the calling process.<br>
 * Original signature : <code>__pid_t getpid()</code>
 */
extern __pid_t getpid();
/**
 * Get the process ID of the calling process's parent.<br>
 * Original signature : <code>__pid_t getppid()</code>
 */
extern __pid_t getppid();
/**
 * Get the process group ID of the calling process.<br>
 * Original signature : <code>__pid_t getpgrp()</code>
 */
extern __pid_t getpgrp();
/**
 * Get the process group ID of process PID.<br>
 * Original signature : <code>__pid_t __getpgid(__pid_t)</code>
 */
extern __pid_t __getpgid(__pid_t __pid);
/** Original signature : <code>__pid_t getpgid(__pid_t)</code> */
extern __pid_t getpgid(__pid_t __pid);
/**
 * Set the process group ID of the process matching PID to PGID.<br>
 * If PID is zero, the current process's process group ID is set.<br>
 * If PGID is zero, the process ID of the process is used.<br>
 * Original signature : <code>int setpgid(__pid_t, __pid_t)</code>
 */
extern int setpgid(__pid_t __pid, __pid_t __pgid);
/**
 * Set the process group ID of the calling process to its own PID.<br>
 * This is exactly the same as `setpgid (0, 0)'.<br>
 * Original signature : <code>int setpgrp()</code>
 */
extern int setpgrp();
/**
 * Create a new session with the calling process as its leader.<br>
 * The process group IDs of the session and the calling process<br>
 * are set to the process ID of the calling process, which is returned.<br>
 * Original signature : <code>__pid_t setsid()</code>
 */
extern __pid_t setsid();
/**
 * Return the session ID of the given process.<br>
 * Original signature : <code>__pid_t getsid(__pid_t)</code>
 */
extern __pid_t getsid(__pid_t __pid);
/**
 * Get the real user ID of the calling process.<br>
 * Original signature : <code>__uid_t getuid()</code>
 */
extern __uid_t getuid();
/**
 * Get the effective user ID of the calling process.<br>
 * Original signature : <code>__uid_t geteuid()</code>
 */
extern __uid_t geteuid();
/**
 * Get the real group ID of the calling process.<br>
 * Original signature : <code>__gid_t getgid()</code>
 */
extern __gid_t getgid();
/**
 * Get the effective group ID of the calling process.<br>
 * Original signature : <code>__gid_t getegid()</code>
 */
extern __gid_t getegid();
/**
 * If SIZE is zero, return the number of supplementary groups<br>
 * the calling process is in.  Otherwise, fill in the group IDs<br>
 * of its supplementary groups in LIST and return the number written.<br>
 * Original signature : <code>int getgroups(int, __gid_t[])</code>
 */
extern int getgroups(int __size, __gid_t __list[]);
/**
 * Set the user ID of the calling process to UID.<br>
 * If the calling process is the super-user, set the real<br>
 * and effective user IDs, and the saved set-user-ID to UID;<br>
 * if not, the effective user ID is set to UID.<br>
 * Original signature : <code>int setuid(__uid_t)</code>
 */
extern int setuid(__uid_t __uid);
/**
 * Set the real user ID of the calling process to RUID,<br>
 * and the effective user ID of the calling process to EUID.<br>
 * Original signature : <code>int setreuid(__uid_t, __uid_t)</code>
 */
extern int setreuid(__uid_t __ruid, __uid_t __euid);
/**
 * Set the effective user ID of the calling process to UID.<br>
 * Original signature : <code>int seteuid(__uid_t)</code>
 */
extern int seteuid(__uid_t __uid);
/**
 * Set the group ID of the calling process to GID.<br>
 * If the calling process is the super-user, set the real<br>
 * and effective group IDs, and the saved set-group-ID to GID;<br>
 * if not, the effective group ID is set to GID.<br>
 * Original signature : <code>int setgid(__gid_t)</code>
 */
extern int setgid(__gid_t __gid);
/**
 * Set the real group ID of the calling process to RGID,<br>
 * and the effective group ID of the calling process to EGID.<br>
 * Original signature : <code>int setregid(__gid_t, __gid_t)</code>
 */
extern int setregid(__gid_t __rgid, __gid_t __egid);
/**
 * Set the effective group ID of the calling process to GID.<br>
 * Original signature : <code>int setegid(__gid_t)</code>
 */
extern int setegid(__gid_t __gid);
/**
 * Clone the calling process, creating an exact copy.<br>
 * Return -1 for errors, 0 to the new process,<br>
 * and the process ID of the new process to the old process.<br>
 * Original signature : <code>__pid_t fork()</code>
 */
extern __pid_t fork();
/**
 * Clone the calling process, but without copying the whole address space.<br>
 * The calling process is suspended until the new process exits or is<br>
 * replaced by a call to `execve'.  Return -1 for errors, 0 to the new process,<br>
 * and the process ID of the new process to the old process.<br>
 * Original signature : <code>__pid_t vfork()</code>
 */
extern __pid_t vfork();
/**
 * Return the pathname of the terminal FD is open on, or NULL on errors.<br>
 * The returned storage is good only until the next call to this function.<br>
 * Original signature : <code>char* ttyname(int)</code>
 */
extern char* ttyname(int __fd);
/**
 * Store at most BUFLEN characters of the pathname of the terminal FD is<br>
 * open on in BUF.  Return 0 on success, otherwise an error number.<br>
 * Original signature : <code>int ttyname_r(int, char*, size_t)</code>
 */
extern int ttyname_r(int __fd, char* __buf, size_t __buflen);
/**
 * Return 1 if FD is a valid descriptor associated<br>
 * with a terminal, zero if not.<br>
 * Original signature : <code>int isatty(int)</code>
 */
extern int isatty(int __fd);
/**
 * Return the index into the active-logins file (utmp) for<br>
 * the controlling terminal.<br>
 * Original signature : <code>int ttyslot()</code>
 */
extern int ttyslot();
/**
 * Make a link to FROM named TO.<br>
 * Original signature : <code>int link(const char*, const char*)</code>
 */
extern int link(const char* __from, const char* __to);
/**
 * Like link but relative paths in TO and FROM are interpreted relative<br>
 * to FROMFD and TOFD respectively.<br>
 * Original signature : <code>int linkat(int, const char*, int, const char*, int)</code>
 */
extern int linkat(int __fromfd, const char* __from, int __tofd, const char* __to, int __flags);
/**
 * Make a symbolic link to FROM named TO.<br>
 * Original signature : <code>int symlink(const char*, const char*)</code>
 */
extern int symlink(const char* __from, const char* __to);
/**
 * Read the contents of the symbolic link PATH into no more than<br>
 * LEN bytes of BUF.  The contents are not null-terminated.<br>
 * Returns the number of characters read, or -1 for errors.<br>
 * Original signature : <code>ssize_t readlink(const char*, char*, size_t)</code>
 */
extern ssize_t readlink(const char* __path, char* __buf, size_t __len);
/**
 * Like symlink but a relative path in TO is interpreted relative to TOFD.<br>
 * Original signature : <code>int symlinkat(const char*, int, const char*)</code>
 */
extern int symlinkat(const char* __from, int __tofd, const char* __to);
/**
 * Like readlink but a relative PATH is interpreted relative to FD.<br>
 * Original signature : <code>ssize_t readlinkat(int, const char*, char*, size_t)</code>
 */
extern ssize_t readlinkat(int __fd, const char* __path, char* __buf, size_t __len);
/**
 * Remove the link NAME.<br>
 * Original signature : <code>int unlink(const char*)</code>
 */
extern int unlink(const char* __name);
/**
 * Remove the link NAME relative to FD.<br>
 * Original signature : <code>int unlinkat(int, const char*, int)</code>
 */
extern int unlinkat(int __fd, const char* __name, int __flag);
/**
 * Remove the directory PATH.<br>
 * Original signature : <code>int rmdir(const char*)</code>
 */
extern int rmdir(const char* __path);
/**
 * Return the foreground process group ID of FD.<br>
 * Original signature : <code>__pid_t tcgetpgrp(int)</code>
 */
extern __pid_t tcgetpgrp(int __fd);
/**
 * Set the foreground process group ID of FD set PGRP_ID.<br>
 * Original signature : <code>int tcsetpgrp(int, __pid_t)</code>
 */
extern int tcsetpgrp(int __fd, __pid_t __pgrp_id);
/**
 * Return the login name of the user.<br>
 * This function is a possible cancellation point and therefore not<br>
 * marked with __THROW.<br>
 * Original signature : <code>char* getlogin()</code>
 */
extern char* getlogin();
/**
 * Return at most NAME_LEN characters of the login name of the user in NAME.<br>
 * If it cannot be determined or some other error occurred, return the error<br>
 * code.  Otherwise return 0.<br>
 * This function is a possible cancellation point and therefore not<br>
 * marked with __THROW.<br>
 * Original signature : <code>int getlogin_r(char*, size_t)</code>
 */
extern int getlogin_r(char* __name, size_t __name_len);
/**
 * Set the login name returned by `getlogin'.<br>
 * Original signature : <code>int setlogin(const char*)</code>
 */
extern int setlogin(const char* __name);

extern ""C"" {
/**
	 * For communication from 'getopt' to the caller.<br>
	 * When 'getopt' finds an option that takes an argument,<br>
	 * the argument value is returned here.<br>
	 * Also, when 'ordering' is RETURN_IN_ORDER,<br>
	 * each non-option ARGV-element is returned here.
	 */
	extern char* optarg;
	/**
	 * Index in ARGV of the next element to be scanned.<br>
	 * This is used for communication to and from the caller<br>
	 * and for communication between successive calls to 'getopt'.<br>
	 * On entry to 'getopt', zero means this is the first call; initialize.<br>
	 * When 'getopt' returns -1, this is the index of the first of the<br>
	 * non-option elements that the caller should itself scan.<br>
	 * Otherwise, 'optind' communicates from one call to the next<br>
	 * how much of ARGV has been scanned so far.
	 */
	extern int optind;
	/**
	 * Callers store zero here to inhibit the error message 'getopt' prints<br>
	 * for unrecognized options.
	 */
	extern int opterr;
	/** Set to an option character which was unrecognized. */
	extern int optopt;
	/**
	 * Get definitions and prototypes for functions to process the<br>
	 * arguments in ARGV (ARGC of them, minus the program name) for<br>
	 * options given in OPTS.<br>
	 * Return the option character from OPTS just read.  Return -1 when<br>
	 * there are no more options.  For unrecognized options, or options<br>
	 * missing arguments, 'optopt' is set to the option letter, and '?' is<br>
	 * returned.<br>
	 * The OPTS string is a list of characters which are recognized option<br>
	 * letters, optionally followed by colons, specifying that that letter<br>
	 * takes an argument, to be placed in 'optarg'.<br>
	 * If a letter in OPTS is followed by two colons, its argument is<br>
	 * optional.  This behavior is specific to the GNU 'getopt'.<br>
	 * The argument '--' causes premature termination of argument<br>
	 * scanning, explicitly telling 'getopt' that there are no more<br>
	 * options.<br>
	 * If OPTS begins with '-', then non-option arguments are treated as<br>
	 * arguments to the option '\1'.  This behavior is specific to the GNU<br>
	 * 'getopt'.  If OPTS begins with '+', or POSIXLY_CORRECT is set in<br>
	 * the environment, then do not permute arguments.<br>
	 * For standards compliance, the 'argv' argument has the type<br>
	 * char *const *, but this is inaccurate; if argument permutation is<br>
	 * enabled, the argv array (not the strings it points to) must be<br>
	 * writable.<br>
	 * Original signature : <code>int getopt(int, const char**, const char*)</code>
	 */
	extern int getopt(int ___argc, const char** ___argv, const char* __shortopts);
}
extern ""C"" {

}
/**
 * Put the name of the current host in no more than LEN bytes of NAME.<br>
 * The result is null-terminated if LEN is large enough for the full<br>
 * name and the terminator.<br>
 * Original signature : <code>int gethostname(char*, size_t)</code>
 */
extern int gethostname(char* __name, size_t __len);
/**
 * Set the name of the current host to NAME, which is LEN bytes long.<br>
 * This call is restricted to the super-user.<br>
 * Original signature : <code>int sethostname(const char*, size_t)</code>
 */
extern int sethostname(const char* __name, size_t __len);
/**
 * Set the current machine's Internet number to ID.<br>
 * This call is restricted to the super-user.<br>
 * Original signature : <code>int sethostid(long long)</code>
 */
extern int sethostid(long long __id);
/**
 * Get and set the NIS (aka YP) domain name, if any.<br>
 * Called just like `gethostname' and `sethostname'.<br>
 * The NIS domain name is usually the empty string when not using NIS.<br>
 * Original signature : <code>int getdomainname(char*, size_t)</code>
 */
extern int getdomainname(char* __name, size_t __len);
/** Original signature : <code>int setdomainname(const char*, size_t)</code> */
extern int setdomainname(const char* __name, size_t __len);
/**
 * Revoke access permissions to all processes currently communicating<br>
 * with the control terminal, and then send a SIGHUP signal to the process<br>
 * group of the control terminal.<br>
 * Original signature : <code>int vhangup()</code>
 */
extern int vhangup();
/**
 * Revoke the access of all descriptors currently open on FILE.<br>
 * Original signature : <code>int revoke(const char*)</code>
 */
extern int revoke(const char* __file);
/**
 * Enable statistical profiling, writing samples of the PC into at most<br>
 * SIZE bytes of SAMPLE_BUFFER; every processor clock tick while profiling<br>
 * is enabled, the system examines the user PC and increments<br>
 * SAMPLE_BUFFER[((PC - OFFSET) / 2) * SCALE / 65536].  If SCALE is zero,<br>
 * disable profiling.  Returns zero on success, -1 on error.<br>
 * Original signature : <code>int profil(unsigned short*, size_t, size_t, unsigned int)</code>
 */
extern int profil(unsigned short* __sample_buffer, size_t __size, size_t __offset, unsigned int __scale);
/**
 * Turn accounting on if NAME is an existing file.  The system will then write<br>
 * a record for each process as it terminates, to this file.  If NAME is NULL,<br>
 * turn accounting off.  This call is restricted to the super-user.<br>
 * Original signature : <code>int acct(const char*)</code>
 */
extern int acct(const char* __name);
/**
 * Successive calls return the shells listed in `/etc/shells'.<br>
 * Original signature : <code>char* getusershell()</code>
 */
extern char* getusershell();
/**
 * Discard cached info.<br>
 * Original signature : <code>void endusershell()</code>
 */
extern void endusershell();
/**
 * Rewind and re-read the file.<br>
 * Original signature : <code>void setusershell()</code>
 */
extern void setusershell();
/**
 * Put the program in the background, and dissociate from the controlling<br>
 * terminal.  If NOCHDIR is zero, do `chdir ("/")'.  If NOCLOSE is zero,<br>
 * redirects stdin, stdout, and stderr to /dev/null.<br>
 * Original signature : <code>int daemon(int, int)</code>
 */
extern int daemon(int __nochdir, int __noclose);
/**
 * Make PATH be the root directory (the starting point for absolute paths).<br>
 * This call is restricted to the super-user.<br>
 * Original signature : <code>int chroot(const char*)</code>
 */
extern int chroot(const char* __path);
/**
 * Prompt with PROMPT and read a string from the terminal without echoing.<br>
 * Uses /dev/tty if possible; otherwise stderr and stdin.<br>
 * Original signature : <code>char* getpass(const char*)</code>
 */
extern char* getpass(const char* __prompt);
/**
 * Make all changes done to FD actually appear on disk.<br>
 * This function is a cancellation point and therefore not marked with<br>
 * __THROW.<br>
 * Original signature : <code>int fsync(int)</code>
 */
extern int fsync(int __fd);
/**
 * Return identifier for the current host.<br>
 * Original signature : <code>long long gethostid()</code>
 */
extern long long gethostid();
/**
 * Make all changes done to all files actually appear on disk.<br>
 * Original signature : <code>void sync()</code>
 */
extern void sync();
/**
 * Return the number of bytes in a page.  This is the system's page size,<br>
 * which is not necessarily the same as the hardware page size.<br>
 * Original signature : <code>int getpagesize()</code>
 */
extern int getpagesize();
/**
 * Return the maximum number of file descriptors<br>
 * the current process could possibly have.<br>
 * Original signature : <code>int getdtablesize()</code>
 */
extern int getdtablesize();
/**
 * Truncate FILE to LENGTH bytes.<br>
 * Original signature : <code>int truncate(const char*, __off_t)</code>
 */
extern int truncate(const char* __file, __off_t __length);
/**
 * Truncate the file FD is open on to LENGTH bytes.<br>
 * Original signature : <code>int ftruncate(int, __off_t)</code>
 */
extern int ftruncate(int __fd, __off_t __length);
/**
 * Set the end of accessible data space (aka "the break") to ADDR.<br>
 * Returns zero on success and -1 for errors (with errno set).<br>
 * Original signature : <code>int brk(void*)</code>
 */
extern int brk(void* __addr);
/**
 * Increase or decrease the end of accessible data space by DELTA bytes.<br>
 * If successful, returns the address the previous end of data space<br>
 * (i.e. the beginning of the new space, if DELTA > 0);<br>
 * returns (void *) -1 for errors (with errno set).<br>
 * Original signature : <code>void* sbrk(intptr_t)</code>
 */
extern void* sbrk(intptr_t __delta);
/**
 * Invoke `system call' number SYSNO, passing it the remaining arguments.<br>
 * This is completely system-dependent, and not often useful.<br>
 * In Unix, `syscall' sets `errno' for all errors and most calls return -1<br>
 * for errors; in many systems you cannot pass arguments or get return<br>
 * values for all system calls (`pipe', `fork', and `getppid' typically<br>
 * among them).<br>
 * In Mach, all system calls take normal arguments and always return an<br>
 * error code (zero for success).<br>
 * Original signature : <code>long long syscall(long long, null)</code>
 */
extern long long syscall(long long __sysno, ...);
/**
 * `lockf' is a simpler interface to the locking facilities of `fcntl'.<br>
 * LEN is always relative to the current file position.<br>
 * The CMD argument is one of the following.<br>
 * This function is a cancellation point and therefore not marked with<br>
 * __THROW.<br>
 * Original signature : <code>int lockf(int, int, __off_t)</code>
 */
extern int lockf(int __fd, int __cmd, __off_t __len);
/**
 * Synchronize at least the data part of a file with the underlying<br>
 * media.<br>
 * Original signature : <code>int fdatasync(int)</code>
 */
extern int fdatasync(int __fildes);
/**
 * Write LENGTH bytes of randomness starting at BUFFER.  Return 0 on<br>
 * success or -1 on error.<br>
 * Original signature : <code>int getentropy(void*, size_t)</code>
 */
int getentropy(void* __buffer, size_t __length);












typedef __int8_t int8_t;
typedef __int16_t int16_t;
typedef __int32_t int32_t;
typedef __int64_t int64_t;


typedef __uint8_t uint8_t;
typedef __uint16_t uint16_t;
typedef __uint32_t uint32_t;
typedef __uint64_t uint64_t;
/** Signed. */
typedef signed char int_least8_t;
typedef short int_least16_t;
typedef int int_least32_t;
typedef long long int_least64_t;
/** Unsigned. */
typedef unsigned char uint_least8_t;
typedef unsigned short uint_least16_t;
typedef unsigned int uint_least32_t;
typedef unsigned long long uint_least64_t;
/** Signed. */
typedef signed char int_fast8_t;
typedef long long int_fast16_t;
typedef long long int_fast32_t;
typedef long long int_fast64_t;
/** Unsigned. */
typedef unsigned char uint_fast8_t;
typedef unsigned long long uint_fast16_t;
typedef unsigned long long uint_fast32_t;
typedef unsigned long long uint_fast64_t;
/** Types for `void *' pointers. */
typedef unsigned long long uintptr_t;
/** Largest integral types. */
typedef __intmax_t intmax_t;
typedef __uintmax_t uintmax_t;
extern ""C"" {
/** We have to define the `uintmax_t' type using `ldiv_t'. */
	typedef struct imaxdiv_t {
		long long quot; /* Quotient.  */
		long long rem; /* Remainder.  */
	} imaxdiv_t;
	/**
	 * Compute absolute value of N.<br>
	 * Original signature : <code>intmax_t imaxabs(intmax_t)</code>
	 */
	extern intmax_t imaxabs(intmax_t __n);
	/**
	 * Return the `imaxdiv_t' representation of the value of NUMER over DENOM.<br>
	 * Original signature : <code>imaxdiv_t imaxdiv(intmax_t, intmax_t)</code>
	 */
	extern imaxdiv_t imaxdiv(intmax_t __numer, intmax_t __denom);
	/**
	 * Like `strtol' but convert to `intmax_t'.<br>
	 * Original signature : <code>intmax_t strtoimax(const char*, char**, int)</code>
	 */
	extern intmax_t strtoimax(const char* __nptr, char** __endptr, int __base);
	/**
	 * Like `strtoul' but convert to `uintmax_t'.<br>
	 * Original signature : <code>uintmax_t strtoumax(const char*, char**, int)</code>
	 */
	extern uintmax_t strtoumax(const char* __nptr, char** __endptr, int __base);
	/**
	 * Like `wcstol' but convert to `intmax_t'.<br>
	 * Original signature : <code>intmax_t wcstoimax(const wchar_t*, wchar_t**, int)</code>
	 */
	extern intmax_t wcstoimax(const wchar_t* __nptr, wchar_t** __endptr, int __base);
	/**
	 * Like `wcstoul' but convert to `uintmax_t'.<br>
	 * Original signature : <code>uintmax_t wcstoumax(const wchar_t*, wchar_t**, int)</code>
	 */
	extern uintmax_t wcstoumax(const wchar_t* __nptr, wchar_t** __endptr, int __base);
}



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

/** Returned by `clock'. */
typedef __clock_t clock_t;


/** Clock ID used in clock and timer functions. */
typedef __clockid_t clockid_t;


/** Returned by `time'. */
typedef __time_t time_t;


/** Timer ID returned by `timer_create'. */
typedef __timer_t timer_t;

/** Old compatibility names for C types. */
typedef unsigned long long ulong;
typedef unsigned short ushort;
typedef unsigned int uint;
/** These were defined by ISO C without the first `_'. */
typedef unsigned char u_int8_t;
typedef unsigned short u_int16_t;
typedef unsigned int u_int32_t;
typedef unsigned long long u_int64_t;
typedef int register_t;









/**
 * Swap bytes in 32 bit value.<br>
 * Original signature : <code>__uint64_t __bswap_64(__uint64_t)</code>
 */
static __inline __uint64_t __bswap_64(__uint64_t __bsx) {
	return ((((__bsx) & 0xff00000000000000ull) >> 56) | (((__bsx) & 0x00ff000000000000ull) >> 40) | (((__bsx) & 0x0000ff0000000000ull) >> 24) | (((__bsx) & 0x000000ff00000000ull) >> 8) | (((__bsx) & 0x00000000ff000000ull) << 8) | (((__bsx) & 0x0000000000ff0000ull) << 24) | (((__bsx) & 0x000000000000ff00ull) << 40) | (((__bsx) & 0x00000000000000ffull) << 56));
}


/**
 * These inline functions are to ensure the appropriate type<br>
 * conversions and associated diagnostics from macros that convert to<br>
 * a given endianness.<br>
 * Original signature : <code>__uint16_t __uint16_identity(__uint16_t)</code>
 */
static __inline __uint16_t __uint16_identity(__uint16_t __x) {
	return __x;
}
/** Original signature : <code>__uint32_t __uint32_identity(__uint32_t)</code> */
static __inline __uint32_t __uint32_identity(__uint32_t __x) {
	return __x;
}
/** Original signature : <code>__uint64_t __uint64_identity(__uint64_t)</code> */
static __inline __uint64_t __uint64_identity(__uint64_t __x) {
	return __x;
}









typedef struct __sigset_t {
	unsigned long long[(1024 / (8 * sizeof(unsigned long long)))] __val;
} __sigset_t;
/** A set of signals to be blocked, unblocked, or waited for. */
typedef __sigset_t sigset_t;



/**
 * A time value that is accurate to the nearest<br>
 * microsecond but also has a range of years.
 */
struct timeval {
	__time_t tv_sec; /* Seconds.  */
	__suseconds_t tv_usec; /* Microseconds.  */
};


/**
 * POSIX.1b structure for a time value.  This is like a `struct timeval' but<br>
 * has nanoseconds instead of microseconds.
 */
struct timespec {
	__time_t tv_sec; /* Seconds.  */
	__syscall_slong_t tv_nsec; /* Nanoseconds.  */
};
typedef __suseconds_t suseconds_t;
/** The fd_set member is required to be an array of longs. */
typedef long long __fd_mask;
/** fd_set for select and pselect. */
typedef struct fd_set {
	__fd_mask[1024 / (8 * (int)sizeof(__fd_mask))] __fds_bits;
} fd_set;
/** Sometimes the fd_set member is assumed to have this type. */
typedef __fd_mask fd_mask;
extern ""C"" {
/**
	 * Check the first NFDS descriptors each in READFDS (if not NULL) for read<br>
	 * readiness, in WRITEFDS (if not NULL) for write readiness, and in EXCEPTFDS<br>
	 * (if not NULL) for exceptional conditions.  If TIMEOUT is not NULL, time out<br>
	 * after waiting the interval specified therein.  Returns the number of ready<br>
	 * descriptors, or -1 for errors.<br>
	 * This function is a cancellation point and therefore not marked with<br>
	 * __THROW.<br>
	 * Original signature : <code>int select(int, fd_set*, fd_set*, fd_set*, timeval*)</code>
	 */
	extern int select(int __nfds, fd_set* __readfds, fd_set* __writefds, fd_set* __exceptfds, timeval* __timeout);
	/**
	 * Same as above only that the TIMEOUT value is given with higher<br>
	 * resolution and a sigmask which is been set temporarily.  This version<br>
	 * should be used.<br>
	 * This function is a cancellation point and therefore not marked with<br>
	 * __THROW.<br>
	 * Original signature : <code>int pselect(int, fd_set*, fd_set*, fd_set*, timespec*, const __sigset_t*)</code>
	 */
	extern int pselect(int __nfds, fd_set* __readfds, fd_set* __writefds, fd_set* __exceptfds, timespec* __timeout, const __sigset_t* __sigmask);
}





extern ""C"" {
/** Original signature : <code>int gnu_dev_major(__dev_t)</code> */
	extern unsigned int gnu_dev_major(__dev_t __dev);
	/** Original signature : <code>int gnu_dev_minor(__dev_t)</code> */
	extern unsigned int gnu_dev_minor(__dev_t __dev);
	/** Original signature : <code>__dev_t gnu_dev_makedev(unsigned int, unsigned int)</code> */
	extern __dev_t gnu_dev_makedev(unsigned int __major, unsigned int __minor);
}
typedef __blksize_t blksize_t;
/** Types from the Large File Support interface. */
typedef __blkcnt_t blkcnt_t; /* Type to count number of disk blocks.  */
typedef __fsblkcnt_t fsblkcnt_t; /* Type to count file system blocks.  */
typedef __fsfilcnt_t fsfilcnt_t; /* Type to count file system inodes.  */



/** Definitions for internal mutex struct. */
struct __pthread_rwlock_arch_t {
	unsigned int __readers;
	unsigned int __writers;
	unsigned int __wrphase_futex;
	unsigned int __writers_futex;
	unsigned int __pad3;
	unsigned int __pad4;
	int __cur_writer;
	int __shared;
	signed char __rwelision;
	unsigned char[7] __pad1;
	unsigned long long __pad2;
	unsigned int __flags;
};
/** Common definition of pthread_mutex_t. */
typedef struct __pthread_list_t {
	__pthread_internal_list* __prev;
	__pthread_internal_list* __next;
} __pthread_internal_list;
/** Lock elision support. */
struct __pthread_mutex_s {
	int __lock;
	unsigned int __count;
	int __owner;
	unsigned int __nusers;
	int __kind;
	short __spins;
	short __elision;
	__pthread_list_t __list;
};
/** Common definition of pthread_cond_t. */
struct __pthread_cond_s {
	field1_union field1;
	field2_union field2;
	unsigned int[2] __g_refs;
	unsigned int[2] __g_size;
	unsigned int __g1_orig_size;
	unsigned int __wrefs;
	unsigned int[2] __g_signals;
	union  field1_union {
		unsigned long long long __wseq;
		__wseq32_struct __wseq32;
		struct __wseq32_struct {
			unsigned int __low;
			unsigned int __high;
		};
	};
	union  field2_union {
		unsigned long long long __g1_start;
		__g1_start32_struct __g1_start32;
		struct __g1_start32_struct {
			unsigned int __low;
			unsigned int __high;
		};
	};
};
/**
 * Thread identifiers.  The structure of the attribute type is not<br>
 * exposed on purpose.
 */
typedef unsigned long long pthread_t;
/**
 * Data structures for mutex handling.  The structure of the attribute<br>
 * type is not exposed on purpose.
 */
typedef union  pthread_mutexattr_t {
	char[4] __size;
	int __align;
} pthread_mutexattr_t;
/**
 * Data structure for condition variable handling.  The structure of<br>
 * the attribute type is not exposed on purpose.
 */
typedef union  pthread_condattr_t {
	char[4] __size;
	int __align;
} pthread_condattr_t;
/** Keys for thread-specific data */
typedef unsigned int pthread_key_t;
/** Once-only execution */
typedef int pthread_once_t;
union  pthread_attr_t {
	char[56] __size;
	long long __align;
};
typedef pthread_attr_t pthread_attr_t;
typedef union  pthread_mutex_t {
	__pthread_mutex_s __data;
	char[40] __size;
	long long __align;
} pthread_mutex_t;
typedef union  pthread_cond_t {
	__pthread_cond_s __data;
	char[48] __size;
	long long long __align;
} pthread_cond_t;
/**
 * Data structure for reader-writer lock variable handling.  The<br>
 * structure of the attribute type is deliberately not exposed.
 */
typedef union  pthread_rwlock_t {
	__pthread_rwlock_arch_t __data;
	char[56] __size;
	long long __align;
} pthread_rwlock_t;
typedef union  pthread_rwlockattr_t {
	char[8] __size;
	long long __align;
} pthread_rwlockattr_t;
/** POSIX spinlock data type. */
typedef volatile int pthread_spinlock_t;
/**
 * POSIX barriers data type.  The structure of the type is<br>
 * deliberately not exposed.
 */
typedef union  pthread_barrier_t {
	char[32] __size;
	long long __align;
} pthread_barrier_t;
typedef union  pthread_barrierattr_t {
	char[4] __size;
	int __align;
} pthread_barrierattr_t;


struct ssh_counter_struct {
	uint64_t in_bytes;
	uint64_t out_bytes;
	uint64_t in_packets;
	uint64_t out_packets;
};
typedef ssh_counter_struct* ssh_counter;
typedef ssh_agent_struct* ssh_agent;
typedef ssh_buffer_struct* ssh_buffer;
typedef ssh_channel_struct* ssh_channel;
typedef ssh_message_struct* ssh_message;
typedef ssh_pcap_file_struct* ssh_pcap_file;
typedef ssh_key_struct* ssh_key;
typedef ssh_scp_struct* ssh_scp;
typedef ssh_session_struct* ssh_session;
typedef ssh_string_struct* ssh_string;
typedef ssh_event_struct* ssh_event;
typedef ssh_connector_struct* ssh_connector;
typedef void* ssh_gssapi_creds;
/** Socket type */
typedef SOCKET socket_t;
/** the offsets of methods */
enum ssh_kex_types_e {
	SSH_KEX = 0,
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
	SSH_AUTH_SUCCESS = 0,
	SSH_AUTH_DENIED,
	SSH_AUTH_PARTIAL,
	SSH_AUTH_INFO,
	SSH_AUTH_AGAIN,
	SSH_AUTH_ERROR = -1
};
/** messages */
enum ssh_requests_e {
	SSH_REQUEST_AUTH = 1,
	SSH_REQUEST_CHANNEL_OPEN,
	SSH_REQUEST_CHANNEL,
	SSH_REQUEST_SERVICE,
	SSH_REQUEST_GLOBAL
};
enum ssh_channel_type_e {
	SSH_CHANNEL_UNKNOWN = 0,
	SSH_CHANNEL_SESSION,
	SSH_CHANNEL_DIRECT_TCPIP,
	SSH_CHANNEL_FORWARDED_TCPIP,
	SSH_CHANNEL_X11,
	SSH_CHANNEL_AUTH_AGENT
};
enum ssh_channel_requests_e {
	SSH_CHANNEL_REQUEST_UNKNOWN = 0,
	SSH_CHANNEL_REQUEST_PTY,
	SSH_CHANNEL_REQUEST_EXEC,
	SSH_CHANNEL_REQUEST_SHELL,
	SSH_CHANNEL_REQUEST_ENV,
	SSH_CHANNEL_REQUEST_SUBSYSTEM,
	SSH_CHANNEL_REQUEST_WINDOW_CHANGE,
	SSH_CHANNEL_REQUEST_X11
};
enum ssh_global_requests_e {
	SSH_GLOBAL_REQUEST_UNKNOWN = 0,
	SSH_GLOBAL_REQUEST_TCPIP_FORWARD,
	SSH_GLOBAL_REQUEST_CANCEL_TCPIP_FORWARD,
	SSH_GLOBAL_REQUEST_KEEPALIVE
};
enum ssh_publickey_state_e {
	SSH_PUBLICKEY_STATE_ERROR = -1,
	SSH_PUBLICKEY_STATE_NONE = 0,
	SSH_PUBLICKEY_STATE_VALID = 1,
	SSH_PUBLICKEY_STATE_WRONG = 2
};
/** Output buffer not empty */
enum ssh_server_known_e {
	SSH_SERVER_ERROR = -1,
	SSH_SERVER_NOT_KNOWN = 0,
	SSH_SERVER_KNOWN_OK,
	SSH_SERVER_KNOWN_CHANGED,
	SSH_SERVER_FOUND_OTHER,
	SSH_SERVER_FILE_NOT_FOUND
};
/** errors */
enum ssh_error_types_e {
	SSH_NO_ERROR = 0,
	SSH_REQUEST_DENIED,
	SSH_FATAL,
	SSH_EINTR
};
/** some types for keys */
enum ssh_keytypes_e {
	SSH_KEYTYPE_UNKNOWN = 0,
	SSH_KEYTYPE_DSS = 1,
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
/**
 * @addtogroup libssh_log<br>
 * @{
 */
enum {
	SSH_LOG_NOLOG = 0,
	SSH_LOG_WARNING,
	SSH_LOG_PROTOCOL,
	SSH_LOG_PACKET,
	SSH_LOG_FUNCTIONS
};
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
	SSH_OPTIONS_HMAC_S_C
};
enum {
	SSH_SCP_WRITE,
	SSH_SCP_READ,
	SSH_SCP_RECURSIVE = 0x10
};
enum ssh_scp_request_types {
	SSH_SCP_REQUEST_NEWDIR = 1,
	SSH_SCP_REQUEST_NEWFILE,
	SSH_SCP_REQUEST_EOF,
	SSH_SCP_REQUEST_ENDDIR,
	SSH_SCP_REQUEST_WARNING
};
enum ssh_connector_flags_e {
	SSH_CONNECTOR_STDOUT = 1,
	SSH_CONNECTOR_STDERR = 2,
	SSH_CONNECTOR_BOTH = 3
};
/** Original signature : <code>int ssh_blocking_flush(ssh_session, int)</code> */
int ssh_blocking_flush(ssh_session session, int timeout);
/** Original signature : <code>ssh_channel ssh_channel_accept_x11(ssh_channel, int)</code> */
ssh_channel ssh_channel_accept_x11(ssh_channel channel, int timeout_ms);
/** Original signature : <code>int ssh_channel_change_pty_size(ssh_channel, int, int)</code> */
int ssh_channel_change_pty_size(ssh_channel channel, int cols, int rows);
/** Original signature : <code>int ssh_channel_close(ssh_channel)</code> */
int ssh_channel_close(ssh_channel channel);
/** Original signature : <code>void ssh_channel_free(ssh_channel)</code> */
void ssh_channel_free(ssh_channel channel);
/** Original signature : <code>int ssh_channel_get_exit_status(ssh_channel)</code> */
int ssh_channel_get_exit_status(ssh_channel channel);
/** Original signature : <code>ssh_session ssh_channel_get_session(ssh_channel)</code> */
ssh_session ssh_channel_get_session(ssh_channel channel);
/** Original signature : <code>int ssh_channel_is_closed(ssh_channel)</code> */
int ssh_channel_is_closed(ssh_channel channel);
/** Original signature : <code>int ssh_channel_is_eof(ssh_channel)</code> */
int ssh_channel_is_eof(ssh_channel channel);
/** Original signature : <code>int ssh_channel_is_open(ssh_channel)</code> */
int ssh_channel_is_open(ssh_channel channel);
/** Original signature : <code>ssh_channel ssh_channel_new(ssh_session)</code> */
ssh_channel ssh_channel_new(ssh_session session);
/** Original signature : <code>int ssh_channel_open_auth_agent(ssh_channel)</code> */
int ssh_channel_open_auth_agent(ssh_channel channel);
/** Original signature : <code>int ssh_channel_open_forward(ssh_channel, const char*, int, const char*, int)</code> */
int ssh_channel_open_forward(ssh_channel channel, const char* remotehost, int remoteport, const char* sourcehost, int localport);
/** Original signature : <code>int ssh_channel_open_session(ssh_channel)</code> */
int ssh_channel_open_session(ssh_channel channel);
/** Original signature : <code>int ssh_channel_open_x11(ssh_channel, const char*, int)</code> */
int ssh_channel_open_x11(ssh_channel channel, const char* orig_addr, int orig_port);
/** Original signature : <code>int ssh_channel_poll(ssh_channel, int)</code> */
int ssh_channel_poll(ssh_channel channel, int is_stderr);
/** Original signature : <code>int ssh_channel_poll_timeout(ssh_channel, int, int)</code> */
int ssh_channel_poll_timeout(ssh_channel channel, int timeout, int is_stderr);
/** Original signature : <code>int ssh_channel_read(ssh_channel, void*, uint32_t, int)</code> */
int ssh_channel_read(ssh_channel channel, void* dest, uint32_t count, int is_stderr);
/** Original signature : <code>int ssh_channel_read_timeout(ssh_channel, void*, uint32_t, int, int)</code> */
int ssh_channel_read_timeout(ssh_channel channel, void* dest, uint32_t count, int is_stderr, int timeout_ms);
/** Original signature : <code>int ssh_channel_read_nonblocking(ssh_channel, void*, uint32_t, int)</code> */
int ssh_channel_read_nonblocking(ssh_channel channel, void* dest, uint32_t count, int is_stderr);
/** Original signature : <code>int ssh_channel_request_env(ssh_channel, const char*, const char*)</code> */
int ssh_channel_request_env(ssh_channel channel, const char* name, const char* value);
/** Original signature : <code>int ssh_channel_request_exec(ssh_channel, const char*)</code> */
int ssh_channel_request_exec(ssh_channel channel, const char* cmd);
/** Original signature : <code>int ssh_channel_request_pty(ssh_channel)</code> */
int ssh_channel_request_pty(ssh_channel channel);
/** Original signature : <code>int ssh_channel_request_pty_size(ssh_channel, const char*, int, int)</code> */
int ssh_channel_request_pty_size(ssh_channel channel, const char* term, int cols, int rows);
/** Original signature : <code>int ssh_channel_request_shell(ssh_channel)</code> */
int ssh_channel_request_shell(ssh_channel channel);
/** Original signature : <code>int ssh_channel_request_send_signal(ssh_channel, const char*)</code> */
int ssh_channel_request_send_signal(ssh_channel channel, const char* signum);
/** Original signature : <code>int ssh_channel_request_sftp(ssh_channel)</code> */
int ssh_channel_request_sftp(ssh_channel channel);
/** Original signature : <code>int ssh_channel_request_subsystem(ssh_channel, const char*)</code> */
int ssh_channel_request_subsystem(ssh_channel channel, const char* subsystem);
/** Original signature : <code>int ssh_channel_request_x11(ssh_channel, int, const char*, const char*, int)</code> */
int ssh_channel_request_x11(ssh_channel channel, int single_connection, const char* protocol, const char* cookie, int screen_number);
/** Original signature : <code>int ssh_channel_request_auth_agent(ssh_channel)</code> */
int ssh_channel_request_auth_agent(ssh_channel channel);
/** Original signature : <code>int ssh_channel_send_eof(ssh_channel)</code> */
int ssh_channel_send_eof(ssh_channel channel);
/** Original signature : <code>int ssh_channel_select(ssh_channel*, ssh_channel*, ssh_channel*, timeval*)</code> */
int ssh_channel_select(ssh_channel* readchans, ssh_channel* writechans, ssh_channel* exceptchans, timeval* timeout);
/** Original signature : <code>void ssh_channel_set_blocking(ssh_channel, int)</code> */
void ssh_channel_set_blocking(ssh_channel channel, int blocking);
/** Original signature : <code>void ssh_channel_set_counter(ssh_channel, ssh_counter)</code> */
void ssh_channel_set_counter(ssh_channel channel, ssh_counter counter);
/** Original signature : <code>int ssh_channel_write(ssh_channel, const void*, uint32_t)</code> */
int ssh_channel_write(ssh_channel channel, const void* data, uint32_t len);
/** Original signature : <code>int ssh_channel_write_stderr(ssh_channel, const void*, uint32_t)</code> */
int ssh_channel_write_stderr(ssh_channel channel, const void* data, uint32_t len);
/** Original signature : <code>uint32_t ssh_channel_window_size(ssh_channel)</code> */
uint32_t ssh_channel_window_size(ssh_channel channel);
/** Original signature : <code>char* ssh_basename(const char*)</code> */
char* ssh_basename(const char* path);
/** Original signature : <code>void ssh_clean_pubkey_hash(unsigned char**)</code> */
void ssh_clean_pubkey_hash(unsigned char** hash);
/** Original signature : <code>int ssh_connect(ssh_session)</code> */
int ssh_connect(ssh_session session);
/** Original signature : <code>ssh_connector ssh_connector_new(ssh_session)</code> */
ssh_connector ssh_connector_new(ssh_session session);
/** Original signature : <code>void ssh_connector_free(ssh_connector)</code> */
void ssh_connector_free(ssh_connector connector);
/** Original signature : <code>int ssh_connector_set_in_channel(ssh_connector, ssh_channel, ssh_connector_flags_e)</code> */
int ssh_connector_set_in_channel(ssh_connector connector, ssh_channel channel, ssh_connector_flags_e flags);
/** Original signature : <code>int ssh_connector_set_out_channel(ssh_connector, ssh_channel, ssh_connector_flags_e)</code> */
int ssh_connector_set_out_channel(ssh_connector connector, ssh_channel channel, ssh_connector_flags_e flags);
/** Original signature : <code>void ssh_connector_set_in_fd(ssh_connector, socket_t)</code> */
void ssh_connector_set_in_fd(ssh_connector connector, socket_t fd);
/** Original signature : <code>void ssh_connector_set_out_fd(ssh_connector, socket_t)</code> */
void ssh_connector_set_out_fd(ssh_connector connector, socket_t fd);
/** Original signature : <code>char* ssh_copyright()</code> */
const char* ssh_copyright();
/** Original signature : <code>void ssh_disconnect(ssh_session)</code> */
void ssh_disconnect(ssh_session session);
/** Original signature : <code>char* ssh_dirname(const char*)</code> */
char* ssh_dirname(const char* path);
/** Original signature : <code>int ssh_finalize()</code> */
int ssh_finalize();
/**
 * REVERSE PORT FORWARDING<br>
 * Original signature : <code>ssh_channel ssh_channel_accept_forward(ssh_session, int, int*)</code>
 */
ssh_channel ssh_channel_accept_forward(ssh_session session, int timeout_ms, int* destination_port);
/** Original signature : <code>int ssh_channel_cancel_forward(ssh_session, const char*, int)</code> */
int ssh_channel_cancel_forward(ssh_session session, const char* address, int port);
/** Original signature : <code>int ssh_channel_listen_forward(ssh_session, const char*, int, int*)</code> */
int ssh_channel_listen_forward(ssh_session session, const char* address, int port, int* bound_port);
/** Original signature : <code>void ssh_free(ssh_session)</code> */
void ssh_free(ssh_session session);
/** Original signature : <code>char* ssh_get_disconnect_message(ssh_session)</code> */
const char* ssh_get_disconnect_message(ssh_session session);
/** Original signature : <code>char* ssh_get_error(void*)</code> */
const char* ssh_get_error(void* error);
/** Original signature : <code>int ssh_get_error_code(void*)</code> */
int ssh_get_error_code(void* error);
/** Original signature : <code>socket_t ssh_get_fd(ssh_session)</code> */
socket_t ssh_get_fd(ssh_session session);
/** Original signature : <code>char* ssh_get_hexa(const unsigned char*, size_t)</code> */
char* ssh_get_hexa(const unsigned char* what, size_t len);
/** Original signature : <code>char* ssh_get_issue_banner(ssh_session)</code> */
char* ssh_get_issue_banner(ssh_session session);
/** Original signature : <code>int ssh_get_openssh_version(ssh_session)</code> */
int ssh_get_openssh_version(ssh_session session);
/** Original signature : <code>int ssh_get_server_publickey(ssh_session, ssh_key*)</code> */
int ssh_get_server_publickey(ssh_session session, ssh_key* key);
enum ssh_publickey_hash_type {
	SSH_PUBLICKEY_HASH_SHA1,
	SSH_PUBLICKEY_HASH_MD5
};
/** Original signature : <code>int ssh_get_publickey_hash(const ssh_key, ssh_publickey_hash_type, unsigned char**, size_t*)</code> */
int ssh_get_publickey_hash(const ssh_key key, ssh_publickey_hash_type type, unsigned char** hash, size_t* hlen);
/**
 * DEPRECATED FUNCTIONS<br>
 * Original signature : <code>int ssh_get_pubkey_hash(ssh_session, unsigned char**)</code>
 */
int ssh_get_pubkey_hash(ssh_session session, unsigned char** hash);
/** Original signature : <code>ssh_channel ssh_forward_accept(ssh_session, int)</code> */
ssh_channel ssh_forward_accept(ssh_session session, int timeout_ms);
/** Original signature : <code>int ssh_forward_cancel(ssh_session, const char*, int)</code> */
int ssh_forward_cancel(ssh_session session, const char* address, int port);
/** Original signature : <code>int ssh_forward_listen(ssh_session, const char*, int, int*)</code> */
int ssh_forward_listen(ssh_session session, const char* address, int port, int* bound_port);
/** Original signature : <code>int ssh_get_publickey(ssh_session, ssh_key*)</code> */
int ssh_get_publickey(ssh_session session, ssh_key* key);
/** Original signature : <code>int ssh_get_random(void*, int, strong int)</code> */
int ssh_get_random(void* where, int len, strong int int1);
/** Original signature : <code>int ssh_get_version(ssh_session)</code> */
int ssh_get_version(ssh_session session);
/** Original signature : <code>int ssh_get_status(ssh_session)</code> */
int ssh_get_status(ssh_session session);
/** Original signature : <code>int ssh_get_poll_flags(ssh_session)</code> */
int ssh_get_poll_flags(ssh_session session);
/** Original signature : <code>int ssh_init()</code> */
int ssh_init();
/** Original signature : <code>int ssh_is_blocking(ssh_session)</code> */
int ssh_is_blocking(ssh_session session);
/** Original signature : <code>int ssh_is_connected(ssh_session)</code> */
int ssh_is_connected(ssh_session session);
/** Original signature : <code>int ssh_is_server_known(ssh_session)</code> */
int ssh_is_server_known(ssh_session session);
/**
 * LOGGING<br>
 * Original signature : <code>int ssh_set_log_level(int)</code>
 */
int ssh_set_log_level(int level);
/** Original signature : <code>int ssh_get_log_level()</code> */
int ssh_get_log_level();
/** Original signature : <code>void* ssh_get_log_userdata()</code> */
void* ssh_get_log_userdata();
/** Original signature : <code>int ssh_set_log_userdata(void*)</code> */
int ssh_set_log_userdata(void* data);
/** Original signature : <code>void _ssh_log(int, const char*, const char*, null)</code> */
void _ssh_log(int verbosity, const char* function, const char* format, ...);
/**
 * legacy<br>
 * Original signature : <code>void ssh_log(ssh_session, int, const char*, null)</code>
 */
void ssh_log(ssh_session session, int prioriry, const char* format, ...);
/** Original signature : <code>ssh_channel ssh_message_channel_request_open_reply_accept(ssh_message)</code> */
ssh_channel ssh_message_channel_request_open_reply_accept(ssh_message msg);
/** Original signature : <code>int ssh_message_channel_request_reply_success(ssh_message)</code> */
int ssh_message_channel_request_reply_success(ssh_message msg);
/** Original signature : <code>void ssh_message_free(ssh_message)</code> */
void ssh_message_free(ssh_message msg);
/** Original signature : <code>ssh_message ssh_message_get(ssh_session)</code> */
ssh_message ssh_message_get(ssh_session session);
/** Original signature : <code>int ssh_message_subtype(ssh_message)</code> */
int ssh_message_subtype(ssh_message msg);
/** Original signature : <code>int ssh_message_type(ssh_message)</code> */
int ssh_message_type(ssh_message msg);
/** Original signature : <code>int ssh_mkdir(const char*, mode_t)</code> */
int ssh_mkdir(const char* pathname, mode_t mode);
/** Original signature : <code>ssh_session ssh_new()</code> */
ssh_session ssh_new();
/** Original signature : <code>int ssh_options_copy(ssh_session, ssh_session*)</code> */
int ssh_options_copy(ssh_session src, ssh_session* dest);
/** Original signature : <code>int ssh_options_getopt(ssh_session, int*, char**)</code> */
int ssh_options_getopt(ssh_session session, int* argcptr, char** argv);
/** Original signature : <code>int ssh_options_parse_config(ssh_session, const char*)</code> */
int ssh_options_parse_config(ssh_session session, const char* filename);
/** Original signature : <code>int ssh_options_set(ssh_session, ssh_options_e, const void*)</code> */
int ssh_options_set(ssh_session session, ssh_options_e type, const void* value);
/** Original signature : <code>int ssh_options_get(ssh_session, ssh_options_e, char**)</code> */
int ssh_options_get(ssh_session session, ssh_options_e type, char** value);
/** Original signature : <code>int ssh_options_get_port(ssh_session, unsigned int*)</code> */
int ssh_options_get_port(ssh_session session, unsigned int* port_target);
/** Original signature : <code>int ssh_pcap_file_close(ssh_pcap_file)</code> */
int ssh_pcap_file_close(ssh_pcap_file pcap);
/** Original signature : <code>void ssh_pcap_file_free(ssh_pcap_file)</code> */
void ssh_pcap_file_free(ssh_pcap_file pcap);
/** Original signature : <code>ssh_pcap_file ssh_pcap_file_new()</code> */
ssh_pcap_file ssh_pcap_file_new();
/** Original signature : <code>int ssh_pcap_file_open(ssh_pcap_file, const char*)</code> */
int ssh_pcap_file_open(ssh_pcap_file pcap, const char* filename);
/**
 * @brief SSH authentication callback.<br>
 * * @param prompt        Prompt to be displayed.<br>
 * @param buf           Buffer to save the password. You should null-terminate it.<br>
 * @param len           Length of the buffer.<br>
 * @param echo          Enable or disable the echo of what you type.<br>
 * @param verify        Should the password be verified?<br>
 * @param userdata      Userdata to be passed to the callback function. Useful<br>
 *                      for GUI applications.<br>
 * * @return              0 on success, < 0 on error.
 */
typedef int (*ssh_auth_callback)(const char* prompt, char* buf, size_t len, int echo, int verify, void* userdata) ssh_auth_callback;
/** Original signature : <code>ssh_key ssh_key_new()</code> */
ssh_key ssh_key_new();
/** Original signature : <code>void ssh_key_free(ssh_key)</code> */
void ssh_key_free(ssh_key key);
/** Original signature : <code>ssh_keytypes_e ssh_key_type(const ssh_key)</code> */
ssh_keytypes_e ssh_key_type(const ssh_key key);
/** Original signature : <code>char* ssh_key_type_to_char(ssh_keytypes_e)</code> */
const char* ssh_key_type_to_char(ssh_keytypes_e type);
/** Original signature : <code>ssh_keytypes_e ssh_key_type_from_name(const char*)</code> */
ssh_keytypes_e ssh_key_type_from_name(const char* name);
/** Original signature : <code>int ssh_key_is_public(const ssh_key)</code> */
int ssh_key_is_public(const ssh_key k);
/** Original signature : <code>int ssh_key_is_private(const ssh_key)</code> */
int ssh_key_is_private(const ssh_key k);
/** Original signature : <code>int ssh_key_cmp(const ssh_key, const ssh_key, ssh_keycmp_e)</code> */
int ssh_key_cmp(const ssh_key k1, const ssh_key k2, ssh_keycmp_e what);
/** Original signature : <code>int ssh_pki_generate(ssh_keytypes_e, int, ssh_key*)</code> */
int ssh_pki_generate(ssh_keytypes_e type, int parameter, ssh_key* pkey);
/** Original signature : <code>int ssh_pki_import_privkey_base64(const char*, const char*, ssh_auth_callback, void*, ssh_key*)</code> */
int ssh_pki_import_privkey_base64(const char* b64_key, const char* passphrase, ssh_auth_callback auth_fn, void* auth_data, ssh_key* pkey);
/** Original signature : <code>int ssh_pki_import_privkey_file(const char*, const char*, ssh_auth_callback, void*, ssh_key*)</code> */
int ssh_pki_import_privkey_file(const char* filename, const char* passphrase, ssh_auth_callback auth_fn, void* auth_data, ssh_key* pkey);
/** Original signature : <code>int ssh_pki_export_privkey_file(const ssh_key, const char*, ssh_auth_callback, void*, const char*)</code> */
int ssh_pki_export_privkey_file(const ssh_key privkey, const char* passphrase, ssh_auth_callback auth_fn, void* auth_data, const char* filename);
/** Original signature : <code>int ssh_pki_copy_cert_to_privkey(const ssh_key, ssh_key)</code> */
int ssh_pki_copy_cert_to_privkey(const ssh_key cert_key, ssh_key privkey);
/** Original signature : <code>int ssh_pki_import_pubkey_base64(const char*, ssh_keytypes_e, ssh_key*)</code> */
int ssh_pki_import_pubkey_base64(const char* b64_key, ssh_keytypes_e type, ssh_key* pkey);
/** Original signature : <code>int ssh_pki_import_pubkey_file(const char*, ssh_key*)</code> */
int ssh_pki_import_pubkey_file(const char* filename, ssh_key* pkey);
/** Original signature : <code>int ssh_pki_import_cert_base64(const char*, ssh_keytypes_e, ssh_key*)</code> */
int ssh_pki_import_cert_base64(const char* b64_cert, ssh_keytypes_e type, ssh_key* pkey);
/** Original signature : <code>int ssh_pki_import_cert_file(const char*, ssh_key*)</code> */
int ssh_pki_import_cert_file(const char* filename, ssh_key* pkey);
/** Original signature : <code>int ssh_pki_export_privkey_to_pubkey(const ssh_key, ssh_key*)</code> */
int ssh_pki_export_privkey_to_pubkey(const ssh_key privkey, ssh_key* pkey);
/** Original signature : <code>int ssh_pki_export_pubkey_base64(const ssh_key, char**)</code> */
int ssh_pki_export_pubkey_base64(const ssh_key key, char** b64_key);
/** Original signature : <code>int ssh_pki_export_pubkey_file(const ssh_key, const char*)</code> */
int ssh_pki_export_pubkey_file(const ssh_key key, const char* filename);
/** Original signature : <code>char* ssh_pki_key_ecdsa_name(const ssh_key)</code> */
const char* ssh_pki_key_ecdsa_name(const ssh_key key);
/** Original signature : <code>void ssh_print_hexa(const char*, const unsigned char*, size_t)</code> */
void ssh_print_hexa(const char* descr, const unsigned char* what, size_t len);
/** Original signature : <code>int ssh_send_ignore(ssh_session, const char*)</code> */
int ssh_send_ignore(ssh_session session, const char* data);
/** Original signature : <code>int ssh_send_debug(ssh_session, const char*, int)</code> */
int ssh_send_debug(ssh_session session, const char* message, int always_display);
/** Original signature : <code>void ssh_gssapi_set_creds(ssh_session, const ssh_gssapi_creds)</code> */
void ssh_gssapi_set_creds(ssh_session session, const ssh_gssapi_creds creds);
/** Original signature : <code>int ssh_scp_accept_request(ssh_scp)</code> */
int ssh_scp_accept_request(ssh_scp scp);
/** Original signature : <code>int ssh_scp_close(ssh_scp)</code> */
int ssh_scp_close(ssh_scp scp);
/** Original signature : <code>int ssh_scp_deny_request(ssh_scp, const char*)</code> */
int ssh_scp_deny_request(ssh_scp scp, const char* reason);
/** Original signature : <code>void ssh_scp_free(ssh_scp)</code> */
void ssh_scp_free(ssh_scp scp);
/** Original signature : <code>int ssh_scp_init(ssh_scp)</code> */
int ssh_scp_init(ssh_scp scp);
/** Original signature : <code>int ssh_scp_leave_directory(ssh_scp)</code> */
int ssh_scp_leave_directory(ssh_scp scp);
/** Original signature : <code>ssh_scp ssh_scp_new(ssh_session, int, const char*)</code> */
ssh_scp ssh_scp_new(ssh_session session, int mode, const char* location);
/** Original signature : <code>int ssh_scp_pull_request(ssh_scp)</code> */
int ssh_scp_pull_request(ssh_scp scp);
/** Original signature : <code>int ssh_scp_push_directory(ssh_scp, const char*, int)</code> */
int ssh_scp_push_directory(ssh_scp scp, const char* dirname, int mode);
/** Original signature : <code>int ssh_scp_push_file(ssh_scp, const char*, size_t, int)</code> */
int ssh_scp_push_file(ssh_scp scp, const char* filename, size_t size, int perms);
/** Original signature : <code>int ssh_scp_push_file64(ssh_scp, const char*, uint64_t, int)</code> */
int ssh_scp_push_file64(ssh_scp scp, const char* filename, uint64_t size, int perms);
/** Original signature : <code>int ssh_scp_read(ssh_scp, void*, size_t)</code> */
int ssh_scp_read(ssh_scp scp, void* buffer, size_t size);
/** Original signature : <code>char* ssh_scp_request_get_filename(ssh_scp)</code> */
const char* ssh_scp_request_get_filename(ssh_scp scp);
/** Original signature : <code>int ssh_scp_request_get_permissions(ssh_scp)</code> */
int ssh_scp_request_get_permissions(ssh_scp scp);
/** Original signature : <code>size_t ssh_scp_request_get_size(ssh_scp)</code> */
size_t ssh_scp_request_get_size(ssh_scp scp);
/** Original signature : <code>uint64_t ssh_scp_request_get_size64(ssh_scp)</code> */
uint64_t ssh_scp_request_get_size64(ssh_scp scp);
/** Original signature : <code>char* ssh_scp_request_get_warning(ssh_scp)</code> */
const char* ssh_scp_request_get_warning(ssh_scp scp);
/** Original signature : <code>int ssh_scp_write(ssh_scp, const void*, size_t)</code> */
int ssh_scp_write(ssh_scp scp, const void* buffer, size_t len);
/** Original signature : <code>int ssh_select(ssh_channel*, ssh_channel*, socket_t, fd_set*, timeval*)</code> */
int ssh_select(ssh_channel* channels, ssh_channel* outchannels, socket_t maxfd, fd_set* readfds, timeval* timeout);
/** Original signature : <code>int ssh_service_request(ssh_session, const char*)</code> */
int ssh_service_request(ssh_session session, const char* service);
/** Original signature : <code>int ssh_set_agent_channel(ssh_session, ssh_channel)</code> */
int ssh_set_agent_channel(ssh_session session, ssh_channel channel);
/** Original signature : <code>int ssh_set_agent_socket(ssh_session, socket_t)</code> */
int ssh_set_agent_socket(ssh_session session, socket_t fd);
/** Original signature : <code>void ssh_set_blocking(ssh_session, int)</code> */
void ssh_set_blocking(ssh_session session, int blocking);
/** Original signature : <code>void ssh_set_counters(ssh_session, ssh_counter, ssh_counter)</code> */
void ssh_set_counters(ssh_session session, ssh_counter scounter, ssh_counter rcounter);
/** Original signature : <code>void ssh_set_fd_except(ssh_session)</code> */
void ssh_set_fd_except(ssh_session session);
/** Original signature : <code>void ssh_set_fd_toread(ssh_session)</code> */
void ssh_set_fd_toread(ssh_session session);
/** Original signature : <code>void ssh_set_fd_towrite(ssh_session)</code> */
void ssh_set_fd_towrite(ssh_session session);
/** Original signature : <code>void ssh_silent_disconnect(ssh_session)</code> */
void ssh_silent_disconnect(ssh_session session);
/** Original signature : <code>int ssh_set_pcap_file(ssh_session, ssh_pcap_file)</code> */
int ssh_set_pcap_file(ssh_session session, ssh_pcap_file pcapfile);
/**
 * USERAUTH<br>
 * Original signature : <code>int ssh_userauth_none(ssh_session, const char*)</code>
 */
int ssh_userauth_none(ssh_session session, const char* username);
/** Original signature : <code>int ssh_userauth_list(ssh_session, const char*)</code> */
int ssh_userauth_list(ssh_session session, const char* username);
/** Original signature : <code>int ssh_userauth_try_publickey(ssh_session, const char*, const ssh_key)</code> */
int ssh_userauth_try_publickey(ssh_session session, const char* username, const ssh_key pubkey);
/** Original signature : <code>int ssh_userauth_publickey(ssh_session, const char*, const ssh_key)</code> */
int ssh_userauth_publickey(ssh_session session, const char* username, const ssh_key privkey);
/** Original signature : <code>int ssh_userauth_publickey_auto(ssh_session, const char*, const char*)</code> */
int ssh_userauth_publickey_auto(ssh_session session, const char* username, const char* passphrase);
/** Original signature : <code>int ssh_userauth_password(ssh_session, const char*, const char*)</code> */
int ssh_userauth_password(ssh_session session, const char* username, const char* password);
/** Original signature : <code>int ssh_userauth_kbdint(ssh_session, const char*, const char*)</code> */
int ssh_userauth_kbdint(ssh_session session, const char* user, const char* submethods);
/** Original signature : <code>char* ssh_userauth_kbdint_getinstruction(ssh_session)</code> */
const char* ssh_userauth_kbdint_getinstruction(ssh_session session);
/** Original signature : <code>char* ssh_userauth_kbdint_getname(ssh_session)</code> */
const char* ssh_userauth_kbdint_getname(ssh_session session);
/** Original signature : <code>int ssh_userauth_kbdint_getnprompts(ssh_session)</code> */
int ssh_userauth_kbdint_getnprompts(ssh_session session);
/** Original signature : <code>char* ssh_userauth_kbdint_getprompt(ssh_session, unsigned int, char*)</code> */
const char* ssh_userauth_kbdint_getprompt(ssh_session session, unsigned int i, char* echo);
/** Original signature : <code>int ssh_userauth_kbdint_getnanswers(ssh_session)</code> */
int ssh_userauth_kbdint_getnanswers(ssh_session session);
/** Original signature : <code>char* ssh_userauth_kbdint_getanswer(ssh_session, unsigned int)</code> */
const char* ssh_userauth_kbdint_getanswer(ssh_session session, unsigned int i);
/** Original signature : <code>int ssh_userauth_kbdint_setanswer(ssh_session, unsigned int, const char*)</code> */
int ssh_userauth_kbdint_setanswer(ssh_session session, unsigned int i, const char* answer);
/** Original signature : <code>int ssh_userauth_gssapi(ssh_session)</code> */
int ssh_userauth_gssapi(ssh_session session);
/** Original signature : <code>char* ssh_version(int)</code> */
const char* ssh_version(int req_version);
/** Original signature : <code>int ssh_write_knownhost(ssh_session)</code> */
int ssh_write_knownhost(ssh_session session);
/** Original signature : <code>char* ssh_dump_knownhost(ssh_session)</code> */
char* ssh_dump_knownhost(ssh_session session);
/** Original signature : <code>void ssh_string_burn(ssh_string)</code> */
void ssh_string_burn(ssh_string str);
/** Original signature : <code>ssh_string ssh_string_copy(ssh_string)</code> */
ssh_string ssh_string_copy(ssh_string str);
/** Original signature : <code>void* ssh_string_data(ssh_string)</code> */
void* ssh_string_data(ssh_string str);
/** Original signature : <code>int ssh_string_fill(ssh_string, const void*, size_t)</code> */
int ssh_string_fill(ssh_string str, const void* data, size_t len);
/** Original signature : <code>void ssh_string_free(ssh_string)</code> */
void ssh_string_free(ssh_string str);
/** Original signature : <code>ssh_string ssh_string_from_char(const char*)</code> */
ssh_string ssh_string_from_char(const char* what);
/** Original signature : <code>size_t ssh_string_len(ssh_string)</code> */
size_t ssh_string_len(ssh_string str);
/** Original signature : <code>ssh_string ssh_string_new(size_t)</code> */
ssh_string ssh_string_new(size_t size);
/** Original signature : <code>char* ssh_string_get_char(ssh_string)</code> */
const char* ssh_string_get_char(ssh_string str);
/** Original signature : <code>char* ssh_string_to_char(ssh_string)</code> */
char* ssh_string_to_char(ssh_string str);
/** Original signature : <code>void ssh_string_free_char(char*)</code> */
void ssh_string_free_char(char* s);
/** Original signature : <code>int ssh_getpass(const char*, char*, size_t, int, int)</code> */
int ssh_getpass(const char* prompt, char* buf, size_t len, int echo, int verify);
typedef int (*ssh_event_callback)(socket_t fd, int revents, void* userdata) ssh_event_callback;
/** Original signature : <code>ssh_event ssh_event_new()</code> */
ssh_event ssh_event_new();
/** Original signature : <code>int ssh_event_add_fd(ssh_event, socket_t, short, ssh_event_callback, void*)</code> */
int ssh_event_add_fd(ssh_event event, socket_t fd, short events, ssh_event_callback cb, void* userdata);
/** Original signature : <code>int ssh_event_add_session(ssh_event, ssh_session)</code> */
int ssh_event_add_session(ssh_event event, ssh_session session);
/** Original signature : <code>int ssh_event_add_connector(ssh_event, ssh_connector)</code> */
int ssh_event_add_connector(ssh_event event, ssh_connector connector);
/** Original signature : <code>int ssh_event_dopoll(ssh_event, int)</code> */
int ssh_event_dopoll(ssh_event event, int timeout);
/** Original signature : <code>int ssh_event_remove_fd(ssh_event, socket_t)</code> */
int ssh_event_remove_fd(ssh_event event, socket_t fd);
/** Original signature : <code>int ssh_event_remove_session(ssh_event, ssh_session)</code> */
int ssh_event_remove_session(ssh_event event, ssh_session session);
/** Original signature : <code>int ssh_event_remove_connector(ssh_event, ssh_connector)</code> */
int ssh_event_remove_connector(ssh_event event, ssh_connector connector);
/** Original signature : <code>void ssh_event_free(ssh_event)</code> */
void ssh_event_free(ssh_event event);
/** Original signature : <code>char* ssh_get_clientbanner(ssh_session)</code> */
const char* ssh_get_clientbanner(ssh_session session);
/** Original signature : <code>char* ssh_get_serverbanner(ssh_session)</code> */
const char* ssh_get_serverbanner(ssh_session session);
/** Original signature : <code>char* ssh_get_kex_algo(ssh_session)</code> */
const char* ssh_get_kex_algo(ssh_session session);
/** Original signature : <code>char* ssh_get_cipher_in(ssh_session)</code> */
const char* ssh_get_cipher_in(ssh_session session);
/** Original signature : <code>char* ssh_get_cipher_out(ssh_session)</code> */
const char* ssh_get_cipher_out(ssh_session session);
/** Original signature : <code>char* ssh_get_hmac_in(ssh_session)</code> */
const char* ssh_get_hmac_in(ssh_session session);
/** Original signature : <code>char* ssh_get_hmac_out(ssh_session)</code> */
const char* ssh_get_hmac_out(ssh_session session);
/** Original signature : <code>ssh_buffer ssh_buffer_new()</code> */
ssh_buffer ssh_buffer_new();
/** Original signature : <code>void ssh_buffer_free(ssh_buffer)</code> */
void ssh_buffer_free(ssh_buffer buffer);
/** Original signature : <code>int ssh_buffer_reinit(ssh_buffer)</code> */
int ssh_buffer_reinit(ssh_buffer buffer);
/** Original signature : <code>int ssh_buffer_add_data(ssh_buffer, const void*, uint32_t)</code> */
int ssh_buffer_add_data(ssh_buffer buffer, const void* data, uint32_t len);
/** Original signature : <code>uint32_t ssh_buffer_get_data(ssh_buffer, void*, uint32_t)</code> */
uint32_t ssh_buffer_get_data(ssh_buffer buffer, void* data, uint32_t requestedlen);
/** Original signature : <code>void* ssh_buffer_get(ssh_buffer)</code> */
void* ssh_buffer_get(ssh_buffer buffer);
/** Original signature : <code>uint32_t ssh_buffer_get_len(ssh_buffer)</code> */
uint32_t ssh_buffer_get_len(ssh_buffer buffer);
enum ssh_connector_flags_e {
};
enum ssh_connector_flags_e {
};
enum ssh_publickey_hash_type {
};
enum ssh_options_e {
};
enum ssh_options_e {
};
enum ssh_keytypes_e {
};
enum ssh_keytypes_e {
};
enum ssh_keytypes_e {
};
enum ssh_keycmp_e {
};
enum ssh_keytypes_e {
};
enum ssh_keytypes_e {
};
enum ssh_keytypes_e {
};
/**
 * Since libssh.h includes legacy.h, it's important that libssh.h is included<br>
 * first. we don't define LEGACY_H now because we want it to be defined when<br>
 * included from libssh.h<br>
 * All function calls declared in this header are deprecated and meant to be<br>
 * removed in future.
 */
typedef ssh_private_key_struct* ssh_private_key;
typedef ssh_public_key_struct* ssh_public_key;
/** Original signature : <code>int ssh_auth_list(ssh_session)</code> */
int ssh_auth_list(ssh_session session);
/** Original signature : <code>int ssh_userauth_offer_pubkey(ssh_session, const char*, int, ssh_string)</code> */
int ssh_userauth_offer_pubkey(ssh_session session, const char* username, int type, ssh_string publickey);
/** Original signature : <code>int ssh_userauth_pubkey(ssh_session, const char*, ssh_string, ssh_private_key)</code> */
int ssh_userauth_pubkey(ssh_session session, const char* username, ssh_string publickey, ssh_private_key privatekey);
/** Original signature : <code>int ssh_userauth_autopubkey(ssh_session, const char*)</code> */
int ssh_userauth_autopubkey(ssh_session session, const char* passphrase);
/** Original signature : <code>int ssh_userauth_privatekey_file(ssh_session, const char*, const char*, const char*)</code> */
int ssh_userauth_privatekey_file(ssh_session session, const char* username, const char* filename, const char* passphrase);
/** Original signature : <code>void buffer_free(ssh_buffer)</code> */
void buffer_free(ssh_buffer buffer);
/** Original signature : <code>void* buffer_get(ssh_buffer)</code> */
void* buffer_get(ssh_buffer buffer);
/** Original signature : <code>uint32_t buffer_get_len(ssh_buffer)</code> */
uint32_t buffer_get_len(ssh_buffer buffer);
/** Original signature : <code>ssh_buffer buffer_new()</code> */
ssh_buffer buffer_new();
/** Original signature : <code>ssh_channel channel_accept_x11(ssh_channel, int)</code> */
ssh_channel channel_accept_x11(ssh_channel channel, int timeout_ms);
/** Original signature : <code>int channel_change_pty_size(ssh_channel, int, int)</code> */
int channel_change_pty_size(ssh_channel channel, int cols, int rows);
/** Original signature : <code>ssh_channel channel_forward_accept(ssh_session, int)</code> */
ssh_channel channel_forward_accept(ssh_session session, int timeout_ms);
/** Original signature : <code>int channel_close(ssh_channel)</code> */
int channel_close(ssh_channel channel);
/** Original signature : <code>int channel_forward_cancel(ssh_session, const char*, int)</code> */
int channel_forward_cancel(ssh_session session, const char* address, int port);
/** Original signature : <code>int channel_forward_listen(ssh_session, const char*, int, int*)</code> */
int channel_forward_listen(ssh_session session, const char* address, int port, int* bound_port);
/** Original signature : <code>void channel_free(ssh_channel)</code> */
void channel_free(ssh_channel channel);
/** Original signature : <code>int channel_get_exit_status(ssh_channel)</code> */
int channel_get_exit_status(ssh_channel channel);
/** Original signature : <code>ssh_session channel_get_session(ssh_channel)</code> */
ssh_session channel_get_session(ssh_channel channel);
/** Original signature : <code>int channel_is_closed(ssh_channel)</code> */
int channel_is_closed(ssh_channel channel);
/** Original signature : <code>int channel_is_eof(ssh_channel)</code> */
int channel_is_eof(ssh_channel channel);
/** Original signature : <code>int channel_is_open(ssh_channel)</code> */
int channel_is_open(ssh_channel channel);
/** Original signature : <code>ssh_channel channel_new(ssh_session)</code> */
ssh_channel channel_new(ssh_session session);
/** Original signature : <code>int channel_open_forward(ssh_channel, const char*, int, const char*, int)</code> */
int channel_open_forward(ssh_channel channel, const char* remotehost, int remoteport, const char* sourcehost, int localport);
/** Original signature : <code>int channel_open_session(ssh_channel)</code> */
int channel_open_session(ssh_channel channel);
/** Original signature : <code>int channel_poll(ssh_channel, int)</code> */
int channel_poll(ssh_channel channel, int is_stderr);
/** Original signature : <code>int channel_read(ssh_channel, void*, uint32_t, int)</code> */
int channel_read(ssh_channel channel, void* dest, uint32_t count, int is_stderr);
/** Original signature : <code>int channel_read_buffer(ssh_channel, ssh_buffer, uint32_t, int)</code> */
int channel_read_buffer(ssh_channel channel, ssh_buffer buffer, uint32_t count, int is_stderr);
/** Original signature : <code>int channel_read_nonblocking(ssh_channel, void*, uint32_t, int)</code> */
int channel_read_nonblocking(ssh_channel channel, void* dest, uint32_t count, int is_stderr);
/** Original signature : <code>int channel_request_env(ssh_channel, const char*, const char*)</code> */
int channel_request_env(ssh_channel channel, const char* name, const char* value);
/** Original signature : <code>int channel_request_exec(ssh_channel, const char*)</code> */
int channel_request_exec(ssh_channel channel, const char* cmd);
/** Original signature : <code>int channel_request_pty(ssh_channel)</code> */
int channel_request_pty(ssh_channel channel);
/** Original signature : <code>int channel_request_pty_size(ssh_channel, const char*, int, int)</code> */
int channel_request_pty_size(ssh_channel channel, const char* term, int cols, int rows);
/** Original signature : <code>int channel_request_shell(ssh_channel)</code> */
int channel_request_shell(ssh_channel channel);
/** Original signature : <code>int channel_request_send_signal(ssh_channel, const char*)</code> */
int channel_request_send_signal(ssh_channel channel, const char* signum);
/** Original signature : <code>int channel_request_sftp(ssh_channel)</code> */
int channel_request_sftp(ssh_channel channel);
/** Original signature : <code>int channel_request_subsystem(ssh_channel, const char*)</code> */
int channel_request_subsystem(ssh_channel channel, const char* subsystem);
/** Original signature : <code>int channel_request_x11(ssh_channel, int, const char*, const char*, int)</code> */
int channel_request_x11(ssh_channel channel, int single_connection, const char* protocol, const char* cookie, int screen_number);
/** Original signature : <code>int channel_send_eof(ssh_channel)</code> */
int channel_send_eof(ssh_channel channel);
/** Original signature : <code>int channel_select(ssh_channel*, ssh_channel*, ssh_channel*, timeval*)</code> */
int channel_select(ssh_channel* readchans, ssh_channel* writechans, ssh_channel* exceptchans, timeval* timeout);
/** Original signature : <code>void channel_set_blocking(ssh_channel, int)</code> */
void channel_set_blocking(ssh_channel channel, int blocking);
/** Original signature : <code>int channel_write(ssh_channel, const void*, uint32_t)</code> */
int channel_write(ssh_channel channel, const void* data, uint32_t len);
/** Original signature : <code>void privatekey_free(ssh_private_key)</code> */
void privatekey_free(ssh_private_key prv);
/** Original signature : <code>ssh_private_key privatekey_from_file(ssh_session, const char*, int, const char*)</code> */
ssh_private_key privatekey_from_file(ssh_session session, const char* filename, int type, const char* passphrase);
/** Original signature : <code>void publickey_free(ssh_public_key)</code> */
void publickey_free(ssh_public_key key);
/** Original signature : <code>int ssh_publickey_to_file(ssh_session, const char*, ssh_string, int)</code> */
int ssh_publickey_to_file(ssh_session session, const char* file, ssh_string pubkey, int type);
/** Original signature : <code>ssh_string publickey_from_file(ssh_session, const char*, int*)</code> */
ssh_string publickey_from_file(ssh_session session, const char* filename, int* type);
/** Original signature : <code>ssh_public_key publickey_from_privatekey(ssh_private_key)</code> */
ssh_public_key publickey_from_privatekey(ssh_private_key prv);
/** Original signature : <code>ssh_string publickey_to_string(ssh_public_key)</code> */
ssh_string publickey_to_string(ssh_public_key key);
/** Original signature : <code>int ssh_try_publickey_from_file(ssh_session, const char*, ssh_string*, int*)</code> */
int ssh_try_publickey_from_file(ssh_session session, const char* keyfile, ssh_string* publickey, int* type);
/** Original signature : <code>ssh_keytypes_e ssh_privatekey_type(ssh_private_key)</code> */
ssh_keytypes_e ssh_privatekey_type(ssh_private_key privatekey);
/** Original signature : <code>ssh_string ssh_get_pubkey(ssh_session)</code> */
ssh_string ssh_get_pubkey(ssh_session session);
/** Original signature : <code>ssh_message ssh_message_retrieve(ssh_session, uint32_t)</code> */
ssh_message ssh_message_retrieve(ssh_session session, uint32_t packettype);
/** Original signature : <code>ssh_public_key ssh_message_auth_publickey(ssh_message)</code> */
ssh_public_key ssh_message_auth_publickey(ssh_message msg);
/** Original signature : <code>void string_burn(ssh_string)</code> */
void string_burn(ssh_string str);
/** Original signature : <code>ssh_string string_copy(ssh_string)</code> */
ssh_string string_copy(ssh_string str);
/** Original signature : <code>void* string_data(ssh_string)</code> */
void* string_data(ssh_string str);
/** Original signature : <code>int string_fill(ssh_string, const void*, size_t)</code> */
int string_fill(ssh_string str, const void* data, size_t len);
/** Original signature : <code>void string_free(ssh_string)</code> */
void string_free(ssh_string str);
/** Original signature : <code>ssh_string string_from_char(const char*)</code> */
ssh_string string_from_char(const char* what);
/** Original signature : <code>size_t string_len(ssh_string)</code> */
size_t string_len(ssh_string str);
/** Original signature : <code>ssh_string string_new(size_t)</code> */
ssh_string string_new(size_t size);
/** Original signature : <code>char* string_to_char(ssh_string)</code> */
char* string_to_char(ssh_string str);
enum ssh_keytypes_e {
};




extern ""C"" {
typedef uint32_t uid_t;
	typedef uint32_t gid_t;
	typedef sftp_attributes_struct* sftp_attributes;
	typedef sftp_client_message_struct* sftp_client_message;
	typedef sftp_dir_struct* sftp_dir;
	typedef sftp_ext_struct* sftp_ext;
	typedef sftp_file_struct* sftp_file;
	typedef sftp_message_struct* sftp_message;
	typedef sftp_packet_struct* sftp_packet;
	typedef sftp_request_queue_struct* sftp_request_queue;
	typedef sftp_session_struct* sftp_session;
	typedef sftp_status_message_struct* sftp_status_message;
	typedef sftp_statvfs_struct* sftp_statvfs_t;
	struct sftp_session_struct {
		ssh_session session;
		ssh_channel channel;
		int server_version;
		int client_version;
		int version;
		sftp_request_queue queue;
		uint32_t id_counter;
		int errnum;
		void** handles;
		sftp_ext ext;
	};
	struct sftp_packet_struct {
		sftp_session sftp;
		uint8_t type;
		ssh_buffer payload;
	};
	/** file handler */
	struct sftp_file_struct {
		sftp_session sftp;
		char* name;
		uint64_t offset;
		ssh_string handle;
		int eof;
		int nonblocking;
	};
	struct sftp_dir_struct {
		sftp_session sftp;
		char* name;
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
	/** this is a bunch of all data that could be into a message */
	struct sftp_client_message_struct {
		sftp_session sftp;
		uint8_t type;
		uint32_t id;
		char* filename; /* can be "path" */
		uint32_t flags;
		sftp_attributes attr;
		ssh_string handle;
		uint64_t offset;
		uint32_t len;
		int attr_num;
		ssh_buffer attrbuf; /* used by sftp_reply_attrs */
		ssh_string data; /* can be newpath of rename() */
		ssh_buffer complete_message; /* complete message in case of retransmission*/
		char* str_data; /* cstring version of data */
	};
	struct sftp_request_queue_struct {
		sftp_request_queue next;
		sftp_message message;
	};
	/** SSH_FXP_MESSAGE described into .7 page 26 */
	struct sftp_status_message_struct {
		uint32_t id;
		uint32_t status;
		ssh_string error_unused; /* not used anymore */
		ssh_string lang_unused; /* not used anymore */
		char* errormsg;
		char* langmsg;
	};
	struct sftp_attributes_struct {
		char* name;
		char* longname; /* ls -l output on openssh, not reliable else */
		uint32_t flags;
		uint8_t type;
		uint64_t size;
		uint32_t uid;
		uint32_t gid;
		char* owner; /* set if openssh and version 4 */
		char* group; /* set if openssh and version 4 */
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
	/** @brief SFTP statvfs structure. */
	struct sftp_statvfs_struct {
		uint64_t f_bsize; /** file system block size */
		uint64_t f_frsize; /** fundamental fs block size */
		uint64_t f_blocks; /** number of blocks (unit f_frsize) */
		uint64_t f_bfree; /** free blocks in file system */
		uint64_t f_bavail; /** free blocks for non-root */
		uint64_t f_files; /** total file inodes */
		uint64_t f_ffree; /** free file inodes */
		uint64_t f_favail; /** free file inodes for to non-root */
		uint64_t f_fsid; /** file system id */
		uint64_t f_flag; /** bit mask of f_flag values */
		uint64_t f_namemax; /** maximum filename length */
	};
	/**
	 * @brief Start a new sftp session.<br>
	 * * @param session       The ssh session to use.<br>
	 * * @return              A new sftp session or NULL on error.<br>
	 * * @see sftp_free()<br>
	 * Original signature : <code>sftp_session sftp_new(ssh_session)</code>
	 */
	sftp_session sftp_new(ssh_session session);
	/**
	 * @brief Start a new sftp session with an existing channel.<br>
	 * * @param session       The ssh session to use.<br>
	 * @param channel		An open session channel with subsystem already allocated<br>
	 * * @return              A new sftp session or NULL on error.<br>
	 * * @see sftp_free()<br>
	 * Original signature : <code>sftp_session sftp_new_channel(ssh_session, ssh_channel)</code>
	 */
	sftp_session sftp_new_channel(ssh_session session, ssh_channel channel);
	/**
	 * @brief Close and deallocate a sftp session.<br>
	 * * @param sftp          The sftp session handle to free.<br>
	 * Original signature : <code>void sftp_free(sftp_session)</code>
	 */
	void sftp_free(sftp_session sftp);
	/**
	 * @brief Initialize the sftp session with the server.<br>
	 * * @param sftp          The sftp session to initialize.<br>
	 * * @return              0 on success, < 0 on error with ssh error set.<br>
	 * * @see sftp_new()<br>
	 * Original signature : <code>int sftp_init(sftp_session)</code>
	 */
	int sftp_init(sftp_session sftp);
	/**
	 * @brief Get the last sftp error.<br>
	 * * Use this function to get the latest error set by a posix like sftp function.<br>
	 * * @param sftp          The sftp session where the error is saved.<br>
	 * * @return              The saved error (see server responses), < 0 if an error<br>
	 *                      in the function occured.<br>
	 * * @see Server responses<br>
	 * Original signature : <code>int sftp_get_error(sftp_session)</code>
	 */
	int sftp_get_error(sftp_session sftp);
	/**
	 * @brief Get the count of extensions provided by the server.<br>
	 * * @param  sftp         The sftp session to use.<br>
	 * * @return The count of extensions provided by the server, 0 on error or<br>
	 *         not available.<br>
	 * Original signature : <code>int sftp_extensions_get_count(sftp_session)</code>
	 */
	unsigned int sftp_extensions_get_count(sftp_session sftp);
	/**
	 * @brief Get the name of the extension provided by the server.<br>
	 * * @param  sftp         The sftp session to use.<br>
	 * * @param  indexn        The index number of the extension name you want.<br>
	 * * @return              The name of the extension.<br>
	 * Original signature : <code>char* sftp_extensions_get_name(sftp_session, unsigned int)</code>
	 */
	const char* sftp_extensions_get_name(sftp_session sftp, unsigned int indexn);
	/**
	 * @brief Get the data of the extension provided by the server.<br>
	 * * This is normally the version number of the extension.<br>
	 * * @param  sftp         The sftp session to use.<br>
	 * * @param  indexn        The index number of the extension data you want.<br>
	 * * @return              The data of the extension.<br>
	 * Original signature : <code>char* sftp_extensions_get_data(sftp_session, unsigned int)</code>
	 */
	const char* sftp_extensions_get_data(sftp_session sftp, unsigned int indexn);
	/**
	 * @brief Check if the given extension is supported.<br>
	 * * @param  sftp         The sftp session to use.<br>
	 * * @param  name         The name of the extension.<br>
	 * * @param  data         The data of the extension.<br>
	 * * @return 1 if supported, 0 if not.<br>
	 * * Example:<br>
	 * * @code<br>
	 * sftp_extension_supported(sftp, "statvfs@openssh.com", "2");<br>
	 * @endcode<br>
	 * Original signature : <code>int sftp_extension_supported(sftp_session, const char*, const char*)</code>
	 */
	int sftp_extension_supported(sftp_session sftp, const char* name, const char* data);
	/**
	 * @brief Open a directory used to obtain directory entries.<br>
	 * * @param session       The sftp session handle to open the directory.<br>
	 * @param path          The path of the directory to open.<br>
	 * * @return              A sftp directory handle or NULL on error with ssh and<br>
	 *                      sftp error set.<br>
	 * * @see                 sftp_readdir<br>
	 * @see                 sftp_closedir<br>
	 * Original signature : <code>sftp_dir sftp_opendir(sftp_session, const char*)</code>
	 */
	sftp_dir sftp_opendir(sftp_session session, const char* path);
	/**
	 * @brief Get a single file attributes structure of a directory.<br>
	 * * @param session      The sftp session handle to read the directory entry.<br>
	 * @param dir          The opened sftp directory handle to read from.<br>
	 * * @return             A file attribute structure or NULL at the end of the<br>
	 *                     directory.<br>
	 * * @see                sftp_opendir()<br>
	 * @see                sftp_attribute_free()<br>
	 * @see                sftp_closedir()<br>
	 * Original signature : <code>sftp_attributes sftp_readdir(sftp_session, sftp_dir)</code>
	 */
	sftp_attributes sftp_readdir(sftp_session session, sftp_dir dir);
	/**
	 * @brief Tell if the directory has reached EOF (End Of File).<br>
	 * * @param dir           The sftp directory handle.<br>
	 * * @return              1 if the directory is EOF, 0 if not.<br>
	 * * @see                 sftp_readdir()<br>
	 * Original signature : <code>int sftp_dir_eof(sftp_dir)</code>
	 */
	int sftp_dir_eof(sftp_dir dir);
	/**
	 * @brief Get information about a file or directory.<br>
	 * * @param session       The sftp session handle.<br>
	 * @param path          The path to the file or directory to obtain the<br>
	 *                      information.<br>
	 * * @return              The sftp attributes structure of the file or directory,<br>
	 *                      NULL on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>sftp_attributes sftp_stat(sftp_session, const char*)</code>
	 */
	sftp_attributes sftp_stat(sftp_session session, const char* path);
	/**
	 * @brief Get information about a file or directory.<br>
	 * * Identical to sftp_stat, but if the file or directory is a symbolic link,<br>
	 * then the link itself is stated, not the file that it refers to.<br>
	 * * @param session       The sftp session handle.<br>
	 * @param path          The path to the file or directory to obtain the<br>
	 *                      information.<br>
	 * * @return              The sftp attributes structure of the file or directory,<br>
	 *                      NULL on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>sftp_attributes sftp_lstat(sftp_session, const char*)</code>
	 */
	sftp_attributes sftp_lstat(sftp_session session, const char* path);
	/**
	 * @brief Get information about a file or directory from a file handle.<br>
	 * * @param file          The sftp file handle to get the stat information.<br>
	 * * @return              The sftp attributes structure of the file or directory,<br>
	 *                      NULL on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>sftp_attributes sftp_fstat(sftp_file)</code>
	 */
	sftp_attributes sftp_fstat(sftp_file file);
	/**
	 * @brief Free a sftp attribute structure.<br>
	 * * @param file          The sftp attribute structure to free.<br>
	 * Original signature : <code>void sftp_attributes_free(sftp_attributes)</code>
	 */
	void sftp_attributes_free(sftp_attributes file);
	/**
	 * @brief Close a directory handle opened by sftp_opendir().<br>
	 * * @param dir           The sftp directory handle to close.<br>
	 * * @return              Returns SSH_NO_ERROR or SSH_ERROR if an error occured.<br>
	 * Original signature : <code>int sftp_closedir(sftp_dir)</code>
	 */
	int sftp_closedir(sftp_dir dir);
	/**
	 * @brief Close an open file handle.<br>
	 * * @param file          The open sftp file handle to close.<br>
	 * * @return              Returns SSH_NO_ERROR or SSH_ERROR if an error occured.<br>
	 * * @see                 sftp_open()<br>
	 * Original signature : <code>int sftp_close(sftp_file)</code>
	 */
	int sftp_close(sftp_file file);
	/**
	 * @brief Open a file on the server.<br>
	 * * @param session       The sftp session handle.<br>
	 * * @param file          The file to be opened.<br>
	 * * @param accesstype    Is one of O_RDONLY, O_WRONLY or O_RDWR which request<br>
	 *                      opening  the  file  read-only,write-only or read/write.<br>
	 *                      Acesss may also be bitwise-or'd with one or  more of<br>
	 *                      the following:<br>
	 *                      O_CREAT - If the file does not exist it will be<br>
	 *                      created.<br>
	 *                      O_EXCL - When  used with O_CREAT, if the file already<br>
	 *                      exists it is an error and the open will fail.<br>
	 *                      O_TRUNC - If the file already exists it will be<br>
	 *                      truncated.<br>
	 * * @param mode          Mode specifies the permissions to use if a new file is<br>
	 *                      created.  It  is  modified  by  the process's umask in<br>
	 *                      the usual way: The permissions of the created file are<br>
	 *                      (mode & ~umask)<br>
	 * * @return              A sftp file handle, NULL on error with ssh and sftp<br>
	 *                      error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>sftp_file sftp_open(sftp_session, const char*, int, mode_t)</code>
	 */
	sftp_file sftp_open(sftp_session session, const char* file, int accesstype, mode_t mode);
	/**
	 * @brief Make the sftp communication for this file handle non blocking.<br>
	 * * @param[in]  handle   The file handle to set non blocking.<br>
	 * Original signature : <code>void sftp_file_set_nonblocking(sftp_file)</code>
	 */
	void sftp_file_set_nonblocking(sftp_file handle);
	/**
	 * @brief Make the sftp communication for this file handle blocking.<br>
	 * * @param[in]  handle   The file handle to set blocking.<br>
	 * Original signature : <code>void sftp_file_set_blocking(sftp_file)</code>
	 */
	void sftp_file_set_blocking(sftp_file handle);
	/**
	 * @brief Read from a file using an opened sftp file handle.<br>
	 * * @param file          The opened sftp file handle to be read from.<br>
	 * * @param buf           Pointer to buffer to recieve read data.<br>
	 * * @param count         Size of the buffer in bytes.<br>
	 * * @return              Number of bytes written, < 0 on error with ssh and sftp<br>
	 *                      error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>ssize_t sftp_read(sftp_file, void*, size_t)</code>
	 */
	ssize_t sftp_read(sftp_file file, void* buf, size_t count);
	/**
	 * @brief Start an asynchronous read from a file using an opened sftp file handle.<br>
	 * * Its goal is to avoid the slowdowns related to the request/response pattern<br>
	 * of a synchronous read. To do so, you must call 2 functions:<br>
	 * * sftp_async_read_begin() and sftp_async_read().<br>
	 * * The first step is to call sftp_async_read_begin(). This function returns a<br>
	 * request identifier. The second step is to call sftp_async_read() using the<br>
	 * returned identifier.<br>
	 * * @param file          The opened sftp file handle to be read from.<br>
	 * * @param len           Size to read in bytes.<br>
	 * * @return              An identifier corresponding to the sent request, < 0 on<br>
	 *                      error.<br>
	 * * @warning             When calling this function, the internal offset is<br>
	 *                      updated corresponding to the len parameter.<br>
	 * * @warning             A call to sftp_async_read_begin() sends a request to<br>
	 *                      the server. When the server answers, libssh allocates<br>
	 *                      memory to store it until sftp_async_read() is called.<br>
	 *                      Not calling sftp_async_read() will lead to memory<br>
	 *                      leaks.<br>
	 * * @see                 sftp_async_read()<br>
	 * @see                 sftp_open()<br>
	 * Original signature : <code>int sftp_async_read_begin(sftp_file, uint32_t)</code>
	 */
	int sftp_async_read_begin(sftp_file file, uint32_t len);
	/**
	 * @brief Wait for an asynchronous read to complete and save the data.<br>
	 * * @param file          The opened sftp file handle to be read from.<br>
	 * * @param data          Pointer to buffer to recieve read data.<br>
	 * * @param len           Size of the buffer in bytes. It should be bigger or<br>
	 *                      equal to the length parameter of the<br>
	 *                      sftp_async_read_begin() call.<br>
	 * * @param id            The identifier returned by the sftp_async_read_begin()<br>
	 *                      function.<br>
	 * * @return              Number of bytes read, 0 on EOF, SSH_ERROR if an error<br>
	 *                      occured, SSH_AGAIN if the file is opened in nonblocking<br>
	 *                      mode and the request hasn't been executed yet.<br>
	 * * @warning             A call to this function with an invalid identifier<br>
	 *                      will never return.<br>
	 * * @see sftp_async_read_begin()<br>
	 * Original signature : <code>int sftp_async_read(sftp_file, void*, uint32_t, uint32_t)</code>
	 */
	int sftp_async_read(sftp_file file, void* data, uint32_t len, uint32_t id);
	/**
	 * @brief Write to a file using an opened sftp file handle.<br>
	 * * @param file          Open sftp file handle to write to.<br>
	 * * @param buf           Pointer to buffer to write data.<br>
	 * * @param count         Size of buffer in bytes.<br>
	 * * @return              Number of bytes written, < 0 on error with ssh and sftp<br>
	 *                      error set.<br>
	 * * @see                 sftp_open()<br>
	 * @see                 sftp_read()<br>
	 * @see                 sftp_close()<br>
	 * Original signature : <code>ssize_t sftp_write(sftp_file, const void*, size_t)</code>
	 */
	ssize_t sftp_write(sftp_file file, const void* buf, size_t count);
	/**
	 * @brief Seek to a specific location in a file.<br>
	 * * @param file         Open sftp file handle to seek in.<br>
	 * * @param new_offset   Offset in bytes to seek.<br>
	 * * @return             0 on success, < 0 on error.<br>
	 * Original signature : <code>int sftp_seek(sftp_file, uint32_t)</code>
	 */
	int sftp_seek(sftp_file file, uint32_t new_offset);
	/**
	 * @brief Seek to a specific location in a file. This is the<br>
	 * 64bit version.<br>
	 * * @param file         Open sftp file handle to seek in.<br>
	 * * @param new_offset   Offset in bytes to seek.<br>
	 * * @return             0 on success, < 0 on error.<br>
	 * Original signature : <code>int sftp_seek64(sftp_file, uint64_t)</code>
	 */
	int sftp_seek64(sftp_file file, uint64_t new_offset);
	/**
	 * @brief Report current byte position in file.<br>
	 * * @param file          Open sftp file handle.<br>
	 * * @return              The offset of the current byte relative to the beginning<br>
	 *                      of the file associated with the file descriptor. < 0 on<br>
	 *                      error.<br>
	 * Original signature : <code>long sftp_tell(sftp_file)</code>
	 */
	unsigned long sftp_tell(sftp_file file);
	/**
	 * @brief Report current byte position in file.<br>
	 * * @param file          Open sftp file handle.<br>
	 * * @return              The offset of the current byte relative to the beginning<br>
	 *                      of the file associated with the file descriptor. < 0 on<br>
	 *                      error.<br>
	 * Original signature : <code>uint64_t sftp_tell64(sftp_file)</code>
	 */
	uint64_t sftp_tell64(sftp_file file);
	/**
	 * @brief Rewinds the position of the file pointer to the beginning of the<br>
	 * file.<br>
	 * * @param file          Open sftp file handle.<br>
	 * Original signature : <code>void sftp_rewind(sftp_file)</code>
	 */
	void sftp_rewind(sftp_file file);
	/**
	 * @brief Unlink (delete) a file.<br>
	 * * @param sftp          The sftp session handle.<br>
	 * * @param file          The file to unlink/delete.<br>
	 * * @return              0 on success, < 0 on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>int sftp_unlink(sftp_session, const char*)</code>
	 */
	int sftp_unlink(sftp_session sftp, const char* file);
	/**
	 * @brief Remove a directoy.<br>
	 * * @param sftp          The sftp session handle.<br>
	 * * @param directory     The directory to remove.<br>
	 * * @return              0 on success, < 0 on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>int sftp_rmdir(sftp_session, const char*)</code>
	 */
	int sftp_rmdir(sftp_session sftp, const char* directory);
	/**
	 * @brief Create a directory.<br>
	 * * @param sftp          The sftp session handle.<br>
	 * * @param directory     The directory to create.<br>
	 * * @param mode          Specifies the permissions to use. It is modified by the<br>
	 *                      process's umask in the usual way:<br>
	 *                      The permissions of the created file are (mode & ~umask)<br>
	 * * @return              0 on success, < 0 on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>int sftp_mkdir(sftp_session, const char*, mode_t)</code>
	 */
	int sftp_mkdir(sftp_session sftp, const char* directory, mode_t mode);
	/**
	 * @brief Rename or move a file or directory.<br>
	 * * @param sftp          The sftp session handle.<br>
	 * * @param original      The original url (source url) of file or directory to<br>
	 *                      be moved.<br>
	 * * @param newname       The new url (destination url) of the file or directory<br>
	 *                      after the move.<br>
	 * * @return              0 on success, < 0 on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>int sftp_rename(sftp_session, const char*, const char*)</code>
	 */
	int sftp_rename(sftp_session sftp, const char* original, const char* newname);
	/**
	 * @brief Set file attributes on a file, directory or symbolic link.<br>
	 * * @param sftp          The sftp session handle.<br>
	 * * @param file          The file which attributes should be changed.<br>
	 * * @param attr          The file attributes structure with the attributes set<br>
	 *                      which should be changed.<br>
	 * * @return              0 on success, < 0 on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>int sftp_setstat(sftp_session, const char*, sftp_attributes)</code>
	 */
	int sftp_setstat(sftp_session sftp, const char* file, sftp_attributes attr);
	/**
	 * @brief Change the file owner and group<br>
	 * * @param sftp          The sftp session handle.<br>
	 * * @param file          The file which owner and group should be changed.<br>
	 * * @param owner         The new owner which should be set.<br>
	 * * @param group         The new group which should be set.<br>
	 * * @return              0 on success, < 0 on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>int sftp_chown(sftp_session, const char*, uid_t, gid_t)</code>
	 */
	int sftp_chown(sftp_session sftp, const char* file, uid_t owner, gid_t group);
	/**
	 * @brief Change permissions of a file<br>
	 * * @param sftp          The sftp session handle.<br>
	 * * @param file          The file which owner and group should be changed.<br>
	 * * @param mode          Specifies the permissions to use. It is modified by the<br>
	 *                      process's umask in the usual way:<br>
	 *                      The permissions of the created file are (mode & ~umask)<br>
	 * * @return              0 on success, < 0 on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>int sftp_chmod(sftp_session, const char*, mode_t)</code>
	 */
	int sftp_chmod(sftp_session sftp, const char* file, mode_t mode);
	/**
	 * @brief Change the last modification and access time of a file.<br>
	 * * @param sftp          The sftp session handle.<br>
	 * * @param file          The file which owner and group should be changed.<br>
	 * * @param times         A timeval structure which contains the desired access<br>
	 *                      and modification time.<br>
	 * * @return              0 on success, < 0 on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>int sftp_utimes(sftp_session, const char*, timeval*)</code>
	 */
	int sftp_utimes(sftp_session sftp, const char* file, timeval* times);
	/**
	 * @brief Create a symbolic link.<br>
	 * * @param  sftp         The sftp session handle.<br>
	 * * @param  target       Specifies the target of the symlink.<br>
	 * * @param  dest         Specifies the path name of the symlink to be created.<br>
	 * * @return              0 on success, < 0 on error with ssh and sftp error set.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>int sftp_symlink(sftp_session, const char*, const char*)</code>
	 */
	int sftp_symlink(sftp_session sftp, const char* target, const char* dest);
	/**
	 * @brief Read the value of a symbolic link.<br>
	 * * @param  sftp         The sftp session handle.<br>
	 * * @param  path         Specifies the path name of the symlink to be read.<br>
	 * * @return              The target of the link, NULL on error.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>char* sftp_readlink(sftp_session, const char*)</code>
	 */
	char* sftp_readlink(sftp_session sftp, const char* path);
	/**
	 * @brief Get information about a mounted file system.<br>
	 * * @param  sftp         The sftp session handle.<br>
	 * * @param  path         The pathname of any file within the mounted file system.<br>
	 * * @return A statvfs structure or NULL on error.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>sftp_statvfs_t sftp_statvfs(sftp_session, const char*)</code>
	 */
	sftp_statvfs_t sftp_statvfs(sftp_session sftp, const char* path);
	/**
	 * @brief Get information about a mounted file system.<br>
	 * * @param  file         An opened file.<br>
	 * * @return A statvfs structure or NULL on error.<br>
	 * * @see sftp_get_error()<br>
	 * Original signature : <code>sftp_statvfs_t sftp_fstatvfs(sftp_file)</code>
	 */
	sftp_statvfs_t sftp_fstatvfs(sftp_file file);
	/**
	 * @brief Free the memory of an allocated statvfs.<br>
	 * * @param  statvfs_o      The statvfs to free.<br>
	 * Original signature : <code>void sftp_statvfs_free(sftp_statvfs_t)</code>
	 */
	void sftp_statvfs_free(sftp_statvfs_t statvfs_o);
	/**
	 * @brief Synchronize a file's in-core state with storage device<br>
	 * * This calls the "fsync@openssh.com" extention. You should check if the<br>
	 * extensions is supported using:<br>
	 * * @code<br>
	 * int supported = sftp_extension_supported(sftp, "fsync@openssh.com", "1");<br>
	 * @endcode<br>
	 * * @param file          The opened sftp file handle to sync<br>
	 * * @return              0 on success, < 0 on error with ssh and sftp error set.<br>
	 * Original signature : <code>int sftp_fsync(sftp_file)</code>
	 */
	int sftp_fsync(sftp_file file);
	/**
	 * @brief Canonicalize a sftp path.<br>
	 * * @param sftp          The sftp session handle.<br>
	 * * @param path          The path to be canonicalized.<br>
	 * * @return              The canonicalize path, NULL on error.<br>
	 * Original signature : <code>char* sftp_canonicalize_path(sftp_session, const char*)</code>
	 */
	char* sftp_canonicalize_path(sftp_session sftp, const char* path);
	/**
	 * @brief Get the version of the SFTP protocol supported by the server<br>
	 * * @param sftp          The sftp session handle.<br>
	 * * @return              The server version.<br>
	 * Original signature : <code>int sftp_server_version(sftp_session)</code>
	 */
	int sftp_server_version(sftp_session sftp);
	/**
	 * this is not a public interface<br>
	 * Original signature : <code>sftp_packet sftp_packet_read(sftp_session)</code>
	 */
	sftp_packet sftp_packet_read(sftp_session sftp);
	/** Original signature : <code>int sftp_packet_write(sftp_session, uint8_t, ssh_buffer)</code> */
	int sftp_packet_write(sftp_session sftp, uint8_t type, ssh_buffer payload);
	/** Original signature : <code>void sftp_packet_free(sftp_packet)</code> */
	void sftp_packet_free(sftp_packet packet);
	/** Original signature : <code>int buffer_add_attributes(ssh_buffer, sftp_attributes)</code> */
	int buffer_add_attributes(ssh_buffer buffer, sftp_attributes attr);
	/** Original signature : <code>sftp_attributes sftp_parse_attr(sftp_session, ssh_buffer, int)</code> */
	sftp_attributes sftp_parse_attr(sftp_session session, ssh_buffer buf, int expectname);
	/**
	 * sftpserver.c<br>
	 * Original signature : <code>sftp_client_message sftp_get_client_message(sftp_session)</code>
	 */
	sftp_client_message sftp_get_client_message(sftp_session sftp);
	/** Original signature : <code>void sftp_client_message_free(sftp_client_message)</code> */
	void sftp_client_message_free(sftp_client_message msg);
	/** Original signature : <code>uint8_t sftp_client_message_get_type(sftp_client_message)</code> */
	uint8_t sftp_client_message_get_type(sftp_client_message msg);
	/** Original signature : <code>char* sftp_client_message_get_filename(sftp_client_message)</code> */
	const char* sftp_client_message_get_filename(sftp_client_message msg);
	/** Original signature : <code>void sftp_client_message_set_filename(sftp_client_message, const char*)</code> */
	void sftp_client_message_set_filename(sftp_client_message msg, const char* newname);
	/** Original signature : <code>char* sftp_client_message_get_data(sftp_client_message)</code> */
	const char* sftp_client_message_get_data(sftp_client_message msg);
	/** Original signature : <code>uint32_t sftp_client_message_get_flags(sftp_client_message)</code> */
	uint32_t sftp_client_message_get_flags(sftp_client_message msg);
	/** Original signature : <code>int sftp_send_client_message(sftp_session, sftp_client_message)</code> */
	int sftp_send_client_message(sftp_session sftp, sftp_client_message msg);
	/** Original signature : <code>int sftp_reply_name(sftp_client_message, const char*, sftp_attributes)</code> */
	int sftp_reply_name(sftp_client_message msg, const char* name, sftp_attributes attr);
	/** Original signature : <code>int sftp_reply_handle(sftp_client_message, ssh_string)</code> */
	int sftp_reply_handle(sftp_client_message msg, ssh_string handle);
	/** Original signature : <code>ssh_string sftp_handle_alloc(sftp_session, void*)</code> */
	ssh_string sftp_handle_alloc(sftp_session sftp, void* info);
	/** Original signature : <code>int sftp_reply_attr(sftp_client_message, sftp_attributes)</code> */
	int sftp_reply_attr(sftp_client_message msg, sftp_attributes attr);
	/** Original signature : <code>void* sftp_handle(sftp_session, ssh_string)</code> */
	void* sftp_handle(sftp_session sftp, ssh_string handle);
	/** Original signature : <code>int sftp_reply_status(sftp_client_message, uint32_t, const char*)</code> */
	int sftp_reply_status(sftp_client_message msg, uint32_t status, const char* message);
	/** Original signature : <code>int sftp_reply_names_add(sftp_client_message, const char*, const char*, sftp_attributes)</code> */
	int sftp_reply_names_add(sftp_client_message msg, const char* file, const char* longname, sftp_attributes attr);
	/** Original signature : <code>int sftp_reply_names(sftp_client_message)</code> */
	int sftp_reply_names(sftp_client_message msg);
	/** Original signature : <code>int sftp_reply_data(sftp_client_message, const void*, int)</code> */
	int sftp_reply_data(sftp_client_message msg, const void* data, int len);
	/** Original signature : <code>void sftp_handle_remove(sftp_session, void*)</code> */
	void sftp_handle_remove(sftp_session sftp, void* handle);
}
;




/**
 * Copy N bytes of SRC to DEST.<br>
 * Original signature : <code>void* memcpy(void*, const void*, size_t)</code>
 */
extern void* memcpy(void* __dest, const void* __src, size_t __n);
/**
 * Copy N bytes of SRC to DEST, guaranteeing<br>
 * correct behavior for overlapping strings.<br>
 * Original signature : <code>void* memmove(void*, const void*, size_t)</code>
 */
extern void* memmove(void* __dest, const void* __src, size_t __n);
/**
 * Copy no more than N bytes of SRC to DEST, stopping when C is found.<br>
 * Return the position in DEST one byte past where C was copied,<br>
 * or NULL if C was not found in the first N bytes of SRC.<br>
 * Original signature : <code>void* memccpy(void*, const void*, int, size_t)</code>
 */
extern void* memccpy(void* __dest, const void* __src, int __c, size_t __n);
/**
 * Set N bytes of S to C.<br>
 * Original signature : <code>void* memset(void*, int, size_t)</code>
 */
extern void* memset(void* __s, int __c, size_t __n);
/**
 * Compare N bytes of S1 and S2.<br>
 * Original signature : <code>int memcmp(const void*, const void*, size_t)</code>
 */
extern int memcmp(const void* __s1, const void* __s2, size_t __n);
/**
 * Search N bytes of S for C.<br>
 * Original signature : <code>void* memchr(const void*, int, size_t)</code>
 */
extern void* memchr(const void* __s, int __c, size_t __n);
/**
 * Copy SRC to DEST.<br>
 * Original signature : <code>char* strcpy(char*, const char*)</code>
 */
extern char* strcpy(char* __dest, const char* __src);
/**
 * Copy no more than N characters of SRC to DEST.<br>
 * Original signature : <code>char* strncpy(char*, const char*, size_t)</code>
 */
extern char* strncpy(char* __dest, const char* __src, size_t __n);
/**
 * Append SRC onto DEST.<br>
 * Original signature : <code>char* strcat(char*, const char*)</code>
 */
extern char* strcat(char* __dest, const char* __src);
/**
 * Append no more than N characters from SRC onto DEST.<br>
 * Original signature : <code>char* strncat(char*, const char*, size_t)</code>
 */
extern char* strncat(char* __dest, const char* __src, size_t __n);
/**
 * Compare S1 and S2.<br>
 * Original signature : <code>int strcmp(const char*, const char*)</code>
 */
extern int strcmp(const char* __s1, const char* __s2);
/**
 * Compare N characters of S1 and S2.<br>
 * Original signature : <code>int strncmp(const char*, const char*, size_t)</code>
 */
extern int strncmp(const char* __s1, const char* __s2, size_t __n);
/**
 * Compare the collated forms of S1 and S2.<br>
 * Original signature : <code>int strcoll(const char*, const char*)</code>
 */
extern int strcoll(const char* __s1, const char* __s2);
/**
 * Put a transformation of SRC into no more than N bytes of DEST.<br>
 * Original signature : <code>size_t strxfrm(char*, const char*, size_t)</code>
 */
extern size_t strxfrm(char* __dest, const char* __src, size_t __n);

/**
 * POSIX.1-2008: the locale_t type, representing a locale context<br>
 * (implementation-namespace version).  This type should be treated<br>
 * as opaque by applications; some details are exposed for the sake of<br>
 * efficiency in e.g. ctype functions.
 */
struct __locale_struct {
	__locale_data*[13] __locales; /* 13 = __LC_LAST. */
	const unsigned short* __ctype_b;
	const int* __ctype_tolower;
	const int* __ctype_toupper;
	const char*[13] __names;
};
typedef __locale_struct* __locale_t;
typedef __locale_t locale_t;
/**
 * Compare the collated forms of S1 and S2, using sorting rules from L.<br>
 * Original signature : <code>int strcoll_l(const char*, const char*, locale_t)</code>
 */
extern int strcoll_l(const char* __s1, const char* __s2, locale_t __l);
/**
 * Put a transformation of SRC into no more than N bytes of DEST,<br>
 * using sorting rules from L.<br>
 * Original signature : <code>size_t strxfrm_l(char*, const char*, size_t, locale_t)</code>
 */
extern size_t strxfrm_l(char* __dest, const char* __src, size_t __n, locale_t __l);
/**
 * Duplicate S, returning an identical malloc'd string.<br>
 * Original signature : <code>char* strdup(const char*)</code>
 */
extern char* strdup(const char* __s);
/**
 * Return a malloc'd copy of at most N bytes of STRING.  The<br>
 * resultant string is terminated even if no null terminator<br>
 * appears before STRING[N].<br>
 * Original signature : <code>char* strndup(const char*, size_t)</code>
 */
extern char* strndup(const char* __string, size_t __n);
/**
 * Find the first occurrence of C in S.<br>
 * Original signature : <code>char* strchr(const char*, int)</code>
 */
extern char* strchr(const char* __s, int __c);
/**
 * Find the last occurrence of C in S.<br>
 * Original signature : <code>char* strrchr(const char*, int)</code>
 */
extern char* strrchr(const char* __s, int __c);
/**
 * Return the length of the initial segment of S which<br>
 * consists entirely of characters not in REJECT.<br>
 * Original signature : <code>size_t strcspn(const char*, const char*)</code>
 */
extern size_t strcspn(const char* __s, const char* __reject);
/**
 * Return the length of the initial segment of S which<br>
 * consists entirely of characters in ACCEPT.<br>
 * Original signature : <code>size_t strspn(const char*, const char*)</code>
 */
extern size_t strspn(const char* __s, const char* __accept);
/**
 * Find the first occurrence in S of any character in ACCEPT.<br>
 * Original signature : <code>char* strpbrk(const char*, const char*)</code>
 */
extern char* strpbrk(const char* __s, const char* __accept);
/**
 * Find the first occurrence of NEEDLE in HAYSTACK.<br>
 * Original signature : <code>char* strstr(const char*, const char*)</code>
 */
extern char* strstr(const char* __haystack, const char* __needle);
/**
 * Divide S into tokens separated by characters in DELIM.<br>
 * Original signature : <code>char* strtok(char*, const char*)</code>
 */
extern char* strtok(char* __s, const char* __delim);
/**
 * Divide S into tokens separated by characters in DELIM.  Information<br>
 * passed between calls are stored in SAVE_PTR.<br>
 * Original signature : <code>char* __strtok_r(char*, const char*, char**)</code>
 */
extern char* __strtok_r(char* __s, const char* __delim, char** __save_ptr);
/** Original signature : <code>char* strtok_r(char*, const char*, char**)</code> */
extern char* strtok_r(char* __s, const char* __delim, char** __save_ptr);
/**
 * Return the length of S.<br>
 * Original signature : <code>size_t strlen(const char*)</code>
 */
extern size_t strlen(const char* __s);
/**
 * Find the length of STRING, but scan at most MAXLEN characters.<br>
 * If no '\0' terminator is found in that many characters, return MAXLEN.<br>
 * Original signature : <code>size_t strnlen(const char*, size_t)</code>
 */
extern size_t strnlen(const char* __string, size_t __maxlen);
/**
 * Return a string describing the meaning of the `errno' code in ERRNUM.<br>
 * Original signature : <code>char* strerror(int)</code>
 */
extern char* strerror(int __errnum);
/**
 * Fill BUF with a string describing the meaning of the `errno' code in<br>
 * ERRNUM.<br>
 * Original signature : <code>int __xpg_strerror_r(int, char*, size_t)</code>
 */
extern int __xpg_strerror_r(int __errnum, char* __buf, size_t __buflen);
/**
 * Translate error number to string according to the locale L.<br>
 * Original signature : <code>char* strerror_l(int, locale_t)</code>
 */
extern char* strerror_l(int __errnum, locale_t __l);


/**
 * Compare N bytes of S1 and S2 (same as memcmp).<br>
 * Original signature : <code>int bcmp(const void*, const void*, size_t)</code>
 */
extern int bcmp(const void* __s1, const void* __s2, size_t __n);
/**
 * Copy N bytes of SRC to DEST (like memmove, but args reversed).<br>
 * Original signature : <code>void bcopy(const void*, void*, size_t)</code>
 */
extern void bcopy(const void* __src, void* __dest, size_t __n);
/**
 * Set N bytes of S to 0.<br>
 * Original signature : <code>void bzero(void*, size_t)</code>
 */
extern void bzero(void* __s, size_t __n);
/**
 * Find the first occurrence of C in S (same as strchr).<br>
 * Original signature : <code>char* index(const char*, int)</code>
 */
extern char* index(const char* __s, int __c);
/**
 * Find the last occurrence of C in S (same as strrchr).<br>
 * Original signature : <code>char* rindex(const char*, int)</code>
 */
extern char* rindex(const char* __s, int __c);
/**
 * Return the position of the first bit set in I, or 0 if none are set.<br>
 * The least-significant bit is position 1, the most-significant 32.<br>
 * Original signature : <code>int ffs(int)</code>
 */
extern int ffs(int __i);
/**
 * The following two functions are non-standard but necessary for non-32 bit<br>
 * platforms.<br>
 * Original signature : <code>int ffsl(long long)</code>
 */
extern int ffsl(long long __l);
/** Original signature : <code>int ffsll(long long long)</code> */
extern int ffsll(long long long __ll);
/**
 * Compare S1 and S2, ignoring case.<br>
 * Original signature : <code>int strcasecmp(const char*, const char*)</code>
 */
extern int strcasecmp(const char* __s1, const char* __s2);
/**
 * Compare no more than N chars of S1 and S2, ignoring case.<br>
 * Original signature : <code>int strncasecmp(const char*, const char*, size_t)</code>
 */
extern int strncasecmp(const char* __s1, const char* __s2, size_t __n);
/**
 * Compare S1 and S2, ignoring case, using collation rules from LOC.<br>
 * Original signature : <code>int strcasecmp_l(const char*, const char*, locale_t)</code>
 */
extern int strcasecmp_l(const char* __s1, const char* __s2, locale_t __loc);
/**
 * Compare no more than N chars of S1 and S2, ignoring case, using<br>
 * collation rules from LOC.<br>
 * Original signature : <code>int strncasecmp_l(const char*, const char*, size_t, locale_t)</code>
 */
extern int strncasecmp_l(const char* __s1, const char* __s2, size_t __n, locale_t __loc);
/**
 * Set N bytes of S to 0.  The compiler will not delete a call to this<br>
 * function, even if S is dead after the call.<br>
 * Original signature : <code>void explicit_bzero(void*, size_t)</code>
 */
extern void explicit_bzero(void* __s, size_t __n);
/**
 * Return the next DELIM-delimited token from *STRINGP,<br>
 * terminating it with a '\0', and update *STRINGP to point past it.<br>
 * Original signature : <code>char* strsep(char**, const char*)</code>
 */
extern char* strsep(char** __stringp, const char* __delim);
/**
 * Return a string describing the meaning of the signal number in SIG.<br>
 * Original signature : <code>char* strsignal(int)</code>
 */
extern char* strsignal(int __sig);
/**
 * Copy SRC to DEST, returning the address of the terminating '\0' in DEST.<br>
 * Original signature : <code>char* __stpcpy(char*, const char*)</code>
 */
extern char* __stpcpy(char* __dest, const char* __src);
/** Original signature : <code>char* stpcpy(char*, const char*)</code> */
extern char* stpcpy(char* __dest, const char* __src);
/**
 * Copy no more than N characters of SRC to DEST, returning the address of<br>
 * the last character written into DEST.<br>
 * Original signature : <code>char* __stpncpy(char*, const char*, size_t)</code>
 */
extern char* __stpncpy(char* __dest, const char* __src, size_t __n);
/** Original signature : <code>char* stpncpy(char*, const char*, size_t)</code> */
extern char* stpncpy(char* __dest, const char* __src, size_t __n);
extern ""C"" {
/**
	 * @internal<br>
	 * @brief callback to process simple codes<br>
	 * @param code value to transmit<br>
	 * @param user Userdata to pass in callback
	 */
	typedef void (*ssh_callback_int)(int code, void* user) ssh_callback_int;
	/**
	 * @internal<br>
	 * @brief callback for data received messages.<br>
	 * @param data data retrieved from the socket or stream<br>
	 * @param len number of bytes available from this stream<br>
	 * @param user user-supplied pointer sent along with all callback messages<br>
	 * @returns number of bytes processed by the callee. The remaining bytes will<br>
	 * be sent in the next callback message, when more data is available.
	 */
	typedef int (*ssh_callback_data)(const void* data, size_t len, void* user) ssh_callback_data;
	typedef void (*ssh_callback_int_int)(int code, int errno_code, void* user) ssh_callback_int_int;
	typedef int (*ssh_message_callback)(ssh_message message, void* user) ssh_message_callback;
	typedef int (*ssh_channel_callback_int)(ssh_channel channel, int code, void* user) ssh_channel_callback_int;
	typedef int (*ssh_channel_callback_data)(ssh_channel channel, int code, void* data, size_t len, void* user) ssh_channel_callback_data;
	/**
	 * @brief SSH log callback. All logging messages will go through this callback<br>
	 * @param session Current session handler<br>
	 * @param priority Priority of the log, the smaller being the more important<br>
	 * @param message the actual message<br>
	 * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_log_callback)(ssh_session session, int priority, const char* message, void* userdata) ssh_log_callback;
	/**
	 * @brief SSH log callback.<br>
	 * * All logging messages will go through this callback.<br>
	 * * @param priority  Priority of the log, the smaller being the more important.<br>
	 * * @param function  The function name calling the the logging fucntions.<br>
	 * * @param message   The actual message<br>
	 * * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_logging_callback)(int priority, const char* function, const char* buffer, void* userdata) ssh_logging_callback;
	/**
	 * @brief SSH Connection status callback.<br>
	 * @param session Current session handler<br>
	 * @param status Percentage of connection status, going from 0.0 to 1.0<br>
	 * once connection is done.<br>
	 * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_status_callback)(ssh_session session, float status, void* userdata) ssh_status_callback;
	/**
	 * @brief SSH global request callback. All global request will go through this<br>
	 * callback.<br>
	 * @param session Current session handler<br>
	 * @param message the actual message<br>
	 * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_global_request_callback)(ssh_session session, ssh_message message, void* userdata) ssh_global_request_callback;
	/**
	 * @brief Handles an SSH new channel open X11 request. This happens when the server<br>
	 * sends back an X11 connection attempt. This is a client-side API<br>
	 * @param session current session handler<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns a valid ssh_channel handle if the request is to be allowed<br>
	 * @returns NULL if the request should not be allowed<br>
	 * @warning The channel pointer returned by this callback must be closed by the application.
	 */
	typedef ssh_channel (*ssh_channel_open_request_x11_callback)(ssh_session session, const char* originator_address, int originator_port, void* userdata) ssh_channel_open_request_x11_callback;
	/**
	 * @brief Handles an SSH new channel open "auth-agent" request. This happens when the server<br>
	 * sends back an "auth-agent" connection attempt. This is a client-side API<br>
	 * @param session current session handler<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns a valid ssh_channel handle if the request is to be allowed<br>
	 * @returns NULL if the request should not be allowed<br>
	 * @warning The channel pointer returned by this callback must be closed by the application.
	 */
	typedef ssh_channel (*ssh_channel_open_request_auth_agent_callback)(ssh_session session, void* userdata) ssh_channel_open_request_auth_agent_callback;
	/** The structure to replace libssh functions with appropriate callbacks. */
	struct ssh_callbacks_struct {
		size_t size;
		void* userdata;
		ssh_auth_callback auth_function;
		ssh_log_callback log_function;
		connect_status_function_callback* connect_status_function;
		ssh_global_request_callback global_request_function;
		ssh_channel_open_request_x11_callback channel_open_request_x11_function;
		ssh_channel_open_request_auth_agent_callback channel_open_request_auth_agent_function;
		typedef void connect_status_function_callback(void* userdata, float status);
	};
	typedef ssh_callbacks_struct* ssh_callbacks;
	/**
	 * @brief SSH authentication callback.<br>
	 * @param session Current session handler<br>
	 * @param user User that wants to authenticate<br>
	 * @param password Password used for authentication<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns SSH_AUTH_SUCCESS Authentication is accepted.<br>
	 * @returns SSH_AUTH_PARTIAL Partial authentication, more authentication means are needed.<br>
	 * @returns SSH_AUTH_DENIED Authentication failed.
	 */
	typedef int (*ssh_auth_password_callback)(ssh_session session, const char* user, const char* password, void* userdata) ssh_auth_password_callback;
	/**
	 * @brief SSH authentication callback. Tries to authenticates user with the "none" method<br>
	 * which is anonymous or passwordless.<br>
	 * @param session Current session handler<br>
	 * @param user User that wants to authenticate<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns SSH_AUTH_SUCCESS Authentication is accepted.<br>
	 * @returns SSH_AUTH_PARTIAL Partial authentication, more authentication means are needed.<br>
	 * @returns SSH_AUTH_DENIED Authentication failed.
	 */
	typedef int (*ssh_auth_none_callback)(ssh_session session, const char* user, void* userdata) ssh_auth_none_callback;
	/**
	 * @brief SSH authentication callback. Tries to authenticates user with the "gssapi-with-mic" method<br>
	 * @param session Current session handler<br>
	 * @param user Username of the user (can be spoofed)<br>
	 * @param principal Authenticated principal of the user, including realm.<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns SSH_AUTH_SUCCESS Authentication is accepted.<br>
	 * @returns SSH_AUTH_PARTIAL Partial authentication, more authentication means are needed.<br>
	 * @returns SSH_AUTH_DENIED Authentication failed.<br>
	 * @warning Implementations should verify that parameter user matches in some way the principal.<br>
	 * user and principal can be different. Only the latter is guaranteed to be safe.
	 */
	typedef int (*ssh_auth_gssapi_mic_callback)(ssh_session session, const char* user, const char* principal, void* userdata) ssh_auth_gssapi_mic_callback;
	/**
	 * @brief SSH authentication callback.<br>
	 * @param session Current session handler<br>
	 * @param user User that wants to authenticate<br>
	 * @param pubkey public key used for authentication<br>
	 * @param signature_state SSH_PUBLICKEY_STATE_NONE if the key is not signed (simple public key probe),<br>
	 * 							SSH_PUBLICKEY_STATE_VALID if the signature is valid. Others values should be<br>
	 * 							replied with a SSH_AUTH_DENIED.<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns SSH_AUTH_SUCCESS Authentication is accepted.<br>
	 * @returns SSH_AUTH_PARTIAL Partial authentication, more authentication means are needed.<br>
	 * @returns SSH_AUTH_DENIED Authentication failed.
	 */
	typedef int (*ssh_auth_pubkey_callback)(ssh_session session, const char* user, ssh_key_struct* pubkey, char signature_state, void* userdata) ssh_auth_pubkey_callback;
	/**
	 * @brief Handles an SSH service request<br>
	 * @param session current session handler<br>
	 * @param service name of the service (e.g. "ssh-userauth") requested<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns 0 if the request is to be allowed<br>
	 * @returns -1 if the request should not be allowed
	 */
	typedef int (*ssh_service_request_callback)(ssh_session session, const char* service, void* userdata) ssh_service_request_callback;
	/**
	 * @brief Handles an SSH new channel open session request<br>
	 * @param session current session handler<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns a valid ssh_channel handle if the request is to be allowed<br>
	 * @returns NULL if the request should not be allowed<br>
	 * @warning The channel pointer returned by this callback must be closed by the application.
	 */
	typedef ssh_channel (*ssh_channel_open_request_session_callback)(ssh_session session, void* userdata) ssh_channel_open_request_session_callback;
	/**
	 * @brief handle the beginning of a GSSAPI authentication, server side.<br>
	 * @param session current session handler<br>
	 * @param user the username of the client<br>
	 * @param n_oid number of available oids<br>
	 * @param oids OIDs provided by the client<br>
	 * @returns an ssh_string containing the chosen OID, that's supported by both<br>
	 * client and server.<br>
	 * @warning It is not necessary to fill this callback in if libssh is linked<br>
	 * with libgssapi.
	 */
	typedef ssh_string_callback ssh_string(* ssh_gssapi_select_oid_callback);
	/**
	 * @brief handle the negociation of a security context, server side.<br>
	 * @param session current session handler<br>
	 * @param[in] input_token input token provided by client<br>
	 * @param[out] output_token output of the gssapi accept_sec_context method,<br>
	 * 				NULL after completion.<br>
	 * @returns SSH_OK if the token was generated correctly or accept_sec_context<br>
	 * returned GSS_S_COMPLETE<br>
	 * @returns SSH_ERROR in case of error<br>
	 * @warning It is not necessary to fill this callback in if libssh is linked<br>
	 * with libgssapi.
	 */
	typedef int (*ssh_gssapi_accept_sec_ctx_callback)(ssh_session session, ssh_string input_token, ssh_string* output_token, void* userdata) ssh_gssapi_accept_sec_ctx_callback;
	/**
	 * @brief Verify and authenticates a MIC, server side.<br>
	 * @param session current session handler<br>
	 * @param[in] mic input mic to be verified provided by client<br>
	 * @param[in] mic_buffer buffer of data to be signed.<br>
	 * @param[in] mic_buffer_size size of mic_buffer<br>
	 * @returns SSH_OK if the MIC was authenticated correctly<br>
	 * @returns SSH_ERROR in case of error<br>
	 * @warning It is not necessary to fill this callback in if libssh is linked<br>
	 * with libgssapi.
	 */
	typedef int (*ssh_gssapi_verify_mic_callback)(ssh_session session, ssh_string mic, void* mic_buffer, size_t mic_buffer_size, void* userdata) ssh_gssapi_verify_mic_callback;
	/** This structure can be used to implement a libssh server, with appropriate callbacks. */
	struct ssh_server_callbacks_struct {
		size_t size;
		void* userdata;
		ssh_auth_password_callback auth_password_function;
		ssh_auth_none_callback auth_none_function;
		ssh_auth_gssapi_mic_callback auth_gssapi_mic_function;
		ssh_auth_pubkey_callback auth_pubkey_function;
		ssh_service_request_callback service_request_function;
		ssh_channel_open_request_session_callback channel_open_request_session_function;
		ssh_gssapi_select_oid_callback gssapi_select_oid_function;
		ssh_gssapi_accept_sec_ctx_callback gssapi_accept_sec_ctx_function;
		ssh_gssapi_verify_mic_callback gssapi_verify_mic_function;
	};
	typedef ssh_server_callbacks_struct* ssh_server_callbacks;
	/**
	 * @brief Set the session server callback functions.<br>
	 * * This functions sets the callback structure to use your own callback<br>
	 * functions for user authentication, new channels and requests.<br>
	 * * @code<br>
	 * struct ssh_server_callbacks_struct cb = {<br>
	 *   .userdata = data,<br>
	 *   .auth_password_function = my_auth_function<br>
	 * };<br>
	 * ssh_callbacks_init(&cb);<br>
	 * ssh_set_server_callbacks(session, &cb);<br>
	 * @endcode<br>
	 * * @param  session      The session to set the callback structure.<br>
	 * * @param  cb           The callback structure itself.<br>
	 * * @return SSH_OK on success, SSH_ERROR on error.<br>
	 * Original signature : <code>int ssh_set_server_callbacks(ssh_session, ssh_server_callbacks)</code>
	 */
	int ssh_set_server_callbacks(ssh_session session, ssh_server_callbacks cb);
	/**
	 * These are the callbacks exported by the socket structure<br>
	 * They are called by the socket module when a socket event appears
	 */
	struct ssh_socket_callbacks_struct {
		void* userdata;
		ssh_callback_data data;
		ssh_callback_int controlflow;
		ssh_callback_int_int exception;
		ssh_callback_int_int connected;
	};
	typedef ssh_socket_callbacks_struct* ssh_socket_callbacks;
	/**
	 * @brief Prototype for a packet callback, to be called when a new packet arrives<br>
	 * @param session The current session of the packet<br>
	 * @param type packet type (see ssh2.h)<br>
	 * @param packet buffer containing the packet, excluding size, type and padding fields<br>
	 * @param user user argument to the callback<br>
	 * and are called each time a packet shows up<br>
	 * @returns SSH_PACKET_USED Packet was parsed and used<br>
	 * @returns SSH_PACKET_NOT_USED Packet was not used or understood, processing must continue
	 */
	typedef int (*ssh_packet_callback)(ssh_session session, uint8_t type, ssh_buffer packet, void* user) ssh_packet_callback;
	/**
	 * @brief This macro declares a packet callback handler<br>
	 * @code<br>
	 * SSH_PACKET_CALLBACK(mycallback){<br>
	 * ...<br>
	 * }<br>
	 * @endcode
	 */
	struct ssh_packet_callbacks_struct {
		uint8_t start;
		uint8_t n_callbacks;
		ssh_packet_callback* callbacks;
		void* user;
	};
	typedef ssh_packet_callbacks_struct* ssh_packet_callbacks;
	/**
	 * @brief Set the session callback functions.<br>
	 * * This functions sets the callback structure to use your own callback<br>
	 * functions for auth, logging and status.<br>
	 * * @code<br>
	 * struct ssh_callbacks_struct cb = {<br>
	 *   .userdata = data,<br>
	 *   .auth_function = my_auth_function<br>
	 * };<br>
	 * ssh_callbacks_init(&cb);<br>
	 * ssh_set_callbacks(session, &cb);<br>
	 * @endcode<br>
	 * * @param  session      The session to set the callback structure.<br>
	 * * @param  cb           The callback structure itself.<br>
	 * * @return SSH_OK on success, SSH_ERROR on error.<br>
	 * Original signature : <code>int ssh_set_callbacks(ssh_session, ssh_callbacks)</code>
	 */
	int ssh_set_callbacks(ssh_session session, ssh_callbacks cb);
	/**
	 * @brief SSH channel data callback. Called when data is available on a channel<br>
	 * @param session Current session handler<br>
	 * @param channel the actual channel<br>
	 * @param data the data that has been read on the channel<br>
	 * @param len the length of the data<br>
	 * @param is_stderr is 0 for stdout or 1 for stderr<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns number of bytes processed by the callee. The remaining bytes will<br>
	 * be sent in the next callback message, when more data is available.
	 */
	typedef int (*ssh_channel_data_callback)(ssh_session session, ssh_channel channel, void* data, uint32_t len, int is_stderr, void* userdata) ssh_channel_data_callback;
	/**
	 * @brief SSH channel eof callback. Called when a channel receives EOF<br>
	 * @param session Current session handler<br>
	 * @param channel the actual channel<br>
	 * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_channel_eof_callback)(ssh_session session, ssh_channel channel, void* userdata) ssh_channel_eof_callback;
	/**
	 * @brief SSH channel close callback. Called when a channel is closed by remote peer<br>
	 * @param session Current session handler<br>
	 * @param channel the actual channel<br>
	 * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_channel_close_callback)(ssh_session session, ssh_channel channel, void* userdata) ssh_channel_close_callback;
	/**
	 * @brief SSH channel signal callback. Called when a channel has received a signal<br>
	 * @param session Current session handler<br>
	 * @param channel the actual channel<br>
	 * @param signal the signal name (without the SIG prefix)<br>
	 * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_channel_signal_callback)(ssh_session session, ssh_channel channel, const char* signal, void* userdata) ssh_channel_signal_callback;
	/**
	 * @brief SSH channel exit status callback. Called when a channel has received an exit status<br>
	 * @param session Current session handler<br>
	 * @param channel the actual channel<br>
	 * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_channel_exit_status_callback)(ssh_session session, ssh_channel channel, int exit_status, void* userdata) ssh_channel_exit_status_callback;
	/**
	 * @brief SSH channel exit signal callback. Called when a channel has received an exit signal<br>
	 * @param session Current session handler<br>
	 * @param channel the actual channel<br>
	 * @param signal the signal name (without the SIG prefix)<br>
	 * @param core a boolean telling wether a core has been dumped or not<br>
	 * @param errmsg the description of the exception<br>
	 * @param lang the language of the description (format: RFC 3066)<br>
	 * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_channel_exit_signal_callback)(ssh_session session, ssh_channel channel, const char* signal, int core, const char* errmsg, const char* lang, void* userdata) ssh_channel_exit_signal_callback;
	/**
	 * @brief SSH channel PTY request from a client.<br>
	 * @param channel the channel<br>
	 * @param term The type of terminal emulation<br>
	 * @param width width of the terminal, in characters<br>
	 * @param height height of the terminal, in characters<br>
	 * @param pxwidth width of the terminal, in pixels<br>
	 * @param pxheight height of the terminal, in pixels<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns 0 if the pty request is accepted<br>
	 * @returns -1 if the request is denied
	 */
	typedef int (*ssh_channel_pty_request_callback)(ssh_session session, ssh_channel channel, const char* term, int width, int height, int pxwidth, int pwheight, void* userdata) ssh_channel_pty_request_callback;
	/**
	 * @brief SSH channel Shell request from a client.<br>
	 * @param channel the channel<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns 0 if the shell request is accepted<br>
	 * @returns 1 if the request is denied
	 */
	typedef int (*ssh_channel_shell_request_callback)(ssh_session session, ssh_channel channel, void* userdata) ssh_channel_shell_request_callback;
	/**
	 * @brief SSH auth-agent-request from the client. This request is<br>
	 * sent by a client when agent forwarding is available.<br>
	 * Server is free to ignore this callback, no answer is expected.<br>
	 * @param channel the channel<br>
	 * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_channel_auth_agent_req_callback)(ssh_session session, ssh_channel channel, void* userdata) ssh_channel_auth_agent_req_callback;
	/**
	 * @brief SSH X11 request from the client. This request is<br>
	 * sent by a client when X11 forwarding is requested(and available).<br>
	 * Server is free to ignore this callback, no answer is expected.<br>
	 * @param channel the channel<br>
	 * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_channel_x11_req_callback)(ssh_session session, ssh_channel channel, int single_connection, const char* auth_protocol, const char* auth_cookie, uint32_t screen_number, void* userdata) ssh_channel_x11_req_callback;
	/**
	 * @brief SSH channel PTY windows change (terminal size) from a client.<br>
	 * @param channel the channel<br>
	 * @param width width of the terminal, in characters<br>
	 * @param height height of the terminal, in characters<br>
	 * @param pxwidth width of the terminal, in pixels<br>
	 * @param pxheight height of the terminal, in pixels<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns 0 if the pty request is accepted<br>
	 * @returns -1 if the request is denied
	 */
	typedef int (*ssh_channel_pty_window_change_callback)(ssh_session session, ssh_channel channel, int width, int height, int pxwidth, int pwheight, void* userdata) ssh_channel_pty_window_change_callback;
	/**
	 * @brief SSH channel Exec request from a client.<br>
	 * @param channel the channel<br>
	 * @param command the shell command to be executed<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns 0 if the exec request is accepted<br>
	 * @returns 1 if the request is denied
	 */
	typedef int (*ssh_channel_exec_request_callback)(ssh_session session, ssh_channel channel, const char* command, void* userdata) ssh_channel_exec_request_callback;
	/**
	 * @brief SSH channel environment request from a client.<br>
	 * @param channel the channel<br>
	 * @param env_name name of the environment value to be set<br>
	 * @param env_value value of the environment value to be set<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns 0 if the env request is accepted<br>
	 * @returns 1 if the request is denied<br>
	 * @warning some environment variables can be dangerous if changed (e.g.<br>
	 * 			LD_PRELOAD) and should not be fulfilled.
	 */
	typedef int (*ssh_channel_env_request_callback)(ssh_session session, ssh_channel channel, const char* env_name, const char* env_value, void* userdata) ssh_channel_env_request_callback;
	/**
	 * @brief SSH channel subsystem request from a client.<br>
	 * @param channel the channel<br>
	 * @param subsystem the subsystem required<br>
	 * @param userdata Userdata to be passed to the callback function.<br>
	 * @returns 0 if the subsystem request is accepted<br>
	 * @returns 1 if the request is denied
	 */
	typedef int (*ssh_channel_subsystem_request_callback)(ssh_session session, ssh_channel channel, const char* subsystem, void* userdata) ssh_channel_subsystem_request_callback;
	/**
	 * @brief SSH channel write will not block (flow control).<br>
	 * * @param channel the channel<br>
	 * * @param[in] bytes size of the remote window in bytes. Writing as much data<br>
	 *            will not block.<br>
	 * * @param[in] userdata Userdata to be passed to the callback function.<br>
	 * * @returns 0 default return value (other return codes may be added in future).
	 */
	typedef int (*ssh_channel_write_wontblock_callback)(ssh_session session, ssh_channel channel, size_t bytes, void* userdata) ssh_channel_write_wontblock_callback;
	struct ssh_channel_callbacks_struct {
		size_t size;
		void* userdata;
		ssh_channel_data_callback channel_data_function;
		ssh_channel_eof_callback channel_eof_function;
		ssh_channel_close_callback channel_close_function;
		ssh_channel_signal_callback channel_signal_function;
		ssh_channel_exit_status_callback channel_exit_status_function;
		ssh_channel_exit_signal_callback channel_exit_signal_function;
		ssh_channel_pty_request_callback channel_pty_request_function;
		ssh_channel_shell_request_callback channel_shell_request_function;
		ssh_channel_auth_agent_req_callback channel_auth_agent_req_function;
		ssh_channel_x11_req_callback channel_x11_req_function;
		ssh_channel_pty_window_change_callback channel_pty_window_change_function;
		ssh_channel_exec_request_callback channel_exec_request_function;
		ssh_channel_env_request_callback channel_env_request_function;
		ssh_channel_subsystem_request_callback channel_subsystem_request_function;
		ssh_channel_write_wontblock_callback channel_write_wontblock_function;
	};
	typedef ssh_channel_callbacks_struct* ssh_channel_callbacks;
	/**
	 * @brief Set the channel callback functions.<br>
	 * * This functions sets the callback structure to use your own callback<br>
	 * functions for channel data and exceptions<br>
	 * * @code<br>
	 * struct ssh_channel_callbacks_struct cb = {<br>
	 *   .userdata = data,<br>
	 *   .channel_data = my_channel_data_function<br>
	 * };<br>
	 * ssh_callbacks_init(&cb);<br>
	 * ssh_set_channel_callbacks(channel, &cb);<br>
	 * @endcode<br>
	 * * @param  channel      The channel to set the callback structure.<br>
	 * * @param  cb           The callback structure itself.<br>
	 * * @return SSH_OK on success, SSH_ERROR on error.<br>
	 * @warning this function will not replace existing callbacks but set the<br>
	 *          new one atop of them.<br>
	 * Original signature : <code>int ssh_set_channel_callbacks(ssh_channel, ssh_channel_callbacks)</code>
	 */
	int ssh_set_channel_callbacks(ssh_channel channel, ssh_channel_callbacks cb);
	/**
	 * @brief Add channel callback functions<br>
	 * * This function will add channel callback functions to the channel callback<br>
	 * list.<br>
	 * Callbacks missing from a callback structure will be probed in the next<br>
	 * on the list.<br>
	 * * @param  channel      The channel to set the callback structure.<br>
	 * * @param  cb           The callback structure itself.<br>
	 * * @return SSH_OK on success, SSH_ERROR on error.<br>
	 * * @see ssh_set_channel_callbacks<br>
	 * Original signature : <code>int ssh_add_channel_callbacks(ssh_channel, ssh_channel_callbacks)</code>
	 */
	int ssh_add_channel_callbacks(ssh_channel channel, ssh_channel_callbacks cb);
	/**
	 * @brief Remove a channel callback.<br>
	 * * The channel has been added with ssh_add_channel_callbacks or<br>
	 * ssh_set_channel_callbacks in this case.<br>
	 * * @param channel  The channel to remove the callback structure from.<br>
	 * * @param cb       The callback structure to remove<br>
	 * * @returns SSH_OK on success, SSH_ERROR on error.<br>
	 * Original signature : <code>int ssh_remove_channel_callbacks(ssh_channel, ssh_channel_callbacks)</code>
	 */
	int ssh_remove_channel_callbacks(ssh_channel channel, ssh_channel_callbacks cb);
	/**
	 * @group libssh_threads<br>
	 * @{
	 */
	typedef int (*ssh_thread_callback)(void** lock) ssh_thread_callback;
	typedef unsigned long (*ssh_thread_id_callback)() ssh_thread_id_callback;
	struct ssh_threads_callbacks_struct {
		const char* type;
		ssh_thread_callback mutex_init;
		ssh_thread_callback mutex_destroy;
		ssh_thread_callback mutex_lock;
		ssh_thread_callback mutex_unlock;
		ssh_thread_id_callback thread_id;
	};
	/**
	 * @brief Set the thread callbacks structure.<br>
	 * * This is necessary if your program is using libssh in a multithreaded fashion.<br>
	 * This function must be called first, outside of any threading context (in your<br>
	 * main() function for instance), before you call ssh_init().<br>
	 * * @param[in] cb   A pointer to a ssh_threads_callbacks_struct structure, which<br>
	 *                 contains the different callbacks to be set.<br>
	 * * @returns        Always returns SSH_OK.<br>
	 * * @see ssh_threads_callbacks_struct<br>
	 * @see SSH_THREADS_PTHREAD<br>
	 * @bug libgcrypt 1.6 and bigger backend does not support custom callback.<br>
	 *      Using anything else than pthreads here will fail.<br>
	 * Original signature : <code>int ssh_threads_set_callbacks(ssh_threads_callbacks_struct*)</code>
	 */
	int ssh_threads_set_callbacks(ssh_threads_callbacks_struct* cb);
	/**
	 * @brief returns a pointer on the pthread threads callbacks, to be used with<br>
	 * ssh_threads_set_callbacks.<br>
	 * @warning you have to link with the library ssh_threads.<br>
	 * @see ssh_threads_set_callbacks<br>
	 * Original signature : <code>ssh_threads_callbacks_struct* ssh_threads_get_pthread()</code>
	 */
	ssh_threads_callbacks_struct* ssh_threads_get_pthread();
	/**
	 * @brief Get the noop threads callbacks structure<br>
	 * * This can be used with ssh_threads_set_callbacks. These callbacks do nothing<br>
	 * and are being used by default.<br>
	 * * @return Always returns a valid pointer to the noop callbacks structure.<br>
	 * * @see ssh_threads_set_callbacks<br>
	 * Original signature : <code>ssh_threads_callbacks_struct* ssh_threads_get_noop()</code>
	 */
	ssh_threads_callbacks_struct* ssh_threads_get_noop();
	/**
	 * @brief Set the logging callback function.<br>
	 * * @param[in]  cb  The callback to set.<br>
	 * * @return         0 on success, < 0 on errror.<br>
	 * Original signature : <code>int ssh_set_log_callback(ssh_logging_callback)</code>
	 */
	int ssh_set_log_callback(ssh_logging_callback cb);
	/**
	 * @brief Get the pointer to the logging callback function.<br>
	 * * @return The pointer the the callback or NULL if none set.<br>
	 * Original signature : <code>ssh_logging_callback ssh_get_log_callback()</code>
	 */
	ssh_logging_callback ssh_get_log_callback();
	typedef int ssh_string_callback(ssh_session session, const char* user, int n_oid, ssh_string* oids, void* userdata);
}



extern ""C"" {
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
	typedef ssh_bind_struct* ssh_bind;
	/**
	 * @brief Incoming connection callback. This callback is called when a ssh_bind<br>
	 *        has a new incoming connection.<br>
	 * @param sshbind Current sshbind session handler<br>
	 * @param userdata Userdata to be passed to the callback function.
	 */
	typedef void (*ssh_bind_incoming_connection_callback)(ssh_bind sshbind, void* userdata) ssh_bind_incoming_connection_callback;
	/**
	 * @brief These are the callbacks exported by the ssh_bind structure.<br>
	 * They are called by the server module when events appear on the network.
	 */
	struct ssh_bind_callbacks_struct {
		size_t size;
		ssh_bind_incoming_connection_callback incoming_connection;
	};
	typedef ssh_bind_callbacks_struct* ssh_bind_callbacks;
	/**
	 * @brief Creates a new SSH server bind.<br>
	 * * @return A newly allocated ssh_bind session pointer.<br>
	 * Original signature : <code>ssh_bind ssh_bind_new()</code>
	 */
	ssh_bind ssh_bind_new();
	/** Original signature : <code>int ssh_bind_options_set(ssh_bind, ssh_bind_options_e, const void*)</code> */
	int ssh_bind_options_set(ssh_bind sshbind, ssh_bind_options_e type, const void* value);
	/**
	 * @brief Start listening to the socket.<br>
	 * * @param  ssh_bind_o     The ssh server bind to use.<br>
	 * * @return 0 on success, < 0 on error.<br>
	 * Original signature : <code>int ssh_bind_listen(ssh_bind)</code>
	 */
	int ssh_bind_listen(ssh_bind ssh_bind_o);
	/**
	 * @brief Set the callback for this bind.<br>
	 * * @param[in] sshbind   The bind to set the callback on.<br>
	 * * @param[in] callbacks An already set up ssh_bind_callbacks instance.<br>
	 * * @param[in] userdata  A pointer to private data to pass to the callbacks.<br>
	 * * @return              SSH_OK on success, SSH_ERROR if an error occured.<br>
	 * * @code<br>
	 *     struct ssh_callbacks_struct cb = {<br>
	 *         .userdata = data,<br>
	 *         .auth_function = my_auth_function<br>
	 *     };<br>
	 *     ssh_callbacks_init(&cb);<br>
	 *     ssh_bind_set_callbacks(session, &cb);<br>
	 * @endcode<br>
	 * Original signature : <code>int ssh_bind_set_callbacks(ssh_bind, ssh_bind_callbacks, void*)</code>
	 */
	int ssh_bind_set_callbacks(ssh_bind sshbind, ssh_bind_callbacks callbacks, void* userdata);
	/**
	 * @brief  Set the session to blocking/nonblocking mode.<br>
	 * * @param  ssh_bind_o     The ssh server bind to use.<br>
	 * * @param  blocking     Zero for nonblocking mode.<br>
	 * Original signature : <code>void ssh_bind_set_blocking(ssh_bind, int)</code>
	 */
	void ssh_bind_set_blocking(ssh_bind ssh_bind_o, int blocking);
	/**
	 * @brief Recover the file descriptor from the session.<br>
	 * * @param  ssh_bind_o     The ssh server bind to get the fd from.<br>
	 * * @return The file descriptor.<br>
	 * Original signature : <code>socket_t ssh_bind_get_fd(ssh_bind)</code>
	 */
	socket_t ssh_bind_get_fd(ssh_bind ssh_bind_o);
	/**
	 * @brief Set the file descriptor for a session.<br>
	 * * @param  ssh_bind_o     The ssh server bind to set the fd.<br>
	 * * @param  fd           The file descriptssh_bind B<br>
	 * Original signature : <code>void ssh_bind_set_fd(ssh_bind, socket_t)</code>
	 */
	void ssh_bind_set_fd(ssh_bind ssh_bind_o, socket_t fd);
	/**
	 * @brief Allow the file descriptor to accept new sessions.<br>
	 * * @param  ssh_bind_o     The ssh server bind to use.<br>
	 * Original signature : <code>void ssh_bind_fd_toaccept(ssh_bind)</code>
	 */
	void ssh_bind_fd_toaccept(ssh_bind ssh_bind_o);
	/**
	 * @brief Accept an incoming ssh connection and initialize the session.<br>
	 * * @param  ssh_bind_o     The ssh server bind to accept a connection.<br>
	 * @param  session			A preallocated ssh session<br>
	 * @see ssh_new<br>
	 * @return SSH_OK when a connection is established<br>
	 * Original signature : <code>int ssh_bind_accept(ssh_bind, ssh_session)</code>
	 */
	int ssh_bind_accept(ssh_bind ssh_bind_o, ssh_session session);
	/**
	 * @brief Accept an incoming ssh connection on the given file descriptor<br>
	 *        and initialize the session.<br>
	 * * @param  ssh_bind_o     The ssh server bind to accept a connection.<br>
	 * @param  session        A preallocated ssh session<br>
	 * @param  fd             A file descriptor of an already established TCP<br>
	 *                          inbound connection<br>
	 * @see ssh_new<br>
	 * @see ssh_bind_accept<br>
	 * @return SSH_OK when a connection is established<br>
	 * Original signature : <code>int ssh_bind_accept_fd(ssh_bind, ssh_session, socket_t)</code>
	 */
	int ssh_bind_accept_fd(ssh_bind ssh_bind_o, ssh_session session, socket_t fd);
	/** Original signature : <code>ssh_gssapi_creds ssh_gssapi_get_creds(ssh_session)</code> */
	ssh_gssapi_creds ssh_gssapi_get_creds(ssh_session session);
	/**
	 * @brief Handles the key exchange and set up encryption<br>
	 * * @param  session			A connected ssh session<br>
	 * @see ssh_bind_accept<br>
	 * @return SSH_OK if the key exchange was successful<br>
	 * Original signature : <code>int ssh_handle_key_exchange(ssh_session)</code>
	 */
	int ssh_handle_key_exchange(ssh_session session);
	/**
	 * @brief Free a ssh servers bind.<br>
	 * * @param  ssh_bind_o     The ssh server bind to free.<br>
	 * Original signature : <code>void ssh_bind_free(ssh_bind)</code>
	 */
	void ssh_bind_free(ssh_bind ssh_bind_o);
	/**
	 * @brief Set the acceptable authentication methods to be sent to the client.<br>
	 * *<br>
	 * @param[in]  session  The server session<br>
	 * * @param[in]  auth_methods The authentication methods we will support, which<br>
	 *                          can be bitwise-or'd.<br>
	 * *                          Supported methods are:<br>
	 * *                          SSH_AUTH_METHOD_PASSWORD<br>
	 *                          SSH_AUTH_METHOD_PUBLICKEY<br>
	 *                          SSH_AUTH_METHOD_HOSTBASED<br>
	 *                          SSH_AUTH_METHOD_INTERACTIVE<br>
	 *                          SSH_AUTH_METHOD_GSSAPI_MIC<br>
	 * Original signature : <code>void ssh_set_auth_methods(ssh_session, int)</code>
	 */
	void ssh_set_auth_methods(ssh_session session, int auth_methods);
	/**
	 * @brief Reply with a standard reject message.<br>
	 * * Use this function if you don't know what to respond or if you want to reject<br>
	 * a request.<br>
	 * * @param[in] msg       The message to use for the reply.<br>
	 * * @return              0 on success, -1 on error.<br>
	 * * @see ssh_message_get()<br>
	 * Original signature : <code>int ssh_message_reply_default(ssh_message)</code>
	 */
	int ssh_message_reply_default(ssh_message msg);
	/**
	 * @brief Get the name of the authenticated user.<br>
	 * * @param[in] msg       The message to get the username from.<br>
	 * * @return              The username or NULL if an error occured.<br>
	 * * @see ssh_message_get()<br>
	 * @see ssh_message_type()<br>
	 * Original signature : <code>char* ssh_message_auth_user(ssh_message)</code>
	 */
	const char* ssh_message_auth_user(ssh_message msg);
	/**
	 * @brief Get the password of the authenticated user.<br>
	 * * @param[in] msg       The message to get the password from.<br>
	 * * @return              The username or NULL if an error occured.<br>
	 * * @see ssh_message_get()<br>
	 * @see ssh_message_type()<br>
	 * Original signature : <code>char* ssh_message_auth_password(ssh_message)</code>
	 */
	const char* ssh_message_auth_password(ssh_message msg);
	/**
	 * @brief Get the publickey of the authenticated user.<br>
	 * * If you need the key for later user you should duplicate it.<br>
	 * * @param[in] msg       The message to get the public key from.<br>
	 * * @return              The public key or NULL.<br>
	 * * @see ssh_key_dup()<br>
	 * @see ssh_key_cmp()<br>
	 * @see ssh_message_get()<br>
	 * @see ssh_message_type()<br>
	 * Original signature : <code>ssh_key ssh_message_auth_pubkey(ssh_message)</code>
	 */
	ssh_key ssh_message_auth_pubkey(ssh_message msg);
	/** Original signature : <code>int ssh_message_auth_kbdint_is_response(ssh_message)</code> */
	int ssh_message_auth_kbdint_is_response(ssh_message msg);
	/** Original signature : <code>ssh_publickey_state_e ssh_message_auth_publickey_state(ssh_message)</code> */
	ssh_publickey_state_e ssh_message_auth_publickey_state(ssh_message msg);
	/** Original signature : <code>int ssh_message_auth_reply_success(ssh_message, int)</code> */
	int ssh_message_auth_reply_success(ssh_message msg, int partial);
	/** Original signature : <code>int ssh_message_auth_reply_pk_ok(ssh_message, ssh_string, ssh_string)</code> */
	int ssh_message_auth_reply_pk_ok(ssh_message msg, ssh_string algo, ssh_string pubkey);
	/** Original signature : <code>int ssh_message_auth_reply_pk_ok_simple(ssh_message)</code> */
	int ssh_message_auth_reply_pk_ok_simple(ssh_message msg);
	/** Original signature : <code>int ssh_message_auth_set_methods(ssh_message, int)</code> */
	int ssh_message_auth_set_methods(ssh_message msg, int methods);
	/** Original signature : <code>int ssh_message_auth_interactive_request(ssh_message, const char*, const char*, unsigned int, const char**, char*)</code> */
	int ssh_message_auth_interactive_request(ssh_message msg, const char* name, const char* instruction, unsigned int num_prompts, const char** prompts, char* echo);
	/** Original signature : <code>int ssh_message_service_reply_success(ssh_message)</code> */
	int ssh_message_service_reply_success(ssh_message msg);
	/** Original signature : <code>char* ssh_message_service_service(ssh_message)</code> */
	const char* ssh_message_service_service(ssh_message msg);
	/** Original signature : <code>int ssh_message_global_request_reply_success(ssh_message, uint16_t)</code> */
	int ssh_message_global_request_reply_success(ssh_message msg, uint16_t bound_port);
	/** Original signature : <code>void ssh_set_message_callback(ssh_session, ssh_set_message_callback_ssh_bind_message_callback_callback*, void*)</code> */
	void ssh_set_message_callback(ssh_session session, ssh_set_message_callback_ssh_bind_message_callback_callback* ssh_bind_message_callback, void* data);
	/** Original signature : <code>int ssh_execute_message_callbacks(ssh_session)</code> */
	int ssh_execute_message_callbacks(ssh_session session);
	/** Original signature : <code>char* ssh_message_channel_request_open_originator(ssh_message)</code> */
	const char* ssh_message_channel_request_open_originator(ssh_message msg);
	/** Original signature : <code>int ssh_message_channel_request_open_originator_port(ssh_message)</code> */
	int ssh_message_channel_request_open_originator_port(ssh_message msg);
	/** Original signature : <code>char* ssh_message_channel_request_open_destination(ssh_message)</code> */
	const char* ssh_message_channel_request_open_destination(ssh_message msg);
	/** Original signature : <code>int ssh_message_channel_request_open_destination_port(ssh_message)</code> */
	int ssh_message_channel_request_open_destination_port(ssh_message msg);
	/** Original signature : <code>ssh_channel ssh_message_channel_request_channel(ssh_message)</code> */
	ssh_channel ssh_message_channel_request_channel(ssh_message msg);
	/** Original signature : <code>char* ssh_message_channel_request_pty_term(ssh_message)</code> */
	const char* ssh_message_channel_request_pty_term(ssh_message msg);
	/** Original signature : <code>int ssh_message_channel_request_pty_width(ssh_message)</code> */
	int ssh_message_channel_request_pty_width(ssh_message msg);
	/** Original signature : <code>int ssh_message_channel_request_pty_height(ssh_message)</code> */
	int ssh_message_channel_request_pty_height(ssh_message msg);
	/** Original signature : <code>int ssh_message_channel_request_pty_pxwidth(ssh_message)</code> */
	int ssh_message_channel_request_pty_pxwidth(ssh_message msg);
	/** Original signature : <code>int ssh_message_channel_request_pty_pxheight(ssh_message)</code> */
	int ssh_message_channel_request_pty_pxheight(ssh_message msg);
	/** Original signature : <code>char* ssh_message_channel_request_env_name(ssh_message)</code> */
	const char* ssh_message_channel_request_env_name(ssh_message msg);
	/** Original signature : <code>char* ssh_message_channel_request_env_value(ssh_message)</code> */
	const char* ssh_message_channel_request_env_value(ssh_message msg);
	/** Original signature : <code>char* ssh_message_channel_request_command(ssh_message)</code> */
	const char* ssh_message_channel_request_command(ssh_message msg);
	/** Original signature : <code>char* ssh_message_channel_request_subsystem(ssh_message)</code> */
	const char* ssh_message_channel_request_subsystem(ssh_message msg);
	/** Original signature : <code>int ssh_message_channel_request_x11_single_connection(ssh_message)</code> */
	int ssh_message_channel_request_x11_single_connection(ssh_message msg);
	/** Original signature : <code>char* ssh_message_channel_request_x11_auth_protocol(ssh_message)</code> */
	const char* ssh_message_channel_request_x11_auth_protocol(ssh_message msg);
	/** Original signature : <code>char* ssh_message_channel_request_x11_auth_cookie(ssh_message)</code> */
	const char* ssh_message_channel_request_x11_auth_cookie(ssh_message msg);
	/** Original signature : <code>int ssh_message_channel_request_x11_screen_number(ssh_message)</code> */
	int ssh_message_channel_request_x11_screen_number(ssh_message msg);
	/** Original signature : <code>char* ssh_message_global_request_address(ssh_message)</code> */
	const char* ssh_message_global_request_address(ssh_message msg);
	/** Original signature : <code>int ssh_message_global_request_port(ssh_message)</code> */
	int ssh_message_global_request_port(ssh_message msg);
	/** Original signature : <code>int ssh_channel_open_reverse_forward(ssh_channel, const char*, int, const char*, int)</code> */
	int ssh_channel_open_reverse_forward(ssh_channel channel, const char* remotehost, int remoteport, const char* sourcehost, int localport);
	/** Original signature : <code>int ssh_channel_open_x11(ssh_channel, const char*, int)</code> */
	int ssh_channel_open_x11(ssh_channel channel, const char* orig_addr, int orig_port);
	/** Original signature : <code>int ssh_channel_request_send_exit_status(ssh_channel, int)</code> */
	int ssh_channel_request_send_exit_status(ssh_channel channel, int exit_status);
	/** Original signature : <code>int ssh_channel_request_send_exit_signal(ssh_channel, const char*, int, const char*, const char*)</code> */
	int ssh_channel_request_send_exit_signal(ssh_channel channel, const char* signum, int core, const char* errmsg, const char* lang);
	/** Original signature : <code>int ssh_send_keepalive(ssh_session)</code> */
	int ssh_send_keepalive(ssh_session session);
	/**
	 * deprecated functions<br>
	 * Original signature : <code>int ssh_accept(ssh_session)</code>
	 */
	int ssh_accept(ssh_session session);
	/** Original signature : <code>int channel_write_stderr(ssh_channel, const void*, uint32_t)</code> */
	int channel_write_stderr(ssh_channel channel, const void* data, uint32_t len);
	enum ssh_bind_options_e {
	};
	enum ssh_publickey_state_e {
	};
	typedef int ssh_set_message_callback_ssh_bind_message_callback_callback(ssh_session session, ssh_message msg, void* data);
}
