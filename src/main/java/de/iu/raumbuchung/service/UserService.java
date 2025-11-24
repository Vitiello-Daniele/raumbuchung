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

        // Prüfen, ob dies der erste User in der DB ist
        boolean firstUser = countAllUsers() == 0;

        String status;
        String role;

        if (firstUser) {
            // erster Account: sofort aktiv + Admin
            status = "ACTIVE";
            role = "ADMIN";
        } else {
            // alle weiteren: normaler User im Status PENDING
            status = "PENDING";
            role = "USER";
        }

        User user = new User(username, email, hash, status, role);
        em.persist(user);

        // sicherstellen, dass der neue User für spätere COUNT-Abfragen
        // im selben Request sichtbar ist
        em.flush();
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

    private long countAllUsers() {
        Long count = em.createQuery(
                "SELECT COUNT(u) FROM User u",
                Long.class
        ).getSingleResult();
        return count == null ? 0L : count;
    }

    /** Für UI: true, wenn mindestens ein User existiert */
    public boolean hasAnyUser() {
        return countAllUsers() > 0;
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

    // Query, user liste of pending state (für alte Nutzung, falls noch irgendwo verwendet)
    public List<User> findPendingUsers() {
        return em.createQuery(
                "SELECT u FROM User u WHERE u.status = 'PENDING'",
                User.class
        ).getResultList();
    }

    // user freischalten
    public void activateUser(Long id) {
        updateStatus(id, "ACTIVE");
    }

    // ---------- NEU: generische Suche & Status/Rolle-Update ----------

    /**
     * Suche/Filter über User.
     *
     * @param search       Text in Username oder E-Mail (optional)
     * @param statusFilter ALL | PENDING | ACTIVE | BLOCKED
     * @param roleFilter   ALL | USER | ADMIN
     */
    public List<User> searchUsers(String search, String statusFilter, String roleFilter) {
        StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE 1=1");

        if (search != null && !search.trim().isEmpty()) {
            jpql.append(" AND (LOWER(u.username) LIKE :q OR LOWER(u.email) LIKE :q)");
        }

        if (statusFilter != null && !"ALL".equalsIgnoreCase(statusFilter)) {
            jpql.append(" AND UPPER(u.status) = :status");
        }

        if (roleFilter != null && !"ALL".equalsIgnoreCase(roleFilter)) {
            jpql.append(" AND UPPER(u.role) = :role");
        }

        jpql.append(" ORDER BY u.username");

        TypedQuery<User> q = em.createQuery(jpql.toString(), User.class);

        if (search != null && !search.trim().isEmpty()) {
            String like = "%" + search.trim().toLowerCase() + "%";
            q.setParameter("q", like);
        }

        if (statusFilter != null && !"ALL".equalsIgnoreCase(statusFilter)) {
            q.setParameter("status", statusFilter.toUpperCase());
        }

        if (roleFilter != null && !"ALL".equalsIgnoreCase(roleFilter)) {
            q.setParameter("role", roleFilter.toUpperCase());
        }

        return q.getResultList();
    }

    public void updateStatus(Long userId, String newStatus) {
        if (userId == null || newStatus == null) {
            return;
        }
        User u = em.find(User.class, userId);
        if (u != null) {
            u.setStatus(newStatus.toUpperCase());
        }
    }

    public void updateRole(Long userId, String newRole) {
        if (userId == null || newRole == null) {
            return;
        }
        User u = em.find(User.class, userId);
        if (u != null) {
            u.setRole(newRole.toUpperCase());
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
