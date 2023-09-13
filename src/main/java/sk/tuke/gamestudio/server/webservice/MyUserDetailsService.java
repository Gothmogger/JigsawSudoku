package sk.tuke.gamestudio.server.webservice;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.MyUserDetails;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.server.dto.UserDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Transactional
public class MyUserDetailsService implements UserDetailsService {

    @PersistenceContext
    private EntityManager entityManager;

    public MyUserDetailsService() {}

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        //Optional<UserDto> user = entityManager..findByUserName(userName);
        try {
            Object user = null;
            user = entityManager.createQuery("select u from User u where u.userName = :userName")
                    .setParameter("userName", userName)
                    .getSingleResult();
            return new MyUserDetails((User) user);
        } catch (NoResultException e) {
            throw new UsernameNotFoundException("Not found: " + userName);
        }
    }

    public User registerNewUserAccount(User user) {
        try {
            Object existingUser = null;
            existingUser = entityManager.createQuery("select u from User u where u.userName = :userName")
                    .setParameter("userName", user.getUserName())
                    .getSingleResult();

            return null;
        } catch (NoResultException e) {
            entityManager.persist(user);

            return user;
        }
    }
}