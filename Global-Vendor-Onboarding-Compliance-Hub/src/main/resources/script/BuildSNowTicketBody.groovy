import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput

def Message processData(Message message) {
    def props = message.getProperties()
    def vendorId    = (props.get('vendorId')       ?: 'UNKNOWN').toString()
    def countryCode = (props.get('countryCode')    ?: '').toString()
    def industryCode= (props.get('industryCode')   ?: '').toString()
    def riskScore   = (props.get('riskScore')      ?: '').toString()
    def ticketCount = (props.get('openTicketCount')?: '0').toString()
    def payload = [short_description: "High-Risk Vendor Review Required: ${vendorId}".toString(),
        description: "Vendor ${vendorId} (Country: ${countryCode}, Industry: ${industryCode}) scored ${riskScore}. Open compliance tickets: ${ticketCount}.".toString(),
        category: 'vendor_compliance', priority: '2', impact: '2', urgency: '2']
    message.setBody(JsonOutput.toJson(payload).toString())
    message.setHeader('Content-Type', 'application/json')
    return message
}