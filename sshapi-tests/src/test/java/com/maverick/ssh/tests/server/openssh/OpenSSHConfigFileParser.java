package com.maverick.ssh.tests.server.openssh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class OpenSSHConfigFileParser {

	private List<String> lines = new ArrayList<String>();

	public OpenSSHConfigFileParser(File file) throws IOException {
		load(file);
	}

	public void load(File file) throws IOException {
		lines = FileUtils.readLines(file);
	}
	
	public void save(File file) throws IOException {
		FileUtils.writeLines(file, lines);
	}

	public void set(String name, String value) {
		for (int i = lines.size() - 1 ; i >= 0 ; i--) {
			String l = lines.get(i);
			String varName = getName(l);
			if (varName.equals(name)) {
				if(isCommented(l)) {
					l = stripComment(l);
				}
				lines.set(i, varName + " " + value);
				return;
			}
		}
		lines.add(name + " " + value);
	}

	public void comment(String name) {
		for (int i = lines.size() - 1 ; i >= 0 ; i--) {
			String l = lines.get(i);
			String varName = getName(l);
			if (varName.equals(name)) {
				if(!isCommented(l)) {
					lines.set(i, "#" + varName + " " + getValue(l));
				}
				return;
			}
		}
	}
	
	boolean isCommented(String line) {
		line = line.trim();
		return line.startsWith("#");
	}

	String getValue(String line) {
		line = stripComment(line);
		int idx = line.indexOf(" ");
		if(idx > -1) {
			return line.substring(idx + 1);
		}
		return null;
	}

	String getName(String line) {
		line = stripComment(line);
		String[] args = line.split("\\s+");
		if(args.length > 0) {
			return args[0];
		}
		return null;
	}

	private String stripComment(String line) {
		line = line.trim();
		while (line.startsWith("#")) {
			line = line.substring(1);
		}
		return line.trim();
	}
}
