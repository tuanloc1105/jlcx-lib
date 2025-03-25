package vn.com.lcx.common.dto.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "GetCardsByClientV2", namespace = "http://www.openwaygroup.com/wsint")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetCardsByClientV2 {

    @JacksonXmlProperty(localName = "ClientSearchMethod")
    private String clientSearchMethod;

    @JacksonXmlProperty(localName = "ClientIdentifier")
    private String clientIdentifier;

    @JacksonXmlProperty(localName = "UserInfo")
    private String userInfo;

}
