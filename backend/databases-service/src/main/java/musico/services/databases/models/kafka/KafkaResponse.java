package musico.services.databases.models.kafka;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaResponse<T> {
    private T payload;
    private String message;
    private Integer status;
}
