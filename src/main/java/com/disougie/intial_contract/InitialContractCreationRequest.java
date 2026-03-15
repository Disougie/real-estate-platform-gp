package com.disougie.intial_contract;

import jakarta.validation.constraints.NotNull;

public record InitialContractCreationRequest(
		@NotNull
		String property_id,
		Integer rentDuration
) {

}
