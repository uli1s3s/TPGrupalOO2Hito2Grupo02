package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.CajeroDTO;
import com.unla.ghsicilianotfi.entities.Cajero;
import com.unla.ghsicilianotfi.entities.Usuario;
import com.unla.ghsicilianotfi.entities.Rol;
import com.unla.ghsicilianotfi.repositories.ICajeroRepository;
import com.unla.ghsicilianotfi.repositories.IUsuarioRepository;
import com.unla.ghsicilianotfi.repositories.IRolRepository;
import com.unla.ghsicilianotfi.repositories.ICocineroRepository;
import com.unla.ghsicilianotfi.services.ICajeroService;
import com.unla.ghsicilianotfi.services.IEmailService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Service("cajeroService")
public class CajeroService implements ICajeroService {

    private final ICajeroRepository cajeroRepository;
    private final ICocineroRepository cocineroRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IRolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final ModelMapper modelMapper;

    public CajeroService(ICajeroRepository cajeroRepository,
                         ICocineroRepository cocineroRepository,
                         IUsuarioRepository usuarioRepository,
                         IRolRepository rolRepository,
                         PasswordEncoder passwordEncoder,
                         IEmailService emailService,
                         ModelMapper modelMapper) {
        this.cajeroRepository = cajeroRepository;
        this.cocineroRepository = cocineroRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajeroDTO> getAll() {
        return cajeroRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajeroDTO> getAllActivos() {
        return cajeroRepository.findByActivoTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CajeroDTO> findById(Long id) {
        return cajeroRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CajeroDTO> findByDni(String dni) {
        return cajeroRepository.findByDni(dni).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajeroDTO> findByTurno(String turno) {
        return cajeroRepository.findByTurnoIgnoreCase(turno)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CajeroDTO insertOrUpdate(CajeroDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del cajero no pueden ser nulos");
        }

        if (dto.getFechaNacimiento() != null) {
            int edad = Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears();
            if (edad < 18) {
                throw new IllegalArgumentException("El personal del predio debe ser mayor de edad (mínimo 18 años).");
            }
        }

        Cajero cajero;
        if (dto.getIdPersonal() != null) {
            // Actualización
            cajero = cajeroRepository.findById(dto.getIdPersonal())
                    .orElseThrow(() -> new IllegalArgumentException("No existe el cajero con ID: " + dto.getIdPersonal()));

            // Si cambió el DNI, validar duplicados
            if (!cajero.getDni().equalsIgnoreCase(dto.getDni().trim()) &&
                    (cajeroRepository.existsByDni(dto.getDni().trim()) || cocineroRepository.existsByDni(dto.getDni().trim()))) {
                throw new IllegalArgumentException("Ya existe otro empleado con el DNI: " + dto.getDni());
            }
        } else {
            String dni = dto.getDni().trim();
            if (cajeroRepository.existsByDni(dni) || cocineroRepository.existsByDni(dni)) {
                throw new IllegalArgumentException("Ya existe un empleado con el DNI: " + dni);
            }
            if (dto.getEmail() == null || dto.getEmail().isBlank()) {
                throw new IllegalArgumentException("El email es obligatorio para dar de alta un cajero");
            }
            String email = dto.getEmail().trim();
            if (usuarioRepository.findByEmailIgnoreCase(email).isPresent() || usuarioRepository.findByNombreConRol(email).isPresent()) {
                throw new IllegalArgumentException("Ya existe un usuario registrado con el email: " + email);
            }
            // Generar credenciales con rol EMPLEADO
            String rawPassword = UUID.randomUUID().toString().substring(0, 8);
            Rol rolEmpleado = rolRepository.findByNombreIgnoreCase("EMPLEADO")
                    .orElseGet(() -> {
                        Rol r = new Rol();
                        r.setNombre("EMPLEADO");
                        return rolRepository.save(r);
                    });
            Usuario usuario = new Usuario();
            usuario.setNombre(email);
            usuario.setEmail(email);
            usuario.setContrasena(passwordEncoder.encode(rawPassword));
            usuario.setActivo(true);
            usuario.setRol(rolEmpleado);
            usuario = usuarioRepository.save(usuario);
            cajero = new Cajero();
            cajero.setCredenciales(usuario);
            // Enviar credenciales por correo electrónico
            emailService.enviarCredenciales(email, email, rawPassword);
        }

        // Mapear campos
        cajero.setNombre(dto.getNombre().trim());
        cajero.setApellido(dto.getApellido().trim());
        cajero.setDni(dto.getDni().trim());
        cajero.setFechaNacimiento(dto.getFechaNacimiento());
        cajero.setFechaIngreso(dto.getFechaIngreso());
        cajero.setSueldoBase(dto.getSueldoBase());
        cajero.setActivo(dto.isActivo());
        cajero.setTurno(dto.getTurno().trim());

        if (dto.getIdUsuario() != null && cajero.getCredenciales() == null) {
            Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("No existe el usuario con ID: " + dto.getIdUsuario()));
            cajero.setCredenciales(usuario);
        }

        Cajero guardado = cajeroRepository.save(cajero);
        return toDTO(guardado);
    }

    @Override
    @Transactional
    public boolean darDeBajaLogica(Long id) {
        return cajeroRepository.findById(id).map(cajero -> {
            cajero.setActivo(false);
            if (cajero.getCredenciales() != null) {
                cajero.getCredenciales().setActivo(false);
            }
            cajeroRepository.save(cajero);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean darDeAltaLogica(Long id) {
        return cajeroRepository.findById(id).map(cajero -> {
            cajero.setActivo(true);
            if (cajero.getCredenciales() != null) {
                cajero.getCredenciales().setActivo(true);
            }
            cajeroRepository.save(cajero);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean remove(Long id) {
        if (cajeroRepository.existsById(id)) {
            cajeroRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private CajeroDTO toDTO(Cajero cajero) {
        CajeroDTO dto = modelMapper.map(cajero, CajeroDTO.class);
        if (cajero.getCredenciales() != null) {
            dto.setIdUsuario(cajero.getCredenciales().getIdUsuario());
            dto.setEmail(cajero.getCredenciales().getEmail());
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajeroDTO> findConFiltros(String texto, String turno, Boolean activo, Double sueldoMinimo) {
        String textoLimpio = (texto != null && !texto.isBlank()) ? texto.trim() : null;
        String turnoLimpio = (turno != null && !turno.isBlank()) ? turno.trim() : null;
        return cajeroRepository.buscarCajeros(textoLimpio, turnoLimpio, activo, sueldoMinimo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
        
}

