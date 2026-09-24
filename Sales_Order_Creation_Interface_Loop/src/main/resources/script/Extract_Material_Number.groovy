import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def body = message.getBody(String) as String;
    def xml = new XmlSlurper().parseText(body);
    def material = xml.to_Item.Item[0].Material.text();
    message.setProperty('MaterialNumber', material);
    return message;
}