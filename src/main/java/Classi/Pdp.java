package Classi;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.*;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.ResourceFinder;
import org.wso2.balana.finder.ResourceFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.xacml3.Result;
import org.wso2.balana.ctx.Attribute;
import org.wso2.balana.xacml3.Attributes;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Pdp {
    private String richiesta = null;

    public String evaluationRequest(Balana balana, String request){

        PDP pdp = getPDPNewInstance(balana);
        this.richiesta = request;


        return pdp.evaluate(richiesta);
    }

    /**
     * Returns a new PDP instance with new XACML policies
     *
     * @return a  PDP instance
     */

    private static PDP getPDPNewInstance(Balana balana){


        PDPConfig pdpConfig = balana.getPdpConfig();

        // registering new attribute finder. so default PDPConfig is needed to change
        AttributeFinder attributeFinder = pdpConfig.getAttributeFinder();
        List<AttributeFinderModule> finderModules = attributeFinder.getModules();
        finderModules.add(new AttributeController());
        attributeFinder.setModules(finderModules);

        // registering new resource finder. so default PDPConfig is needed to change
        ResourceFinder resourceFinder = pdpConfig.getResourceFinder();
        List<ResourceFinderModule> resourceModules = resourceFinder.getModules();
        resourceModules.add(new HierachicalResourceFinder());
        resourceFinder.setModules(resourceModules);

        return new PDP(new PDPConfig(attributeFinder, pdpConfig.getPolicyFinder(), resourceFinder, true));
    }

    public boolean askEvaluation(Balana balana, String request, String machine) {
        String evaluation = evaluationRequest(balana,request);
        Set<String> permitResources = new HashSet<String>();
        Set<String> denyResources = new HashSet<String>();

        try {
            ResponseCtx responseCtx = ResponseCtx.getInstance(getXacmlResponse(evaluation));
            Set<AbstractResult> results  = responseCtx.getResults();
            for(AbstractResult result : results){
                Set<Attributes> attributesSet = ((Result)result).getAttributes();
                for(Attributes attributes : attributesSet){
                    for(Attribute attribute : attributes.getAttributes()){
                        if(AbstractResult.DECISION_PERMIT == result.getDecision()){
                            permitResources.add(attribute.getValue().encode());
                        } else {
                            denyResources.add(attribute.getValue().encode());
                        }
                    }
                }
            }
        } catch (ParsingException e) {
            e.printStackTrace();
        }
        //manca da confrontare se tra i permessi vi è quello che cerchiamo.
        System.out.println(evaluation);
        System.out.println(permitResources);
        return false;
    }
    public static Element getXacmlResponse(String response) {

        ByteArrayInputStream inputStream;
        DocumentBuilderFactory dbf;
        Document doc;

        inputStream = new ByteArrayInputStream(response.getBytes());
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            doc = dbf.newDocumentBuilder().parse(inputStream);
        } catch (Exception e) {
            System.err.println("DOM of request element can not be created from String");
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                System.err.println("Error in closing input stream of XACML response");
            }
        }
        return doc.getDocumentElement();
    }
}
