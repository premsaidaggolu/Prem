import com.sap.gateway.ip.core.customdev.util.Message
import groovy.xml.XmlSlurper
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

def Message processData(Message message) {
    def root = new XmlSlurper().parse(message.getBody(java.io.Reader))
    def validRecords = []
    def invalidCount = 0
    root.'bankUpdate'.each { update ->
        def iban     = update.iban?.text()?.trim()     ?: ''
        def bankCode = update.bankCode?.text()?.trim() ?: ''
        boolean ibanOk     = validateIBAN(iban)
        boolean bankCodeOk = bankCode ==~ /^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$/ || bankCode ==~ /^[0-9]{6,9}$/
        if (ibanOk && bankCodeOk) validRecords << update
        else invalidCount++
    }
    message.setProperty('invalidBankRecordCount', invalidCount.toString())
    message.setProperty('validBankRecordCount', validRecords.size().toString())
    def builder = new StreamingMarkupBuilder()
    builder.encoding = 'UTF-8'
    def xml = builder.bind { bankUpdates { validRecords.each { rec -> mkp.yield rec } } }
    message.setBody(XmlUtil.serialize(xml))
    return message
}
boolean validateIBAN(String iban) {
    if (!iban || iban.length() < 15 || iban.length() > 34) return false
    String cleaned = iban.replaceAll(/\s/, '').toUpperCase()
    String rearranged = cleaned.substring(4) + cleaned.substring(0, 4)
    String numeric = rearranged.collect { ch -> ch.isLetter() ? (ch as char) - ('A' as char) + 10 : ch }.join('')
    try { return new BigInteger(numeric) % 97 == 1 } catch (ignored) { return false }
}