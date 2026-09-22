import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {
    
    // Get the message body as string
    def body = message.getBody(String.class)
    
    // Parse JSON
    def jsonSlurper = new JsonSlurper()
    def jsonData = jsonSlurper.parseText(body)
    
    // Initialize result list
    def processInfo = []

    // Navigate to process array
    def process = jsonData?."bpmn2:definitions"?."bpmn2:process"
    
    if (process) {
        def processes = process instanceof List ? process : [process]

        processes.each { proc ->
            def callActivity = proc?."bpmn2:callActivity"

            if (callActivity) {
                def callActivityList = callActivity instanceof List ? callActivity : [callActivity]

                callActivityList.each { detail -> 
                    def processDetail = [:]

                    def properties = detail?."bpmn2:extensionElements"?."ifl:property"

                    if (properties) {
                        def propList = properties instanceof List ? properties : [properties]
                        
                        // Create a map for easy property lookup
                        def propMap = [:]
                        propList.each { prop ->
                            propMap[prop.key] = prop.value
                        }
            
                        def componentType = propMap.activityType ?: ""
                        
                        if (componentType == "PgpEncrypt" || componentType == "PgpDecrypt") {
                            processDetail.componentType = componentType
                            processDetail."EncryptKey" = extractAllCellValues(propMap.PgpEncryptionUserIDs ?: "")
                            processDetail."SignerKey" = extractAllCellValues(propMap.PgpDecryptionSignerUserIDs ?: "")
                            processDetail."DecryptKey" = extractAllCellValues(propMap.PgpDecryptionUserIDs ?: "")
                        }
                        
                    }
                    if (!processDetail.isEmpty()) {
                        processInfo.add(processDetail)
                    }
                }
            }

        }
    }
    
    // Convert result to JSON
    def resultJson = JsonOutput.toJson(processInfo)
    
    // Set the result as property "ProcessList"
    message.setProperty("ProcessList", resultJson)
    
    return message
}

/**
 * Extracts all text values between <cell ...> and </cell> tags.
 * e.g. "<row><cell id='EncryptUserID'>test4</cell></row><row><cell id='EncryptUserID'>test2</cell></row>"
 * returns ["test4", "test2"]
 */
def List extractAllCellValues(String raw) {
    if (!raw) return []
    def results = []
    def matcher = raw =~ /<cell[^>]*>([^<]*)<\/cell>/
    while (matcher.find()) {
        results << matcher.group(1)
    }
    return results
}