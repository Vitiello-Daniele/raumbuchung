package de.iu.raumbuchung.web;

import de.iu.raumbuchung.entity.Room;
import de.iu.raumbuchung.service.RoomService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

@Named
@RequestScoped
public class RoomAdminBean {

    @Inject
    private RoomService roomService;

    private Room newRoom = new Room();

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
        roomService.save(newRoom);
        newRoom = new Room(); // formular leeren
        return null; // auf seite bleiben
    }

    public String delete(Long id) {
        roomService.delete(id);
        return null;
    }
}
