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
package net.sf.sshapi.sftp;

import java.util.HashMap;
import java.util.Map;

import net.sf.sshapi.SshException;

/**
 * Specialised {@link SshException} that is thrown by operations in
 * {@link SftpClient}. The SFTP server will return error codes that may be
 * useful to clients.
 * 
 */
public class SftpException extends SshException {

	private static final long serialVersionUID = 1L;

	/**
	 * SSH_FX_OK
	 */
	public static final SftpCode SSH_FX_OK = new SftpCode("sftp.ok", 0);
	/**
	 * SSH_FX_EOF
	 */
	public static final SftpCode SSH_FX_EOF = new SftpCode("sftp.eof", 1);
	/**
	 * SSH_FX_NO_SUCH_FILE
	 */
	public static final SftpCode SSH_FX_NO_SUCH_FILE = new SftpCode("sftp.noSuchFile", 2);
	/**
	 * SSH_FX_PERMISSION_DENIED
	 */
	public static final SftpCode SSH_FX_PERMISSION_DENIED = new SftpCode("sftp.permissionDenied", 3);
	/**
	 * SSH_FX_FAILURE
	 */
	public static final SftpCode SSH_FX_FAILURE = new SftpCode("sftp.failure", 4);
	/**
	 * SSH_FX_BAD_MESSAGE
	 */
	public static final SftpCode SSH_FX_BAD_MESSAGE = new SftpCode("sftp.badMessage", 5);
	/**
	 * SSH_FX_BAD_MESSAGE
	 */
	public static final SftpCode SSH_FX_NO_CONNECTION = new SftpCode("sftp.noConnection", 6);
	/**
	 * SSH_FX_NO_CONNECTION
	 */
	public static final SftpCode SSH_FX_CONNECTION_LOST = new SftpCode("sftp.connectionLost", 7);
	/**
	 * SSH_FX_OP_UNSUPPORTED
	 */
	public static final SftpCode SSH_FX_OP_UNSUPPORTED = new SftpCode("sftp.opUnsupported", 8);
	/**
	 * SSH_FX_NO_SUCH_PATH
	 */
	public static final SftpCode SSH_FX_INVALID_HANDLE = new SftpCode("sftp.invalidHandle", 9);
	/**
	 * SSH_FX_NO_SUCH_PATH
	 */
	public static final SftpCode SSH_FX_NO_SUCH_PATH = new SftpCode("sftp.noSuchPath", 10);
	/**
	 * SSH_FX_FILE_ALREADY_EXISTS
	 */
	public static final SftpCode SSH_FX_FILE_ALREADY_EXISTS = new SftpCode("sftp.fileAlreadyExists", 11);
	/**
	 * SSH_FX_WRITE_PROTECT
	 */
	public static final SftpCode SSH_FX_WRITE_PROTECT = new SftpCode("sftp.writeProtect", 12);
	/**
	 * SSH_FX_NO_MEDIA
	 */
	public static final SftpCode SSH_FX_NO_MEDIA = new SftpCode("sftp.noMedia", 13);
	/**
	 * SSH_FX_NO_SPACE_ON_FILESYSTEM
	 */
	public static final SftpCode SSH_FX_NO_SPACE_ON_FILESYSTEM = new SftpCode("sftp.noSpaceOnFileSystem", 14);
	/**
	 * SSH_FX_QUOTA_EXCEEDED
	 */
	public static final SftpCode SSH_FX_QUOTA_EXCEEDED = new SftpCode("sftp.quoteExceeded", 15);
	/**
	 * SSH_FX_UNKNOWN_PRINCIPAL
	 */
	public static final SftpCode SSH_FX_UNKNOWN_PRINCIPAL = new SftpCode("sftp.unknownPrincipal", 16);
	/**
	 * SSH_FX_LOCK_CONFLICT
	 */
	public static final SftpCode SSH_FX_LOCK_CONFLICT = new SftpCode("sftp.lockConflict", 17);
	/**
	 * SSH_FX_DIR_NOT_EMPTY
	 */
	public static final SftpCode SSH_FX_DIR_NOT_EMPTY = new SftpCode("sftp.dirNotEmpty", 18);
	/**
	 * SSH_FX_NOT_A_DIRECTORY
	 */
	public static final SftpCode SSH_FX_NOT_A_DIRECTORY = new SftpCode("sftp.notADirectory", 19);
	/**
	 * SSH_FX_INVALID_FILENAME
	 */
	public static final SftpCode SSH_FX_INVALID_FILENAME = new SftpCode("sftp.invalidFilename", 20);
	/**
	 * SSH_FX_LINK_LOOP
	 */
	public static final SftpCode SSH_FX_LINK_LOOP = new SftpCode("sftp.linkLoop", 21);
	/**
	 * SSH_FX_CANNOT_DELETE
	 */
	public static final SftpCode SSH_FX_CANNOT_DELETE = new SftpCode("sftp.cannotDelete", 22);
	/**
	 * SSH_FX_INVALID_PARAMETER
	 */
	public static final SftpCode SSH_FX_INVALID_PARAMETER = new SftpCode("sftp.invalidParameter", 23);
	/**
	 * SSH_FX_FILE_IS_A_DIRECTORY
	 */
	public static final SftpCode SSH_FX_FILE_IS_A_DIRECTORY = new SftpCode("sftp.fileIsADirectory", 24);
	/**
	 * SSH_FX_BYTE_RANGE_LOCK_CONFLICT
	 */
	public static final SftpCode SSH_FX_BYTE_RANGE_LOCK_CONFLICT = new SftpCode("sftp.byteRangeLockConflict", 25);
	/**
	 * SSH_FX_BYTE_RANGE_LOCK_REFUSED
	 */
	public static final SftpCode SSH_FX_BYTE_RANGE_LOCK_REFUSED = new SftpCode("sftp.byteRangeLockRefused", 26);
	/**
	 * SSH_FX_DELETE_PENDING
	 */
	public static final SftpCode SSH_FX_DELETE_PENDING = new SftpCode("sftp.deletePending", 27);
	/**
	 * SSH_FX_FILE_CORRUPT
	 */
	public static final SftpCode SSH_FX_FILE_CORRUPT = new SftpCode("sftp.fileCorrupt", 28);
	/**
	 * SSH_FX_OWNER_INVALID
	 */
	public static final SftpCode SSH_FX_OWNER_INVALID = new SftpCode("sftp.ownerInvalid", 29);
	/**
	 * SSH_FX_GROUP_INVALID
	 */
	public static final SftpCode SSH_FX_GROUP_INVALID = new SftpCode("sftp.groupInvalid", 30);
	/**
	 * SSH_FX_NO_MATCHING_BYTE_RANGE_LOCK
	 */
	public static final SftpCode SSH_FX_NO_MATCHING_BYTE_RANGE_LOCK = new SftpCode("sftp.noMatchingByteRangeLock", 31);
	/**
	 * Transfer cancelled
	 */
	public static final Code TRANSFER_CANCELLED = new Code("sftp.transferCancelled");/**
	 * Transfer cancelled
	 */
	public static final Code OUT_OF_BUFFER_SPACE = new Code("sftp.outOfBufferSpace");

