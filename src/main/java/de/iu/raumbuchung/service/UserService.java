package de.iu.raumbuchung.service;

import de.iu.raumbuchung.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

@Stateless
public class UserService {

    @PersistenceContext(unitName = "roomPU")
    private EntityManager em;

    public void registerNewUser(String username, String email, String rawPassword) throws DuplicateUserException {
        if (existsByUsernameOrEmail(username, email)) {
            throw new DuplicateUserException("username oder email schon vergeben");
        }

        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        String status;
        String role;

        if ("admin".equalsIgnoreCase(username)) {
            // spezieller admin-user: direkt aktiv, später evtl über "Wenn keine user dann installationsuser setzen (1. registierter user erhält admin etc.)"
            status = "ACTIVE";
            role = "ADMIN";
        } else {
            // normale user
            status = "PENDING";
            role = "USER";
        }

        User user = new User(username, email, hash, status, role);
        em.persist(user);
    }

    private boolean existsByUsernameOrEmail(String username, String email) {
        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username OR u.email = :email",
                Long.class
        );
        q.setParameter("username", username);
        q.setParameter("email", email);
        Long count = q.getSingleResult();
        return count != null && count > 0;
    }
    
    public User authenticate(String username, String rawPassword) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :username",
                User.class
        );
        q.setParameter("username", username);

        User user;
        try {
            user = q.getSingleResult();
        } catch (Exception e) {
            return null; // kein user gefunden
        }

        // passwort prüfen
        if (!BCrypt.checkpw(rawPassword, user.getPasswordHash())) {
            return null;
        }

        // status prüfen (case-insensitive)
        String status = user.getStatus();
        if (status == null || !"ACTIVE".equalsIgnoreCase(status)) {
            return null;
        }

        return user;
    }
    
    //Query, user liste of pending state 
    public List<User> findPendingUsers() {
        return em.createQuery(
                "SELECT u FROM User u WHERE u.status = 'PENDING'",
                User.class
        ).getResultList();
    }
    
    //user freischalten
    public void activateUser(Long id) {
        User user = em.find(User.class, id);
        if (user != null) {
            user.setStatus("ACTIVE");
        }
    }
    
    public User findByUsername(String username) {
        try {
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username",
                    User.class
            ).setParameter("username", username)
             .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

}
