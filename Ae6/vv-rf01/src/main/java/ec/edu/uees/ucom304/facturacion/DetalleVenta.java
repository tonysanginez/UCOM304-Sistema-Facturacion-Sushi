package ec.edu.uees.ucom304.facturacion;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DetalleVenta {
    private final Producto producto;
    private final int cantidad;
    private final BigDecimal precioUnitario; // precio cobrado en el momento de la venta

    public DetalleVenta(Producto producto, int cantidad, BigDecimal precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal calcularSubtotal() {
        return producto.obtenerPrecio().multiply(BigDecimal.valueOf(cantidad));
    }
}
