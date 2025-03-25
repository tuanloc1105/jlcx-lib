package vn.com.lcx.common.dto.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JacksonXmlRootElement(localName = "OutObject", namespace = "http://www.openwaygroup.com/wsint")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetCardsByClientV2ResponseOutObject {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "CardDetailsAPIRecord")
    private List<CardDetailsAPIRecord> cardDetailsAPIRecord;

}
