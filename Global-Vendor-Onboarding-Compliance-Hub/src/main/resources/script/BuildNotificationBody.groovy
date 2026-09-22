import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def props = message.getProperties()
    def vendorId    = (props.get('vendorId')        ?: 'N/A').toString()
    def riskLevel   = (props.get('riskLevel')       ?: 'N/A').toString()
    def region      = (props.get('complianceRegion')?: 'N/A').toString()
    def auditAction = (props.get('auditAction')     ?: 'PROCESSED').toString()
    def riskScore   = (props.get('riskScore')       ?: 'N/A').toString()
    def timestamp   = new Date().format('yyyy-MM-dd HH:mm:ss z')
    def statusColor = (riskLevel == 'HIGH') ? '#c0392b' : '#27ae60'
    def html = ("<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;'>" +
        "<h2>Vendor Compliance Notification</h2>" +
        "<table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse;'>" +
        "<tr><th>Field</th><th>Value</th></tr>" +
        "<tr><td>Vendor ID</td><td>" + vendorId + "</td></tr>" +
        "<tr><td>Action</td><td>" + auditAction + "</td></tr>" +
        "<tr><td>Region</td><td>" + region + "</td></tr>" +
        "<tr><td>Risk Level</td><td style='color:" + statusColor + ";'>" + riskLevel + "</td></tr>" +
        "<tr><td>Risk Score</td><td>" + riskScore + "</td></tr>" +
        "<tr><td>Processed At</td><td>" + timestamp + "</td></tr>" +
        "</table></body></html>").toString()
    message.setBody(html)
    return message
}