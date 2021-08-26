package cc.raupach.sync.printful.dto;


import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
public class Costs {

    private String currency;
    private String subtotal;
    private String discount;
    private String shipping;
    private String digitization;
    private String tax;
    private String vat;
    private String total;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
