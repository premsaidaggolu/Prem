import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    // Get the value of the environment variable "TENANT_NAME"
    String tenantName = System.getenv("TENANT_NAME").toString();
    String systemID = System.getenv("IT_SYSTEM_ID").toString();
    String uxDomain = System.getenv("IT_TENANT_UX_DOMAIN").toString();
    String tenantURL = "https://${tenantName}.${systemID}.${uxDomain}";
    String authURL = "https://${tenantName}.authentication.${uxDomain.replace('cfapps.','')}";

    // Get the value of a custom environment variable (e.g., defined in the iFlow's properties)
    //def customVariable = System.getenv("MY_CUSTOM_VARIABLE");

    message.setProperty('TENANT_NAME',tenantName);
    message.setProperty('IT_SYSTEM_ID',systemID);
    message.setProperty('IT_TENANT_UX_DOMAIN',uxDomain);
    message.setProperty('TENANT_URL',tenantURL);
    message.setProperty('AUTH_URL',authURL);

    return message;
}