package spring.mvc.shop.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Пользователь с id=" + id + " не найден");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}