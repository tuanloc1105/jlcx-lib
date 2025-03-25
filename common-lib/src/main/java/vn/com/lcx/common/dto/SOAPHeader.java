package vn.com.lcx.common.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@NoArgsConstructor
@Data
public class SOAPHeader {
    // Empty header as per your example
}
