package pe.edu.vallegrande.vgmsorganizations.infrastructure.rest.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsorganizations.application.services.FareService;
import pe.edu.vallegrande.vgmsorganizations.application.services.OrganizationService;
import pe.edu.vallegrande.vgmsorganizations.application.services.ParameterService;
import pe.edu.vallegrande.vgmsorganizations.application.services.StreetService;
import pe.edu.vallegrande.vgmsorganizations.application.services.ZoneService;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.FareRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.ParameterRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.StreetRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.ZoneRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.FareResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.OrganizationResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.ParameterResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.StreetResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.ZoneResponse;
import reactor.core.publisher.Mono;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

/**
 * Controlador REST para operaciones de ADMIN
 * El Gateway ya validó el token y los roles
 */
@RestController
@RequestMapping("/api/admin")
@Slf4j
@Validated
public class AdminRest {

	private final ZoneService zoneService;
	private final StreetService streetService;
	private final ParameterService parameterService;
	private final OrganizationService organizationService;
	private final FareService fareService;

	// Constructor explícito (PRS1 Standard - No usar @RequiredArgsConstructor)
	public AdminRest(ZoneService zoneService,
			StreetService streetService,
			ParameterService parameterService,
			OrganizationService organizationService,
			FareService fareService) {
		this.zoneService = zoneService;
		this.organizationService = organizationService;
		this.streetService = streetService;
		this.parameterService = parameterService;
		this.fareService = fareService;
	}

	// ===============================
	// GESTIÓN DE ZONAS
	// ===============================

