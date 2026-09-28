package ec.edu.uees.ucom304.facturacion;

import java.util.Optional;

public interface ProductoRepository {
    Producto save(Producto producto);
    Optional<Producto> findById(String codigoProducto);
}
