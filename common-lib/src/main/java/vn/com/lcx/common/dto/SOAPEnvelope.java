package vn.com.lcx.common.dto;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SOAPEnvelope<T> {

    @JacksonXmlProperty(localName = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private SOAPHeader header;

    @JacksonXmlProperty(localName = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private T body;

    // Getters and Setters
}
