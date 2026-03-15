package com.disougie.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReviewStarsValidator implements ConstraintValidator<ReviewStars, Double> {

	@Override
	public boolean isValid(Double stars, ConstraintValidatorContext context) {
		
		if(stars == null || stars < 0 || stars > 5)
			return false;
		
		return true;
	}

}
