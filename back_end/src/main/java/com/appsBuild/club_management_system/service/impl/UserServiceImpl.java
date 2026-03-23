package com.appsBuild.club_management_system.service.impl;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class UserServiceImpl {
    private final UserRepository userRepository;
    public User save(User user) {
        if (user == null) {
            throw new NoContentException("User cannot be null");
        }
        return userRepository.save(user);
    }
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }
    public Optional<User> findByIdOptional(Long id) {
        return userRepository.findById(id);
    }
    public List<User> findAll() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new NoContentException("No users found");
        }
        return users;
    }
    public User update(Long id, User user) {
        findById(id);
        if (user == null) {
            throw new NoContentException("User cannot be null");
        }
        user.setUserId(id);
        return userRepository.save(user);
    }
    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
