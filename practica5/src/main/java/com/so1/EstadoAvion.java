package com.so1;

/**
 * Estados posibles de un avión durante la simulación.
 */

public enum EstadoAvion {
    ESPERANDO, // Esperando una sección de pista disponible
    EN_POSICION, // Avión en tierra, etapa 1 completada (pre-despegue)
    DESPEGANDO, // Ejecutando la carrera de despegue
    ATERRIZANDO, // Reservando secciones y aterrizando
    COMPLETADO // Terminó su operación (despegó o aterrizó)
}