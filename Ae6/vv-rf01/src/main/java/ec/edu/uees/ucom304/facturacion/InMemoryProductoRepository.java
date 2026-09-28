package ec.edu.uees.ucom304.facturacion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryProductoRepository implements ProductoRepository {
    private final Map<String, Producto> data = new HashMap<>();

    @Override
    public Producto save(Producto producto) {
        data.put(producto.getCodigoProducto(), producto);
        return producto;
    }

    @Override
    public Optional<Producto> findById(String codigoProducto) {
        return Optional.ofNullable(data.get(codigoProducto));
    }
}
