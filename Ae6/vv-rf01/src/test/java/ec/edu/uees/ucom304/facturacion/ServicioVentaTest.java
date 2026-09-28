package ec.edu.uees.ucom304.facturacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServicioVentaTest {
    private InMemoryVentaRepository ventaRepository;
    private InMemoryProductoRepository productoRepository;
    private ServicioVenta servicio;

    @BeforeEach
    void setUp() {
        ventaRepository = new InMemoryVentaRepository();
        productoRepository = new InMemoryProductoRepository();
        servicio = new ServicioVenta(ventaRepository, productoRepository);

        // productos de prueba, precios con IVA incluido
        productoRepository.save(new Producto("P01", "Sushi burrito Crabby Crunch", new BigDecimal("11.50"), true));
        productoRepository.save(new Producto("P02", "Sushi burrito Cosmo", new BigDecimal("9.20"), true));
        productoRepository.save(new Producto("P03", "Poke de atun", new BigDecimal("12.65"), false));
    }

    // CP-01 | RF01 - CA-01
    @Test
    void debeRegistrarVentaConTotalYDesgloseDeIva() {
        // Arrange
        Venta venta = servicio.iniciarVenta();
        servicio.agregarProducto(venta.getCodigoVenta(), "P01", 2);
        servicio.agregarProducto(venta.getCodigoVenta(), "P02", 1);

        // Act
        Venta registrada = servicio.confirmarVenta(venta.getCodigoVenta());

        // Assert
        assertAll(
                () -> assertEquals(EstadoVenta.REGISTRADA, registrada.getEstado()),
                () -> assertEquals(2, registrada.getDetalles().size()),
                () -> assertEquals(new BigDecimal("32.20"), registrada.calcularTotal()),
                () -> assertEquals(new BigDecimal("28.00"), registrada.calcularSubtotalSinImpuestos()),
                () -> assertEquals(new BigDecimal("4.20"), registrada.calcularIva())
        );
    }

    // CP-02 | RF01 - CA-02
    @Test
    void debeRechazarProductoNoDisponible() {
        Venta venta = servicio.iniciarVenta();

        assertThrows(IllegalStateException.class,
                () -> servicio.agregarProducto(venta.getCodigoVenta(), "P03", 1));
        assertTrue(venta.getDetalles().isEmpty());
    }

    // CP-03 | RF01 - CA-04 (cantidades fuera del limite)
    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void debeRechazarCantidadCeroONegativa(int cantidad) {
        Venta venta = servicio.iniciarVenta();

        assertThrows(IllegalArgumentException.class,
                () -> servicio.agregarProducto(venta.getCodigoVenta(), "P01", cantidad));
        assertTrue(venta.getDetalles().isEmpty());
    }

    // CP-03 | RF01 - CA-04 (limite valido)
    @Test
    void debeAceptarCantidadMinimaDeUno() {
        Venta venta = servicio.iniciarVenta();

        servicio.agregarProducto(venta.getCodigoVenta(), "P01", 1);

        assertEquals(1, venta.getDetalles().size());
    }

    // CP-04 | RF01 - CA-03
    @Test
    void debeRechazarConfirmarVentaSinProductos() {
        Venta venta = servicio.iniciarVenta();

        assertThrows(IllegalStateException.class,
                () -> servicio.confirmarVenta(venta.getCodigoVenta()));
        assertEquals(EstadoVenta.EN_PROCESO, venta.getEstado());
    }

    // CP-05 | RF01 - CA-01 (precio del momento)
    @Test
    void debeMantenerPrecioDeLaVentaSiCambiaElPrecioDelProducto() {
        // Arrange
        Venta venta = servicio.iniciarVenta();
        servicio.agregarProducto(venta.getCodigoVenta(), "P02", 1);

        // Act: se sube el precio en el catalogo antes de confirmar
        productoRepository.findById("P02").orElseThrow().setPrecio(new BigDecimal("10.35"));
        Venta registrada = servicio.confirmarVenta(venta.getCodigoVenta());

        // Assert
        assertAll(
                () -> assertEquals(new BigDecimal("9.20"), registrada.getDetalles().get(0).getPrecioUnitario()),
                () -> assertEquals(new BigDecimal("9.20"), registrada.calcularTotal())
        );
    }
}
