import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def props       = message.getProperties()
    int retryCount  = (props.get('retryCount')  ?: '0').toString().toInteger()
    int maxRetries  = (props.get('maxRetries')  ?: '3').toString().toInteger()
    int baseDelayMs = (props.get('baseDelayMs') ?: '100').toString().toInteger()
    if (retryCount > 0) {
        long delayMs = Math.min(baseDelayMs * (long) Math.pow(2, retryCount - 1), 10000L)
        try { Thread.sleep(delayMs) } catch (ignored) {}
    }
    message.setProperty('retryCount', (retryCount + 1).toString())
    if (retryCount >= maxRetries) {
        throw new Exception("Bank Validation API failed after ${maxRetries} retries.".toString())
    }
    return message
}