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
import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.repositories.IFestivalRepository;

import com.unla.ghsicilianotfi.entities.Costo;
import com.unla.ghsicilianotfi.entities.TipoCostoFestival;
import com.unla.ghsicilianotfi.entities.TipoUnidadAplicable;
import com.unla.ghsicilianotfi.helpers.TarifaCanon;

import com.unla.ghsicilianotfi.entities.Personal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.math.BigDecimal;

import java.time.LocalDate;

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
    private final IFestivalRepository festivalRepository;

    public UnidadVentaService(IUnidadVentaRepository unidadVentaRepository,
            IUsuarioRepository usuarioRepository,
            IRolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            ParametrosCanon parametrosCanon,
            IFestivalRepository festivalRepository) {
	this.unidadVentaRepository = unidadVentaRepository;
	this.usuarioRepository = usuarioRepository;
	this.rolRepository = rolRepository;
	this.passwordEncoder = passwordEncoder;
	this.parametrosCanon = parametrosCanon;
    this.festivalRepository = festivalRepository;
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
        
        // 0. El festival tiene que existir y estar activo
        Festival festival = buscarFestivalActivo(datos.getIdFestival());

        // 1. Crear segun el tipo elegido
        UnidadVenta unidad = crearSegunTipo(datos);

        // 2. Datos comunes
        unidad.setNombreComercial(datos.getNombreComercial().trim());
        unidad.setSuperficieM2(datos.getSuperficieM2());
        unidad.getFestivales().add(festival);

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
    // Validacion de carga: solo se puede registrar en un festival activo y vigente
    // =====================================================
    private Festival buscarFestivalActivo(Long idFestival) {

        if (idFestival == null) {
            throw new IllegalArgumentException("Seleccioná el festival en el que vas a participar");
        }

        Festival festival = festivalRepository.findById(idFestival)
                .orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe"));

        if (!festival.isActivo()) {
            throw new IllegalArgumentException("Solo podés registrarte en un festival activo");
        }

        if (festival.getFechaFin().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("El festival seleccionado ya finalizó");
        }

        return festival;
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
    public List<ReporteMayoresCanonDTO> unidadesConMayorCanon(Long idFestival, int cantidad) {

        if (cantidad < 1) {
            throw new IllegalArgumentException("La cantidad de unidades debe ser al menos 1");
        }
        if (idFestival == null) {
            throw new IllegalArgumentException("Seleccioná un festival");
        }

        Festival festival = festivalRepository.findById(idFestival)
                .orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe"));

        // 1. Calcular el canon de cada unidad DEL festival, con los costos DE ESE festival
        List<ReporteMayoresCanonDTO> reporte = new ArrayList<>();

        for (UnidadVenta unidad : festival.getUnidades()) {
            reporte.add(new ReporteMayoresCanonDTO(
                    unidad.getNombreComercial(),
                    unidad.getCodigoUnico(),
                    unidad.getTipo(),
                    unidad.calcularCanon(armarTarifa(unidad, festival))
            ));
        }

        // 2. Ordenar de mayor a menor canon
        reporte.sort(Comparator.comparing(ReporteMayoresCanonDTO::getCanon).reversed());

        // 3. Devolver solo las primeras "cantidad" unidades
        if (reporte.size() > cantidad) {
            return new ArrayList<>(reporte.subList(0, cantidad));
        }
        return reporte;
    }
    
    // =====================================================
    // MI UNIDAD: empleados disponibles, asignar, quitar y canon
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<Personal> listarEmpleadosDisponibles() {
        return unidadVentaRepository.buscarEmpleadosDisponibles();
    }

    @Override
    @Transactional
    public void asignarEmpleado(Long idUnidad, Long idPersonal, String nombreResponsable) {

        UnidadVenta unidad = buscarUnidadDelResponsable(idUnidad, nombreResponsable);

        Personal empleado = unidadVentaRepository.buscarEmpleadoPorId(idPersonal)
                .orElseThrow(() -> new IllegalArgumentException("El empleado no existe"));

        // Solo empleados activos
        if (!empleado.isActivo()) {
            throw new IllegalArgumentException("No se puede asignar un empleado dado de baja");
        }

        // Disponibilidad: un empleado solo puede trabajar en una unidad
        if (unidadVentaRepository.existsByEmpleados_IdPersonal(idPersonal)) {
            throw new IllegalArgumentException("El empleado ya está asignado a una unidad de venta");
        }

        unidad.getEmpleados().add(empleado);
        unidadVentaRepository.save(unidad);
    }

    @Override
    @Transactional
    public void quitarEmpleado(Long idUnidad, Long idPersonal, String nombreResponsable) {

        UnidadVenta unidad = buscarUnidadDelResponsable(idUnidad, nombreResponsable);

        boolean quitado = unidad.getEmpleados()
                .removeIf(empleado -> empleado.getIdPersonal().equals(idPersonal));

        if (!quitado) {
            throw new IllegalArgumentException("El empleado no pertenece a esta unidad");
        }

        unidadVentaRepository.save(unidad);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> calcularCanonPorFestival(Long idUnidad) {

        UnidadVenta unidad = unidadVentaRepository.findById(idUnidad)
                .orElseThrow(() -> new IllegalArgumentException("La unidad de venta no existe"));

        // LinkedHashMap mantiene el orden en que se agregan los festivales
        Map<String, BigDecimal> canones = new LinkedHashMap<>();

        for (Festival festival : unidad.getFestivales()) {
            canones.put(festival.getNombre(), unidad.calcularCanon(armarTarifa(unidad, festival)));
        }

        return canones;
    }

    // La unidad tiene que existir y estar a cargo del responsable que inicio sesión
    private UnidadVenta buscarUnidadDelResponsable(Long idUnidad, String nombreResponsable) {

        UnidadVenta unidad = unidadVentaRepository.findById(idUnidad)
                .orElseThrow(() -> new IllegalArgumentException("La unidad de venta no existe"));

        if (!unidad.getResponsable().getNombre().equals(nombreResponsable)) {
            throw new IllegalArgumentException("No podés modificar una unidad que no está a tu cargo");
        }

        return unidad;
    }
    
    // =====================================================
    // Arma la tarifa de canon de una unidad segun los costos de UN festival
    // =====================================================
    private TarifaCanon armarTarifa(UnidadVenta unidad, Festival festival) {

        BigDecimal costoPorM2 = buscarMonto(festival, TipoCostoFestival.POR_SUPERFICIE,
                unidad, parametrosCanon.getCostoPorM2());

        BigDecimal plusElectricidad = buscarMonto(festival, TipoCostoFestival.USO_ELECTRICIDAD,
                unidad, parametrosCanon.getPlusElectricidad());

        BigDecimal descuentoMontaje = buscarMonto(festival, TipoCostoFestival.POR_MONTAJE,
                unidad, parametrosCanon.getDescuentoPorMinutoMontaje());

        return new TarifaCanon(costoPorM2, plusElectricidad, descuentoMontaje);
    }

    // Monto de un tipo de costo del festival:
    // - si el festival lo tiene y aplica a esta unidad -> el monto del festival
    // - si lo tiene pero es para el otro tipo de unidad -> 0
    // - si el festival no lo configuro -> el valor por defecto del enunciado
    private BigDecimal buscarMonto(Festival festival, TipoCostoFestival tipo,
                                   UnidadVenta unidad, BigDecimal valorPorDefecto) {
        if (festival == null) {
            return valorPorDefecto;
        }

        for (Costo costo : festival.getCostos()) {
            if (costo.getTipoCosto() == tipo) {
                if (aplicaA(costo.getTipoUnidadAplicable(), unidad)) {
                    return costo.getMonto();
                }
                return BigDecimal.ZERO;
            }
        }

        return valorPorDefecto;
    }

    // ¿El costo aplica a este tipo de unidad?
    private boolean aplicaA(TipoUnidadAplicable aplicable, UnidadVenta unidad) {
        if (aplicable == TipoUnidadAplicable.TODAS) {
            return true;
        }
        if (aplicable == TipoUnidadAplicable.FOOD_TRUCK) {
            return unidad instanceof FoodTruck;
        }
        if (aplicable == TipoUnidadAplicable.PUESTO_DESMONTABLE) {
            return unidad instanceof PuestoDesmontable;
        }
        return false;
    }
    
    
}