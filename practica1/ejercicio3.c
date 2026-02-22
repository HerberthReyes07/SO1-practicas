#include <stdio.h>     // Proporciona funciones de entrada/salida estándar (printf, scanf, etc.)
#include <unistd.h>    // Proporciona acceso a llamadas al sistema POSIX (fork, getpid, sleep, execvp, access, etc.)
#include <sys/types.h> // Define tipos de datos utilizados por el sistema (pid_t, size_t, etc.)
#include <sys/wait.h>  // Proporciona el uso de funciones y macros para esperar procesos hijos (wait, WIFEXITED, etc.)
#include <stdlib.h>    // Proporciona funciones de utilidad general (malloc, free, exit, EXIT_SUCCESS, EXIT_FAILURE, etc.)

int main()
{
    pid_t pid;
    int status;

    printf("\n============================================\n");
    printf("   EJERCICIO 3 - VERIFICACION DE DIRECTORIOS\n");
    printf("============================================\n\n");

    char directorio[256]; // Buffer para almacenar la ruta del directorio
    printf("Ingrese la ruta del directorio a analizar: ");
    scanf("%255s", directorio); // Limita la entrada a 255 caracteres para evitar desbordamiento

    printf("\n--- CREANDO PROCESO HIJO ---\n");

    // Crear proceso hijo mediante fork()
    pid = fork();

    if (pid < 0)
    {
        // Error al crear el proceso hijo
        perror("[ERROR] No se pudo crear el proceso hijo");
        return EXIT_FAILURE;
    }
    else if (pid == 0)
    {
        // ===== CODIGO DEL PROCESO HIJO =====
        printf("\n[HIJO] PID: %d | Padre PID: %d\n", getpid(), getppid());
        printf("[HIJO] Verificando existencia del directorio...\n");

        // Verifica si el directorio existe usando access() con el flag F_OK
        if (access(directorio, F_OK) != 0)
        {
            printf("\n[ERROR] El directorio '%s' no existe o no es accesible.\n\n", directorio);
            exit(EXIT_FAILURE); // Termina el hijo con código de error
        }

        printf("[HIJO] Directorio encontrado. Listando contenido...\n\n");
        printf("========================================\n");
        printf("   CONTENIDO DEL DIRECTORIO\n");
        printf("========================================\n");

        // Ejecuta el comando 'ls -l' para listar el contenido del directorio
        // execvp() reemplaza la imagen del proceso actual con el nuevo programa
        char *args[] = {"ls", "-l", directorio, NULL};
        execvp("ls", args);

        // Si execvp() falla, se ejecuta el código siguiente
        perror("[ERROR] execvp fallo");
        exit(EXIT_FAILURE);
    }
    else
    {
        // ===== CODIGO DEL PROCESO PADRE =====
        printf("\n[PADRE] PID: %d\n", getpid());
        printf("[PADRE] Esperando a que el hijo (PID: %d) termine...\n", pid);

        // Espera a que el proceso hijo termine y obtiene su estado
        pid_t hijo_terminado = wait(&status);

        printf("\n========================================\n");
        printf("   RESULTADO DEL PROCESO HIJO\n");
        printf("========================================\n");
        printf("[PADRE] El hijo (PID: %d) ha finalizado.\n", hijo_terminado);

        // Verifica si el hijo terminó normalmente
        if (WIFEXITED(status))
        {
            int codigo_salida = WEXITSTATUS(status); // Obtiene el código de salida del hijo

            if (codigo_salida == EXIT_SUCCESS)
            {
                printf("[PADRE] Estado: Termino correctamente (EXIT_SUCCESS)\n");
                printf("[PADRE] Interpretacion: El directorio existe y se listo correctamente.\n");
            }
            else if (codigo_salida == EXIT_FAILURE)
            {
                printf("[PADRE] Estado: Termino con error (EXIT_FAILURE)\n");
                printf("[PADRE] Interpretacion: El directorio no existe o hubo un error.\n");
            }
            else
            {
                printf("[PADRE] Estado: Termino con codigo personalizado: %d\n", codigo_salida);
            }
        }
        else
        {
            printf("[PADRE] Estado: Termino anormalmente (senal o interrupcion)\n");
        }

        printf("========================================\n");
        printf("\n[PADRE] Finalizando proceso padre.\n");
        printf("========================================\n\n");
    }

    return EXIT_SUCCESS;
}