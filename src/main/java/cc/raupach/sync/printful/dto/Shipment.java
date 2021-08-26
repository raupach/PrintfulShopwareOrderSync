package cc.raupach.sync.printful.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigInteger;
import java.util.Date;

@Getter
@Setter
public class Shipment {

    private BigInteger id;
    private String carrier;
    private String service;
    private String tracking_number;
    private String tracking_url;
    private String ship_date;
    private BigInteger shipped_at;
    private Boolean reshipment;
    private Date created;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
