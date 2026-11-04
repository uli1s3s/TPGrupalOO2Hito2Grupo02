
package com.unla.ghsicilianotfi.controllers;

import com.unla.ghsicilianotfi.entities.Cajero;
import com.unla.ghsicilianotfi.entities.CierreCaja;
import com.unla.ghsicilianotfi.entities.Personal;
import com.unla.ghsicilianotfi.entities.Pedido;
import com.unla.ghsicilianotfi.entities.UnidadVenta;

import com.unla.ghsicilianotfi.repositories.ICierreCajaRepository;
import com.unla.ghsicilianotfi.repositories.IPedidoRepository;
import com.unla.ghsicilianotfi.repositories.IUnidadVentaRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasRole('EMPLEADO')")
public class CierreCajaController {

    private final IUnidadVentaRepository unidadRepository;
    private final IPedidoRepository pedidoRepository;
    private final ICierreCajaRepository cierreRepository;

    public CierreCajaController(
            IUnidadVentaRepository unidadRepository,
            IPedidoRepository pedidoRepository,
            ICierreCajaRepository cierreRepository) {

        this.unidadRepository = unidadRepository;
        this.pedidoRepository = pedidoRepository;
        this.cierreRepository = cierreRepository;
    }

    // Datos del cajero y su unidad de venta.
    private record DatosCaja(
            UnidadVenta unidad,
            Cajero cajero
    ) {}

    // Busca al cajero autenticado y verifica que esté activo.
    private Optional<DatosCaja> buscarCaja(String username) {

        Optional<UnidadVenta> unidadOpt =
                unidadRepository.buscarUnidadDeEmpleado(username);

        if (unidadOpt.isEmpty()) {
            return Optional.empty();
        }

        UnidadVenta unidad = unidadOpt.get();

        return unidad.getEmpleados().stream()
                .filter(Personal::isActivo)
                .filter(p -> p instanceof Cajero)
                .filter(p -> p.getCredenciales() != null)
                .filter(p -> username.equals(
                        p.getCredenciales().getNombre()))
                .map(p -> new DatosCaja(unidad, (Cajero) p))
                .findFirst();
    }

    // ============================================
    // MOSTRAR CIERRE DE CAJA
    // ============================================

    @GetMapping("/cerrar-caja")
    @Transactional(readOnly = true)
    public String verCaja(
            Authentication authentication,
            Model model) {

        Optional<DatosCaja> datosOpt =
                buscarCaja(authentication.getName());

        if (datosOpt.isEmpty()) {
            model.addAttribute("error",
                    "No tenés una asignación activa como cajero.");

            return "empleados/cerrar-caja";
        }

        DatosCaja datos = datosOpt.get();
        LocalDate hoy = LocalDate.now();
        Long idUnidad = datos.unidad().getIdUnidad();

        List<Pedido> pedidos =
                pedidoRepository.findByUnidadVenta_IdUnidadAndFechaPedido(
                        idUnidad,
                        hoy
                );

        double total = pedidos.stream()
                .mapToDouble(Pedido::getTotal)
                .sum();

        Optional<CierreCaja> cierreOpt =
                cierreRepository.findByUnidadVenta_IdUnidadAndFecha(
                        idUnidad,
                        hoy
                );

        boolean cerrada = cierreOpt
                .map(CierreCaja::isCerrada)
                .orElse(false);

        boolean puedeReabrir = cierreOpt
                .filter(CierreCaja::isCerrada)
                .filter(c -> c.getCajero().getIdPersonal()
                        .equals(datos.cajero().getIdPersonal()))
                .isPresent();

        model.addAttribute("unidad",
                datos.unidad().getNombreComercial());

        model.addAttribute("cajero",
                datos.cajero().getNombre() + " "
                        + datos.cajero().getApellido());

        model.addAttribute("fecha", hoy);
        model.addAttribute("cantidadPedidos", pedidos.size());
        model.addAttribute("total", total);
        model.addAttribute("cerrada", cerrada);
        model.addAttribute("puedeReabrir", puedeReabrir);

        cierreOpt.ifPresent(c -> {
            model.addAttribute("cantidadReaperturas",
                    c.getCantidadReaperturas());
            model.addAttribute("motivoReapertura",
                    c.getMotivoReapertura());
        });

        return "empleados/cerrar-caja";
    }

    // ============================================
    // CONFIRMAR CIERRE DE CAJA
    // ============================================

