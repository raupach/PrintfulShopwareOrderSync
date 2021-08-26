package cc.raupach.sync.printful.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
public class Order {

    private BigInteger id;
    private String external_id;
    private Integer store;
    private OrderStatus status;
    private String shipping;
    private Address recipient;
    private List<Item> items;
    private OrderPackingSlip packing_slip;
    private Costs costs;
    private Costs retail_costs;
    private List<Shipment>shipments;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
