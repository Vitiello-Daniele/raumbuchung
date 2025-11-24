package de.iu.raumbuchung.service;

import de.iu.raumbuchung.entity.Booking;
import de.iu.raumbuchung.entity.Room;
import de.iu.raumbuchung.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Stateless
public class BookingService {

    @PersistenceContext(unitName = "roomPU")
    private EntityManager em;

    public void createBooking(User user,
                              Long roomId,
                              LocalDate date,
                              LocalTime start,
                              LocalTime end) throws OverlappingBookingException {

        if (start == null || end == null || !start.isBefore(end)) {
            throw new OverlappingBookingException("zeitraum ungültig");
        }

        Room room = em.find(Room.class, roomId);
        if (room == null) {
            throw new OverlappingBookingException("raum nicht gefunden");
        }

        Long count = em.createQuery(
                        "SELECT COUNT(b) FROM Booking b " +
                                "WHERE b.room = :room " +
                                "AND b.date = :date " +
                                "AND b.startTime < :end " +
                                "AND b.endTime > :start",
                        Long.class)
                .setParameter("room", room)
                .setParameter("date", date)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        if (count != null && count > 0) {
            throw new OverlappingBookingException("es gibt schon eine buchung in diesem zeitraum");
        }

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setDate(date);
        booking.setStartTime(start);
        booking.setEndTime(end);

        em.persist(booking);
    }

    public List<Booking> findByUser(User user) {
        return em.createQuery(
                        "SELECT b FROM Booking b " +
                                "WHERE b.user = :user " +
                                "ORDER BY b.date, b.startTime",
                        Booking.class)
                .setParameter("user", user)
                .getResultList();
    }

    /** alle Buchungen für einen Raum an einem Tag (für Timeline) */
    public List<Booking> findBookingsForRoomAndDate(Long roomId, LocalDate date) {
        return em.createQuery(
                        "SELECT b FROM Booking b " +
                                "WHERE b.room.id = :roomId " +
                                "AND b.date = :date " +
                                "ORDER BY b.startTime",
                        Booking.class)
                .setParameter("roomId", roomId)
                .setParameter("date", date)
                .getResultList();
    }

    /** löscht eine Buchung, aber nur, wenn sie dem gegebenen User gehört */
    public void deleteBookingForUser(Long bookingId, User user) {
        Booking booking = em.createQuery(
                        "SELECT b FROM Booking b " +
                                "WHERE b.id = :id AND b.user = :user",
                        Booking.class)
                .setParameter("id", bookingId)
                .setParameter("user", user)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (booking != null) {
            em.remove(booking);
        }
    }

    /** alle Buchungen zu einem Room löschen (Variante B) */
    public void deleteByRoom(Room room) {
        if (room == null || room.getId() == null) {
            return;
        }
        deleteByRoomId(room.getId());
    }

    /** alle Buchungen zu einer Room-ID löschen */
    public void deleteByRoomId(Long roomId) {
        if (roomId == null) {
            return;
        }

        em.createQuery("DELETE FROM Booking b WHERE b.room.id = :roomId")
          .setParameter("roomId", roomId)
          .executeUpdate();
    }
}
