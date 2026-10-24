package com.concesionaria.notificacion.notificacion.controller;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.notificacion.notificacion.dto.NotificacionResponse;
import com.concesionaria.notificacion.notificacion.enums.EstadoNotificacion;
import com.concesionaria.notificacion.notificacion.enums.TipoNotificacion;
import com.concesionaria.notificacion.notificacion.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** RF-38: historial de notificaciones enviadas. */
@Tag(name = "Notificaciones", description = "Historial de emails enviados (RF-38)")
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    /** ?tipo=VENTA_CONFIRMADA&estado=FALLIDA&destinatario=juan@mail.com */
    @Operation(summary = "Historial de notificaciones", description = "?tipo=VENTA_CONFIRMADA&estado=FALLIDA&destinatario=juan@mail.com\n\n**Roles:** ADMIN.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<NotificacionResponse> buscar(@RequestParam(required = false) TipoNotificacion tipo,
                                             @RequestParam(required = false) EstadoNotificacion estado,
                                             @RequestParam(required = false) String destinatario,
                                             @RequestParam(required = false) Long referenciaId,
                                             @ParameterObject @PageableDefault(size = 20, sort = "fechaCreacion",
                                                     direction = Sort.Direction.DESC) Pageable pageable) {
        return notificacionService.buscar(tipo, estado, destinatario, referenciaId, pageable);
    }

    /** Los emails que recibió el usuario logueado. */
    @Operation(summary = "Mis notificaciones", description = "Los emails que recibió el usuario logueado.\n\n**Requiere login** (cualquier rol).")
    @GetMapping("/mias")
    public Page<NotificacionResponse> mias(@AuthenticationPrincipal Jwt jwt,
                                           @ParameterObject @PageableDefault(size = 20, sort = "fechaCreacion",
                                                   direction = Sort.Direction.DESC) Pageable pageable) {
        return notificacionService.buscar(null, null, UsuarioActual.desde(jwt).email(), null, pageable);
    }

    @Operation(summary = "Ver una notificación", description = "**Roles:** ADMIN.")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public NotificacionResponse obtener(@PathVariable Long id) {
        return notificacionService.obtener(id);
    }

    @Operation(summary = "Reenviar un email", description = "**Roles:** ADMIN.")
    @PostMapping("/{id}/reenviar")
    @PreAuthorize("hasRole('ADMIN')")
    public NotificacionResponse reenviar(@PathVariable Long id) {
        return notificacionService.reenviar(id);
    }
}
