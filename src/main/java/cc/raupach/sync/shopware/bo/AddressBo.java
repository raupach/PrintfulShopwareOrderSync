package cc.raupach.sync.shopware.bo;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
@Builder
public class AddressBo {

  private String addressId;
  private String title;
  private String firstName;
  private String lastName;
  private String street;
  private String city;
  private String stateCode;
  private String countryCode;
  private String countryName;
  private String zipcode;
  private String additionalAddressLine1;
  private String additionalAddressLine2;
  private String company;
  private String phoneNumber;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
