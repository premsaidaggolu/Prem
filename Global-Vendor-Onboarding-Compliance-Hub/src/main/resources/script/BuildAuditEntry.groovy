import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def props = message.getProperties()
    def vendorId    = (props.get('vendorId')        ?: 'UNKNOWN').toString()
    def auditAction = (props.get('auditAction')     ?: 'PROCESSED').toString()
    def riskLevel   = (props.get('riskLevel')       ?: 'UNKNOWN').toString()
    def riskScore   = (props.get('riskScore')       ?: '0').toString()
    def region      = (props.get('complianceRegion')?: 'UNKNOWN').toString()
    def breakdown   = (props.get('riskScoreBreakdown') ?: '').toString()
    def timestamp   = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
    def entryId     = (vendorId + '_' + System.currentTimeMillis()).toString()
    def xml = ("<?xml version='1.0' encoding='UTF-8'?><auditEntry>" +
        "<entryId>" + entryId + "</entryId>" +
        "<vendorId>" + vendorId + "</vendorId>" +
        "<action>" + auditAction + "</action>" +
        "<timestamp>" + timestamp + "</timestamp>" +
        "<riskLevel>" + riskLevel + "</riskLevel>" +
        "<riskScore>" + riskScore + "</riskScore>" +
        "<riskScoreBreakdown>" + breakdown + "</riskScoreBreakdown>" +
        "<complianceRegion>" + region + "</complianceRegion>" +
        "<source>Global-Vendor-Onboarding-Compliance-Hub</source></auditEntry>").toString()
    message.setBody(xml)
    message.setProperty('auditEntryId', entryId)
    return message
}