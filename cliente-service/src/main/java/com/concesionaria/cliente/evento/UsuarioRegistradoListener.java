package com.concesionaria.cliente.evento;

import com.concesionaria.cliente.cliente.service.ClienteService;
import com.concesionaria.cliente.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RF-02 + RF-15: cuando alguien se registra en el portal (rol CLIENTE) se crea su perfil
 * de cliente, o se vincula al que ya había cargado un vendedor con el mismo email.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsuarioRegistradoListener {

    private final ClienteService clienteService;

    @RabbitListener(queues = RabbitConfig.COLA_USUARIO_REGISTRADO)
    public void alRegistrarUsuario(UsuarioRegistradoEvento evento) {
        if (!"CLIENTE".equals(evento.rol())) {
            return;
        }
        log.info("Usuario {} registrado en el portal ({})", evento.usuarioId(), evento.eventoId());
        clienteService.registrarDesdePortal(evento.usuarioId(), evento.email(), evento.nombre(), evento.apellido());
    }
}
