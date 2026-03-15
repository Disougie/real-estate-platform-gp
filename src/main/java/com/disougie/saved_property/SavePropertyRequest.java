package com.disougie.saved_property;

import jakarta.validation.constraints.NotNull;

public record SavePropertyRequest(@NotNull String property_id) {

}
