import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import java.util.Properties
import java.io.StringReader

def Message processData(Message message) {
    
    // Get the message body as string
    def body = message.getBody(String.class)
    
    // Get the parameter property (should contain parameters.prop content)
    def parameterContent = message.getProperty("parameter")
    
    // Parse the parameters.prop content into a Properties object
    def paramMap = [:]
    if (parameterContent != null && !parameterContent.isEmpty()) {
        try {
            Properties props = new Properties()
            props.load(new StringReader(parameterContent))
            
            // Convert Properties to Map for easier access
            props.each { key, value ->
                paramMap[key.toString()] = value.toString()
            }
        } catch (Exception e) {
            message.setProperty("ParameterParseError", e.getMessage())
        }
    }
    
    // Replace all {{parameter}} patterns in the body
    def updatedBody = body
    def pattern = ~/\{\{([^}]+)\}\}/
    
    updatedBody = updatedBody.replaceAll(pattern) { match ->
        def paramName = match[1].trim()
        
        // Check if the parameter exists in paramMap
        if (paramMap.containsKey(paramName)) {
            // Escape special JSON characters in the value
            def val = paramMap[paramName]
            val = val.replace('\\', '\\\\')
            val = val.replace('"', '\\"')
            val = val.replace('\n', '\\n')
            val = val.replace('\r', '\\r')
            val = val.replace('\t', '\\t')
            return val
        } else {
            // If not found, keep the original {{parameter}} format
            return match[0]
        }
    }
    
    // Set the updated body back to the message
    message.setBody(updatedBody)
    
    // Set property to track how many replacements were made
    def replacementCount = 0
    body.findAll(pattern) { match ->
        def paramName = match[1].trim()
        if (paramMap.containsKey(paramName)) {
            replacementCount++
        }
    }
    message.setProperty("ReplacementCount", replacementCount.toString())
    
    return message
}
