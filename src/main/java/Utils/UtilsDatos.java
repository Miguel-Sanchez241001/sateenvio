package Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilsDatos {


    public static String extractUsername(String urlConnection) {
        String regex = "^([^/]+)/";
        Matcher matcher = Pattern.compile(regex).matcher(urlConnection);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Nombre de usuario no encontrado en la URL de conexión");
    }

    public static String extractPassword(String urlConnection) {
        String regex = "/([^@]+)";
        Matcher matcher = Pattern.compile(regex).matcher(urlConnection);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Contraseña no encontrada en la URL de conexión");
    }

    // Método para extraer la URL de conexión para JDBC
    public static String extractJdbcUrl(String urlConnection) {
        String regex = "@//(.+)";
        Matcher matcher = Pattern.compile(regex).matcher(urlConnection);
        if (matcher.find()) {
            return "jdbc:oracle:thin:@" + matcher.group(1);
        }
        throw new IllegalArgumentException("URL de conexión no válida");
    }
    
    
    
    
    
    
    
}
