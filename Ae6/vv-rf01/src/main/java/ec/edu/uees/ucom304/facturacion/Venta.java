package ec.edu.uees.ucom304.facturacion;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Venta {
    private static final BigDecimal FACTOR_IVA = new BigDecimal("1.15");

    private final String codigoVenta;
    private final LocalDateTime fechaVenta;
    private EstadoVenta estado;
    private final List<DetalleVenta> detalles = new ArrayList<>();

    public Venta(String codigoVenta) {
        this.codigoVenta = codigoVenta;
        this.fechaVenta = LocalDateTime.now();
        this.estado = EstadoVenta.EN_PROCESO;
    }

    public void agregarDetalle(Producto producto, int cantidad) {
        // validaciones antes de agregar
        if (estado != EstadoVenta.EN_PROCESO) {
            throw new IllegalStateException("La venta ya fue registrada, no se pueden agregar productos");
        }
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (!producto.verificarDisponibilidad()) {
            throw new IllegalStateException("El producto " + producto.getNombre() + " no está disponible");
        }

        // se guarda el precio del momento
        detalles.add(new DetalleVenta(producto, cantidad, producto.obtenerPrecio()));
    }

    // total de la venta, los precios ya incluyen IVA
    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVenta detalle : detalles) {
            total = total.add(detalle.calcularSubtotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    // desglose: base sin IVA
    public BigDecimal calcularSubtotalSinImpuestos() {
        return calcularTotal().divide(FACTOR_IVA, 2, RoundingMode.HALF_UP);
    }

    // desglose: IVA = total - base
    public BigDecimal calcularIva() {
        return calcularTotal().subtract(calcularSubtotalSinImpuestos());
    }

    public void registrarVenta() {
        if (estado != EstadoVenta.EN_PROCESO) {
            throw new IllegalStateException("La venta ya fue registrada");
        }
        if (detalles.isEmpty()) {
            throw new IllegalStateException("Debe seleccionarse al menos un producto");
        }
        estado = EstadoVenta.REGISTRADA;
    }
}
