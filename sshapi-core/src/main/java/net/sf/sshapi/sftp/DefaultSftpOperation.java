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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultSftpOperation implements SftpOperation {
	private List<String> deleted = new ArrayList<String>();
	private List<String> unchanged = new ArrayList<String>();
	private List<String> updated = new ArrayList<String>();
	private List<String> created = new ArrayList<String>();
	private long size;
	private Map<String, Exception> errors = new HashMap<String, Exception>();

	@Override
	public List<String> all() {
		Set<String> all = new LinkedHashSet<>();
		all.addAll(deleted);
		all.addAll(unchanged);
		all.addAll(updated);
		all.addAll(created);
		return new ArrayList<>(all);
	}

	@Override
	public List<String> deleted() {
		return deleted;
	}

	@Override
	public List<String> unchanged() {
		return unchanged;
	}

	@Override
	public List<String> updated() {
		return updated;
	}

	@Override
	public List<String> created() {
		return created;
	}

	@Override
	public Map<String, Exception> errors() {
		return errors;
	}

	@Override
	public long size() {
		return size;
	}

	@Override
	public long files() {
		return updated.size() + created.size() + unchanged.size() + deleted.size();
	}

	public void increaseSize(long size) {
		this.size += size;
	}

	public void add(SftpOperation op) {
		updated.addAll(op.updated());
		created.addAll(op.created());
		unchanged.addAll(op.unchanged());
		deleted.addAll(op.deleted());
		errors.putAll(op.errors());
	}

	public boolean contains(String path) {
		return updated.contains(path) || created.contains(path) || deleted.contains(path) || unchanged.contains(path);
	}

	@Override
	public String toString() {
		return String.format("All: %d, Updated: %d, Created: %d, Unchanged: %d, Deleted: %d, Errors: %d", files(), updated.size(),
				created.size(), unchanged.size(), deleted.size(), errors.size());
	}
}
