/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.authentication.iam;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.apache.commons.codec.binary.Base64;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.JoseException;


public class JWTUtils {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(JWTUtils.class.getName());

    private JWTUtils() {

    }

    /**
     * Parse secured JWT (Json Web Token). If successful, returns the set of claims extracted from the token body.
     *
     * @param jwt JWT (Json Web Token) to parse
     * @param jwtSigned boolean variable indicating whether the token is signed.
     * @param jwtSignatureKey Key used to sign the token.
     * @param jwtEncrypted boolean variable indicating whether the token is encrypted.
     * @param jwtEncryptionKey Key used to encrypt the token.
     * @param issuer service that issued the JWT.
     * @param service service to which the JWT is intended for.
     *
     * @return the set of claims extracted from the token body, or empty claims if the token is invalid.
     */
    public static JwtClaims parseJWT(String jwt, boolean jwtSigned, String jwtSignatureKey, boolean jwtEncrypted,
            String jwtEncryptionKey, String issuer, String service) {

        return validatePlainJWT(decypherJWT(jwt, jwtSigned, jwtSignatureKey, jwtEncrypted, jwtEncryptionKey),
                                issuer,
                                service);
    }

    /**
     * Decypher secured JWT (Json Web Token). If successful, returns plain content of the token.
     *
     * @param jwt JWT (Json Web Token) to parse
     * @param jwtSigned boolean variable indicating whether the token is signed.
     * @param jwtSignatureKey Key used to sign the token.
     * @param jwtEncrypted boolean variable indicating whether the token is encrypted.
     * @param jwtEncryptionKey Key used to encrypt the token.
     *
     * @return plain content of the token.
     */
    public static String decypherJWT(String jwt, boolean jwtSigned, String jwtSignatureKey, boolean jwtEncrypted,
            String jwtEncryptionKey) {
        if (jwtSigned) {
            jwt = verifySignature(jwt, jwtSignatureKey);
        }

        if (jwtEncrypted) {

            if (!jwtSigned) {
                jwt = decodeJWT(jwt);
                jwt = jwt.replaceFirst("\\{\"alg\":.+\\}", "");
            }

            jwt = decryptJWT(jwt, jwtEncryptionKey);
        }

        LOG.debug("Plain JWT: " + jwt);

        return jwt;
    }

    /**
     * Parse signed JWT (Json Web Token).
     *
     * @param jwt JWT (Json Web Token) to parse
     * @param jwtSignatureKey Key used to sign the token
     *
     * @return JsonWebSignature used to verify the key
     */
    private static String verifySignature(String jwt, String jwtSignatureKey) {

        Key key = new AesKey(jwtSignatureKey.getBytes(StandardCharsets.UTF_8));

        try {
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(jwt);
            jws.setKey(key);

            if (!jws.verifySignature()) {
                throw new IAMException("JWT signature verification failed. Used signature key: " + jwtSignatureKey);
            }

            return jws.getPayload();

        } catch (JoseException je) {
            throw new IAMException("JWT signature verification failed", je);
        }
    }

    /**
     * Parse signed JWT (Json Web Token).
     * @param jwt JWT (Json Web Token) to decode.
     *
     * @return Base-64 decoded Json Web Token.
     */
    private static String decodeJWT(String jwt) {

        byte[] decodedBytes = Base64.decodeBase64(jwt.getBytes(StandardCharsets.UTF_8));
        return new String(decodedBytes, StandardCharsets.UTF_8);

    }

    /**
     * Parse encrypted JWT (Json Web Token).
     *
     * @param jwt encrypted JWT (Json Web Token) to parse.
     * @param jwtEncryptionKey Key used to encrypt the token.
     *
     * @return plain text (decrypted) Json Web Token.
     */
    private static String decryptJWT(String jwt, String jwtEncryptionKey) {
        JsonWebEncryption jwe = new JsonWebEncryption();

        try {
            JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk("\n" + "{\"kty\":\"oct\",\n" + " \"k\":\"" +
                                                              jwtEncryptionKey + "\"\n" + "}");
            jwe.setCompactSerialization(jwt);

            jwe.setKey(new AesKey(jsonWebKey.getKey().getEncoded()));

            return jwe.getPlaintextString();

        } catch (JoseException je) {
            throw new IAMException("Decrypting secured JWT failed. Used encryption key: " + jwtEncryptionKey, je);
        }
    }

    /**
     * Parses plain text JWT (Json Web Token). If successful, returns the set of claims extracted from the token body.
     * If unsuccessful (token is invalid or not containing all required properties), simply returns empty claims.
     *
     * @param jwt  Plain text JWT (Json Web Token) to parse
     * @param issuer service that issued the JWT.
     * @param service service to which the JWT is intended for.
     *
     * @return the set of claims extracted from the token body, or empty claims if the token is invalid.
     */
    private static JwtClaims validatePlainJWT(String jwt, String issuer, String service) {

        try {

            JsonWebSignature jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.NONE);
            jws.setAlgorithmConstraints(AlgorithmConstraints.ALLOW_ONLY_NONE);
            jws.setPayload(jwt);

            JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime() // the JWT must have an expiration time
                                                              .setRequireSubject() // the JWT must have a subject claim
                                                              .setExpectedIssuer(issuer) // the JWT needs to have been issued by
                                                              .setExpectedAudience(service) // the JWT is intended for
                                                              .setRequireIssuedAt() //the JWT must have a 'iat' claim
                                                              .setDisableRequireSignature()
                                                              .setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                                                                                                                   AlgorithmIdentifiers.NONE))
                                                              .build(); // create the JwtConsumer instance

            //  Validate the JWT and return the resulting Claims
            return jwtConsumer.processToClaims(jws.getCompactSerialization());

        } catch (JoseException je) {
            throw new IAMException("Building JWT from Json failed", je);

        } catch (InvalidJwtException e) {
            throw new IAMException("Invalid JWT error", e);
        }
    }
}
