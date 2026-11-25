package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.CocineroDTO;
import com.unla.ghsicilianotfi.entities.CategoriaCocinero;
import com.unla.ghsicilianotfi.entities.Cocinero;
import com.unla.ghsicilianotfi.entities.Rol;
import com.unla.ghsicilianotfi.entities.Usuario;
import com.unla.ghsicilianotfi.repositories.ICajeroRepository;
import com.unla.ghsicilianotfi.repositories.ICategoriaCocineroRepository;
import com.unla.ghsicilianotfi.repositories.ICocineroRepository;
import com.unla.ghsicilianotfi.repositories.IRolRepository;
import com.unla.ghsicilianotfi.repositories.IUsuarioRepository;
import com.unla.ghsicilianotfi.services.ICocineroService;
import com.unla.ghsicilianotfi.services.IEmailService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("cocineroService")
public class CocineroService implements ICocineroService {

    private final ICocineroRepository cocineroRepository;
    private final ICajeroRepository cajeroRepository;
    private final ICategoriaCocineroRepository categoriaCocineroRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IRolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final ModelMapper modelMapper;

    public CocineroService(ICocineroRepository cocineroRepository,
                           ICajeroRepository cajeroRepository,
                           ICategoriaCocineroRepository categoriaCocineroRepository,
                           IUsuarioRepository usuarioRepository,
                           IRolRepository rolRepository,
                           PasswordEncoder passwordEncoder,
                           IEmailService emailService,
                           ModelMapper modelMapper) {
        this.cocineroRepository = cocineroRepository;
        this.cajeroRepository = cajeroRepository;
        this.categoriaCocineroRepository = categoriaCocineroRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CocineroDTO> getAll() {
        return cocineroRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CocineroDTO> getAllActivos() {
        return cocineroRepository.findByActivoTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CocineroDTO> findById(Long id) {
        return cocineroRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CocineroDTO> findByDni(String dni) {
        return cocineroRepository.findByDni(dni).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CocineroDTO> findByCategoria(Long idCategoria) {
        return cocineroRepository.findByCategoria_IdCategoria(idCategoria)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CocineroDTO> findConFiltros(String texto, Long idCategoria, Boolean activo, Double sueldoMinimo) {
        String textoLimpio = (texto != null && !texto.isBlank()) ? texto.trim() : null;
        return cocineroRepository.findConFiltros(textoLimpio, idCategoria, activo, sueldoMinimo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CocineroDTO insertOrUpdate(CocineroDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del cocinero no pueden ser nulos");
        }

        // Validación de mayoría de edad (mínimo 18 años)
        if (dto.getFechaNacimiento() != null) {
            int edad = Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears();
            if (edad < 18) {
                throw new IllegalArgumentException("El personal del predio debe ser mayor de edad (mínimo 18 años).");
            }
        }

        if (dto.getIdCategoria() == null) {
            throw new IllegalArgumentException("La categoría de cocinero es obligatoria");
        }

        CategoriaCocinero categoria = categoriaCocineroRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("No existe la categoría con ID: " + dto.getIdCategoria()));

        Cocinero cocinero;
        if (dto.getIdPersonal() != null) {
            // Actualización
            cocinero = cocineroRepository.findById(dto.getIdPersonal())
                    .orElseThrow(() -> new IllegalArgumentException("No existe el cocinero con ID: " + dto.getIdPersonal()));

            // Si cambió el DNI, verificar que no esté duplicado
            if (!cocinero.getDni().equalsIgnoreCase(dto.getDni().trim()) &&
                    (cocineroRepository.existsByDni(dto.getDni().trim()) || cajeroRepository.existsByDni(dto.getDni().trim()))) {
                throw new IllegalArgumentException("Ya existe otro empleado con el DNI: " + dto.getDni());
            }
        } else {
            // Inserción nueva
            String dni = dto.getDni().trim();
            if (cocineroRepository.existsByDni(dni) || cajeroRepository.existsByDni(dni)) {
                throw new IllegalArgumentException("Ya existe un empleado con el DNI: " + dni);
            }

            if (dto.getEmail() == null || dto.getEmail().isBlank()) {
                throw new IllegalArgumentException("El email es obligatorio para dar de alta un cocinero");
            }

            String email = dto.getEmail().trim();
            if (usuarioRepository.findByEmailIgnoreCase(email).isPresent() || usuarioRepository.findByNombreConRol(email).isPresent()) {
                throw new IllegalArgumentException("Ya existe un usuario registrado con el email: " + email);
            }

            // Generar credenciales de usuario con rol EMPLEADO
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

            cocinero = new Cocinero();
            cocinero.setCredenciales(usuario);

            // Enviar credenciales por correo electrónico
            emailService.enviarCredenciales(email, email, rawPassword);
        }

        // Mapear campos
        cocinero.setNombre(dto.getNombre().trim());
        cocinero.setApellido(dto.getApellido().trim());
        cocinero.setDni(dto.getDni().trim());
        cocinero.setFechaNacimiento(dto.getFechaNacimiento());
        cocinero.setFechaIngreso(dto.getFechaIngreso());
        cocinero.setSueldoBase(dto.getSueldoBase());
        cocinero.setActivo(dto.isActivo());
        cocinero.setEspecialidad(dto.getEspecialidad());
        cocinero.setCategoria(categoria);

        if (dto.getIdUsuario() != null && cocinero.getCredenciales() == null) {
            Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("No existe el usuario con ID: " + dto.getIdUsuario()));
            cocinero.setCredenciales(usuario);
        }

        Cocinero guardado = cocineroRepository.save(cocinero);
        return toDTO(guardado);
    }

    @Override
    @Transactional
    public boolean darDeBajaLogica(Long id) {
        return cocineroRepository.findById(id).map(cocinero -> {
            cocinero.setActivo(false);
            if (cocinero.getCredenciales() != null) {
                cocinero.getCredenciales().setActivo(false);
            }
            cocineroRepository.save(cocinero);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean darDeAltaLogica(Long id) {
        return cocineroRepository.findById(id).map(cocinero -> {
            cocinero.setActivo(true);
            if (cocinero.getCredenciales() != null) {
                cocinero.getCredenciales().setActivo(true);
            }
            cocineroRepository.save(cocinero);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean remove(Long id) {
        if (cocineroRepository.existsById(id)) {
            cocineroRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private CocineroDTO toDTO(Cocinero cocinero) {
        CocineroDTO dto = modelMapper.map(cocinero, CocineroDTO.class);
        if (cocinero.getCategoria() != null) {
            dto.setIdCategoria(cocinero.getCategoria().getIdCategoria());
            dto.setNombreCategoria(cocinero.getCategoria().getNombre());
        }
        if (cocinero.getCredenciales() != null) {
            dto.setIdUsuario(cocinero.getCredenciales().getIdUsuario());
            dto.setEmail(cocinero.getCredenciales().getEmail());
        }
        return dto;
    }
}
