package aluraChallenge.literatura.repository;

import aluraChallenge.literatura.models.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    Optional<Autor> findByNombreContainsIgnoreCase(String nombre);
    
    @Query("SELECT a FROM Autor a WHERE " +
           "(a.fechaNacimiento IS NULL OR a.fechaNacimiento <= :año) AND " +
           "(a.fechaMuerte IS NULL OR a.fechaMuerte >= :año)")
    List<Autor> findAutoresVivosEnAño(@Param("año") int año);
    
    List<Autor> findByNombreContaining(String nombre);
}