	private static Map<Integer, Code> codes;

	/**
	 * Constructor
	 * 
	 * @param serverErrorCode server error code
	 */
	public SftpException(int serverErrorCode) {
		super(getCodeForServerCode(serverErrorCode));
	}

	/**
	 * Constructor
	 * 
	 * @param code error code
	 */
	public SftpException(Code code) {
		super(code);
	}

	/**
	 * Constructor
	 * 
	 * @param serverErrorCode server error code
	 * @param message message
	 */
	public SftpException(int serverErrorCode, String message) {
		super(getCodeForServerCode(serverErrorCode), message);
	}

	/**
	 * Constructor
	 * 
	 * @param serverErrorCode server error code
	 * @param message message
	 */
	public SftpException(Code serverErrorCode, String message) {
		super(serverErrorCode, message);
	}

	/**
	 * Constructor
	 * 
	 * @param serverErrorCode server error code
	 * @param message message
	 * @param cause cause
	 */
	public SftpException(Code serverErrorCode, String message, Throwable cause) {
		super(serverErrorCode, message, cause);
	}

	/**
	 * Constructor
	 * 
	 * @param serverErrorCode server error code
	 * @param cause cause
	 */
	public SftpException(Code serverErrorCode, Throwable cause) {
		super(serverErrorCode, cause);
	}

	/**
	 * Constructor
	 * 
	 * @param serverErrorCode server error code
	 * @param cause cause
	 */
	public SftpException(int serverErrorCode, Throwable cause) {
		super(getCodeForServerCode(serverErrorCode), cause);
	}

	/**
	 * Constructor
	 * 
	 * @param serverErrorCode server error code
	 * @param message message
	 * @param cause cause
	 */
	public SftpException(int serverErrorCode, String message, Throwable cause) {
		super(getCodeForServerCode(serverErrorCode), message, cause);
	}

	/**
	 * Get the native error code given an SFTP server error code.
	 * 
	 * @param serverCode SFTP server error code
	 * @return native error code
	 */
	public final static Code getCodeForServerCode(int serverCode) {
		Code code = (Code) codes.get(Integer.valueOf(serverCode));
		if(code == null) {
			/* Vendor code */
			return new SftpCode("vendor-" + serverCode, serverCode);
		} 
		else
			return code;
	}

	/**
	 * SFTP error code.
	 */
	public static class SftpCode extends Code {
		private int serverCode;

		/**
		 * Constructor.
		 * 
		 * @param id
		 * @param serverCode
		 */
		public SftpCode(String id, int serverCode) {
			super(id);
			if (codes == null) {
				codes = new HashMap<>();
			}
			codes.put(Integer.valueOf(serverCode), this);
			this.serverCode = serverCode;
		}
		
		/**
		 * The code.
		 * 
		 * @return code
		 */
		public int getServerCode() {
			return serverCode;
		}
	}
}
