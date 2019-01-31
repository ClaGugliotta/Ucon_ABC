package Classi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.AttributeAssignment;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.xacml3.Advice;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;

/**
 * Access Controll che utilizza il framework Balana
 */
public class AccessControll{
    private static Balana balana;
    private static String string = "[1] Manager [2] Factory Manager [3] Department Manager [4] Operator [5] Programmer";
    private static String string2 = "[1] statistic [2] robot1 [3] robot2 [4] computer1";
    private static int array[] = {1,2,3,4,5};
    private static String users[] = {"manager","factory_manager","department_manager","operator","programmer"};
    private static String machines[] ={"statistic","robota","robotb","computer"};


    public static void main(String[] args){
        Scanner buffer = new Scanner(System.in);
        String usr = null;
        String machine = null;
        int selezione = 0;
        int selezionemacchina = 0;


        printInit();
        initBalana();

        System.out.println("Seleziona ora il tipo di utente che si vuole far loggare indicando il numero dell'utente:");
        System.out.println(string);


        usr = buffer.nextLine();


        System.out.println("Seleziona ora il tipo di macchina in cui si vuole loggare:");
        System.out.println(string2);

        machine = buffer.nextLine();



        if(usr != null && machine != null){
            selezione = Integer.valueOf(usr);
            selezionemacchina = Integer.valueOf(machine);
            String request = createXACMLRequest(users[selezione-1],"Descendants");
            String response = new Pdp().evaluationRequest(balana,request);
            System.out.println(response);
        }
        else{
            System.out.println("i campi non possono essere vuoti");
        }
        /*A questo punto si selezione il tipo di utente e poi si deve visualizzare il tipo di macchina che si vuole accedere,
        * Quante macchine ?
        * Valutare diritti su macchina ad altro modulo
        * "lanciare la macchina semplice se tutto positivo"*/
    }

    private static void printInit() {
        System.out.println("Ci troviamo in una fabbrica 4.0, dove si possono collegare X categorie di utenti" +
                "Ogniuno di questi ha un determinato diritto sulle macchine e potrà eseguire determinate azioni " +
                "Tipo accedere a singole macchine, avere potere di scrittura o lettura su tali macchine etc.." +
                "Per semplicità si suppone che tutta la parte di Login sia stata eseguita.");
    }

    private static void initBalana(){

        try{
            // using file based policy repository. so set the policy location as system property
            String policyLocation = (new File(".")).getCanonicalPath() + File.separator + "resources";
            System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyLocation);
        } catch (IOException e) {
            System.err.println("Can not locate policy repository");
        }
        balana = Balana.getInstance();
    }

    public static String createXACMLRequest(String userName, String type){

        return "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">access</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">"+ userName +"</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" IncludeInResult=\"true\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">root</AttributeValue>\n" +
                "</Attribute>\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:2.0:resource:scope\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + type + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "</Request>";

    }
}
