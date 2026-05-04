package com.disougie.app_user.admin;

import static com.disougie.app_user.AppUserRole.ADMIN;
import static com.disougie.app_user.AppUserRole.LAWYER;
import static com.disougie.app_user.AppUserRole.USER;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.disougie.app_user.AppUserResponse;
import com.disougie.app_user.registration.RegistrationRequest;
import com.disougie.app_user.registration.RegistrationResponse;
import com.disougie.blog.BlogAdminsResponse;
import com.disougie.blog.BlogCreationResponse;
import com.disougie.blog.BlogRequest;
import com.disougie.property.PropertyAdminResponse;
import com.disougie.util.PageResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(
		name = "BearerAuth"
)
public class AdminController {
	
	private final AdminService adminService;
	
	/*
	 * 	Users Operations
	 */
	
	@GetMapping("users")
	public ResponseEntity<PageResponse<AppUserResponse>> getUsers(
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "10") int size
	){
		PageResponse<AppUserResponse> response = adminService
				.getAppUsers(USER, page, size);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("users/search")
	public ResponseEntity<PageResponse<AppUserResponse>> searchUser(
										@RequestParam String text,
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "10") int size
	){
		PageResponse<AppUserResponse> response = adminService
				.searchAppUser(text, USER, page, size);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("users")
	public ResponseEntity<RegistrationResponse> registerUser(
											@RequestBody RegistrationRequest request
	){
		RegistrationResponse response = adminService.registerAppUser(request, USER);
		return ResponseEntity.ok(response);
	}
	
	/*
	 * 	Lawyer Operations
	 */
	
	@GetMapping("lawyers")
	public ResponseEntity<PageResponse<AppUserResponse>> getLawyers(
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "10") int size
	){
		PageResponse<AppUserResponse> response = adminService
				.getAppUsers(LAWYER,page, size);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("lawyers/search")
	public ResponseEntity<PageResponse<AppUserResponse>> searchLawyer(
										@RequestParam String text,
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "10") int size
	){
		PageResponse<AppUserResponse> response = adminService
				.searchAppUser(text, LAWYER, page, size);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("lawyers")
	public ResponseEntity<RegistrationResponse> registerLawyer(
							@Validated @RequestBody RegistrationRequest request
	){
		RegistrationResponse response = adminService.registerAppUser(request,LAWYER);
		return ResponseEntity.ok(response);
	}
	
	/*
	 * 	Admins Operations
	 */
	
	@GetMapping("admins")
	public ResponseEntity<PageResponse<AppUserResponse>> getAdmins(
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "10") int size
	){
		PageResponse<AppUserResponse> response = adminService
				.getAppUsers(ADMIN, page, size);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("admins/search")
	public ResponseEntity<PageResponse<AppUserResponse>> searchAdmin(
										@RequestParam String text,
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "10") int size
	){
		PageResponse<AppUserResponse> response = adminService
				.searchAppUser(text, ADMIN, page, size);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("admins")
	public ResponseEntity<RegistrationResponse> registerAdmin(
							@Validated @RequestBody RegistrationRequest request
	){
		RegistrationResponse response = adminService.registerAppUser(request, ADMIN);
		return ResponseEntity.ok(response);
	}
	
	/*
	 * 	Common Delete Operation
	 */
	
	@DeleteMapping("users/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id){
		adminService.deleteAppUser(id);
		return null;
	}
	
	/*
	 * Properties Operations
	 */
	
	@GetMapping("properties")
	public ResponseEntity<PageResponse<PropertyAdminResponse>> getProperties(
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "10") int size
	){
		PageResponse<PropertyAdminResponse> response = adminService.getProperties(page, size);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("properties/search")
	public ResponseEntity<PageResponse<PropertyAdminResponse>> searchProperty(
										@RequestParam String text,
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "10") int size
	){
		PageResponse<PropertyAdminResponse> response = adminService
				.searchProperty(text, page, size);
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("properties/{id}")
	public ResponseEntity<?> deleteProperty(@PathVariable String id){
		adminService.deleteProperty(id);
		return ResponseEntity.noContent().build();
	}
	
	/*
	 * 	Blogs Operations
	 */
	
	@GetMapping("blogs")
	public ResponseEntity<PageResponse<BlogAdminsResponse>> getBlogs(
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "10") int size
	){
		PageResponse<BlogAdminsResponse> response = adminService.getBlogs(page, size);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("blogs/search")
	public ResponseEntity<PageResponse<BlogAdminsResponse>> searchBlog(
										@RequestParam String text,
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "10") int size
	){
		PageResponse<BlogAdminsResponse> response = adminService
				.searchBlog(text, page, size);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("blogs")
	public ResponseEntity<BlogCreationResponse> addBlog(
							@Validated @RequestBody BlogRequest request
	){
		BlogCreationResponse response = adminService.addBlog(request);
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("blogs/{id}")
	public ResponseEntity<?> deleteBlog(@PathVariable long id){
		adminService.deleteBlog(id);
		return ResponseEntity.noContent().build();
	}
	
	@PatchMapping("blogs/{id}")
	public ResponseEntity<?> updateBlog(@PathVariable long id, 
										@RequestBody BlogRequest request){
		adminService.updateBlog(id, request);
		return ResponseEntity.noContent().build();
	}

}
