package cc.raupach.sync.shopware.dto.attributes;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
public class Order {

    private String orderNumber;
    private String billingAddressId;
    private String billingAddressVersionId;
    private String currencyId;
    private String stateId;
    private Double amountTotal;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
