package ec.edu.uees.ucom304.facturacion;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

// Coordina el registro de la venta (ControlVenta en SEQ01)
@RequiredArgsConstructor
public class ServicioVenta {
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    public Venta iniciarVenta() {
        Venta venta = new Venta(UUID.randomUUID().toString());
        return ventaRepository.save(venta);
    }

    public Venta agregarProducto(String codigoVenta, String codigoProducto, int cantidad) {
        Venta venta = buscarVenta(codigoVenta);
        Producto producto = productoRepository.findById(codigoProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto inexistente: " + codigoProducto));

        venta.agregarDetalle(producto, cantidad);
        return ventaRepository.save(venta);
    }

    public Venta confirmarVenta(String codigoVenta) {
        Venta venta = buscarVenta(codigoVenta);
        venta.registrarVenta();
        return ventaRepository.save(venta);
    }

    private Venta buscarVenta(String codigoVenta) {
        return ventaRepository.findById(codigoVenta)
                .orElseThrow(() -> new IllegalArgumentException("Venta inexistente: " + codigoVenta));
    }
}
