package functionaltests.schedulerdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.ow2.proactive.authentication.crypto.HybridEncryptionUtil.HybridEncryptedData;


public class TestThirdPartyCredentials extends BaseSchedulerDBTest {

    @Test
    public void crud_operations() throws Exception {

        HybridEncryptedData superPassword = new HybridEncryptedData("superkey".getBytes(), "superpassword"
                .getBytes());
        dbManager.putThirdPartyCredential("existing_user", "existing_key", superPassword);

        assertEquals(Collections.singleton("existing_key"), dbManager
                .thirdPartyCredentialsKeySet("existing_user"));
        assertEquals(Collections.<String> emptySet(), dbManager
                .thirdPartyCredentialsKeySet("non_existing_user"));

        HybridEncryptedData superPassword2 = new HybridEncryptedData(new byte[0], "superpassword_2"
                .getBytes());
        dbManager.putThirdPartyCredential("existing_user", "existing_key_2", superPassword2);

        assertArrayEquals("superkey".getBytes(), dbManager.thirdPartyCredentialsMap("existing_user").get(
                "existing_key").getEncryptedSymmetricKey());
        assertArrayEquals("superpassword".getBytes(), dbManager.thirdPartyCredentialsMap("existing_user")
                .get("existing_key").getEncryptedData());
        assertArrayEquals("superpassword_2".getBytes(), dbManager.thirdPartyCredentialsMap("existing_user")
                .get("existing_key_2").getEncryptedData());

        assertEquals(new HashSet<String>(Arrays.asList("existing_key", "existing_key_2")), dbManager
                .thirdPartyCredentialsKeySet("existing_user"));
        assertEquals(Collections.<String> emptySet(), dbManager
                .thirdPartyCredentialsKeySet("non_existing_user"));

        dbManager.removeThirdPartyCredential("existing_user", "non_existing_key");
        dbManager.removeThirdPartyCredential("non_existing_user", "non_existing_key");
        assertEquals(new HashSet<String>(Arrays.asList("existing_key", "existing_key_2")), dbManager
                .thirdPartyCredentialsKeySet("existing_user"));

        dbManager.removeThirdPartyCredential("existing_user", "existing_key");
        assertEquals(Collections.singleton("existing_key_2"), dbManager
                .thirdPartyCredentialsKeySet("existing_user"));

        dbManager.removeThirdPartyCredential("existing_user", "existing_key_2");
        assertEquals(Collections.<String> emptySet(), dbManager.thirdPartyCredentialsKeySet("existing_user"));

    }

    @Test
    public void value_updates() throws Exception {
        HybridEncryptedData superPassword = new HybridEncryptedData(new byte[0], "superpassword".getBytes());
        dbManager.putThirdPartyCredential("existing_user", "existing_key", superPassword);

        HybridEncryptedData superPasswordUpdated = new HybridEncryptedData(new byte[0],
            "superpassword_updated".getBytes());
        dbManager.putThirdPartyCredential("existing_user", "existing_key", superPasswordUpdated);
        assertArrayEquals("superpassword_updated".getBytes(), dbManager.thirdPartyCredentialsMap(
                "existing_user").get("existing_key").getEncryptedData());

        dbManager.putThirdPartyCredential("existing_user", "existing_key", superPasswordUpdated);
        assertArrayEquals("superpassword_updated".getBytes(), dbManager.thirdPartyCredentialsMap(
                "existing_user").get("existing_key").getEncryptedData());
    }

