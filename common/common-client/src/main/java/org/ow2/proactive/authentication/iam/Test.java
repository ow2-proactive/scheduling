package org.ow2.proactive.authentication.iam;

/**
 * Created by nebil on 01/10/18.
 */
public class Test {


    public static void main(String [] args){

        String jwt = "eyJhbGciOiJIUzUxMiJ9.ZXlKNmFYQWlPaUpFUlVZaUxDSmhiR2NpT2lKa2FYSWlMQ0psYm1NaU9pSkJNVEk0UTBKRExVaFR" +
                "NalUySW4wLi5laVllRUtfSjB1dHo3dHlCcVdac1JnLnRYbTY0Q08yZ0FxMWtNT0dtVXBYNFVEMDdmZ2N3b0dKdHdZdkVWTXZEaUx2" +
                "QlVsbmsyYzB2eFd1WVBCTkQ1M3QzTXVBdGVNWk05a3Y2R0ZyVHRGSUpCR0hjZV9mTFMtT245UEZCS2d4RHlKVV9KeFZwaU1peVFLY" +
                "mdUaEhSa0JySG8xZ09hTGZjaW9aWDJGaFFvcS1uOVQxM0NzMngxcDV5amFFaTc4UUxlTmhfWm9QQ0cxY1Z1MlR5SWlXeTFiTzdINE" +
                "FjNUN5NjVxcjlyXzI5S2pzWVFWWWZYczZLejhYMWhnTW56UURrMlNkNG9YR0liVTRGakh5d0JiZHB0VTAwU21la1NmQVJFc3FxZ2l" +
                "sQXFhdTJDNW1mOV81Q1ZRMkRxUWoxVi1Tc21QUE9TeEZubE04MWdXU0Rmak93UzM5TVNEYldabXlPNTVnOVFXMktJMS0tSWFaTXBM" +
                "ZkplQ0hvZXJvZEFMVndwRUcyNWNVNzdQZVJySEtFTGYtYWJIdEhFQmpsbDVtV3hyMGo3ZlZiNG81VFcxTDZHV01HSHoyQllrV0tZa" +
                "lY4UmRuZzdlczRGYTY0ZkZnMldZWi1hT2FzSDdtMmhldDdNdzMweERUMmdCWjFWWTdxeXN6X1ZjNVpUSkhWQ0R6eTF4ZzY5Vjhycm" +
                "U1SzhiVTkzUXphNUxwZmdDTVZHNl9feEE2R3A1VUZwQzY0V1BacFZndVd4NEt6VzZrQ1BZelpHRGtJYWk3OXhBaVBTX3hhSGNfT0N" +
                "aNU91T2lReGJQTkZfMi00aDNyUEl1emJKOG9HMmpITmp5S3huWlJTSjRGN1g1bHRCb2pnWWdpb3d1RFBCX00zbGQzaHp5OFY3ak1Z" +
                "YUsyRVpnRHB2OFFRLjMtc2FHaGZmZ0YwTDVSY294MlZScUE=.MCencKzyb0iFhyeLkYrwW3az8tQm3Q5bD40BQC2HfaBvtTJQe6GJ" +
                "YSFym9TM92QiBNmtIiOpmTrEnH5j_JmbjQ";

        String encryptionKey = "3OxZlzUq9v1jfox3PSSDGZk7RNfHL9ENcpLoULWSCj4";
        String signatureKey = "_pyp8dL5rHqLXiS6aBzvbJ_LZdBN3-oQqbJVbC5mveTdsZ1r3WM_j3SUENB65XS7EAu7Kop3rfQx81GS_mu1Yw";

        JWTUtils.parseJWT(jwt,true, signatureKey, true, encryptionKey,"https://activeeon:8444/iam", "https://activeeon:8444/iam");

        String json = "{\"credentialType\":\"UsernamePasswordCredential\"," +
                "\"aud\":\"https:\\/\\/activeeon:8444\\/iam\"," +
                "\"sub\":\"user\",\"role\":\"user\"," +
                "\"credential\":\"eyJhbGciOiJIUzUxMiJ9.ZXlKNmFYQWlPaUpFUlVZaUxDSmhiR2NpT2lKa2FYSWlMQ0psYm1NaU9pSkJNVEk0UTBKRExVaFRNalUySW4wLi5FbTJpU1hBbEx5VFVxWFJLYklfY213LkV4VE44TXV3U2d6S0tRN3ZxQmxSd0EuTjdzVEs3cktHQ0RTOUVpamlrUkc2QQ==.LI0SOBCF8MFbyW74phUytIYxWpSZ6aTBRtToQfdATHz05sK8bggaqDrn1Ksu_NkjJMMJ2Ho7Ori_EU0bjtImlw\"," +
                "\"authenticationMethod\":\"JsonResourceAuthenticationHandler\"," +
                "\"successfulAuthenticationHandlers\":[\"JsonResourceAuthenticationHandler\"]," +
                "\"iss\":\"https:\\/\\/activeeon:8444\\/iam\",\"cn\":\"user\",\"exp\":1538391151,\"iat\":1538387551," +
                "\"jti\":\"TGT-5-HUDS6LXzfOjpm-Vfw0C5H21HGAC3f-ZPt-RUQ3oeW6xbefCuxSqta5IKDNn63agT0p8activeeon\"}";
    }
}
