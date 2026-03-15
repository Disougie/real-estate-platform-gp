package com.disougie.intial_contract;

public record initialContractResponse(
		
		long id,
		ContractData contract_data,
		UserData owner_data,
		UserData seeker_data,
		PropertyData property_data,
		FinancialData financial_data,
		InitialContractStatus status 
		
		
) {

}
