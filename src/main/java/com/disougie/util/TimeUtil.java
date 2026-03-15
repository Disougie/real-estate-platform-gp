package com.disougie.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeUtil {
	
	public static LocalDateTime now() {
		return LocalDateTime.now(ZoneId.of("Africa/Khartoum"));
	}
	
}
