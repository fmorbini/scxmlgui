package com.mxgraph.examples.swing.editor.utils;


import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.mxgraph.util.mxUtils;

public class XMLUtils {
	
	public static String escapeStringForXML(String aText){
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character =  iterator.current();
		while (character != CharacterIterator.DONE ){
			if (character == '<') {
				result.append("&lt;");
			}
			else if (character == '>') {
				result.append("&gt;");
			}
			else if (character == '\"') {
				result.append("&quot;");
			}
			else if (character == '\'') {
				result.append("&#039;");
			}
			else if (character == '&') {
				result.append("&amp;");
			}
			else {
				//the char is not a special one
				//add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}
	
	public static String domNode2String(Node node,boolean escapeStrings)  {
		String ret="";
		switch (node.getNodeType()) {

		case Node.DOCUMENT_NODE:
			// recurse on each child
			NodeList nodes = node.getChildNodes();
			if (nodes != null) {
				for (int i=0; i<nodes.getLength(); i++) {
					ret+=domNode2String(nodes.item(i),escapeStrings);
				}
			}
			break;

		case Node.ELEMENT_NODE:
			String name = node.getNodeName();
			ret+="<" + name;
			NamedNodeMap attributes = node.getAttributes();
			for (int i=0; i<attributes.getLength(); i++) {
				Node current = attributes.item(i);
				ret+=" " + current.getNodeName() +"=\"" + ((escapeStrings)?escapeStringForXML(current.getNodeValue()):current.getNodeValue())+"\"";
			}
			ret+=">";

			// recurse on each child
			NodeList children = node.getChildNodes();
			if (children != null) {
				for (int i=0; i<children.getLength(); i++) {
					ret+=domNode2String(children.item(i),escapeStrings);
				}
			}

			ret+="</" + name + ">";
			break;

		case Node.TEXT_NODE:
			ret+=(escapeStrings)?escapeStringForXML(node.getNodeValue()):node.getNodeValue();
			break;
//		case Node.COMMENT_NODE:
//			ret+="<!--"+node.getNodeValue()+"-->";
//			break;
		}
		return ret;
	}

	
	private static String BI=" "; 
	private static String prettyPrintDom(Node node,String indent,boolean isRoot, boolean escapeStrings)  {
		String ret="";
		switch (node.getNodeType()) {

		case Node.DOCUMENT_NODE:
			// recurse on each child
			NodeList nodes = node.getChildNodes();
			if (nodes != null) {
				for (int i=0; i<nodes.getLength(); i++) {
					ret+=prettyPrintDom(nodes.item(i),indent,isRoot,escapeStrings);
				}
			}
			break;

		case Node.ELEMENT_NODE:
			String name = node.getNodeName();
			ret+=indent+"<" + name;
			NamedNodeMap attributes = node.getAttributes();
			for (int i=0; i<attributes.getLength(); i++) {
				Node current = attributes.item(i);
				ret+=" " + current.getNodeName() +"=\"" + ((escapeStrings)?escapeStringForXML(current.getNodeValue()):current.getNodeValue())+"\"";
			}
			ret+=">";

			// recurse on each child
			NodeList children = node.getChildNodes();
			if (children != null) {
				for (int i=0; i<children.getLength(); i++) {
					String tmp=prettyPrintDom(children.item(i),indent+((isRoot)?"":BI),false,escapeStrings);
					if (!tmp.replaceAll("[\\s]+", "").equals(""))
						if (tmp.endsWith("\n"))
							if (ret.endsWith("\n"))
								ret+=tmp;
							else
								ret+="\n"+tmp;
						else
							ret+=tmp;
				}
			}
			if (ret.endsWith("\n"))
				ret+=indent+"</" + name + ">\n";
			else
				ret+="</" + name + ">\n";
			break;

		case Node.TEXT_NODE:
			ret+=(escapeStrings)?escapeStringForXML(node.getNodeValue()):node.getNodeValue();
			break;
			
		case Node.COMMENT_NODE:
			ret+="<!-- "+node.getNodeValue()+" -->";
			break;
		}
		return ret;
	}

	public static String prettyPrintXMLString(String xml,String indent, boolean escapeStrings) throws Exception {
		if (xml==null) return null;
		// add surrounding top level node just in case
		xml="<xml>"+xml+"</xml>";		
		BI=indent;
    	Document doc=mxUtils.parse(xml);
		xml=prettyPrintDom(doc,"",true,escapeStrings);
		//remove added top level node.
		xml=xml.replaceAll("^[\\s]*<xml>[\\s]*|[\\s]*</xml>[\\s]*$", "");
		return xml;
	}
	
    public static void main(String[] args) {
    	String a="<data name=\"information_state\"> <is:promised> <is:give-safety>false</is:give-safety> <is:give-secrecy>false</is:give-secrecy> <is:financial-reward>false</is:financial-reward> </is:promised> <is:preferenceCount> <is:give-safety>0</is:give-safety> <is:give-secrecy>0</is:give-secrecy> <is:financial-reward>0</is:financial-reward> </is:preferenceCount> <is:elicitCount> <is:give-safety>0</is:give-safety> <is:give-secrecy>0</is:give-secrecy> <is:financial-reward>0</is:financial-reward> </is:elicitCount> <is:number-of-offers>0</is:number-of-offers> <is:number-of-threats>0</is:number-of-threats> <is:number-of-compliments>0</is:number-of-compliments> <is:number-of-insults>0</is:number-of-insults> <is:wants-to-lie>false</is:wants-to-lie> <is:dialogue-length>0</is:dialogue-length> <is:turn-contains-elicit>false</is:turn-contains-elicit> </data>";
    	String c="<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\" profile=\"ecmascript\" intial=\"amani\"><state id=\"amani\"><parallel><state id=\"networks\"><parallel intial=\"network.question_answer\"><state id=\"network.question_answer\"><state id=\"question_resolved\"><onentry></onentry><transition event=\"player.ynq\" target=\"question_not_resolved\"/><transition event=\"player.whq\" target=\"question_not_resolved\"/><transition event=\"player.assert\" target=\"question_not_resolved\"/></state><state id=\"question_not_resolved\"><onentry></onentry><transition event=\"amani.assert\" cond=\"Data(question_analysis,'qa:is-sensitive')ne'true'\" target=\"question_not_resolved\"/><transition event=\"amani.assert\" cond=\"Data(information_state,'is:wants-to-lie')ne'true'andData(question_analysis,'qa:is-sensitive')eq'true'andData(question_analysis,'qa:constraint-satisfied')eq'true'andData(information_state,'is:turn-contains-elicit')ne'true'\" target=\"question_not_resolved\"/><transition event=\"amani.assert_lie\" cond=\"Data(information_state,'is:wants-to-lie')eq'true'andData(question_analysis,'qa:is-sensitive')eq'true'\" target=\"question_not_resolved\"/><transition event=\"amani.refuse_answer\" cond=\"!empty(_eventdata)\" target=\"question_not_resolved\"/><transition event=\"player.ynq\" target=\"question_not_resolved\"/><transition event=\"player.whq\" target=\"question_not_resolved\"/><transition event=\"player.assert\" target=\"question_not_resolved\"/></state></state><state id=\"network.thanks\"><state id=\"not_thanked\"><transition event=\"player.thanks\" target=\"thanked\"/></state><state id=\"thanked\"><transition event=\"amani.response-thanks\" target=\"not_thanked\"/></state></state><state id=\"network.greeting\"><state id=\"both_not_greeted\"><transition event=\"player.greeting\" target=\"amani_greeted\"/></state><state id=\"player_greeted\"><transition event=\"player.greeting\" target=\"both_greeted\"/></state><state id=\"amani_greeted\"><transition event=\"amani.greeting\" target=\"both_greeted\"/></state><state id=\"both_greeted\"><transition event=\"player.greeting\" target=\"amani_greeted\"/></state></state><state id=\"network.offer\"><state id=\"offer_not_elicited\"><onentry></onentry><transition event=\"player.offer\" target=\"offer_given\"></transition><transition event=\"amani.elicit-offer\" cond=\"In('question_not_resolved')\" target=\"offer_elicited\"><var expr=\"Data(offer, 'speech_act//offer/@name')\" name=\"offerName\"></var><assign expr=\"Data(information_state, 'is:preferenceCount/is:' + offerName) - 1\" location=\"Data(information_state, 'is:preferenceCount/is:' + offerName)\"></assign><assign expr=\"Data(information_state, 'is:elicitCount/is:' + offerName) + 1\" location=\"Data(information_state, 'is:elicitCount/is:' + offerName)\"></assign></transition><transition event=\"player.clarify_elicit_offer\" target=\"offer_not_elicited\"/></state><state id=\"offer_elicited\"><transition event=\"player.offer\" target=\"offer_given\"><assign expr=\"_eventdata\" location=\"Data(offer, 'speech_act')\"></assign></transition><transition event=\"amani.elicit-offer\" cond=\"In('question_not_resolved')\" target=\"offer_elicited\"><assign expr=\"_eventdata\" location=\"Data(offer, 'speech_act')\"></assign> <var expr=\"Data(offer, 'speech_act//offer/@name')\" name=\"offerName\"></var> <assign expr=\"Data(information_state, 'is:preferenceCount/is:' + offerName) - 1\" location=\"Data(information_state, 'is:preferenceCount/is:' + offerName)\"></assign> <assign expr=\"Data(information_state, 'is:elicitCount/is:' + offerName) + 1\" location=\"Data(information_state, 'is:elicitCount/is:' + offerName)\"></assign></transition><transition event=\"player.accept\" target=\"offer_elicited\"/><transition event=\"player.reject\" target=\"offer_elicited\"/><transition event=\"player.yes\" target=\"offer_elicited\"/><transition event=\"player.no\" target=\"offer_elicited\"/><transition event=\"player.clarify_elicit_offer\" target=\"offer_elicited\"/><transition event=\"player.unknown\" target=\"offer_elicited\"/><transition event=\"player.whq\" target=\"offer_elicited\"/><transition event=\"player.ynq\" target=\"offer_elicited\"/><transition event=\"player.assert\" target=\"offer_elicited\"/></state><state id=\"offer_given\"><onentry><assign expr=\"true\" location=\"Data(information_state, 'is:promised/is:' + Data(offer,'speech_act//offer/@name'))\"></assign> <assign expr=\"0\" location=\"Data(information_state, 'is:preferenceCount/is:' + Data(offer,'speech_act//offer/@name'))\"></assign> <assign expr=\"1 + Data(information_state, 'is:number-of-offers')\" location=\"Data(information_state, 'is:number-of-offers')\"></assign></onentry><transition event=\"player.offer\" target=\"offer_given\"><assign expr=\"_eventdata\" location=\"Data(offer, 'speech_act')\"></assign></transition><transition event=\"amani.response-offer\" target=\"offer_not_elicited\"/></state><state id=\"offer_not_given\"><transition event=\"amani.refuse_answer\" target=\"offer_not_given\"/></state></state><state id=\"network.threat\"><state id=\"threat_not_elicited\"><onentry><assign expr=\"Data(util,'empty')\" location=\"Data(threat, 'speech_act')\"></assign></onentry><transition event=\"player.threat\" target=\"threat_given\"><assign expr=\"_eventdata\" location=\"Data(threat, 'speech_act')\"></assign></transition><transition event=\"amani.elicit-threat\" cond=\"In('both_greeted')||In('player_greeted')\" target=\"threat_elicited\"><assign expr=\"_eventdata\" location=\"Data(threat, 'speech_act')\"></assign> <var expr=\"Data(offer, 'speech_act//threat/@name')\" name=\"threatName\"></var> <assign expr=\"Data(information_state, 'is:preferenceCount/is:' + threatName) - 1\" location=\"Data(information_state, 'is:preferenceCount/is:' + threatName)\"></assign> <assign expr=\"Data(information_state, 'is:elicitCount/is:' + threatName) + 1\" location=\"Data(information_state, 'is:elicitCount/is:' + threatName)\"></assign></transition></state><state id=\"threat_elicited\"><transition event=\"player.threat\" target=\"threat_given\"><assign expr=\"_eventdata\" location=\"Data(threat, 'speech_act')\"></assign></transition><transition event=\"player.accept\" target=\"threat_given\"/><transition event=\"player.yes\" target=\"threat_given\"/><transition event=\"player.reject\" target=\"threat_not_given\"/><transition event=\"player.no\" target=\"threat_not_given\"/><transition event=\"amani.elicit-threat\" target=\"threat_elicited\"><assign expr=\"_eventdata\" location=\"Data(threat, 'speech_act')\"></assign> <var expr=\"Data(offer, 'speech_act//threat/@name')\" name=\"threatName\"></var> <assign expr=\"Data(information_state, 'is:preferenceCount/is:' + threatName) - 1\" location=\"Data(information_state, 'is:preferenceCount/is:' + threatName)\"></assign> <assign expr=\"Data(information_state, 'is:elicitCount/is:' + threatName) + 1\" location=\"Data(information_state, 'is:elicitCount/is:' + threatName)\"></assign></transition><transition event=\"player.clarify_elicit_offer\" target=\"threat_elicited\"/><transition event=\"player.unknown\" target=\"threat_elicited\"/><transition event=\"player.whq\" target=\"threat_elicited\"/><transition event=\"player.ynq\" target=\"threat_elicited\"/><transition event=\"player.assert\" target=\"threat_elicited\"/></state><state id=\"threat_given\"><onentry><assign expr=\"true\" location=\"Data(information_state, 'is:promised/is:' + Data(_eventdata,'speech_act//threat/@name'))\"></assign> <assign expr=\"0\" location=\"Data(information_state, 'is:preferenceCount/is:' + Data(_eventdata,'speech_act//threat/@name'))\"></assign> <assign expr=\"1 + Data(information_state, 'is:number-of-threats')\" location=\"Data(information_state, 'is:number-of-threats')\"></assign></onentry><transition event=\"amani.response-threat\" target=\"threat_not_elicited\"/><transition event=\"player.threat\" target=\"threat_given\"><assign expr=\"_eventdata\" location=\"Data(threat, 'speech_act')\"></assign></transition></state><state id=\"threat_not_given\"><transition event=\"amani.refuse_answer\" target=\"threat_not_given\"/></state></state><state id=\"network.compliment\"><state id=\"compliment_replied\"><onentry><assign expr=\"Data(util,'empty')\" location=\"Data(compliment,'speech_act')\"></assign></onentry><transition event=\"player.compliment\" target=\"compliment_given\"/></state><state id=\"compliment_given\"><onentry><assign expr=\"_eventdata\" location=\"Data(compliment,'speech_act')\"></assign> <assign expr=\"1 + Data(information_state, 'is:number-of-compliments')\" location=\"Data(information_state, 'is:number-of-compliments')\"></assign> <assign expr=\"Data(information_state, 'is:number-of-compliments') le Data(information_state, 'is:number-of-insults')\" location=\"Data(information_state, 'is:wants-to-lie')\"></assign></onentry><transition event=\"player.compliment\" target=\"compliment_given\"/><transition event=\"amani.response-compliment\" target=\"compliment_given\"/></state></state><state id=\"network.insult\"><state id=\"insult_replied\"><onentry><assign expr=\"Data(util,'empty')\" location=\"Data(insult,'speech_act')\"></assign></onentry><transition event=\"player.insult\" target=\"insult_given\"/></state><state id=\"insult_given\"><onentry><assign expr=\"_eventdata\" location=\"Data(insult,'speech_act')\"></assign> <assign expr=\"1 + Data(information_state, 'is:number-of-insults')\" location=\"Data(information_state, 'is:number-of-insults')\"></assign> <assign expr=\"Data(information_state, 'is:number-of-compliments') le Data(information_state, 'is:number-of-insults')\" location=\"Data(information_state, 'is:wants-to-lie')\"></assign></onentry><transition event=\"player.insult\" target=\"insult_given\"/><transition event=\"amani.response-insult\" target=\"insult_given\"/></state></state><state id=\"network.pre_closing\"><state id=\"pre_closing_dormant\"><transition event=\"player.pre_closing\" target=\"pre_closing_amani\"/></state><state id=\"pre_closing\"><transition event=\"player.pre_closing\" target=\"pre_closing_amani\"/></state><state id=\"pre_closing_amani\"><transition event=\"amani.pre_closing\" target=\"pre_closing\"/></state></state><state id=\"network.closing\"><state id=\"closing_dormant\"><transition event=\"amani.closing\" cond=\"In('pre_closing')\" target=\"closing\"/><transition event=\"player.closing\" target=\"closing_amani\"/></state><state id=\"closing\"><transition event=\"player.closing\" target=\"closing_amani\"/></state><state id=\"closing_amani\"><transition event=\"amani.closing\" target=\"closing\"/></state></state><state id=\"network.goal\"><state id=\"goal_responded\"><transition event=\"player.goal\" target=\"goal_not_responded\"/></state><state id=\"goal_not_responded\"><transition event=\"player.goal\" target=\"goal_not_responded\"/><transition event=\"amani.goal\" target=\"goal_responded\"/></state></state><state id=\"network.unknown\"><state id=\"unknown_responded\"><transition event=\"player.unknown\" target=\"do_request_repair\"/></state><state id=\"do_request_repair\"><transition event=\"amani.request_repair_object\" cond=\"Data(conversation_topic,'topic/@name')ne''\" target=\"do_request_repair\"/><transition event=\"amani.request_repair\" cond=\"Data(conversation_topic,'topic/@name')eq''\" target=\"do_request_repair\"/></state><state id=\"do_confirm\"><transition event=\"player.yes\" target=\"do_confirm\"/><transition event=\"player.no\" target=\"do_confirm\"><assign expr=\"''\" location=\"Data(conversation_topic,'topic/@name')\"></assign></transition><transition event=\"player.unknown\" target=\"do_confirm\"><assign expr=\"''\" location=\"Data(conversation_topic,'topic/@name')\"></assign></transition><transition event=\"player.*\" target=\"do_confirm\"/></state><state id=\"do_request_repair_attribute\"><transition event=\"amani.request_repair_attribute\" target=\"do_request_repair_attribute\"/></state></state><state id=\"network.grounding\"><state id=\"topic_grounded\"><transition event=\"player.repeat_back\" target=\"topic_grounded\"/><transition event=\"player.request_repair_object\" target=\"topic_grounded\"/><transition event=\"player.request_repair\" target=\"topic_grounded\"/></state><state id=\"need_to_confirm_topic\"><transition event=\"amani.repeat_back\" target=\"need_to_confirm_topic\"/></state><state id=\"need_to_repeat\"><transition event=\"amani.repeat\" target=\"need_to_repeat\"/><transition event=\"*\" target=\"need_to_repeat\"/></state></state><state id=\"network.topic_shift\"><state id=\"topic_shift_responded\"><transition event=\"player.topic_shift\" target=\"topic_shift_not_responded\"/></state><state id=\"topic_shift_not_responded\"><transition event=\"amani.ack\" target=\"topic_shift_responded\"/><transition event=\"player.topic_shift\" target=\"topic_shift_not_responded\"/></state></state><state id=\"network.topic\"><state id=\"track-topic\"><transition event=\"player.whq\" cond=\"Data(conversation_topic,'topic/@name')neData(_eventdata,'//speech_act/primitive_speech_act/whq/object/@name')\" target=\"track-topic\"><assign expr=\"Data(_eventdata, '//speech_act/primitive_speech_act/whq/object/@name')\" location=\"Data(conversation_topic, 'topic/@name')\"></assign></transition><transition event=\"player.ynq\" cond=\"Data(conversation_topic,'topic/@name')neData(_eventdata,'//speech_act/primitive_speech_act/ynq/object/@name')\" target=\"track-topic\"><assign expr=\"Data(_eventdata, '//speech_act/primitive_speech_act/ynq/object/@name')\" location=\"Data(conversation_topic, 'topic/@name')\"></assign></transition><transition event=\"player.assert\" cond=\"Data(conversation_topic,'topic/@name')neData(_eventdata,'//speech_act/primitive_speech_act/assert/object/@name')\" target=\"track-topic\"><assign expr=\"Data(_eventdata, '//speech_act/primitive_speech_act/assert/object/@name')\" location=\"Data(conversation_topic, 'topic/@name')\"></assign></transition></state><state id=\"repeat-back\"><transition event=\"amani.repeat_back\" target=\"repeat-back\"/></state></state></parallel></state><state id=\"mind\"><parallel intial=\"single-node-network.question\"><state id=\"single-node-network.question\"><state id=\"analyze-question\"><transition event=\"player.whq\" target=\"analyze-question\"><assign SIAttribute=\"name\" SIName=\"1_strange_man_name\" SIObject=\"strange_man\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/whq/object[@name=\"strange_man\"]/attribute/@name') eq 'name'\" location=\"Data(analysis_variables, 'av:about_1_strange_man_name')\" type=\"match\"></assign> <assign SIAttribute=\"location\" SIName=\"2_strange_man_location\" SIObject=\"strange_man\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/whq/object[@name=\"strange_man\"]/attribute/@name') eq 'location'\" location=\"Data(analysis_variables, 'av:about_2_strange_man_location')\" type=\"match\"></assign> <assign SIAttribute=\"general_description\" SIName=\"3_strange_man_general_description\" SIObject=\"strange_man\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/whq/object[@name=\"strange_man\"]/attribute/@name') eq 'general_description'\" location=\"Data(analysis_variables, 'av:about_3_strange_man_general_description')\" type=\"match\"></assign> <assign SIAttribute=\"daily_routine\" SIName=\"4_strange_man_daily_routine\" SIObject=\"strange_man\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/whq/object[@name=\"strange_man\"]/attribute/@name') eq 'daily_routine'\" location=\"Data(analysis_variables, 'av:about_4_strange_man_daily_routine')\" type=\"match\"></assign> <assign SIAttribute=\"occupant\" SIName=\"5_the_shop_occupant\" SIObject=\"the_shop\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/whq/object[@name=\"the_shop\"]/attribute/@name') eq 'occupant'\" location=\"Data(analysis_variables, 'av:about_5_the_shop_occupant')\" type=\"match\"></assign> <assign SIAttribute=\"perpetrator\" SIName=\"6_the_incident_perpetrator\" SIObject=\"the_incident\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/whq/object[@name=\"the_incident\"]/attribute/@name') eq 'perpetrator'\" location=\"Data(analysis_variables, 'av:about_6_the_incident_perpetrator')\" type=\"match\"></assign></transition><transition event=\"player.ynq\" target=\"analyze-question\"><assign SIName=\"1_strange_man_name\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/ynq/object[@name=\"strange_man\"]/attribute/@name') eq 'name'\" location=\"Data(analysis_variables, 'av:about_1_strange_man_name')\" type=\"match\"></assign> <assign SIName=\"2_strange_man_location\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/ynq/object[@name=\"strange_man\"]/attribute/@name') eq 'location'\" location=\"Data(analysis_variables, 'av:about_2_strange_man_location')\" type=\"match\"></assign> <assign SIName=\"3_strange_man_general_description\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/ynq/object[@name=\"strange_man\"]/attribute/@name') eq 'general_description'\" location=\"Data(analysis_variables, 'av:about_3_strange_man_general_description')\" type=\"match\"></assign> <assign SIName=\"4_strange_man_daily_routine\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/ynq/object[@name=\"strange_man\"]/attribute/@name') eq 'daily_routine'\" location=\"Data(analysis_variables, 'av:about_4_strange_man_daily_routine')\" type=\"match\"></assign> <assign SIName=\"5_the_shop_occupant\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/ynq/object[@name=\"the_shop\"]/attribute/@name') eq 'occupant'\" location=\"Data(analysis_variables, 'av:about_5_the_shop_occupant')\" type=\"match\"></assign> <assign SIName=\"6_the_incident_perpetrator\" expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/ynq/object[@name=\"the_incident\"]/attribute/@name') eq 'perpetrator'\" location=\"Data(analysis_variables, 'av:about_6_the_incident_perpetrator')\" type=\"match\"></assign></transition></state></state><state id=\"single-node-network.sensitive-information\"><state id=\"analyze-sensitive-information\"><onentry><assign SIName=\"1_strange_man_name\" expr=\"Data(analysis_variables, 'av:about_1_strange_man_name') and (Data(information_state, 'is:promised/is:give-safety'))\" location=\"Data(analysis_variables, 'av:about_1_strange_man_name_ConstraintSatisfied')\" type=\"constraint\"></assign> <if SIName=\"1_strange_man_name\" cond=\"(! Data(information_state, 'is:wants-to-lie') and Data(analysis_variables, 'av:about_1_strange_man_name') and ! Data(analysis_variables, 'av:about_1_strange_man_name_ConstraintSatisfied'))\" type=\"preference\"> <assign expr=\"1\" location=\"Data(information_state, 'is:preferenceCount/is:give-safety')\"></assign> </if> <assign SIName=\"2_strange_man_location\" expr=\"Data(analysis_variables, 'av:about_2_strange_man_location') and (Data(information_state, 'is:promised/is:give-secrecy'))\" location=\"Data(analysis_variables, 'av:about_2_strange_man_location_ConstraintSatisfied')\" type=\"constraint\"></assign> <if SIName=\"2_strange_man_location\" cond=\"(! Data(information_state, 'is:wants-to-lie') and Data(analysis_variables, 'av:about_2_strange_man_location') and ! Data(analysis_variables, 'av:about_2_strange_man_location_ConstraintSatisfied'))\" type=\"preference\"> <assign expr=\"1\" location=\"Data(information_state, 'is:preferenceCount/is:give-secrecy')\"></assign> </if> <assign SIName=\"3_strange_man_general_description\" expr=\"Data(analysis_variables, 'av:about_3_strange_man_general_description') and (Data(information_state, 'is:promised/is:give-safety'))\" location=\"Data(analysis_variables, 'av:about_3_strange_man_general_description_ConstraintSatisfied')\" type=\"constraint\"></assign> <if SIName=\"3_strange_man_general_description\" cond=\"(! Data(information_state, 'is:wants-to-lie') and Data(analysis_variables, 'av:about_3_strange_man_general_description') and ! Data(analysis_variables, 'av:about_3_strange_man_general_description_ConstraintSatisfied'))\" type=\"preference\"> <assign expr=\"1\" location=\"Data(information_state, 'is:preferenceCount/is:give-safety')\"></assign> </if> <assign SIName=\"4_strange_man_daily_routine\" expr=\"Data(analysis_variables, 'av:about_4_strange_man_daily_routine') and (Data(information_state, 'is:promised/is:financial-reward'))\" location=\"Data(analysis_variables, 'av:about_4_strange_man_daily_routine_ConstraintSatisfied')\" type=\"constraint\"></assign> <if SIName=\"4_strange_man_daily_routine\" cond=\"(! Data(information_state, 'is:wants-to-lie') and Data(analysis_variables, 'av:about_4_strange_man_daily_routine') and ! Data(analysis_variables, 'av:about_4_strange_man_daily_routine_ConstraintSatisfied'))\" type=\"preference\"> <assign expr=\"1\" location=\"Data(information_state, 'is:preferenceCount/is:financial-reward')\"></assign> </if> <assign SIName=\"5_the_shop_occupant\" expr=\"Data(analysis_variables, 'av:about_5_the_shop_occupant') and (Data(information_state, 'is:promised/is:give-secrecy'))\" location=\"Data(analysis_variables, 'av:about_5_the_shop_occupant_ConstraintSatisfied')\" type=\"constraint\"></assign> <if SIName=\"5_the_shop_occupant\" cond=\"(! Data(information_state, 'is:wants-to-lie') and Data(analysis_variables, 'av:about_5_the_shop_occupant') and ! Data(analysis_variables, 'av:about_5_the_shop_occupant_ConstraintSatisfied'))\" type=\"preference\"> <assign expr=\"1\" location=\"Data(information_state, 'is:preferenceCount/is:give-secrecy')\"></assign> </if> <assign SIName=\"6_the_incident_perpetrator\" expr=\"Data(analysis_variables, 'av:about_6_the_incident_perpetrator') and ((Data(information_state, 'is:promised/is:give-safety')) and (Data(information_state, 'is:promised/is:give-secrecy')))\" location=\"Data(analysis_variables, 'av:about_6_the_incident_perpetrator_ConstraintSatisfied')\" type=\"constraint\"></assign> <if SIName=\"6_the_incident_perpetrator\" cond=\"(! Data(information_state, 'is:wants-to-lie') and Data(analysis_variables, 'av:about_6_the_incident_perpetrator') and ! Data(analysis_variables, 'av:about_6_the_incident_perpetrator_ConstraintSatisfied'))\" type=\"preference\"> <assign expr=\"1\" location=\"Data(information_state, 'is:preferenceCount/is:give-safety')\"></assign> <assign expr=\"1\" location=\"Data(information_state, 'is:preferenceCount/is:give-secrecy')\"></assign> </if> <assign expr=\"false || Data(analysis_variables, 'av:about_1_strange_man_name') || Data(analysis_variables, 'av:about_2_strange_man_location') || Data(analysis_variables, 'av:about_3_strange_man_general_description') || Data(analysis_variables, 'av:about_4_strange_man_daily_routine') || Data(analysis_variables, 'av:about_5_the_shop_occupant') || Data(analysis_variables, 'av:about_6_the_incident_perpetrator')\" location=\"Data(question_analysis, 'qa:is-sensitive')\"></assign> <assign expr=\"false || Data(analysis_variables, 'av:about_1_strange_man_name_ConstraintSatisfied') || Data(analysis_variables, 'av:about_2_strange_man_location_ConstraintSatisfied') || Data(analysis_variables, 'av:about_3_strange_man_general_description_ConstraintSatisfied') || Data(analysis_variables, 'av:about_4_strange_man_daily_routine_ConstraintSatisfied') || Data(analysis_variables, 'av:about_5_the_shop_occupant_ConstraintSatisfied') || Data(analysis_variables, 'av:about_6_the_incident_perpetrator_ConstraintSatisfied')\" location=\"Data(question_analysis, 'qa:constraint-satisfied')\"></assign></onentry><transition event=\"player.whq\" target=\"analyze-sensitive-information\"/><transition event=\"player.ynq\" target=\"analyze-sensitive-information\"/></state></state><state id=\"single-node-network.sensitive-information-constraint\"><state id=\"analyze-sensitive-information-constraint\"><onentry><assign SIName=\"1_strange_man_name\" expr=\"Data(analysis_variables, 'av:about_1_strange_man_name') and (Data(information_state, 'is:promised/is:give-safety'))\" location=\"Data(analysis_variables, 'av:about_1_strange_man_name_ConstraintSatisfied')\" type=\"constraint\"></assign> <assign SIName=\"2_strange_man_location\" expr=\"Data(analysis_variables, 'av:about_2_strange_man_location') and (Data(information_state, 'is:promised/is:give-secrecy'))\" location=\"Data(analysis_variables, 'av:about_2_strange_man_location_ConstraintSatisfied')\" type=\"constraint\"></assign> <assign SIName=\"3_strange_man_general_description\" expr=\"Data(analysis_variables, 'av:about_3_strange_man_general_description') and (Data(information_state, 'is:promised/is:give-safety'))\" location=\"Data(analysis_variables, 'av:about_3_strange_man_general_description_ConstraintSatisfied')\" type=\"constraint\"></assign> <assign SIName=\"4_strange_man_daily_routine\" expr=\"Data(analysis_variables, 'av:about_4_strange_man_daily_routine') and (Data(information_state, 'is:promised/is:financial-reward'))\" location=\"Data(analysis_variables, 'av:about_4_strange_man_daily_routine_ConstraintSatisfied')\" type=\"constraint\"></assign> <assign SIName=\"5_the_shop_occupant\" expr=\"Data(analysis_variables, 'av:about_5_the_shop_occupant') and (Data(information_state, 'is:promised/is:give-secrecy'))\" location=\"Data(analysis_variables, 'av:about_5_the_shop_occupant_ConstraintSatisfied')\" type=\"constraint\"></assign> <assign SIName=\"6_the_incident_perpetrator\" expr=\"Data(analysis_variables, 'av:about_6_the_incident_perpetrator') and ((Data(information_state, 'is:promised/is:give-safety')) and (Data(information_state, 'is:promised/is:give-secrecy')))\" location=\"Data(analysis_variables, 'av:about_6_the_incident_perpetrator_ConstraintSatisfied')\" type=\"constraint\"></assign> <assign expr=\"false || Data(analysis_variables, 'av:about_1_strange_man_name') || Data(analysis_variables, 'av:about_2_strange_man_location') || Data(analysis_variables, 'av:about_3_strange_man_general_description') || Data(analysis_variables, 'av:about_4_strange_man_daily_routine') || Data(analysis_variables, 'av:about_5_the_shop_occupant') || Data(analysis_variables, 'av:about_6_the_incident_perpetrator')\" location=\"Data(question_analysis, 'qa:is-sensitive')\"></assign> <assign expr=\"false || Data(analysis_variables, 'av:about_1_strange_man_name_ConstraintSatisfied') || Data(analysis_variables, 'av:about_2_strange_man_location_ConstraintSatisfied') || Data(analysis_variables, 'av:about_3_strange_man_general_description_ConstraintSatisfied') || Data(analysis_variables, 'av:about_4_strange_man_daily_routine_ConstraintSatisfied') || Data(analysis_variables, 'av:about_5_the_shop_occupant_ConstraintSatisfied') || Data(analysis_variables, 'av:about_6_the_incident_perpetrator_ConstraintSatisfied')\" location=\"Data(question_analysis, 'qa:constraint-satisfied')\"></assign></onentry><transition event=\"*\" target=\"analyze-sensitive-information-constraint\"/></state></state><state id=\"single-node-network.lie\"><state id=\"analyze-lie\"><transition event=\"player.threat\" cond=\"Data(information_state,'is:number-of-threats')gt0\" target=\"analyze-lie\"><assign expr=\"true\" location=\"Data(information_state, 'is:wants-to-lie')\"></assign></transition></state></state><state id=\"single-node-network.commitment-updater\"><state id=\"update-commitment\"><transition event=\"amani.assert\" target=\"update-commitment\"><actions:merge expr=\"Data(_eventdata, '//speech_act/primitive_speech_act/assert/object')\" location=\"Data(commitments, 'commitments/character[@name=\"amani\"]')\"></actions:merge></transition><transition event=\"player.assert\" target=\"update-commitment\"><actions:merge expr=\"Data(_eventdata, '//speech_act/primitive_speech_act/assert/object')\" location=\"Data(commitments, 'commitments/character[@name=\"player\"]')\"></actions:merge></transition></state></state><state id=\"single-node-network.last-speech-act-updater\"><state id=\"update-last-speech-act\"><transition event=\"amani.*\" cond=\"Data(_eventdata,'//speech_act/@speaker')eq'amani'\" target=\"update-last-speech-act\"><assign expr=\"Data(_eventdata, '//speech_act')\" location=\"Data(last_speech_act,'speech_act[@speaker=\"amani\"]')\"></assign> <actions:appendNode expr=\"Data(_eventdata, '//speech_act')\" location=\"Data(last_turn, 'turn[@speaker=\"amani\"]')\"></actions:appendNode> <assign expr=\"! empty( Data(last_turn, 'turn[@speaker=\"amani\"]//elicit//offer/@name') )\" location=\"Data(information_state,'is:turn-contains-elicit')\"></assign></transition><transition event=\"player.*\" target=\"update-last-speech-act\"><assign expr=\"Data(util,'empty')\" location=\"Data(last_turn,'turn[@speaker=\"amani\"]')\"></assign> <assign expr=\"false\" location=\"Data(information_state,'is:turn-contains-elicit')\"></assign></transition></state></state><state id=\"single-node-network.dialog-length\"><state id=\"analyze-length\"><transition event=\"*\" target=\"analyze-length\"></transition></state></state></parallel></state></parallel></state></scxml>";
    	String d="<assign expr=\"Data(_eventdata,'//speech_act/primitive_speech_act/whq/object[@name=\"strange_man\"]/attribute/@name') eq 'name'\" location=\"Data(analysis_variables, 'av:about_1_strange_man_name')\" type=\"match\"></assign>";
    	String e="<x a=\"a&quot;\"/>";
    	try {
			System.out.println(prettyPrintXMLString(d," ",true));
			Document doc=mxUtils.parse(d);
			String b=prettyPrintDom(doc,"",true,true);
			System.out.println(b);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    }

	public static String isParsableXMLString(String xml) {
		try {
			xml="<xml>"+xml+"</xml>";
			mxUtils.parse(xml);
		} catch (SAXParseException e) {
			return e.getMessage();
		} catch (Exception e) {
		}
		return null;
	}
}
