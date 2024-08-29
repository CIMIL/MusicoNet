package musico.services.user.models;

import lombok.Data;

@Data
public class KafkaResponse<T> {
    private T payload;
    private String message;
    private Integer status;
}
