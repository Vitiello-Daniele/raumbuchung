package de.iu.raumbuchung.service;

import de.iu.raumbuchung.entity.Location;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class LocationService {

    @PersistenceContext(unitName = "roomPU")
    private EntityManager em;

    public List<Location> findAll() {
        return em.createQuery(
                "SELECT l FROM Location l ORDER BY l.name",
                Location.class
        ).getResultList();
    }

    public void create(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        // doppelte vermeiden
        Long count = em.createQuery(
                        "SELECT COUNT(l) FROM Location l WHERE LOWER(l.name) = LOWER(:n)",
                        Long.class)
                .setParameter("n", trimmed)
                .getSingleResult();
        if (count > 0) {
            return;
        }

        em.persist(new Location(trimmed));
    }

    public Location findById(Long id) {
        return em.find(Location.class, id);
    }

    public void delete(Long id) {
        Location loc = em.find(Location.class, id);
        if (loc != null) {
            em.remove(loc);
        }
    }
}
