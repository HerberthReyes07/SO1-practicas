#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>

typedef struct
{
	int id;					   // Identificador de la sucursal (1 o 2)
	int *turno;				   // Variable compartida que indica a quién le toca el turno
	int enMantenimiento;	   // Indica si la sucursal está en mantenimiento
	volatile int *detener;	   // Variable de control para detener la simulación
	int solicitudesRealizadas; // Contador de solicitudes realizadas
	int siempreInactivo;	   // Indica si la sucursal nunca realiza solicitudes (escenario 2)
} Sucursal;

const int NUMERO_ITERACIONES = 5;

// Función para pausar la ejecución en milisegundos
void pausa(int ms)
{
	usleep(ms * 1000);
}

// Función principal que ejecuta cada hilo (sucursal)
void *operarSucursal(void *arg)
{
	Sucursal *sucursal = (Sucursal *)arg;
	int otro = (sucursal->id == 1) ? 2 : 1;

	for (int i = 0; i < NUMERO_ITERACIONES; i++)
	{
		// Espera activa por turno
		while (*(sucursal->turno) != sucursal->id)
		{
			// Permite salir del bucle si el sistema se detiene
			if (*(sucursal->detener) == 1)
			{
				printf("Sucursal %d deja de esperar (sistema detenido)\n", sucursal->id);
				return NULL;
			}

			printf("Sucursal %d esperando turno...\n", sucursal->id);
			pausa(200);
		}

		printf("\n[Sucursal %d] ES SU TURNO\n", sucursal->id);

		// Caso especial: sucursal en mantenimiento
		if (sucursal->enMantenimiento)
		{
			printf("Sucursal %d en mantenimiento... NO CEDE TURNO\n", sucursal->id);

			// Se queda bloqueando el turno hasta que el programa finalice
			while (*(sucursal->detener) == 0)
			{
				pausa(200);
			}
			return NULL;
		}

		// Escenario 2: sucursal nunca solicita
		if (sucursal->siempreInactivo)
		{
			printf("Sucursal %d no necesita productos\n", sucursal->id);
		}
		else
		{
			// Simula si la sucursal realiza o no una solicitud
			int decision = rand() % 2;

			if (decision == 1)
			{
				printf("Sucursal %d realiza solicitud a bodega\n", sucursal->id);
				sucursal->solicitudesRealizadas++;
			}
			else
			{
				printf("Sucursal %d no necesita productos\n", sucursal->id);
			}
		}

		// Cede turno a la otra sucursal
		printf("Sucursal %d cede turno a Sucursal %d\n", sucursal->id, otro);
		*(sucursal->turno) = otro;

		pausa(300);
	}

	return NULL;
}

// Función para validar entrada de opción en el menú
int leerOpcion(int min, int max)
{
	int opcion;
	do
	{
		printf("Seleccione una opcion (%d-%d): ", min, max);
		scanf("%d", &opcion);
	} while (opcion < min || opcion > max);
	return opcion;
}

// ESCENARIO 1: Funcionamiento normal (alternancia correcta)
void escenarioNormal()
{
	printf("\n============================================================\n");
	printf(" ESCENARIO 1 - OPERACION NORMAL\n");
	printf("============================================================\n\n");

	int turno = 1;
	volatile int detener = 0;

	// Ambas sucursales realizan solicitudes y ceden turno correctamente
	Sucursal s1 = {1, &turno, 0, &detener, 0, 0};
	Sucursal s2 = {2, &turno, 0, &detener, 0, 0};

	pthread_t t1, t2;

	pthread_create(&t1, NULL, operarSucursal, &s1);
	pthread_create(&t2, NULL, operarSucursal, &s2);

	pthread_join(t1, NULL);
	pthread_join(t2, NULL);

	printf("\n------------------------------------------------------------\n");
	printf(" RESUMEN DEL ESCENARIO 1\n");
	printf("------------------------------------------------------------\n");
	printf("Sucursal 1 solicitudes: %d\n", s1.solicitudesRealizadas);
	printf("Sucursal 2 solicitudes: %d\n", s2.solicitudesRealizadas);
	printf("Turno final: Sucursal %d\n", turno);

	printf("\n[ANALISIS]\n");
	printf("- Las sucursales alternaron correctamente el turno sin bloqueos.\n");
	printf("- El sistema funciona adecuadamente cuando ambas cooperan.\n");
	printf("- La diferencia en solicitudes se debe a la simulacion aleatoria del comportamiento de cada sucursal.\n");
}

