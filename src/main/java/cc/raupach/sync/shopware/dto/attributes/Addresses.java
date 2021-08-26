package cc.raupach.sync.shopware.dto.attributes;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
public class Addresses {

    private String countryId;
    private String orderId;
    private String salutationId;
    private String firstName;
    private String lastName;
    private String street;
    private String zipcode;
    private String city;
    private String company;
    private String department;
    private String title;
    private String phoneNumber;
    private String additionalAddressLine1;
    private String additionalAddressLine2;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
