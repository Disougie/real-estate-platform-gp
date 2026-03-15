package com.disougie.open_api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiDocController {
	
	@GetMapping("/")
	public String apiDoc(){
		return "redirect:/swagger-ui/index.html";
	}
}
