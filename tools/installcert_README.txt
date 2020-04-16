InstallCert tool to import a server certificate to the java truststore

See https://github.com/escline/InstallCert

In can be used, for example, to import a ldap server certificate.

In order to use it, run the following commands

cd PROACTIVE_DIR
jre/bin/java -jar tools/installcert-usn-20140115.jar <server_ipaddress_or_hostname>:<port>

This will update the jre/lib/security/jssecacerts keystore and also create a extracerts keystore in PROACTIVE_DIR
with default password "changeit"
this keystore can be used, for example, as truststore in the ldap configuration file