    @Test
    public void long_credential_value() throws Exception {
        String longString = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEowIBAAKCAQEAxO0WhOyc4qv5aSNi8MMrDJOo3qmkpbkcOSvy4FJPOq4ZfnJ3\n"
            + "w3zWocmdXbr6d3HKOYiJywGHf69Vah/9nbren4y17FW3UabRmPMIYAgbtvS8kf8z\n"
            + "+WHCyYEm9eqZf7AWd3guMrbx2bzfOUVKmYNsxB04qzGwmSjQeE7YhAg45oBYwRY+\n"
            + "QaD0XDcNsgJwDaYVU/D3Bc4G6tsiYoPSBYQ+10zEoT4S/3LduRsUWlxasm3eP8ED\n"
            + "ougAIXjmScEkwjv/RedIa06WwIoE5Ci5HtsSngBdioaLFZOMO/e+BuN5pp3gny8g\n"
            + "9EhQREd9Tx7s31yxt4MDvULimWFx1l45sVmkMwIDAQABAoIBAQCKGp+FXw7zZJoI\n"
            + "Yvm7UZQ6QL/YT+6ZDoW9jpXJPdA0ne5hIFPfdAht9B/5oOyQoeuph5jjFtJ4+HSV\n"
            + "dZP+bxQ7nonjEYX7rFsnwaEo/+a321D3rps7lJTvjjTNl9ZIlyxaYp07kdNw2SVP\n"
            + "W8nieSnpK3kXjkSEVPxGszzi84U8GKDkDgXt3QdPzeN9ceFA3x06euvPFULMw7yV\n"
            + "VGZGRE13hIOBhXRcH9jKxguJbheFUVFHf4gu5BrYsEK0yLLoFFobTuXLjtA5EGLH\n"
            + "6OVF1M9873C07n9zUWUTRIPeW3rmh+OjKHS3nz0lryNRQKuNxP7bR5Kdy32www3S\n"
            + "rmCYwcWhAoGBAPFse5Kqe+wRtXSGKtKimgRh8xeLZA2MwjTI7513Va/xR9XjLjrL\n"
            + "s9kknEkRIf9jAdwFehPxiBProActiveSchedulingGhfRoHoFAFVqrs/oHScMfO5\n"
            + "lIUFkgojGVPXwvIcilPTWzWaD4KUDY7QSUjTfmPS/wtBgtk2cuEd0t1DAoGBANDQ\n"
            + "1isgPk7L2D4fh9aeA/6qwFcZInN+cdEdaIoHKc77TsR/86OXQIIg7O6uLI39JMbo\n"
            + "p//11r5vm5ubjbAnykYJL+t5B+jWieTvngE4tEIJhcgqoSRya/DKL9ARn2tTjftm\n"
            + "Hpxp2okHM5+r0q80c989PwD4IMO65h4wEAt3HLZRAoGAITaDaZH6qmdlRzqN+ZxV\n"
            + "A/VVtA+BHDwZG5npHQilySawc0Rlv8D2ZREcTxEEVFYSk2pNeSDpTx/NUSarqR8E\n"
            + "GukR7z9xH4CfIpAC8IwQiOIf+OrkFFubixFRHgPmIBq2vwgeH5ocGiuvpo8nrlYJ\n"
            + "PvOZl7IXVD0W+zr6Yu3vbHECgYAjNgPXM9Gt4cut9g0m0HBmAg764N8hUIIKvAXD\n"
            + "uJ+BKnlGwzinLjsPdlPdj3st2jDYZaTmkWLLq/A2Vg2XVa5TDvuInlkKFxsbgphH\n"
            + "JnOm6wonDaEsjyrKaJ2VXVNferBnYvnocCUMlC1NUGDvcE3Vp/M2y6BiwOJK1tnt\n"
            + "xQEPcQKBgFf7A3BlBLAOfnSFr1JUW5LqhNOwangXLAyMkLtMlL4QemGj+yhwzE/N\n"
            + "YOoInV75eaD8In57HQlwgbRIazyJ9b8gDensPlDFlVAQ98ffOb42gR11QRinQ6PL\n"
            + "VYXdGf7hRbfCSUqDDEYoJI18q8H0yomBE3pMoRRiuGX3A/YW6wyT\n" + "-----END RSA PRIVATE KEY-----";
        HybridEncryptedData longPassword = new HybridEncryptedData(new byte[0], longString.getBytes());

        dbManager.putThirdPartyCredential("existing_user", "long_credential", longPassword);

        assertArrayEquals(longString.getBytes(), dbManager.thirdPartyCredentialsMap("existing_user").get(
                "long_credential").getEncryptedData());
    }
}
