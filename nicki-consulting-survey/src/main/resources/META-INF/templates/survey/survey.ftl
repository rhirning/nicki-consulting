<?xml version="1.0" encoding="iso-8859-1" standalone="yes"?>
<simpleTemplate name="document">
	<data><![CDATA[
<document>
        <styles>
                <style name="right" align="RIGHT"/>
                <style name="left" align="LEFT"/>
                <style name="center" align="CENTER" vertical-align="CENTER"/>
                <style name="bottom" vertical-align="BOTTOM"/>
                <style name="top" vertical-align="TOP"/>
        </styles>
	<pages>
		<page name="${survey.name}">
			<table x="1" y="1">
				<row>
					<column><text>Thema</text></column>
					<column><text>Bewertung</text></column>
					<#list helper.getChoices(survey) as choice>
						<column><text>${choice.name!}</text></column>
					</#list>
					<column><text>Kommentare</text></column>
				</row>
				<row>
					<column><text></text></column>
					<column><text></text></column>
					<#list helper.getChoices(survey) as choice>
						<column><text>${choice.description!}</text></column>
					</#list>
				</row>
				<#list topicWrappers as topic>
					<row>
						<column><text>${topic.name!}</text></column>
						<column><text>${topic.value!}</text></column>
						<#list helper.getChoices(survey) as choice>
							<column><text>${helper.countVotes(topic.topic, choice)!}</text></column>
						</#list>
						<#if helper.getComments(topic.topic)??>
							<column><text>${dataHelper.getAsString(helper.getComments(topic.topic), "\\n")}</text></column>	
						</#if>					
					</row>
				</#list>
			</table>
		</page>
	</pages>
</document>
	]]></data>
</simpleTemplate>
