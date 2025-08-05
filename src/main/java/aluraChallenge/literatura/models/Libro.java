package aluraChallenge.literatura.models;

import jakarta.persistence.*;

@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String titulo;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "autor_id")
    private Autor autor;
    
    private String idioma;
    private Integer numeroDescargas;
    
    // Constructores
    public Libro() {}
    
    public Libro(String titulo, Autor autor, String idioma, Integer numeroDescargas) {
        this.titulo = titulo;
        this.autor = autor;
        this.idioma = idioma;
        this.numeroDescargas = numeroDescargas;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public Autor getAutor() { return autor; }
    public void setAutor(Autor autor) { this.autor = autor; }
    
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    
    public Integer getNumeroDescargas() { return numeroDescargas; }
    public void setNumeroDescargas(Integer numeroDescargas) { this.numeroDescargas = numeroDescargas; }
    
    @Override
    public String toString() {
        return String.format("""
            ***** LIBRO *****
            Título: %s
            Autor: %s
            Idioma: %s
            Número de descargas: %d
            ******************""", 
            titulo, 
            autor != null ? autor.getNombre() : "Desconocido", 
            idioma, 
            numeroDescargas != null ? numeroDescargas : 0);
    }
}