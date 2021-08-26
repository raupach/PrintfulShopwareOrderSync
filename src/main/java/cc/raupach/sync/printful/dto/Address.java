package cc.raupach.sync.printful.dto;

import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Address {

    private String name;
    private String company;
    private String address1;
    private String address2;
    private String city;
    private String state_code;
    private String state_name;
    private String country_code;
    private String country_name;
    private String zip;
    private String phone;
    private String email;


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
