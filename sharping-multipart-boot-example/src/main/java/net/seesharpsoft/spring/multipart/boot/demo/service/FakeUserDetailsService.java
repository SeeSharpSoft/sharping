package net.seesharpsoft.spring.multipart.boot.demo.service;

import net.seesharpsoft.spring.multipart.boot.demo.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

@Service
public class FakeUserDetailsService implements UserDetailsService {
    @Autowired
    private PersonRepository personRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws
            UsernameNotFoundException {
//        Person person = personRepository.findByFirstNameEquals(username);
//        if (person == null) {
//            throw new UsernameNotFoundException("Username " + username + " not found");
//        }
        return new User(username, "password", getGrantedAuthorities(username));
    }

    private Collection<? extends GrantedAuthority> getGrantedAuthorities(String
                                                                                 username) {
        Collection<? extends GrantedAuthority> authorities;
        if (username.equals("John")) {
            authorities = Arrays.asList(() -> "ROLE_ADMIN", () -> "ROLE_BASIC");
        } else {
            authorities = Arrays.asList(() -> "ROLE_BASIC");
        }
        return authorities;
    }
}