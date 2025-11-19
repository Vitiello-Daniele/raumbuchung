package de.iu.raumbuchung.service;

import de.iu.raumbuchung.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.mindrot.jbcrypt.BCrypt;

@Stateless
public class UserService {

    @PersistenceContext(unitName = "roomPU") // unsere persistence unit
    private EntityManager em;

    public void registerNewUser(String username, String email, String rawPassword) {
        // passwort hashen
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        // neuer user mit status PENDING
        User user = new User(username, email, hash, "PENDING");

        em.persist(user);
    }
}
