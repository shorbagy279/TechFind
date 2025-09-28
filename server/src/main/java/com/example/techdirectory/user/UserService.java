package com.example.techdirectory.user;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
  private final UserRepository repo;
  public UserService(UserRepository repo){ this.repo=repo; }
  @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
}
