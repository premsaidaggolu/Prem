import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def props = message.getProperties()
    def industryCode = (props.get('industryCode') ?: '').toString().trim()
    def countryCode  = (props.get('countryCode')  ?: '').toString().trim().toUpperCase()
    def regDateStr   = (props.get('registrationDate') ?: '').toString().trim()
    def thresholdStr = (props.get('riskScoreThreshold') ?: '70').toString().trim()
    int threshold    = thresholdStr.isInteger() ? thresholdStr.toInteger() : 70
    def industryWeights = ['CONSTRUCTION': 25, 'MINING': 30, 'CHEMICALS': 28, 'ARMS': 40,
        'FINANCIAL': 20, 'HEALTHCARE': 15, 'TECHNOLOGY': 10, 'RETAIL': 8,
        'LOGISTICS': 18, 'ENERGY': 22]
    int industryScore = industryWeights.get(industryCode.toUpperCase(), 12)
    def highRiskCountries = ['KP', 'IR', 'SY', 'CU', 'BY', 'MM', 'SS', 'SD'] as Set
    def medRiskCountries  = ['RU', 'CN', 'VN', 'NG', 'KH', 'PK', 'LY', 'YE'] as Set
    int countryScore
    if (highRiskCountries.contains(countryCode))     countryScore = 40
    else if (medRiskCountries.contains(countryCode)) countryScore = 20
    else                                              countryScore = 5
    int ageScore = 0
    if (regDateStr) {
        try {
            def fmt = new java.text.SimpleDateFormat('yyyy-MM-dd')
            def regDate = fmt.parse(regDateStr)
            long ageDays = (new Date().time - regDate.time) / (1000L * 60 * 60 * 24)
            if (ageDays < 30)       ageScore = 20
            else if (ageDays < 90)  ageScore = 12
            else if (ageDays < 365) ageScore = 6
            else                    ageScore = 0
        } catch (ignored) { ageScore = 10 }
    } else { ageScore = 10 }
    int totalScore = industryScore + countryScore + ageScore
    String riskLevel = (totalScore > threshold) ? 'HIGH' : 'LOW'
    message.setProperty('riskScore', totalScore.toString())
    message.setProperty('riskLevel', riskLevel)
    message.setProperty('riskScoreBreakdown', "industry=${industryScore},country=${countryScore},age=${ageScore},total=${totalScore},threshold=${threshold}".toString())
    return message
}