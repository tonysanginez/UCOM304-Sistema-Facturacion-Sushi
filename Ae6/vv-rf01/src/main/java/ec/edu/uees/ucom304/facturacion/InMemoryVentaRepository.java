package ec.edu.uees.ucom304.facturacion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryVentaRepository implements VentaRepository {
    private final Map<String, Venta> data = new HashMap<>();

    @Override
    public Venta save(Venta venta) {
        data.put(venta.getCodigoVenta(), venta);
        return venta;
    }

    @Override
    public Optional<Venta> findById(String codigoVenta) {
        return Optional.ofNullable(data.get(codigoVenta));
    }
}
