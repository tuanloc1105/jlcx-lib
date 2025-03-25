package vn.com.lcx.common.dto.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "GetCardsByClientV2Result", namespace = "http://www.openwaygroup.com/wsint")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetCardsByClientV2Result {
    @JacksonXmlProperty(localName = "RetCode")
    private String retCode;

    @JacksonXmlProperty(localName = "RetMsg")
    private String retMsg;

    @JacksonXmlProperty(localName = "ResultInfo")
    private String resultInfo;

    @JacksonXmlProperty(localName = "OutObject")
    private GetCardsByClientV2ResponseOutObject outObject;
}
