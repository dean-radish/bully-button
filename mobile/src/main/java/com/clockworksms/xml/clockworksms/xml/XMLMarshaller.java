package com.clockworksms.xml.clockworksms.xml;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Peter on 10/20/2016.
 */

public class XMLMarshaller {
    private static final String OPEN = "<";
    private static final String OPEN_CLOSURE = "</";
    private static final String CLOSE = ">";
    private static final String EMPTY = "";
    private static final String LINE_FEED = System
            .getProperty("line.separator");
    private static final String LINE_FEED_AND_SPACER = LINE_FEED + " ";
    private static final String LINE_FEED_AND_DOUBLE_SPACER = LINE_FEED_AND_SPACER
            + " ";
    private static final String DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    public String marshal(XmlRequest aRequest) {
        if (aRequest instanceof MessageRequest)
            return marshalMessage((MessageRequest)aRequest);
        else
            return marshalCredit((CreditRequest)aRequest);
    }

    public String marshalCredit(CreditRequest aRequest) {
        StringBuilder builder = new StringBuilder();
        StringBuilder creditChildNodes = new StringBuilder();
        addNode(creditChildNodes, "Key", aRequest.getApiKey());
        addNode(builder, "Credit", creditChildNodes.toString());
        builder.insert(0, DECLARATION);
        return builder.toString();
    }
    public String marshalMessage(MessageRequest aRequest) {
        StringBuilder builder = new StringBuilder();
        StringBuilder messageChildNodes = new StringBuilder();
        addNode(messageChildNodes, "Key", aRequest.getApiKey());
        Iterator<xmlSMS> it = aRequest.getSms().iterator();
        while (it.hasNext()) {
            xmlSMS sms = it.next();
            StringBuilder smsChildNodes = new StringBuilder();
            addNode(smsChildNodes, "ClientID", sms.getClientId());
            addNode(smsChildNodes, "Content", sms.getContent());
            addNode(smsChildNodes, "From", sms.getFrom());
            addNode(smsChildNodes, "InvalidCharAction",
                    sms.getInvalidCharacterAction());
            addNode(smsChildNodes, "Long", sms.getLongMessage());
            addNode(smsChildNodes, "To", sms.getTo());
            addNode(smsChildNodes, "Truncate", sms.getTruncateMessage());
            addNode(smsChildNodes, "WrapperID", sms.getWrapperId());
            addNode(messageChildNodes, "SMS", smsChildNodes.toString());
        }
        addNode(builder, "Message", messageChildNodes.toString());
        builder.insert(0, DECLARATION);
        return builder.toString();
    }
    private StringBuilder addNode(StringBuilder builder, String content) {
        if (content != null && !"".equals(content)) {
            if (builder.length() > 0) {
                builder.append(LINE_FEED);
            }
            builder.append(content);
        }
        return builder;
    }
    private StringBuilder addNode(StringBuilder builder, String name,
                                  int content) {
        return addNode(builder, name, "" + content);
    }
    private StringBuilder addNode(StringBuilder builder, String name,
                                  Boolean content) {
        if (content == null) {
            return builder;
        }
        return addNode(builder, name, content.toString());
    }
    private StringBuilder addNode(StringBuilder builder, String name,
                                  String content) {
        if (content == null || "".equals(content)) {
            return builder;
        }
// if (builder.length() > 0) {
        builder.append(LINE_FEED_AND_SPACER);
// }
        builder.append(OPEN);
        builder.append(name);
        builder.append(CLOSE);
        if (content.startsWith(LINE_FEED)) {
            builder.append(content.replace(LINE_FEED_AND_SPACER,
                    LINE_FEED_AND_DOUBLE_SPACER));
//builder.append(LINE_FEED);
            builder.append(LINE_FEED_AND_SPACER);
        } else {
            builder.append(content);
        }
        builder.append(OPEN_CLOSURE);
        builder.append(name);
        builder.append(CLOSE);
        return builder;
    }

          
   public XmlResponse unmarshal(String mess) throws Exception {
          SAXParserFactory saxPF = SAXParserFactory.newInstance();
          SAXParser saxP = saxPF.newSAXParser();
          XMLReader xmlR = saxP.getXMLReader();
          SmsResponseContentContentHandler myXMLHandler = new SmsResponseContentContentHandler();
          xmlR.setContentHandler(myXMLHandler);
          xmlR.parse(new InputSource(new StringReader(mess)));

          return myXMLHandler.getResponse();
    }

   public class SmsResponseContentContentHandler extends DefaultHandler {
       private String elementValue = null;
       private Boolean elementOn = false;
       private XmlResponse response = null;
       private CreditResponse creditResponse = null;
       private MessageResponse messageResponse = null;
       private SmsResponse smsResponse = null;
       private List<SmsResponse> smsResponses = null;


       public XmlResponse getResponse() {
           return response;
       }

       public void startElement(String uri, String localName, String qName,
                                               Attributes attributes) throws SAXException {

           elementOn = true;

           if (qName.equals("Credit_Resp"))
           {
                     creditResponse = new CreditResponse();
                     response = creditResponse;
               } else if (qName.equals("Message_Resp"))
           {
                     messageResponse = new MessageResponse();
                     response = messageResponse;
               } else if (qName.equals("SMS_Resp")) {
                   smsResponse = new SmsResponse();
                   if (smsResponses == null) {
                         smsResponses = new ArrayList<SmsResponse>();
                       }
               }
       }

           /**
     * This will be called when the tags of the XML end.
     **/
           @Override
            public void endElement(String uri, String localName, String qName)
           throws SAXException {

           elementOn = false;

           //shared
           if (qName.equalsIgnoreCase("ErrNo")) {
                     if (smsResponse != null) {
                                smsResponse.setErrorNo((Integer.parseInt(elementValue)));
                         } else {
                                response.setErrorNumber(Integer.parseInt(elementValue));
                         }
               } else if (qName.equalsIgnoreCase("ErrDesc")) {
                     if (smsResponse != null) {
                                smsResponse.setErrorDescription(elementValue);
                         } else {
                                response.setErrorDescription(elementValue);
                         }

               }

           //credit
           else if (qName.equalsIgnoreCase("Credit"))
               creditResponse.setCredit(Long.parseLong(elementValue));

           //message text nodes
           else if (qName.equalsIgnoreCase("To"))
               smsResponse.setTo(elementValue);
           else if (qName.equalsIgnoreCase("WrapperID"))
                 smsResponse.setWrapperId(Integer.parseInt(elementValue));
           else if (qName.equalsIgnoreCase("MessageID"))
                 smsResponse.setMessageId(elementValue);

           //message inner nodes
           else if (qName.equals("Message_Resp")) {
                     messageResponse.setSmsResponses(smsResponses);
                     smsResponses = null;
               }
           else if (qName.equalsIgnoreCase("SMS_Resp")) {
                   smsResponses.add(smsResponse);
                   smsResponse = null;
               }


           elementValue = null;
       }

       public void characters(char[] ch, int start, int length)
           throws SAXException {

           if (elementOn) {
                   elementValue = new String(ch, start, length);
                   elementOn = false;
               }

       }

   }
     

}