package de.iu.raumbuchung.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms") // tabelle rooms
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    private Long id;

    @Column(nullable = false, length = 100) // raumname
    private String name;

    @Column(nullable = false, length = 100) // standort
    private String location;

    @Column(nullable = false) // kapazität
    private int capacity;

    @Column(length = 255) // ausstattung
    private String equipment;

    public Room() {
    }

    public Room(String name, String location, int capacity, String equipment) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.equipment = equipment;
    }

    // getter und setter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }
}
