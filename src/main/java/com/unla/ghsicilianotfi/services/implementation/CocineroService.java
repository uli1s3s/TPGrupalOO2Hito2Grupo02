package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.CocineroDTO;
import com.unla.ghsicilianotfi.entities.CategoriaCocinero;
import com.unla.ghsicilianotfi.entities.Cocinero;
import com.unla.ghsicilianotfi.entities.Usuario;
import com.unla.ghsicilianotfi.repositories.ICategoriaCocineroRepository;
import com.unla.ghsicilianotfi.repositories.ICocineroRepository;
import com.unla.ghsicilianotfi.repositories.IUsuarioRepository;
import com.unla.ghsicilianotfi.services.ICocineroService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("cocineroService")
public class CocineroService implements ICocineroService {

    private final ICocineroRepository cocineroRepository;
    private final ICategoriaCocineroRepository categoriaCocineroRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public CocineroService(ICocineroRepository cocineroRepository,
                           ICategoriaCocineroRepository categoriaCocineroRepository,
                           IUsuarioRepository usuarioRepository,
                           ModelMapper modelMapper) {
        this.cocineroRepository = cocineroRepository;
        this.categoriaCocineroRepository = categoriaCocineroRepository;
        this.usuarioRepository = usuarioRepository;
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
    @Transactional
    public CocineroDTO insertOrUpdate(CocineroDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del cocinero no pueden ser nulos");
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
                    cocineroRepository.existsByDni(dto.getDni().trim())) {
                throw new IllegalArgumentException("Ya existe otro empleado con el DNI: " + dto.getDni());
            }
        } else {
            // Inserción nueva
            if (cocineroRepository.existsByDni(dto.getDni().trim())) {
                throw new IllegalArgumentException("Ya existe un empleado con el DNI: " + dto.getDni());
            }
            cocinero = new Cocinero();
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

        if (dto.getIdUsuario() != null) {
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
            cocineroRepository.save(cocinero);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean darDeAltaLogica(Long id) {
        return cocineroRepository.findById(id).map(cocinero -> {
            cocinero.setActivo(true);
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
        }
        return dto;
    }
}

