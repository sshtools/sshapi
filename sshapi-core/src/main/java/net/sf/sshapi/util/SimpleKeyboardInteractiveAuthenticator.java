/* 
 * Copyright (c) 2010 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.sshapi.auth.SshKeyboardInteractiveAuthenticator;

/**
 * Simple keyboard interactive authenticator that return replies with a fixed
 * set of answers.
 */
public class SimpleKeyboardInteractiveAuthenticator implements SshKeyboardInteractiveAuthenticator {

	private String[] answers;

	public SimpleKeyboardInteractiveAuthenticator(String answer) {
		this(new String[] { answer });
	}

	public SimpleKeyboardInteractiveAuthenticator(String[] answers) {
		this.answers = answers;
	}

	public String[] challenge(String name, String instruction, String[] prompt, boolean[] echo) {
		List<String> answers = new ArrayList<>(Arrays.asList(this.answers));
		while(prompt.length < answers.size()) {
			answers.remove(answers.size() - 1);
		}
		while(prompt.length > answers.size()) {
			answers.add("");
		}
		return (String[])answers.toArray(new String[0]);
	}

	public String getTypeName() {
		return "keyboard-interactive";
	}

}
