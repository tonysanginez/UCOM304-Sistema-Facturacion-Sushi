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
        productoRepository.save(new Producto("P03", "Poké de atún", new BigDecimal("12.65"), false));

        // ramen disponible pero inactivo (ya no se vende)
        Producto ramen = new Producto("P04", "Ramen", new BigDecimal("10.35"), true);
        ramen.setActivo(false);
        productoRepository.save(ramen);
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

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> servicio.agregarProducto(venta.getCodigoVenta(), "P03", 1));

        assertAll(
                () -> assertEquals("El producto Poké de atún no está disponible", error.getMessage()),
                () -> assertTrue(venta.getDetalles().isEmpty())
        );
    }

    // CP-03 | RF01 - CA-04 (cantidades fuera del límite)
    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void debeRechazarCantidadCeroONegativa(int cantidad) {
        Venta venta = servicio.iniciarVenta();

        assertThrows(IllegalArgumentException.class,
                () -> servicio.agregarProducto(venta.getCodigoVenta(), "P01", cantidad));
        assertTrue(venta.getDetalles().isEmpty());
    }

    // CP-03 | RF01 - CA-04 (límite válido)
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

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> servicio.confirmarVenta(venta.getCodigoVenta()));

        assertAll(
                () -> assertEquals("Debe seleccionarse al menos un producto", error.getMessage()),
                () -> assertEquals(EstadoVenta.EN_PROCESO, venta.getEstado())
        );
    }

    // CP-05 | RF01 - CA-01 (precio del momento)
    @Test
    void debeMantenerPrecioDeLaVentaSiCambiaElPrecioDelProducto() {
        // Arrange
        Venta venta = servicio.iniciarVenta();
        servicio.agregarProducto(venta.getCodigoVenta(), "P02", 1);

        // Act: se sube el precio en el catálogo antes de confirmar
        productoRepository.findById("P02").get().setPrecio(new BigDecimal("10.35"));
        Venta registrada = servicio.confirmarVenta(venta.getCodigoVenta());

        // Assert
        assertAll(
                () -> assertEquals(new BigDecimal("9.20"), registrada.getDetalles().get(0).getPrecioUnitario()),
                () -> assertEquals(new BigDecimal("9.20"), registrada.calcularTotal())
        );
    }

    // CP-06 | RF01 - CA-05 (estado: venta ya registrada)
    @Test
    void debeRechazarAgregarProductoAVentaRegistrada() {
        Venta venta = servicio.iniciarVenta();
        servicio.agregarProducto(venta.getCodigoVenta(), "P01", 1);
        servicio.confirmarVenta(venta.getCodigoVenta());

        assertThrows(IllegalStateException.class,
                () -> servicio.agregarProducto(venta.getCodigoVenta(), "P02", 1));
        assertAll(
                () -> assertEquals(1, venta.getDetalles().size()),
                () -> assertEquals(new BigDecimal("11.50"), venta.calcularTotal())
        );
    }

    // CP-06 | RF01 - CA-05 (estado: confirmar dos veces)
    @Test
    void debeRechazarConfirmarDosVecesLaMismaVenta() {
        Venta venta = servicio.iniciarVenta();
        servicio.agregarProducto(venta.getCodigoVenta(), "P01", 1);
        servicio.confirmarVenta(venta.getCodigoVenta());

        assertThrows(IllegalStateException.class,
                () -> servicio.confirmarVenta(venta.getCodigoVenta()));
        assertEquals(EstadoVenta.REGISTRADA, venta.getEstado());
    }

    // CP-07 | RF01 - CA-02 (producto inactivo)
    @Test
    void debeRechazarProductoInactivo() {
        Venta venta = servicio.iniciarVenta();

        assertThrows(IllegalStateException.class,
                () -> servicio.agregarProducto(venta.getCodigoVenta(), "P04", 1));
        assertTrue(venta.getDetalles().isEmpty());
    }

    // CP-08 | RF01 - CA-01 (DEF-02: detalles protegidos)
    @Test
    void debeImpedirModificarLosDetallesDesdeFueraDeLaVenta() {
        Venta venta = servicio.iniciarVenta();
        servicio.agregarProducto(venta.getCodigoVenta(), "P01", 1);
        Producto cosmo = productoRepository.findById("P02").get();

        assertThrows(UnsupportedOperationException.class,
                () -> venta.getDetalles().add(new DetalleVenta(cosmo, 1, cosmo.getPrecio())));
        assertEquals(new BigDecimal("11.50"), venta.calcularTotal());
    }
}
