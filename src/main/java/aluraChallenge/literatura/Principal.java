package aluraChallenge.literatura;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import aluraChallenge.literatura.dto.DatosLibro;
import aluraChallenge.literatura.dto.ResultadoBusqueda;
import aluraChallenge.literatura.models.Autor;
import aluraChallenge.literatura.models.Libro;
import aluraChallenge.literatura.repository.AutorRepository;
import aluraChallenge.literatura.repository.LibroRepository;
import aluraChallenge.literatura.service.ConsumoApi;

@Component
public class Principal {
    
    private final Scanner teclado = new Scanner(System.in);
    
    @Autowired
    private ConsumoApi consumoApi;
    
    @Autowired
    private LibroRepository libroRepository;
    
    @Autowired
    private AutorRepository autorRepository;
    
    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    
                    ╔══════════════════════════════════════════╗
                    ║            📚 LITERALURA 📚              ║
                    ╠══════════════════════════════════════════╣
                    ║ Elija la opción a través de su número:   ║
                    ║                                          ║
                    ║ 1 - Buscar libro por título             ║
                    ║ 2 - Listar libros registrados           ║
                    ║ 3 - Listar autores registrados          ║
                    ║ 4 - Listar autores vivos en un año      ║
                    ║ 5 - Listar libros por idioma            ║
                    ║ 6 - Top 10 libros más descargados       ║
                    ║ 7 - Buscar autor por nombre             ║
                    ║ 8 - Estadísticas de la biblioteca       ║
                    ║ 9 - Buscar libros por tema              ║
                    ║ 10- Explorar libros populares           ║
                    ║ 0 - Salir                               ║
                    ╚══════════════════════════════════════════╝
                    """;
            System.out.println(menu);
            System.out.print("👉 Opción: ");
            
            try {
                opcion = teclado.nextInt();
                teclado.nextLine(); // Limpiar buffer
                
                switch (opcion) {
                    case 1 -> buscarLibroPorTitulo();
                    case 2 -> listarLibrosRegistrados();
                    case 3 -> listarAutoresRegistrados();
                    case 4 -> listarAutoresVivosEnAño();
                    case 5 -> listarLibrosPorIdioma();
                    case 6 -> top10LibrosMasDescargados();
                    case 7 -> buscarAutorPorNombre();
                    case 8 -> mostrarEstadisticas();
                    case 9 -> buscarLibrosPorTema();
                    case 10 -> explorarLibrosPopulares();
                    case 0 -> salir();
                    default -> System.out.println("❌ Opción inválida. Por favor, elija un número del 0 al 10.");
                }
            } catch (InputMismatchException e) {
                System.out.println("❌ Por favor, ingrese un número válido.");
                teclado.nextLine(); // Limpiar buffer
            }
        }
    }
    
    private void salir() {
        System.out.println("\n👋 ¡Gracias por usar LiterAlura! ¡Hasta pronto!");
        System.exit(0);
    }

    private void buscarLibroPorTitulo() {
        System.out.print("\n📖 Escribe el nombre del libro que deseas buscar: ");
        var tituloLibro = teclado.nextLine();
        
        if (tituloLibro.trim().isEmpty()) {
            System.out.println("❌ El título no puede estar vacío.");
            return;
        }
        
        try {
            System.out.println("🔍 Buscando en la biblioteca de Gutendx...");
            
            var json = consumoApi.buscarLibrosPorTitulo(tituloLibro);
            var datosBusqueda = consumoApi.convertirDatos(json, ResultadoBusqueda.class);
            
            if (datosBusqueda.libros().isEmpty()) {
                System.out.println("😔 Lo siento, no se encontró el libro: " + tituloLibro);
                return;
            }
            
            var datosLibro = datosBusqueda.libros().get(0);
            
            // Verificar si el libro ya existe
            Optional<Libro> libroExistente = libroRepository.findByTituloContainsIgnoreCase(datosLibro.titulo());
            if (libroExistente.isPresent()) {
                System.out.println("📚 ¡Este libro ya está en tu biblioteca!");
                System.out.println(libroExistente.get());
                return;
            }
            
            // Crear/buscar autor
            Autor autor = null;
            if (!datosLibro.autores().isEmpty()) {
                var datosAutor = datosLibro.autores().get(0);
                Optional<Autor> autorExistente = autorRepository.findByNombreContainsIgnoreCase(datosAutor.nombre());
                
                if (autorExistente.isPresent()) {
                    autor = autorExistente.get();
                } else {
                    autor = new Autor(datosAutor.nombre(), 
                            datosAutor.fechaNacimiento(), 
                            datosAutor.fechaMuerte());
                    autor = autorRepository.save(autor);
                    System.out.println("✨ Nuevo autor agregado: " + autor.getNombre());
                }
            }
            
            // Crear libro
            String idioma = datosLibro.idiomas().isEmpty() ? "desconocido" : datosLibro.idiomas().get(0);
            Integer descargas = datosLibro.numeroDescargas() != null ? datosLibro.numeroDescargas() : 0;
            
            Libro libro = new Libro(datosLibro.titulo(), autor, idioma, descargas);
            libroRepository.save(libro);
            
            System.out.println("\n🎉 ¡Libro agregado exitosamente a tu biblioteca!");
            System.out.println(libro);
            
        } catch (Exception e) {
            System.out.println("❌ Error al buscar el libro: " + e.getMessage());
        }
    }
    
    private void listarLibrosRegistrados() {
        List<Libro> libros = libroRepository.findAll();
        
        if (libros.isEmpty()) {
            System.out.println("\n📚 Tu biblioteca está vacía. ¡Busca algunos libros!");
            return;
        }
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           📚 TU BIBLIOTECA 📚          ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.printf("Total de libros: %d%n%n", libros.size());
        
        libros.forEach(libro -> {
            System.out.println(libro);
            System.out.println("─".repeat(50));
        });
    }
    
    private void listarAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();
        
        if (autores.isEmpty()) {
            System.out.println("\n👤 No hay autores registrados en tu biblioteca.");
            return;
        }
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          👥 AUTORES REGISTRADOS 👥     ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.printf("Total de autores: %d%n%n", autores.size());
        
        autores.forEach(autor -> {
            System.out.println(autor);
            if (!autor.getLibros().isEmpty()) {
                System.out.println("📖 Libros en tu biblioteca:");
                autor.getLibros().forEach(libro -> 
                    System.out.println("   • " + libro.getTitulo()));
            }
            System.out.println("─".repeat(50));
        });
    }
    
    private void listarAutoresVivosEnAño() {
        System.out.print("\n📅 Ingrese el año para buscar autores vivos: ");
        
        try {
            var año = teclado.nextInt();
            teclado.nextLine();
            
            if (año < 0 || año > 2024) {
                System.out.println("❌ Por favor ingrese un año válido.");
                return;
            }
            
            List<Autor> autoresVivos = autorRepository.findAutoresVivosEnAño(año);
            
            if (autoresVivos.isEmpty()) {
                System.out.printf("😔 No se encontraron autores vivos en el año %d en tu biblioteca.%n", año);
                return;
            }
            
            System.out.printf("%n🌟 Autores vivos en %d:%n", año);
            System.out.println("═".repeat(40));
            
            autoresVivos.forEach(autor -> {
                System.out.println(autor);
                System.out.println("─".repeat(30));
            });
            
        } catch (InputMismatchException e) {
            System.out.println("❌ Por favor, ingrese un año válido (número entero).");
            teclado.nextLine();
        }
    }
    
    private void listarLibrosPorIdioma() {
        var menuIdioma = """
                
