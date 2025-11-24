package de.iu.raumbuchung.web;

import de.iu.raumbuchung.service.DuplicateUserException;
import de.iu.raumbuchung.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@RequestScoped
public class RegisterBean implements Serializable {

    private String username;
    private String email;
    private String password;
    private String passwordRepeat;

    // wurde erfolgreich registriert (für die grüne Box)
    private boolean registrationSuccessful;

    // Installationsmodus: true, wenn noch kein User existiert
    private boolean installationMode;

    @Inject
    private UserService userService;

    @PostConstruct
    public void init() {
        // Beim Laden: prüfen, ob schon User vorhanden sind
        installationMode = !userService.hasAnyUser();
    }

    public String register() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        registrationSuccessful = false;

        // simple Validierung Passwort == Wiederholung
        if (password == null || passwordRepeat == null || !password.equals(passwordRepeat)) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Passwörter stimmen nicht überein", null));
            return null;
        }

        try {
            userService.registerNewUser(username, email, password);
            registrationSuccessful = true;

            // nach erster erfolgreichen Registrierung kein Installationsmodus mehr
            installationMode = false;

            // Felder leeren
            password = null;
            passwordRepeat = null;

        } catch (DuplicateUserException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Username oder E-Mail ist bereits vergeben.", null));
            registrationSuccessful = false;
        } catch (Exception e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Registrierung fehlgeschlagen.", null));
            registrationSuccessful = false;
        }

        return null; // auf der Seite bleiben
    }

    // --- Getter / Setter ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }

    public void setRegistrationSuccessful(boolean registrationSuccessful) {
        this.registrationSuccessful = registrationSuccessful;
    }

    public boolean isInstallationMode() {
        return installationMode;
    }

    public void setInstallationMode(boolean installationMode) {
        this.installationMode = installationMode;
    }
}
