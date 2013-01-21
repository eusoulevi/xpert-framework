<ui:composition  xmlns="http://www.w3.org/1999/xhtml"
                 xmlns:h="http://java.sun.com/jsf/html"
                 xmlns:f="http://java.sun.com/jsf/core"
                 xmlns:ui="http://java.sun.com/jsf/facelets"
                 xmlns:p="http://primefaces.org/ui"
                 xmlns:x="http://xpert.com/faces"
                 xmlns:custom="http://java.sun.com/jsf/composite/components"
                 xmlns:xc="http://java.sun.com/jsf/composite/xpert/components">
    <h:form id="formDetail${entity.name}">
        <h:panelGrid columns="4" styleClass="grid-detail">
        <#list entity.fields as field>
            <#if field.collection == false && field.id == false>
           
            <h:outputLabel value="${sharp}{${resourceBundle}['${entity.nameLower}.${field.name}']}:" />
            <#if field.lazy == true>
            <h:outputText value="${sharp}{${entity.nameLower}MB.entity.${field.name}}">
                <x:initializer/>
            </h:outputText>
            </#if>
            <#if field.decimal == true>
            <h:outputText value="${sharp}{${entity.nameLower}MB.entity.${field.name}}">
                <f:convertNumber />
            </h:outputText>
            </#if>
            <#if field.date == true>
            <h:outputText value="${sharp}{${entity.nameLower}MB.entity.${field.name}}">
                <f:convertDateTime />
            </h:outputText>
            </#if>
            <#if field.yesNo == true>
            <h:outputText value="${sharp}{${entity.nameLower}MB.entity.${field.name}}" converter ="yesNoConverter" />
            </#if>
            <#if field.lazy == false && field.decimal == false && field.date == false && field.yesNo == false>
            <h:outputText value="${sharp}{${entity.nameLower}MB.entity.${field.name}}"/>
            </#if>
            </#if>
        </#list>

        </h:panelGrid>
        <p:separator/>
        <div style="text-align: center;">
            <p:commandButton type="button" value="${sharp}{xmsg['close']}" onclick="widget${entity.name}Detail.hide()" />
            <x:securityArea rolesAllowed="${entity.nameLower}.audit">
                <xc:audit for="${sharp}{${entity.nameLower}MB.entity}"/>
            </x:securityArea>
        </div>
    </h:form>
</ui:composition>