package de.iu.raumbuchung.service;

public class OverlappingBookingException extends Exception {
    public OverlappingBookingException(String message) {
        super(message);
    }
}
