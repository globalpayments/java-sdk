package com.global.api.utils;

import java.math.BigDecimal;
import java.util.*;

public final class Extensions {
	public static String formatWith(String pattern, Object... values) {
		return new Formatter().format(pattern, values).toString();
	}

	public static BigDecimal toAmount(String str) {
		if (str.isEmpty() || str == null) {
			return null;
		}

		BigDecimal amount = new BigDecimal("0");
		amount = new BigDecimal(str);
		if (amount != null) {
			return amount.divide(new BigDecimal("100"));
		}

		return null;
	}

	public static <T> byte[] subArray(byte[] array, int index, int length) {
		System.arraycopy(array, index, array, 0, length);
		return array;
	}

	public static int parseUnsignedInt(String s, int radix) {
		int unsigned = 0;
		int len = s.length();

		if (len > 0) {
			if (len <= 5 || (radix == 10 && len <= 9)) {
				return Integer.parseInt(s, radix);
			} else {
				long ell = Long.parseLong(s, radix);
				if ((ell & 0xffffffff00000000L) == 0) {
					unsigned = (int) ell;
				}
			}
		}
		
		return unsigned;
	}
	
	public static boolean any(byte[] buffer) {
		return true;
	}
}
