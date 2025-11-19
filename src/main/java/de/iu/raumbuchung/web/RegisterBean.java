package de.iu.raumbuchung.web;

import de.iu.raumbuchung.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class RegisterBean {

    private String username;
    private String email;
    private String password;

    @Inject
    private UserService userService;

    public String register() {
        // user über service anlegen
        userService.registerNewUser(username, email, password);

        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage("registrierung erfolgreich"));

        // zurück auf startseite
        return "index?faces-redirect=true";
    }

    // getter und setter
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
}
