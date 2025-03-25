package vn.com.lcx.common.dto.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "GetCardsByClientV2Response", namespace = "http://www.openwaygroup.com/wsint")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetCardsByClientV2Response {

    @JacksonXmlProperty(localName = "GetCardsByClientV2Result")
    private GetCardsByClientV2Result getCardsByClientV2Result;

}
