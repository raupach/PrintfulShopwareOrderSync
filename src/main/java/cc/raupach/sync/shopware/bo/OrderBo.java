package cc.raupach.sync.shopware.bo;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@Getter
@Setter
@Builder
public class OrderBo {

    private String orderId;
    private String orderNumber;
    private Double amountTotal;
    private AddressBo deliverAddress;
    private List<ProductBo> products;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
