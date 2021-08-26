package cc.raupach.sync.printful.dto;

import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigInteger;

@Getter
@Setter
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Item {

    private BigInteger id;
    private String external_id;
    private BigInteger variant_id;
    private BigInteger sync_variant_id;
    private String external_variant_id;
    private Integer quantity;
    private String price;
    private String retail_price;
    private String name;
    private String sku;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
