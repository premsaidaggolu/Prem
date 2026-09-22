// Remove all attachments from the message
import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def body = message.getBody(String)
    // Remove the last comma before a closing curly or square bracket
    body = body.replaceFirst(/,\s*$/, "")
    message.setBody(body)
    return message
}