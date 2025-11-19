package de.iu.raumbuchung.web;

import de.iu.raumbuchung.entity.User;
import de.iu.raumbuchung.service.UserService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    private String username;
    private String password;
    private User currentUser;

    @Inject
    private UserService userService;

    public String login() {
        User user = userService.findByUsername(username);

        if (user == null) {
            addError("username existiert nicht");
            return null;
        }

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            addError("passwort ist falsch");
            return null;
        }

        // status prüfen: nur ACTIVE darf rein (admin ist eh ACTIVE)
        // hier nicht nach admin filtern, da später installationsseite
        if (user.getStatus() == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            addError("konto ist noch nicht freigeschaltet");
            return null;
        }

        this.currentUser = user;
        this.password = null;

        return "index?faces-redirect=true";
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public String logout() {
        currentUser = null;
        username = null;
        password = null;
        return "index?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // getter/setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}
