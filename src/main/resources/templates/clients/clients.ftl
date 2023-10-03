<#import "../navigation/navigation.ftl" as navigation>
<html lang="de">
<head>
    <title>Deliverylayer | Clients</title>
    <link href="css/clients.css" rel="stylesheet">
</head>


<#macro content>
    <style>
        <#include "css/clients.css">
    </style>
    <div class="client-container">
        <#list clients as client>
            <div class="client <#if client.active == false>offline</#if>">
                <div class="client-name">${client.clientId}</div>
                <div class="status-area">
                    <div class="client-status"><#if client.active>Online <#else> Offline </#if></div>
                    <div class="status-indicator"></div>
                </div>
                <a href="/deliverylayer/ui/clients/client/${client.clientId}" class="client-configuration-button">View Client Configuration</a>
            </div>
        </#list>
    </div>
</#macro>


<@navigation.navbar activeBarItem=3 />
<@content/>

</html>