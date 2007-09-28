<%@ page import="java.io.*, java.util.*, org.w3c.dom.*" %>
<%@ page import="org.apache.soap.Constants, org.apache.soap.util.*, org.apache.soap.util.xml.*, org.apache.soap.server.*" %>

<h1>Deploy a Service</h1>

<% 
  String configFilename = config.getInitParameter(Constants.CONFIGFILENAME);
  if (configFilename == null)
    configFilename = application.getInitParameter(Constants.CONFIGFILENAME);

ServiceManager serviceManager =
  org.apache.soap.server.http.ServerHTTPUtils.getServiceManagerFromContext(application, configFilename);

if (!request.getMethod().equals ("POST")) { 
%>

<form action="deploy.jsp" method="POST">
    <table border="1" width="100%">
        <tr>
            <th colspan="2"><h2>Service Deployment Descriptor
            Template</h2>
            </th>
        </tr>
        <tr>
            <th>Property</th>
            <th>Details</th>
        </tr>
        <tr>
            <td>ID</td>
            <td><input type="text" size="60" name="id"></td>
        </tr>
        <tr>
            <td>Scope</td>
            <td><select name="scope" size="1">
                <option selected value="0">Request</option>
                <option value="1">Session</option>
                <option value="2">Application</option>
            </select></td>
        </tr>
        <tr>
            <td>Methods</td>
            <td><input type="text" size="60" name="methods"><br>
            (Whitespace separated list of method names) </td>
        </tr>
        <tr>
            <td>Provider Type</td>
      
            <td><select name="providerType" size="1">
                <option selected value="0">Java</option>
                <option value="1">Script</option>
		<option value="3">User-Defined</option>
            	</select></td>
        </tr>
	<tr>
		<td> </td>
		<td><div align="left"><table border="0">
                <tr>
                    <td>For User-Defined Provider Type, Enter FULL Class Name:</td>
		</tr>
		<tr>
                    <td><input type="text" size="60" name="userProviderClassString"></td>
                </tr>
		</table></div></td>
	</tr>
	<tr>
		<td> </td>
		<td>Number of Options: <input type="text" size="10" name="noOpts" /><br>
            	<div align="center"><center><table border="0">
                <tr>
                    <td align="center" >Key</td>
                    <td align="center" >Value</td>
		</tr>
                <tr>
                    <td><input type="text" size="15" name="optionkey1"></td>
                    <td><input type="text" size="20" name="optionvalue1"></td>
                </tr>
                <tr>
                    <td><input type="text" size="15" name="optionkey2"></td>
                    <td><input type="text" size="20" name="optionvalue2"></td>
                </tr>
                <tr>
                    <td><input type="text" size="15" name="optionkey3"></td>
                    <td><input type="text" size="20" name="optionvalue3"></td>
                </tr>
                <tr>
                    <td><input type="text" size="15" name="optionkey4"></td>
                    <td><input type="text" size="20" name="optionvalue4"></td>
                </tr>
		</table></center></div></td>
	</tr>
        <tr>
            <td>Java Provider</td>
            <td><div align="left"><table border="0">
                <tr>
                    <td>Provider Class</td>
                    <td><input type="text" size="40"
                    name="providerClass"></td>
                </tr>
                <tr>
                    <td width="150">Static?</td>
                    <td><select name="isStatic" size="1">
                        <option value="true">Yes</option>
                        <option selected value="no">No</option>
                    </select></td>
                </tr>
            </table>
            </div></td>
        </tr>
        <tr>
            <td>Script Provider</td>
            <td><div align="left"><table border="0">
                <tr>
                    <td>Script Language</td>
                    <td><select name="scriptLanguage" size="1">
                        <option value="bml">BML</option>
                        <option value="jacl">Jacl</option>
                        <option value="javascript">JavaScript (Rhino)</option>
                        <option value="jpython">JPython</option>
                        <option value="jscript">JScript</option>
                        <option value="perlscript">PerlScript</option>
                        <option value="vbscript">VBScript</option>
                        <option value="other">Other .. (type in)</option>
                    </select><input type="text" size="20"
                    name="scriptLanguageTypein"></td>
                </tr>
                <tr>
                    <td width="150">Script Filename, or</td>
                    <td><input type="text" size="40"
                    name="scriptFilename"></td>
                </tr>
                <tr>
                    <td>Script</td>
                    <td><textarea name="script" rows="10"
                    cols="40"></textarea></td>
                </tr>
            </table>
            </div></td>
        </tr>
        <tr>
            <td>Type Mappings</td>
            <td>Number of mappings: <input type="text" size="10"
            name="nmaps" /><br>
            <div align="center"><center><table border="0">
                <tr>
                    <td align="center" rowspan="2">Encoding Style</td>
                    <td align="center" colspan="2">Element Type </td>
                    <td align="center" rowspan="2">Java Type</td>
                    <td align="center" rowspan="2">Java to XML
                    Serializer </td>
                    <td align="center" rowspan="2">XML to Java
                    Deserializer </td>
                </tr>
                <tr>
                    <td align="center">Namespace URI</td>
                    <td align="center">Local Part</td>
                </tr>
                <tr>
                    <td><select name="encstyle1" size="1">
                        <option selected value="0">SOAP</option>
                        <option value="1">XMI</option>
                    </select></td>
                    <td><input type="text" size="15"
                    name="nsuri1"></td>
                    <td><input type="text" size="10"
                    name="localpart1"></td>
                    <td><input type="text" size="15"
                    name="classname1"></td>
                    <td><input type="text" size="15"
                    name="java2xml1"></td>
                    <td><input type="text" size="15"
                    name="xml2java1"></td>
                </tr>
                <tr>
                    <td><select name="encstyle2" size="1">
                        <option selected value="0">SOAP</option>
                        <option value="1">XMI</option>
                    </select></td>
                    <td><input type="text" size="15"
                    name="nsuri2"></td>
                    <td><input type="text" size="10"
                    name="localpart2"></td>
                    <td><input type="text" size="15"
                    name="classname2"></td>
                    <td><input type="text" size="15"
                    name="java2xml2"></td>
                    <td><input type="text" size="15"
                    name="xml2java2"></td>
                </tr>
                <tr>
                    <td><select name="encstyle3" size="1">
                        <option selected value="0">SOAP</option>
                        <option value="1">XMI</option>
                    </select></td>
                    <td><input type="text" size="15"
                    name="nsuri3"></td>
                    <td><input type="text" size="10"
                    name="localpart3"></td>
                    <td><input type="text" size="15"
                    name="classname3"></td>
                    <td><input type="text" size="15"
                    name="java2xml3"></td>
                    <td><input type="text" size="15"
                    name="xml2java3"></td>
                </tr>
                <tr>
                    <td><select name="encstyle4" size="1">
                        <option selected value="0">SOAP</option>
                        <option value="1">XMI</option>
                    </select></td>
                    <td><input type="text" size="15"
                    name="nsuri4"></td>
                    <td><input type="text" size="10"
                    name="localpart4"></td>
                    <td><input type="text" size="15"
                    name="classname4"></td>
                    <td><input type="text" size="15"
                    name="java2xml4"></td>
                    <td><input type="text" size="15"
                    name="xml2java4"></td>
                </tr>
                <tr>
                    <td><select name="encstyle5" size="1">
                        <option selected value="0">SOAP</option>
                        <option value="1">XMI</option>
                    </select></td>
                    <td><input type="text" size="15"
                    name="nsuri5"></td>
                    <td><input type="text" size="10"
                    name="localpart5"></td>
                    <td><input type="text" size="15"
                    name="classname5"></td>
                    <td><input type="text" size="15"
                    name="java2xml5"></td>
                    <td><input type="text" size="15"
                    name="xml2java5"></td>
                </tr>
                <tr>
                    <td><select name="encstyle6" size="1">
                        <option selected value="0">SOAP</option>
                        <option value="1">XMI</option>
                    </select></td>
                    <td><input type="text" size="15"
                    name="nsuri6"></td>
                    <td><input type="text" size="10"
                    name="localpart6"></td>
                    <td><input type="text" size="15"
                    name="classname6"></td>
                    <td><input type="text" size="15"
                    name="java2xml6"></td>
                    <td><input type="text" size="15"
                    name="xml2java6"></td>
                </tr>
                <tr>
                    <td><select name="encstyle7" size="1">
                        <option selected value="0">SOAP</option>
                        <option value="1">XMI</option>
                    </select></td>
                    <td><input type="text" size="15"
                    name="nsuri7"></td>
                    <td><input type="text" size="10"
                    name="localpart7"></td>
                    <td><input type="text" size="15"
                    name="classname7"></td>
                    <td><input type="text" size="15"
                    name="java2xml7"></td>
                    <td><input type="text" size="15"
                    name="xml2java7"></td>
                </tr>
                <tr>
                    <td><select name="encstyle8" size="1">
                        <option selected value="0">SOAP</option>
                        <option value="1">XMI</option>
                    </select></td>
                    <td><input type="text" size="15"
                    name="nsuri8"></td>
                    <td><input type="text" size="10"
                    name="localpart8"></td>
                    <td><input type="text" size="15"
                    name="classname8"></td>
                    <td><input type="text" size="15"
                    name="java2xml8"></td>
                    <td><input type="text" size="15"
                    name="xml2java8"></td>
                </tr>
                <tr>
                    <td><select name="encstyle9" size="1">
                        <option selected value="0">SOAP</option>
                        <option value="1">XMI</option>
                    </select></td>
                    <td><input type="text" size="15"
                    name="nsuri9"></td>
                    <td><input type="text" size="10"
                    name="localpart9"></td>
                    <td><input type="text" size="15"
                    name="classname9"></td>
                    <td><input type="text" size="15"
                    name="java2xml9"></td>
                    <td><input type="text" size="15"
                    name="xml2java9"></td>
                </tr>
                <tr>
                    <td><select name="encstyle10" size="1">
                        <option selected value="0">SOAP</option>
                        <option value="1">XMI</option>
                    </select></td>
                    <td><input type="text" size="15"
                    name="nsuri10"></td>
                    <td><input type="text" size="10"
                    name="localpart10"></td>
                    <td><input type="text" size="15"
                    name="classname10"></td>
                    <td><input type="text" size="15"
                    name="java2xml10"></td>
                    <td><input type="text" size="15"
                    name="xml2java10"></td>
                </tr>
            </table>
            </center></div></td>
        </tr>
	<tr><td>Default Mapping Registry Class</td><td><input type="text" size="60" name="defaultSMR"></td></tr>
    </table>
    <p><input type="submit" value="Deploy" /></p>
</form>

<%
} else {
  String id = request.getParameter ("id");
  DeploymentDescriptor dd = new DeploymentDescriptor ();
  dd.setID (id);

  // get the provider info
  int scope = Integer.parseInt (request.getParameter ("scope"));
  String providerTypeStr = request.getParameter ("providerType");
  String className = request.getParameter ("providerClass");
  boolean isStatic = request.getParameter ("isStatic").equals ("true");
  String scriptLang = request.getParameter ("scriptLanguage");
  String userClass = request.getParameter ("userProviderClassString");

  if (scriptLang.equals ("other")) {
    scriptLang = request.getParameter ("scriptLanguageTypeIn");
  }
  String scriptFilename = request.getParameter ("scriptFilename");
  String script = request.getParameter ("script");
  String methodsStr = request.getParameter ("methods");
  StringTokenizer st = new StringTokenizer (methodsStr);
  int nTokens = st.countTokens ();
  String[] methods = new String[nTokens];
  for (int i = 0; i < nTokens; i++) {
    methods[i] = st.nextToken ();
  }

  dd.setScope (scope);
  dd.setMethods (methods);

  if (providerTypeStr.equals ("0")) {
    dd.setProviderType (DeploymentDescriptor.PROVIDER_JAVA);
    dd.setProviderClass (className);
    dd.setIsStatic (isStatic);
  } else {
	if (providerTypeStr.equals("3")) {
		dd.setProviderType (DeploymentDescriptor.PROVIDER_USER_DEFINED);
		dd.setServiceClass(userClass);
		dd.setProviderClass (className);
		dd.setIsStatic (isStatic);

		// get any options
		int optnum = 0;


		try {
		    optnum = Integer.parseInt (request.getParameter ("noOpts"));
		} catch (NumberFormatException e) {
			optnum = 0;
		}

		if (optnum != 0) {
		
			Hashtable optionsTble = new Hashtable();
		
			for (int j = 1; j <= optnum; j++) { 
			      String keyS= request.getParameter ("optionkey" + j);
			      String valueS= request.getParameter ("optionvalue" + j);
				optionsTble.put(keyS, valueS);
			}
			dd.setProps(optionsTble);
    		}

	
   	} else {
    		if (!scriptFilename.equals ("")) { // filename specified
      			dd.setProviderType (DeploymentDescriptor.PROVIDER_SCRIPT_FILE);
    		} else { // there better be a script to run
      			dd.setProviderType (DeploymentDescriptor.PROVIDER_SCRIPT_STRING);
    		}
    		dd.setScriptLanguage (scriptLang);
    		dd.setScriptFilenameOrString (scriptFilename);
  	}
  }
  String[] encs = {org.apache.soap.Constants.NS_URI_SOAP_ENC,
       org.apache.soap.Constants.NS_URI_XMI_ENC};

  // set up any type mappings
  int nmaps = 0;

  try {
    nmaps = Integer.parseInt (request.getParameter ("nmaps"));
  } catch (NumberFormatException e) {
  }

  if (nmaps != 0) {
    TypeMapping[] mappings = new TypeMapping[nmaps];
    for (int i = 1; i <= nmaps; i++) { // the form is hard-coded to a max of 10
      int encStyle = Integer.parseInt (request.getParameter ("encstyle" + i));
      String nsuri = request.getParameter ("nsuri" + i);
      String localPart = request.getParameter ("localpart" + i);
      String classNameStr = request.getParameter ("classname" + i);
      String java2XMLClass = request.getParameter ("java2xml" + i);
      String xml2JavaClass = request.getParameter ("xml2java" + i);
      QName elementType = (nsuri.equals ("") || localPart.equals ("")) 
                          ? null : new QName (nsuri, localPart);
      // map "" to null (the unfilled params come as empty strings because
      // they are infact actual parameters)
      classNameStr = classNameStr.equals ("") ? null : classNameStr;
      java2XMLClass = java2XMLClass.equals ("") ? null : java2XMLClass;
      xml2JavaClass = xml2JavaClass.equals ("") ? null : xml2JavaClass;
      mappings[i-1] = new TypeMapping (encs[encStyle], elementType,
				       classNameStr, java2XMLClass,
				       xml2JavaClass);
    }
    dd.setMappings (mappings);
  }

  String defaultSMR = request.getParameter("defaultSMR");
  if (defaultSMR != null) {
	dd.setDefaultSMRClass(defaultSMR);
  }
  // ok now deploy it
  serviceManager.deploy (dd);

  // show what was deployed
  out.println ("<p>Service <b>" + dd.getID () + "</b> deployed.</p>");
}
%>
