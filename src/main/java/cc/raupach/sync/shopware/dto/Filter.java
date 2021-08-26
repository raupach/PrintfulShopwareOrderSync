package cc.raupach.sync.shopware.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
@Builder
public class Filter {

    private FilterType type;
    private String field;
    private String value;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
