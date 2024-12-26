#!/bin/bash

#------------------------------------------------------------
# Script básico para probar la ejecución del JAR
#------------------------------------------------------------

# Variables
readonly JAR="sateenvio.jar"        # Cambia esto al nombre de tu JAR
readonly typeProcess="2"            # Tipo de proceso (1 o 2 según corresponda)
readonly urlConnection="bn_sate/bn_sate@//10.7.12.177:1521/orades"  # URL de conexión a Oracle
readonly outputPath="output.txt"  # Ruta al archivo de salida si typeProcess es '2'
readonly procedureName="BN_SATE.BNPD_00_SOLICITUD_GEN_1"  # Nombre del procedimiento a ejecutar
readonly argument="0"                # Argumento adicional requerido por tu JAR

# Moverse al directorio del JAR

# Ejecutar el JAR
OUTPUT=$(java -jar "$JAR" "$typeProcess" "$urlConnection" "$outputPath" "$procedureName" "$argument")
codigoError=$?

# Verificar si hubo un error en la ejecución del JAR
if [ $codigoError -ne 0 ]; then
    echo "Error: Falló la ejecución del JAR. Código de error: $codigoError"
    echo "Mensaje de salida: $OUTPUT"
    exit $codigoError
fi

# Verificar si la salida contiene "OK"
if [[ "$OUTPUT" == *"OK"* ]]; then
    echo "Ejecución del JAR exitosa"
else
    echo "Error: Ejecución del JAR fallida. Mensaje: $OUTPUT"
    exit 1
fi

# Mensaje de éxito
echo "Prueba completada con éxito."
