package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.CajeroDTO;
import com.unla.ghsicilianotfi.entities.Cajero;
import com.unla.ghsicilianotfi.entities.Usuario;
import com.unla.ghsicilianotfi.repositories.ICajeroRepository;
import com.unla.ghsicilianotfi.repositories.IUsuarioRepository;
import com.unla.ghsicilianotfi.services.ICajeroService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("cajeroService")
public class CajeroService implements ICajeroService {

    private final ICajeroRepository cajeroRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public CajeroService(ICajeroRepository cajeroRepository,
                         IUsuarioRepository usuarioRepository,
                         ModelMapper modelMapper) {
        this.cajeroRepository = cajeroRepository;
        this.usuarioRepository = usuarioRepository;
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

        Cajero cajero;
        if (dto.getIdPersonal() != null) {
            // Actualización
            cajero = cajeroRepository.findById(dto.getIdPersonal())
                    .orElseThrow(() -> new IllegalArgumentException("No existe el cajero con ID: " + dto.getIdPersonal()));

            // Si cambió el DNI, validar duplicados
            if (!cajero.getDni().equalsIgnoreCase(dto.getDni().trim()) &&
                    cajeroRepository.existsByDni(dto.getDni().trim())) {
                throw new IllegalArgumentException("Ya existe otro empleado con el DNI: " + dto.getDni());
            }
        } else {
            // Inserción nueva
            if (cajeroRepository.existsByDni(dto.getDni().trim())) {
                throw new IllegalArgumentException("Ya existe un empleado con el DNI: " + dto.getDni());
            }
            cajero = new Cajero();
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

        if (dto.getIdUsuario() != null) {
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
            cajeroRepository.save(cajero);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean darDeAltaLogica(Long id) {
        return cajeroRepository.findById(id).map(cajero -> {
            cajero.setActivo(true);
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
        }
        return dto;
    }
}