                🌍 Seleccione el idioma:
                ┌─────────────────────────┐
                │ es - Español            │
                │ en - Inglés             │
                │ fr - Francés            │
                │ pt - Portugués          │
                │ de - Alemán             │
                │ it - Italiano           │
                └─────────────────────────┘
                👉 Ingrese el código del idioma: """;
        
        System.out.print(menuIdioma);
        var idioma = teclado.nextLine().trim().toLowerCase();
        
        if (idioma.isEmpty()) {
            System.out.println("❌ Código de idioma no puede estar vacío.");
            return;
        }
        
        List<Libro> librosPorIdioma = libroRepository.findByIdioma(idioma);
        
        if (librosPorIdioma.isEmpty()) {
            System.out.printf("😔 No se encontraron libros en idioma '%s' en tu biblioteca.%n", idioma.toUpperCase());
            return;
        }
        
        String nombreIdioma = obtenerNombreIdioma(idioma);
        System.out.printf("%n📚 Libros en %s (%d encontrados):%n", nombreIdioma, librosPorIdioma.size());
        System.out.println("═".repeat(50));
        
        librosPorIdioma.forEach(libro -> {
            System.out.println(libro);
            System.out.println("─".repeat(40));
        });
    }
    
    private String obtenerNombreIdioma(String codigo) {
        return switch (codigo.toLowerCase()) {
            case "es" -> "Español";
            case "en" -> "Inglés";
            case "fr" -> "Francés";
            case "pt" -> "Portugués";
            case "de" -> "Alemán";
            case "it" -> "Italiano";
            default -> codigo.toUpperCase();
        };
    }
    
    private void top10LibrosMasDescargados() {
        List<Libro> libros = libroRepository.findAllOrderByNumeroDescargasDesc();
        
        if (libros.isEmpty()) {
            System.out.println("\n📚 No hay libros registrados para mostrar estadísticas.");
            return;
        }
        
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       🏆 TOP 10 MÁS DESCARGADOS 🏆      ║");
        System.out.println("╚══════════════════════════════════════════╝");
        
        libros.stream()
                .limit(10)
                .forEach(libro -> {
                    System.out.printf("📖 %s%n", libro.getTitulo());
                    System.out.printf("   👤 %s%n", libro.getAutor().getNombre());
                    System.out.printf("   📥 %,d descargas%n", libro.getNumeroDescargas());
                    System.out.println("   " + "─".repeat(40));
                });
    }
    
    private void buscarAutorPorNombre() {
        System.out.print("\n👤 Ingrese el nombre del autor a buscar: ");
        var nombreAutor = teclado.nextLine().trim();
        
        if (nombreAutor.isEmpty()) {
            System.out.println("❌ El nombre del autor no puede estar vacío.");
            return;
        }
        
        List<Autor> autores = autorRepository.findByNombreContaining(nombreAutor);
        
        if (autores.isEmpty()) {
            System.out.printf("😔 No se encontró ningún autor con el nombre '%s'.%n", nombreAutor);
            return;
        }
        
        System.out.printf("%n🔍 Autores encontrados (%d):%n", autores.size());
        System.out.println("═".repeat(40));
        
        autores.forEach(autor -> {
            System.out.println(autor);
            if (!autor.getLibros().isEmpty()) {
                System.out.println("📚 Sus libros en tu biblioteca:");
                autor.getLibros().forEach(libro -> 
                    System.out.printf("   • %s (%,d descargas)%n", 
                        libro.getTitulo(), libro.getNumeroDescargas()));
            }
            System.out.println("─".repeat(40));
        });
    }
    
    private void mostrarEstadisticas() {
        List<Libro> libros = libroRepository.findAll();
        List<Autor> autores = autorRepository.findAll();
        
        if (libros.isEmpty()) {
            System.out.println("\n📊 No hay datos suficientes para mostrar estadísticas.");
            return;
        }
        
        // Calcular estadísticas
        DoubleSummaryStatistics statsDescargas = libros.stream()
                .mapToDouble(Libro::getNumeroDescargas)
                .summaryStatistics();
        
        Map<String, Long> librosPorIdioma = libros.stream()
                .collect(Collectors.groupingBy(Libro::getIdioma, Collectors.counting()));
        
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║         📊 ESTADÍSTICAS BIBLIOTECA 📊   ║");
        System.out.println("╚══════════════════════════════════════════╝");
        
        System.out.printf("📚 Total de libros: %d%n", libros.size());
        System.out.printf("👥 Total de autores: %d%n", autores.size());
        System.out.println();
        
        System.out.println("📥 Estadísticas de descargas:");
        System.out.printf("   • Promedio: %,.0f descargas%n", statsDescargas.getAverage());
        System.out.printf("   • Máximo: %,.0f descargas%n", statsDescargas.getMax());
        System.out.printf("   • Mínimo: %,.0f descargas%n", statsDescargas.getMin());
        System.out.printf("   • Total: %,.0f descargas%n", statsDescargas.getSum());
        System.out.println();
        
        System.out.println("🌍 Distribución por idiomas:");
        librosPorIdioma.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("   • %s: %d libros%n", 
                    obtenerNombreIdioma(entry.getKey()), entry.getValue()));
        
        // Autor más prolífico
        Optional<Autor> autorMasProlífico = autores.stream()
                .max(Comparator.comparing(a -> a.getLibros().size()));
        
        if (autorMasProlífico.isPresent() && !autorMasProlífico.get().getLibros().isEmpty()) {
            System.out.println();
            System.out.printf("🏆 Autor más prolífico: %s (%d libros)%n", 
                autorMasProlífico.get().getNombre(),
                autorMasProlífico.get().getLibros().size());
        }
    }
    
    // NUEVAS FUNCIONALIDADES
    private void buscarLibrosPorTema() {
        System.out.print("\n🔍 Ingrese el tema que desea buscar (ej: children, fiction, history): ");
        var tema = teclado.nextLine().trim();
        
        if (tema.isEmpty()) {
            System.out.println("❌ El tema no puede estar vacío.");
            return;
        }
        
        try {
            System.out.println("🔍 Buscando libros sobre: " + tema + "...");
            
            var json = consumoApi.buscarLibrosPorTema(tema);
            var datosBusqueda = consumoApi.convertirDatos(json, ResultadoBusqueda.class);
            
            if (datosBusqueda.libros().isEmpty()) {
                System.out.printf("😔 No se encontraron libros sobre el tema '%s'.%n", tema);
                return;
            }
            
            System.out.printf("%n📚 Libros encontrados sobre '%s' (%d resultados):%n", tema, datosBusqueda.libros().size());
            System.out.println("═".repeat(60));
            
            // Mostrar los primeros 10 resultados
            datosBusqueda.libros().stream()
                    .limit(10)
                    .forEach(libro -> {
                        System.out.printf("📖 %s%n", libro.titulo());
                        if (!libro.autores().isEmpty()) {
                            System.out.printf("   👤 %s%n", libro.autores().get(0).nombre());
                        }
                        System.out.printf("   📥 %,d descargas%n", libro.numeroDescargas());
                        System.out.println("   " + "─".repeat(50));
                        
                        // Preguntar si quiere guardar el libro
                        System.out.print("   ¿Desea agregar este libro a su biblioteca? (s/n): ");
                        String respuesta = teclado.nextLine().trim().toLowerCase();
                        if (respuesta.equals("s") || respuesta.equals("si")) {
                            guardarLibroDesdeAPI(libro);
                        }
                        System.out.println();
                    });
            
        } catch (Exception e) {
            System.out.println("❌ Error al buscar libros por tema: " + e.getMessage());
        }
    }
    
    private void explorarLibrosPopulares() {
        try {
            System.out.println("🔍 Explorando los libros más populares de Gutendx...");
            
            var json = consumoApi.obtenerLibrosMasPopulares(20);
            var datosBusqueda = consumoApi.convertirDatos(json, ResultadoBusqueda.class);
            
            if (datosBusqueda.libros().isEmpty()) {
                System.out.println("😔 No se pudieron obtener los libros populares.");
                return;
            }
            
            System.out.printf("%n🌟 Los libros más populares de Project Gutenberg:%n");
            System.out.println("═".repeat(60));
            
            datosBusqueda.libros().stream()
                    .limit(15)
                    .forEach(libro -> {
                        System.out.printf("📖 %s%n", libro.titulo());
                        if (!libro.autores().isEmpty()) {
                            System.out.printf("   👤 %s%n", libro.autores().get(0).nombre());
                        }
                        System.out.printf("   🌍 %s%n", !libro.idiomas().isEmpty() ? 
                            obtenerNombreIdioma(libro.idiomas().get(0)) : "Desconocido");
                        System.out.printf("   📥 %,d descargas%n", libro.numeroDescargas());
                        
                        // Verificar si ya está en la biblioteca
                        Optional<Libro> libroExistente = libroRepository.findByTituloContainsIgnoreCase(libro.titulo());
                        if (libroExistente.isPresent()) {
                            System.out.println("   ✅ Ya está en tu biblioteca");
                        } else {
                            System.out.print("   ¿Agregar a tu biblioteca? (s/n): ");
                            String respuesta = teclado.nextLine().trim().toLowerCase();
                            if (respuesta.equals("s") || respuesta.equals("si")) {
                                guardarLibroDesdeAPI(libro);
                            }
                        }
                        System.out.println("   " + "─".repeat(50));
                    });
            
        } catch (Exception e) {
            System.out.println("❌ Error al obtener libros populares: " + e.getMessage());
        }
    }
    
    private void guardarLibroDesdeAPI(DatosLibro datosLibro) {
        try {
            // Verificar si ya existe
            Optional<Libro> libroExistente = libroRepository.findByTituloContainsIgnoreCase(datosLibro.titulo());
            if (libroExistente.isPresent()) {
                System.out.println("   ⚠️  El libro ya está en tu biblioteca.");
                return;
            }
            
            // Crear/buscar autor
            Autor autor = null;
            if (!datosLibro.autores().isEmpty()) {
                var datosAutor = datosLibro.autores().get(0);
                Optional<Autor> autorExistente = autorRepository.findByNombreContainsIgnoreCase(datosAutor.nombre());
                
                if (autorExistente.isPresent()) {
                    autor = autorExistente.get();
                } else {
                    autor = new Autor(datosAutor.nombre(), 
                            datosAutor.fechaNacimiento(), 
                            datosAutor.fechaMuerte());
                    autor = autorRepository.save(autor);
                }
            }
            
            // Crear libro
            String idioma = datosLibro.idiomas().isEmpty() ? "desconocido" : datosLibro.idiomas().get(0);
            Integer descargas = datosLibro.numeroDescargas() != null ? datosLibro.numeroDescargas() : 0;
            
            Libro libro = new Libro(datosLibro.titulo(), autor, idioma, descargas);
            libroRepository.save(libro);
            
            System.out.println("   ✅ ¡Libro agregado exitosamente!");
            
        } catch (Exception e) {
            System.out.println("   ❌ Error al guardar el libro: " + e.getMessage());
        }
    }
}