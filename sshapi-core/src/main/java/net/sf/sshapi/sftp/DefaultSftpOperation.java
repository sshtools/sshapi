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
