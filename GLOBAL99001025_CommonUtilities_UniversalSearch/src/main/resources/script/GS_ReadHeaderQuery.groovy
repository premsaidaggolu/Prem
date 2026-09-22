import com.sap.gateway.ip.core.customdev.util.Message
import java.net.URLDecoder

def Message processData(Message message) {
    
    // Read CamelHttpQuery header
    // Expected format: iflowid=YYYY
    def query = message.getHeader("CamelHttpQuery", String.class)
    
    if (query) {
        // URL-decode the query string
        def decoded = URLDecoder.decode(query, "UTF-8")
        
        // Parse query parameters into a map
        def params = [:]
        decoded.split("&").each { pair ->
            def keyValue = pair.split("=", 2)
            if (keyValue.length == 2) {
                params[keyValue[0].trim()] = keyValue[1].trim()
            }
        }
        
        // Set query as property
        if (params.format) {
            message.setProperty("format", params.format)
        }
        
        // Remove CamelHttpQuery header
        message.getHeaders().remove("CamelHttpQuery")
        
    }
    
    return message
}
