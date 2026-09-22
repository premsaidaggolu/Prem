import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def body = message.getBody(String)
    // Replace [<?xml version="1.0" encoding="UTF-8"?><results></results> ] with "Not Iflow"
    body = body.replaceAll(/\[\s*<\?xml version="1\.0" encoding="UTF-8"\?>\s*<results><\/results>\s*\]/, '"Not iFlow"')
    message.setBody(body)
    return message
}