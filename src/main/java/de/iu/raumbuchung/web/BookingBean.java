package de.iu.raumbuchung.web;

import de.iu.raumbuchung.entity.Booking;
import de.iu.raumbuchung.entity.Room;
import de.iu.raumbuchung.entity.User;
import de.iu.raumbuchung.service.BookingService;
import de.iu.raumbuchung.service.OverlappingBookingException;
import de.iu.raumbuchung.service.RoomService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Named
@ViewScoped
public class BookingBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private BookingService bookingService;

    @Inject
    private RoomService roomService;

    @Inject
    private LoginBean loginBean;

    // Tagesübersicht für Timeline
    private List<Booking> dayBookings;

    private Long roomId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    // Stunden-Ticks (jede Stunde) und Labels (alle 2h)
    private List<Integer> hourTicks;
    private List<Integer> labelHours;

    // Tab-Auswahl für "Offen" / "Abgelaufen"
    private String bookingFilter = "OPEN"; // OPEN oder PAST

    @PostConstruct
    public void init() {
        if (roomId == null) {
            List<Room> rooms = roomService.findAll();
            if (rooms != null && !rooms.isEmpty()) {
                this.roomId = rooms.get(0).getId();
            }
        }
        if (date == null) {
            this.date = LocalDate.now();
        }
        if (roomId != null && date != null) {
            reloadDayBookings();
        }

        // Striche: jede Stunde 0..24
        hourTicks = IntStream.rangeClosed(0, 24)
                .boxed()
                .collect(Collectors.toList());

        // Labels: nur alle 2 Stunden 0,2,4,...,24
        labelHours = IntStream.rangeClosed(0, 12)
                .map(i -> i * 2)
                .boxed()
                .collect(Collectors.toList());
    }

    // alle verfügbaren räume für dropdown
    public List<Room> getRooms() {
        return roomService.findAll();
    }

    // alle buchungen des aktuellen users
    public List<Booking> getMyBookings() {
        User u = loginBean.getCurrentUser();
        if (u == null) {
            return List.of();
        }
        return bookingService.findByUser(u);
    }

    // offene (zukünftige / laufende) Buchungen des Users
    public List<Booking> getMyOpenBookings() {
        User u = loginBean.getCurrentUser();
        if (u == null) {
            return Collections.emptyList();
        }

        List<Booking> all = bookingService.findByUser(u);
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate heute = LocalDate.now();
        LocalTime jetzt = LocalTime.now();

        return all.stream()
                .filter(b -> b.getDate() != null
                        && b.getStartTime() != null
                        && b.getEndTime() != null)
                .filter(b -> {
                    LocalDate d = b.getDate();
                    if (d.isAfter(heute)) {
                        return true;
                    }
                    if (d.isEqual(heute)) {
                        return b.getEndTime().isAfter(jetzt);
                    }
                    return false;
                })
                .sorted(Comparator
                        .comparing(Booking::getDate)
                        .thenComparing(Booking::getStartTime))
                .collect(Collectors.toList());
    }

    // abgelaufene Buchungen des Users
    public List<Booking> getMyPastBookings() {
        User u = loginBean.getCurrentUser();
        if (u == null) {
            return Collections.emptyList();
        }

        List<Booking> all = bookingService.findByUser(u);
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate heute = LocalDate.now();
        LocalTime jetzt = LocalTime.now();

        return all.stream()
                .filter(b -> b.getDate() != null
                        && b.getStartTime() != null
                        && b.getEndTime() != null)
                .filter(b -> {
                    LocalDate d = b.getDate();
                    if (d.isBefore(heute)) {
                        return true;
                    }
                    if (d.isEqual(heute)) {
                        return b.getEndTime().isBefore(jetzt) || b.getEndTime().equals(jetzt);
                    }
                    return false;
                })
                .sorted(Comparator
                        .comparing(Booking::getDate).reversed()
                        .thenComparing(Booking::getStartTime).reversed())
                .collect(Collectors.toList());
    }

    // Startseite: nur die nächsten 5 offenen Buchungen
    public List<Booking> getUpcomingBookings() {
        List<Booking> open = getMyOpenBookings();
        if (open.isEmpty()) {
            return open;
        }
        return open.stream().limit(5).collect(Collectors.toList());
    }

    public String book() {
        User u = loginBean.getCurrentUser();
        if (u == null) {
            addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Bitte zuerst einloggen.");
            return null;
        }

        if (!"ACTIVE".equalsIgnoreCase(u.getStatus())) {
            addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Dein Konto ist noch nicht freigeschaltet.");
            return null;
        }

        boolean hasError = false;

        // Räume prüfen: wenn keine Räume angelegt sind
        List<Room> rooms = roomService.findAll();
        if (rooms == null || rooms.isEmpty()) {
            addFieldMessage("bookingForm:room", FacesMessage.SEVERITY_ERROR,
                    "Es sind noch keine Räume angelegt.");
            hasError = true;
        } else if (roomId == null) {
            addFieldMessage("bookingForm:room", FacesMessage.SEVERITY_ERROR,
                    "Bitte einen Raum auswählen.");
            hasError = true;
        }

        // Pflichtfelder Datum / Zeiten
        if (date == null) {
            addFieldMessage("bookingForm:date", FacesMessage.SEVERITY_ERROR,
                    "Bitte ein Datum wählen.");
            hasError = true;
        }
        if (startTime == null) {
            addFieldMessage("bookingForm:start", FacesMessage.SEVERITY_ERROR,
                    "Bitte eine Startzeit wählen.");
            hasError = true;
        }
        if (endTime == null) {
            addFieldMessage("bookingForm:end", FacesMessage.SEVERITY_ERROR,
                    "Bitte eine Endzeit wählen.");
            hasError = true;
        }

        if (hasError) {
            return null;
        }

        // Logische Checks nur, wenn alles gesetzt ist
        LocalDate heute = LocalDate.now();
        LocalTime jetzt = LocalTime.now();

        // Datum in Vergangenheit
        if (date.isBefore(heute)) {
            addFieldMessage("bookingForm:date", FacesMessage.SEVERITY_ERROR,
                    "Du kannst nicht in der Vergangenheit buchen.");
            return null;
        }

        // Heute & Endzeit vor aktueller Zeit
        if (date.isEqual(heute) && endTime.isBefore(jetzt)) {
            addFieldMessage("bookingForm:end", FacesMessage.SEVERITY_ERROR,
                    "Du kannst nicht in der Vergangenheit buchen.");
            return null;
        }

        // Ende muss nach Start liegen
        if (!endTime.isAfter(startTime)) {
            addFieldMessage("bookingForm:end", FacesMessage.SEVERITY_ERROR,
                    "Die Endzeit muss nach der Startzeit liegen.");
            return null;
        }

        try {
            bookingService.createBooking(u, roomId, date, startTime, endTime);
            addGlobalMessage(FacesMessage.SEVERITY_INFO, "Buchung gespeichert.");
            reloadDayBookings();
        } catch (OverlappingBookingException e) {
            // Überschneidung: Meldung oben
            addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    "Es gibt eine Überschneidung, Buchung nicht möglich.");
        }

        return null;
    }

    /** eigene Buchung löschen (nur wenn sie dem aktuellen User gehört) */
    public void delete(Long bookingId) {
        User u = loginBean.getCurrentUser();
        if (u == null) {
            addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Bitte zuerst einloggen.");
            return;
        }

        bookingService.deleteBookingForUser(bookingId, u);
        addGlobalMessage(FacesMessage.SEVERITY_INFO, "Buchung gelöscht.");

        // Timeline aktualisieren
        reloadDayBookings();
    }

    // --- Message-Helfer ---

    private void addGlobalMessage(FacesMessage.Severity severity, String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severity, msg, null));
    }

    private void addFieldMessage(String clientId, FacesMessage.Severity severity, String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(clientId, new FacesMessage(severity, msg, null));
    }

    // --- Getter / Setter ---

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

    public void reloadDayBookings() {
        if (roomId == null || date == null) {
            dayBookings = Collections.emptyList();
        } else {
            dayBookings = bookingService.findBookingsForRoomAndDate(roomId, date);
        }
    }

    public List<Booking> getDayBookings() {
        if (dayBookings == null && roomId != null && date != null) {
            reloadDayBookings();
        }
        return dayBookings == null ? Collections.emptyList() : dayBookings;
    }

    // --- Zeitleisten-Hilfsfunktionen ---

    private int toMinutes(LocalTime time) {
        if (time == null) {
            return 0;
        }
        return time.getHour() * 60 + time.getMinute();
    }

    private String percentFromMinutes(int minutes) {
        double p = (minutes / 1440.0) * 100.0;
        return String.format(Locale.US, "%.2f%%", p);
    }

    public String startPercent(Booking b) {
        if (b == null || b.getStartTime() == null) {
            return "0%";
        }
        int minutes = toMinutes(b.getStartTime());
        return percentFromMinutes(minutes);
    }

    public String widthPercent(Booking b) {
        if (b == null || b.getStartTime() == null || b.getEndTime() == null) {
            return "0%";
        }
        int startMin = toMinutes(b.getStartTime());
        int endMin = toMinutes(b.getEndTime());
        int duration = endMin - startMin;
        if (duration <= 0) {
            return "0%";
        }
        return percentFromMinutes(duration);
    }

    public String getSelectionStartPercent() {
        if (startTime == null) {
            return "0%";
        }
        int minutes = toMinutes(startTime);
        return percentFromMinutes(minutes);
    }

    public String getSelectionWidthPercent() {
        if (startTime == null || endTime == null) {
            return "0%";
        }
        int startMin = toMinutes(startTime);
        int endMin = toMinutes(endTime);
        int duration = endMin - startMin;
        if (duration <= 0) {
            return "0%";
        }
        return percentFromMinutes(duration);
    }

    // ----------- Stunden-Marken -----------

    public List<Integer> getHourTicks() {
        return hourTicks;
    }

    public List<Integer> getLabelHours() {
        return labelHours;
    }

    /** Prozentposition für eine Stunde. */
    public String percentForHour(Integer hour) {
        if (hour == null) {
            return "0%";
        }
        int minutes = hour * 60;
        return percentFromMinutes(minutes);
    }

    // ----------- Tab-Handling "Offen" / "Abgelaufen" -----------

    public String getBookingFilter() {
        return bookingFilter;
    }

    public void setBookingFilter(String bookingFilter) {
        this.bookingFilter = bookingFilter;
    }

    public void showOpen() {
        this.bookingFilter = "OPEN";
    }

    public void showPast() {
        this.bookingFilter = "PAST";
    }
}
