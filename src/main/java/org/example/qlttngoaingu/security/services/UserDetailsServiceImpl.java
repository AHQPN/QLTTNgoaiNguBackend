package org.example.qlttngoaingu.security.services;



import org.example.qlttngoaingu.repository.UserRepository;
import org.example.qlttngoaingu.entity.User;
import org.example.qlttngoaingu.security.model.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    // constructor injection (recommended)
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // transactional read-only
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        User user = userRepository.findByPhoneNumberOrEmail(identifier,identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found" ));

        return UserDetailsImpl.build(user);
    }
}
