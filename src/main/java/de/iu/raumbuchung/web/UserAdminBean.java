package de.iu.raumbuchung.web;

import de.iu.raumbuchung.entity.User;
import de.iu.raumbuchung.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

@Named
@RequestScoped
public class UserAdminBean {

    @Inject
    private UserService userService;

    @Inject
    private LoginBean loginBean;

    // Filter-Felder
    private String searchTerm;
    // Standard: alle anzeigen (nicht nur PENDING)
    private String statusFilter = "ALL";
    private String roleFilter = "ALL";

    // Ergebnisliste
    private List<User> users;

    // --------- Daten-Lieferung ---------

    public List<User> getUsers() {
        if (users == null) {
            loadUsers();
        }
        return users;
    }

    public void loadUsers() {
        users = userService.searchUsers(searchTerm, statusFilter, roleFilter);
    }

    // --------- Aktionen ---------

    public void applyFilter() {
        loadUsers();
    }

    public void activate(Long userId) {
        userService.updateStatus(userId, "ACTIVE");
        addMessage(FacesMessage.SEVERITY_INFO, "User wurde freigeschaltet.");
        loadUsers();
    }

    public void makeAdmin(Long userId) {
        userService.updateRole(userId, "ADMIN");
        addMessage(FacesMessage.SEVERITY_INFO, "User hat jetzt Adminrechte.");
        loadUsers();
    }

    public void makeUser(Long userId) {
        // eigene Adminrechte nicht entziehen
        if (loginBean.getCurrentUser() != null
                && loginBean.getCurrentUser().getId() != null
                && loginBean.getCurrentUser().getId().equals(userId)) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Du kannst dir deine eigenen Adminrechte nicht entziehen.");
            return;
        }
        userService.updateRole(userId, "USER");
        addMessage(FacesMessage.SEVERITY_INFO, "Adminrechte wurden entfernt.");
        loadUsers();
    }

    private void addMessage(FacesMessage.Severity sev, String text) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(sev, text, null));
    }

    // --------- Getter / Setter ---------

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }

    public String getRoleFilter() {
        return roleFilter;
    }

    public void setRoleFilter(String roleFilter) {
        this.roleFilter = roleFilter;
    }
}
