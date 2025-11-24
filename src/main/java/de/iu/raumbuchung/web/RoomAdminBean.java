package de.iu.raumbuchung.web;

import de.iu.raumbuchung.entity.Room;
import de.iu.raumbuchung.service.RoomService;
import de.iu.raumbuchung.service.LocationService;
import de.iu.raumbuchung.entity.Location;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

@Named
@RequestScoped
public class RoomAdminBean {

    @Inject
    private RoomService roomService;

    @Inject
    private LocationService locationService;

    private String newLocationName;

    private String selectedLocation;

    private Room newRoom = new Room();

    // --------- Räume ---------

    public List<Room> getRooms() {
        return roomService.findAll();
    }

    public Room getNewRoom() {
        return newRoom;
    }

    public void setNewRoom(Room newRoom) {
        this.newRoom = newRoom;
    }

    public String save() {
        boolean hasError = false;

        // Name
        if (newRoom.getName() == null || newRoom.getName().trim().isEmpty()) {
            addFieldMessage("roomForm:name",
                    FacesMessage.SEVERITY_ERROR,
                    "Bitte einen Namen für den Raum eingeben");
            hasError = true;
        }

        // Standorte vorhanden?
        List<Location> locations = getLocations();
        if (locations == null || locations.isEmpty()) {
            addFieldMessage("roomForm:location",
                    FacesMessage.SEVERITY_ERROR,
                    "Es sind noch keine Standorte angelegt. Bitte oben zuerst einen Standort erstellen.");
            hasError = true;
        } else {
            // Standort ausgewählt?
            String loc = newRoom.getLocation();
            if (loc == null || loc.trim().isEmpty()) {
                addFieldMessage("roomForm:location",
                        FacesMessage.SEVERITY_ERROR,
                        "Bitte einen Standort auswählen");
                hasError = true;
            }
        }

        // Etage
        if (newRoom.getFloor() == null) {
            addFieldMessage("roomForm:floor",
                    FacesMessage.SEVERITY_ERROR,
                    "Bitte eine Etage eingeben");
            hasError = true;
        }

        // Raumcode
        if (newRoom.getRoomCode() == null || newRoom.getRoomCode().trim().isEmpty()) {
            addFieldMessage("roomForm:roomCode",
                    FacesMessage.SEVERITY_ERROR,
                    "Bitte eine Raumnummer / Bezeichnung eingeben");
            hasError = true;
        }

        // Kapazität
        if (newRoom.getCapacity() == null) {
            addFieldMessage("roomForm:capacity",
                    FacesMessage.SEVERITY_ERROR,
                    "Bitte eine Kapazität angeben");
            hasError = true;
        } else if (newRoom.getCapacity() <= 0) {
            addFieldMessage("roomForm:capacity",
                    FacesMessage.SEVERITY_ERROR,
                    "Kapazität muss größer als 0 sein");
            hasError = true;
        }

        if (hasError) {
            return null;
        }

        roomService.save(newRoom);
        addGlobalMessage(FacesMessage.SEVERITY_INFO, "Raum gespeichert.");
        newRoom = new Room(); // formular leeren
        return null; // auf seite bleiben
    }

    public String delete(Long id) {
        roomService.delete(id);
        return null;
    }

    public List<String> getAllLocations() {
        return roomService.findAllLocations();
    }

    public String getSelectedLocation() {
        return selectedLocation;
    }

    public void setSelectedLocation(String selectedLocation) {
        this.selectedLocation = selectedLocation;
    }

    // --------- Standorte ---------

    public String getNewLocationName() {
        return newLocationName;
    }

    public void setNewLocationName(String newLocationName) {
        this.newLocationName = newLocationName;
    }

    public List<Location> getLocations() {
        return locationService.findAll();
    }

    public void saveLocation() {
        String name = newLocationName != null ? newLocationName.trim() : null;

        if (name == null || name.isEmpty()) {
            addFieldMessage("locationForm:locName",
                    FacesMessage.SEVERITY_ERROR,
                    "Bitte einen Standortnamen eingeben");
            return;
        }

        // Duplikate verhindern (Case-insensitive)
        List<Location> existing = getLocations();
        if (existing != null) {
            boolean exists = existing.stream()
                    .filter(loc -> loc.getName() != null)
                    .anyMatch(loc -> loc.getName().equalsIgnoreCase(name));

            if (exists) {
                addFieldMessage("locationForm:locName",
                        FacesMessage.SEVERITY_ERROR,
                        "Es existiert bereits ein Standort mit diesem Namen.");
                return;
            }
        }

        locationService.create(name);
        newLocationName = null;
    }

    public void deleteLocation(Long id) {
        Location loc = locationService.findById(id);
        if (loc == null) {
            return;
        }
        // lösche räume und location
        roomService.deleteByLocation(loc.getName());
        locationService.delete(id);
    }

    // --------- Message-Helfer ---------

    private void addFieldMessage(String clientId, FacesMessage.Severity severity, String text) {
        FacesContext.getCurrentInstance()
                .addMessage(clientId, new FacesMessage(severity, text, null));
    }

    private void addGlobalMessage(FacesMessage.Severity severity, String text) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severity, text, null));
    }
}
