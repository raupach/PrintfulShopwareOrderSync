package cc.raupach.sync.printful.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@Getter
@Setter
@Builder
public class NewOrderRequest {

    private String external_id;
    private Address recipient;
    private List<Item> items;
    private OrderPackingSlip packing_slip;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
