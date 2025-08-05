package aluraChallenge.literatura;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiteraturaApplication implements CommandLineRunner {

    @Autowired
    private Principal principal;

    public static void main(String[] args) {
        SpringApplication.run(LiteraturaApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("""
            ╔════════════════════════════════════════════════════╗
            ║                                                    ║
            ║      ¡Bienvenido a LITERALURA!                   ║
            ║                                                    ║
            ║    Tu catálogo personal de libros                  ║
            ║    Powered by Gutendx API                          ║
            ║                                                    ║
            ╚════════════════════════════════════════════════════╝
            """);
        
        principal.muestraElMenu();
    }
}
