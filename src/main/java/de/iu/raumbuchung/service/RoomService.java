package de.iu.raumbuchung.service;

import de.iu.raumbuchung.entity.Room;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class RoomService {

    @PersistenceContext(unitName = "roomPU")
    private EntityManager em;

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

    public void delete(Long id) {
        Room r = em.find(Room.class, id);
        if (r != null) {
            em.remove(r);
        }
    }
}
