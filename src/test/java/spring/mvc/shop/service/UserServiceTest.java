package spring.mvc.shop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spring.mvc.shop.dto.user.CreateUserDto;
import spring.mvc.shop.dto.user.UpdateUserDto;
import spring.mvc.shop.entity.User;
import spring.mvc.shop.exception.EmailAlreadyExistsException;
import spring.mvc.shop.exception.UserNotFoundException;
import spring.mvc.shop.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Сергей Горват")
                .email("gorvat@example.com")
                .phone("+79001234567")
                .address("Москва, ул. Ленина 1")
                .build();
    }

    @Test
    @DisplayName("findAll — возвращает список пользователей")
    void findAll_ReturnsUserList() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<User> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Сергей Горват");
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("findById — пользователь найден")
    void findById_UserExists_ReturnsUser() {
        when(userRepository.findByIdWithOrders(1L)).thenReturn(Optional.of(testUser));

        User result = userService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("gorvat@example.com");
    }

    @Test
    @DisplayName("findById — пользователь не найден → UserNotFoundException")
    void findById_UserNotFound_ThrowsException() {
        when(userRepository.findByIdWithOrders(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create — успешное создание пользователя")
    void create_ValidRequest_ReturnsCreatedUser() {
        CreateUserDto dto = new CreateUserDto("Мария Петрова",
                "maria@example.com", "+79009876543", null);

        when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder()
                    .id(2L)
                    .name(u.getName())
                    .email(u.getEmail())
                    .phone(u.getPhone())
                    .build();
            return u;
        });

        User result = userService.create(dto);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getEmail()).isEqualTo("maria@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("create — дублирующийся email → EmailAlreadyExistsException")
    void create_DuplicateEmail_ThrowsException() {
        CreateUserDto dto = new CreateUserDto("Копия", "gorvat@example.com", null, null);

        when(userRepository.existsByEmail("gorvat@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("gorvat@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("update — успешное обновление имени")
    void update_ValidRequest_ReturnsUpdatedUser() {
        UpdateUserDto dto = new UpdateUserDto("Новое Имя", null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("Новое Имя");
        assertThat(result.getEmail()).isEqualTo("gorvat@example.com");
    }

    @Test
    @DisplayName("update — смена email на занятый → EmailAlreadyExistsException")
    void update_EmailTaken_ThrowsException() {
        UpdateUserDto dto = new UpdateUserDto(null, "taken@example.com", null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, dto))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    @DisplayName("delete — успешное удаление")
    void delete_UserExists_DeletesCalled() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete — пользователь не найден → UserNotFoundException")
    void delete_UserNotFound_ThrowsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}