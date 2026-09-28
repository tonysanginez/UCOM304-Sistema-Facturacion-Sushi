package ec.edu.uees.ucom304.facturacion;

import java.util.Optional;

public interface VentaRepository {
    Venta save(Venta venta);
    Optional<Venta> findById(String codigoVenta);
}
