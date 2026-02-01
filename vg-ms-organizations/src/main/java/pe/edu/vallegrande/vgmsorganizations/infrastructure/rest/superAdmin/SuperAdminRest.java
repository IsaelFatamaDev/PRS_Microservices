package pe.edu.vallegrande.vgmsorganizations.infrastructure.rest.superAdmin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.vallegrande.vgmsorganizations.application.services.OrganizationService;
import pe.edu.vallegrande.vgmsorganizations.application.services.UserService;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.client.external.UserAuthClient;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.CreateAdminResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.OrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.UserCreateRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.OrganizationResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.OrganizationWithAdminsResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.repository.OrganizationRepository;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

@RequestMapping("/api/management")
@RestController
@Slf4j
@Validated
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class SuperAdminRest {

	private final OrganizationService organizationService;
	private final UserService userService;
	private final OrganizationRepository organizationRepository;

	public SuperAdminRest(OrganizationService organizationService,
			UserService userService,
			OrganizationRepository organizationRepository,
			UserAuthClient userAuthClient) {
		this.organizationService = organizationService;
		this.userService = userService;
		this.organizationRepository = organizationRepository;
	}

	private final String logosPath = "uploads/logos";

	@PostMapping("/organizations")
	public Mono<ResponseEntity<ResponseDto<OrganizationResponse>>> createOrganization(
			@RequestBody OrganizationRequest request) {

		return organizationService.create(request)
				.map(organization -> ResponseEntity.status(HttpStatus.CREATED)
						.body(new ResponseDto<>(true, organization, "Organization created successfully")))
				.onErrorResume(e -> {
					return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(new ResponseDto<>(false,
									new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
											"Error creating organization",
											e.getMessage()))));
				});
	}

	@GetMapping("/organizations")
	public Mono<ResponseEntity<ResponseDto<List<OrganizationResponse>>>> getAllOrganizations() {
		return organizationService.findAll()
				.collectList()
				.map(organizations -> ResponseEntity
						.ok(new ResponseDto<>(true, organizations, "Organizations fetched successfully")))
				.onErrorResume(e -> {
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
											"Error fetching organizations",
											e.getMessage()))));
				});
	}

	@GetMapping("/organizations/admins")
	public Mono<ResponseEntity<ResponseDto<List<OrganizationWithAdminsResponse>>>> getOrganizationsWithAdmins() {
		return organizationService.getOrganizationsWithAdmins()
				.collectList()
				.map(result -> ResponseEntity
						.ok(new ResponseDto<>(true, result, "Organizations with admins fetched successfully")))
				.onErrorResume(e -> {
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
											"Error fetching organizations with admins",
											e.getMessage()))));
				});
	}

	@GetMapping("/organizations/{id}")
	public Mono<ResponseEntity<ResponseDto<OrganizationResponse>>> getOrganizationById(
			@PathVariable String id) {

		return organizationService.findById(id)
				.map(organization -> ResponseEntity
						.ok(new ResponseDto<>(true, organization, "Organization fetched successfully")))
				.onErrorResume(e -> {
					return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(new ResponseDto<>(false,
									new ErrorMessage(HttpStatus.NOT_FOUND.value(),
											"Organization not found",
											e.getMessage()))));
				});
	}

	@PostMapping("/organizations/{id}/create-admin")
	public Mono<ResponseEntity<ResponseDto<CreateAdminResponse>>> createAdmin(
			@PathVariable String id,
			@RequestBody UserCreateRequest request) {

		return userService.createAdmin(request, id)
				.map(adminResponse -> ResponseEntity.status(HttpStatus.CREATED)
						.body(new ResponseDto<>(true, adminResponse, "Admin created successfully")))
				.onErrorResume(e -> {
					return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(new ResponseDto<>(false,
									new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
											"Error creating admin",
											e.getMessage()))));
				});
	}

	@PutMapping("/organizations/{id}")
	public Mono<ResponseEntity<ResponseDto<OrganizationResponse>>> updateOrganization(
			@PathVariable String id,
			@RequestBody OrganizationRequest request) {

		return organizationService.update(id, request)
				.map(organization -> ResponseEntity
						.ok(new ResponseDto<>(true, organization, "Organization updated successfully")))
				.onErrorResume(e -> {
					return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(new ResponseDto<>(false,
									new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
											"Error updating organization",
											e.getMessage()))));
				});
	}

	@DeleteMapping("/organizations/{id}")
	public Mono<ResponseEntity<ResponseDto<String>>> deleteOrganization(
			@PathVariable String id) {

		return organizationService.delete(id)
				.then(Mono.just(ResponseEntity.ok(new ResponseDto<>(true, "Organization deleted successfully",
						"Organization deleted successfully"))))
				.onErrorResume(e -> {
					return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(new ResponseDto<>(false,
									new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
											"Error deleting organization",
											e.getMessage()))));
				});
	}

	@PatchMapping("/organizations/{id}/restore")
	public Mono<ResponseEntity<ResponseDto<String>>> restoreOrganization(
			@PathVariable String id) {

		return organizationService.restore(id)
				.then(Mono.just(ResponseEntity.ok(new ResponseDto<>(true, "Organization restored successfully",
						"Organization restored successfully"))))
				.onErrorResume(e -> {
					return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(new ResponseDto<>(false,
									new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
											"Error restoring organization",
											e.getMessage()))));
				});
	}

	@GetMapping("/organizations/simple")
	public Mono<ResponseEntity<ResponseDto<List<Object>>>> getOrganizationsSimple() {
		// Endpoint súper simple, solo datos básicos
		return organizationRepository
				.findAllByStatus(pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants.ACTIVE)
				.map(org -> java.util.Map.of(
						"organizationId", org.getOrganizationId(),
						"organizationCode", org.getOrganizationCode() != null ? org.getOrganizationCode() : "",
						"organizationName", org.getOrganizationName(),
						"legalRepresentative", org.getLegalRepresentative(),
						"status", org.getStatus()))
				.cast(Object.class)
				.collectList()
				.map(organizations -> ResponseEntity
						.ok(new ResponseDto<>(true, organizations, "Organizations simple list fetched successfully")))
				.onErrorResume(e -> {
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
											"Error fetching organizations",
											e.getMessage()))));
				});
	}

	@GetMapping("/organizations/statistics")
	public Mono<ResponseEntity<ResponseDto<Object>>> getStatistics() {

		return organizationService.findAllLight()
				.count()
				.map(totalOrganizations -> {
					Object statisticsData = java.util.Map.of(
							"totalOrganizations", totalOrganizations,
							"timestamp", java.time.LocalDateTime.now());
					return ResponseEntity
							.ok(new ResponseDto<>(true, statisticsData, "Statistics fetched successfully"));
				})
				.onErrorResume(e -> {
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
											"Error fetching statistics",
											e.getMessage()))));
				});
	}

	/**
	 * Endpoint to serve organization logos
	 * This provides an alternative way to access logos through the management API
	 */
	@GetMapping("/organizations/logos/{fileName}")
	public Mono<ResponseEntity<Resource>> getOrganizationLogo(@PathVariable String fileName) {
		return Mono.fromCallable(() -> {
			try {
				Path filePath = Paths.get(logosPath, fileName);

				if (!Files.exists(filePath)) {
					return ResponseEntity.notFound().build();
				}

				Resource resource = new FileSystemResource(filePath);

				// Determine content type based on file extension
				String contentType = getContentType(fileName);

				return ResponseEntity.ok()
						.contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
						.header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache for 1 hour
						.body(resource);

			} catch (Exception e) {
				return ResponseEntity.internalServerError().build();
			}
		});
	}

	/**
	 * Determines content type based on file extension
	 */
	private String getContentType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

		switch (extension) {
			case "jpg":
			case "jpeg":
				return "image/jpeg";
			case "png":
				return "image/png";
			case "webp":
				return "image/webp";
			default:
				return "application/octet-stream";
		}
	}

}