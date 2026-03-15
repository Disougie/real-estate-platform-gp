package com.disougie.app_user.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.app_user.AppUserResponse;
import com.disougie.app_user.AppUserResponseMapper;
import com.disougie.app_user.AppUserRole;
import com.disougie.app_user.registration.RegistrationRequest;
import com.disougie.app_user.registration.RegistrationResponse;
import com.disougie.app_user.registration.RegistrationService;
import com.disougie.blog.Blog;
import com.disougie.blog.BlogAdminsResponse;
import com.disougie.blog.BlogCreationResponse;
import com.disougie.blog.BlogRepository;
import com.disougie.blog.BlogRequest;
import com.disougie.exception.ResourceNotFoundException;
import com.disougie.property.PropertyAdminResponse;
import com.disougie.property.PropertyAdminResponseMapper;
import com.disougie.property.PropertyRepository;
import com.disougie.property.entity.Property;
import com.disougie.security.JwtService;
import com.disougie.util.PageResponse;
import com.disougie.util.PageResponseMapper;
import com.disougie.util.TimeUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

	
	private final RegistrationService registrationService;
	private final AppUserRepository appUserRepository;
	private final PropertyRepository propertyRepository;
	private final BlogRepository blogRepository;
	private final AppUserResponseMapper appUserResponseMapper; 
	private final PropertyAdminResponseMapper propertyAdminResponseMapper;
	@SuppressWarnings("rawtypes") 
	private final PageResponseMapper pageResponseMapper;

	@SuppressWarnings("unchecked")
	public PageResponse<AppUserResponse> getAppUsers(AppUserRole role, int page, int size) {
		Page<AppUserResponse> pageOfUsers = appUserRepository
				.findByRoleForAdmin(PageRequest.of(page, size), role.name())
				.map(appUserResponseMapper);
		return pageResponseMapper.apply(pageOfUsers);
	}

	@SuppressWarnings("unchecked")
	public PageResponse<AppUserResponse> searchAppUser(String text,
													   AppUserRole role, 
													   int page, 
													   int size) {
		Page<AppUserResponse> pageOfUsers = appUserRepository
				.searchByNameOrEmail(PageRequest.of(page, size), text, role.getRole())
				.map(appUserResponseMapper);
		return pageResponseMapper.apply(pageOfUsers);
	}

	public RegistrationResponse registerAppUser(RegistrationRequest request, 
												AppUserRole role) {
		return registrationService.registerUser(request, role);
	}

	public void deleteAppUser(Long id) {
		AppUser user = appUserRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("User not found")
		);
		appUserRepository.delete(user);
	}

	@SuppressWarnings("unchecked")
	public PageResponse<PropertyAdminResponse> getProperties(int page, int size) {
		Page<PropertyAdminResponse> pageOfProperties = propertyRepository
				.findAll(PageRequest.of(page, size))
				.map(propertyAdminResponseMapper);
		return pageResponseMapper.apply(pageOfProperties);
	}

	@SuppressWarnings("unchecked")
	public PageResponse<PropertyAdminResponse> searchProperty(String text, int page, int size) {
		Page<PropertyAdminResponse> pageOfProperties = propertyRepository
				.findByTextForAdmin(text, page, size)
				.map(propertyAdminResponseMapper);
		return pageResponseMapper.apply(pageOfProperties);
	}

	public void deleteProperty(String id) {
		Property property = propertyRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("Propety not found")
		);
		propertyRepository.delete(property);
	}

	@SuppressWarnings("unchecked")
	public PageResponse<BlogAdminsResponse> getBlogs(int page, int size) {
		Page<BlogAdminsResponse> pageOfBlogs = blogRepository
				.findAll(PageRequest.of(page, size))
				.map(blog -> new BlogAdminsResponse(
						blog.getId(), 
						blog.getWriter().getName(), 
						blog.getTitle(),
						blog.getContent(),
						blog.getCreatedAt()
					)
				);
		return pageResponseMapper.apply(pageOfBlogs);
	}

	@SuppressWarnings("unchecked")
	public PageResponse<BlogAdminsResponse> searchBlog(String text, int page, int size) {
		Page<BlogAdminsResponse> pageOfBlogs = blogRepository
				.findByContent(text, PageRequest.of(page, size))
				.map(blog -> new BlogAdminsResponse(
						blog.getId(), 
						blog.getWriter().getName(), 
						blog.getTitle(),
						blog.getContent(),
						blog.getCreatedAt()
					)
				);
		return pageResponseMapper.apply(pageOfBlogs);
	}

	public BlogCreationResponse addBlog(BlogRequest request) {
		Blog savedBlog = blogRepository.save(
				new Blog(
						null, 
						JwtService.getCurrentUser(),
						request.title(),
						request.content(),
						TimeUtil.now()
				)
		);
		return new BlogCreationResponse(savedBlog.getId());
	}

	public void deleteBlog(long id) {
		Blog blog = blogRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("blog not found")
		);
		blogRepository.delete(blog);
	}

	public void updateBlog(long id, BlogRequest request) {
		Blog blog = blogRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("blog not found")
		);
		if(request.title() != null)
			blog.setTitle(request.title());
		if(request.content() != null)
			blog.setContent(request.content());
		
		blogRepository.save(blog);
	}

}
