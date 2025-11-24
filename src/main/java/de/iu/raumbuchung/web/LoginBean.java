package de.iu.raumbuchung.web;

import de.iu.raumbuchung.entity.User;
import de.iu.raumbuchung.service.UserService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import jakarta.faces.context.ExternalContext;
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
        FacesContext ctx = FacesContext.getCurrentInstance();

        User user = userService.findByUsername(username);

        if (user == null) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Username existiert nicht", null));
            return null;
        }

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Passwort ist falsch", null));
            return null;
        }

        currentUser = user;
        password = null;

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Dein Konto ist noch nicht freigeschaltet.", null));
        }

        // immer auf index, dort wird über loggedIn unterschieden
        return "index?faces-redirect=true";
    }

    public boolean isActiveUser() {
        return currentUser != null
                && "ACTIVE".equalsIgnoreCase(currentUser.getStatus());
    }

    public boolean isPendingUser() {
        return currentUser != null
                && !"ACTIVE".equalsIgnoreCase(currentUser.getStatus());
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

    public void redirectIfNotAllowed() {
        // wenn nicht eingeloggt oder nicht aktiv -> auf index zurück
        if (!isLoggedIn() || !isActiveUser()) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ExternalContext ec = ctx.getExternalContext();

            try {
                String contextPath = ec.getRequestContextPath();
                ec.redirect(contextPath + "/index.xhtml");
                ctx.responseComplete();
            } catch (IOException e) {
                // ggf. loggen
            }
        }
    }
}
