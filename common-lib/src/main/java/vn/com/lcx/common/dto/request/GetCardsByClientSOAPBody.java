package vn.com.lcx.common.dto.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetCardsByClientSOAPBody {

    @JacksonXmlProperty(localName = "GetCardsByClientV2", namespace = "http://www.openwaygroup.com/wsint")
    private GetCardsByClientV2 getCardsByClientV2;

}
