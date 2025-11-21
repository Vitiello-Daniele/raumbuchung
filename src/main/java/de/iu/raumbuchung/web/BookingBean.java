package de.iu.raumbuchung.web;

import de.iu.raumbuchung.entity.Booking;
import de.iu.raumbuchung.entity.Room;
import de.iu.raumbuchung.entity.User;
import de.iu.raumbuchung.service.BookingService;
import de.iu.raumbuchung.service.OverlappingBookingException;
import de.iu.raumbuchung.service.RoomService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Named
@RequestScoped
public class BookingBean {

    @Inject
    private BookingService bookingService;

    @Inject
    private RoomService roomService;

    @Inject
    private LoginBean loginBean;

    private Long roomId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public List<Room> getRooms() {
        return roomService.findAll();
    }

    public List<Booking> getMyBookings() {
        User u = loginBean.getCurrentUser();
        if (u == null) {
            return List.of();
        }
        return bookingService.findByUser(u);
    }

    public String book() {
        User u = loginBean.getCurrentUser();
        if (u == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "bitte zuerst einloggen");
            return null;
        }

        try {
            bookingService.createBooking(u, roomId, date, startTime, endTime);
            addMessage(FacesMessage.SEVERITY_INFO, "buchung gespeichert");
        } catch (OverlappingBookingException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }

        return null;
    }

    private void addMessage(FacesMessage.Severity severity, String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severity, msg, null));
    }

    // getter und setter
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
