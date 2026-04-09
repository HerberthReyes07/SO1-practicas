package com.so1.estacionamientoInteligente;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gestiona la escritura de eventos del estacionamiento en CSV y consola.
 */
public class ParkingLogger {
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final Path logPath;
    private final BufferedWriter writer;
    // Protege la escritura concurrente para evitar lineas intercaladas.
    private final Object logLock = new Object();

    /**
     * Crea/limpia el archivo de log y escribe el encabezado CSV.
     */
    public ParkingLogger() {
        try {
            Path logsDir = Paths.get("logs");
            Files.createDirectories(logsDir);
            logPath = logsDir.resolve("ejercicio1.log");
            writer = Files.newBufferedWriter(
                    logPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            writer.write(
                    "timestamp,evento,vehiculo,carrosDentro,camionesDentro,libresTotal,libresVIP,esperando,desistieron");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el archivo de log", e);
        }
    }

    public void registrar(
            String evento,
            TipoVehiculo tipo,
            String nombre,
            int carrosDentro,
            int camionesDentro,
            int libresTotal,
            int libresVip,
            int esperando,
            int desistimientos) {
        String timestamp = LocalDateTime.now().format(formatoFecha);

        // Formato CSV persistente para evidencia del ejercicio.
        String linea = String.format(
                "%s,%s,%s(%s),%d,%d,%d,%d,%d,%d",
            timestamp,
                evento,
                tipo,
                nombre,
                carrosDentro,
                camionesDentro,
                libresTotal,
                libresVip,
                esperando,
                desistimientos);

        synchronized (logLock) {
            try {
                writer.write(linea);
                writer.newLine();
                writer.flush();
                String cabecera = String.format(
                    "[PARKING][%s][%s] %s (%s)",
                        evento,
                    timestamp,
                        nombre,
                    tipo);
                String detalleEstado = String.format(
                    "  Estado: carros=%d, camiones=%d | Libres total=%d, VIP=%d",
                        carrosDentro,
                        camionesDentro,
                        libresTotal,
                    libresVip);
                String detalleCola = String.format(
                    "  Cola: esperando=%d | desistieron=%d",
                        esperando,
                        desistimientos);

                System.out.println("------------------------------------------------------------");
                System.out.println(cabecera);
                System.out.println(detalleEstado);
                System.out.println(detalleCola);
            } catch (IOException e) {
                System.err.println("Error escribiendo log: " + e.getMessage());
            }
        }
    }

            public void registrarDesistimiento(
                String nombre,
                TipoVehiculo tipo,
                int esperaMaxMs,
                int carrosDentro,
                int camionesDentro,
                int libresTotal,
                int libresVip,
                int esperando,
                int desistimientos) {
            String timestamp = LocalDateTime.now().format(formatoFecha);
            String cabecera = String.format(
                "[PARKING][DESISTE][%s] %s (%s)",
                timestamp,
                nombre,
                tipo);
            String motivo = String.format(
                "  Motivo: supero la espera maxima de %d ms",
                esperaMaxMs);
            String detalleEstado = String.format(
                "  Estado: carros=%d, camiones=%d | Libres total=%d, VIP=%d",
                carrosDentro,
                camionesDentro,
                libresTotal,
                libresVip);
            String detalleCola = String.format(
                "  Cola: esperando=%d | desistieron=%d",
                esperando,
                desistimientos);

            synchronized (logLock) {
                System.out.println("------------------------------------------------------------");
                System.out.println(cabecera);
                System.out.println(motivo);
                System.out.println(detalleEstado);
                System.out.println(detalleCola);
            }
            }

    public void cerrar() {
        synchronized (logLock) {
            try {
                writer.flush();
                writer.close();
                System.out.println("Log guardado en: " + logPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("No se pudo cerrar el log: " + e.getMessage());
            }
        }
    }
}
