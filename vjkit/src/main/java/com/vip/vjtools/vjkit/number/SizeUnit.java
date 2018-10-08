package com.vip.vjtools.vjkit.number;

/**
 * Representation of basic size units，just like TimeUnit.
 * 
 * Usage example:
 * assertTrue(SizeUnit.BYTES.toMegaBytes(1024 * 1024) == 1.0);<br/>
 * assertTrue(SizeUnit.GIGABYTES.toBytes(1) == 1024.0 * 1024.0 * 1024.0);
 */
public enum SizeUnit {
	/** Smallest memory unit. */
	BYTES,
	/** "One thousand" (1024) bytes. */
	KILOBYTES,
	/** "One million" (1024x1024) bytes. */
	MEGABYTES,
	/** "One billion" (1024x1024x1024) bytes. */
	GIGABYTES;
	/** Number of bytes in a kilobyte. */
	private final double BYTES_PER_KILOBYTE = 1024.0;
	/** Number of kilobytes in a megabyte. */
	private final double KILOBYTES_PER_MEGABYTE = 1024.0;
	/** Number of megabytes per gigabyte. */
	private final double MEGABYTES_PER_GIGABYTE = 1024.0;

	/**
	 * Returns the number of bytes corresponding to the provided input for a particular unit of memory.
	 *
	 * @param input Number of units of memory.
	 * @return Number of bytes corresponding to the provided number of particular memory units.
	 */
	public double toBytes(final long input) {
		double bytes;
		switch (this) {
			case BYTES:
				bytes = input;
				break;
			case KILOBYTES:
				bytes = input * BYTES_PER_KILOBYTE;
				break;
			case MEGABYTES:
				bytes = input * BYTES_PER_KILOBYTE * KILOBYTES_PER_MEGABYTE;
				break;
			case GIGABYTES:
				bytes = input * BYTES_PER_KILOBYTE * KILOBYTES_PER_MEGABYTE * MEGABYTES_PER_GIGABYTE;
				break;
			default:
				throw new RuntimeException("No value '" + this + "' recognized for enum MemoryUnit.");
		}
		return bytes;
	}

	/**
	 * Returns the number of kilobytes corresponding to the provided input for a particular unit of memory.
	 *
	 * @param input Number of units of memory.
	 * @return Number of kilobytes corresponding to the provided number of particular memory units.
	 */
	public double toKiloBytes(final long input) {
		double kilobytes;
		switch (this) {
			case BYTES:
				kilobytes = input / BYTES_PER_KILOBYTE;
				break;
			case KILOBYTES:
				kilobytes = input;
				break;
			case MEGABYTES:
				kilobytes = input * KILOBYTES_PER_MEGABYTE;
				break;
			case GIGABYTES:
				kilobytes = input * KILOBYTES_PER_MEGABYTE * MEGABYTES_PER_GIGABYTE;
				break;
			default:
				throw new RuntimeException("No value '" + this + "' recognized for enum MemoryUnit.");
		}
		return kilobytes;
	}

	/**
	 * Returns the number of megabytes corresponding to the provided input for a particular unit of memory.
	 *
	 * @param input Number of units of memory.
	 * @return Number of megabytes corresponding to the provided number of particular memory units.
	 */
	public double toMegaBytes(final long input) {
		double megabytes;
		switch (this) {
			case BYTES:
				megabytes = input / BYTES_PER_KILOBYTE / KILOBYTES_PER_MEGABYTE;
				break;
			case KILOBYTES:
				megabytes = input / KILOBYTES_PER_MEGABYTE;
				break;
			case MEGABYTES:
				megabytes = input;
				break;
			case GIGABYTES:
				megabytes = input * MEGABYTES_PER_GIGABYTE;
				break;
			default:
				throw new RuntimeException("No value '" + this + "' recognized for enum MemoryUnit.");
		}
		return megabytes;
	}

	/**
	 * Returns the number of gigabytes corresponding to the provided input for a particular unit of memory.
	 *
	 * @param input Number of units of memory.
	 * @return Number of gigabytes corresponding to the provided number of particular memory units.
	 */
	public double toGigaBytes(final long input) {
		double gigabytes;
		switch (this) {
			case BYTES:
				gigabytes = input / BYTES_PER_KILOBYTE / KILOBYTES_PER_MEGABYTE / MEGABYTES_PER_GIGABYTE;
				break;
			case KILOBYTES:
				gigabytes = input / KILOBYTES_PER_MEGABYTE / MEGABYTES_PER_GIGABYTE;
				break;
			case MEGABYTES:
				gigabytes = input / MEGABYTES_PER_GIGABYTE;
				break;
			case GIGABYTES:
				gigabytes = input;
				break;
			default:
				throw new RuntimeException("No value '" + this + "' recognized for enum MemoryUnit.");
		}
		return gigabytes;
	}
}