package com.disougie.property.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@Setter
@Getter
public class Review {
	
	private double stars;
	private int noOfReview ;
	
	public Review(double stars) {
		this.stars = stars;
		this.noOfReview = 1;
	}
	
	public void mergeReviews(Review review) {
		double curReviewsStars = this.stars * noOfReview;
		this.noOfReview += 1;
		this.stars =  (curReviewsStars + review.stars) / noOfReview ;
	}
	
}
