package de.iu.raumbuchung.web;

import de.iu.raumbuchung.entity.User;
import de.iu.raumbuchung.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

@Named
@RequestScoped
public class AdminBean {

    @Inject
    private UserService userService;

    public List<User> getPendingUsers() {
        return userService.findPendingUsers();
    }

    public String activate(Long userId) {
        userService.activateUser(userId);
        return null; // auf seite bleiben
    }
}
