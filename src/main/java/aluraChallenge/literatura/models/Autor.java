package aluraChallenge.literatura.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "autores")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String nombre;
    
    private Integer fechaNacimiento;
    private Integer fechaMuerte;
    
    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Libro> libros;
    
    // Constructores
    public Autor() {}
    
    public Autor(String nombre, Integer fechaNacimiento, Integer fechaMuerte) {
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaMuerte = fechaMuerte;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public Integer getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(Integer fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    
    public Integer getFechaMuerte() { return fechaMuerte; }
    public void setFechaMuerte(Integer fechaMuerte) { this.fechaMuerte = fechaMuerte; }
    
    public List<Libro> getLibros() { return libros; }
    public void setLibros(List<Libro> libros) { this.libros = libros; }
    
    // Método para verificar si estaba vivo en un año específico
    public boolean estabaVivoEn(int año) {
        boolean nacioAntes = fechaNacimiento == null || fechaNacimiento <= año;
        boolean murioDepues = fechaMuerte == null || fechaMuerte >= año;
        return nacioAntes && murioDepues;
    }
    
    @Override
    public String toString() {
        return String.format("Autor: %s (%s - %s)", 
            nombre, 
            fechaNacimiento != null ? fechaNacimiento : "?",
            fechaMuerte != null ? fechaMuerte.toString() : "presente"
        );
    }
}
