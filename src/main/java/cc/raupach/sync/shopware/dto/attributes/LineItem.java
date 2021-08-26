package cc.raupach.sync.shopware.dto.attributes;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
public class LineItem {

    private String orderId;
    private String productId;
    private Integer quantity;
    private String label;
    private Double unitPrice;
    private Double totalPrice;


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
