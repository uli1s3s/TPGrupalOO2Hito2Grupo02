package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.RegistroUnidadVentaDTO;
import com.unla.ghsicilianotfi.entities.FoodTruck;
import com.unla.ghsicilianotfi.entities.PuestoDesmontable;
import com.unla.ghsicilianotfi.entities.Rol;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.entities.Usuario;
import com.unla.ghsicilianotfi.repositories.IRolRepository;
import com.unla.ghsicilianotfi.repositories.IUnidadVentaRepository;
import com.unla.ghsicilianotfi.repositories.IUsuarioRepository;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service("unidadVentaService")
public class UnidadVentaService implements IUnidadVentaService {

    private static final String TIPO_FOOD_TRUCK = "FOOD_TRUCK";
    private static final String TIPO_PUESTO = "PUESTO_DESMONTABLE";
    private static final String ROL_RESPONSABLE = "RESPONSABLE";

    private final IUnidadVentaRepository unidadVentaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IRolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UnidadVentaService(IUnidadVentaRepository unidadVentaRepository,
                              IUsuarioRepository usuarioRepository,
                              IRolRepository rolRepository,
                              PasswordEncoder passwordEncoder) {
        this.unidadVentaRepository = unidadVentaRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UnidadVenta> listarUnidades() {
        return unidadVentaRepository.findAll();
    }
    
    @Override
    public List<UnidadVenta> listarPorResponsable(String nombreUsuario) {
        return unidadVentaRepository.findByResponsable_Nombre(nombreUsuario);
    }

    @Override
    @Transactional
    public UnidadVenta registrarUnidad(RegistroUnidadVentaDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException("Los datos de la unidad son obligatorios");
        }

        // 1) El codigo unico no se puede repetir
        String codigo = datos.getCodigoUnico().trim().toUpperCase(Locale.ROOT);
        if (unidadVentaRepository.existsByCodigoUnico(codigo)) {
            throw new IllegalArgumentException(
                    "Ya existe una unidad de venta con el código: " + codigo);
        }

        // 2) Crear segun el tipo elegido
        UnidadVenta unidad = crearSegunTipo(datos);

        // 3. Datos comunes
        unidad.setNombreComercial(datos.getNombreComercial().trim());
        unidad.setSuperficieM2(datos.getSuperficieM2());
        unidad.setCodigoUnico(codigo);

        // 4. Crear y asociar el usuario responsable
        unidad.setResponsable(crearResponsable(datos));

        // 5. Guardar
        return unidadVentaRepository.save(unidad);
    }

    // =====================================================
    // Crea un FoodTruck o un PuestoDesmontable con sus datos propios
    // =====================================================
    private UnidadVenta crearSegunTipo(RegistroUnidadVentaDTO datos) {

        if (TIPO_FOOD_TRUCK.equals(datos.getTipoUnidad())) {

            if (datos.getPatente() == null || datos.getPatente().isBlank()) {
                throw new IllegalArgumentException("La patente es obligatoria para un food truck");
            }
            if (datos.getElectricidad() == null) {
                throw new IllegalArgumentException("Indicá si el food truck requiere electricidad");
            }

            FoodTruck foodTruck = new FoodTruck();
            foodTruck.setPatente(datos.getPatente().trim().toUpperCase(Locale.ROOT));
            foodTruck.setElectricidad(datos.getElectricidad());
            return foodTruck;
        }

        if (TIPO_PUESTO.equals(datos.getTipoUnidad())) {

            if (datos.getCantidadCarpas() == null || datos.getCantidadCarpas() < 1) {
                throw new IllegalArgumentException("La cantidad de carpas debe ser al menos 1");
            }
            if (datos.getTiempoMontajeMinutos() == null || datos.getTiempoMontajeMinutos() < 1) {
                throw new IllegalArgumentException("El tiempo de montaje debe ser al menos 1 minuto");
            }

            PuestoDesmontable puesto = new PuestoDesmontable();
            puesto.setCantidadCarpas(datos.getCantidadCarpas());
            puesto.setTiempoMontajeMinutos(datos.getTiempoMontajeMinutos());
            return puesto;
        }

        throw new IllegalArgumentException("El tipo de unidad seleccionado no es válido");
    }

    // =====================================================
    // Crea el usuario responsable (email = nombre de usuario)
    // =====================================================
    private Usuario crearResponsable(RegistroUnidadVentaDTO datos) {

        String email = datos.getEmailResponsable().trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.findByNombreConRol(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + email);
        }

        Rol rolResponsable = rolRepository.findByNombreIgnoreCase(ROL_RESPONSABLE)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el rol RESPONSABLE. Ejecutá el script de datos de prueba."));
        
        Usuario responsable = new Usuario();
        responsable.setNombre(email);
        responsable.setEmail(email);
        responsable.setContrasena(passwordEncoder.encode(datos.getContrasenaResponsable()));
        responsable.setActivo(true);
        responsable.setRol(rolResponsable);

        return usuarioRepository.save(responsable);
    }
}