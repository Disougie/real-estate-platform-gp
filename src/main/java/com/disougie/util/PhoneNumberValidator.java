package com.disougie.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
	
	private String region;
	
	@Override
	public void initialize(PhoneNumber constraintAnnotation) {
		this.region = constraintAnnotation.region();
	}

	@Override
	public boolean isValid(String phone, ConstraintValidatorContext context) {
		
		if(phone.isBlank())
			return false;
		
		PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
		
		try {
			Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phone, region);
			
			return phoneNumberUtil.isValidNumber(phoneNumber);
			
		} catch (NumberParseException e) {
			return false;
		}

	}

}
