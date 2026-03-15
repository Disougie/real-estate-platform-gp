package com.disougie.blog;

import jakarta.validation.constraints.NotEmpty;

public record BlogRequest(@NotEmpty String title, @NotEmpty String content) {

}
