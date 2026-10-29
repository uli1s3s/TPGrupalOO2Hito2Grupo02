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

import com.unla.ghsicilianotfi.configuration.ParametrosCanon;
import com.unla.ghsicilianotfi.dtos.ReporteMayoresCanonDTO;

import java.util.ArrayList;
import java.util.Comparator;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service("unidadVentaService")
public class UnidadVentaService implements IUnidadVentaService {

    private static final String TIPO_FOOD_TRUCK = "FOOD_TRUCK";
    private static final String TIPO_PUESTO = "PUESTO_DESMONTABLE";
    private static final String ROL_RESPONSABLE = "RESPONSABLE";
    // Lógica de validación propia del código: 2 letras del tipo + 8 dígitos (10 caracteres)
    private static final String FORMATO_CODIGO = "^(FT|PD)\\d{8}$";
    private final ParametrosCanon parametrosCanon;

    private final IUnidadVentaRepository unidadVentaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IRolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UnidadVentaService(IUnidadVentaRepository unidadVentaRepository,
            IUsuarioRepository usuarioRepository,
            IRolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            ParametrosCanon parametrosCanon) {
	this.unidadVentaRepository = unidadVentaRepository;
	this.usuarioRepository = usuarioRepository;
	this.rolRepository = rolRepository;
	this.passwordEncoder = passwordEncoder;
	this.parametrosCanon = parametrosCanon;
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

        // 1. Crear segun el tipo elegido
        UnidadVenta unidad = crearSegunTipo(datos);

        // 2. Datos comunes
        unidad.setNombreComercial(datos.getNombreComercial().trim());
        unidad.setSuperficieM2(datos.getSuperficieM2());

        // 3. Codigo unico generado por el sistema (FT00000001, PD00000001, ...)
        unidad.setCodigoUnico(generarCodigoUnico(unidad));

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
    // Genera el siguiente codigo segun el tipo: prefijo + numero de 8 dígitos
    // =====================================================
    private String generarCodigoUnico(UnidadVenta unidad) {

        String prefijo = unidad.getPrefijoCodigo();
        int siguienteNumero = 1;

        // Buscar el ultimo codigo cargado con ese prefijo y sumarle 1
        Optional<UnidadVenta> ultima = unidadVentaRepository
                .findTopByCodigoUnicoStartingWithOrderByCodigoUnicoDesc(prefijo);

        if (ultima.isPresent() && esCodigoValido(ultima.get().getCodigoUnico())) {
            String numero = ultima.get().getCodigoUnico().substring(prefijo.length());
            siguienteNumero = Integer.parseInt(numero) + 1;
        }

        String codigo = prefijo + String.format("%08d", siguienteNumero);

        // Validacion propia antes de guardar
        if (!esCodigoValido(codigo)) {
            throw new IllegalArgumentException("El código generado no tiene un formato válido: " + codigo);
        }
        if (unidadVentaRepository.existsByCodigoUnico(codigo)) {
            throw new IllegalArgumentException("No se pudo generar un código único. Intentá nuevamente.");
        }

        return codigo;
    }

    // El código debe tener 10 caracteres: FT o PD seguido de 8 dígitos
    private boolean esCodigoValido(String codigo) {
        return codigo != null && codigo.matches(FORMATO_CODIGO);
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
    
    @Override
    @Transactional(readOnly = true)
    public List<ReporteMayoresCanonDTO> unidadesConMayorCanon(int cantidad) {

        if (cantidad < 1) {
            throw new IllegalArgumentException("La cantidad de unidades debe ser al menos 1");
        }

        // 1. Calcular el canon de cada unidad (cada tipo usa su propia formula)
        List<ReporteMayoresCanonDTO> reporte = new ArrayList<>();

        for (UnidadVenta unidad : unidadVentaRepository.findAll()) {
            reporte.add(new ReporteMayoresCanonDTO(
                    unidad.getNombreComercial(),
                    unidad.getCodigoUnico(),
                    unidad.getTipo(),
                    unidad.calcularCanon(parametrosCanon)
            ));
        }

        // 2. Ordenar de mayor a menor canon
        reporte.sort(Comparator.comparing(ReporteMayoresCanonDTO::getCanon).reversed());

        // 3. Devolver solo las primeras cantidad unidades
        if (reporte.size() > cantidad) {
            return new ArrayList<>(reporte.subList(0, cantidad));
        }
        return reporte;
    }
}