// ESCENARIO 2: Una sucursal no hace solicitudes pero sigue cediendo turno
void escenarioSinSolicitud()
{
	printf("\n============================================================\n");
	printf(" ESCENARIO 2 - UNA SUCURSAL NO REALIZA SOLICITUDES\n");
	printf("============================================================\n\n");

	int turno = 1;
	volatile int detener = 0;

	// Sucursal 1 nunca solicita, pero sigue cediendo el turno
	Sucursal s1 = {1, &turno, 0, &detener, 0, 1};
	Sucursal s2 = {2, &turno, 0, &detener, 0, 0};

	pthread_t t1, t2;

	pthread_create(&t1, NULL, operarSucursal, &s1);
	pthread_create(&t2, NULL, operarSucursal, &s2);

	pthread_join(t1, NULL);
	pthread_join(t2, NULL);

	printf("\n------------------------------------------------------------\n");
	printf(" RESUMEN DEL ESCENARIO 2\n");
	printf("------------------------------------------------------------\n");
	printf("Sucursal 1 solicitudes: %d\n", s1.solicitudesRealizadas);
	printf("Sucursal 2 solicitudes: %d\n", s2.solicitudesRealizadas);
	printf("Turno final: Sucursal %d\n", turno);

	printf("\n[ANALISIS]\n");
	printf("- Sucursal 1 no realizo solicitudes, pero siguio cediendo el turno.\n");
	printf("- El sistema sigue funcionando sin bloqueos.\n");
	printf("- Esto demuestra que el problema no es no solicitar, sino no ceder el turno.\n");
}

// ESCENARIO 3: Una sucursal en mantenimiento no cede turno, causando bloqueo
void escenarioMantenimiento()
{
	printf("\n============================================================\n");
	printf(" ESCENARIO 3 - MANTENIMIENTO (BLOQUEO)\n");
	printf("============================================================\n\n");

	int turno = 1;
	volatile int detener = 0;

	// Sucursal 1 en mantenimiento, no cede turno y bloquea a Sucursal 2
	Sucursal s1 = {1, &turno, 1, &detener, 0, 0};
	Sucursal s2 = {2, &turno, 0, &detener, 0, 0};

	pthread_t t1, t2;

	pthread_create(&t1, NULL, operarSucursal, &s1);
	pthread_create(&t2, NULL, operarSucursal, &s2);

	// Espera un tiempo para observar el bloqueo
	sleep(3);
	detener = 1;

	pthread_join(t1, NULL);
	pthread_join(t2, NULL);

	printf("\n------------------------------------------------------------\n");
	printf(" RESUMEN DEL ESCENARIO 3\n");
	printf("------------------------------------------------------------\n");
	printf("Sucursal 1 solicitudes: %d\n", s1.solicitudesRealizadas);
	printf("Sucursal 2 solicitudes: %d\n", s2.solicitudesRealizadas);
	printf("Turno final: Sucursal %d\n", turno);

	printf("\n[ANALISIS]\n");
	printf("- Sucursal 1 no cedio el turno debido a mantenimiento.\n");
	printf("- Sucursal 2 quedo en espera indefinida.\n");
	printf("- El algoritmo falla porque depende de que ambos procesos cedan el turno,\n");
	printf("  lo cual no siempre ocurre en escenarios reales.\n");
	printf("- Esto evidencia una falla del algoritmo de alternancia estricta.\n");
}

// MENÚ PRINCIPAL
int main()
{
	int opcion;

	do
	{
		printf("\n============================================================\n");
		printf("        MENU PRINCIPAL - SIMULACION DE SUCURSALES\n");
		printf("============================================================\n");
		printf("1. Escenario normal\n");
		printf("2. Una sucursal no solicita\n");
		printf("3. Mantenimiento (bloqueo)\n");
		printf("4. Ejecutar todos\n");
		printf("5. Salir\n");

		opcion = leerOpcion(1, 5);

		switch (opcion)
		{
		case 1:
			escenarioNormal();
			break;
		case 2:
			escenarioSinSolicitud();
			break;
		case 3:
			escenarioMantenimiento();
			break;
		case 4:
			escenarioNormal();
			escenarioSinSolicitud();
			escenarioMantenimiento();
			break;
		}

	} while (opcion != 5);

	return 0;
}