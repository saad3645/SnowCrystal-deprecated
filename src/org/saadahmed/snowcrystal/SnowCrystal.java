package org.saadahmed.snowcrystal;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.saadahmed.codec.Base64Hex;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.sql.Timestamp;


/**
 *
 * @author Saad Ahmed
 */
public class SnowCrystal {

	/*
	 * Timestamp bits:             64
	 * Mac bits:                   48
	 * Sequence bits:              16
	 *                            ---
	 * Total bits:                128
	 * Total bytes:                16
	 * Base64 String length:       24/22
	 */

	public static final int TIMESTAMP_LENGTH = 8;
	public static final int MACHINE_LENGTH = 6;
	public static final int SEQUENCE_LENGTH = 2;
	public static final int SNOWCRYSTAL_LENGTH = TIMESTAMP_LENGTH + MACHINE_LENGTH + SEQUENCE_LENGTH;


	private static long lastTimestamp;
	private static byte[] mac;
	private static short lastSequence;


	private final byte[] binary;


	protected SnowCrystal(long timestamp, byte[] macAddress, short sequence) {

		ByteBuffer buffer = ByteBuffer.allocate(SNOWCRYSTAL_LENGTH);
		buffer.putLong(timestamp);

		// if the MAC address is less than 48 bits (6 bytes) then
		// copy the first n bytes and fill the remaining with 0s.
		if (macAddress.length < MACHINE_LENGTH) {
			buffer.put(macAddress);

			for (int i = 0; i < MACHINE_LENGTH - macAddress.length; i++) {
				buffer.put((byte)0);
			}
		}

		// if the MAC address is bigger than 48 bits (6 bytes), then
		// keep the first 48 bits and discard the rest
		else if (macAddress.length > MACHINE_LENGTH) {
			buffer.put(macAddress, 0, MACHINE_LENGTH);
		}

		else if (macAddress.length == MACHINE_LENGTH) {
			buffer.put(macAddress);
		}

		buffer.putShort(sequence);

		this.binary = buffer.array();
	}

	protected SnowCrystal(byte[] binary) {
		if (binary.length < SNOWCRYSTAL_LENGTH) {
			this.binary = new byte[SNOWCRYSTAL_LENGTH];

			int i = 0;
			for (; i < binary.length; i++) {
				this.binary[i] = binary[i];
			}

			for (; i < this.binary.length; i++) {
				this.binary[i] = 0;
			}
		}

		else {
			this.binary = new byte[SNOWCRYSTAL_LENGTH];
			System.arraycopy(binary, 0, this.binary, 0, SNOWCRYSTAL_LENGTH);
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof SnowCrystal) {
			return (this.toString().equals(object.toString()));
		}

		else return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	public long timestamp() {
		return ByteBuffer.wrap(binary, 0, TIMESTAMP_LENGTH).getLong();
	}

	public byte[] machineId() {
		return ByteBuffer.wrap(binary, TIMESTAMP_LENGTH, MACHINE_LENGTH).array();
	}

	public String machineIdHex() {
		return Hex.encodeHexString(ByteBuffer.wrap(binary, TIMESTAMP_LENGTH, MACHINE_LENGTH).array());
	}

	public short sequence() {
		return ByteBuffer.wrap(binary, (TIMESTAMP_LENGTH + MACHINE_LENGTH), SEQUENCE_LENGTH).getShort();
	}

	public byte[] unwrap() {
		return binary;
	}

	@Override
	public String toString() {
		return Hex.encodeHexString(binary);
	}

	public String toBase64String() {
		return Base64.encodeBase64String(binary);
	}

	public String toBase64URLSafeString() {
		return Base64.encodeBase64URLSafeString(binary);
	}

	public String toBase64HexString() {
		return Base64Hex.encodeBase64HexString(binary);
	}


	public static void initialize() {
		try {
			InetAddress ip = InetAddress.getLocalHost();
			SnowCrystal.mac = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
		}

		catch (Exception e) {
			e.printStackTrace();
			SnowCrystal.mac = new byte[] {0,0,0,0,0,0};
		}

		SnowCrystal.lastSequence = -1;
	}

	public static void setMacAddress(byte[] macAddress) {
		SnowCrystal.mac = macAddress;
	}

	public static SnowCrystal create() {
		if (mac == null) {
			initialize();
		}

		return generate();
	}

	public static SnowCrystal create(byte[] macAddress) {
		setMacAddress(macAddress);
		return create();
	}

	public static SnowCrystal create(long timestamp, byte[] macAddress, short sequence) {
		return new SnowCrystal(timestamp, macAddress, sequence);
	}

	public static SnowCrystal createFromBytes(byte[] binary) {
		return new SnowCrystal(binary);
	}

	public static SnowCrystal createFromHexString(String hexString) throws DecoderException {
		return new SnowCrystal(Hex.decodeHex(hexString.toCharArray()));
	}

	public static SnowCrystal createFromBase64String(String base64String) {
		return new SnowCrystal(Base64.decodeBase64(base64String));
	}

	public static SnowCrystal createFromBase64HexString(String base64HexString) {
		return new SnowCrystal(Base64Hex.decodeBase64Hex(base64HexString));
	}

	public static String hexString() {
		return create().toString();
	}

	public static String hexString(byte[] macAddress) {
		return create(macAddress).toString();
	}

	public static String base64String() {
		return create().toBase64String();
	}

	public static String base64String(byte[] macAddress) {
		return create(macAddress).toBase64String();
	}

	public static String base64UrlSafeString() {
		return create().toBase64URLSafeString();
	}

	public static String base64UrlSafeString(byte[] macAddress) {
		return create(macAddress).toBase64URLSafeString();
	}

	public static String base64HexString() {
		return create().toBase64HexString();
	}

	public static String base64HexString(byte[] macAddress) {
		return create(macAddress).toBase64HexString();
	}


	private static SnowCrystal generate() {
		long timestamp;
		short sequence;

		synchronized (SnowCrystal.class) {
			timestamp = System.currentTimeMillis();

			if (timestamp < lastTimestamp) {
				throw new SnowCrystalException("Clock has rolled backwards: current system time [" + new Timestamp(timestamp) +
						"] < last checked time [" + new Timestamp(lastTimestamp) + "]");
			}

			if (timestamp == lastTimestamp) {

				// Sequence rollover protection
				// if current sequence = 1111111111111111, incrementing would make
				// it rollover. So go to sleep for 1 millisecond while the thread
				// is locked. After the thread wakes up, the clock will have moved
				// forward, and then it will be safe to reset the sequence
				if (lastSequence == -1) {
					do {
						try {
							Thread.sleep(1);
						}

						catch (InterruptedException e) {
							e.printStackTrace();
						}

						timestamp = System.currentTimeMillis();

					// check to make sure the clock has indeed moved forward
					// in case we were woken up by interruption
					} while(timestamp == lastTimestamp);
				}

				sequence = (short)(lastSequence + 1);
			}

			else {
				sequence = 0;
			}

			// don't forget to update the last used timestamp and sequence!
			lastTimestamp = timestamp;
			lastSequence = sequence;
		}

		return new SnowCrystal(timestamp, SnowCrystal.mac, sequence);
	}

}
