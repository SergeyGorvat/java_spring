package spring.mvc.shop.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {
    private int status;
    private String error;
    private Map<String, String> fieldErrors;
    private long timestamp;
}
