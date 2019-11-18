package com.maverick.ssh.tests;

public class Size {

	public long size;

	public Size tib(long tib) {
		size += (tib * 1024l * 1024l * 1024l * 1024l);
		return this;
	}

	public Size tb(long tb) {
		size += (tb * 1000l * 1000l * 1000l * 1000l);
		return this;
	}

	public Size gib(long gib) {
		size += (gib * 1024l * 1024l * 1024l);
		return this;
	}

	public Size gb(long gb) {
		size += (gb * 1000l * 1000l * 1000l);
		return this;
	}

	public Size mib(long mib) {
		size += (mib * 1024l * 1024l);
		return this;
	}

	public Size mb(long mb) {
		size += (mb * 1000l * 1000l);
		return this;
	}

	public Size kib(long kib) {
		size += (kib * 1024l);
		return this;
	}

	public Size kb(long kb) {
		size += (kb * 1000l);
		return this;
	}

	public Size b(long b) {
		size += b;
		return this;
	}

	public static Size size() {
		return new Size();
	}

	public long toBytes() {
		return size;
	}

	public int toBytesInt() {
		return (int) size;
	}

	public long toKib() {
		return size / 1024l;
	}

	public int toKibInt() {
		return (int) (size / 1024l);
	}

	public double toKibDouble() {
		return (double) size / 1024.0;
	}

	public long toKb() {
		return size / 1000l;
	}

	public int toKbInt() {
		return (int) (size / 1000l);
	}

	public double toKbDouble() {
		return (double) size / 1000.0;
	}

	public long toMib() {
		return size / 1024l / 1024l;
	}

	public int toMibInt() {
		return (int) (size / 1024l / 1024l);
	}

	public double toMibDouble() {
		return (double) size / 1024.0 / 1024.0;
	}

	public long toMb() {
		return size / 1000l / 1000l;
	}

	public int toMbInt() {
		return (int) (size / 1000l / 1000l);
	}

	public double toMbDouble() {
		return (double) size / 1000.0 / 1000.0;
	}

	public long toGib() {
		return size / 1024l / 1024l / 1024l;
	}

	public int toGibInt() {
		return (int) (size / 1024l / 1024l / 1024l);
	}

	public double toGibDouble() {
		return (double) size / 1024.0 / 1024.0 / 1024.0;
	}

	public long toGb() {
		return size / 1000l / 1000l / 1000l;
	}

	public int toGbInt() {
		return (int) (size / 1000l / 1000l / 1000l);
	}

	public double toGbDouble() {
		return (double) size / 1000.0 / 1000.0 / 1000.0;
	}

	public long toTib() {
		return size / 1024l / 1024l / 1024l / 1024l;
	}

	public int toTibInt() {
		return (int) (size / 1024l / 1024l / 1024l / 1024l);
	}

	public double toTibDouble() {
		return (double) size / 1024.0 / 1024.0 / 1024.0 / 1024.0;
	}

	public long toTb() {
		return size / 1000l / 1000l / 1000l / 1000l;
	}

	public int toTbInt() {
		return (int) (size / 1000l / 1000l / 1000l / 1000l);
	}

	public double toTbDouble() {
		return (double) size / 1000.0 / 1000.0 / 1000.0 / 1000.0;
	}
}