	@PostMapping("/zones")
@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<ZoneResponse>>> createZone(@RequestBody ZoneRequest request) {
		return zoneService.create(request)
				.map(zone -> ResponseEntity.status(HttpStatus.CREATED)
						.body(new ResponseDto<>(true, zone, "Zona creada correctamente")))
				.onErrorResume(e -> {
					log.error("Error creating zone: {}", e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al crear la zona", e.getMessage()))));
				});
	}

	@GetMapping("/zones")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<List<ZoneResponse>>>> getAllZones() {
		return zoneService.findAll()
				.collectList()
				.map(zones -> ResponseEntity.ok(new ResponseDto<>(true, zones, "Zonas obtenidas correctamente")))
				.onErrorResume(e -> {
					log.error("Error fetching zones: {}", e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al obtener las zonas", e.getMessage()))));
				});
	}

	@GetMapping("/zones/organization/{organizationId}")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseDto<List<ZoneResponse>>> getZonesByOrganization(@PathVariable String organizationId) {
		return zoneService.findByOrganizationId(organizationId)
				.collectList()
				.map(zones -> new ResponseDto<>(true, zones))
				.onErrorResume(e -> {
					log.error("Error fetching zones for organization {}: {}", organizationId, e.getMessage());
					return Mono.just(new ResponseDto<>(false,
							new ErrorMessage(500, "Error al obtener las zonas de la organización", e.getMessage())));
				});
	}

	@GetMapping("/zones/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<ZoneResponse>>> getZoneById(@PathVariable String id) {
		return zoneService.findById(id)
				.map(zone -> ResponseEntity.ok(new ResponseDto<>(true, zone, "Zona obtenida correctamente")))
				.onErrorResume(e -> {
					log.error("Error fetching zone {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al obtener la zona", e.getMessage()))));
				});
	}

	@PutMapping("/zones/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<ZoneResponse>>> updateZone(@PathVariable String id,
			@RequestBody ZoneRequest request) {
		return zoneService.update(id, request)
				.map(zone -> ResponseEntity.ok(new ResponseDto<>(true, zone, "Zona actualizada correctamente")))
				.onErrorResume(e -> {
					log.error("Error updating zone {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al actualizar la zona", e.getMessage()))));
				});
	}

	@DeleteMapping("/zones/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<String>>> deleteZone(@PathVariable String id) {
		return zoneService.delete(id)
				.then(Mono.just(ResponseEntity
						.ok(new ResponseDto<>(true, "Zona eliminada correctamente", "Zona eliminada correctamente"))))
				.onErrorResume(e -> {
					log.error("Error deleting zone {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al eliminar la zona", e.getMessage()))));
				});
	}

	@PatchMapping("/zones/{id}/restore")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<String>>> restoreZone(@PathVariable String id) {
		return zoneService.restore(id)
				.then(Mono.just(ResponseEntity
						.ok(new ResponseDto<>(true, "Zona restaurada correctamente", "Zona restaurada correctamente"))))
				.onErrorResume(e -> {
					log.error("Error restoring zone {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al restaurar la zona", e.getMessage()))));
				});
	}

	// ===============================
	// GESTIÓN DE CALLES
	// ===============================

	@PostMapping("/streets")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<StreetResponse>>> createStreet(@RequestBody StreetRequest request) {
		return streetService.create(request)
				.map(street -> ResponseEntity.status(HttpStatus.CREATED)
						.body(new ResponseDto<>(true, street, "Calle creada correctamente")))
				.onErrorResume(e -> {
					log.error("Error creating street: {}", e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al crear la calle", e.getMessage()))));
				});
	}

	@GetMapping("/streets")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<List<StreetResponse>>>> getAllStreets() {
		return streetService.findAll()
				.collectList()
				.map(streets -> ResponseEntity.ok(new ResponseDto<>(true, streets, "Calles obtenidas correctamente")))
				.onErrorResume(e -> {
					log.error("Error fetching streets: {}", e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al obtener las calles", e.getMessage()))));
				});
	}

	@GetMapping("/streets/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<StreetResponse>>> getStreetById(@PathVariable String id) {
		return streetService.findById(id)
				.map(street -> ResponseEntity.ok(new ResponseDto<>(true, street, "Calle obtenida correctamente")))
				.onErrorResume(e -> {
					log.error("Error fetching street {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al obtener la calle", e.getMessage()))));
				});
	}

	@PutMapping("/streets/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<StreetResponse>>> updateStreet(@PathVariable String id,
			@RequestBody StreetRequest request) {
		return streetService.update(id, request)
				.map(street -> ResponseEntity.ok(new ResponseDto<>(true, street, "Calle actualizada correctamente")))
				.onErrorResume(e -> {
					log.error("Error updating street {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al actualizar la calle", e.getMessage()))));
				});
	}

	@DeleteMapping("/streets/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<String>>> deleteStreet(@PathVariable String id) {
		return streetService.delete(id)
				.then(Mono.just(ResponseEntity
						.ok(new ResponseDto<>(true, "Calle eliminada correctamente", "Calle eliminada correctamente"))))
				.onErrorResume(e -> {
					log.error("Error deleting street {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al eliminar la calle", e.getMessage()))));
				});
	}

	@PatchMapping("/streets/{id}/restore")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<String>>> restoreStreet(@PathVariable String id) {
		return streetService.restore(id)
				.then(Mono.just(ResponseEntity.ok(
						new ResponseDto<>(true, "Calle restaurada correctamente", "Calle restaurada correctamente"))))
				.onErrorResume(e -> {
					log.error("Error restoring street {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al restaurar la calle", e.getMessage()))));
				});
	}

	@GetMapping("/streets/zone/{zoneId}")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<List<StreetResponse>>>> getStreetsByZone(@PathVariable String zoneId) {
		return streetService.findByZoneId(zoneId)
				.collectList()
				.map(streets -> ResponseEntity.ok(new ResponseDto<>(true, streets, "Calles obtenidas correctamente")))
				.onErrorResume(e -> {
					log.error("Error fetching streets for zone {}: {}", zoneId, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al obtener las calles de la zona", e.getMessage()))));
				});
	}
	// ===============================
	// GESTIÓN DE ORGANIZACIÓN (VISTA DE ADMIN)
	// ===============================

	@GetMapping("/organization/{organizationId}")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
	public Mono<ResponseEntity<ResponseDto<OrganizationResponse>>> getMyOrganization(
			@PathVariable String organizationId) {
		return organizationService.findById(organizationId)
				.map(organization -> ResponseEntity
						.ok(new ResponseDto<>(true, organization, "Organización obtenida correctamente")))
				.onErrorResume(e -> {
					log.error("Error fetching organization {}: {}", organizationId, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al obtener la organización", e.getMessage()))));
				});
	}

	// ===============================
	// GESTIÓN DE PARÁMETROS
	// ===============================

	@GetMapping("/parameters")
	public Mono<ResponseEntity<ResponseDto<List<ParameterResponse>>>> findAllParameters() {
		return parameterService.findAll()
				.collectList()
				.map(list -> ResponseEntity.ok(new ResponseDto<>(true, list, "Parámetros obtenidos correctamente")))
				.onErrorResume(e -> {
					log.error("Error fetching parameters: {}", e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al obtener los parámetros", e.getMessage()))));
				});
	}

	@GetMapping("/parameters/organization/{organizationId}")
	public Mono<ResponseEntity<ResponseDto<List<ParameterResponse>>>> getParametersByOrganization(
			@PathVariable String organizationId) {
		return parameterService.findByOrganizationId(organizationId)
				.collectList()
				.map(list -> ResponseEntity
						.ok(new ResponseDto<>(true, list, "Parámetros de organización obtenidos correctamente")))
				.onErrorResume(e -> {
					log.error("Error fetching parameters for organization {}: {}", organizationId, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al obtener los parámetros de la organización",
											e.getMessage()))));
				});
	}

	@GetMapping("/parameters/{id}")
	public Mono<ResponseEntity<ResponseDto<ParameterResponse>>> findParameterById(@PathVariable String id) {
		return parameterService.findById(id)
				.map(param -> ResponseEntity.ok(new ResponseDto<>(true, param, "Parámetro obtenido correctamente")))
				.onErrorResume(e -> {
					log.error("Error fetching parameter {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(new ResponseDto<>(false,
									new ErrorMessage(404, "Parámetro no encontrado", e.getMessage()))));
				});
	}

	@PostMapping("/parameters")
	public Mono<ResponseEntity<ResponseDto<ParameterResponse>>> createParameter(@RequestBody ParameterRequest request) {
		return parameterService.create(request)
				.map(param -> ResponseEntity.status(HttpStatus.CREATED)
						.body(new ResponseDto<>(true, param, "Parámetro creado correctamente")))
				.onErrorResume(e -> {
					log.error("Error creating parameter: {}", e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(new ResponseDto<>(false,
									new ErrorMessage(400, "Error al crear el parámetro", e.getMessage()))));
				});
	}

	@PutMapping("/parameters/{id}")
	public Mono<ResponseEntity<ResponseDto<ParameterResponse>>> updateParameter(@PathVariable String id,
			@RequestBody ParameterRequest request) {
		return parameterService.update(id, request)
				.map(param -> ResponseEntity.ok(new ResponseDto<>(true, param, "Parámetro actualizado correctamente")))
				.onErrorResume(e -> {
					log.error("Error updating parameter {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(new ResponseDto<>(false,
									new ErrorMessage(400, "Error al actualizar el parámetro", e.getMessage()))));
				});
	}

	@DeleteMapping("/parameters/{id}")
	public Mono<ResponseEntity<ResponseDto<String>>> deleteParameter(@PathVariable String id) {
		return parameterService.delete(id)
				.then(Mono.just(
						ResponseEntity.ok(new ResponseDto<>(true, "Parámetro desactivado", "Parámetro desactivado"))))
				.onErrorResume(e -> {
					log.error("Error deleting parameter {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(new ResponseDto<>(false,
									new ErrorMessage(400, "Error al eliminar el parámetro", e.getMessage()))));
				});
	}

	@PatchMapping("/parameters/restore/{id}")
	public Mono<ResponseEntity<ResponseDto<String>>> restoreParameter(@PathVariable String id) {
		return parameterService.restore(id)
				.then(Mono.just(
						ResponseEntity.ok(new ResponseDto<>(true, "Parámetro restaurado", "Parámetro restaurado"))))
				.onErrorResume(e -> {
					log.error("Error restoring parameter {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(new ResponseDto<>(false,
									new ErrorMessage(400, "Error al restaurar el parámetro", e.getMessage()))));
				});
	}

	// ===============================
	// GESTIÓN DE TARIFAS
	// ===============================

	@PostMapping("/fare")
	public Mono<ResponseEntity<ApiResponse<FareResponse>>> createFare(@RequestBody FareRequest request) {
		return fareService.create(request)
				.map(fare -> ResponseEntity.status(HttpStatus.CREATED)
						.body(ApiResponse.success("Tarifa creada correctamente", fare)))
				.onErrorResume(e -> {
					log.error("Error creating fare: {}", e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(ApiResponse.error("Error al crear la tarifa",
									new ErrorMessage(500, "Error interno", e.getMessage()))));
				});
	}

	@GetMapping("/fare")
	public Mono<ResponseEntity<ApiResponse<List<FareResponse>>>> getAllFares() {
		return fareService.findAll()
				.collectList()
				.map(fares -> ResponseEntity.ok(ApiResponse.success("Tarifas obtenidas correctamente", fares)))
				.onErrorResume(e -> {
					log.error("Error fetching fares: {}", e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(ApiResponse.error("Error al obtener las tarifas",
									new ErrorMessage(500, "Error interno", e.getMessage()))));
				});
	}

	@GetMapping("/fare/{id}")
	public Mono<ResponseEntity<ApiResponse<FareResponse>>> getFareById(@PathVariable String id) {
		return fareService.findById(id)
				.map(fare -> ResponseEntity.ok(ApiResponse.success("Tarifa obtenida correctamente", fare)))
				.onErrorResume(e -> {
					log.error("Error fetching fare {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(ApiResponse.error("Error al obtener la tarifa",
									new ErrorMessage(500, "Error interno", e.getMessage()))));
				});
	}

	@GetMapping("/fare/zone/{zoneId}")
	public Mono<ResponseEntity<ApiResponse<List<FareResponse>>>> getFaresByZone(@PathVariable String zoneId) {
		return fareService.findByZoneId(zoneId)
				.collectList()
				.map(fares -> ResponseEntity
						.ok(ApiResponse.success("Tarifas de zona obtenidas correctamente", fares)))
				.onErrorResume(e -> {
					log.error("Error fetching fares for zone {}: {}", zoneId, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(ApiResponse.error("Error al obtener tarifas de la zona",
									new ErrorMessage(500, "Error interno", e.getMessage()))));
				});
	}

	@PutMapping("/fare/{id}")
	public Mono<ResponseEntity<ResponseDto<FareResponse>>> updateFare(@PathVariable String id,
			@RequestBody FareRequest request) {
		return fareService.update(id, request)
				.map(fare -> ResponseEntity.ok(new ResponseDto<>(true, fare, "Tarifa actualizada correctamente")))
				.onErrorResume(e -> {
					log.error("Error updating fare {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al actualizar la tarifa", e.getMessage()))));
				});
	}

	@DeleteMapping("/fare/delete/{id}")
	public Mono<ResponseEntity<ResponseDto<String>>> deleteFare(@PathVariable String id) {
		return fareService.delete(id)
				.then(Mono.just(ResponseEntity
						.ok(new ResponseDto<>(true, "Tarifa eliminada correctamente",
								"Tarifa eliminada correctamente"))))
				.onErrorResume(e -> {
					log.error("Error deleting fare {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al eliminar la tarifa", e.getMessage()))));
				});
	}

	@PatchMapping("/fare/restore/{id}")
	public Mono<ResponseEntity<ResponseDto<String>>> restoreFare(@PathVariable String id) {
		return fareService.restore(id)
				.then(Mono.just(ResponseEntity
						.ok(new ResponseDto<>(true, "Tarifa restaurada correctamente",
								"Tarifa restaurada correctamente"))))
				.onErrorResume(e -> {
					log.error("Error restoring fare {}: {}", id, e.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseDto<>(false,
									new ErrorMessage(500, "Error al restaurar la tarifa", e.getMessage()))));
				});
	}
}
