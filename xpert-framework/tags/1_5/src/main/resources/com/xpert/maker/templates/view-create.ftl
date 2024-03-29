<ui:composition  xmlns="http://www.w3.org/1999/xhtml"
                 xmlns:h="http://java.sun.com/jsf/html"
                 xmlns:f="http://java.sun.com/jsf/core"
                 xmlns:ui="http://java.sun.com/jsf/facelets"
                 xmlns:p="http://primefaces.org/ui"
                 template="${template}"
                 xmlns:x="http://xpert.com/faces"
                 xmlns:xc="http://java.sun.com/jsf/composite/xpert/components">
   <ui:param name="title" value="${sharp}{${resourceBundle}['${entity.nameLower}.create']}" />
   <ui:define name="body">
       <ui:include src="menu${entity.name}.xhtml" />
       <ui:include src="formCreate${entity.name}.xhtml" />
   </ui:define>
</ui:composition>