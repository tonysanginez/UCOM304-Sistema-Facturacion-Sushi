## Ae6 | Expediente de V&V de RF01 (Registrar venta)

- `Sanginez_Tony_Ae6_Expediente_VV.docx`: expediente de verificación y validación
- `vv-rf01/`: proyecto Maven con las clases de dominio y las pruebas JUnit 5
- `evidencias/`: capturas de `mvn test`, reporte de Surefire y evidencia de DEF-01

Requisitos: JDK 17 o superior (se ejecutó con JDK 21) y Maven 3.9 o superior.

```bash
cd Ae6/vv-rf01
mvn test
```

Resultado esperado: `Tests run: 11, Failures: 0, Errors: 0`.
