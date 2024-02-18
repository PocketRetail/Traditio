<#import "../navigation/navigation.ftl" as navigation>

<html lang="de">
<head>
    <title>Deliverylayer | Client</title>
    <script src="https://kit.fontawesome.com/7b66b03a12.js" crossorigin="anonymous"></script>
</head>
<style>
    <#include "css/client.css">
</style>
<body style="margin-left:100px">
<@navigation.navbar activeBarItem=3 />
<div class="client-details<#if !client.active>offline</#if>">
    <div class="client-details-headline">
        <h1>Client Details</h1>
        <div class="status-indicator ${client.active?string('online', 'offline')}">
            <span class="tooltiptext">${client.active?string("Aktiv", "Inaktiv")}</span>
        </div>
    </div>
    <form action="/client/${client.clientId}" method="post">
        <label>Client ID: ${client.clientId}</label>
        <label>Client Description: <input type="text" name="description"
                                          value="<#if client.clientDescription??>${client.clientDescription}</#if>"></label>
        <label>Client URI: ${client.clientURI}</label>
        <button type="submit">Update Description</button>
    </form>
</div>
<div class="client-requests-container">
    <div style="display: flex; align-items: center;">
        <h1>Client Requests</h1>
        <i class="fa-solid fa-arrows-rotate" style="height: 11px;margin-left: 10px"></i>
    </div>
    <div class="client-requests">
        <#list clientRequests as request>
            <div class="request">
                <div class="request-details">
                    <p>Request ID: ${request.clientRequestId}</p>
                    <p>Request Type: ${request.clientRequestType}</p>
                    <p>Request URI: ${request.clientRequestURI}</p>
                    <p>Request Name: ${request.clientRequestName}</p>
                </div>
                <div class="chart-container">
                    <!-- Das Diagramm wird hier eingefügt -->
                </div>
                <i class="fa-solid fa-circle-info"></i>
            </div>
        </#list>
    </div>
</div>
</body>
</html>