import org.junit.Test;
import org.saadahmed.snowcrystal.SnowCrystal;

import static org.junit.Assert.*;


public class SnowCrystalTest {

	@Test
	public void SnowCrystalPrint() {

		SnowCrystal crystal1 = SnowCrystal.create();
		SnowCrystal crystal2 = SnowCrystal.create();
		SnowCrystal crystal3 = SnowCrystal.create();
		SnowCrystal crystal4 = SnowCrystal.create();

		System.out.println("Hex Strings:");
		System.out.println(crystal1.toString());
		System.out.println(crystal2.toString());
		System.out.println(crystal3.toString());
		System.out.println(crystal4.toString());

		System.out.println("Base64 Strings:");
		System.out.println(crystal1.toBase64String());
		System.out.println(crystal2.toBase64String());
		System.out.println(crystal3.toBase64String());
		System.out.println(crystal4.toBase64String());

		System.out.println("Base64UrlSafe Strings:");
		System.out.println(crystal1.toBase64URLSafeString());
		System.out.println(crystal2.toBase64URLSafeString());
		System.out.println(crystal3.toBase64URLSafeString());
		System.out.println(crystal4.toBase64URLSafeString());

		System.out.println("Base64Hex Strings:");
		System.out.println(crystal1.toBase64HexString());
		System.out.println(crystal2.toBase64HexString());
		System.out.println(crystal3.toBase64HexString());
		System.out.println(crystal4.toBase64HexString());

	}
}
