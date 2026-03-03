package spring.mvc.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.mvc.shop.dto.user.CreateUserDto;
import spring.mvc.shop.dto.user.UpdateUserDto;
import spring.mvc.shop.entity.User;
import spring.mvc.shop.exception.EmailAlreadyExistsException;
import spring.mvc.shop.exception.UserNotFoundException;
import spring.mvc.shop.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findByIdWithOrders(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public User create(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        User newUser = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .build();

        return userRepository.save(newUser);
    }

    @Transactional
    public User update(Long id, UpdateUserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (!dto.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
                    throw new EmailAlreadyExistsException(dto.getEmail());
                }
                user.setEmail(dto.getEmail());
            }
        }

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());

        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}
