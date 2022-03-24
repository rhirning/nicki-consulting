<?xml version="1.0" encoding="iso-8859-1" standalone="yes"?>
<simpleTemplate name="mobile">
	<data>${confirmationCode}</data>
    <part>
 
        <entry>
            <key>subject</key>
            <value>Neue Umfragethemen</value>
        </entry>
        <entry>
            <key>body</key>
            <value><![CDATA[Hallo ${person.name},<br/><br/>
<#if notifies??>
<#list notifies as notify>
es gibt neue Umfragethemen zur Umfrage <b>${helper.getSurvey(notify.surveyId).name}</b><br/><br/>
</#list>
</#if>
]]>
</value>
        </entry>
        <entry>
        	<key>pdf</key>
        	<value><![CDATA[<?xml version="1.0" encoding="UTF-8"?>
				<document size="A4">
					<pages>
						<#include "Wlan/start-page.ftl">
						<#include "Wlan/page.ftl">
					</pages>
					<data>
						<#include "Wlan/ticketWithoutPassword.ftl">
						<#include "Wlan/disclaimer.ftl">
					</data>
				</document>
			]]>
			</value>
        </entry>
    </part>
</simpleTemplate>