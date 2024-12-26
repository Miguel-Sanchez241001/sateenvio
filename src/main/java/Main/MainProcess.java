package Main;

import Utils.UtilsDatos;
import model.InputParametros;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;

public class MainProcess {
    private static final Logger log = Logger.getLogger(MainProcess.class);

    public static void main(String... args) {
         if (args.length < 4) {
            log.error("Número insuficiente de argumentos. Se requieren 4 argumentos: " +
                    "<typeProcess> <urlConection> <pathFile> <procedure>  ");
            System.out.println("FAILED");
            System.exit(1);
        }

        // Capturar argumentos
        InputParametros inputParameters = new InputParametros();
        inputParameters.setTypeProcess(args[0]);
        inputParameters.setUrlConection(args[1]);
        inputParameters.setPathFile(args[2]);
        inputParameters.setProcedureName(args[3]);

        // Extraer la información de la URL de conexión usando regex
        String urlConnection = inputParameters.getUrlConection();
        String jdbcUrl = UtilsDatos.extractJdbcUrl(urlConnection);
        String username = UtilsDatos.extractUsername(urlConnection);
        String password = UtilsDatos.extractPassword(urlConnection);

        log.info("Iniciando ejecución con los siguientes parámetros:");
        log.info("Tipo de proceso: " + inputParameters.getTypeProcess());
        log.info("Nombre del procedimiento: " + inputParameters.getProcedureName());
        if ("2".equals(inputParameters.getTypeProcess())) {
            log.info("Archivo de salida: " + inputParameters.getPathFile());
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Habilitar DBMS_OUTPUT
            try (CallableStatement enableStmt = connection.prepareCall("{call dbms_output.enable(1000000)}")) {
                enableStmt.execute();
            }

            // Llamar al procedimiento almacenado
            try (CallableStatement procedureStmt = connection.prepareCall("{call " + inputParameters.getProcedureName() + " }")) {
                procedureStmt.execute();
                log.info("Procedimiento ejecutado correctamente: " + inputParameters.getProcedureName());
            }

            // Verificar el typeProcess
            if ("2".equals(inputParameters.getTypeProcess())) {
                // Si el typeProcess es 2, capturar la salida de DBMS_OUTPUT y escribirla a un archivo
                StringBuilder output = new StringBuilder();
                try (CallableStatement getOutputStmt = connection.prepareCall("{call dbms_output.get_line(?, ?)}")) {
                    getOutputStmt.registerOutParameter(1, Types.VARCHAR); // Mensaje de salida
                    getOutputStmt.registerOutParameter(2, Types.INTEGER); // Estado (0 si hay línea, 1 si no hay más líneas)

                    while (true) {
                        getOutputStmt.execute();
                        String line = getOutputStmt.getString(1);
                        int status = getOutputStmt.getInt(2);
                        if (status == 1) break; // No hay más líneas
                        output.append(line).append(System.lineSeparator());
                    }
                }

                File file = new File(inputParameters.getPathFile());
                if (!file.exists()){
                    FileUtils.touch(file);
                    FileUtils.writeStringToFile(file, output.toString(), "UTF-8");
                }else{
                    String data =  FileUtils.readFileToString(file,"UTF-8");
                    StringBuilder output2 = new StringBuilder();
                    output2.append(data);
                    output2.append(output).append(System.lineSeparator());
                    FileUtils.writeStringToFile(file, output2.toString(), "UTF-8");
                }

                log.info("Salida de DBMS_OUTPUT guardada en el archivo: " + inputParameters.getPathFile());
            } else {
                // Si el typeProcess no es 2, solo ejecutar el procedimiento
                log.info("Procedimiento ejecutado sin capturar salida (tipo de proceso 1).");
            }

            System.out.println("OK");
            System.exit(0);

        } catch (SQLException e) {
            log.error("Error al ejecutar el procedimiento almacenado: ", e);
            System.out.println("FAILED");
            System.exit(1);
        } catch (IOException e) {
            log.error("Error al escribir la salida en el archivo: ", e);
            System.out.println("FAILED");
            System.exit(1);
        }
    }
}
