package de.iu.raumbuchung.service;

import de.iu.raumbuchung.entity.Room;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class RoomService {

    @PersistenceContext(unitName = "roomPU")
    private EntityManager em;

    @Inject
    private BookingService bookingService;

    public List<Room> findAll() {
        return em.createQuery("SELECT r FROM Room r ORDER BY r.name", Room.class)
                .getResultList();
    }

    public void save(Room room) {
        if (room.getId() == null) {
            em.persist(room);
        } else {
            em.merge(room);
        }
    }

    /** Einzelnen Raum löschen + zugehörige Buchungen */
    public void delete(Long id) {
        if (id == null) {
            return;
        }

        Room r = em.find(Room.class, id);
        if (r != null) {
            // zuerst alle Buchungen dieses Raums löschen
            bookingService.deleteByRoom(r);

            // dann den Raum selbst löschen
            em.remove(r);
        }
    }

    // alle unterschiedlichen standorte, alphabetisch sortiert
    public List<String> findAllLocations() {
        return em.createQuery(
                "SELECT DISTINCT r.location FROM Room r ORDER BY r.location",
                String.class
        ).getResultList();
    }

    /**
     * Alle Räume eines Standorts löschen.
     * Variante B: Für jeden Raum erst alle Buchungen löschen,
     * dann den Raum selbst.
     */
    public void deleteByLocation(String locationName) {
        if (locationName == null || locationName.trim().isEmpty()) {
            return;
        }

        List<Room> rooms = em.createQuery(
                        "SELECT r FROM Room r WHERE LOWER(r.location) = LOWER(:loc)",
                        Room.class)
                .setParameter("loc", locationName.trim().toLowerCase())
                .getResultList();

        for (Room r : rooms) {
            // Buchungen zu diesem Raum löschen
            bookingService.deleteByRoom(r);

            // sicherstellen, dass die Entity gemanaged ist
            Room managed = em.contains(r) ? r : em.merge(r);
            em.remove(managed);
        }
    }
}
