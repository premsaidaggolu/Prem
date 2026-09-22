import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def props = message.getProperties()
    def vendorId    = (props.get('vendorId')           ?: 'UNKNOWN').toString()
    def countryCode = (props.get('countryCode')        ?: 'N/A').toString()
    def industryCode= (props.get('industryCode')       ?: 'N/A').toString()
    def riskScore   = (props.get('riskScore')          ?: 'N/A').toString()
    def breakdown   = (props.get('riskScoreBreakdown') ?: 'N/A').toString()
    def ticketCount = (props.get('openTicketCount')    ?: '0').toString()
    def region      = (props.get('complianceRegion')   ?: 'N/A').toString()
    def timestamp   = new Date().format('yyyy-MM-dd HH:mm:ss z')
    def html = ("<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;'>" +
        "<h2 style='color:#c0392b;'>High-Risk Vendor Compliance Alert</h2>" +
        "<table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse;'>" +
        "<tr><th>Field</th><th>Value</th></tr>" +
        "<tr><td>Vendor ID</td><td>" + vendorId + "</td></tr>" +
        "<tr><td>Country</td><td>" + countryCode + "</td></tr>" +
        "<tr><td>Industry</td><td>" + industryCode + "</td></tr>" +
        "<tr><td>Compliance Region</td><td>" + region + "</td></tr>" +
        "<tr><td>Risk Score</td><td>" + riskScore + "</td></tr>" +
        "<tr><td>Score Breakdown</td><td>" + breakdown + "</td></tr>" +
        "<tr><td>Open Tickets</td><td>" + ticketCount + "</td></tr>" +
        "<tr><td>Detected At</td><td>" + timestamp + "</td></tr>" +
        "</table></body></html>").toString()
    message.setBody(html)
    return message
}