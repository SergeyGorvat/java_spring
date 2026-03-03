package spring.mvc.shop.config;

/**
 * Интерфейсы представлений для @JsonView.
 * <p>
 * UserSummary — базовая информация о пользователе (без заказов).
 * Используется при запросе списка всех пользователей.
 * <p>
 * UserDetails extends UserSummary — полная информация, включая заказы.
 * Используется при запросе конкретного пользователя.
 */

public class Views {

    public interface UserSummary {
    }

    public interface UserDetails extends UserSummary {
    }
}
