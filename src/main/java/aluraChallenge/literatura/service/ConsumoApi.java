package aluraChallenge.literatura.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ConsumoApi {
    
    private static final Logger logger = LoggerFactory.getLogger(ConsumoApi.class);
    
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String URL_BASE = "https://gutendex.com/books/";
    
    public String obtenerDatos(String url) {
        logger.info("Realizando petición a: {}", url);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "LiterAlura/1.0")
                .header("Accept", "application/json")
                .GET()
                .build();
        
        try {
            HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            logger.info("Código de respuesta: {}", response.statusCode());
            
            if (response.statusCode() == 200) {
                logger.debug("Respuesta exitosa, longitud: {}", response.body().length());
                return response.body();
            } else {
                String errorMsg = String.format("Error HTTP %d: %s", 
                    response.statusCode(), response.body());
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
        } catch (IOException e) {
            String errorMsg = "Error de conexión: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMsg = "Petición interrumpida: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error inesperado: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    public <T> T convertirDatos(String json, Class<T> clase) {
        try {
            logger.debug("Convirtiendo JSON a clase: {}", clase.getSimpleName());
            logger.debug("JSON a convertir (primeros 200 chars): {}", 
                json.length() > 200 ? json.substring(0, 200) + "..." : json);
            
            if (json == null || json.trim().isEmpty()) {
                throw new RuntimeException("JSON vacío o nulo");
            }
            
            return objectMapper.readValue(json, clase);
            
        } catch (JsonProcessingException e) {
            String errorMsg = String.format("Error al convertir JSON a %s: %s", 
                clase.getSimpleName(), e.getMessage());
            logger.error(errorMsg, e);
            logger.error("JSON problemático: {}", json);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    // Métodos específicos para diferentes tipos de búsqueda
    public String buscarLibrosPorTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        
        String tituloEncoded = URLEncoder.encode(titulo.trim(), StandardCharsets.UTF_8);
        String url = URL_BASE + "?search=" + tituloEncoded;
        
        logger.info("Buscando libros por título: '{}'", titulo);
        logger.info("URL de búsqueda: {}", url);
        
        return obtenerDatos(url);
    }
    
    public String buscarLibrosPorAutor(String autor) {
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("El autor no puede estar vacío");
        }
        
        String autorEncoded = URLEncoder.encode(autor.trim(), StandardCharsets.UTF_8);
        String url = URL_BASE + "?search=" + autorEncoded;
        
        logger.info("Buscando libros por autor: '{}'", autor);
        return obtenerDatos(url);
    }
    
    public String buscarLibrosPorIdioma(String idioma) {
        if (idioma == null || idioma.trim().isEmpty()) {
            throw new IllegalArgumentException("El idioma no puede estar vacío");
        }
        
        // Validar formato de idioma (ej: "en", "es", "fr")
        if (!idioma.matches("^[a-z]{2}$")) {
            throw new IllegalArgumentException("El idioma debe ser un código de 2 letras (ej: 'es', 'en')");
        }
        
        String url = URL_BASE + "?languages=" + idioma.toLowerCase();
        logger.info("Buscando libros por idioma: '{}'", idioma);
        
        return obtenerDatos(url);
    }
    
    public String buscarLibrosPorAñoAutor(int añoInicio, int añoFin) {
        if (añoInicio > añoFin) {
            throw new IllegalArgumentException("El año de inicio no puede ser mayor al año de fin");
        }
        
        if (añoInicio < -3000 || añoFin > 2024) {
            throw new IllegalArgumentException("Rango de años inválido");
        }
        
        String url = URL_BASE + "?author_year_start=" + añoInicio + "&author_year_end=" + añoFin;
        logger.info("Buscando libros por rango de años del autor: {} - {}", añoInicio, añoFin);
        
        return obtenerDatos(url);
    }
    
    public String obtenerLibrosMasPopulares(int limite) {
        if (limite <= 0) {
            throw new IllegalArgumentException("El límite debe ser mayor a 0");
        }
        
        // La API de Gutendex usa paginación, no límite directo
        String url = URL_BASE + "?sort=popular";
        logger.info("Obteniendo libros más populares");
        
        return obtenerDatos(url);
    }
    
    public String buscarLibrosPorTema(String tema) {
        if (tema == null || tema.trim().isEmpty()) {
            throw new IllegalArgumentException("El tema no puede estar vacío");
        }
        
        String temaEncoded = URLEncoder.encode(tema.trim(), StandardCharsets.UTF_8);
        String url = URL_BASE + "?topic=" + temaEncoded;
        
        logger.info("Buscando libros por tema: '{}'", tema);
        return obtenerDatos(url);
    }
    
    // Método adicional para testing/debugging
    public String testConexion() {
        logger.info("Probando conexión con la API");
        return obtenerDatos(URL_BASE);
    }
}