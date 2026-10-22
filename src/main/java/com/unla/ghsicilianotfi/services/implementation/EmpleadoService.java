package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.AltaEmpleadoDTO;
import com.unla.ghsicilianotfi.entities.*;
import com.unla.ghsicilianotfi.repositories.*;
import com.unla.ghsicilianotfi.services.IEmailService;
import com.unla.ghsicilianotfi.services.IEmpleadoService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("empleadoService")
public class EmpleadoService implements IEmpleadoService {

    private final ICocineroRepository cocineroRepository;
    private final ICajeroRepository cajeroRepository;
    private final ICategoriaCocineroRepository categoriaCocineroRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IRolRepository rolRepository;
    private final IEmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(ICocineroRepository cocineroRepository,
                           ICajeroRepository cajeroRepository,
                           ICategoriaCocineroRepository categoriaCocineroRepository,
                           IUsuarioRepository usuarioRepository,
                           IRolRepository rolRepository,
                           IEmailService emailService,
                           PasswordEncoder passwordEncoder) {
        this.cocineroRepository = cocineroRepository;
        this.cajeroRepository = cajeroRepository;
        this.categoriaCocineroRepository = categoriaCocineroRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AltaEmpleadoDTO> traerTodosActivos() {
        List<AltaEmpleadoDTO> lista = new ArrayList<>();

        for (Cocinero c : cocineroRepository.findByActivoTrue()) {
            lista.add(mapearCocineroADTO(c));
        }

        for (Cajero ca : cajeroRepository.findByActivoTrue()) {
            lista.add(mapearCajeroADTO(ca));
        }

        return lista;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AltaEmpleadoDTO> filtrarActivos(String busqueda, String tipo) {
        return traerTodosActivos().stream()
                .filter(emp -> {
                    boolean coincideBusqueda = true;
                    if (busqueda != null && !busqueda.isBlank()) {
                        String b = busqueda.trim().toLowerCase();
                        coincideBusqueda = emp.getNombre().toLowerCase().contains(b)
                                || emp.getApellido().toLowerCase().contains(b)
                                || emp.getDni().toLowerCase().contains(b);
                    }

                    boolean coincideTipo = true;
                    if (tipo != null && !tipo.isBlank() && !tipo.equalsIgnoreCase("TODOS")) {
                        coincideTipo = emp.getTipoEmpleado().equalsIgnoreCase(tipo.trim());
                    }

                    return coincideBusqueda && coincideTipo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AltaEmpleadoDTO darDeAltaEmpleado(AltaEmpleadoDTO dto) {
        validarDatosEmpleado(dto);

        // 1. Generar contraseña aleatoria de 8 caracteres
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);

        // 2. Obtener o crear rol EMPLEADO
        Rol rolEmpleado = rolRepository.findByNombreIgnoreCase("EMPLEADO")
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setNombre("EMPLEADO");
                    return rolRepository.save(r);
                });

        // 3. Crear el Usuario para el empleado
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getEmail().trim());
        usuario.setEmail(dto.getEmail().trim());
        usuario.setContrasena(passwordEncoder.encode(rawPassword));
        usuario.setActivo(true);
        usuario.setRol(rolEmpleado);
        usuario = usuarioRepository.save(usuario);

        AltaEmpleadoDTO resultado;

        // 4. Guardar según tipo (Cocinero o Cajero)
        if ("COCINERO".equalsIgnoreCase(dto.getTipoEmpleado())) {
            if (dto.getIdCategoria() == null) {
                throw new IllegalArgumentException("Debe seleccionar una categoría para el cocinero");
            }

            CategoriaCocinero categoria = categoriaCocineroRepository.findById(dto.getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("La categoría seleccionada no existe"));

            Cocinero cocinero = new Cocinero();
            cocinero.setNombre(dto.getNombre().trim());
            cocinero.setApellido(dto.getApellido().trim());
            cocinero.setDni(dto.getDni().trim());
            cocinero.setFechaNacimiento(dto.getFechaNacimiento());
            cocinero.setFechaIngreso(dto.getFechaIngreso());
            cocinero.setSueldoBase(dto.getSueldoBase());
            cocinero.setActivo(true);
            cocinero.setCredenciales(usuario);
            cocinero.setCategoria(categoria);
            cocinero.setEspecialidad(dto.getEspecialidad());

            Cocinero guardado = cocineroRepository.save(cocinero);
            resultado = mapearCocineroADTO(guardado);
        } else if ("CAJERO".equalsIgnoreCase(dto.getTipoEmpleado())) {
            Cajero cajero = new Cajero();
            cajero.setNombre(dto.getNombre().trim());
            cajero.setApellido(dto.getApellido().trim());
            cajero.setDni(dto.getDni().trim());
            cajero.setFechaNacimiento(dto.getFechaNacimiento());
            cajero.setFechaIngreso(dto.getFechaIngreso());
            cajero.setSueldoBase(dto.getSueldoBase());
            cajero.setActivo(true);
            cajero.setCredenciales(usuario);
            cajero.setTurno((dto.getTurno() != null && !dto.getTurno().isBlank()) ? dto.getTurno().trim() : "MAÑANA");

            Cajero guardado = cajeroRepository.save(cajero);
            resultado = mapearCajeroADTO(guardado);
        } else {
            throw new IllegalArgumentException("Tipo de empleado no soportado: " + dto.getTipoEmpleado());
        }

        // 5. Enviar el correo con usuario y contraseña generada
        emailService.enviarCredenciales(dto.getEmail().trim(), dto.getEmail().trim(), rawPassword);

        return resultado;
    }

    @Override
    @Transactional
    public AltaEmpleadoDTO actualizarEmpleado(AltaEmpleadoDTO dto) {
        if (dto == null || dto.getIdPersonal() == null) {
            throw new IllegalArgumentException("El ID del empleado es requerido para actualizar");
        }

        Optional<Cocinero> cocineroOpt = cocineroRepository.findById(dto.getIdPersonal());
        if (cocineroOpt.isPresent()) {
            Cocinero cocinero = cocineroOpt.get();

            if (!cocinero.getDni().equalsIgnoreCase(dto.getDni().trim())) {
                if (cocineroRepository.existsByDni(dto.getDni().trim()) || cajeroRepository.existsByDni(dto.getDni().trim())) {
                    throw new IllegalArgumentException("Ya existe otro empleado registrado con el DNI: " + dto.getDni());
                }
                cocinero.setDni(dto.getDni().trim());
            }

            cocinero.setNombre(dto.getNombre().trim());
            cocinero.setApellido(dto.getApellido().trim());
            cocinero.setFechaNacimiento(dto.getFechaNacimiento());
            cocinero.setFechaIngreso(dto.getFechaIngreso());
            cocinero.setSueldoBase(dto.getSueldoBase());
            cocinero.setEspecialidad(dto.getEspecialidad());

            if (dto.getIdCategoria() != null) {
                CategoriaCocinero cat = categoriaCocineroRepository.findById(dto.getIdCategoria())
                        .orElseThrow(() -> new IllegalArgumentException("La categoría seleccionada no existe"));
                cocinero.setCategoria(cat);
            }

            return mapearCocineroADTO(cocineroRepository.save(cocinero));
        }

        Optional<Cajero> cajeroOpt = cajeroRepository.findById(dto.getIdPersonal());
        if (cajeroOpt.isPresent()) {
            Cajero cajero = cajeroOpt.get();

            if (!cajero.getDni().equalsIgnoreCase(dto.getDni().trim())) {
                if (cocineroRepository.existsByDni(dto.getDni().trim()) || cajeroRepository.existsByDni(dto.getDni().trim())) {
                    throw new IllegalArgumentException("Ya existe otro empleado registrado con el DNI: " + dto.getDni());
                }
                cajero.setDni(dto.getDni().trim());
            }

            cajero.setNombre(dto.getNombre().trim());
            cajero.setApellido(dto.getApellido().trim());
            cajero.setFechaNacimiento(dto.getFechaNacimiento());
            cajero.setFechaIngreso(dto.getFechaIngreso());
            cajero.setSueldoBase(dto.getSueldoBase());

            if (dto.getTurno() != null && !dto.getTurno().isBlank()) {
                cajero.setTurno(dto.getTurno().trim());
            }

            return mapearCajeroADTO(cajeroRepository.save(cajero));
        }

        throw new IllegalArgumentException("No se encontró el empleado con ID: " + dto.getIdPersonal());
    }

    @Override
    @Transactional
    public boolean darDeBajaLogica(Long idPersonal) {
        Optional<Cocinero> cocineroOpt = cocineroRepository.findById(idPersonal);
        if (cocineroOpt.isPresent()) {
            Cocinero c = cocineroOpt.get();
            c.setActivo(false);
            if (c.getCredenciales() != null) {
                c.getCredenciales().setActivo(false);
            }
            cocineroRepository.save(c);
            return true;
        }

        Optional<Cajero> cajeroOpt = cajeroRepository.findById(idPersonal);
        if (cajeroOpt.isPresent()) {
            Cajero ca = cajeroOpt.get();
            ca.setActivo(false);
            if (ca.getCredenciales() != null) {
                ca.getCredenciales().setActivo(false);
            }
            cajeroRepository.save(ca);
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public boolean darDeAltaLogica(Long idPersonal) {
        Optional<Cocinero> cocineroOpt = cocineroRepository.findById(idPersonal);
        if (cocineroOpt.isPresent()) {
            Cocinero c = cocineroOpt.get();
            c.setActivo(true);
            if (c.getCredenciales() != null) {
                c.getCredenciales().setActivo(true);
            }
            cocineroRepository.save(c);
            return true;
        }

        Optional<Cajero> cajeroOpt = cajeroRepository.findById(idPersonal);
        if (cajeroOpt.isPresent()) {
            Cajero ca = cajeroOpt.get();
            ca.setActivo(true);
            if (ca.getCredenciales() != null) {
                ca.getCredenciales().setActivo(true);
            }
            cajeroRepository.save(ca);
            return true;
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AltaEmpleadoDTO> buscarPorId(Long idPersonal) {
        Optional<Cocinero> c = cocineroRepository.findById(idPersonal);
        if (c.isPresent()) {
            return Optional.of(mapearCocineroADTO(c.get()));
        }
        Optional<Cajero> ca = cajeroRepository.findById(idPersonal);
        return ca.map(this::mapearCajeroADTO);
    }

    private void validarDatosEmpleado(AltaEmpleadoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del empleado no pueden ser nulos");
        }

        // Validación de mayoría de edad (Página 1 del PDF)
        if (dto.getFechaNacimiento() != null) {
            int edad = Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears();
            if (edad < 18) {
                throw new IllegalArgumentException("El personal del predio debe ser mayor de edad (mínimo 18 años).");
            }
        }

        // Validación de DNI único
        String dni = dto.getDni().trim();
        if (cocineroRepository.existsByDni(dni) || cajeroRepository.existsByDni(dni)) {
            throw new IllegalArgumentException("Ya existe un empleado registrado con el DNI: " + dni);
        }

        // Validación de email / usuario único
        String email = dto.getEmail().trim();
        if (usuarioRepository.findByNombreConRol(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + email);
        }
    }

    private AltaEmpleadoDTO mapearCocineroADTO(Cocinero c) {
        AltaEmpleadoDTO dto = new AltaEmpleadoDTO();
        dto.setIdPersonal(c.getIdPersonal());
        dto.setNombre(c.getNombre());
        dto.setApellido(c.getApellido());
        dto.setDni(c.getDni());
        dto.setFechaNacimiento(c.getFechaNacimiento());
        dto.setFechaIngreso(c.getFechaIngreso());
        dto.setSueldoBase(c.getSueldoBase());
        dto.setActivo(c.isActivo());
        dto.setTipoEmpleado("COCINERO");
        dto.setEspecialidad(c.getEspecialidad());

        if (c.getCategoria() != null) {
            dto.setIdCategoria(c.getCategoria().getIdCategoria());
            dto.setNombreCategoria(c.getCategoria().getNombre());
        }

        if (c.getCredenciales() != null) {
            dto.setEmail(c.getCredenciales().getEmail());
        }

        return dto;
    }

    private AltaEmpleadoDTO mapearCajeroADTO(Cajero ca) {
        AltaEmpleadoDTO dto = new AltaEmpleadoDTO();
        dto.setIdPersonal(ca.getIdPersonal());
        dto.setNombre(ca.getNombre());
        dto.setApellido(ca.getApellido());
        dto.setDni(ca.getDni());
        dto.setFechaNacimiento(ca.getFechaNacimiento());
        dto.setFechaIngreso(ca.getFechaIngreso());
        dto.setSueldoBase(ca.getSueldoBase());
        dto.setActivo(ca.isActivo());
        dto.setTipoEmpleado("CAJERO");
        dto.setTurno(ca.getTurno());

        if (ca.getCredenciales() != null) {
            dto.setEmail(ca.getCredenciales().getEmail());
        }

        return dto;
    }
}

