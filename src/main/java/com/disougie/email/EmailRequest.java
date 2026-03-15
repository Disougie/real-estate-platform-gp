package com.disougie.email;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class EmailRequest implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String recipientName;
	private String recipientEmail;
	private String token;
	
}
