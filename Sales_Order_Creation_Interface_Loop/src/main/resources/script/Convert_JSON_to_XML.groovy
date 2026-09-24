import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper;
import groovy.xml.MarkupBuilder;

def Message processData(Message message) {
    def body = message.getBody(String) as String;
    def json = new JsonSlurper().parseText(body);

    def writer = new StringWriter();
    def xml = new MarkupBuilder(writer);

    xml.SalesOrder {
        SalesOrderType(json.SalesOrderType ?: '')
        SalesOrganization(json.SalesOrganization ?: '')
        DistributionChannel(json.DistributionChannel ?: '')
        OrganizationDivision(json.OrganizationDivision ?: '')
        PurchaseOrderByCustomer(json.PurchaseOrderByCustomer ?: '')
        SoldToParty(json.SoldToParty ?: '')
        to_Item {
            (json.to_Item ?: []).each { item ->
                Item {
                    SalesOrderItem(item.SalesOrderItem ?: '')
                    Material(item.Material ?: '')
                    RequestedQuantity(item.RequestedQuantity ?: '')
                    RequestedQuantityUnit(item.RequestedQuantityUnit ?: '')
                }
            }
        }
    }

    message.setBody(writer.toString());
    message.setHeader('Content-Type', 'application/xml');
    return message;
}