package com.disougie.intial_contract;

public record initialContractResponse(
		
		long id,
		String created_at,
		String expire_at,
		UserData owner_data,
		UserData seeker_data,
		PropertyData property_data,
		FinancialData financial_data,
		InitialContractStatus status 
		
		
) {

}
