import com.sap.gateway.ip.core.customdev.util.Message
import groovy.xml.XmlSlurper

def Message processData(Message message) {
    def props         = message.getProperties()
    def retentionDays = (props.get('Audit Log Retention Days (e.g. 90)') ?: '90').toString().toInteger()
    long cutoffMs     = new Date().time - (retentionDays * 24L * 60 * 60 * 1000)
    int purgedCount   = 0
    try {
        def bodyStr = message.getBody(java.lang.String)
        if (bodyStr && bodyStr.trim().startsWith('<')) {
            def root = new XmlSlurper().parseText(bodyStr)
            root.entry.each { entry -> purgedCount++ }
        }
    } catch (ignored) {}
    message.setProperty('auditEntriesPurged', purgedCount.toString())
    message.setProperty('auditPurgeCutoffDate', new Date(cutoffMs).format('yyyy-MM-dd'))
    message.setBody(("{\"purgedCount\":" + purgedCount + ",\"cutoffDate\":\"" + new Date(cutoffMs).format('yyyy-MM-dd') + "\"}").toString())
    return message
}