package com.concesionaria.cliente.cliente.service;

import com.concesionaria.cliente.cliente.Cliente;
import com.concesionaria.cliente.cliente.dto.ClienteRequest;
import com.concesionaria.cliente.cliente.dto.ClienteResponse;
import com.concesionaria.cliente.cliente.dto.PerfilRequest;
import com.concesionaria.cliente.cliente.enums.OrigenCliente;
import com.concesionaria.cliente.cliente.enums.TipoDocumento;
import com.concesionaria.cliente.cliente.repository.ClienteRepository;
import com.concesionaria.cliente.evento.ClienteEvento;
import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.common.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.util.Optional;

/** RF-14 a RF-16 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ApplicationEventPublisher eventos;
    private final TransactionTemplate transactionTemplate;

    // ---------------------------------------------------------------- Empleados (RF-14, RF-16)

    /** RF-14: el vendedor carga un cliente que vino al local (sin cuenta en el portal). */
    @Transactional
    public ClienteResponse crear(ClienteRequest request, UsuarioActual empleado) {
        validarContacto(request);
        String documento = validarDocumento(request.tipoDocumento(), request.numeroDocumento(), null);
        String email = validarEmail(request.email(), null);

        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nombre(request.nombre().trim())
                .apellido(request.apellido().trim())
                .tipoDocumento(request.tipoDocumento())
                .numeroDocumento(documento)
                .email(email)
                .telefono(limpiar(request.telefono()))
                .direccion(request.direccion() != null ? request.direccion().aEntidad() : null)
                .fechaNacimiento(request.fechaNacimiento())
                .origen(OrigenCliente.LOCAL)
                .registradoPorId(empleado.id())
                .build());

        eventos.publishEvent(ClienteEvento.creado(cliente));
        return ClienteResponse.desde(cliente);
    }

    /**
     * RF-16: un empleado corrige los datos. Si el cliente tiene cuenta en el portal,
     * el email no se cambia acá (es su usuario de login).
     */
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscar(id);
        validarContacto(request);
        String documento = validarDocumento(request.tipoDocumento(), request.numeroDocumento(), id);

        if (cliente.getUsuarioId() == null) {
            cliente.setEmail(validarEmail(request.email(), id));
        } else if (request.email() != null && !request.email().equalsIgnoreCase(cliente.getEmail())) {
            throw new SolicitudInvalidaException("El cliente tiene cuenta en el portal: el email lo cambia él desde su cuenta");
        }
        cliente.setNombre(request.nombre().trim());
        cliente.setApellido(request.apellido().trim());
        cliente.setTipoDocumento(request.tipoDocumento());
        cliente.setNumeroDocumento(documento);
        cliente.setTelefono(limpiar(request.telefono()));
        cliente.setDireccion(request.direccion() != null ? request.direccion().aEntidad() : null);
        cliente.setFechaNacimiento(request.fechaNacimiento());

        eventos.publishEvent(ClienteEvento.actualizado(cliente));
        return ClienteResponse.desde(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return ClienteResponse.desde(buscar(id));
    }

    /** RF-16 */
    @Transactional(readOnly = true)
    public Page<ClienteResponse> buscar(String texto, OrigenCliente origen, Pageable pageable) {
        String filtro = StringUtils.hasText(texto) ? texto.trim() : null;
        return clienteRepository.buscar(origen, filtro, pageable).map(ClienteResponse::desde);
    }

    // ---------------------------------------------------------------- Cliente del portal (RF-15)

    /** RF-15: perfil del cliente logueado. Si todavía no existe, se crea con los datos del token. */
    public ClienteResponse perfilPropio(UsuarioActual usuario) {
        return ClienteResponse.desde(obtenerOCrearPerfil(usuario));
    }

    /** RF-15: completar o editar documento, teléfono, dirección y fecha de nacimiento. */
    public ClienteResponse actualizarPerfil(UsuarioActual usuario, PerfilRequest request) {
        Long id = obtenerOCrearPerfil(usuario).getId();
        return transactionTemplate.execute(estado -> {
            Cliente cliente = buscar(id);
            if ((request.tipoDocumento() == null) != !StringUtils.hasText(request.numeroDocumento())) {
                throw new SolicitudInvalidaException("Indicá el tipo y el número de documento juntos");
            }
            if (request.tipoDocumento() != null) {
                cliente.setTipoDocumento(request.tipoDocumento());
                cliente.setNumeroDocumento(validarDocumento(request.tipoDocumento(), request.numeroDocumento(), id));
            }
            cliente.setTelefono(limpiar(request.telefono()));
            cliente.setDireccion(request.direccion() != null ? request.direccion().aEntidad() : null);
            cliente.setFechaNacimiento(request.fechaNacimiento());

            eventos.publishEvent(ClienteEvento.actualizado(cliente));
            return ClienteResponse.desde(cliente);
        });
    }

    // ---------------------------------------------------------------- Evento de auth-service

    /**
     * Alguien se registró en el portal:
     * - si ya tiene perfil (evento repetido), no hace nada;
     * - si un vendedor ya lo había cargado con el mismo email, vincula ese cliente a su cuenta;
     * - si no, crea el perfil (origen PORTAL) con los datos que ya tenemos.
     */
    @Transactional
    public Cliente registrarDesdePortal(Long usuarioId, String email, String nombre, String apellido) {
        Optional<Cliente> existente = clienteRepository.findByUsuarioId(usuarioId);
        if (existente.isPresent()) {
            return existente.get();
        }
        Optional<Cliente> mismoEmail = clienteRepository.findByEmailIgnoreCase(email);
        if (mismoEmail.isPresent()) {
            Cliente cliente = mismoEmail.get();
            if (cliente.getUsuarioId() != null) {
                log.warn("El email {} ya pertenece al cliente {} con otra cuenta; no se vincula el usuario {}",
                        email, cliente.getId(), usuarioId);
                return cliente;
            }
            cliente.setUsuarioId(usuarioId);
            log.info("Cliente {} cargado en el local vinculado a la cuenta {}", cliente.getId(), usuarioId);
            eventos.publishEvent(ClienteEvento.actualizado(cliente));
            return cliente;
        }
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .usuarioId(usuarioId)
                .email(email.toLowerCase())
                .nombre(nombre)
                .apellido(apellido)
                .origen(OrigenCliente.PORTAL)
                .build());
        eventos.publishEvent(ClienteEvento.creado(cliente));
        return cliente;
    }

    // ---------------------------------------------------------------- Auxiliares

    private Cliente obtenerOCrearPerfil(UsuarioActual usuario) {
        Optional<Cliente> existente = clienteRepository.findByUsuarioId(usuario.id());
        if (existente.isPresent()) {
            return existente.get();
        }
        // El evento de registro todavía no llegó (o se perdió): se crea con los datos del token
        String[] partes = usuario.nombre() != null ? usuario.nombre().trim().split("\\s+", 2) : new String[]{"-"};
        String nombre = partes[0];
        String apellido = partes.length > 1 ? partes[1] : "-";
        try {
            return transactionTemplate.execute(estado ->
                    registrarDesdePortal(usuario.id(), usuario.email(), nombre, apellido));
        } catch (DataIntegrityViolationException e) {
            // Lo creó el evento al mismo tiempo: usamos ese
            return clienteRepository.findByUsuarioId(usuario.id()).orElseThrow(() -> e);
        }
    }

    private Cliente buscar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Cliente " + id + " no encontrado"));
    }

    private String validarDocumento(TipoDocumento tipo, String numero, Long idExcluido) {
        String normalizado = DocumentoValidator.normalizar(tipo, numero);
        boolean repetido = idExcluido == null
                ? clienteRepository.existsByTipoDocumentoAndNumeroDocumento(tipo, normalizado)
                : clienteRepository.existsByTipoDocumentoAndNumeroDocumentoAndIdNot(tipo, normalizado, idExcluido);
        if (repetido) {
            throw new ConflictoException("Ya existe un cliente con " + tipo + " " + normalizado);
        }
        return normalizado;
    }

    private String validarEmail(String email, Long idExcluido) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        String normalizado = email.trim().toLowerCase();
        boolean repetido = idExcluido == null
                ? clienteRepository.existsByEmailIgnoreCase(normalizado)
                : clienteRepository.existsByEmailIgnoreCaseAndIdNot(normalizado, idExcluido);
        if (repetido) {
            throw new ConflictoException("Ya existe un cliente con el email " + normalizado);
        }
        return normalizado;
    }

    private static void validarContacto(ClienteRequest request) {
        if (!StringUtils.hasText(request.email()) && !StringUtils.hasText(request.telefono())) {
            throw new SolicitudInvalidaException("Cargá al menos un email o un teléfono de contacto");
        }
    }

    private static String limpiar(String texto) {
        return StringUtils.hasText(texto) ? texto.trim() : null;
    }
}
