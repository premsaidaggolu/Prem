import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.util.Properties
import java.io.StringReader

def Message processData(Message message) {
    
    // Get the message body as string
    def body = message.getBody(String.class)

    def extractSchedule1Value = { String text ->
        if (!text) {
            return ""
        }

        def matcher = text =~ /<row><cell>\s*schedule1\s*<\/cell><cell>(.*?)<\/cell><\/row>/
        if (matcher.find()) {
            return matcher.group(1).replace('&amp;', '&').trim()
        }

        return text.trim()
    }
    
    // Parse JSON
    def jsonSlurper = new JsonSlurper()
    def jsonData = jsonSlurper.parseText(body)

    // Extract timer scheduleKey from start event and resolve it from external parameters if needed
    def scheduleValue = "none"
    def startEvent = jsonData?."bpmn2:definitions"?."bpmn2:process"?."bpmn2:startEvent"
    def timerProps = startEvent?."bpmn2:timerEventDefinition"?."bpmn2:extensionElements"?."ifl:property"
    if (timerProps) {
        def timerPropList = timerProps instanceof List ? timerProps : [timerProps]
        def scheduleProp = timerPropList.find { it?.key == "scheduleKey" }
        def rawScheduleValue = scheduleProp?.value?.toString() ?: ""

        def parameterContent = message.getProperty("parameter")?.toString()
        if (rawScheduleValue ==~ /^\{\{[^}]+\}\}$/ && parameterContent) {
            def params = new Properties()
            params.load(new StringReader(parameterContent))

            def parameterName = rawScheduleValue.replaceAll(/^\{\{/, "").replaceAll(/\}\}$/, "").trim()
            scheduleValue = extractSchedule1Value(params.getProperty(parameterName, rawScheduleValue))
        } else {
            scheduleValue = extractSchedule1Value(rawScheduleValue)
        }

        if (!scheduleValue?.toString()?.trim()) {
            scheduleValue = "none"
        }
    }
    message.setProperty("Schedule", scheduleValue)
    
    // Initialize result list
    def messageFlowInfo = []
    
    // Navigate to messageFlow array
    def messageFlows = jsonData?."bpmn2:definitions"?."bpmn2:collaboration"?."bpmn2:messageFlow"
    
    if (messageFlows) {
        // Handle both single object and array
        def flows = messageFlows instanceof List ? messageFlows : [messageFlows]
        
        flows.each { flow ->
            def flowInfo = [:]
            
            // Get adapter properties
            def properties = flow?."bpmn2:extensionElements"?."ifl:property"
            
            if (properties) {
                def propList = properties instanceof List ? properties : [properties]
                
                // Create a map for easy property lookup
                def propMap = [:]
                propList.each { prop ->
                    propMap[prop.key] = prop.value?.toString() ?: ""
                }
    
                def adapterName = propMap.ComponentType ?: ""
                flowInfo.adapter = adapterName
                flowInfo.componentVersion = propMap.componentVersion ?: ""
                flowInfo.cmdVariantUri = propMap.cmdVariantUri ?: ""
                
                if (adapterName == "HTTP") {
                    def authMethod = propMap.authenticationMethod ?: propMap.senderAuthType ?: ""
                    flowInfo."authentication_method" = authMethod

                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def credentialInfo = propMap.credentialName ?: propMap.privateKeyAlias ?: ""
                    flowInfo."credential_name" = credentialInfo

                    def urlPath = propMap.httpAddressWithoutQuery ?: ""
                    flowInfo."endpoint" = urlPath
                }
                if (adapterName == "HCIOData") {
                    def messageProtocol = propMap.MessageProtocol ?: ""
                    flowInfo."HCIOData_version" = messageProtocol

                    def address = propMap.address ?: ""
                    flowInfo."endpoint" = address

                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction
                }
                if (adapterName == "AdvancedEventMesh") {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def host = propMap.host ?: ""
                    flowInfo."Host" = host

                    def destinationName = propMap.destinationName ?: ""
                    flowInfo."Destination_Name" = destinationName

                    def queueName = propMap.queueName ?: ""
                    flowInfo."Queue_Name" = queueName
                }
                if (adapterName == 'AmazonWebServices') {
                    def messageProtocol = propMap.MessageProtocol ?: ""
                    flowInfo."AmazonWebServices_version" = messageProtocol

                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def host = propMap.host ?: ""
                    flowInfo."Host" = host

                    def bucketName = propMap.bucketName ?: ""
                    flowInfo."Bucket_Name" = bucketName

                    def accountNumber = propMap.accountNumber ?: ""
                    flowInfo."Account_Number" = accountNumber

                    def queueName = propMap.queueName ?: ""
                    flowInfo."Queue_Name" = queueName

                    def topicName = propMap.topicName ?: ""
                    flowInfo."Topic_Name" = topicName
                }
                if (adapterName == 'AMQP') {
                    def messageProtocol = propMap.MessageProtocol ?: ""
                    flowInfo."AMQP_version" = messageProtocol

                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def host = propMap.host ?: ""
                    flowInfo."Host" = host

                    def port = propMap.port ?: ""
                    flowInfo."Port" = port

                    def credentialName = propMap.credentialName ?: ""
                    flowInfo."Credential_Name" = credentialName
                }
                if (adapterName == 'AS2') {
                    def messageProtocol = propMap.MessageProtocol ?: ""
                    flowInfo."AS2_version" = messageProtocol

                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def address = propMap.address ?: ""
                    flowInfo."Endpoint" = address
                    
                    def receipientURL = propMap.receipientURL ?: ""
                    flowInfo."Receipient_URL" = receipientURL
                }
                if (adapterName == 'AzureStorage') {
                    def messageProtocol = propMap.MessageProtocol ?: ""
                    flowInfo."AzureStorage_version" = messageProtocol

                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def accountName = propMap.senderAccountName ?: propMap.receiverAccountName ?: ""
                    flowInfo."Account_Name" = accountName

                    def shareName = propMap.senderShareName ?: propMap.receiverShareName ?: ""
                    flowInfo."Share_Name" = shareName

                    def containerName = propMap.senderContainerName ?: propMap.receiverContainerName ?: ""
                    flowInfo."Container_Name" = containerName

                    def queueName = propMap.senderQueueName ?: propMap.receiverQueueName ?: ""
                    flowInfo."Queue_Name" = queueName

                    def tableName = propMap.tableName ?: ""
                    flowInfo."Table_Name" = tableName

                    def blobPath = propMap.senderBlobPath ?: propMap.receiverBlobPath ?: ""
                    flowInfo."Blob_Path" = blobPath

                    def sasToken = propMap.sasToken ?: ""
                    flowInfo."SAS_Token" = sasToken

                    def accessKey = propMap.accessKey ?: ""
                    flowInfo."Access_Key" = accessKey

                    def dynamicKey = propMap.dynamicKey ?: ""
                    flowInfo."Dynamic_Key" = dynamicKey

                    def dynamicSasToken = propMap.dynamicSas ?: ""
                    flowInfo."Dynamic_SAS_Token" = dynamicSasToken
                }
                if (adapterName == 'DataStoreConsumer') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def storageName = propMap.storageName ?: ""
                    flowInfo."Storage_Name" = storageName
                }
                if (adapterName == 'FTP') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def host = propMap.host ?: ""
                    flowInfo."Host" = host

                    def credential_name = propMap.credential_name ?: ""
                    flowInfo."Credential_Name" = credential_name
                }
                if(adapterName == 'IDOC') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def address = propMap.address ?: ""
                    flowInfo."Address" = address
                }
                if(adapterName == 'JDBC') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def alias = propMap.alias ?: ""
                    flowInfo."JDBC_Data_Source_Alias" = alias
                }
                if(adapterName == 'JMS') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def queueName = propMap.QueueName_inbound ?: propMap.QueueName_outbound ?: ""
                    flowInfo."Queue_Name" = queueName
                }
                if(adapterName == 'Kafka') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def host = propMap.host ?: ""
                    flowInfo."Host" = host

                    def topic = propMap.topic ?: ""
                    flowInfo."Topic" = topic

                    def authentication = propMap.authentication ?: ""
                    flowInfo."Authentication" = authentication

                    def credential = propMap.credentialName ?: propMap.alias ?: ""
                    flowInfo."Credential" = credential
                }
                if(adapterName == 'Mail') {
                    def messageProtocol = propMap.MessageProtocol ?: ""
                    flowInfo."Mail_version" = messageProtocol

                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def server = propMap.server ?: ""
                    flowInfo."Address" = server

                    def locationId = propMap.locationId ?: ""
                    flowInfo."Location_ID" = locationId

                    def user = propMap.user ?: ""
                    flowInfo."Credential" = user
                }
                if(adapterName == 'ODataSender') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def edmxPath = propMap.edmxPath ?: ""
                    flowInfo."EDMX_Path" = edmxPath

                    def entitySet = propMap.entitySet ?: ""
                    flowInfo."Entity_Set" = entitySet

                    def authentication = propMap.authentication ?: ""
                    flowInfo."Authentication" = authentication

                    def clientCertificates = propMap.clientCertificates ?: ""
                    flowInfo."Client_Certificates" = clientCertificates
                }
                if(adapterName == 'PollingSFTP') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def host = propMap.host ?: ""
                    flowInfo."Address" = host

                    def authentication = propMap.authentication ?: ""
                    flowInfo."Authentication" = authentication

                    def credential_name = propMap.credential_name ?: ""
                    flowInfo."Credential_Name" = credential_name

                    def username = propMap.username ?: ""
                    flowInfo."Username" = username

                    def privateKeyAlias = propMap.privateKeyAlias ?: ""
                    flowInfo."Private_Key_Alias" = privateKeyAlias
                }
                if(adapterName == 'ProcessDirect') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def address = propMap.address ?: ""
                    flowInfo."Address" = address
                }          
                if(adapterName == 'RabbitMQ') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def host = propMap.host ?: ""
                    flowInfo."Host" = host

                    def port = propMap.port ?: ""
                    flowInfo."Port" = port

                    def locationId = propMap.locationId ?: ""
                    flowInfo."Location_ID" = locationId

                    def virtualHost = propMap.virtualHost ?: ""
                    flowInfo."Virtual_Host" = virtualHost

                    def authentication = propMap.senderAuthentication ?: propMap.receiverAuthentication ?:""
                    flowInfo."Authentication" = authentication

                    def credential = propMap.credential ?: ""
                    flowInfo."Credential_Name" = credential

                    def privateKey = propMap.privateKey ?: ""
                    flowInfo."Private_Key" = privateKey

                    def queueName = propMap.queueName ?: ""
                    flowInfo."Queue_Name" = queueName

                    def destinationName = propMap.destinationName ?: ""
                    flowInfo."Destination_Name" = destinationName
                }
                if(adapterName == 'RFC') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def destination = propMap.destination ?: ""
                    flowInfo."Destination" = destination
                }
                if(adapterName == 'Salesforce') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction
                    
                    def authentication = propMap.accountType ?: ""
                    flowInfo."Authentication" = authentication

                    if(authentication == 'oauthClientCredentials') {
                        def loginUrl = propMap.loginUrl ?: ""
                        flowInfo."Address" = loginUrl
                        
                        def basicCredentialName = propMap.basicCredentialName ?: ""
                        flowInfo."Basic_Credential" = basicCredentialName

                        def oauthCredentialName = propMap.oauthCredentialName ?: ""
                        flowInfo."OAuth_Credential" = oauthCredentialName

                    } else if (authentication == 'jwt') {
                        def audience = propMap.audience ?: ""
                        flowInfo."Audience" = audience

                        def subjectAlias = propMap.subjectAlias ?: ""
                        flowInfo."Subject_Alias" = subjectAlias
                        
                        def issuerAlias = propMap.issuerAlias ?: ""
                        flowInfo."Issuer_Alias" = issuerAlias

                        def keystoreAlias = propMap.keystoreAlias ?: ""
                        flowInfo."Keystore_Alias" = keystoreAlias
                    }
                    if(direction == 'Sender') {
                        def channelNamePushTopic = propMap.channelNamePushTopic ?: ""
                        flowInfo."Channel_Name" = channelNamePushTopic
                    }
                    
                }
                if(adapterName == 'SalesforcePubSub') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction
                    
                    def authentication = propMap.accountType ?: ""
                    flowInfo."Authentication" = authentication

                    def apiEndpoint = propMap.apiEndpoint ?: ""
                    flowInfo."Host" = apiEndpoint
                    
                    def apiPort = propMap.apiPort ?: ""
                    flowInfo."Port" = apiPort

                    def tenantId = propMap.tenantId ?: ""
                    flowInfo."Tenant_ID" = tenantId

                    if(authentication == 'oauthClientCredentials') {
                        def loginUrl = propMap.loginUrl ?: ""
                        flowInfo."Token_URL" = loginUrl
                        
                        def basicCredentialName = propMap.basicCredentialName ?: ""
                        flowInfo."Credential_Name" = basicCredentialName

                        def oauthCredentialName = propMap.oauthCredentialName ?: ""
                        flowInfo."OAuth_Credential_Name" = oauthCredentialName

                    } else if (authentication == 'jwt') {
                        def audience = propMap.audience ?: ""
                        flowInfo."Audience" = audience

                        def subjectAlias = propMap.subjectAlias ?: ""
                        flowInfo."Subject_Alias" = subjectAlias
                        
                        def issuerAlias = propMap.issuerAlias ?: ""
                        flowInfo."Issuer_Alias" = issuerAlias

                        def keystoreAlias = propMap.keystoreAlias ?: ""
                        flowInfo."Keystore_Alias" = keystoreAlias
                    }

                    def channelName = propMap.channelName ?: ""
                    flowInfo."Channel_Name" = channelName

                }
                if(adapterName == 'SMB') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def hostname = propMap.hostname ?: ""
                    flowInfo."Address" = hostname
                    
                    def share = propMap.share ?: ""
                    flowInfo."Share" = share
                    
                    def credentials = propMap.credentials ?: ""
                    flowInfo."Credential_Name" = credentials
                    
                    def directory = propMap.directory ?: ""
                    flowInfo."Directory" = directory
                    
                    def fileName = propMap.fileName ?: ""
                    flowInfo."FileName" = fileName
                }
                if(adapterName == 'Snowflake') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def authentication = propMap.authentication ?: ""
                    flowInfo."Authentication" = authentication
                    if (authentication == 'basic') {
                        def credentials = propMap.credentials ?: ""
                        flowInfo."Credential_Name" = credentials
                    } else if (authentication == 'keyPair') {
                        def user = propMap.user ?: ""
                        flowInfo."User" = user

                        def privateKeyAlias = propMap.privateKeyAlias ?: ""
                        flowInfo."Private_Key_Alias" = privateKeyAlias
                    } 
                    def accountName = propMap.accountName ?: ""
                    flowInfo."Address" = accountName

                    def database = propMap.database ?: ""
                    flowInfo."Database" = database

                    def schema = propMap.schema ?: ""
                    flowInfo."Schema" = schema

                    def warehouse = propMap.warehouse ?: ""
                    flowInfo."Warehouse" = warehouse
                }
                if(adapterName == 'SOAP') {
                    def messageProtocol = propMap.MessageProtocol ?: ""
                    flowInfo."SOAP_version" = messageProtocol

                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def address = propMap.address ?: ""
                    flowInfo."Address" = address

                    if(direction == 'Receiver') {
                        def authentication = propMap.authentication ?: ""
                        flowInfo."Authentication" = authentication
                        
                        def credentialName = propMap.credentialName ?: ""
                        flowInfo."Credential_Name" = credentialName

                        def privateKeyAlias = propMap.privateKeyAlias ?: ""
                        flowInfo."Private_Key_Alias" = privateKeyAlias
                    }
                }
                if(adapterName == 'SuccessFactors') {
                    def messageProtocol = propMap.MessageProtocol ?: ""
                    flowInfo."SuccessFactors_version" = messageProtocol

                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def address = propMap.address ?: ""
                    flowInfo."Address" = address

                    def resourcePath = propMap.resourcePath ?: propMap.urlSuffixSfOData ?: propMap.urlSuffixSOAP ?: ""
                    flowInfo."Address_Suffix" = resourcePath

                    def alias = propMap.alias ?: ""
                    flowInfo."Credential_Name" = alias
                }
                if(adapterName == 'XI') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def address = propMap.Address_inbound ?: propMap.Address ?: ""
                    flowInfo."Address" = address

                    def authentication = propMap.AuthenticationType ?: ""
                    flowInfo."Authentication" = authentication

                    if(authentication == 'BasicAuthentication') {
                        def BasicAuthCredentialName = propMap.BasicAuthCredentialName ?: ""
                        flowInfo."Credential_Name" = BasicAuthCredentialName
                    } else if (authentication == 'ClientCertificate') {
                        def ClientCertificateAlias = propMap.ClientCertificateAlias ?: ""
                        flowInfo."Private_Key_Alias" = ClientCertificateAlias
                    }
                }
                if(adapterName == 'ZCommonLogger') {
                    def direction = propMap.direction ?: ""
                    flowInfo."direction" = direction

                    def logTitle = propMap.logTitle ?: ""
                    flowInfo."Log_Title-SK" = logTitle

                    def logMessage = propMap.logMessage ?: ""
                    flowInfo."Log_Message-SK" = logMessage
                }
            }
            messageFlowInfo.add(flowInfo)
        }
    }
    
    // Convert result to JSON
    def resultJson = JsonOutput.toJson(messageFlowInfo)
    
    // Set the result as property "AdapterList"
    message.setProperty("AdapterList", resultJson)
    
    return message
}
