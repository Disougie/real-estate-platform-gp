package com.disougie.imagekit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.disougie.property.entity.Image;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.exceptions.BadRequestException;
import io.imagekit.sdk.exceptions.ForbiddenException;
import io.imagekit.sdk.exceptions.InternalServerException;
import io.imagekit.sdk.exceptions.TooManyRequestsException;
import io.imagekit.sdk.exceptions.UnauthorizedException;
import io.imagekit.sdk.exceptions.UnknownException;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
	
	private final ImageKit imageKit;
	
	public List<Image> uploadImages(List<MultipartFile> fileImages){
		
		List<Image> images  = new ArrayList<>();
		for(MultipartFile image: fileImages) {			
			try {
				String imageName = image.getOriginalFilename() + UUID.randomUUID().toString();
				FileCreateRequest fileCreateRequest = new FileCreateRequest(
						image.getBytes(), imageName
				);			
				fileCreateRequest.setFolder("property_images");
				Result result = imageKit.upload(fileCreateRequest);
				images.add(
						new Image(result.getUrl(), result.getFileId())
				);
			} catch (InternalServerException | BadRequestException | UnknownException | ForbiddenException
					| TooManyRequestsException | UnauthorizedException | IOException e) {
				log.error(e.getMessage());
			}
		}
		
		return images;
	}
	
	public void deleteImage(String fileId) {
		try {
			imageKit.deleteFile(fileId);
		} catch (ForbiddenException | TooManyRequestsException | InternalServerException | UnauthorizedException
				| BadRequestException | UnknownException e) {
			log.error(e.getMessage());
		}
	}
}
