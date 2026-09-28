package ec.edu.uees.ucom304.facturacion;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Producto {
    private final String codigoProducto;
    private final String nombre;
    private BigDecimal precio; // precio de carta, ya incluye IVA
    private boolean disponible;
    private boolean activo;

    public Producto(String codigoProducto, String nombre, BigDecimal precio, boolean disponible) {
        this.codigoProducto = codigoProducto;
        this.nombre = nombre;
        this.precio = precio;
        this.disponible = disponible;
        this.activo = true;
    }

    public boolean verificarDisponibilidad() {
        return disponible && activo;
    }

    public BigDecimal obtenerPrecio() {
        return precio;
    }
}
