import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def props       = message.getProperties()
    def vendorId    = (props.get('vendorId')     ?: 'N/A').toString()
    def errMsg      = (props.get('errorMessage') ?: 'See message processing log for details').toString()
    def auditAction = (props.get('auditAction')  ?: 'UNKNOWN').toString()
    def timestamp   = new Date().format('yyyy-MM-dd HH:mm:ss z')
    def html = ("<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;'>" +
        "<h2 style='color:#c0392b;'>SAP Integration Error Alert</h2>" +
        "<p><strong>Integration:</strong> Global Vendor Onboarding and Compliance Hub</p>" +
        "<table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse;'>" +
        "<tr><th>Field</th><th>Value</th></tr>" +
        "<tr><td>Vendor ID</td><td>" + vendorId + "</td></tr>" +
        "<tr><td>Action</td><td>" + auditAction + "</td></tr>" +
        "<tr><td>Error</td><td style='color:#c0392b;'>" + errMsg + "</td></tr>" +
        "<tr><td>Detected At</td><td>" + timestamp + "</td></tr>" +
        "</table></body></html>").toString()
    message.setBody(html)
    return message
}