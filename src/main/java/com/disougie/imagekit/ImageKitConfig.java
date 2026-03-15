package com.disougie.imagekit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.imagekit.sdk.ImageKit;

@Configuration
public class ImageKitConfig {
	
	@Value("${imagekit.url}")
	private String imagekitUrl;
	
	@Value("${imagekit.public.key}")
	private String imageketPublicKey;
	
	@Value("${imagekit.private.key}")
	private String imageketPrivateKey;
	
	@Bean
	ImageKit imageKit() {
		ImageKit imageKit = ImageKit.getInstance();
		io.imagekit.sdk.config.Configuration configuration = 
				new io.imagekit.sdk.config.Configuration();
		
		configuration.setUrlEndpoint(imagekitUrl);
		configuration.setPublicKey(imageketPublicKey);
		configuration.setPrivateKey(imageketPrivateKey);
		imageKit.setConfig(configuration);
		
		return imageKit;
	}

}
