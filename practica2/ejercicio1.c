#include <stdio.h>
#include <stdlib.h> 
#include <pthread.h>
#include <string.h>

// Estructura para representar cada alimento con sus atributos y mutex
typedef struct {
    char nombre[30]; // Nombre del alimento
    double cantidadActual; // Cantidad actual en el inventario
    double produccion; // Cantidad que se produce en cada iteración
    double consumo; // Cantidad que se consume en cada iteración
    double capacidadMaxima; // Capacidad máxima del inventario para este alimento
    double totalProducido; // Total producido durante la simulación
    double totalConsumido; // Total consumido durante la simulación
    int vecesSinStock; // Contador de veces que se intentó consumir sin stock suficiente
    int vecesLleno; // Contador de veces que se intentó producir pero el inventario estaba lleno
    pthread_mutex_t mutex; // Mutex para proteger el acceso a este alimento
} Alimento;

// Número de iteraciones para la simulación
const int NUMERO_SIMULACIONES = 75;

// Cantidades de producción y consumo para cada alimento
const double PRODUCCION_TRIGO = 10;
const double CONSUMO_TRIGO = 5;

const double PRODUCCION_CARNE = 8;
const double CONSUMO_CARNE = 4;

const double PRODUCCION_FRUTA_VERDURA = 12;
const double CONSUMO_FRUTA_VERDURA = 6;

// Inicializa un alimento con sus parámetros y mutex
void inicializarAlimento(Alimento* alimento, const char* nombre, double produccion, double consumo) {
    strcpy(alimento->nombre, nombre);
    alimento->produccion = produccion;
    alimento->consumo = consumo;
    alimento->cantidadActual = 0;
    alimento->totalProducido = 0;
    alimento->totalConsumido = 0;
    alimento->vecesSinStock = 0;
    alimento->vecesLleno = 0;
    pthread_mutex_init(&alimento->mutex, NULL);
}

// Inicializa cada alimento con sus valores específicos
void iniciarTrigo(Alimento* trigo) {
    inicializarAlimento(trigo, "Trigo", PRODUCCION_TRIGO, CONSUMO_TRIGO);
}

void iniciarCarne(Alimento* carne) {
    inicializarAlimento(carne, "Carne", PRODUCCION_CARNE, CONSUMO_CARNE);
}

void iniciarFrutaVerdura(Alimento* frutaVerdura) {
    inicializarAlimento(frutaVerdura, "Frutas y Verduras", PRODUCCION_FRUTA_VERDURA, CONSUMO_FRUTA_VERDURA);
}

// Imprime el resumen estadístico de un alimento
void imprimirResumen(Alimento* alimento) {
    printf("  %-25s %.2f kg\n", "Cantidad Final:", alimento->cantidadActual);
    printf("  %-25s %.2f kg\n", "Total Producido:", alimento->totalProducido);
    printf("  %-25s %.2f kg\n", "Total Consumido:", alimento->totalConsumido);
    printf("  %-25s %d veces\n", "Sin Stock:", alimento->vecesSinStock);
    printf("  %-25s %d veces\n", "Tuvo Capacidad Llena:", alimento->vecesLleno);
}

// Solicita y valida la capacidad máxima de un alimento
double obtenerCapacidadMaxima(const char* nombre_alimento) {
    double capacidad;
    printf("  Capacidad maxima de %s (kg): ", nombre_alimento);
    scanf("%lf", &capacidad);

    if (capacidad < 0) {
        printf("  [ERROR] La cantidad ingresada no puede ser negativa.\n");
        return -1;
    }
    return capacidad;
}

// Productor: incrementa la cantidad de alimento si hay espacio
void* producir(void* arg) {

    Alimento* alimento = (Alimento*)arg;

    for (int i = 0; i < NUMERO_SIMULACIONES; i++) {
        pthread_mutex_lock(&alimento->mutex); // Bloquear el mutex para acceder al alimento

        if (alimento->cantidadActual + alimento->produccion > alimento->capacidadMaxima)
        {
            alimento->vecesLleno++; // Incrementar el contador de veces que se intentó producir pero el inventario estaba lleno
            printf("[PRODUCTOR - %s]: Intento producir %.2f kg - [%d] RECHAZADO (capacidad maxima alcanzada)\n", alimento->nombre, alimento->produccion, alimento->vecesLleno);
            pthread_mutex_unlock(&alimento->mutex); // Desbloquear el mutex antes de continuar
            continue;
        }
        
        alimento->cantidadActual += alimento->produccion; // Incrementar la cantidad actual con la producción
        alimento->totalProducido += alimento->produccion; // Incrementar el total producido
        pthread_mutex_unlock(&alimento->mutex); // Desbloquear el mutex después de modificar el alimento
    }
    return NULL;
}