    @PostMapping("/cerrar-caja")
    @Transactional
    public String confirmarCierre(
            Authentication authentication,
            RedirectAttributes redirect) {

        Optional<DatosCaja> datosOpt =
                buscarCaja(authentication.getName());

        if (datosOpt.isEmpty()) {
            redirect.addFlashAttribute("error",
                    "Solo un cajero activo y asignado puede cerrar caja.");

            return "redirect:/cerrar-caja";
        }

        DatosCaja datos = datosOpt.get();
        LocalDate hoy = LocalDate.now();
        Long idUnidad = datos.unidad().getIdUnidad();

        Optional<CierreCaja> cierreOpt =
                cierreRepository.findByUnidadVenta_IdUnidadAndFecha(
                        idUnidad,
                        hoy
                );

        if (cierreOpt.isPresent() && cierreOpt.get().isCerrada()) {
            redirect.addFlashAttribute("error",
                    "La caja de esta unidad ya fue cerrada hoy.");

            return "redirect:/cerrar-caja";
        }

        // Si existe un cierre reabierto, solamente puede
        // volver a cerrarlo el cajero que lo realizó.
        if (cierreOpt.isPresent()
                && !cierreOpt.get().getCajero().getIdPersonal()
                .equals(datos.cajero().getIdPersonal())) {

            redirect.addFlashAttribute("error",
                    "Solo el cajero que realizó el cierre puede volver a cerrarlo.");

            return "redirect:/cerrar-caja";
        }

        List<Pedido> pedidos =
                pedidoRepository.findByUnidadVenta_IdUnidadAndFechaPedido(
                        idUnidad,
                        hoy
                );

        if (pedidos.isEmpty()) {
            redirect.addFlashAttribute("error",
                    "No hay pedidos registrados para cerrar la caja.");

            return "redirect:/cerrar-caja";
        }

        double total = pedidos.stream()
                .mapToDouble(Pedido::getTotal)
                .sum();

        if (cierreOpt.isPresent()) {

            // La caja había sido reabierta:
            // actualiza el cierre existente.
            CierreCaja cierre = cierreOpt.get();

            cierre.volverACerrar(
                    datos.cajero(),
                    pedidos.size(),
                    total
            );

            cierreRepository.saveAndFlush(cierre);

        } else {

            // Primer cierre del día.
            CierreCaja cierre = new CierreCaja(
                    datos.unidad(),
                    datos.cajero(),
                    hoy,
                    pedidos.size(),
                    total
            );

            cierreRepository.saveAndFlush(cierre);
        }

        redirect.addFlashAttribute("exito",
                "La caja se cerró correctamente.");

        return "redirect:/cerrar-caja";
    }

    // ============================================
    // REABRIR CAJA
    // ============================================

    @PostMapping("/reabrir-caja")
    @Transactional
    public String reabrirCaja(
            Authentication authentication,
            @RequestParam("motivo") String motivo,
            RedirectAttributes redirect) {

        Optional<DatosCaja> datosOpt =
                buscarCaja(authentication.getName());

        if (datosOpt.isEmpty()) {
            redirect.addFlashAttribute("error",
                    "Solo un cajero activo puede reabrir la caja.");

            return "redirect:/cerrar-caja";
        }

        DatosCaja datos = datosOpt.get();

        Optional<CierreCaja> cierreOpt =
                cierreRepository.findByUnidadVenta_IdUnidadAndFecha(
                        datos.unidad().getIdUnidad(),
                        LocalDate.now()
                );

        if (cierreOpt.isEmpty()) {
            redirect.addFlashAttribute("error",
                    "No existe un cierre de caja para reabrir.");

            return "redirect:/cerrar-caja";
        }

        CierreCaja cierre = cierreOpt.get();

        if (!cierre.getCajero().getIdPersonal()
                .equals(datos.cajero().getIdPersonal())) {

            redirect.addFlashAttribute("error",
                    "Solo el cajero que realizó el cierre puede reabrirlo.");

            return "redirect:/cerrar-caja";
        }

        if (!cierre.isCerrada()) {
            redirect.addFlashAttribute("error",
                    "La caja ya se encuentra abierta.");

            return "redirect:/cerrar-caja";
        }

        if (motivo == null || motivo.isBlank()) {
            redirect.addFlashAttribute("error",
                    "Debés indicar un motivo para reabrir la caja.");

            return "redirect:/cerrar-caja";
        }

        if (motivo.trim().length() > 255) {
            redirect.addFlashAttribute("error",
                    "El motivo no puede superar los 255 caracteres.");

            return "redirect:/cerrar-caja";
        }

        cierre.reabrir(motivo);

        cierreRepository.saveAndFlush(cierre);

        redirect.addFlashAttribute("exito",
                "La caja se reabrió correctamente. Ya podés registrar nuevos pedidos.");

        return "redirect:/cerrar-caja";
    }
}
