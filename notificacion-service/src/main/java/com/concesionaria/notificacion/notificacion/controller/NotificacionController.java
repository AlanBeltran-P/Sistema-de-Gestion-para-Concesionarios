package com.concesionaria.notificacion.notificacion.controller;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.notificacion.notificacion.dto.NotificacionResponse;
import com.concesionaria.notificacion.notificacion.enums.EstadoNotificacion;
import com.concesionaria.notificacion.notificacion.enums.TipoNotificacion;
import com.concesionaria.notificacion.notificacion.service.NotificacionService;
import lombok.RequiredArgsConstructor;
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
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    /** ?tipo=VENTA_CONFIRMADA&estado=FALLIDA&destinatario=juan@mail.com */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<NotificacionResponse> buscar(@RequestParam(required = false) TipoNotificacion tipo,
                                             @RequestParam(required = false) EstadoNotificacion estado,
                                             @RequestParam(required = false) String destinatario,
                                             @RequestParam(required = false) Long referenciaId,
                                             @PageableDefault(size = 20, sort = "fechaCreacion",
                                                     direction = Sort.Direction.DESC) Pageable pageable) {
        return notificacionService.buscar(tipo, estado, destinatario, referenciaId, pageable);
    }

    /** Los emails que recibió el usuario logueado. */
    @GetMapping("/mias")
    public Page<NotificacionResponse> mias(@AuthenticationPrincipal Jwt jwt,
                                           @PageableDefault(size = 20, sort = "fechaCreacion",
                                                   direction = Sort.Direction.DESC) Pageable pageable) {
        return notificacionService.buscar(null, null, UsuarioActual.desde(jwt).email(), null, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public NotificacionResponse obtener(@PathVariable Long id) {
        return notificacionService.obtener(id);
    }

    @PostMapping("/{id}/reenviar")
    @PreAuthorize("hasRole('ADMIN')")
    public NotificacionResponse reenviar(@PathVariable Long id) {
        return notificacionService.reenviar(id);
    }
}
