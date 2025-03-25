package vn.com.lcx.common.dto.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "OutObject", namespace = "http://www.openwaygroup.com/wsint")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CardDetailsAPIRecord {

    @JacksonXmlProperty(localName = "Institution")
    private String institution;

    @JacksonXmlProperty(localName = "Branch")
    private String branch;

    @JacksonXmlProperty(localName = "ClientCategory")
    private String clientCategory;

    @JacksonXmlProperty(localName = "ProductCategory")
    private String productCategory;

    @JacksonXmlProperty(localName = "ContractCategory")
    private String contractCategory;

    @JacksonXmlProperty(localName = "ParentProduct")
    private String parentProduct;

    @JacksonXmlProperty(localName = "CounterpartChannel")
    private String counterpartChannel;

    @JacksonXmlProperty(localName = "CardNumber")
    private String cardNumber;

    @JacksonXmlProperty(localName = "CardName")
    private String cardName;

    @JacksonXmlProperty(localName = "Product")
    private String product;

    @JacksonXmlProperty(localName = "DateOpen")
    private String dateOpen;

    @JacksonXmlProperty(localName = "ExpirationDate")
    private String expirationDate;

    @JacksonXmlProperty(localName = "Available")
    private String available;

    @JacksonXmlProperty(localName = "Currency")
    private String currency;

    @JacksonXmlProperty(localName = "Blocked")
    private String blocked;

    @JacksonXmlProperty(localName = "CreditLimit")
    private String creditLimit;

    @JacksonXmlProperty(localName = "AddLimit")
    private String addLimit;

    @JacksonXmlProperty(localName = "CBSNumber")
    private String cBSNumber;

    @JacksonXmlProperty(localName = "SequenceNumber")
    private String sequenceNumber;

    @JacksonXmlProperty(localName = "OrderReason")
    private String orderReason;

    @JacksonXmlProperty(localName = "AvailableProductionAction")
    private String availableProductionAction;

    @JacksonXmlProperty(localName = "ProductCode")
    private String productCode;

    @JacksonXmlProperty(localName = "ParentProductCode")
    private String parentProductCode;

    @JacksonXmlProperty(localName = "MaxPinAttempts")
    private String maxPinAttempts;

    @JacksonXmlProperty(localName = "PinAttemptsCounter")
    private String pinAttemptsCounter;

    @JacksonXmlProperty(localName = "RiskFactor")
    private String riskFactor;

    @JacksonXmlProperty(localName = "EmbossedFirstName")
    private String embossedFirstName;

    @JacksonXmlProperty(localName = "EmbossedLastName")
    private String embossedLastName;

    @JacksonXmlProperty(localName = "AddInfo01")
    private String addInfo01;

    @JacksonXmlProperty(localName = "CustomRules")
    private String customRules;

    @JacksonXmlProperty(localName = "Status")
    private String status;

    @JacksonXmlProperty(localName = "StatusCode")
    private String statusCode;

    @JacksonXmlProperty(localName = "ProductionStatus")
    private String productionStatus;

    @JacksonXmlProperty(localName = "Ready")
    private String ready;

    @JacksonXmlProperty(localName = "ExternalCode")
    private String externalCode;

    @JacksonXmlProperty(localName = "ID")
    private String id;

    @JacksonXmlProperty(localName = "AmendmentDate")
    private String amendmentDate;

    @JacksonXmlProperty(localName = "Client")
    private String client;

    @JacksonXmlProperty(localName = "Parent")
    private String parent;

    @JacksonXmlProperty(localName = "ServPack")
    private String servPack;

    @JacksonXmlProperty(localName = "AmendmentOfficer")
    private String amendmentOfficer;
}
