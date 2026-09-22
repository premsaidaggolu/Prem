import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput

def Message processData(Message message) {
    def props = message.getProperties()
    def vendorId    = (props.get('vendorId')        ?: 'UNKNOWN').toString()
    def riskLevel   = (props.get('riskLevel')       ?: 'PENDING').toString()
    def region      = (props.get('complianceRegion')?: 'UNKNOWN').toString()
    def auditAction = (props.get('auditAction')     ?: 'PROCESSED').toString()
    def riskScore   = (props.get('riskScore')       ?: '0').toString()
    def jmsPayload  = [eventType: 'VENDOR_COMPLIANCE_UPDATE', vendorId: vendorId,
        riskLevel: riskLevel, riskScore: riskScore, region: region, action: auditAction,
        timestamp: new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'"),
        source: 'Global-Vendor-Onboarding-Compliance-Hub']
    message.setBody(JsonOutput.toJson(jmsPayload).toString())
    message.setHeader('Content-Type', 'application/json')
    return message
}