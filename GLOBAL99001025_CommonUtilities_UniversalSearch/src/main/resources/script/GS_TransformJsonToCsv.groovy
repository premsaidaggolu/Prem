import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {
    def body = message.getBody(String.class)
    def jsonSlurper = new JsonSlurper()
    def jsonData = jsonSlurper.parseText(body)

    def environment = jsonData?.results?.environment ?: ""
    def lastUpdate = jsonData?.results?.last_update ?: ""

    def headers = "PackageName,environment,iFlowName,iFlowVersion,ModifiedBy,CreatedBy,last_update,Schedule,Adapter,AdapterList,ProcessList"
    def sb = new StringBuilder()
    sb.append(headers).append("\n")

    def packages = jsonData?.results?.Packages
    if (packages) {
        packages.each { pkg ->
            def packageName = pkg?.PackageName ?: ""
            def iFlows = pkg?.iFlows

            // Skip if iFlows is "Not iFlow" or null
            if (iFlows == null || iFlows instanceof String) {
                // Output one row with no iFlow data
                sb.append(escCsv(packageName)).append(",")
                sb.append(escCsv(environment)).append(",")
                sb.append(",") // iFlowName
                sb.append(",") // iFlowVersion
                sb.append(",") // ModifiedBy
                sb.append(",") // CreatedBy
                sb.append(escCsv(lastUpdate)).append(",")
                sb.append("none").append(",") // Schedule
                sb.append(",") // Adapter
                sb.append(",") // AdapterList
                sb.append("")  // ProcessList
                sb.append("\n")
                return
            }

            def flowList = iFlows instanceof List ? iFlows : [iFlows]

            flowList.each { iflow ->
                def iFlowName = iflow?.iFlowName ?: ""
                def iFlowVersion = iflow?.iFlowVersion ?: ""
                def modifiedBy = (iflow?.ModifiedBy ?: iflow?.modifiedBy ?: "").toString().trim()
                def createdBy = (iflow?.CreatedBy ?: iflow?.createdBy ?: "").toString().trim()
                def schedule = (iflow?.Schedule ?: iflow?.schedule ?: "none").toString().trim()
                if (!schedule) {
                    schedule = "none"
                }

                // Convert ProcessList to JSON string
                def processList = iflow?.ProcessList
                def processStr = ""
                if (processList != null && !(processList instanceof List && processList.isEmpty())) {
                    processStr = JsonOutput.toJson(processList)
                }

                // Handle AdapterList - one row per adapter
                def adapterList = iflow?.AdapterList
                def aList = []
                if (adapterList != null && !(adapterList instanceof List && adapterList.isEmpty())) {
                    aList = adapterList instanceof List ? adapterList : [adapterList]
                }

                if (aList.size() > 0) {
                    aList.each { adapter ->
                        def adapterName = adapter?.adapter ?: ""
                        def adapterStr = JsonOutput.toJson(adapter)

                        sb.append(escCsv(packageName)).append(",")
                        sb.append(escCsv(environment)).append(",")
                        sb.append(escCsv(iFlowName)).append(",")
                        sb.append(escCsv(iFlowVersion)).append(",")
                        sb.append(escCsv(modifiedBy)).append(",")
                        sb.append(escCsv(createdBy)).append(",")
                        sb.append(escCsv(lastUpdate)).append(",")
                        sb.append(escCsv(schedule)).append(",")
                        sb.append(escCsv(adapterName)).append(",")
                        sb.append(escCsv(adapterStr)).append(",")
                        sb.append(escCsv(processStr))
                        sb.append("\n")
                    }
                } else {
                    // No adapter - output one row
                    sb.append(escCsv(packageName)).append(",")
                    sb.append(escCsv(environment)).append(",")
                    sb.append(escCsv(iFlowName)).append(",")
                    sb.append(escCsv(iFlowVersion)).append(",")
                    sb.append(escCsv(modifiedBy)).append(",")
                    sb.append(escCsv(createdBy)).append(",")
                    sb.append(escCsv(lastUpdate)).append(",")
                    sb.append(escCsv(schedule)).append(",")
                    sb.append(",") // Adapter
                    sb.append(",") // AdapterList
                    sb.append(escCsv(processStr))
                    sb.append("\n")
                }
            }
        }
    }

    message.setBody(sb.toString())
    return message
}

def String escCsv(String val) {
    if (!val) return ""
    if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
        return "\"" + val.replace("\"", "\"\"") + "\""
    }
    return val
}