// Consumidor: decrementa la cantidad de alimento si hay disponibilidad
void* consumir(void* arg) {

    Alimento* alimento = (Alimento*)arg;

    for (int i = 0; i < NUMERO_SIMULACIONES; i++) {
        pthread_mutex_lock(&alimento->mutex); // Bloquear el mutex para acceder al alimento

        if (alimento->cantidadActual < alimento->consumo)
        {
            alimento->vecesSinStock++; // Incrementar el contador de veces que se intentó consumir sin stock suficiente
            printf("[CONSUMIDOR - %s]: Intento consumir %.2f kg - [%d] RECHAZADO (stock insuficiente)\n", alimento->nombre, alimento->consumo,  alimento->vecesSinStock);
            pthread_mutex_unlock(&alimento->mutex); // Desbloquear el mutex antes de continuar
            continue;
        }

        alimento->cantidadActual -= alimento->consumo; // Decrementar la cantidad actual con el consumo
        alimento->totalConsumido += alimento->consumo; // Incrementar el total consumido
        pthread_mutex_unlock(&alimento->mutex); // Desbloquear el mutex después de modificar el alimento
    }
    return NULL;
}

int main() {

    printf("\n========================================\n");
    printf("   SIMULADOR DE INVENTARIO ALIMENTARIO\n");
    printf("========================================\n\n");

    // Inicializar alimentos
    Alimento trigo = {0};
    iniciarTrigo(&trigo);
    
    Alimento carne = {0};
    iniciarCarne(&carne);
    
    Alimento frutaVerdura = {0};
    iniciarFrutaVerdura(&frutaVerdura);

    // Obtener capacidades máximas de cada alimento con validación
    printf("Ingrese las capacidades maximas de cada alimento:\n");
    trigo.capacidadMaxima = obtenerCapacidadMaxima("trigo");
    if (trigo.capacidadMaxima < 0) return EXIT_FAILURE;

    carne.capacidadMaxima = obtenerCapacidadMaxima("carne");
    if (carne.capacidadMaxima < 0) return EXIT_FAILURE;

    frutaVerdura.capacidadMaxima = obtenerCapacidadMaxima("frutas y verduras");
    if (frutaVerdura.capacidadMaxima < 0) return EXIT_FAILURE;

    printf("\nIniciando simulacion con %d iteraciones...\n\n", NUMERO_SIMULACIONES);

    // Crear hilos para productores y consumidores
    pthread_t productorTrigo, consumidorTrigo1, consumidorTrigo2;
    pthread_t productorCarne, consumidorCarne1, consumidorCarne2;
    pthread_t productorFrutaVerdura, consumidorFrutaVerdura1, consumidorFrutaVerdura2;

    // Iniciar hilos para trigo
    pthread_create(&productorTrigo, NULL, producir, &trigo);
    pthread_create(&consumidorTrigo1, NULL, consumir, &trigo);
    pthread_create(&consumidorTrigo2, NULL, consumir, &trigo);

    // Iniciar hilos para carne
    pthread_create(&productorCarne, NULL, producir, &carne);
    pthread_create(&consumidorCarne1, NULL, consumir, &carne);
    pthread_create(&consumidorCarne2, NULL, consumir, &carne);

    // Iniciar hilos para frutas y verduras
    pthread_create(&productorFrutaVerdura, NULL, producir, &frutaVerdura);
    pthread_create(&consumidorFrutaVerdura1, NULL, consumir, &frutaVerdura);
    pthread_create(&consumidorFrutaVerdura2, NULL, consumir, &frutaVerdura);

    // Esperar a que los hilos de trigo terminen
    pthread_join(productorTrigo, NULL);
    pthread_join(consumidorTrigo1, NULL);
    pthread_join(consumidorTrigo2, NULL);

    // Esperar a que los hilos de carne terminen
    pthread_join(productorCarne, NULL);
    pthread_join(consumidorCarne1, NULL);
    pthread_join(consumidorCarne2, NULL);

    // Esperar a que los hilos de frutas y verduras terminen
    pthread_join(productorFrutaVerdura, NULL);
    pthread_join(consumidorFrutaVerdura1, NULL);
    pthread_join(consumidorFrutaVerdura2, NULL);

    // Limpiar recursos
    pthread_mutex_destroy(&trigo.mutex);
    pthread_mutex_destroy(&carne.mutex);
    pthread_mutex_destroy(&frutaVerdura.mutex);

    // Mostrar resumen final
    printf("\n========================================\n");
    printf("        RESUMEN FINAL DE SIMULACION\n");
    printf("========================================\n\n");

    printf("TRIGO:\n");
    imprimirResumen(&trigo);
    printf("\n");
    printf("========================================\n\n");

    printf("CARNE:\n");
    imprimirResumen(&carne);
    printf("\n");
    printf("========================================\n\n");

    printf("FRUTAS Y VERDURAS:\n");
    imprimirResumen(&frutaVerdura);

    printf("\n========================================\n\n");

    return EXIT_SUCCESS;
}